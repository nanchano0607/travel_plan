package com.min.edu.board.controller;


import com.min.edu.board.dto.CommentsRequestDTO;
import com.min.edu.board.dto.CommentsResponseDTO;
import com.min.edu.board.service.CommentsService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentCtrl {

    private final CommentsService commentsService;

    // 댓글 작성
    @PostMapping("/comment")
    public ApiResponse<CommentsResponseDTO> createComment(@RequestBody CommentsRequestDTO requestDTO) {
        return ApiResponse.success(commentsService.createComment(requestDTO));
    }

    // 특정 게시글 댓글 전체 조회
    @GetMapping("/post/{postId}")
    public ApiResponse<List<CommentsResponseDTO>> getCommentsByPostId(@PathVariable Long postId) {
        return ApiResponse.success(commentsService.getCommentsByPostId(postId));
    }

    // 특정 게시글 최상위 댓글만 조회 (대댓글 제외)
    @GetMapping("/post/{postId}/top")
    public ApiResponse<List<CommentsResponseDTO>> getTopLevelComments(@PathVariable Long postId) {
        return ApiResponse.success(commentsService.getTopLevelComments(postId));
    }

    // 대댓글 조회
    @GetMapping("/{parentCommentId}/replies")
    public ApiResponse<List<CommentsResponseDTO>> getReplies(@PathVariable Long parentCommentId) {
        return ApiResponse.success(commentsService.getReplies(parentCommentId));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ApiResponse<CommentsResponseDTO> updateComment(@PathVariable Long commentId,
                                                        @RequestBody String newContent) {
        return ApiResponse.success(commentsService.updateComment(commentId, newContent));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        commentsService.deleteComment(commentId);
        return ApiResponse.success("댓글이 삭제 되었습니다.", null);
    }

    // 특정 게시글 댓글 수 조회
    @GetMapping("/post/{postId}/count")
    public ApiResponse<Integer> getCommentCount(@PathVariable Long postId) {
        return ApiResponse.success(commentsService.getCommentCount(postId));
    }

}
