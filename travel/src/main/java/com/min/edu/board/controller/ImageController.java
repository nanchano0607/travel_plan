package com.min.edu.board.controller;
import com.min.edu.board.entity.ImageEntity;
import com.min.edu.board.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<?> uploadImage(@PathVariable Long boardId,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        ImageEntity saved = imageService.uploadImage(boardId, file);
        return ResponseEntity.ok(saved);
    }
}