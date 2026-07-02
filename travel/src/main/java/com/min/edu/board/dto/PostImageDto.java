package com.min.edu.board.dto;

import com.min.edu.board.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImageDto {

    private Long imageId;
    private String fileName;
    private String imageUrl;

    public static PostImageDto from(ImageEntity image) {
        String normalizedPath = image.getFilePath().replace("\\", "/");
        int uploadsIndex = normalizedPath.indexOf("uploads/");
        String imageUrl = "/" + (uploadsIndex >= 0 ? normalizedPath.substring(uploadsIndex) : normalizedPath);

        return PostImageDto.builder()
                .imageId(image.getImageId())
                .fileName(image.getFileName())
                .imageUrl(imageUrl)
                .build();
    }
}
