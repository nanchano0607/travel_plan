import { useEffect, useRef, useState } from 'react'

const GOOGLE_MAPS_SCRIPT_ID = 'google-maps-places-script'

function loadGoogleMapsScript(apiKey) {
  if (window.google?.maps?.places) {
    return Promise.resolve()
  }

  const existingScript = document.getElementById(GOOGLE_MAPS_SCRIPT_ID)
  if (existingScript) {
    return new Promise((resolve, reject) => {
      existingScript.addEventListener('load', resolve, { once: true })
      existingScript.addEventListener('error', reject, { once: true })
    })
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.id = GOOGLE_MAPS_SCRIPT_ID
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&language=ko`
    script.async = true
    script.defer = true
    script.addEventListener('load', resolve, { once: true })
    script.addEventListener('error', reject, { once: true })
    document.head.appendChild(script)
  })
}

function RegionAutocomplete({ value, onSelect, onClear }) {
  const inputRef = useRef(null)
  const autocompleteRef = useRef(null)
  const [status, setStatus] = useState('')

  useEffect(() => {
    const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

    if (!apiKey) {
      setStatus('프론트 .env에 VITE_GOOGLE_MAPS_API_KEY를 설정하세요.')
      return
    }

    let mounted = true

    loadGoogleMapsScript(apiKey)
      .then(() => {
        if (!mounted || !inputRef.current) return

        const autocomplete = new window.google.maps.places.Autocomplete(inputRef.current, {
          fields: ['place_id', 'name', 'formatted_address', 'geometry', 'types'],
          types: ['(regions)'],
        })

        autocompleteRef.current = autocomplete
        autocomplete.addListener('place_changed', () => {
          const place = autocomplete.getPlace()
          const location = place.geometry?.location

          if (!place.place_id || !location) {
            setStatus('자동완성 목록에서 지역을 선택하세요.')
            onClear()
            return
          }

          const region = {
            regionName: place.name || place.formatted_address,
            regionId: place.place_id,
            regionAddress: place.formatted_address || '',
            regionLatitude: Number(location.lat().toFixed(8)),
            regionLongitude: Number(location.lng().toFixed(8)),
            regionTypes: place.types || [],
          }

          console.log('[RegionAutocomplete] selected place', place)
          console.log('[RegionAutocomplete] region payload', region)

          setStatus('')
          onSelect(region)
        })
      })
      .catch(() => {
        if (mounted) setStatus('Google 지역 자동완성을 불러오지 못했습니다.')
      })

    return () => {
      mounted = false
      if (autocompleteRef.current) {
        window.google?.maps?.event?.clearInstanceListeners(autocompleteRef.current)
      }
    }
  }, [onClear, onSelect])

  return (
    <div className="region-autocomplete">
      <input
        ref={inputRef}
        defaultValue={value}
        placeholder="예: 도쿄, 서울, 제주도"
        onChange={onClear}
      />
      {status && <small className="field-help error">{status}</small>}
    </div>
  )
}

export default RegionAutocomplete
