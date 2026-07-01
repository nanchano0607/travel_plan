package com.min.edu.board.controller;

import com.min.edu.board.dto.PostDto;
import com.min.edu.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostDto> createBoard(@RequestBody PostDto dto) {
        return ResponseEntity.ok(postService.createBoard(dto));
    }

    // 게시글 전체 조회 (여행 계획 공유)
    @GetMapping
    public ResponseEntity<List<PostDto>> getAllBoards() {
        return ResponseEntity.ok(postService.getAllBoards());
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getBoard(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updateBoard(@PathVariable Long id, @RequestBody PostDto dto) {
        return ResponseEntity.ok(postService.updateBoard(id, dto));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        postService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    // 게시글 목록 페이징
    @GetMapping("/page")
    public ResponseEntity<Page<PostDto>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getBoardList(page, size));
    }

    // 제목 검색
    @GetMapping("/search/title")
    public ResponseEntity<Page<PostDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.searchByTitle(title, page, size));
    }

    // 작성자 검색
    @GetMapping("/search/user")
    public ResponseEntity<Page<PostDto>> searchByUserId(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.searchByUserId(userId, page, size));
    }
}