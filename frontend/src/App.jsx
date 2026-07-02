import { useEffect, useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'

import AppHeader from './components/AppHeader.jsx'
import RequireLoginModal from './components/RequireLoginModal.jsx'
import SideNav from './components/SideNav.jsx'
import { ROUTE_PATHS } from './constants/navigation.js'
import AuthPage from './pages/AuthPage.jsx'
import BoardDetailPage from './pages/BoardDetailPage.jsx'
import BoardPage from './pages/BoardPage.jsx'
import BoardWritePage from './pages/BoardWritePage.jsx'
import MyPage from './pages/MyPage.jsx'
import OverviewPage from './pages/OverviewPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage.jsx'
import TravelPage from './pages/TravelPage.jsx'

function App() {
  const [menuOpen, setMenuOpen] = useState(false)
  const [token, setToken] = useState(() => localStorage.getItem('accessToken') || '')
  const [resetToken, setResetToken] = useState('')
  const routerNavigate = useNavigate()
  const location = useLocation()
  const activePage = getActivePage(location.pathname)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)

    if (location.pathname === '/reset-password') {
      setResetToken(params.get('token') || '')
      return
    }

    const oauthToken = params.get('token')
    const linked = params.get('linked')

    if (oauthToken) {
      localStorage.setItem('accessToken', oauthToken)
      setToken(oauthToken)
      routerNavigate(location.pathname, { replace: true })
    }

    if (linked === 'true') {
      routerNavigate('/auth', { replace: true })
    }
  }, [location.pathname, location.search, routerNavigate])

  const navigate = (target) => {
    routerNavigate(ROUTE_PATHS[target] || target)
    if (window.innerWidth < 860) setMenuOpen(false)
  }

  const handleLogin = (accessToken, userInfo = {}) => {
    localStorage.setItem('accessToken', accessToken)
    if (userInfo.nickname) localStorage.setItem('nickname', userInfo.nickname)
    if (userInfo.name) localStorage.setItem('name', userInfo.name)
    setToken(accessToken)
    navigate('/')
  }

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('nickname')
    localStorage.removeItem('name')
    setToken('')
    navigate('auth')
  }

  const handleResetDone = () => {
    navigate('auth')
  }

  return (
    <div className="app-shell">
      <AppHeader
        onMenu={() => setMenuOpen((value) => !value)}
        onNavigate={navigate}
        token={token}
        onLogout={handleLogout}
      />
      <div className={`app-body ${menuOpen ? 'nav-open' : 'nav-closed'}`}>
        <SideNav active={activePage} open={menuOpen} onNavigate={navigate} />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<OverviewPage onNavigate={navigate} />} />
            <Route path="/auth" element={<AuthPage onLogin={handleLogin} token={token} />} />
            <Route path="/reset-password" element={<ResetPasswordPage token={resetToken} onDone={handleResetDone} />} />
            <Route path="/travel" element={<RequireLoginModal><TravelPage /></RequireLoginModal>} />
            <Route path="/board" element={<BoardPage />} />
            <Route path="/board/write" element={<BoardWritePage />} />
            <Route path="/board/:postId" element={<RequireLoginModal><BoardDetailPage /></RequireLoginModal>} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}

function getActivePage(pathname) {
  if (pathname === '/travel') return 'travel'
  if (pathname.startsWith('/board')) return 'board'
  if (pathname === '/mypage') return 'mypage'
  return 'overview'
}

export default App
