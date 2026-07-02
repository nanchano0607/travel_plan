import { useNavigate } from 'react-router-dom'

function RequireLoginModal({ children }) {
  const navigate = useNavigate()
  const hasAccessToken = Boolean(localStorage.getItem('accessToken'))

  if (hasAccessToken) {
    return children || null
  }

  return (
    <div className="require-login-layer" role="dialog" aria-modal="true" aria-labelledby="require-login-title">
      <div className="require-login-backdrop" />
      <section className="require-login-modal">
        <span className="require-login-icon">🔒</span>
        <h2 id="require-login-title">로그인이 필요한 페이지입니다.</h2>
        <p>로그인하러 가기</p>
        <button className="primary" onClick={() => navigate('/auth')}>확인</button>
      </section>
    </div>
  )
}

export default RequireLoginModal
