package com.min.edu.board.service;

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
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final PostRepository postRepository;

    @Value("${file.upload-dir}") // application.properties에 저장 경로 설정 필요
    private String uploadDir;

    @Transactional
    public ImageEntity uploadImage(Long boardId, MultipartFile file) throws IOException {
        PostEntity post = postRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        String originalFilename = file.getOriginalFilename();
        String savedFilename = UUID.randomUUID() + "_" + originalFilename;
        String savePath = uploadDir + savedFilename;

        file.transferTo(new File(savePath));

        ImageEntity image = ImageEntity.builder()
                .filePath(savePath)
                .fileName(originalFilename)
                .imageSize(file.getSize())
                .post(post)
                .build();

        return imageRepository.save(image);
    }
}