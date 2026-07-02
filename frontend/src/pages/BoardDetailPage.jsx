import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { getCurrentUserId, requestJson, unwrapList } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import {
  getCommentId,
  getContent,
  getLikeCount,
  getPostId,
  getTitle,
  getWriter,
} from './boardHelpers.js'

function BoardDetailPage() {
  const { postId } = useParams()
  const navigate = useNavigate()
  const userId = getCurrentUserId()
  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [commentContent, setCommentContent] = useState('')
  const [status, setStatus] = useState({ type: 'loading', message: '게시글 상세 정보를 불러오는 중입니다.' })
  const [commentStatus, setCommentStatus] = useState({ type: 'idle', message: '' })
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)

  useEffect(() => {
    loadDetail()
  }, [postId])

  async function loadDetail() {
    setStatus({ type: 'loading', message: '게시글 상세 정보를 불러오는 중입니다.' })
    setCommentStatus({ type: 'idle', message: '' })

    try {
      const [postDetail, commentList] = await Promise.all([
        requestJson(`/api/posts/${postId}`),
        requestJson(`/api/comment/post/${postId}`).catch(() => []),
      ])

      setPost(postDetail)
      setComments(unwrapList(commentList))
      setStatus({ type: 'success', message: '게시글을 불러왔습니다.' })
    } catch (error) {
      setStatus({ type: 'error', message:'로그인 후 확인 가능합니다' })
    }
  }

  async function handleLikePost() {
    if (!userId) {
      setStatus({ type: 'error', message: '좋아요는 로그인 후 사용할 수 있습니다.' })
      return
    }

    try {
      await requestJson(`/api/posts/${postId}/likes/${userId}`, { method: 'POST' })
      setStatus({ type: 'success', message: '좋아요 요청을 보냈습니다.' })
      await loadDetail()
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  async function handleCreateComment(event) {
    event.preventDefault()

    if (!userId) {
      setCommentStatus({ type: 'error', message: '댓글 작성은 로그인 후 사용할 수 있습니다.' })
      return
    }

    if (!commentContent.trim()) {
      setCommentStatus({ type: 'error', message: '댓글 내용을 입력하세요.' })
      return
    }

    setIsSubmittingComment(true)
    setCommentStatus({ type: 'loading', message: '댓글을 저장하는 중입니다.' })

    try {
      await requestJson('/api/comment', {
        method: 'POST',
        body: {
          postId: Number(postId),
          userId: Number(userId),
          parentCommentId: null,
          content: commentContent,
        },
      })

      setCommentContent('')
      const commentList = await requestJson(`/api/comment/post/${postId}`)
      setComments(unwrapList(commentList))
      setCommentStatus({ type: 'success', message: '댓글이 저장되었습니다.' })
    } catch (error) {
      setCommentStatus({ type: 'error', message: error.message })
    } finally {
      setIsSubmittingComment(false)
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="COMMUNITY"
        title="게시글 상세"
        description="게시글 본문과 댓글 목록을 확인합니다."
        action={
          <div className="page-actions">
            <button className="secondary" onClick={() => navigate('/board')}>목록으로</button>
            <button className="primary" onClick={handleLikePost}>좋아요</button>
          </div>
        }
      />

      <div className="board-detail-layout">
        <section className="board-detail-main">
          {status.message && <div className={`travel-status ${status.type}`}>{status.message}</div>}

          {post && (
            <article className="post-detail-card">
              <div className="post-detail-meta">
                <span>{getWriter(post)}</span>
                <small>게시글 ID: {getPostId(post) || postId}</small>
              </div>
              <h2>{getTitle(post)}</h2>
              <p>{getContent(post) || '내용이 없습니다.'}</p>
              <div className="post-detail-stats">
                <small>좋아요 {getLikeCount(post)}</small>
              </div>
            </article>
          )}
        </section>

        <aside className="board-comment-panel">
          <h2>댓글 {comments.length}</h2>

          <form className="comment-form" onSubmit={handleCreateComment}>
            <label>
              댓글 작성
              <textarea
                value={commentContent}
                onChange={(event) => setCommentContent(event.target.value)}
                placeholder="댓글을 입력하세요."
              />
            </label>
            {commentStatus.message && <div className={`travel-status ${commentStatus.type}`}>{commentStatus.message}</div>}
            <button className="primary wide" type="submit" disabled={isSubmittingComment}>
              {isSubmittingComment ? '저장 중...' : '댓글 저장'}
            </button>
          </form>

          <div className="comment-list">
            {comments.length === 0 && <p className="field-help">아직 댓글이 없습니다.</p>}
            {comments.map((comment, index) => (
              <article key={getCommentId(comment) || index}>
                <strong>{getWriter(comment)}</strong>
                <p>{comment.content || comment.comment || ''}</p>
                <small>좋아요 {getLikeCount(comment)}</small>
              </article>
            ))}
          </div>
        </aside>
      </div>
    </>
  )
}

export default BoardDetailPage
