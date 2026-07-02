import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import {  getAccessToken, getCurrentUserId, requestJson, resolveFileUrl, unwrapList } from '../api/http.js'
import PageHeader from '../components/PageHeader.jsx'
import RequireLoginModal from '../components/RequireLoginModal.jsx'
import { getContent, getPostId, getTitle } from './boardHelpers.js'

const EMPTY_FORM = {
  title: '',
  content: '',
}

function BoardWritePage() {
  const { postId } = useParams()
  const isEditMode = Boolean(postId)
  const [form, setForm] = useState(EMPTY_FORM)
  const [status, setStatus] = useState({ type: 'idle', message: '' })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isLoadingPost, setIsLoadingPost] = useState(isEditMode)
  const [existingImages, setExistingImages] = useState([])
  const [selectedFiles, setSelectedFiles] = useState([])
  const [deletingImageId, setDeletingImageId] = useState(null)
  const navigate = useNavigate()
  const userId = getCurrentUserId()
  const hasAccessToken = Boolean(getAccessToken())

  if (!hasAccessToken) {
    return <RequireLoginModal />
  }

  useEffect(() => {
    if (!isEditMode) return

    let ignore = false

    async function loadPost() {
      setIsLoadingPost(true)
      setStatus({ type: 'loading', message: '게시글 정보를 불러오는 중입니다.' })

      try {
        const [post, images] = await Promise.all([
          requestJson(`/api/posts/${postId}`),
          requestJson(`/api/posts/${postId}/images`).catch(() => []),
        ])
        if (ignore) return

        setForm({ title: getTitle(post), content: getContent(post) })
        setExistingImages(unwrapList(images))
        setStatus({ type: 'idle', message: '' })
      } catch (error) {
        if (ignore) return
        setStatus({ type: 'error', message: error.message })
      } finally {
        if (!ignore) setIsLoadingPost(false)
      }
    }

    loadPost()

    return () => {
      ignore = true
    }
  }, [isEditMode, postId])

  function handleSelectFiles(event) {
    setSelectedFiles(Array.from(event.target.files || []))
  }

  function removeSelectedFile(index) {
    setSelectedFiles((files) => files.filter((_, fileIndex) => fileIndex !== index))
  }

  async function handleDeleteExistingImage(imageId) {
    setDeletingImageId(imageId)

    try {
      await requestJson(`/api/posts/${postId}/images/${imageId}`, { method: 'DELETE' })
      setExistingImages((images) => images.filter((image) => image.imageId !== imageId))
    } catch (error) {
      setStatus({ type: 'error', message: error.message })
    } finally {
      setDeletingImageId(null)
    }
  }

  async function uploadSelectedFiles(targetPostId) {
    for (const file of selectedFiles) {
      const formData = new FormData()
      formData.append('file', file)
      await requestJson(`/api/posts/${targetPostId}/images`, { method: 'POST', body: formData })
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.title.trim() || !form.content.trim()) {
      setStatus({ type: 'error', message: '제목과 내용을 입력하세요.' })
      return
    }

    setIsSubmitting(true)
    setStatus({ type: 'loading', message: isEditMode ? '게시글을 수정하는 중입니다.' : '게시글을 저장하는 중입니다.' })

    try {
      if (isEditMode) {
        await requestJson(`/api/posts/${postId}`, {
          method: 'PUT',
          body: {
            title: form.title,
            content: form.content,
            ...(userId ? { userId: Number(userId) } : {}),
          },
        })

        if (selectedFiles.length > 0) {
          setStatus({ type: 'loading', message: '이미지를 업로드하는 중입니다.' })
          await uploadSelectedFiles(postId)
        }

        setStatus({ type: 'success', message: '게시글이 수정되었습니다.' })
        navigate(`/board/${postId}`)
        return
      }

      const createdPost = await requestJson('/api/posts', {
        method: 'POST',
        body: {
          title: form.title,
          content: form.content,
          ...(userId ? { userId: Number(userId) } : {}),
        },
      })

      const createdPostId = getPostId(createdPost)

      if (createdPostId && selectedFiles.length > 0) {
        setStatus({ type: 'loading', message: '이미지를 업로드하는 중입니다.' })
        await uploadSelectedFiles(createdPostId)
      }

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
        title={isEditMode ? '게시글 수정' : '게시글 작성'}
        description={isEditMode ? '작성한 게시글의 제목과 내용을 수정합니다.' : '새 여행 후기나 공유할 일정을 작성합니다.'}
        action={<button className="secondary" onClick={() => navigate(isEditMode ? `/board/${postId}` : '/board')}>{isEditMode ? '상세로' : '목록으로'}</button>}
      />

      <section className="board-write-page">
        <form className="board-write-card" onSubmit={handleSubmit}>
          <h2>{isEditMode ? '게시글 수정' : '새 게시글'}</h2>
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

          <label>
            이미지
            <input type="file" accept="image/*" multiple onChange={handleSelectFiles} />
          </label>

          {selectedFiles.length > 0 && (
            <ul className="image-pending-list">
              {selectedFiles.map((file, index) => (
                <li key={`${file.name}-${index}`}>
                  <span>{file.name}</span>
                  <button type="button" onClick={() => removeSelectedFile(index)}>제거</button>
                </li>
              ))}
            </ul>
          )}

          {isEditMode && existingImages.length > 0 && (
            <div className="image-preview-grid">
              {existingImages.map((image) => (
                <figure key={image.imageId}>
                  <img src={resolveFileUrl(image.imageUrl)} alt={image.fileName} />
                  <button
                    type="button"
                    onClick={() => handleDeleteExistingImage(image.imageId)}
                    disabled={deletingImageId === image.imageId}
                  >
                    {deletingImageId === image.imageId ? '삭제 중...' : '삭제'}
                  </button>
                </figure>
              ))}
            </div>
          )}

          {!userId && (
            <p className="field-help error">
              현재 Access Token에서 userId를 읽지 못했습니다. 백엔드 정책에 따라 저장이 실패할 수 있습니다.
            </p>
          )}

          {status.message && <div className={`travel-status ${status.type}`}>{status.message}</div>}

          <button className="primary wide" type="submit" disabled={isSubmitting || isLoadingPost}>
            {isSubmitting ? (isEditMode ? '수정 중...' : '저장 중...') : isEditMode ? '수정 완료' : '게시글 저장'}
          </button>
        </form>
      </section>
    </>
  )
}

export default BoardWritePage
