package com.min.edu.board.service;

import com.min.edu.board.dto.PostDto;
import com.min.edu.board.entity.PostEntity;
import com.min.edu.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 게시글 작성
    @Transactional
    public PostDto createBoard(PostDto dto) {
        PostEntity board = PostEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .userId(dto.getUserId())
                .planId(dto.getPlanId()) // 추가
                .build();
        PostEntity savedBoard = postRepository.save(board);
        return new PostDto(savedBoard);
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<PostDto> getAllBoards() {
        return postRepository.findAll()
                .stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostDto getBoard(Long id) {
        PostEntity board = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id: " + id));
        return new PostDto(board);
    }

    // 게시글 수정
    @Transactional
    public PostDto updateBoard(Long id, PostDto dto) {
        PostEntity board = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id: " + id));
        board.update(dto.getTitle(), dto.getContent());
        return new PostDto(board);
    }

    @Transactional
    public void deleteBoard(Long id, Long userId) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + id));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalStateException("본인 글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // 게시글 목록 페이징
    @Transactional(readOnly = true)
    public Page<PostDto> getBoardList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable)
                .map(PostDto::new);
    }

    // 제목 검색
    @Transactional(readOnly = true)
    public Page<PostDto> searchByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByTitleContaining(title, pageable)
                .map(PostDto::new);
    }

    // 작성자 검색
    @Transactional(readOnly = true)
    public Page<PostDto> searchByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByUserId(userId, pageable)
                .map(PostDto::new);
    }
}