import { useMemo, useState } from 'react'

import PageHeader from './components/PageHeader.jsx'
import TravelPage from './pages/TravelPage.jsx'

const NAV_ITEMS = [
  { id: 'overview', label: '전체 흐름', icon: '⌂' },
  { id: 'auth', label: '로그인 · 회원가입', icon: '◎' },
  { id: 'travel', label: 'AI 여행 계획', icon: '✦' },
  { id: 'board', label: '게시판 · 댓글', icon: '▤' },
]

const API_GROUPS = {
  auth: [
    { name: '로컬 회원가입', method: 'POST', path: '/api/auth/signup', body: '{\n  "email": "demo@example.com",\n  "password": "Password1!",\n  "nickname": "여행자"\n}' },
    { name: '로컬 로그인', method: 'POST', path: '/api/auth/login', body: '{\n  "email": "demo@example.com",\n  "password": "Password1!"\n}' },
    { name: '내 정보 조회', method: 'GET', path: '/api/users/me', body: '' },
  ],
  travel: [
    { name: 'AI 일정 생성', method: 'POST', path: '/api/travel-plans/generate', body: '{\n  "destination": "제주",\n  "days": 3,\n  "budget": 500000\n}' },
    { name: '내 여행 목록', method: 'GET', path: '/api/travel-plans/my', body: '' },
    { name: '여행 계획 저장', method: 'POST', path: '/api/travel-plans', body: '{\n  "title": "제주 3일 여행",\n  "isPublic": true\n}' },
  ],
  board: [
    { name: '게시글 목록', method: 'GET', path: '/api/posts?page=0&size=10', body: '' },
    { name: '게시글 작성', method: 'POST', path: '/api/posts', body: '{\n  "title": "제주 여행 후기",\n  "content": "좋았어요!"\n}' },
    { name: '댓글 작성', method: 'POST', path: '/api/posts/1/comments', body: '{\n  "content": "좋은 일정이네요."\n}' },
  ],
}

function App() {
  const [activePage, setActivePage] = useState('overview')
  const [menuOpen, setMenuOpen] = useState(() => window.innerWidth >= 860)

  const navigate = (page) => {
    setActivePage(page)
    if (window.innerWidth < 860) setMenuOpen(false)
  }

  return (
    <div className="app-shell">
      <Header onMenu={() => setMenuOpen((value) => !value)} onNavigate={navigate} />
      <div className="app-body">
        <SideNav active={activePage} open={menuOpen} onNavigate={navigate} />
        <main className="main-content">
          {activePage === 'overview' && <OverviewPage onNavigate={navigate} />}
          {activePage === 'auth' && <AuthPage />}
          {activePage === 'travel' && <TravelPage />}
          {activePage === 'board' && <BoardPage />}
        </main>
      </div>
    </div>
  )
}

function Header({ onMenu, onNavigate }) {
  return (
    <header className="top-header">
      <button className="menu-button" onClick={onMenu} aria-label="메뉴 열기">☰</button>
      <button className="brand" onClick={() => onNavigate('overview')}>
        <span className="brand-mark">잇</span>
        <span><strong>여정잇다</strong><small>Travel together</small></span>
      </button>
      <div className="header-search">
        <span>⌕</span>
        <input aria-label="여행 검색" placeholder="여행지와 게시글을 검색해 보세요" />
      </div>
      <div className="header-actions"><span className="server-chip"><i /> 핵심 기능 3개</span></div>
    </header>
  )
}

function SideNav({ active, open, onNavigate }) {
  return (
    <aside className={`side-nav ${open ? 'open' : ''}`}>
      <div className="side-caption">SERVICE MAP</div>
      <nav>
        {NAV_ITEMS.map((item) => (
          <button key={item.id} className={active === item.id ? 'active' : ''} onClick={() => onNavigate(item.id)}>
            <span>{item.icon}</span>{item.label}
          </button>
        ))}
      </nav>
      <div className="side-note">
        <strong>구현 범위</strong>
        <p>인증, AI 여행 계획, 게시판 기능에 집중합니다.</p>
      </div>
    </aside>
  )
}

function OverviewPage({ onNavigate }) {
  const flows = [
    { no: '01', title: '로그인과 회원가입', text: '로컬·Google·Kakao·Naver 인증', page: 'auth', color: 'mint' },
    { no: '02', title: 'AI 여행 계획 생성·수정', text: '조건 입력, 일정 생성, 장소 편집 및 저장', page: 'travel', color: 'blue' },
    { no: '03', title: '게시판과 댓글', text: '게시글 검색·작성·댓글·대댓글', page: 'board', color: 'purple' },
  ]
  return (
    <>
      <PageHeader eyebrow="COMMON LAYOUT" title="핵심 기능 화면" description="로그인·회원가입, AI 여행 계획, 게시판·댓글 화면을 하나의 레이아웃에서 이동합니다." />
      <section className="hero-panel">
        <div><span className="pill">AI TRAVEL PLANNER</span><h2>어디로 떠나고 싶나요?</h2><p>국내·해외 여행을 선택하고 조건을 입력하면 AI가 일정 초안을 만듭니다.</p></div>
        <div className="destination-tabs"><button className="selected">국내 여행</button><button>해외 여행</button></div>
      </section>
      <section className="section-block">
        <div className="section-heading"><div><span>USER JOURNEY</span><h2>핵심 화면 흐름</h2></div><p>카드를 눌러 기능 화면으로 이동하세요.</p></div>
        <div className="flow-grid">
          {flows.map((item, index) => (
            <button key={item.no} className="flow-card" onClick={() => onNavigate(item.page)}>
              <div className={`flow-icon ${item.color}`}>{item.no}</div><h3>{item.title}</h3><p>{item.text}</p><span>화면 보기 →</span>{index < flows.length - 1 && <b className="flow-arrow">›</b>}
            </button>
          ))}
        </div>
      </section>
      <section className="quick-grid">
        <div className="quick-card"><span>AUTH</span><strong>01</strong><p>로그인 · 회원가입</p></div>
        <div className="quick-card"><span>AI PLAN</span><strong>02</strong><p>여행 계획 생성 · 수정</p></div>
        <div className="quick-card"><span>COMMUNITY</span><strong>03</strong><p>게시판 · 댓글</p></div>
      </section>
    </>
  )
}

function AuthPage() {
  const [mode, setMode] = useState('login')
  return (
    <>
      <PageHeader eyebrow="AUTH" title="로그인 · 회원가입" description="로컬 로그인과 OAuth 진입점을 한 화면에서 확인합니다." />
      <div className="auth-layout">
        <section className="auth-card">
          <div className="switch-tabs"><button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>로그인</button><button className={mode === 'signup' ? 'active' : ''} onClick={() => setMode('signup')}>회원가입</button></div>
          <form onSubmit={(event) => event.preventDefault()}>
            <label>이메일<input type="email" placeholder="demo@example.com" /></label>
            <label>비밀번호<input type="password" placeholder="8자 이상 입력" /></label>
            {mode === 'signup' && <label>닉네임<input placeholder="여행자" /></label>}
            <div className="form-options"><label><input type="checkbox" /> 로그인 유지</label><button type="button">비밀번호 찾기</button></div>
            <button className="primary wide" type="submit">{mode === 'login' ? '로그인' : '회원가입'}</button>
          </form>
          <div className="divider"><span>또는</span></div>
          <div className="social-buttons"><button>G&nbsp; Google</button><button className="kakao">Kakao</button><button className="naver">Naver</button></div>
        </section>
        <section className="info-panel"><span className="pill">TEST CHECKLIST</span><h2>확인할 기능</h2><CheckList items={['비밀번호 정책과 이메일 중복 검증','회원가입 후 이메일 인증 상태','로그인 성공 시 Access Token 발급','잘못된 로그인 요청의 401 응답','Google·Kakao·Naver OAuth 콜백']} /></section>
      </div>
    </>
  )
}

function BoardPage() {
  const posts = [
    ['제주도 3박 4일, 이 동선 정말 좋았어요','제주 · 맛집 · 드라이브','여행하는 민수','24'],
    ['부모님과 함께한 경주 여행 코스','경주 · 가족 · 역사','초록여행','18'],
    ['비 오는 부산에서 즐긴 실내 여행','부산 · 카페 · 전시','여행수집가','31'],
  ]
  return (
    <>
      <PageHeader eyebrow="COMMUNITY" title="여행 일정 게시판" description="공유된 일정을 검색하고 상세·댓글 기능으로 이어지는 화면입니다." action={<button className="primary">새 글 작성</button>} />
      <div className="board-toolbar"><div className="search-box">⌕<input placeholder="제목, 작성자, 태그 검색" /><button>검색</button></div><select><option>최신순</option><option>인기순</option></select></div>
      <div className="post-grid">{posts.map((post, index) => <article className="post-card" key={post[0]}><div className={`post-cover cover-${index + 1}`}><span>{index === 0 ? 'JEJU' : index === 1 ? 'GYEONGJU' : 'BUSAN'}</span></div><div className="post-content"><span>{post[1]}</span><h3>{post[0]}</h3><div><small>{post[2]}</small><small>♡ {post[3]}</small></div></div></article>)}</div>
    </>
  )
}

function ProfilePage({ onApi }) {
  return (
    <>
      <PageHeader eyebrow="MY PAGE" title="나의 여행 보관함" description="내 정보와 저장한 여행 계획, 작성한 게시글을 확인합니다." action={<button className="secondary" onClick={onApi}>내 정보 API</button>} />
      <section className="profile-banner"><div className="profile-avatar">PJ</div><div><span>ACTIVE · USER</span><h2>박진현</h2><p>여행을 기록하고 함께 나누는 중입니다.</p></div><button>프로필 수정</button></section>
      <div className="profile-tabs"><button className="active">내 여행 계획 <b>2</b></button><button>내 게시글 <b>3</b></button><button>좋아요 <b>8</b></button></div>
      <div className="plan-list"><Plan title="제주 3일 힐링 여행" meta="3일 · 장소 12개 · 저장됨" /><Plan title="경주 가족 여행" meta="2일 · 장소 8개 · 공유 중" /></div>
    </>
  )
}

function Plan({ title, meta }) {
  return <article><div className="plan-index">✦</div><div><h3>{title}</h3><p>{meta}</p></div><button>열기 →</button></article>
}

function CheckList({ items }) {
  return <ul className="check-list">{items.map((item) => <li key={item}><i>✓</i>{item}</li>)}</ul>
}

function ApiLab({ baseUrl, setBaseUrl, accessToken, setAccessToken, onSave }) {
  const allEndpoints = useMemo(() => Object.entries(API_GROUPS).flatMap(([group, endpoints]) => endpoints.map((endpoint) => ({ ...endpoint, group }))), [])
  const [selected, setSelected] = useState(allEndpoints[0])
  const [method, setMethod] = useState(selected.method)
  const [path, setPath] = useState(selected.path)
  const [body, setBody] = useState(selected.body)
  const [result, setResult] = useState({ state: 'idle', status: '', time: '', content: '// 요청을 보내면 백엔드 응답이 여기에 표시됩니다.' })

  const chooseEndpoint = (endpoint) => { setSelected(endpoint); setMethod(endpoint.method); setPath(endpoint.path); setBody(endpoint.body); setResult({ state: 'idle', status: '', time: '', content: '// 요청 준비 완료' }) }

  const sendRequest = async () => {
    const started = performance.now()
    setResult({ state: 'loading', status: '', time: '', content: '요청 중...' })
    try {
      const response = await fetch(`${baseUrl.replace(/\/$/, '')}${path}`, {
        method,
        headers: { 'Content-Type': 'application/json', ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}) },
        ...(method !== 'GET' && body.trim() ? { body } : {}),
      })
      const text = await response.text()
      let pretty = text
      try { pretty = JSON.stringify(JSON.parse(text), null, 2) } catch { /* text response */ }
      setResult({ state: response.ok ? 'success' : 'error', status: `${response.status} ${response.statusText}`, time: `${Math.round(performance.now() - started)}ms`, content: pretty || '(응답 본문 없음)' })
    } catch (error) {
      setResult({ state: 'error', status: 'NETWORK ERROR', time: `${Math.round(performance.now() - started)}ms`, content: `${error.message}\n\n백엔드 서버 실행 여부와 CORS 설정을 확인하세요.` })
    }
  }

  return (
    <>
      <PageHeader eyebrow="BACKEND LAB" title="API 요청 · 응답 확인" description="예시 경로는 백엔드 명세에 맞게 수정해서 사용하세요." action={<button className="secondary" onClick={onSave}>연결 설정 저장</button>} />
      <section className="connection-panel"><label>Backend Base URL<input value={baseUrl} onChange={(event) => setBaseUrl(event.target.value)} /></label><label>Access Token<input value={accessToken} onChange={(event) => setAccessToken(event.target.value)} placeholder="Bearer를 제외한 토큰 값" /></label><span className="connection-state"><i /> 설정 대기</span></section>
      <div className="api-layout">
        <aside className="endpoint-list">{Object.entries(API_GROUPS).map(([group, endpoints]) => <div key={group}><h3>{group.toUpperCase()}</h3>{endpoints.map((endpoint) => <button key={endpoint.name} className={selected.name === endpoint.name ? 'active' : ''} onClick={() => chooseEndpoint({ ...endpoint, group })}><b className={endpoint.method.toLowerCase()}>{endpoint.method}</b><span>{endpoint.name}</span></button>)}</div>)}</aside>
        <section className="request-panel"><div className="request-line"><select value={method} onChange={(event) => setMethod(event.target.value)}><option>GET</option><option>POST</option><option>PUT</option><option>PATCH</option><option>DELETE</option></select><input value={path} onChange={(event) => setPath(event.target.value)} /><button className="primary" onClick={sendRequest} disabled={result.state === 'loading'}>{result.state === 'loading' ? '전송 중' : '요청 보내기'}</button></div><div className="editor-grid"><div><h3>REQUEST BODY</h3><textarea value={body} onChange={(event) => setBody(event.target.value)} spellCheck="false" /></div><div><h3>RESPONSE <span className={`result-chip ${result.state}`}>{result.status}</span><small>{result.time}</small></h3><pre>{result.content}</pre></div></div></section>
      </div>
    </>
  )
}

export default App
