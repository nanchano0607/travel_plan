package com.min.edu.board.service;

import com.min.edu.board.entity.PostEntity;
import com.min.edu.board.entity.PostLikeEntity;
import com.min.edu.board.entity.PostLikeId;
import com.min.edu.board.repository.PostLikeRepository;
import com.min.edu.board.repository.PostRepository;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        PostLikeId likeId = new PostLikeId(postId, userId);

        if (postLikeRepository.existsById(likeId)) {
            // 이미 좋아요 누른 상태 → 취소
            postLikeRepository.deleteById(likeId);
            updateLikeCount(postId, -1);
            return false;
        } else {
            // 좋아요 안 누른 상태 → 추가
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

            PostLikeEntity like = PostLikeEntity.builder()
                    .id(likeId)
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(like);
            updateLikeCount(postId, 1);
            return true;
        }
    }

    private void updateLikeCount(Long postId, int delta) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        post.updateLikeCount(post.getLikeCount() + delta);
    }
}