package com.min.edu.board.service;

import com.min.edu.board.dto.CommentsRequestDTO;
import com.min.edu.board.dto.CommentsResponseDTO;
import com.min.edu.board.entity.CommentsEntity;
import com.min.edu.board.repository.CommentsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;

    // 댓글 작성
    @Transactional
    public CommentsResponseDTO createComment(CommentsRequestDTO requestDto) {
        CommentsEntity comments = requestDto.toEntity();
        return CommentsResponseDTO.fromEntity(commentsRepository.save(comments));
    }

    // 특정 게시글 댓글 전체 조회
    @Transactional(readOnly = true)
    public List<CommentsResponseDTO> getCommentsByPostId(Long postId) {
        return  commentsRepository.findByPostId(postId)
                .stream()
                .map(CommentsResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 게시글 최상위 댓글만 조회 (대댓글 제외)
    @Transactional(readOnly = true)
    public List<CommentsResponseDTO> getTopLevelComments(Long postId) {
        return commentsRepository.findByPostIdAndParentCommentIdIsNull(postId)
                .stream()
                .map(CommentsResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 대댓글 조회
    @Transactional(readOnly = true)
    public List<CommentsResponseDTO> getReplies(Long parentCommentId) {
        return commentsRepository.findByParentCommentId(parentCommentId)
                .stream()
                .map(CommentsResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public CommentsResponseDTO updateComment(Long commentId, String newContent) {
        CommentsEntity comments = commentsRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        comments.setContent(newContent);
        return CommentsResponseDTO.fromEntity(comments);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        CommentsEntity comments = commentsRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
        commentsRepository.delete(comments);
    }

    // 특정 게시글 댓글 수 조회
    @Transactional(readOnly = true)
    public int getCommentCount(Long postId) {
        return commentsRepository.countByPostId(postId);
    }

}
