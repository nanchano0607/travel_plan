import { useState } from 'react'

import PageHeader from '../components/PageHeader.jsx'

const BACKEND = 'http://localhost:8080'

function ResetPasswordPage({ token, onDone }) {
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState({ text: '', ok: true })
  const [done, setDone] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage({ text: '', ok: true })

    if (newPassword.length < 8) {
      setMessage({ text: '비밀번호는 8자 이상이어야 합니다.', ok: false })
      return
    }
    if (newPassword !== confirmPassword) {
      setMessage({ text: '비밀번호가 일치하지 않습니다.', ok: false })
      return
    }

    setLoading(true)
    try {
      const res = await fetch(`${BACKEND}/api/auth/password-reset/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword }),
      })
      const text = await res.text()
      let data = {}
      try { data = JSON.parse(text) } catch { /* JSON 아닌 응답 */ }

      if (!res.ok) {
        setMessage({ text: data.message || '재설정에 실패했습니다. 링크가 만료되었을 수 있습니다.', ok: false })
        return
      }

      setDone(true)
      setMessage({ text: '비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.', ok: true })
    } catch {
      setMessage({ text: '서버에 연결할 수 없습니다.', ok: false })
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <PageHeader eyebrow="AUTH" title="비밀번호 재설정" description="이메일로 받은 링크를 통해 새 비밀번호를 설정합니다." />
      <div className="auth-layout" style={{ display: 'flex', justifyContent: 'center' }}>
        <section className="auth-card">
          {!token ? (
            <p style={{ textAlign: 'center', padding: '1.5rem 0' }}>유효하지 않은 재설정 링크입니다.</p>
          ) : done ? (
            <>
              <p style={{ color: '#27ae60', textAlign: 'center', padding: '1.5rem 0 0.5rem' }}>{message.text}</p>
              <button className="primary wide" onClick={onDone}>로그인하러 가기</button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <label>새 비밀번호
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    placeholder="8자 이상 입력"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <button type="button" className="secondary" onClick={() => setShowPassword((v) => !v)} style={{ whiteSpace: 'nowrap' }}>
                    {showPassword ? '숨기기' : '보기'}
                  </button>
                </div>
              </label>
              <label>새 비밀번호 확인
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="다시 입력"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </label>

              {message.text && (
                <p style={{ color: message.ok ? '#27ae60' : '#e74c3c', fontSize: '0.85rem', margin: '0.25rem 0' }}>{message.text}</p>
              )}

              <button className="primary wide" type="submit" disabled={loading}>
                {loading ? '처리 중...' : '비밀번호 변경'}
              </button>
            </form>
          )}
        </section>
      </div>
    </>
  )
}

export default ResetPasswordPage
