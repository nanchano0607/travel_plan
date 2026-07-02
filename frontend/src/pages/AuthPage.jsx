import { useState } from 'react'

import PageHeader from '../components/PageHeader.jsx'

const BACKEND = 'http://localhost:8080'

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

  const handleRequestReset = async (event) => {
    event.preventDefault()
    if (!resetEmail) {
      setResetMessage({ text: '이메일을 입력해 주세요.', ok: false })
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
      const data = await parseResponse(res)

      if (!res.ok) {
        setResetMessage({ text: data.message || '비밀번호 재설정 요청에 실패했습니다.', ok: false })
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
      setMessage({ text: '이메일을 입력해 주세요.', ok: false })
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
      const data = await parseResponse(res)

      if (!res.ok) {
        setMessage({ text: data.message || '인증 메일 발송에 실패했습니다.', ok: false })
        return
      }

      setEmailSent(true)
      setMessage({ text: '인증 메일을 발송했습니다. 코드를 입력해 주세요.', ok: true })
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
      const data = await parseResponse(res)

      if (!res.ok) {
        setMessage({ text: data.message || '이메일 인증에 실패했습니다.', ok: false })
        return
      }

      setEmailVerified(true)
      setMessage({ text: '이메일 인증이 완료되었습니다.', ok: true })
    } catch {
      setMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setVerifyLoading(false)
    }
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
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
      const data = await parseResponse(res)

      if (!res.ok) {
        setMessage({ text: data.message || '요청에 실패했습니다.', ok: false })
        return
      }

      if (mode === 'login') {
        const accessToken = data?.data?.accessToken || data?.accessToken

        if (!accessToken) {
          setMessage({ text: '로그인 응답에 Access Token이 없습니다.', ok: false })
          return
        }

        const userInfo = data?.data || data || {}
        onLogin(accessToken, userInfo)
        return
      }

      setMessage({ text: '회원가입이 완료되었습니다. 로그인해 주세요.', ok: true })
      handleModeChange('login')
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
        <PageHeader eyebrow="AUTH" title="로그인·회원가입" description="현재 로그인 상태입니다." />
        <div className="auth-layout" style={{ display: 'flex', justifyContent: 'center' }}>
          <section className="auth-card">
            <p style={{ textAlign: 'center', padding: '1.5rem 0 0.5rem', fontWeight: 600 }}>이미 로그인되어 있습니다.</p>
            <p style={{ fontSize: '0.85rem', color: '#71827e', textAlign: 'center', marginBottom: '1rem' }}>
              상단의 닉네임을 누르면 마이페이지로 이동할 수 있습니다.
            </p>
          </section>
        </div>
      </>
    )
  }

  return (
    <>
      <PageHeader eyebrow="AUTH" title="로그인·회원가입" description="로컬 로그인과 OAuth 진입점을 확인합니다." />
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
              <label>이메일<input type="email" placeholder="demo@example.com" value={resetEmail} onChange={(event) => setResetEmail(event.target.value)} /></label>

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
                  <label>이메일<input type="email" placeholder="demo@example.com" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
                  <PasswordField
                    password={password}
                    setPassword={setPassword}
                    showPassword={showPassword}
                    setShowPassword={setShowPassword}
                  />
                </>
              ) : (
                <>
                  <label>이메일
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <input
                        type="email"
                        placeholder="demo@example.com"
                        value={email}
                        onChange={(event) => {
                          setEmail(event.target.value)
                          setEmailSent(false)
                          setEmailVerified(false)
                        }}
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
                        {emailVerified ? '인증 완료' : emailSent ? '재전송' : '인증 메일 전송'}
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
                          onChange={(event) => setVerifyCode(event.target.value.replace(/\D/g, ''))}
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

                  <label>이름<input placeholder="홍길동" value={name} onChange={(event) => setName(event.target.value)} disabled={!emailVerified} /></label>
                  <label>닉네임<input placeholder="여행자" value={nickname} onChange={(event) => setNickname(event.target.value)} disabled={!emailVerified} /></label>
                  <PasswordField
                    password={password}
                    setPassword={setPassword}
                    showPassword={showPassword}
                    setShowPassword={setShowPassword}
                    disabled={!emailVerified}
                  />
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

function PasswordField({ password, setPassword, showPassword, setShowPassword, disabled = false }) {
  return (
    <label>비밀번호
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <input
          type={showPassword ? 'text' : 'password'}
          placeholder="8자 이상 입력"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          disabled={disabled}
          style={{ flex: 1 }}
        />
        <button
          type="button"
          className="secondary"
          onClick={() => setShowPassword((value) => !value)}
          disabled={disabled}
          style={{ whiteSpace: 'nowrap' }}
        >
          {showPassword ? '숨기기' : '보기'}
        </button>
      </div>
    </label>
  )
}

async function parseResponse(response) {
  const text = await response.text()

  try {
    return text ? JSON.parse(text) : {}
  } catch {
    return {}
  }
}

export default AuthPage
