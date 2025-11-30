package com.difbriy.web.dto.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NewsDto {
    @NotBlank(message = "Title must not be null or empty")
    private String title;
    
    @NotBlank(message = "Description must not be null or empty")
    private String description;
    
    @NotBlank(message = "Content must not be null or empty")
    private String content;
    
    @NotBlank(message = "Category must not be null or empty")
    private String category;
    
    @NotBlank(message = "Author must not be null or empty")
    private String author;
    
    @NotNull(message = "PublishedAt must not be null")
    private LocalDateTime publishedAt;
}