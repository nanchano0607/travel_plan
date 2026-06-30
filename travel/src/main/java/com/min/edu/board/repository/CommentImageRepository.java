package com.min.edu.board.repository;

import com.min.edu.board.entity.CommentImageEntity;
import com.min.edu.board.entity.CommentImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentImageRepository extends JpaRepository<CommentImageEntity, CommentImageId> {

    // 특정 댓글의 이미지 전체 조회
    List<CommentImageEntity> findByCommentId(Long commentId);

    // 특정 댓글 이미지 삭제
    void deleteByCommentId(Long commentId);

}
