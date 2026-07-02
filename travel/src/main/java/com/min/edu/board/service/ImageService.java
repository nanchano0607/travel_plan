package com.min.edu.board.service;

import com.min.edu.board.dto.PostImageDto;
import com.min.edu.board.entity.PostEntity;
import com.min.edu.board.entity.ImageEntity;
import com.min.edu.board.repository.PostRepository;
import com.min.edu.board.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final PostRepository postRepository;

    @Value("${file.upload-dir}") // application.properties에 저장 경로 설정 필요
    private String uploadDir;

    @Transactional
    public PostImageDto uploadImage(Long postId, MultipartFile file) throws IOException {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        String originalFilename = file.getOriginalFilename();
        String savedFilename = UUID.randomUUID() + "_" + originalFilename;

        // 프로젝트 루트 기준 절대경로로 변환
        String absoluteUploadDir = System.getProperty("user.dir") + "/" + uploadDir;
        String savePath = absoluteUploadDir + savedFilename;

        // 폴더 없으면 자동 생성
        File dir = new File(absoluteUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        file.transferTo(new File(savePath));

        ImageEntity image = ImageEntity.builder()
                .filePath(savePath)
                .fileName(originalFilename)
                .imageSize(file.getSize())
                .post(post)
                .build();

        ImageEntity saved = imageRepository.save(image);
        return PostImageDto.from(saved);
    }

    // 게시글 이미지 전체 조회
    @Transactional(readOnly = true)
    public List<PostImageDto> getImagesByPostId(Long postId) {
        return imageRepository.findByPost_Id(postId)
                .stream()
                .map(PostImageDto::from)
                .collect(Collectors.toList());
    }

    // 게시글 이미지 삭제
    @Transactional
    public void deleteImage(Long postId, Long imageId) {
        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지가 존재하지 않습니다."));

        if (image.getPost() == null || !image.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 이미지가 아닙니다.");
        }

        File file = new File(image.getFilePath());
        if (file.exists()) file.delete();

        imageRepository.delete(image);
    }
}
