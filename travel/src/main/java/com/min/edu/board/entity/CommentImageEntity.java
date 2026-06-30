package com.min.edu.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CommentImageId.class)
public class CommentImageEntity {

    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Id
    @Column(name = "image_id")
    private Long imageId;

}
