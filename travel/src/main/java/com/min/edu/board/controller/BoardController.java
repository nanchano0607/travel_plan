package com.min.edu.board.controller;

import com.min.edu.board.dto.BoardDto;
import com.min.edu.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<BoardDto> createBoard(@RequestBody BoardDto dto) {
        return ResponseEntity.ok(boardService.createBoard(dto));
    }

    // 게시글 전체 조회 (여행 계획 공유)
    @GetMapping
    public ResponseEntity<List<BoardDto>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardDto> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getBoard(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable Long id, @RequestBody BoardDto dto) {
        return ResponseEntity.ok(boardService.updateBoard(id, dto));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    // 게시글 목록 페이징
    @GetMapping("/page")
    public ResponseEntity<Page<BoardDto>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(boardService.getBoardList(page, size));
    }

    // 제목 검색
    @GetMapping("/search/title")
    public ResponseEntity<Page<BoardDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(boardService.searchByTitle(title, page, size));
    }

    // 작성자 검색
    @GetMapping("/search/user")
    public ResponseEntity<Page<BoardDto>> searchByUserId(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(boardService.searchByUserId(userId, page, size));
    }
}