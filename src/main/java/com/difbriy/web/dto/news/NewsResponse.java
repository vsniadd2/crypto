package com.difbriy.web.dto.news;

import com.difbriy.web.entity.News;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NewsResponse(
        Long id,
        String title,
        String description,
        String content,
        String category,
        String author,
        LocalDateTime publishedAt,
        ImageResponse image
) {
    public static NewsResponse toDto(News news){
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .content(news.getContent())
                .category(news.getCategory())
                .author(news.getAuthor())
                .publishedAt(news.getPublishedAt())
                .image(ImageResponse.toImageDto(news.getImage()))
                .build();
    }
}
