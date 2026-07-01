package com.min.edu.board.service;

import com.min.edu.board.entity.CommentImageEntity;
import com.min.edu.board.entity.CommentImageId;
import com.min.edu.board.entity.ImageEntity;
import com.min.edu.board.repository.CommentImageRepository;
import com.min.edu.board.repository.ImageRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final CommentImageRepository commentImageRepository;
    private final ImageRepository imageRepository;

    // 이미지 업도르 및 댓글에 연결
    @Transactional
    public void uploadImage(Long commentId, MultipartFile file) throws IOException {
        // String uploadDir = "uploads/comment/";  ← 이 줄만 삭제
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;  // 이 줄은 그대로, 위에서 선언한 필드를 사용

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

    // 특정 댓글 이미지 삭제 <<- 수정 07/01
    // 이미지 Hard Delete
    public void deleteImage(Long commentId, Long imageId) {
        CommentImageEntity commentImage = commentImageRepository
                .findById(new CommentImageId(commentId, imageId))
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
        commentImageRepository.delete(commentImage);

        // image 테이블에서 삭제 + 실제 파일 Hard Delete
        ImageEntity image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

        File file = new File(image.getFilePath());
        if (file.exists()) file.delete();

        imageRepository.delete(image);
    }

}
