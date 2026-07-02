package com.min.edu.board.service;

import com.min.edu.board.dto.CommentsRequestDTO;
import com.min.edu.board.dto.CommentsResponseDTO;
import com.min.edu.board.entity.CommentsEntity;
import com.min.edu.board.repository.CommentsRepository;

import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
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
    public CommentsResponseDTO updateComment(Long commentId, String newContent, Long userId) {
        CommentsEntity comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException((ErrorCode.UNAUTHORIZED_USER));
        }
        comment.setContent(newContent);
        return CommentsResponseDTO.fromEntity(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommentsEntity comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        commentsRepository.delete(comment);
    }

    // 특정 게시글 댓글 수 조회
    @Transactional(readOnly = true)
    public int getCommentCount(Long postId) {
        return commentsRepository.countByPostId(postId);
    }

    // 특정 유저가 작성한 댓글 전체 조회
    @Transactional(readOnly = true)
    public List<CommentsResponseDTO> getCommentsByUserId(Long userId) {
        return commentsRepository.findByUserId(userId)
                .stream()
                .map(CommentsResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
