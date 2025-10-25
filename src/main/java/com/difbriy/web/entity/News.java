package com.difbriy.web.entity;

import jakarta.persistence.*;
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

    private String title;
    private String description;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String category;
    private String author;
    private LocalDateTime publishedAt;
    private String imagePath;


}