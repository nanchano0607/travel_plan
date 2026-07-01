package com.min.edu.board.controller;

import com.min.edu.board.service.CommentLikeService;
import com.min.edu.board.service.CommentsService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentLikesctrl {

    private final CommentsService commentsService;
    private final CommentLikeService commentLikesService;

    // 좋아요 추가
    @PostMapping("/{commentId}/likes/{userId}")
    public ApiResponse<Void> addLike(@PathVariable Long commentId,
                                     @PathVariable Long userId) {
        commentLikesService.addLike(commentId, userId);
        return ApiResponse.success("좋아요를 눌렀습니다.", null);
    }

    // 좋아요 취소
    @DeleteMapping("/{commentId}/likes/{userId}")
    public ApiResponse<Void> removeLike(@PathVariable Long commentId,
                                        @PathVariable Long userId) {
        commentLikesService.removeLike(commentId, userId);
        return ApiResponse.success("좋아요를 취소했습니다.", null);
    }

    // 좋아요 수 조회
    @GetMapping("/{commentId}/likes/count")
    public ApiResponse<Integer> getLikeCount(@PathVariable Long commentId) {
        return ApiResponse.success(commentLikesService.getLikeCount(commentId));
    }

    // 좋아요 여부 확인
    @GetMapping("/{commentId}/likes/{userId}")
    public ApiResponse<Boolean> isLiked(@PathVariable Long commentId,
                                        @PathVariable Long userId) {
        return ApiResponse.success(commentLikesService.isLiked(commentId,userId));
    }

}
