package com.min.edu.board.controller;


import com.min.edu.board.dto.CommentsRequestDTO;
import com.min.edu.board.dto.CommentsResponseDTO;
import com.min.edu.board.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentCtrl {

    private final CommentsService commentsService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<CommentsResponseDTO> createComment(@RequestBody CommentsRequestDTO requestDTO) {
        return ResponseEntity.ok(commentsService.createComment(requestDTO));
    }

    // 특정 게시글 댓글 전체 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentsResponseDTO>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentsService.getCommentsByPostId(postId));
    }

    // 특정 게시글 최상위 댓글만 조회 (대댓글 제외)
    @GetMapping("/post/{postId}/top")
    public ResponseEntity<List<CommentsResponseDTO>> getTopLevelComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentsService.getTopLevelComments(postId));
    }

    // 대댓글 조회
    @GetMapping("/{parentCommentId}/replies")
    public ResponseEntity<List<CommentsResponseDTO>> getReplies(@PathVariable Long parentCommentId) {
        return ResponseEntity.ok(commentsService.getReplies(parentCommentId));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentsResponseDTO> updateComment(@PathVariable Long commentId,
                                                        @RequestBody String newContent) {
        return ResponseEntity.ok(commentsService.updateComment(commentId, newContent));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentsService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // 특정 게시글 댓글 수 조회
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Integer> getCommentCount(@PathVariable Long postId) {
        return ResponseEntity.ok(commentsService.getCommentCount(postId));
    }

}
