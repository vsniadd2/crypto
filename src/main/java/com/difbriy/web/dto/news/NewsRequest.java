package com.difbriy.web.dto.news;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


@Builder
public record NewsRequest(
        @NotBlank(message = "Title must not be null or empty")
        String title,

        @NotBlank(message = "Description must not be null or empty")
        String description,

        @NotBlank(message = "Content must not be null or empty")
        String content,

        @NotBlank(message = "Category must not be null or empty")
        String category,

        @NotBlank(message = "Author must not be null or empty")
        String author
) {
}
