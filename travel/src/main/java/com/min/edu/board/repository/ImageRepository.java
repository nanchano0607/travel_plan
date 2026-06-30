package com.min.edu.board.repository;
import java.util.List;
import com.min.edu.board.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByBoard_Id(Long boardId); // BoardEntity의 PK 필드명에 맞게 수정
}