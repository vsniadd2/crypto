package com.difbriy.web.mapper;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.entity.News;
import org.springframework.stereotype.Component;

@Component
public class NewsMapper {

    public News toEntity(NewsDto dto) {
        return News.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(dto.getContent())
                .category(dto.getCategory())
                .author(dto.getAuthor())
                .publishedAt(dto.getPublishedAt())
                .build();
    }

    public NewsResponseDto toDto(News news) {
        return NewsResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .content(news.getContent())
                .category(news.getCategory())
                .author(news.getAuthor())
                .publishedAt(news.getPublishedAt())
                .build();
    }
}
