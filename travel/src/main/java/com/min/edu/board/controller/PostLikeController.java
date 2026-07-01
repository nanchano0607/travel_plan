package com.min.edu.board.controller;

import com.min.edu.board.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
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
}