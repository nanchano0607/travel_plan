import { useNavigate } from 'react-router-dom'

import { getAccessToken } from '../api/http.js'

function RequireLoginModal({ children }) {
  const navigate = useNavigate()
  const hasAccessToken = Boolean(getAccessToken())

  if (hasAccessToken) return children ?? null

  return (
    <div className="require-login-layer" role="dialog" aria-modal="true" aria-labelledby="require-login-title">
      <div className="require-login-backdrop" />
      <section className="require-login-modal">
        <div className="require-login-icon" aria-hidden="true">!</div>
        <h2 id="require-login-title">로그인이 필요합니다</h2>
        <p>로그인 후 이용할 수 있는 페이지입니다.</p>
        <button className="primary" onClick={() => navigate('/auth')}>로그인하러 가기</button>
      </section>
    </div>
  )
}

export default RequireLoginModal
