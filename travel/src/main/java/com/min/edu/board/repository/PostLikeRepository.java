package com.min.edu.board.repository;

import com.min.edu.board.entity.PostLikeEntity;
import com.min.edu.board.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLikeEntity, PostLikeId> {
}