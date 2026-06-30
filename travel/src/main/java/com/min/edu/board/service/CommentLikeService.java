package com.min.edu.board.service;

import com.min.edu.board.entity.CommentLikesEntity;
import com.min.edu.board.repository.CommentLikesRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikesRepository commentLikesRepository;

    // 좋아요
    @Transactional
    public void addLike(Long commentId, Long userId) {
        if (commentLikesRepository.existsByCommentIdAndUserId(commentId,userId)){
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }
        CommentLikesEntity like = CommentLikesEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        commentLikesRepository.save(like);
    }

    // 좋아요 취소
    @Transactional
    public void removeLike(Long commentId, Long userId) {
        if (!commentLikesRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new CustomException(ErrorCode.LIKE_NOT_FOUND);
        }
        commentLikesRepository.deleteByCommentIdAndUserId(commentId, userId);
    }

    // 좋아요 수 조회
    @Transactional(readOnly = true)
    public int getLikeCount(Long commentId) {
        return commentLikesRepository.countByCommentId(commentId);
    }

    // 좋아요 여부 확인
    @Transactional(readOnly = true)
    public boolean isLiked(Long commentId, Long userId) {
        return commentLikesRepository.existsByCommentIdAndUserId(commentId,userId);
    }

}
