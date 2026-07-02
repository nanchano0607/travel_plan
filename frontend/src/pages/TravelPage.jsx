import { useCallback, useEffect, useMemo, useRef, useState } from 'react'

import GoogleMapView from '../components/GoogleMapView.jsx'
import PageHeader from '../components/PageHeader.jsx'
import PlaceAutocomplete from '../components/PlaceAutocomplete.jsx'
import RegionAutocomplete from '../components/RegionAutocomplete.jsx'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
const DRAFT_SESSION_STORAGE_KEY = 'travel-page-draft'
const DEFAULT_FORM = {
  startDate: '2026-07-10',
  endDate: '2026-07-11',
  headcount: 2,
  budget: 0,
}

function TravelPage() {
  const [restoredDraftSession] = useState(() => loadTravelDraftSession())
  const [region, setRegion] = useState(() => restoredDraftSession?.region || null)
  const [form, setForm] = useState(() => ({ ...DEFAULT_FORM, ...(restoredDraftSession?.form || {}) }))
  const [draftPlan, setDraftPlan] = useState(() => restoredDraftSession?.draftPlan || null)
  const [planInsight, setPlanInsight] = useState(() => restoredDraftSession?.planInsight || null)
  const [savedPlan, setSavedPlan] = useState(null)
  const [editingKey, setEditingKey] = useState('')
  const [selectedMapDay, setSelectedMapDay] = useState(() => restoredDraftSession?.selectedMapDay || '')
  const [status, setStatus] = useState({ type: 'idle', message: '' })
  const [insightStatus, setInsightStatus] = useState({ type: 'idle', message: '' })
  const [isGenerating, setIsGenerating] = useState(false)
  const [isRegenerating, setIsRegenerating] = useState(false)
  const [isAnalyzingInsight, setIsAnalyzingInsight] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isReordering, setIsReordering] = useState(false)
  const [isAddingItem, setIsAddingItem] = useState(false)
  const [addingDayNumber, setAddingDayNumber] = useState('')
  const latestInsightRequestIdRef = useRef(0)

  const currentPlan = savedPlan || draftPlan
  const planItemsByDay = useMemo(() => groupPlanItemsByDay(currentPlan?.planItems || []), [currentPlan])
  const budgetSummary = useMemo(() => createBudgetSummary(currentPlan), [currentPlan])
  const isInsightPending = Boolean(
    draftPlan
      && !savedPlan
      && !planInsight
      && (isAnalyzingInsight || insightStatus.type === 'loading'),
  )
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

  useEffect(() => {
    if (!region || !draftPlan || savedPlan) {
      removeTravelDraftSession()
      return
    }

    saveTravelDraftSession({
      region,
      form,
      draftPlan,
      planInsight,
      selectedMapDay,
    })
  }, [region, form, draftPlan, planInsight, selectedMapDay, savedPlan])

  useEffect(() => {
    if (!draftPlan || savedPlan) return undefined

    const handleBeforeUnload = (event) => {
      event.preventDefault()
      event.returnValue = ''
    }

    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => window.removeEventListener('beforeunload', handleBeforeUnload)
  }, [draftPlan, savedPlan])

  const clearRegion = useCallback(() => {
    setRegion(null)
    setDraftPlan(null)
    setPlanInsight(null)
    setSavedPlan(null)
    setSelectedMapDay('')
    setAddingDayNumber('')
    setInsightStatus({ type: 'idle', message: '' })
    removeTravelDraftSession()
  }, [])

  const selectRegion = useCallback((selectedRegion) => {
    setRegion(selectedRegion)
    setDraftPlan(null)
    setPlanInsight(null)
    setSavedPlan(null)
    setSelectedMapDay('')
    setAddingDayNumber('')
    setStatus({ type: 'idle', message: '' })
    setInsightStatus({ type: 'idle', message: '' })
  }, [])

  const updateForm = (field) => (event) => {
    const value = field === 'headcount' || field === 'budget'
      ? Number(event.target.value)
      : event.target.value
    setForm((previous) => ({ ...previous, [field]: value }))
  }

  const buildPlanCondition = () => ({
    regionName: region.regionName,
    regionId: region.regionId,
    latitude: region.regionLatitude,
    longitude: region.regionLongitude,
    startDate: form.startDate,
    endDate: form.endDate,
    headcount: Number(form.headcount),
  })

  const analyzePlanInsight = useCallback(async (planDraft) => {
    if (!planDraft?.planItems?.length) return

    const requestId = latestInsightRequestIdRef.current + 1
    latestInsightRequestIdRef.current = requestId
    const targetPlanKey = createPlanSnapshotKey(planDraft)

    setIsAnalyzingInsight(true)
    setPlanInsight(null)
    setInsightStatus({ type: 'loading', message: '예상 금액 계산중 · 한줄평 분석 작성중입니다.' })

    try {
      const result = await requestApi('/api/plan/ai/insight', {
        method: 'POST',
        body: planDraft,
      })

      if (latestInsightRequestIdRef.current !== requestId) return

      setPlanInsight(result.data)
      setDraftPlan((previous) => {
        if (createPlanSnapshotKey(previous) !== targetPlanKey) return previous
        return mergePlanInsight(previous, result.data)
      })
      setInsightStatus({ type: 'success', message: '장소별 예상 금액과 한줄평이 반영되었습니다.' })
    } catch (error) {
      if (latestInsightRequestIdRef.current !== requestId) return
      setInsightStatus({ type: 'error', message: error.message })
    } finally {
      if (latestInsightRequestIdRef.current === requestId) {
        setIsAnalyzingInsight(false)
      }
    }
  }, [])

  const generatePlan = async () => {
    if (!region?.regionId || isGenerating || isRegenerating || isReordering || isAddingItem) return

    setIsGenerating(true)
    setSavedPlan(null)
    setPlanInsight(null)
    setInsightStatus({ type: 'idle', message: '' })
    setStatus({ type: 'loading', message: 'AI가 일정 초안을 생성하고 있습니다.' })

    try {
      const result = await requestApi('/api/plan/ai', {
        method: 'POST',
        body: buildPlanCondition(),
      })

      const nextDraftPlan = {
        ...result.data,
        budget: Number(form.budget) || 0,
      }

      setDraftPlan(nextDraftPlan)
      setStatus({ type: 'success', message: 'AI 일정 초안이 생성되었습니다.' })
      void analyzePlanInsight(nextDraftPlan)
    } catch (error) {
      setDraftPlan(null)
      setPlanInsight(null)
      setInsightStatus({ type: 'idle', message: '' })
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsGenerating(false)
    }
  }

  const regeneratePlan = async () => {
    if (!region?.regionId || !draftPlan || savedPlan || isGenerating || isRegenerating || isReordering || isAddingItem) return

    const previousPlanItems = draftPlan.planItems || []
    if (previousPlanItems.length === 0) return

    setIsRegenerating(true)
    setEditingKey('')
    setPlanInsight(null)
    setInsightStatus({ type: 'idle', message: '' })
    setStatus({ type: 'loading', message: 'AI가 이전 초안과 다른 일정을 재생성하고 있습니다.' })

    try {
      const result = await requestApi('/api/plan/ai/retry', {
        method: 'POST',
        body: {
          condition: buildPlanCondition(),
          previousPlanItems,
        },
      })

      const nextDraftPlan = {
        ...result.data,
        budget: Number(form.budget) || 0,
      }

      setDraftPlan(nextDraftPlan)
      setStatus({ type: 'success', message: 'AI 일정 초안이 재생성되었습니다.' })
      void analyzePlanInsight(nextDraftPlan)
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsRegenerating(false)
    }
  }

  const savePlan = async () => {
    if (!draftPlan || isSaving || isAnalyzingInsight || isReordering || isAddingItem || savedPlan) return

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

      removeTravelDraftSession()
      setSavedPlan(result.data)
      setDraftPlan(result.data)
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
      oneLineReview: null,
      estimatedCost: null,
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

  const movePlanItem = useCallback(async (targetItem, direction) => {
    if (!currentPlan || isReordering || isAddingItem || isAnalyzingInsight) return

    const reorderedPlan = movePlanItemInDay(currentPlan, targetItem, direction)
    if (reorderedPlan === currentPlan) return

    if (!savedPlan?.planId) {
      setDraftPlan(reorderedPlan)
      setStatus({ type: 'success', message: '초안 일정 순서가 변경되었습니다.' })
      return
    }

    setIsReordering(true)
    setStatus({ type: 'loading', message: '저장된 일정 순서를 변경하고 있습니다.' })

    try {
      const result = await requestApi(`/api/plan/${savedPlan.planId}/items/order`, {
        method: 'PATCH',
        body: {
          items: createReorderRequestItems(reorderedPlan),
        },
      })

      setSavedPlan(result.data)
      setDraftPlan(result.data)
      setStatus({ type: 'success', message: '저장된 일정 순서가 변경되었습니다.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsReordering(false)
    }
  }, [currentPlan, isAddingItem, isAnalyzingInsight, isReordering, savedPlan])

  const addPlanItem = useCallback(async (dayNumber, selectedPlace) => {
    if (!currentPlan || isAddingItem || isReordering || isAnalyzingInsight) return

    const nextSequence = getNextSequence(currentPlan.planItems || [], dayNumber)
    const newItem = createPlanItemFromPlace(dayNumber, nextSequence, selectedPlace)

    if (!savedPlan?.planId) {
      setDraftPlan((previous) => appendPlanItem(previous, newItem))
      setAddingDayNumber('')
      setStatus({ type: 'success', message: '초안에 일정이 추가되었습니다.' })
      return
    }

    setIsAddingItem(true)
    setStatus({ type: 'loading', message: '저장된 계획에 일정을 추가하고 있습니다.' })

    try {
      const result = await requestApi(`/api/plan/${savedPlan.planId}/items`, {
        method: 'POST',
        body: newItem,
      })

      setSavedPlan(result.data)
      setDraftPlan(result.data)
      setAddingDayNumber('')
      setStatus({ type: 'success', message: '저장된 계획에 일정이 추가되었습니다.' })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsAddingItem(false)
    }
  }, [currentPlan, isAddingItem, isAnalyzingInsight, isReordering, savedPlan])

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
            disabled={!region?.regionId || isGenerating || isRegenerating || isReordering || isAddingItem}
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
            {draftPlan && !savedPlan && (
              <button
                className="regenerate-button"
                disabled={isGenerating || isRegenerating || isSaving || isReordering || isAddingItem || (draftPlan.planItems || []).length === 0}
                onClick={regeneratePlan}
                type="button"
              >
                {isRegenerating ? '재생성 중' : '재생성'}
              </button>
            )}
          </div>

          {status.message && (
            <div className={`travel-status timeline-status ${status.type}`}>
              {status.message}
            </div>
          )}

          {insightStatus.message && (
            <div className={`travel-status timeline-status ${insightStatus.type}`}>
              {insightStatus.message}
            </div>
          )}

          {!currentPlan && (
            <div className="empty-timeline">
              <strong>생성된 일정이 없습니다.</strong>
              <small>여행 지역과 날짜를 선택한 뒤 AI 일정 생성을 실행하세요.</small>
            </div>
          )}

          {currentPlan && (
            <div className="budget-panel">
              <div className="budget-metric">
                <span>예상 총액</span>
                <strong>
                  {budgetSummary.hasAnyEstimatedCost ? formatKrw(budgetSummary.totalEstimatedCost) : '계산 중'}
                </strong>
                <small>장소별 예상 금액 합산</small>
              </div>
              <div className="budget-metric">
                <span>입력 예산</span>
                <strong>{budgetSummary.budget > 0 ? formatKrw(budgetSummary.budget) : '미입력'}</strong>
                <small className={`budget-compare ${budgetSummary.state}`}>
                  {budgetSummary.comparisonText}
                </small>
              </div>
              <p>{planInsight?.budgetComment || '예상 금액 분석이 완료되면 예산 비교가 반영됩니다.'}</p>
              {planInsight?.assumptions?.length > 0 && (
                <ul>
                  {planInsight.assumptions.map((assumption) => (
                    <li key={assumption}>{assumption}</li>
                  ))}
                </ul>
              )}
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
              <div className="timeline-day-head">
                <h3>DAY {dayNumber}</h3>
                <button
                  disabled={isGenerating || isRegenerating || isSaving || isReordering || isAddingItem || isAnalyzingInsight}
                  onClick={() => setAddingDayNumber((previous) => previous === String(dayNumber) ? '' : String(dayNumber))}
                  type="button"
                >
                  {addingDayNumber === String(dayNumber) ? '닫기' : '장소 추가'}
                </button>
              </div>

              {addingDayNumber === String(dayNumber) && (
                <div className="place-add-box">
                  <PlaceAutocomplete
                    placeholder={`DAY ${dayNumber}에 추가할 장소`}
                    onSelect={(selectedPlace) => addPlanItem(dayNumber, selectedPlace)}
                  />
                </div>
              )}

              {planItems.map((item) => {
                const itemKey = `${item.dayNumber}-${item.sequence}`
                const isEditing = editingKey === itemKey
                const itemInsight = createPlanItemInsightView(item, isInsightPending)
                const itemIndex = planItems.findIndex((planItem) => planItem === item)
                const canMoveUp = itemIndex > 0
                const canMoveDown = itemIndex >= 0 && itemIndex < planItems.length - 1

                return (
                  <div className="timeline-item" key={itemKey}>
                    <i>{item.sequence}</i>
                    <div>
                      <strong>{item.placeName}</strong>
                      <small>{item.placeId || 'placeId 없음'}</small>
                      <small>{item.latitude}, {item.longitude}</small>
                      <div className="item-insight">
                        <span className={itemInsight.isPending ? 'pending' : ''}>
                          {itemInsight.costText}
                        </span>
                        <p>{itemInsight.reviewText}</p>
                      </div>
                      {isEditing && (
                        <div className="place-edit-box">
                          <PlaceAutocomplete
                            placeholder={`${item.placeName} 대신 방문할 장소`}
                            onSelect={(selectedPlace) => updatePlanItemPlace(item, selectedPlace)}
                          />
                        </div>
                      )}
                    </div>
                    <div className="item-actions">
                      <button
                        disabled={!canMoveUp || isReordering || isAddingItem || isAnalyzingInsight}
                        onClick={() => movePlanItem(item, -1)}
                        type="button"
                      >
                        위
                      </button>
                      <button
                        disabled={!canMoveDown || isReordering || isAddingItem || isAnalyzingInsight}
                        onClick={() => movePlanItem(item, 1)}
                        type="button"
                      >
                        아래
                      </button>
                      <button type="button" onClick={() => setEditingKey(isEditing ? '' : itemKey)}>
                        {isEditing ? '닫기' : '수정'}
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>
          ))}

          <button
            className="secondary wide"
            disabled={!draftPlan || isSaving || isAnalyzingInsight || isReordering || isAddingItem || Boolean(savedPlan)}
            onClick={savePlan}
            type="button"
          >
            {savedPlan ? '저장 완료' : isSaving ? '저장 중' : isAnalyzingInsight ? '분석 반영 중' : isReordering ? '순서 변경 중' : isAddingItem ? '일정 추가 중' : '이 일정 저장하기'}
          </button>
        </section>
      </div>
    </>
  )
}

async function requestApi(path, options) {
  const accessToken = localStorage.getItem('accessToken')
  const headers = {
    'Content-Type': 'application/json',
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method,
    headers,
    ...(options.body !== undefined ? { body: JSON.stringify(options.body) } : {}),
  })

  const text = await response.text()
  const contentType = response.headers.get('content-type') || ''

  if (text && !contentType.includes('application/json')) {
    const preview = text.replace(/\s+/g, ' ').slice(0, 120)
    const lowerPreview = preview.toLowerCase()

    if (contentType.includes('text/html') || lowerPreview.includes('<!doctype html') || lowerPreview.includes('<html')) {
      throw new Error('서버가 JSON 대신 HTML을 반환했습니다. 로그인 토큰이 없거나 만료되어 로그인 페이지로 이동했을 수 있습니다.')
    }

    throw new Error(`서버가 JSON이 아닌 응답을 반환했습니다. (${response.status}) 응답: ${preview}`)
  }
  let payload = null

  if (text) {
    try {
      payload = JSON.parse(text)
    } catch {
      throw new Error(`서버 응답을 JSON으로 읽을 수 없습니다. (${response.status})`)
    }
  }

  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.message || getFallbackErrorMessage(response.status))
  }

  if (!payload) {
    throw new Error('서버 응답 데이터가 비어 있습니다.')
  }

  if (!Object.prototype.hasOwnProperty.call(payload, 'data')) {
    throw new Error('서버 응답에 data 필드가 없습니다.')
  }

  return payload
}

function getFallbackErrorMessage(status) {
  if (status === 429) {
    return 'AI 요청 제한에 걸렸습니다. 잠시 후 다시 시도해주세요.'
  }

  return `요청에 실패했습니다. (${status})`
}

function loadTravelDraftSession() {
  try {
    const rawDraftSession = window.sessionStorage.getItem(DRAFT_SESSION_STORAGE_KEY)
    return rawDraftSession ? JSON.parse(rawDraftSession) : null
  } catch {
    removeTravelDraftSession()
    return null
  }
}

function saveTravelDraftSession(draftSession) {
  try {
    window.sessionStorage.setItem(DRAFT_SESSION_STORAGE_KEY, JSON.stringify(draftSession))
  } catch {
    // sessionStorage can be unavailable in restricted browser modes.
  }
}

function removeTravelDraftSession() {
  try {
    window.sessionStorage.removeItem(DRAFT_SESSION_STORAGE_KEY)
  } catch {
    // sessionStorage can be unavailable in restricted browser modes.
  }
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

function createBudgetSummary(plan) {
  const planItems = plan?.planItems || []
  const budget = Number(plan?.budget) || 0
  const hasAnyEstimatedCost = planItems.some((item) => item.estimatedCost !== null && item.estimatedCost !== undefined)
  const totalEstimatedCost = planItems.reduce((sum, item) => {
    const estimatedCost = Number(item.estimatedCost)
    return Number.isFinite(estimatedCost) ? sum + estimatedCost : sum
  }, 0)

  if (!hasAnyEstimatedCost) {
    return {
      budget,
      totalEstimatedCost,
      hasAnyEstimatedCost,
      state: 'pending',
      comparisonText: '분석 완료 후 예산과 비교합니다.',
    }
  }

  if (budget <= 0) {
    return {
      budget,
      totalEstimatedCost,
      hasAnyEstimatedCost,
      state: 'neutral',
      comparisonText: '입력 예산이 없어 총액만 표시합니다.',
    }
  }

  const remainingBudget = budget - totalEstimatedCost
  if (remainingBudget > 0) {
    return {
      budget,
      totalEstimatedCost,
      hasAnyEstimatedCost,
      state: 'under',
      comparisonText: `${formatKrw(remainingBudget)} 여유`,
    }
  }

  if (remainingBudget < 0) {
    return {
      budget,
      totalEstimatedCost,
      hasAnyEstimatedCost,
      state: 'over',
      comparisonText: `${formatKrw(Math.abs(remainingBudget))} 초과`,
    }
  }

  return {
    budget,
    totalEstimatedCost,
    hasAnyEstimatedCost,
    state: 'equal',
    comparisonText: '입력 예산과 동일합니다.',
  }
}

function mergePlanInsight(plan, insight) {
  if (!plan || !insight?.items?.length) return plan

  const insightItemMap = new Map(
    insight.items.map((item) => [createPlanItemKey(item), item]),
  )

  return {
    ...plan,
    planItems: (plan.planItems || []).map((item) => {
      const insightItem = insightItemMap.get(createPlanItemKey(item))
      if (!insightItem) return item

      return {
        ...item,
        oneLineReview: insightItem.oneLineReview,
        estimatedCost: insightItem.estimatedCost,
      }
    }),
  }
}

function createPlanSnapshotKey(plan) {
  if (!plan) return ''

  return JSON.stringify({
    title: plan.title,
    regionName: plan.regionName,
    startDate: plan.startDate,
    endDate: plan.endDate,
    planItems: (plan.planItems || []).map((item) => ({
      dayNumber: item.dayNumber,
      sequence: item.sequence,
      placeName: item.placeName,
      latitude: item.latitude,
      longitude: item.longitude,
    })),
  })
}

function createPlanItemKey(item) {
  return `${item.dayNumber}-${item.sequence}`
}

function createPlanItemInsightView(item, isInsightPending) {
  const hasEstimatedCost = item.estimatedCost !== null && item.estimatedCost !== undefined
  const hasOneLineReview = Boolean(item.oneLineReview)

  return {
    costText: hasEstimatedCost ? formatKrw(item.estimatedCost) : isInsightPending ? '금액 계산중' : '예상 금액 없음',
    reviewText: hasOneLineReview ? item.oneLineReview : isInsightPending ? '한줄평 작성중' : '한줄평 없음',
    isPending: isInsightPending && (!hasEstimatedCost || !hasOneLineReview),
  }
}

function createPlanItemFromPlace(dayNumber, sequence, selectedPlace) {
  return {
    placeName: selectedPlace.regionName,
    dayNumber,
    sequence,
    placeId: selectedPlace.regionId,
    latitude: selectedPlace.latitude,
    longitude: selectedPlace.longitude,
    oneLineReview: null,
    estimatedCost: null,
  }
}

function appendPlanItem(plan, newItem) {
  if (!plan) return plan

  return {
    ...plan,
    planItems: normalizePlanItemSequences([...(plan.planItems || []), newItem]),
  }
}

function movePlanItemInDay(plan, targetItem, direction) {
  if (!plan) return plan

  const planItems = plan.planItems || []
  const dayItems = planItems
    .filter((item) => item.dayNumber === targetItem.dayNumber)
    .slice()
    .sort((a, b) => a.sequence - b.sequence)
  const currentIndex = dayItems.findIndex((item) => getPlanItemIdentityKey(item) === getPlanItemIdentityKey(targetItem))
  const nextIndex = currentIndex + direction

  if (currentIndex < 0 || nextIndex < 0 || nextIndex >= dayItems.length) {
    return plan
  }

  const reorderedDayItems = dayItems.slice()
  const movingItem = reorderedDayItems[currentIndex]
  reorderedDayItems[currentIndex] = reorderedDayItems[nextIndex]
  reorderedDayItems[nextIndex] = movingItem

  const updatedDayItemMap = new Map(
    reorderedDayItems.map((item, index) => [
      getPlanItemIdentityKey(item),
      { ...item, sequence: index + 1 },
    ]),
  )

  return {
    ...plan,
    planItems: planItems.map((item) => updatedDayItemMap.get(getPlanItemIdentityKey(item)) || item),
  }
}

function normalizePlanItemSequences(planItems) {
  const groups = new Map()
  planItems.forEach((item) => {
    groups.set(item.dayNumber, [...(groups.get(item.dayNumber) || []), item])
  })

  const normalizedItems = []
  groups.forEach((items) => {
    items
      .slice()
      .sort((a, b) => a.sequence - b.sequence)
      .forEach((item, index) => {
        normalizedItems.push({ ...item, sequence: index + 1 })
      })
  })

  return normalizedItems
}

function getNextSequence(planItems, dayNumber) {
  return planItems
    .filter((item) => item.dayNumber === dayNumber)
    .reduce((maxSequence, item) => Math.max(maxSequence, Number(item.sequence) || 0), 0) + 1
}

function createReorderRequestItems(plan) {
  return (plan.planItems || [])
    .filter((item) => item.planItemId)
    .map((item) => ({
      planItemId: item.planItemId,
      dayNumber: item.dayNumber,
      sequence: item.sequence,
    }))
}

function getPlanItemIdentityKey(item) {
  if (item.planItemId) {
    return `saved-${item.planItemId}`
  }

  return `draft-${item.dayNumber}-${item.sequence}-${item.placeName}-${item.latitude}-${item.longitude}`
}

function formatKrw(value) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0)
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
