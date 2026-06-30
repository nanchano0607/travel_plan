package com.min.edu.board.controller;

import com.min.edu.board.entity.CommentImageEntity;
import com.min.edu.board.service.CommentImageService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentImageCtrl {

    private final CommentImageService commentImageService;

    // 이미지 업로드
    @PostMapping("/{commentId}/images")
    public ApiResponse<Void> uploadImage(@PathVariable Long commentId,
                                         @RequestParam("file")MultipartFile file) throws IOException {
        commentImageService.uploadImage(commentId, file);
        return ApiResponse.success("이미지가 업로드되었습니다.", null);
    }

    // 댓글 이미지 전체 조회
    @GetMapping("/{commentId}/images")
    public ApiResponse<List<CommentImageEntity>> getImages(@PathVariable Long commentId) {
        return ApiResponse.success(commentImageService.getImagesByCommentId(commentId));
    }

    // 댓글 이미지 삭제
    @DeleteMapping("/{commentId}/images/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long commentId,
                                         @PathVariable Long imageId) {
        commentImageService.deleteImage(commentId, imageId);
        return ApiResponse.success("이미지가 삭제되었습니다.", null);
    }

}
