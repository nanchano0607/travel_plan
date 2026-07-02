package com.min.edu.board.repository;

import com.min.edu.board.entity.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {

    // 게시글의 댓글 전체 조회
    List<CommentsEntity> findByPostId(Long postId);

    // 게시글의 댓글 최상위 댓글만 조회(대댓글 제외)
    List<CommentsEntity> findByPostIdAndParentCommentIdIsNull(Long postId);

    // 댓글의 대댓글 조회
    List<CommentsEntity> findByParentCommentId(Long parentCommentId);

    // 특정 유저의 댓글 전체 조회
    List<CommentsEntity> findByUserId(Long userId);

    // 게시글 댓글 수 조회
    int countByPostId(Long postId);


    Long postId(Long postId);

    Object userId(Long userId);
}
