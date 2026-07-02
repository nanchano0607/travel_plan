package com.min.edu.board.repository;

import java.util.List;

import com.min.edu.board.entity.PostLikeEntity;
import com.min.edu.board.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLikeEntity, PostLikeId> {

    // 특정 유저가 좋아요 한 게시글 전체 조회
    List<PostLikeEntity> findById_UserId(Long userId);
}