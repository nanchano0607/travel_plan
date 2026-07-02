import { useState, useEffect } from "react";
import { getTopLevelComments, createComment, getCommentCount } from "./CommentApi.js";
import CommentItem from "./CommentItem";

/**
 * 게시글 상세 페이지에 그대로 넣는 댓글 영역
 * 
 * 사용 예 : 
 * <CommentSection postId={post.postId} currentUserId={loginUser?.userId}/>
 */
export default function CommentSection({ postId, currentUserId }) {
    const [comment, setComment] = useState([]);
    const [content, setContent] = useState("");
    const [totalCount, setTotalCount] = useState(0);
    const [loading, setLoading] = useState(true);

    const loadComment = async () => {
        try {
            const [listRes, countRes] = await Promise.all([
                getTopLevelComments(postId),
                getCommentCount(postId),
            ]);
            setComment(listRes.data ?? []);
            setTotalCount(countRes.data ?? 0);
        } catch (error) {
            console.error("댓글 목록 조회 실패", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (postId) loadComment();
    }, [postId]);

    const handleSubmit = async () => {
        if (!content.trim()) return;
        if (!currentUserId) {
            alert("로그인이 필요한 서비스 입니다.");
            return;
        }
        try {
            await createComment({
                postId,
                userId: currentUserId,
                parentCommentId: null,
                content,
            });
            setContent("");
            loadComment();
        } catch (err) {
            console.error("댓글 작성 실패", err);
            alert("댓글 작성에 실패했어요. 😥");
        }
    };

    const handleDeleted = (commentId) => {
        setComment((prev) => prev.filter((c) => c.commentId !== commentId));
        setTotalCount((c) => Math.max(0, c - 1));
    };

    return (
        <>
            <h2>댓글 {totalCount}</h2>

            <form className="comment-form" onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
                <label>
                    댓글 작성
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder={currentUserId ? "댓글을 입력해주세요" : "로그인 후 입력 가능합니다."}
                        rows={3}
                        disabled={!currentUserId}
                    />
                </label>
                <button className="primary wide" type="submit" disabled={!currentUserId}>등록</button>
            </form>

            {loading ? (
                <p className="field-help">댓글을 불러오는중...</p>
            ) : (
                <div className="comment-list">
                    {comment.length === 0 && <p className="field-help">아직 댓글이 없어요. 첫 댓글을 남겨보세요!</p>}
                    {comment.map((c) => (
                        <CommentItem
                            key={c.commentId}
                            comment={c}
                            postId={postId}
                            currentUserId={currentUserId}
                            onDeleted={handleDeleted}
                        />
                    ))}
                </div>
            )}
        </>
    );
}
