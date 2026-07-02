package com.min.edu.board.repository;

import com.min.edu.board.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 제목 검색
    Page<PostEntity> findByTitleContaining(String title, Pageable pageable);

    // 작성자 검색
    Page<PostEntity> findByUserId(Long userId, Pageable pageable);
}