import { getCurrentUser } from '../api/http.js'

function AppHeader({ onMenu, onNavigate, token, onLogout }) {
  const currentUser = token ? getCurrentUser() : null
  const accountName = currentUser?.nickname || '내 계정'

  return (
    <header className="top-header">
      <button className="menu-button" onClick={onMenu} aria-label="메뉴 열기">☰</button>

      <button className="brand" onClick={() => onNavigate('overview')}>
        <span className="brand-mark">T</span>
        <span>
          <strong>여정잇다</strong>
          <small>Travel together</small>
        </span>
      </button>

      <div className="header-actions">
        {token ? (
          <div className="account-box">
            <button className="account-name" onClick={() => onNavigate('mypage')} title={accountName}>
              {accountName}
            </button>
            <button className="secondary" onClick={onLogout}>로그아웃</button>
          </div>
        ) : (
          <button className="primary" onClick={() => onNavigate('auth')}>로그인</button>
        )}
      </div>
    </header>
  )
}

export default AppHeader
