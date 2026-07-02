const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export function getAccessToken() {
  return localStorage.getItem('accessToken') || ''
}

export function getCurrentUserId() {
  return getCurrentUser()?.userId || null
}

export function getCurrentUser() {
  const token = getAccessToken()
  if (!token) return null

  try {
    const [, payloadText] = token.split('.')
    if (!payloadText) return null

    const normalizedPayload = payloadText.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(
      normalizedPayload.length + ((4 - (normalizedPayload.length % 4)) % 4),
      '=',
    )
    const payload = JSON.parse(atob(paddedPayload))

    return {
      userId: payload.userId || payload.sub || null,
      email: payload.email || '',
      nickname: payload.nickname || localStorage.getItem('nickname') || '',
      role: payload.role || '',
      issuedAt: payload.iat ? new Date(payload.iat * 1000) : null,
      expiredAt: payload.exp ? new Date(payload.exp * 1000) : null,
    }
  } catch {
    return null
  }
}

export async function requestJson(path, options = {}) {
  const token = getAccessToken()
  const isFormData = options.body instanceof FormData
  const headers = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body: isFormData || !options.body ? options.body : JSON.stringify(options.body),
  })

  const text = await response.text()
  let payload = null

  try {
    payload = text ? JSON.parse(text) : null
  } catch {
    throw new Error(`서버 응답을 JSON으로 읽을 수 없습니다. (${response.status})`)
  }

  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.message || `요청에 실패했습니다. (${response.status})`)
  }

  return payload?.data ?? payload
}

export function unwrapList(data) {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.posts)) return data.posts
  if (Array.isArray(data?.list)) return data.list
  return []
}

export function resolveFileUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}
