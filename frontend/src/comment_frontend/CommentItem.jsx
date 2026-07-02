import { useState, useEffect } from "react";
import { resolveFileUrl } from "../api/http.js";
import {
    getReplies,
    createComment,
    updateComment,
    deleteComment,
    addCommentLike,
    removeCommentLike,
    getCommentLikeCount,
    checkCommentLiked,
    uploadCommentImage,
    getCommentImages,
} from "./CommentApi.js";

/**
 * 댓글 한 개 (대댓글 포함) 렌더링
 *
 * props
 * - comment: 댓글 데이터 (commentId, userId, content, createdAt ...)
 * - postId: 게시글 번호 (대댓글 작성시 필요)
 * - currentUserId: 로그인한 유저 id (좋아요, 수정/삭제 권한 체크용)
 * - onDeleted: 댓글 삭제 후 부모에게 알려주는 콜백
 */
export default function CommentItem({ comment, postId, currentUserId, onDeleted }) {
    const [replies, setReplies] = useState([]);
    const [showReplies, setShowReplies] = useState(false);
    const [replyMode, setReplyMode] = useState(false);
    const [replyContent, setReplyContent] = useState("");

    const [editMode, setEditMode] = useState(false);
    const [editContent, setEditContent] = useState(comment.content);

    const [liked, setLiked] = useState(false);
    const [likeCount, setLikeCount] = useState(0);

    const [images, setImages] = useState([]);
    const [uploading, setUploading] = useState(false);

    const isMine = currentUserId && currentUserId === comment.userId;

    // 좋아요 상태 + 이미지 초기 로딩
    useEffect(() => {
        if (!comment.commentId) return;

        getCommentLikeCount(comment.commentId)
            .then((res) => setLikeCount(res.data ?? 0))
            .catch(() => {});

        if (currentUserId) {
            checkCommentLiked(comment.commentId, currentUserId)
                .then((res) => setLiked(!!res.data))
                .catch(() => {});
        }

        getCommentImages(comment.commentId)
            .then((res) => setImages(res.data ?? []))
            .catch(() => {});
    }, [comment.commentId, currentUserId]);

    // 대댓글 펼치기
    const handleToggleReplies = async () => {
        if (!showReplies) {
            try {
                const res = await getReplies(comment.commentId);
                setReplies(res.data ?? []);
            } catch (err) {
                console.error("대댓글 조회 실패", err);
            }
        }
        setShowReplies((prev) => !prev);
    };

    // 대댓글 작성
    const handleReplySubmit = async () => {
        if (!replyContent.trim()) return;
        try {
            await createComment({
                postId,
                userId: currentUserId,
                parentCommentId: comment.commentId,
                content: replyContent,
            });
            setReplyContent("");
            setReplyMode(false);
            const res = await getReplies(comment.commentId);
            setReplies(res.data ?? []);
            setShowReplies(true);
        } catch (err) {
            console.error("대댓글 작성 실패", err);
            alert("대댓글 작성에 실패했어요.");
        }
    };

    // 댓글 수정
    const handleEditSubmit = async () => {
        if (!editContent.trim()) return;
        try {
            await updateComment(comment.commentId, editContent);
            comment.content = editContent; // 화면에 바로 반영
            setEditMode(false);
        } catch (err) {
            console.error("댓글 수정 실패", err);
            alert("댓글 수정에 실패했어요.");
        }
    };

    // 댓글 삭제
    const handleDelete = async () => {
        if (!window.confirm("댓글을 삭제할까요?")) return;
        try {
            await deleteComment(comment.commentId);
            onDeleted?.(comment.commentId);
        } catch (err) {
            console.error("댓글 삭제 실패", err);
            alert("댓글 삭제에 실패했어요.");
        }
    };

    // 좋아요 토글
    const handleToggleLike = async () => {
        if (!currentUserId) {
            alert("로그인이 필요해요.");
            return;
        }
        try {
            if (liked) {
                await removeCommentLike(comment.commentId, currentUserId);
                setLiked(false);
                setLikeCount((c) => Math.max(0, c - 1));
            } else {
                await addCommentLike(comment.commentId, currentUserId);
                setLiked(true);
                setLikeCount((c) => c + 1);
            }
        } catch (err) {
            console.error("좋아요 처리 실패", err);
        }
    };

    // 이미지 업로드
    const handleImageUpload = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        setUploading(true);
        try {
            await uploadCommentImage(comment.commentId, file);
            const res = await getCommentImages(comment.commentId);
            setImages(res.data ?? []);
        } catch (err) {
            console.error("이미지 업로드 실패", err);
            alert("이미지 업로드에 실패했어요");
        } finally {
            setUploading(false);
            e.target.value = "";
        }
    };

    return (
        <article className="comment-item">
            <div className="comment-item-head">
                <strong>유저 #{comment.userId}</strong>
                <small>{comment.createdAt ? new Date(comment.createdAt).toLocaleString() : ""}</small>
            </div>

            {editMode ? (
                <div className="comment-edit-box">
                    <textarea
                        value={editContent}
                        onChange={(e) => setEditContent(e.target.value)}
                        rows={2}
                    />
                    <div className="comment-item-actions">
                        <button className="secondary" onClick={handleEditSubmit}>저장</button>
                        <button className="secondary" onClick={() => setEditMode(false)}>취소</button>
                    </div>
                </div>
            ) : (
                <p>{comment.content}</p>
            )}

            {/* 첨부 이미지 */}
            {images.length > 0 && (
                <div className="comment-image-list">
                    {images.map((img) => (
                        <img
                            key={img.imageId}
                            src={resolveCommentImageUrl(img)}
                            alt={img.fileName || "댓글 첨부 이미지"}
                            className="comment-image-thumb"
                        />
                    ))}
                </div>
            )}

            <div className="comment-item-actions">
                <button className="secondary" onClick={handleToggleLike}>
                    {liked ? "♥" : "♡"} {likeCount}
                </button>
                <button className="secondary" onClick={() => setReplyMode((v) => !v)}>답글</button>

                {isMine && !editMode && (
                    <>
                        <button className="secondary" onClick={() => setEditMode(true)}>수정</button>
                        <button className="secondary" onClick={handleDelete}>삭제</button>
                    </>
                )}

                <label className="comment-image-upload secondary">
                    {uploading ? "업로드 중..." : "이미지 첨부"}
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleImageUpload}
                        disabled={uploading}
                        hidden
                    />
                </label>

                <button className="secondary" onClick={handleToggleReplies}>
                    {showReplies ? "답글 숨기기" : "답글 보기"}
                </button>
            </div>

            {replyMode && (
                <div className="reply-write-box">
                    <textarea
                        value={replyContent}
                        onChange={(e) => setReplyContent(e.target.value)}
                        placeholder="답글을 입력해주세요"
                        rows={2}
                    />
                    <div className="comment-item-actions">
                        <button className="primary" onClick={handleReplySubmit}>등록</button>
                        <button className="secondary" onClick={() => setReplyMode(false)}>취소</button>
                    </div>
                </div>
            )}

            {showReplies && replies.length > 0 && (
                <div className="reply-list">
                    {replies.map((reply) => (
                        <CommentItem
                            key={reply.commentId}
                            comment={reply}
                            postId={postId}
                            currentUserId={currentUserId}
                            onDeleted={(id) => setReplies((prev) => prev.filter((r) => r.commentId !== id))}
                        />
                    ))}
                </div>
            )}
        </article>
    );
}

function resolveCommentImageUrl(image) {
    const rawPath = image?.imageUrl || image?.filePath || "";
    const normalizedPath = rawPath.replace(/\\/g, "/");
    const uploadsIndex = normalizedPath.indexOf("/uploads/");

    if (uploadsIndex >= 0) {
        return resolveFileUrl(normalizedPath.slice(uploadsIndex));
    }

    const relativeUploadsIndex = normalizedPath.indexOf("uploads/");
    if (relativeUploadsIndex >= 0) {
        return resolveFileUrl(`/${normalizedPath.slice(relativeUploadsIndex)}`);
    }

    return resolveFileUrl(normalizedPath);
}
