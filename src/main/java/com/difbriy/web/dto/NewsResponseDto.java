package com.difbriy.web.dto;

import com.difbriy.web.entity.News;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewsResponseDto {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String author;
    private LocalDateTime publishedAt;
    private String imageUrl;

    public NewsResponseDto() {}

    public NewsResponseDto(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.description = news.getDescription();
        this.content = news.getContent();
        this.category = news.getCategory();
        this.author = news.getAuthor();
        this.publishedAt = news.getPublishedAt();
        
        // Формируем правильный URL для изображения
        if (news.getImagePath() != null && !news.getImagePath().isEmpty()) {
            this.imageUrl = "/api/news/image/" + news.getImagePath();
        } else {
            this.imageUrl = null;
        }
    }



} 