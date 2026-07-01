package com.min.edu.board.dto;

import com.min.edu.board.entity.PostEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long planId;      // 추가
    private int likeCount;    // 추가

    public PostDto(PostEntity post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.userId = post.getUserId();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.planId = post.getPlanId();       // 추가
        this.likeCount = post.getLikeCount(); // 추가
    }
}