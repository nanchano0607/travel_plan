package com.min.edu.board.repository;

import com.min.edu.board.entity.CommentLikesEntity;
import com.min.edu.board.entity.CommentLikesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikesRepository extends JpaRepository<CommentLikesEntity, CommentLikesId> {

    // 댓글 좋아요 수 조회
    int countByCommentId(Long commentId);

    // 특정 유저가 댓글 좋아요 눌렀는지 확인
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    // 댓글 좋아요 취소
    void deleteByCommentIdAndUserId(Long commentId, Long userId);

}
