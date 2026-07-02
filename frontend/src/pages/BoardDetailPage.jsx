import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import CommentSection from '../comment_frontend/CommentSection.jsx'
import { getCurrentUserId, requestJson, resolveFileUrl, unwrapList } from '../api/http.js'
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
  const [images, setImages] = useState([])
  const [deletingImageId, setDeletingImageId] = useState(null)
  const [comments, setComments] = useState([])
  const [commentContent, setCommentContent] = useState('')
  const [status, setStatus] = useState({ type: 'loading', message: '게시글 상세 정보를 불러오는 중입니다.' })
  const [commentStatus, setCommentStatus] = useState({ type: 'idle', message: '' })
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [commentLikes, setCommentLikes] = useState({})
  const [postLiked, setPostLiked] = useState(false)

  useEffect(() => {
    loadDetail()
  }, [postId])

  async function loadDetail() {
    setStatus({ type: 'loading', message: '게시글 상세 정보를 불러오는 중입니다.' })
    setCommentStatus({ type: 'idle', message: '' })

    try {
      const [postDetail, commentList, imageList] = await Promise.all([
        requestJson(`/api/posts/${postId}`),
        requestJson(`/api/comment/post/${postId}`).catch(() => []),
        requestJson(`/api/posts/${postId}/images`).catch(() => []),
      ])

      setPost(postDetail)
      setImages(unwrapList(imageList))
      const normalizedComments = unwrapList(commentList)
      setComments(normalizedComments)
      setStatus({ type: 'success', message: '게시글을 불러왔습니다.' })
      loadCommentLikes(normalizedComments)
    } catch (error) {
      setStatus({ type: 'error', message:'로그인 후 확인 가능합니다' })
    }
  }

  async function handleDeleteImage(imageId) {
    if (!window.confirm('이 이미지를 삭제하시겠습니까?')) return

    setDeletingImageId(imageId)

    try {
      await requestJson(`/api/posts/${postId}/images/${imageId}`, { method: 'DELETE' })
      setImages((previous) => previous.filter((image) => image.imageId !== imageId))
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setDeletingImageId(null)
    }
  }

  async function loadCommentLikes(commentList) {
    const entries = await Promise.all(
      commentList.map(async (comment) => {
        const commentId = getCommentId(comment)
        if (!commentId) return null

        try {
          const [count, liked] = await Promise.all([
            requestJson(`/api/comment/${commentId}/likes/count`),
            userId ? requestJson(`/api/comment/${commentId}/likes/${userId}`) : Promise.resolve(false),
          ])
          return [commentId, { count: Number(count) || 0, liked: Boolean(liked) }]
        } catch {
          return [commentId, { count: 0, liked: false }]
        }
      }),
    )

    setCommentLikes(Object.fromEntries(entries.filter(Boolean)))
  }

  async function handleToggleCommentLike(commentId) {
    if (!userId) {
      setCommentStatus({ type: 'error', message: '좋아요는 로그인 후 사용할 수 있습니다.' })
      return
    }

    const current = commentLikes[commentId] || { count: 0, liked: false }

    try {
      await requestJson(`/api/comment/${commentId}/likes/${userId}`, {
        method: current.liked ? 'DELETE' : 'POST',
      })

      const count = await requestJson(`/api/comment/${commentId}/likes/count`)
      setCommentLikes((previous) => ({
        ...previous,
        [commentId]: { count: Number(count) || 0, liked: !current.liked },
      }))
    } catch (error) {
      setCommentStatus({ type: 'error', message: error.message })
    }
  }

  async function handleDeletePost() {
    if (!userId) {
      setStatus({ type: 'error', message: '삭제는 로그인 후 사용할 수 있습니다.' })
      return
    }

    if (!window.confirm('이 게시글을 삭제하시겠습니까?')) return

    setIsDeleting(true)
    setStatus({ type: 'loading', message: '게시글을 삭제하는 중입니다.' })

    try {
      await requestJson(`/api/posts/${postId}`, { method: 'DELETE' })
      navigate('/board')
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
      setIsDeleting(false)
    }
  }

  async function handleLikePost() {
    if (!userId) {
      setStatus({ type: 'error', message: '좋아요는 로그인 후 사용할 수 있습니다.' })
      return
    }

    try {
      await requestJson(`/api/posts/${postId}/likes/${userId}`, { method: 'POST' })
      setPostLiked((previous) => !previous)
      setStatus({ type: 'success', message: postLiked ? '좋아요를 취소했습니다.' : '좋아요를 눌렀습니다.' })
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
      const normalizedComments = unwrapList(commentList)
      setComments(normalizedComments)
      loadCommentLikes(normalizedComments)
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
            {post && userId && String(post.userId) === String(userId) && (
              <>
                <button className="secondary" onClick={() => navigate(`/board/${postId}/edit`)}>수정</button>
                <button className="secondary" onClick={handleDeletePost} disabled={isDeleting}>
                  {isDeleting ? '삭제 중...' : '삭제'}
                </button>
              </>
            )}
          </div>
        }
      />

      <div className="board-detail-layout">
        <section className="board-detail-main">
          {status.message && <div className={`travel-status ${status.type}`}>{status.message}</div>}

          {post && (
            <article className="post-detail-card">
              <button
                className={`like-button floating-like-button detail-like-button ${postLiked ? 'liked' : ''}`}
                onClick={handleLikePost}
                aria-label="좋아요"
              >
                ♥
              </button>
              <div className="post-detail-meta">
                <span>{getWriter(post)}</span>
                <small>게시글 ID: {getPostId(post) || postId}</small>
              </div>
              <h2>{getTitle(post)}</h2>
              <p>{getContent(post) || '내용이 없습니다.'}</p>

              {images.length > 0 && (
                <div className="post-image-grid">
                  {images.map((image) => (
                    <figure key={image.imageId}>
                      <a href={resolveFileUrl(image.imageUrl)} target="_blank" rel="noreferrer">
                        <img src={resolveFileUrl(image.imageUrl)} alt={image.fileName} />
                      </a>
                      {userId && String(post.userId) === String(userId) && (
                        <button
                          type="button"
                          onClick={() => handleDeleteImage(image.imageId)}
                          disabled={deletingImageId === image.imageId}
                          aria-label="이미지 삭제"
                        >
                          ✕
                        </button>
                      )}
                    </figure>
                  ))}
                </div>
              )}

              <div className="post-detail-stats">
                <small>좋아요 {getLikeCount(post)}</small>
              </div>
            </article>
          )}
        </section>


          <aside className="board-comment-panel">
  <CommentSection postId={postId} currentUserId={userId} />
</aside>

    </div>
    </>
  )
}

export default BoardDetailPage
