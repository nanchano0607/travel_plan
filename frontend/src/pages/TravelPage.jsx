import { useCallback, useEffect, useMemo, useState } from 'react'

import GoogleMapView from '../components/GoogleMapView.jsx'
import PageHeader from '../components/PageHeader.jsx'
import PlaceAutocomplete from '../components/PlaceAutocomplete.jsx'
import RegionAutocomplete from '../components/RegionAutocomplete.jsx'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

function TravelPage() {
  const [region, setRegion] = useState(null)
  const [form, setForm] = useState({
    startDate: '2026-07-10',
    endDate: '2026-07-11',
    headcount: 2,
    budget: 0,
  })
  const [draftPlan, setDraftPlan] = useState(null)
  const [savedPlan, setSavedPlan] = useState(null)
  const [editingKey, setEditingKey] = useState('')
  const [selectedMapDay, setSelectedMapDay] = useState('')
  const [status, setStatus] = useState({ type: 'idle', message: '' })
  const [isGenerating, setIsGenerating] = useState(false)
  const [isSaving, setIsSaving] = useState(false)

  const currentPlan = savedPlan || draftPlan
  const planItemsByDay = useMemo(() => groupPlanItemsByDay(currentPlan?.planItems || []), [currentPlan])
  const selectedDayItems = useMemo(() => {
    return planItemsByDay.find(([dayNumber]) => String(dayNumber) === selectedMapDay)?.[1] || []
  }, [planItemsByDay, selectedMapDay])

  useEffect(() => {
    if (planItemsByDay.length === 0) {
      setSelectedMapDay('')
      return
    }

    const hasSelectedDay = planItemsByDay.some(([dayNumber]) => String(dayNumber) === selectedMapDay)
    if (!hasSelectedDay) {
      setSelectedMapDay(String(planItemsByDay[0][0]))
    }
  }, [planItemsByDay, selectedMapDay])

  const clearRegion = useCallback(() => {
    setRegion(null)
    setDraftPlan(null)
    setSavedPlan(null)
    setSelectedMapDay('')
  }, [])

  const selectRegion = useCallback((selectedRegion) => {
    setRegion(selectedRegion)
    setDraftPlan(null)
    setSavedPlan(null)
    setSelectedMapDay('')
    setStatus({ type: 'idle', message: '' })
  }, [])

  const updateForm = (field) => (event) => {
    const value = field === 'headcount' || field === 'budget'
      ? Number(event.target.value)
      : event.target.value
    setForm((previous) => ({ ...previous, [field]: value }))
  }

  const generatePlan = async () => {
    if (!region?.regionId || isGenerating) return

    setIsGenerating(true)
    setSavedPlan(null)
    setStatus({ type: 'loading', message: 'AI가 일정 초안을 생성하고 있습니다.' })

    try {
      const result = await requestApi('/api/plan/ai', {
        method: 'POST',
        body: {
          regionName: region.regionName,
          regionId: region.regionId,
          latitude: region.regionLatitude,
          longitude: region.regionLongitude,
          startDate: form.startDate,
          endDate: form.endDate,
          headcount: Number(form.headcount),
        },
      })

      setDraftPlan({
        ...result.data,
        budget: Number(form.budget) || 0,
      })
      setStatus({ type: 'success', message: 'AI 일정 초안이 생성되었습니다.' })
    } catch (error) {
      setDraftPlan(null)
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsGenerating(false)
    }
  }

  const savePlan = async () => {
    if (!draftPlan || isSaving || savedPlan) return

    setIsSaving(true)
    setStatus({ type: 'loading', message: '여행 계획을 저장하고 있습니다.' })

    try {
      const result = await requestApi('/api/plan', {
        method: 'POST',
        body: {
          ...draftPlan,
          budget: Number(form.budget) || 0,
          headcount: Number(draftPlan.headcount) || Number(form.headcount),
          planItems: draftPlan.planItems || [],
        },
      })

      setSavedPlan(result.data)
      setStatus({ type: 'success', message: `저장되었습니다. planId: ${result.data.planId}` })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsSaving(false)
    }
  }

  const updatePlanItemPlace = useCallback(async (targetItem, selectedPlace) => {
    const updatedItem = {
      placeName: selectedPlace.regionName,
      placeId: selectedPlace.regionId,
      latitude: selectedPlace.latitude,
      longitude: selectedPlace.longitude,
    }

    if (!savedPlan?.planId) {
      setDraftPlan((previous) => replacePlanItem(previous, targetItem, updatedItem))
      setEditingKey('')
      setStatus({ type: 'success', message: '초안 장소가 수정되었습니다.' })
      return
    }

    setStatus({ type: 'loading', message: '저장된 일정 항목을 수정하고 있습니다.' })

    try {
      const result = await requestApi(
        `/api/plan/${savedPlan.planId}/items/${targetItem.dayNumber}/${targetItem.sequence}`,
        {
          method: 'PATCH',
          body: selectedPlace,
        },
      )

      setSavedPlan((previous) => replacePlanItem(previous, targetItem, result.data))
      setDraftPlan((previous) => replacePlanItem(previous, targetItem, result.data))
      setEditingKey('')
      setStatus({ type: 'success', message: '저장된 일정 항목이 수정되었습니다.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }, [savedPlan])

  return (
    <>
      <PageHeader
        eyebrow="AI TRAVEL"
        title="AI 여행 계획 생성 · 수정"
        description="지역 선택, 일정 초안 생성, 장소 수정, 최종 저장까지 한 화면에서 진행합니다."
      />
      <div className="travel-layout">
        <section className="condition-card">
          <h2>여행 조건 입력</h2>
          <p>Google 지역 자동완성으로 대표 지역을 선택합니다.</p>

          <label>
            여행 지역
            <RegionAutocomplete
              value={region?.regionName || ''}
              onSelect={selectRegion}
              onClear={clearRegion}
            />
          </label>

          {region && (
            <div className="selected-region">
              <span>선택된 지역</span>
              <strong>{region.regionName}</strong>
              <small>{region.regionAddress}</small>
              <code>{region.regionId}</code>
              <small>
                {region.regionLatitude}, {region.regionLongitude}
              </small>
            </div>
          )}

          <div className="field-row">
            <label>
              시작일
              <input type="date" value={form.startDate} onChange={updateForm('startDate')} />
            </label>
            <label>
              종료일
              <input type="date" value={form.endDate} onChange={updateForm('endDate')} />
            </label>
          </div>

          <div className="field-row">
            <label>
              인원
              <input min="1" type="number" value={form.headcount} onChange={updateForm('headcount')} />
            </label>
            <label>
              예산
              <input min="0" type="number" value={form.budget} onChange={updateForm('budget')} />
            </label>
          </div>

          <button
            className="primary wide"
            disabled={!region?.regionId || isGenerating}
            onClick={generatePlan}
            type="button"
          >
            {isGenerating ? 'AI 일정 생성 중' : 'AI 일정 생성하기'}
          </button>

          {status.message && (
            <div className={`travel-status ${status.type}`}>
              {status.message}
            </div>
          )}
        </section>

        <section className="timeline-card">
          <div className="timeline-head">
            <div>
              <span>{savedPlan ? `PLAN ${savedPlan.planId}` : draftPlan ? 'DRAFT' : 'READY'}</span>
              <h2>{currentPlan ? currentPlan.title : '지역을 먼저 선택하세요'}</h2>
            </div>
          </div>

          {!currentPlan && (
            <div className="empty-timeline">
              <strong>생성된 일정이 없습니다.</strong>
              <small>여행 지역과 날짜를 선택한 뒤 AI 일정 생성을 실행하세요.</small>
            </div>
          )}

          {currentPlan && (
            <div className="map-section">
              <div className="day-tabs">
                {planItemsByDay.map(([dayNumber, planItems]) => (
                  <button
                    className={String(dayNumber) === selectedMapDay ? 'active' : ''}
                    key={dayNumber}
                    onClick={() => setSelectedMapDay(String(dayNumber))}
                    type="button"
                  >
                    DAY {dayNumber}
                    <small>{planItems.length}</small>
                  </button>
                ))}
              </div>
              <GoogleMapView planItems={selectedDayItems} />
            </div>
          )}

          {planItemsByDay.map(([dayNumber, planItems]) => (
            <div className="timeline-day" key={dayNumber}>
              <h3>DAY {dayNumber}</h3>
              {planItems.map((item) => {
                const itemKey = `${item.dayNumber}-${item.sequence}`
                const isEditing = editingKey === itemKey

                return (
                  <div className="timeline-item" key={itemKey}>
                    <i>{item.sequence}</i>
                    <div>
                      <strong>{item.placeName}</strong>
                      <small>{item.placeId || 'placeId 없음'}</small>
                      <small>{item.latitude}, {item.longitude}</small>
                      {isEditing && (
                        <div className="place-edit-box">
                          <PlaceAutocomplete
                            placeholder={`${item.placeName} 대신 방문할 장소`}
                            onSelect={(selectedPlace) => updatePlanItemPlace(item, selectedPlace)}
                          />
                        </div>
                      )}
                    </div>
                    <button type="button" onClick={() => setEditingKey(isEditing ? '' : itemKey)}>
                      {isEditing ? '닫기' : '수정'}
                    </button>
                  </div>
                )
              })}
            </div>
          ))}

          <button
            className="secondary wide"
            disabled={!draftPlan || isSaving || Boolean(savedPlan)}
            onClick={savePlan}
            type="button"
          >
            {savedPlan ? '저장 완료' : isSaving ? '저장 중' : '이 일정 저장하기'}
          </button>
        </section>
      </div>
    </>
  )
}

async function requestApi(path, options) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(options.body),
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.message || `요청에 실패했습니다. (${response.status})`)
  }

  return payload
}

function groupPlanItemsByDay(planItems) {
  const groups = new Map()
  planItems
    .slice()
    .sort((a, b) => a.dayNumber - b.dayNumber || a.sequence - b.sequence)
    .forEach((item) => {
      const key = item.dayNumber
      groups.set(key, [...(groups.get(key) || []), item])
    })

  return Array.from(groups.entries())
}

function replacePlanItem(plan, targetItem, updatedItem) {
  if (!plan) return plan

  return {
    ...plan,
    planItems: (plan.planItems || []).map((item) => {
      const isSameItem = item.dayNumber === targetItem.dayNumber && item.sequence === targetItem.sequence
      return isSameItem ? { ...item, ...updatedItem } : item
    }),
  }
}

export default TravelPage
