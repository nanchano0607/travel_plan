package com.min.edu.board.controller;

import com.min.edu.board.dto.PostDto;
import com.min.edu.board.service.PostService;
import com.min.edu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.List;





@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ApiResponse<PostDto> createBoard(
            @RequestBody PostDto dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dto.setUserId(userId);
        return ApiResponse.success(postService.createBoard(dto));
    }

    // 게시글 전체 조회
    @GetMapping
    public ApiResponse<List<PostDto>> getAllBoards() {
        return ApiResponse.success(postService.getAllBoards());
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<PostDto> getBoard(@PathVariable Long id) {
        return ApiResponse.success(postService.getBoard(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ApiResponse<PostDto> updateBoard(
            @PathVariable Long id,
            @RequestBody PostDto dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        dto.setUserId(userId);
        return ApiResponse.success(postService.updateBoard(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBoard(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deleteBoard(id, userId);   // 서비스에서 작성자 일치 검증
        return ApiResponse.success(null);
    }

    // 게시글 목록 페이징
    @GetMapping("/page")
    public ApiResponse<Page<PostDto>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.getBoardList(page, size));
    }

    /// 제목 검색
    @GetMapping("/search/title")
    public ApiResponse<Page<PostDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.searchByTitle(title, page, size));
    }

    // 작성자 검색
    @GetMapping("/search/user")
    public ApiResponse<Page<PostDto>> searchByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(postService.searchByUserId(userId, page, size));
    }
}