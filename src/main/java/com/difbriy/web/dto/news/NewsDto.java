package com.difbriy.web.dto.news;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewsDto {
    private String title;
    private String description;
    private String content;
    private String category;
    private String author;
    private LocalDateTime publishedAt;
}