package com.min.edu.board.controller;

import java.util.List;

import com.min.edu.board.dto.PostDto;
import com.min.edu.board.service.PostLikeService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요 토글 (추가/취소)
    @PostMapping("/{postId}/likes/{userId}")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        boolean result = postLikeService.toggleLike(postId, userId);
        return ResponseEntity.ok(result);
    }

    // 내가 좋아요 한 게시글 목록 조회
    @GetMapping("/likes/me")
    public ApiResponse<List<PostDto>> getMyLikedPosts(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(postLikeService.getLikedPosts(userId));
    }
}