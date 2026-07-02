package com.min.edu.board.dto;

import com.min.edu.board.entity.CommentsEntity;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsResponseDTO {

    private Long commentId;            // 댓글 번호
    private Long postId;               // 게시글 번호

    @JsonIgnore
    private Long userId;             // 작성자
    private Long parentCommentId;      // 부모 댓글 번호
    private String content;            // 내용
    private LocalDateTime createdAt;   // 작성 일자
    private LocalDateTime updateAt;    // 수정 일자

    public static CommentsResponseDTO fromEntity(CommentsEntity entity) {
        return CommentsResponseDTO.builder()
                .commentId(entity.getCommentId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .parentCommentId(entity.getParentCommentId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updateAt(entity.getUpdateAt())
                .build();
    }

}
