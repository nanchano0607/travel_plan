import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getCurrentUser, requestJson, unwrapList } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import RequireLoginModal from '../components/RequireLoginModal.jsx'
import {
  getCommentCount,
  getContentPreview,
  getLikeCount,
  getPostId,
  getTitle,
} from './boardHelpers.js'

function MyPage() {
  const navigate = useNavigate()
  const currentUser = getCurrentUser()
  const [posts, setPosts] = useState([])
  const [status, setStatus] = useState({ type: 'idle', message: '' })

  useEffect(() => {
    if (!currentUser?.userId) return

    let ignore = false

    async function loadMyPosts() {
      setStatus({ type: 'loading', message: '내가 작성한 게시글을 불러오는 중입니다.' })

      try {
        const data = await requestJson('/api/posts')
        if (ignore) return

        setPosts(unwrapList(data))
        setStatus({ type: 'success', message: '' })
      } catch (error) {
        if (ignore) return
        setStatus({ type: 'error', message: error.message })
      }
    }

    loadMyPosts()

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
        description="내 계정 정보와 내가 작성한 여행 게시글을 확인합니다."
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
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>MY POSTS</span>
            <h2>내가 작성한 게시글</h2>
          </div>
          <p>게시글을 누르면 상세 페이지로 이동합니다.</p>
        </div>

        {status.type === 'loading' && <div className="travel-status loading">{status.message}</div>}
        {status.type === 'error' && <div className="travel-status error">{status.message}</div>}

        {status.type !== 'loading' && myPosts.length === 0 && (
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
    </>
  )
}

function createInitial(value) {
  return (value?.[0] || 'U').toUpperCase()
}

export default MyPage
