import { useEffect, useMemo, useState } from 'react'

import PageHeader from './components/PageHeader.jsx'
import TravelPage from './pages/TravelPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage.jsx'

const BACKEND = 'http://localhost:8080'

const NAV_ITEMS = [
  { id: 'overview', label: '전체 흐름', icon: '⌂' },
  { id: 'auth', label: '로그인 · 회원가입', icon: '◎' },
  { id: 'travel', label: 'AI 여행 계획', icon: '✦' },
  { id: 'board', label: '게시판 · 댓글', icon: '▤' },
]

const API_GROUPS = {
  auth: [
    { name: '로컬 회원가입', method: 'POST', path: '/api/auth/signup', body: '{\n  "email": "demo@example.com",\n  "password": "Password1!",\n  "name": "홍길동",\n  "nickname": "여행자"\n}' },
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
  const [token, setToken] = useState(() => localStorage.getItem('accessToken') || '')
  const [resetToken, setResetToken] = useState('')

  // 비밀번호 재설정 메일 링크(/reset-password?token=...) 진입 처리
  // OAuth 콜백: URL에 ?token= 또는 ?linked=true 가 있으면 처리 후 URL 정리
  useEffect(() => {
    const params = new URLSearchParams(window.location.search)

    if (window.location.pathname === '/reset-password') {
      setResetToken(params.get('token') || '')
      setActivePage('reset-password')
      return
    }

    const oauthToken = params.get('token')
    const linked = params.get('linked')

    if (oauthToken) {
      localStorage.setItem('accessToken', oauthToken)
      setToken(oauthToken)
      window.history.replaceState({}, '', window.location.pathname)
    }

    if (linked === 'true') {
      window.history.replaceState({}, '', window.location.pathname)
      navigate('auth')
    }
  }, [])

  const handleResetDone = () => {
    window.history.replaceState({}, '', '/')
    navigate('auth')
  }

  const handleLogin = (accessToken) => {
    localStorage.setItem('accessToken', accessToken)
    setToken(accessToken)
    navigate('overview')
  }

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    setToken('')
  }

  const navigate = (page) => {
    setActivePage(page)
    if (window.innerWidth < 860) setMenuOpen(false)
  }

  return (
    <div className="app-shell">
      <Header onMenu={() => setMenuOpen((v) => !v)} onNavigate={navigate} token={token} onLogout={handleLogout} />
      <div className="app-body">
        <SideNav active={activePage} open={menuOpen} onNavigate={navigate} />
        <main className="main-content">
          {activePage === 'overview' && <OverviewPage onNavigate={navigate} />}
          {activePage === 'auth' && <AuthPage onLogin={handleLogin} token={token} />}
          {activePage === 'reset-password' && <ResetPasswordPage token={resetToken} onDone={handleResetDone} />}
          {activePage === 'travel' && <TravelPage />}
          {activePage === 'board' && <BoardPage />}
        </main>
      </div>
    </div>
  )
}

function Header({ onMenu, onNavigate, token, onLogout }) {
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
      <div className="header-actions">
        {token
          ? <button className="secondary" onClick={onLogout}>로그아웃</button>
          : <span className="server-chip"><i /> 핵심 기능 3개</span>
        }
      </div>
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

function AuthPage({ onLogin, token }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [nickname, setNickname] = useState('')
  const [verifyCode, setVerifyCode] = useState('')
  const [emailSent, setEmailSent] = useState(false)
  const [emailVerified, setEmailVerified] = useState(false)
  const [message, setMessage] = useState({ text: '', ok: true })
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [verifyLoading, setVerifyLoading] = useState(false)
  const [resetEmail, setResetEmail] = useState('')
  const [resetLoading, setResetLoading] = useState(false)
  const [resetMessage, setResetMessage] = useState({ text: '', ok: true })

  const resetSignupState = () => {
    setEmailSent(false)
    setEmailVerified(false)
    setVerifyCode('')
    setMessage({ text: '', ok: true })
    setResetMessage({ text: '', ok: true })
  }

  const handleModeChange = (newMode) => {
    setMode(newMode)
    resetSignupState()
  }

  const handleForgotPassword = () => {
    setResetEmail(email)
    handleModeChange('forgot')
  }

  const handleRequestReset = async (e) => {
    e.preventDefault()
    if (!resetEmail) {
      setResetMessage({ text: '이메일을 입력해주세요.', ok: false })
      return
    }
    setResetLoading(true)
    setResetMessage({ text: '', ok: true })

    try {
      const res = await fetch(`${BACKEND}/api/auth/password-reset/request`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: resetEmail }),
      })
      const text4 = await res.text()
      let data = {}
      try { data = JSON.parse(text4) } catch { /* JSON 아닌 응답 */ }

      if (!res.ok) {
        setResetMessage({ text: data.message || '요청에 실패했습니다.', ok: false })
        return
      }

      setResetMessage({ text: '비밀번호 재설정 링크를 이메일로 보냈습니다.', ok: true })
    } catch {
      setResetMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setResetLoading(false)
    }
  }

  const handleSendVerification = async () => {
    if (!email) {
      setMessage({ text: '이메일을 입력해주세요.', ok: false })
      return
    }
    setVerifyLoading(true)
    setMessage({ text: '', ok: true })

    try {
      const res = await fetch(`${BACKEND}/api/auth/email-verifications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const text1 = await res.text()
      let data = {}
      try { data = JSON.parse(text1) } catch { /* JSON 아닌 응답 */ }

      if (!res.ok) {
        setMessage({ text: data.message || '발송에 실패했습니다.', ok: false })
        return
      }

      setEmailSent(true)
      setMessage({ text: '인증 메일을 발송했습니다. 코드를 입력해주세요.', ok: true })
    } catch {
      setMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setVerifyLoading(false)
    }
  }

  const handleConfirmVerification = async () => {
    setVerifyLoading(true)
    setMessage({ text: '', ok: true })

    try {
      const res = await fetch(`${BACKEND}/api/auth/email-verifications/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, code: verifyCode }),
      })
      const text2 = await res.text()
      let data = {}
      try { data = JSON.parse(text2) } catch { /* JSON 아닌 응답 */ }

      if (!res.ok) {
        setMessage({ text: data.message || '인증에 실패했습니다.', ok: false })
        return
      }

      setEmailVerified(true)
      setMessage({ text: '이메일 인증이 완료됐습니다.', ok: true })
    } catch {
      setMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setVerifyLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage({ text: '', ok: true })
    setLoading(true)

    try {
      const url = mode === 'login' ? '/api/auth/login' : '/api/auth/signup'
      const body = mode === 'login' ? { email, password } : { email, password, name, nickname }

      const res = await fetch(`${BACKEND}${url}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })

      const text3 = await res.text()
      let data = {}
      try { data = JSON.parse(text3) } catch { /* JSON 아닌 응답 */ }

      if (!res.ok) {
        setMessage({ text: data.message || '요청에 실패했습니다.', ok: false })
        return
      }

      if (mode === 'login') {
        onLogin(data.accessToken)
      } else {
        setMessage({ text: '회원가입 완료! 로그인해주세요.', ok: true })
        handleModeChange('login')
      }
    } catch {
      setMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setLoading(false)
    }
  }

  const handleSocial = (provider) => {
    window.location.href = `${BACKEND}/oauth2/authorization/${provider}`
  }

  if (token) {
    return (
      <>
        <PageHeader eyebrow="AUTH" title="로그인 · 회원가입" description="로컬 로그인과 OAuth 진입점을 한 화면에서 확인합니다." />
        <div className="auth-layout" style={{ display: 'flex', justifyContent: 'center' }}>
          <section className="auth-card">
            <p style={{ textAlign: 'center', padding: '1.5rem 0 0.5rem', fontWeight: 600 }}>로그인 상태입니다.</p>
            <p style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', textAlign: 'center', marginBottom: '1rem' }}>발급된 Access Token</p>
            <pre style={{ fontSize: '0.7rem', wordBreak: 'break-all', background: 'var(--color-surface)', padding: '1rem', borderRadius: '8px', whiteSpace: 'pre-wrap' }}>{token}</pre>
          </section>
        </div>
      </>
    )
  }

  return (
    <>
      <PageHeader eyebrow="AUTH" title="로그인 · 회원가입" description="로컬 로그인과 OAuth 진입점을 한 화면에서 확인합니다." />
      <div className="auth-layout">
        <section className="auth-card">
          {mode !== 'forgot' && (
            <div className="switch-tabs">
              <button className={mode === 'login' ? 'active' : ''} onClick={() => handleModeChange('login')}>로그인</button>
              <button className={mode === 'signup' ? 'active' : ''} onClick={() => handleModeChange('signup')}>회원가입</button>
            </div>
          )}

          {mode === 'forgot' ? (
            <form onSubmit={handleRequestReset}>
              <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', margin: '0 0 0.5rem' }}>
                가입한 이메일을 입력하면 비밀번호 재설정 링크를 보내드립니다.
              </p>
              <label>이메일<input type="email" placeholder="demo@example.com" value={resetEmail} onChange={(e) => setResetEmail(e.target.value)} /></label>

              {resetMessage.text && (
                <p style={{ color: resetMessage.ok ? '#27ae60' : '#e74c3c', fontSize: '0.85rem', margin: '0.25rem 0' }}>{resetMessage.text}</p>
              )}

              <button className="primary wide" type="submit" disabled={resetLoading}>
                {resetLoading ? '전송 중...' : '재설정 링크 보내기'}
              </button>
              <div className="form-options">
                <button type="button" onClick={() => handleModeChange('login')}>로그인으로 돌아가기</button>
              </div>
            </form>
          ) : (
          <form onSubmit={handleSubmit}>
            {mode === 'login' ? (
              <>
                <label>이메일<input type="email" placeholder="demo@example.com" value={email} onChange={(e) => setEmail(e.target.value)} /></label>
                <label>비밀번호
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <input type={showPassword ? 'text' : 'password'} placeholder="8자 이상 입력" value={password} onChange={(e) => setPassword(e.target.value)} style={{ flex: 1 }} />
                    <button type="button" className="secondary" onClick={() => setShowPassword((v) => !v)} style={{ whiteSpace: 'nowrap' }}>{showPassword ? '숨기기' : '보기'}</button>
                  </div>
                </label>
              </>
            ) : (
              <>
                <label>이메일
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <input
                      type="email"
                      placeholder="demo@example.com"
                      value={email}
                      onChange={(e) => { setEmail(e.target.value); setEmailSent(false); setEmailVerified(false) }}
                      disabled={emailVerified}
                      style={{ flex: 1 }}
                    />
                    <button
                      type="button"
                      className="secondary"
                      onClick={handleSendVerification}
                      disabled={verifyLoading || emailVerified}
                      style={{ whiteSpace: 'nowrap' }}
                    >
                      {emailVerified ? '✓ 완료' : emailSent ? '재전송' : '인증 메일 전송'}
                    </button>
                  </div>
                </label>

                {emailSent && !emailVerified && (
                  <label>인증 코드
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <input
                        type="text"
                        placeholder="6자리 숫자"
                        maxLength={6}
                        value={verifyCode}
                        onChange={(e) => setVerifyCode(e.target.value.replace(/\D/g, ''))}
                        style={{ flex: 1 }}
                      />
                      <button
                        type="button"
                        className="secondary"
                        onClick={handleConfirmVerification}
                        disabled={verifyLoading || verifyCode.length !== 6}
                        style={{ whiteSpace: 'nowrap' }}
                      >
                        확인
                      </button>
                    </div>
                  </label>
                )}

                <label>이름<input placeholder="홍길동" value={name} onChange={(e) => setName(e.target.value)} disabled={!emailVerified} /></label>
                <label>닉네임<input placeholder="여행자" value={nickname} onChange={(e) => setNickname(e.target.value)} disabled={!emailVerified} /></label>
                <label>비밀번호
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <input type={showPassword ? 'text' : 'password'} placeholder="8자 이상 입력" value={password} onChange={(e) => setPassword(e.target.value)} disabled={!emailVerified} style={{ flex: 1 }} />
                    <button type="button" className="secondary" onClick={() => setShowPassword((v) => !v)} disabled={!emailVerified} style={{ whiteSpace: 'nowrap' }}>{showPassword ? '숨기기' : '보기'}</button>
                  </div>
                </label>
              </>
            )}

            {message.text && (
              <p style={{ color: message.ok ? '#27ae60' : '#e74c3c', fontSize: '0.85rem', margin: '0.25rem 0' }}>{message.text}</p>
            )}

            {mode === 'login' && (
              <div className="form-options">
                <label><input type="checkbox" /> 로그인 유지</label>
                <button type="button" onClick={handleForgotPassword}>비밀번호 찾기</button>
              </div>
            )}

            <button className="primary wide" type="submit" disabled={loading || (mode === 'signup' && !emailVerified)}>
              {loading ? '처리 중...' : mode === 'login' ? '로그인' : '회원가입'}
            </button>
          </form>
          )}

          {mode === 'login' && (
            <>
              <div className="divider"><span>또는</span></div>
              <div className="social-buttons">
                <button onClick={() => handleSocial('google')}>G&nbsp; Google</button>
                <button className="kakao" onClick={() => handleSocial('kakao')}>Kakao</button>
                <button className="naver" onClick={() => handleSocial('naver')}>Naver</button>
              </div>
            </>
          )}
        </section>
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
