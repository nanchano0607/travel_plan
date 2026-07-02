import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { requestJson } from '../api/http.js'
import GoogleMapView from '../components/GoogleMapView.jsx'

function PlanDetailPage() {
  const { planId } = useParams()
  const navigate = useNavigate()
  const [plan, setPlan] = useState(null)
  const [status, setStatus] = useState({ type: 'loading', message: '여행 계획을 불러오는 중입니다.' })
  const [selectedDay, setSelectedDay] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadPlan() {
      setStatus({ type: 'loading', message: '여행 계획을 불러오는 중입니다.' })

      try {
        const data = await requestJson(`/api/plan/${planId}`)
        if (ignore) return

        setPlan(data)
        setStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setStatus({ type: 'error', message: error.message })
      }
    }

    loadPlan()

    return () => {
      ignore = true
    }
  }, [planId])

  const planItemsByDay = useMemo(() => groupPlanItemsByDay(plan?.planItems || []), [plan])

  useEffect(() => {
    if (planItemsByDay.length === 0) {
      setSelectedDay('')
      return
    }

    const hasSelectedDay = planItemsByDay.some(([dayNumber]) => String(dayNumber) === selectedDay)
    if (!hasSelectedDay) setSelectedDay(String(planItemsByDay[0][0]))
  }, [planItemsByDay, selectedDay])

  const selectedDayItems = useMemo(() => {
    return planItemsByDay.find(([dayNumber]) => String(dayNumber) === selectedDay)?.[1] || []
  }, [planItemsByDay, selectedDay])

  const totalDistanceKm = useMemo(() => calculateTotalDistanceKm(selectedDayItems), [selectedDayItems])

  if (status.type === 'loading') {
    return <div className="travel-status loading">{status.message}</div>
  }

  if (status.type === 'error') {
    return (
      <>
        <div className="travel-status error">{status.message}</div>
        <button className="secondary" onClick={() => navigate('/mypage')}>마이페이지로</button>
      </>
    )
  }

  return (
    <div className="plan-detail-page">
      <div className="plan-detail-header">
        <div className="plan-detail-title">
          <span>🗺️</span>
          <h2>{plan?.title || plan?.regionName || '제목 없는 여행 계획'}</h2>
        </div>

        <div className="day-tabs">
          {planItemsByDay.map(([dayNumber]) => (
            <button
              key={dayNumber}
              className={String(dayNumber) === selectedDay ? 'active' : ''}
              onClick={() => setSelectedDay(String(dayNumber))}
              type="button"
            >
              DAY {dayNumber}
            </button>
          ))}
        </div>
      </div>

      <div className="plan-detail-layout">
        <div className="plan-detail-list">
          {selectedDayItems.length === 0 && (
            <div className="home-empty-card">
              <h3>등록된 일정이 없습니다</h3>
              <p>이 날짜에는 저장된 장소가 없습니다.</p>
            </div>
          )}

          {selectedDayItems.map((item) => (
            <article className="plan-detail-item" key={`${item.dayNumber}-${item.sequence}`}>
              <i>{item.sequence}</i>
              <div>
                <strong>{item.placeName}</strong>
                <small>
                  {item.estimatedCost !== null && item.estimatedCost !== undefined
                    ? formatKrw(item.estimatedCost)
                    : '예상 비용 정보 없음'}
                </small>
                <small>{item.oneLineReview || '한줄평 없음'}</small>
              </div>
            </article>
          ))}
        </div>

        <div className="plan-detail-map">
          <GoogleMapView planItems={selectedDayItems} />
          {selectedDayItems.length > 1 && (
            <div className="plan-detail-distance-chip">
              총 이동 거리 약 {totalDistanceKm.toFixed(1)}km (직선 거리)
            </div>
          )}
        </div>
      </div>

      <button className="secondary" onClick={() => navigate('/mypage')}>마이페이지로</button>
    </div>
  )
}

function groupPlanItemsByDay(planItems) {
  const groups = new Map()

  ;(planItems || [])
    .slice()
    .sort((a, b) => (a.dayNumber - b.dayNumber) || (a.sequence - b.sequence))
    .forEach((item) => {
      groups.set(item.dayNumber, [...(groups.get(item.dayNumber) || []), item])
    })

  return Array.from(groups.entries())
}

function calculateTotalDistanceKm(items) {
  const points = items
    .filter((item) => Number.isFinite(Number(item.latitude)) && Number.isFinite(Number(item.longitude)))
    .slice()
    .sort((a, b) => a.sequence - b.sequence)

  let totalKm = 0
  for (let index = 1; index < points.length; index += 1) {
    totalKm += haversineDistanceKm(points[index - 1], points[index])
  }

  return totalKm
}

function haversineDistanceKm(pointA, pointB) {
  const earthRadiusKm = 6371
  const toRad = (deg) => (deg * Math.PI) / 180
  const lat1 = Number(pointA.latitude)
  const lon1 = Number(pointA.longitude)
  const lat2 = Number(pointB.latitude)
  const lon2 = Number(pointB.longitude)
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const sinHalfDLat = Math.sin(dLat / 2)
  const sinHalfDLon = Math.sin(dLon / 2)
  const centralAngle = sinHalfDLat * sinHalfDLat
    + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * sinHalfDLon * sinHalfDLon

  return earthRadiusKm * 2 * Math.atan2(Math.sqrt(centralAngle), Math.sqrt(1 - centralAngle))
}

function formatKrw(value) {
  return `${Number(value).toLocaleString('ko-KR')}원`
}

export default PlanDetailPage
