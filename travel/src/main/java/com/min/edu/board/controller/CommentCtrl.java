package com.min.edu.board.controller;


import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.min.edu.board.dto.CommentsRequestDTO;
import com.min.edu.board.dto.CommentsResponseDTO;
import com.min.edu.board.service.CommentsService;
import com.min.edu.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentCtrl {

    private final CommentsService commentsService;

    // 댓글 작성
    @PostMapping
    public ApiResponse<CommentsResponseDTO> createComment(@RequestBody CommentsRequestDTO requestDTO, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        requestDTO.setUserId(userId);
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
                                                          @RequestBody String newContent,
                                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(commentsService.updateComment(commentId, newContent, userId));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentsService.deleteComment(commentId, userId);
        return ApiResponse.success("댓글이 삭제 되었습니다.", null);
    }

    // 특정 게시글 댓글 수 조회
    @GetMapping("/post/{postId}/count")
    public ApiResponse<Integer> getCommentCount(@PathVariable Long postId) {
        return ApiResponse.success(commentsService.getCommentCount(postId));
    }

    // 내가 작성한 댓글 전체 조회
    @GetMapping("/user/me")
    public ApiResponse<List<CommentsResponseDTO>> getMyComments(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(commentsService.getCommentsByUserId(userId));
    }

}
