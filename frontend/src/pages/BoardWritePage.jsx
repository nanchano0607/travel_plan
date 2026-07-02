import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getAccessToken, getCurrentUserId, requestJson } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import RequireLoginModal from '../components/RequireLoginModal.jsx'
import { getPostId } from './boardHelpers.js'

const EMPTY_FORM = {
  title: '',
  content: '',
}

function BoardWritePage() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [status, setStatus] = useState({ type: 'idle', message: '' })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()
  const userId = getCurrentUserId()
  const hasAccessToken = Boolean(getAccessToken())

  if (!hasAccessToken) {
    return <RequireLoginModal />
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.title.trim() || !form.content.trim()) {
      setStatus({ type: 'error', message: '제목과 내용을 입력하세요.' })
      return
    }

    setIsSubmitting(true)
    setStatus({ type: 'loading', message: '게시글을 저장하는 중입니다.' })

    try {
      const createdPost = await requestJson('/api/posts', {
        method: 'POST',
        body: {
          title: form.title,
          content: form.content,
          ...(userId ? { userId: Number(userId) } : {}),
        },
      })

      const createdPostId = getPostId(createdPost)
      setStatus({ type: 'success', message: '게시글이 저장되었습니다.' })

      if (createdPostId) {
        navigate(`/board/${createdPostId}`)
        return
      }

      navigate('/board')
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="COMMUNITY"
        title="게시글 작성"
        description="새 여행 후기나 공유할 일정을 작성합니다."
        action={<button className="secondary" onClick={() => navigate('/board')}>목록으로</button>}
      />

      <section className="board-write-page">
        <form className="board-write-card" onSubmit={handleSubmit}>
          <h2>새 게시글</h2>
          <label>
            제목
            <input
              value={form.title}
              onChange={(event) => setForm((value) => ({ ...value, title: event.target.value }))}
              placeholder="예: 제주 3박 4일 여행 후기"
            />
          </label>
          <label>
            내용
            <textarea
              value={form.content}
              onChange={(event) => setForm((value) => ({ ...value, content: event.target.value }))}
              placeholder="여행 일정, 추천 장소, 느낀 점을 적어보세요."
            />
          </label>

          {!userId && (
            <p className="field-help error">
              현재 Access Token에서 userId를 읽지 못했습니다. 백엔드 정책에 따라 저장이 실패할 수 있습니다.
            </p>
          )}

          {status.message && <div className={`travel-status ${status.type}`}>{status.message}</div>}

          <button className="primary wide" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '저장 중...' : '게시글 저장'}
          </button>
        </form>
      </section>
    </>
  )
}

export default BoardWritePage
