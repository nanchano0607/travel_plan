import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { requestJson, unwrapList } from '../api/http.js'
import {
  getCommentCount,
  getContentPreview,
  getLikeCount,
  getPostId,
  getTitle,
  getWriter,
} from './boardHelpers.js'

function OverviewPage({ onNavigate }) {
  const [posts, setPosts] = useState([])
  const [postStatus, setPostStatus] = useState({
    type: 'loading',
    message: '인기 게시글을 불러오는 중입니다.',
  })
  const navigate = useNavigate()

  useEffect(() => {
    let ignore = false

    async function loadPopularPosts() {
      try {
        const data = await requestJson('/api/posts')
        if (ignore) return

        setPosts(unwrapList(data))
        setPostStatus({ type: 'success', message: '' })
      } catch {
        if (ignore) return
        setPostStatus({ type: 'error', message: '게시글을 불러오지 못했습니다.' })
      }
    }

    loadPopularPosts()

    return () => {
      ignore = true
    }
  }, [])

  const popularPosts = useMemo(() => {
    return [...posts]
      .sort((a, b) => getLikeCount(b) - getLikeCount(a))
      .slice(0, 3)
  }, [posts])

  return (
    <>
      <section className="home-hero">
        <div className="home-hero-copy">
          <span className="pill">AI TRAVEL PLANNER</span>
          <h1>여행 계획, 이제 혼자 고민하지 마세요</h1>
          <p>
            가고 싶은 지역과 조건을 입력하면 AI가 여행 일정을 제안하고,
            완성된 계획은 게시판에서 사람들과 공유할 수 있습니다.
          </p>
          <div className="home-hero-actions">
            <button className="primary" onClick={() => onNavigate('travel')}>AI 여행 계획 만들기</button>
            <button className="secondary" onClick={() => onNavigate('board')}>공유된 여행 보기</button>
          </div>
        </div>
      </section>

      <section className="section-block">
        <div className="section-heading">
          <div>
            <span>POPULAR POSTS</span>
            <h2>인기 여행 게시글</h2>
          </div>
        </div>

        {postStatus.type === 'loading' && <StatusMessage type="loading" message={postStatus.message} />}
        {postStatus.type === 'error' && <StatusMessage type="error" message={postStatus.message} />}

        {postStatus.type !== 'loading' && popularPosts.length === 0 && (
          <div className="home-empty-card">
            <h3>아직 인기 게시글이 없습니다</h3>
            <p>첫 번째 여행 글을 작성해서 사람들과 일정을 공유해보세요.</p>
            <button className="primary" onClick={() => navigate('/board/write')}>게시글 작성하기</button>
          </div>
        )}

        {popularPosts.length > 0 && (
          <div className="post-grid">
            {popularPosts.map((post, index) => {
              const postId = getPostId(post)

              return (
                <article className="post-card" key={postId || `${getTitle(post)}-${index}`}>
                  <button
                    className={`post-cover cover-${(index % 3) + 1}`}
                    onClick={() => postId && navigate(`/board/${postId}`)}
                    disabled={!postId}
                  >
                    <span>TOP {index + 1}</span>
                  </button>
                  <div className="post-content">
                    <span>{getWriter(post)}</span>
                    <h3>{getTitle(post)}</h3>
                    <p>{getContentPreview(post)}</p>
                    <div>
                      <small>좋아요 {getLikeCount(post)}</small>
                      <small>댓글 {getCommentCount(post)}</small>
                    </div>
                    <div className="post-actions">
                      <button type="button" onClick={() => postId && navigate(`/board/${postId}`)} disabled={!postId}>
                        자세히 보기
                      </button>
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

function StatusMessage({ type, message }) {
  return <div className={`travel-status ${type}`}>{message}</div>
}

export default OverviewPage
