package com.min.edu.board.repository;

import com.min.edu.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 제목 검색
    Page<BoardEntity> findByTitleContaining(String title, Pageable pageable);

    // 작성자 검색
    Page<BoardEntity> findByUserId(String userId, Pageable pageable);
}