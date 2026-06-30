package com.min.edu.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imaged_id")
    private Long imageId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_size")
    private Long imageSize;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
