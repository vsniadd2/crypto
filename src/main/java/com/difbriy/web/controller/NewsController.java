package com.difbriy.web.controller;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.service.news.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/news")
@Slf4j
public class NewsController {
    private final NewsService newsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NewsController(NewsService newsService, ObjectMapper objectMapper) {
        this.newsService = newsService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsResponseDto> createNewsMultipart(
            @RequestPart("news") String newsJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        NewsDto newsDto = objectMapper.readValue(newsJson, NewsDto.class);
        NewsResponseDto savedNews = processNews(newsDto, image);
        return ResponseEntity.ok(savedNews);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsResponseDto> createNewsJson(
            @RequestBody NewsDto newsDto
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
        return newsService.saveNews(newsDto, imagePath);
    }

    @GetMapping
    public ResponseEntity<Page<NewsResponseDto>> getNewsList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsResponseDto> newsPage = newsService.getNewsPage(pageable);
        return ResponseEntity.ok(newsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDto> getNewsById(@PathVariable Long id) {
        NewsResponseDto news = newsService.getNewsById(id);
        if (news == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(news);
    }

    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getNewsImage(@PathVariable String filename) throws IOException {
        Path imagePath = Paths.get("uploads/news-images/", filename);
        Resource resource = new UrlResource(imagePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(imagePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}