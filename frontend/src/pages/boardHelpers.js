export function getPostId(post) {
  return post.postId ?? post.id ?? post.boardId
}

export function getCommentId(comment) {
  return comment.commentId ?? comment.id
}

export function getTitle(post) {
  return post.title || post.postTitle || '제목 없는 게시글'
}

export function getContent(post) {
  return post.content || post.body || post.description || ''
}

export function getContentPreview(post, maxLength = 72) {
  const content = getContent(post)
  return content.length > maxLength ? `${content.slice(0, maxLength)}...` : content || '내용 미리보기가 없습니다.'
}

export function getWriter(item) {
  return item.nickname || item.writer || item.author || item.userName || (item.userId ? `USER ${item.userId}` : '작성자')
}

export function getLikeCount(item) {
  return Number(item.likeCount ?? item.likesCount ?? item.likes ?? item.likeCnt ?? 0)
}

export function getCommentCount(post) {
  return Number(post.commentCount ?? post.commentsCount ?? post.commentCnt ?? 0)
}

export function getCreatedTime(post) {
  return new Date(post.createdAt || post.createdDate || post.created_at || 0).getTime()
}
