package com.min.edu.board.service;

import com.min.edu.board.entity.CommentImageEntity;
import com.min.edu.board.entity.ImageEntity;
import com.min.edu.board.repository.CommentImageRepository;
import com.min.edu.board.repository.ImageRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentImageService {

    private final CommentImageRepository commentImageRepository;
    private final ImageRepository imageRepository;

    // 이미지 업도르 및 댓글에 연결
    @Transactional
    public void uploadImage(Long commentId, MultipartFile file) throws IOException {
        // 파일 저장 경로
        String uploadDir = "uploads/comment/";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        // 폴더가 없을 시 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // 파일 저장
        file.transferTo(new File(filePath));

        // image 테이블에 저장
        ImageEntity image = ImageEntity.builder()
                .fileName(fileName)
                .filePath(filePath)
                .imageSize(file.getSize())
                .build();
        ImageEntity savesImage = imageRepository.save(image);

        // comment_image 테이블에 연결
        CommentImageEntity commentImage = CommentImageEntity.builder()
                .commentId(commentId)
                .imageId(savesImage.getImageId())
                .build();
        commentImageRepository.save(commentImage);

    }

    // 특정 댓글 이미지 전체 조회
    @Transactional(readOnly = true)
    public List<CommentImageEntity> getImagesByCommentId(Long commentId) {
        return commentImageRepository.findByCommentId(commentId);
    }

    // 특정 댓글 이미지 삭제
    public void deleteImage(Long commentId, Long imageId) {
        CommentImageEntity commentImage = commentImageRepository
                .findById(new com.min.edu.board.entity.CommentImageId(commentId, imageId))
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
        commentImageRepository.delete(commentImage);
    }

}
