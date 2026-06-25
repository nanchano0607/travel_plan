package com.min.edu.board.service;

import com.min.edu.board.dto.BoardDto;
import com.min.edu.board.entity.BoardEntity;
import com.min.edu.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 게시글 작성
    @Transactional
    public BoardDto createBoard(BoardDto dto) {
        BoardEntity board = BoardEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .userId(dto.getUserId())
                .build();
        BoardEntity savedBoard = boardRepository.save(board);
        return new BoardDto(savedBoard);
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<BoardDto> getAllBoards() {
        return boardRepository.findAll()
                .stream()
                .map(BoardDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public BoardDto getBoard(Long id) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id: " + id));
        return new BoardDto(board);
    }

    // 게시글 수정
    @Transactional
    public BoardDto updateBoard(Long id, BoardDto dto) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id: " + id));
        board.update(dto.getTitle(), dto.getContent());
        return new BoardDto(board);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id: " + id));
        boardRepository.delete(board);
    }
}