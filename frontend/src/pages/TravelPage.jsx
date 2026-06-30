import { useCallback, useState } from 'react'

import PageHeader from '../components/PageHeader.jsx'
import RegionAutocomplete from '../components/RegionAutocomplete.jsx'

function TravelPage() {
  const [region, setRegion] = useState(null)

  const clearRegion = useCallback(() => {
    setRegion(null)
  }, [])

  const selectRegion = useCallback((selectedRegion) => {
    setRegion(selectedRegion)
  }, [])

  return (
    <>
      <PageHeader
        eyebrow="AI TRAVEL"
        title="AI 여행 계획 생성 · 수정"
        description="조건 입력부터 결과 타임라인, 장소 수정과 저장 흐름을 확인합니다."
      />
      <div className="travel-layout">
        <section className="condition-card">
          <h2>여행 조건 입력</h2>
          <p>AI에게 전달할 기본 조건입니다.</p>

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
              기간
              <select defaultValue="3">
                <option value="2">2일</option>
                <option value="3">3일</option>
                <option value="4">4일</option>
              </select>
            </label>
            <label>
              예산
              <input type="number" placeholder="500000" />
            </label>
          </div>

          <label>
            여행 스타일
            <div className="tag-row">
              <button className="selected" type="button">맛집</button>
              <button type="button">자연</button>
              <button type="button">힐링</button>
              <button type="button">액티비티</button>
            </div>
          </label>

          <button className="primary wide" disabled={!region?.regionId}>
            AI 일정 생성하기 ✦
          </button>
        </section>

        <section className="timeline-card">
          <div className="timeline-head">
            <div>
              <span>DAY 1</span>
              <h2>{region ? `${region.regionName} 추천 일정` : '지역을 먼저 선택하세요'}</h2>
            </div>
            <button type="button">지도 보기</button>
          </div>
          {['대표 명소', '근처 산책 코스', '지역 맛집', '저녁 명소'].map((place, index) => (
            <div className="timeline-item" key={place}>
              <i>{index + 1}</i>
              <div>
                <strong>{place}</strong>
                <small>{10 + index * 2}:00 · 추천 장소</small>
              </div>
              <button type="button">수정</button>
            </div>
          ))}
          <button className="secondary wide" type="button">이 일정 저장하기</button>
        </section>
      </div>
    </>
  )
}

export default TravelPage
