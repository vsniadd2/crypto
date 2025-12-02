package com.difbriy.web.admin.controller;

import com.difbriy.web.admin.service.NewsAdminService;
import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/news")
public class NewsAdminController {
    private final NewsAdminService newsAdminService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsResponseDto> createNewsMultipart(
            @RequestPart("news") String newsJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        NewsDto newsDto = objectMapper.readValue(newsJson, NewsDto.class);
        NewsResponseDto savedNews = processNews(newsDto, image);
        return ResponseEntity.ok(savedNews);
    }
//f
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NewsResponseDto> createNewsJson(
            @Valid @RequestBody NewsDto newsDto
    ) throws IOException {
        NewsResponseDto savedNews = processNews(newsDto, null);
        return ResponseEntity.ok(savedNews);
    }

    private NewsResponseDto processNews(NewsDto newsDto, MultipartFile image) throws IOException {
        newsDto.setPublishedAt(LocalDateTime.now());

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/news-images/";
            Files.createDirectories(Paths.get(uploadDir));
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            image.transferTo(filePath);
            imagePath = fileName;
        }
        return newsAdminService.saveNews(newsDto, imagePath);
    }

    @PutMapping("/{newsId}")
    public ResponseEntity<NewsResponseDto> editingNewsById(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsDto newsDto) {
        NewsResponseDto updatedNews = newsAdminService.editingNewsById(newsId, newsDto);
        return ResponseEntity.ok(updatedNews);
    }
}
