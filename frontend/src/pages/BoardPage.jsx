import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { getCurrentUserId, requestJson, unwrapList } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import {
  getCommentCount,
  getContent,
  getContentPreview,
  getCreatedTime,
  getLikeCount,
  getPostId,
  getTitle,
  getWriter,
} from './boardHelpers.js'

function BoardPage() {
  const [posts, setPosts] = useState([])
  const [keyword, setKeyword] = useState('')
  const [sortType, setSortType] = useState('latest')
  const [status, setStatus] = useState({ type: 'idle', message: '' })
  const [likedPostIds, setLikedPostIds] = useState(() => new Set())
  const navigate = useNavigate()
  const userId = getCurrentUserId()

  const filteredPosts = useMemo(() => {
    const loweredKeyword = keyword.trim().toLowerCase()

    return [...posts]
      .filter((post) => {
        if (!loweredKeyword) return true

        return [getTitle(post), getContent(post), getWriter(post)]
          .some((value) => String(value).toLowerCase().includes(loweredKeyword))
      })
      .sort((a, b) => {
        if (sortType === 'likes') return getLikeCount(b) - getLikeCount(a)
        return getCreatedTime(b) - getCreatedTime(a)
      })
  }, [keyword, posts, sortType])

  useEffect(() => {
    loadPosts()
  }, [])

  async function loadPosts() {
    setStatus({ type: 'loading', message: '게시글 목록을 불러오는 중입니다.' })

    try {
      const data = await requestJson('/api/posts')
      const list = unwrapList(data)
      setPosts(list)
      setStatus({ type: 'success', message: `게시글 ${list.length}개를 불러왔습니다.` })
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  async function handleLikePost(post) {
    const postId = getPostId(post)

    if (!userId) {
      setStatus({ type: 'error', message: '좋아요는 로그인 후 사용할 수 있습니다.' })
      return
    }

    if (!postId) {
      setStatus({ type: 'error', message: '게시글 ID를 찾을 수 없습니다.' })
      return
    }

    const postIdKey = String(postId)
    const isLiked = likedPostIds.has(postIdKey)

    try {
      await requestJson(`/api/posts/${postId}/likes/${userId}`, { method: 'POST' })
      setLikedPostIds((previous) => {
        const next = new Set(previous)
        if (isLiked) {
          next.delete(postIdKey)
        } else {
          next.add(postIdKey)
        }
        return next
      })
      setStatus({ type: 'success', message: isLiked ? '좋아요를 취소했습니다.' : '좋아요를 눌렀습니다.' })
      await loadPosts()
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="COMMUNITY"
        title="여행 일정 게시판"
        description="게시글 목록을 보고, 상세 페이지에서 본문과 댓글을 확인할 수 있습니다."
        action={
          <div className="page-actions">
            <button className="secondary" onClick={loadPosts}>새로고침</button>
            <button className="primary" onClick={() => navigate('/board/write')}>글 작성</button>
          </div>
        }
      />

      <div className="board-toolbar">
        <div className="search-box board-search-box">
          <span>🔎</span>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="제목, 내용, 작성자 검색"
          />
          <button type="button" onClick={() => setKeyword('')}>초기화</button>
        </div>
        <div className="sort-toggle" aria-label="게시글 정렬">
          <button
            type="button"
            className={sortType === 'latest' ? 'active' : ''}
            onClick={() => setSortType('latest')}
          >
            최신순
          </button>
          <button
            type="button"
            className={sortType === 'likes' ? 'active' : ''}
            onClick={() => setSortType('likes')}
          >
            좋아요순
          </button>
        </div>
      </div>

      {status.message && <StatusMessage type={status.type} message={status.message} />}

      <div className="post-grid board-list-grid">
        {filteredPosts.map((post, index) => {
          const postId = getPostId(post)

          return (
            <article className="post-card" key={postId || `${getTitle(post)}-${index}`}>
              <div className="post-cover-wrap">
                <button
                  className={`post-cover cover-${(index % 3) + 1}`}
                  onClick={() => postId && navigate(`/board/${postId}`)}
                  disabled={!postId}
                >
                  <span>{postId ? `POST ${postId}` : 'POST'}</span>
                </button>
                <button
                  type="button"
                  className={`like-button floating-like-button ${likedPostIds.has(String(postId)) ? 'liked' : ''}`}
                  onClick={() => handleLikePost(post)}
                  aria-label="좋아요"
                >
                  ♥
                </button>
              </div>
              <div className="post-content">
                <span>{getWriter(post)}</span>
                <h3>{getTitle(post)}</h3>
                <p>{getContentPreview(post)}</p>
                <div>
                  <small>좋아요 {getLikeCount(post)}</small>
                  <small>댓글 {getCommentCount(post)}</small>
                </div>
                <div className="post-actions">
                  {postId ? <Link to={`/board/${postId}`}>상세 보기</Link> : <button disabled>상세 없음</button>}
                </div>
              </div>
            </article>
          )
        })}
      </div>

      {status.type !== 'loading' && filteredPosts.length === 0 && (
        <StatusMessage type="idle" message="표시할 게시글이 없습니다." />
      )}
    </>
  )
}

function StatusMessage({ type, message }) {
  return <div className={`travel-status ${type}`}>{message}</div>
}

export default BoardPage
