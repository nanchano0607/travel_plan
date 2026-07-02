import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getCurrentUser, requestJson, unwrapList } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import RequireLoginModal from '../components/RequireLoginModal.jsx'
import {
  getCommentCount,
  getCommentId,
  getContent,
  getContentPreview,
  getLikeCount,
  getPostId,
  getTitle,
} from './boardHelpers.js'

function MyPage() {
  const navigate = useNavigate()
  const currentUser = getCurrentUser()

  const [posts, setPosts] = useState([])
  const [postsStatus, setPostsStatus] = useState({ type: 'idle', message: '' })

  const [plans, setPlans] = useState([])
  const [plansStatus, setPlansStatus] = useState({ type: 'idle', message: '' })

  const [comments, setComments] = useState([])
  const [commentsStatus, setCommentsStatus] = useState({ type: 'idle', message: '' })

  const [likedPosts, setLikedPosts] = useState([])
  const [likedPostsStatus, setLikedPostsStatus] = useState({ type: 'idle', message: '' })

  useEffect(() => {
    if (!currentUser?.userId) return

    let ignore = false

    async function loadMyPosts() {
      setPostsStatus({ type: 'loading', message: '내가 작성한 게시글을 불러오는 중입니다.' })

      try {
        const data = await requestJson('/api/posts')
        if (ignore) return

        setPosts(unwrapList(data))
        setPostsStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setPostsStatus({ type: 'error', message: error.message })
      }
    }

    loadMyPosts()

    return () => {
      ignore = true
    }
  }, [currentUser?.userId])

  useEffect(() => {
    if (!currentUser?.userId) return

    let ignore = false

    async function loadMyPlans() {
      setPlansStatus({ type: 'loading', message: '내 여행 계획을 불러오는 중입니다.' })

      try {
        const data = await requestJson('/api/plan')
        if (ignore) return

        setPlans(unwrapList(data))
        setPlansStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setPlansStatus({ type: 'error', message: error.message })
      }
    }

    loadMyPlans()

    return () => {
      ignore = true
    }
  }, [currentUser?.userId])

  useEffect(() => {
    if (!currentUser?.userId) return

    let ignore = false

    async function loadMyComments() {
      setCommentsStatus({ type: 'loading', message: '내가 작성한 댓글을 불러오는 중입니다.' })

      try {
        const data = await requestJson('/api/comment/user/me')
        if (ignore) return

        setComments(unwrapList(data))
        setCommentsStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setCommentsStatus({ type: 'error', message: error.message })
      }
    }

    loadMyComments()

    return () => {
      ignore = true
    }
  }, [currentUser?.userId])

  useEffect(() => {
    if (!currentUser?.userId) return

    let ignore = false

    async function loadLikedPosts() {
      setLikedPostsStatus({ type: 'loading', message: '좋아요 한 게시글을 불러오는 중입니다.' })

      try {
        const data = await requestJson('/api/posts/likes/me')
        if (ignore) return

        setLikedPosts(unwrapList(data))
        setLikedPostsStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setLikedPostsStatus({ type: 'error', message: error.message })
      }
    }

    loadLikedPosts()

    return () => {
      ignore = true
    }
  }, [currentUser?.userId])

  const myPosts = useMemo(() => {
    if (!currentUser?.userId) return []

    return posts.filter((post) => String(post.userId ?? post.writerId ?? post.authorId ?? '') === String(currentUser.userId))
  }, [currentUser?.userId, posts])

  if (!currentUser) {
    return <RequireLoginModal />
  }

  const displayName = currentUser.nickname || '내 계정'

  return (
    <>
      <PageHeader
        eyebrow="MY PAGE"
        title="마이페이지"
        description="내 계정 정보, 게시글, 여행 계획, 댓글, 좋아요 목록을 한눈에 확인합니다."
      />

      <section className="mypage-profile-card">
        <div className="mypage-avatar">{createInitial(displayName)}</div>
        <div className="mypage-profile-info">
          <span>내 계정</span>
          <h2>{displayName}</h2>
          <p>여행 일정을 만들고 공유할 수 있습니다.</p>
        </div>
      </section>

      <section className="mypage-grid">
        <article className="mypage-summary-card">
          <span>작성 게시글</span>
          <strong>{myPosts.length}</strong>
          <p>내가 작성한 게시글을 모아 보여줍니다.</p>
        </article>
        <article className="mypage-summary-card">
          <span>여행 계획</span>
          <strong>{plans.length}</strong>
          <p>저장한 여행 계획을 모아 보여줍니다.</p>
        </article>
        <article className="mypage-summary-card">
          <span>작성 댓글</span>
          <strong>{comments.length}</strong>
          <p>내가 남긴 댓글을 모아 보여줍니다.</p>
        </article>
        <article className="mypage-summary-card">
          <span>좋아요 한 글</span>
          <strong>{likedPosts.length}</strong>
          <p>좋아요를 누른 게시글을 모아 보여줍니다.</p>
        </article>
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>MY POSTS</span>
            <h2>내가 작성한 게시글</h2>
          </div>
          <p>게시글을 누르면 상세 페이지로 이동합니다.</p>
        </div>

        {postsStatus.type === 'loading' && <div className="travel-status loading">{postsStatus.message}</div>}
        {postsStatus.type === 'error' && <div className="travel-status error">{postsStatus.message}</div>}

        {postsStatus.type !== 'loading' && myPosts.length === 0 && (
          <div className="home-empty-card">
            <h3>아직 작성한 게시글이 없습니다</h3>
            <p>여행 계획이나 후기를 게시판에 공유해보세요.</p>
            <button className="primary" onClick={() => navigate('/board/write')}>게시글 작성하기</button>
          </div>
        )}

        {myPosts.length > 0 && (
          <div className="post-grid">
            {myPosts.map((post, index) => {
              const postId = getPostId(post)

              return (
                <article className="post-card" key={postId || `${getTitle(post)}-${index}`}>
                  <button
                    className={`post-cover cover-${(index % 3) + 1}`}
                    onClick={() => postId && navigate(`/board/${postId}`)}
                    disabled={!postId}
                  >
                    <span>{postId ? `POST ${postId}` : 'POST'}</span>
                  </button>
                  <div className="post-content">
                    <span>내 게시글</span>
                    <h3>{getTitle(post)}</h3>
                    <p>{getContentPreview(post)}</p>
                    <div>
                      <small>좋아요 {getLikeCount(post)}</small>
                      <small>댓글 {getCommentCount(post)}</small>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>MY TRAVEL PLANS</span>
            <h2>나의 여행 계획</h2>
          </div>
          <p>여행 계획을 누르면 세부 일정과 지도를 확인할 수 있습니다.</p>
        </div>

        {plansStatus.type === 'loading' && <div className="travel-status loading">{plansStatus.message}</div>}
        {plansStatus.type === 'error' && <div className="travel-status error">{plansStatus.message}</div>}

        {plansStatus.type !== 'loading' && plans.length === 0 && (
          <div className="home-empty-card">
            <h3>아직 저장한 여행 계획이 없습니다</h3>
            <p>AI로 여행 일정을 만들고 저장해보세요.</p>
            <button className="primary" onClick={() => navigate('/travel')}>여행 계획 만들기</button>
          </div>
        )}

        {plans.length > 0 && (
          <div className="plan-list">
            {plans.map((plan) => (
              <article key={plan.planId}>
                <div className="plan-index">{(plan.planItems || []).length}</div>
                <div>
                  <h3>{plan.title || plan.regionName || '제목 없는 여행 계획'}</h3>
                  <p>{plan.regionName} · {formatPlanDateRange(plan)}</p>
                </div>
                <button onClick={() => navigate(`/mypage/plans/${plan.planId}`)}>세부 일정 보기</button>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>MY COMMENTS</span>
            <h2>내가 작성한 댓글</h2>
          </div>
          <p>댓글을 누르면 원본 게시글로 이동합니다.</p>
        </div>

        {commentsStatus.type === 'loading' && <div className="travel-status loading">{commentsStatus.message}</div>}
        {commentsStatus.type === 'error' && <div className="travel-status error">{commentsStatus.message}</div>}

        {commentsStatus.type !== 'loading' && comments.length === 0 && (
          <div className="home-empty-card">
            <h3>아직 작성한 댓글이 없습니다</h3>
            <p>게시글에 댓글을 남겨보세요.</p>
            <button className="primary" onClick={() => navigate('/board')}>게시판 가기</button>
          </div>
        )}

        {comments.length > 0 && (
          <div className="comment-list">
            {comments.map((comment) => (
              <article
                key={getCommentId(comment)}
                className={comment.postId ? 'clickable' : ''}
                onClick={() => comment.postId && navigate(`/board/${comment.postId}`)}
              >
                <strong>게시글 #{comment.postId ?? '알 수 없음'}</strong>
                <p>{getContent(comment)}</p>
                <small>{formatDate(comment.createdAt)}</small>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>LIKED POSTS</span>
            <h2>좋아요 한 게시글</h2>
          </div>
          <p>좋아요를 누른 게시글 목록입니다.</p>
        </div>

        {likedPostsStatus.type === 'loading' && <div className="travel-status loading">{likedPostsStatus.message}</div>}
        {likedPostsStatus.type === 'error' && <div className="travel-status error">{likedPostsStatus.message}</div>}

        {likedPostsStatus.type !== 'loading' && likedPosts.length === 0 && (
          <div className="home-empty-card">
            <h3>좋아요 한 게시글이 없습니다</h3>
            <p>마음에 드는 게시글에 좋아요를 눌러보세요.</p>
            <button className="primary" onClick={() => navigate('/board')}>게시판 가기</button>
          </div>
        )}

        {likedPosts.length > 0 && (
          <div className="post-grid">
            {likedPosts.map((post, index) => {
              const postId = getPostId(post)

              return (
                <article className="post-card" key={postId || `${getTitle(post)}-${index}`}>
                  <button
                    className={`post-cover cover-${(index % 3) + 1}`}
                    onClick={() => postId && navigate(`/board/${postId}`)}
                    disabled={!postId}
                  >
                    <span>{postId ? `POST ${postId}` : 'POST'}</span>
                  </button>
                  <div className="post-content">
                    <span>좋아요 한 글</span>
                    <h3>{getTitle(post)}</h3>
                    <p>{getContentPreview(post)}</p>
                    <div>
                      <small>좋아요 {getLikeCount(post)}</small>
                      <small>댓글 {getCommentCount(post)}</small>
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>
    </>
  )
}

function createInitial(value) {
  return (value?.[0] || 'U').toUpperCase()
}

function formatPlanDateRange(plan) {
  if (!plan?.startDate || !plan?.endDate) return '일정 미정'
  return `${plan.startDate} ~ ${plan.endDate}`
}

function formatDate(value) {
  if (!value) return ''

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export default MyPage
