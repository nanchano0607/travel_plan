package com.min.edu.board.controller;

import com.min.edu.board.dto.PostImageDto;
import com.min.edu.board.service.ImageService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/images")
public class ImageController {

    private final ImageService imageService;

    // 이미지 업로드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostImageDto> uploadImage(@PathVariable Long postId,
                                                  @RequestParam("file") MultipartFile file) throws IOException {
        PostImageDto saved = imageService.uploadImage(postId, file);
        return ApiResponse.success("이미지가 업로드되었습니다.", saved);
    }

    // 게시글 이미지 전체 조회
    @GetMapping
    public ApiResponse<List<PostImageDto>> getImages(@PathVariable Long postId) {
        return ApiResponse.success(imageService.getImagesByPostId(postId));
    }

    // 게시글 이미지 삭제
    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long postId, @PathVariable Long imageId) {
        imageService.deleteImage(postId, imageId);
        return ApiResponse.success("이미지가 삭제되었습니다.", null);
    }
}
