import { useEffect, useMemo, useRef, useState } from 'react'

const GOOGLE_MAPS_SCRIPT_ID = 'google-maps-places-script'
const DEFAULT_CENTER = { lat: 37.5665, lng: 126.978 }

function loadGoogleMapsScript(apiKey) {
  if (window.google?.maps) {
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

function GoogleMapView({ planItems }) {
  const mapContainerRef = useRef(null)
  const mapRef = useRef(null)
  const markersRef = useRef([])
  const polylineRef = useRef(null)
  const infoWindowRef = useRef(null)
  const [status, setStatus] = useState('')

  const orderedItems = useMemo(() => {
    return planItems
      .filter((item) => Number.isFinite(Number(item.latitude)) && Number.isFinite(Number(item.longitude)))
      .slice()
      .sort((a, b) => a.sequence - b.sequence)
  }, [planItems])

  useEffect(() => {
    const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

    if (!apiKey) {
      setStatus('프론트 .env에 VITE_GOOGLE_MAPS_API_KEY를 설정하세요.')
      return
    }

    let mounted = true

    loadGoogleMapsScript(apiKey)
      .then(() => {
        if (!mounted || !mapContainerRef.current) return

        if (!mapRef.current) {
          mapRef.current = new window.google.maps.Map(mapContainerRef.current, {
            center: DEFAULT_CENTER,
            zoom: 12,
            mapTypeControl: false,
            fullscreenControl: false,
            streetViewControl: false,
          })
          infoWindowRef.current = new window.google.maps.InfoWindow()
        }

        setStatus('')
      })
      .catch(() => {
        if (mounted) setStatus('Google 지도를 불러오지 못했습니다.')
      })

    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    if (!mapRef.current || !window.google?.maps) return

    markersRef.current.forEach((marker) => marker.setMap(null))
    markersRef.current = []

    if (polylineRef.current) {
      polylineRef.current.setMap(null)
      polylineRef.current = null
    }

    if (orderedItems.length === 0) {
      mapRef.current.setCenter(DEFAULT_CENTER)
      mapRef.current.setZoom(12)
      return
    }

    const bounds = new window.google.maps.LatLngBounds()
    const path = orderedItems.map((item) => ({
      lat: Number(item.latitude),
      lng: Number(item.longitude),
    }))

    orderedItems.forEach((item) => {
      const position = {
        lat: Number(item.latitude),
        lng: Number(item.longitude),
      }

      bounds.extend(position)

      const marker = new window.google.maps.Marker({
        map: mapRef.current,
        position,
        label: {
          text: String(item.sequence),
          color: '#ffffff',
          fontWeight: '700',
        },
        title: item.placeName,
      })

      marker.addListener('click', () => {
        infoWindowRef.current?.setContent(`
          <div style="min-width:150px">
            <strong>${escapeHtml(item.sequence)}. ${escapeHtml(item.placeName)}</strong>
            <div style="margin-top:4px;color:#667;font-size:12px">${escapeHtml(item.placeId || '')}</div>
          </div>
        `)
        infoWindowRef.current?.open(mapRef.current, marker)
      })

      markersRef.current.push(marker)
    })

    if (path.length >= 2) {
      polylineRef.current = new window.google.maps.Polyline({
        map: mapRef.current,
        path,
        strokeColor: '#176b65',
        strokeOpacity: 0.85,
        strokeWeight: 4,
      })
    }

    if (path.length === 1) {
      mapRef.current.setCenter(path[0])
      mapRef.current.setZoom(14)
    } else {
      mapRef.current.fitBounds(bounds, 48)
    }
  }, [orderedItems])

  return (
    <div className="map-panel">
      <div ref={mapContainerRef} className="map-canvas" />
      {status && <small className="field-help error">{status}</small>}
      {!status && orderedItems.length === 0 && (
        <small className="map-empty">좌표가 있는 장소가 없습니다.</small>
      )}
    </div>
  )
}

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}

export default GoogleMapView
