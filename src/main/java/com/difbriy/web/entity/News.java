package com.difbriy.web.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotBlank(message = "The field must not be empty")
    String title;
    @NotBlank(message = "The field must not be empty")
    String description;
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "The field must not be empty")
    String content;
    @NotBlank(message = "The field must not be empty")
    String category;
    @NotBlank(message = "The field must not be empty")
    String author;
    LocalDateTime publishedAt;
    String imagePath;

}