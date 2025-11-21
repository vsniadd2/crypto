package com.difbriy.web.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The field must not be empty")
    private String title;
    @NotBlank(message = "The field must not be empty")
    private String description;
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "The field must not be empty")
    private String content;
    @NotBlank(message = "The field must not be empty")
    private String category;
    @NotBlank(message = "The field must not be empty")
    private String author;
    private LocalDateTime publishedAt;
    private String imagePath;


}