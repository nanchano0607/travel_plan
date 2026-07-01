package com.min.edu.board.repository;
import java.util.List;
import com.min.edu.board.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByPost_Id(Long postId);
}