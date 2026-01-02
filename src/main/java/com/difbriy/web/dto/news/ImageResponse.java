package com.difbriy.web.dto.news;

import com.difbriy.web.entity.Image;
import lombok.Builder;

@Builder
public record ImageResponse(
        Long id,
        String contentType,

        String fileName,

        byte[] content
) {
    public static ImageResponse toImageDto(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getContentType(),
                image.getFileName(),
                image.getContent()
        );
    }
}
