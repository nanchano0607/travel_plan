import { useState } from 'react'

import { requestJson } from '../api/http.js'

function EditProfileModal({ profile, onClose, onUpdated }) {
  const [name, setName] = useState(profile?.name || '')
  const [nickname, setNickname] = useState(profile?.nickname || '')
  const [phone, setPhone] = useState(profile?.phone || '')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState({ text: '', ok: true })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setMessage({ text: '', ok: true })

    try {
      const updated = await requestJson('/api/auth/profile', {
        method: 'PATCH',
        body: { name, nickname, phone },
      })
      onUpdated(updated)
    } catch (error) {
      setMessage({ text: error.message, ok: false })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="require-login-layer" role="dialog" aria-modal="true" aria-labelledby="edit-profile-title">
      <div className="require-login-backdrop" onClick={onClose} />
      <section className="auth-card" style={{ position: 'relative', zIndex: 1 }}>
        <h2 id="edit-profile-title" style={{ margin: '0 0 18px' }}>회원정보 수정</h2>
        <form onSubmit={handleSubmit}>
          <label>이름<input value={name} onChange={(event) => setName(event.target.value)} /></label>
          <label>닉네임<input value={nickname} onChange={(event) => setNickname(event.target.value)} /></label>
          <label>전화번호<input value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="010-0000-0000" /></label>

          {message.text && (
            <p style={{ color: message.ok ? '#27ae60' : '#e74c3c', fontSize: '0.85rem', margin: '0.25rem 0' }}>{message.text}</p>
          )}

          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="secondary" type="button" onClick={onClose} style={{ flex: 1 }}>취소</button>
            <button className="primary" type="submit" disabled={loading} style={{ flex: 1 }}>
              {loading ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default EditProfileModal
