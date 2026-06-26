package com.min.edu.board.dto;

import com.min.edu.board.entity.CommentsEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsRequestDTO {

    private Long postId;
    private Long userId;
    private Long parentCommentId;
    private String content;

    public CommentsEntity toEntity() {
        return CommentsEntity.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .content(content)
                .build();
    }

}
