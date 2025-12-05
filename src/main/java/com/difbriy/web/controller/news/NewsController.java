package com.difbriy.web.controller.news;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.service.news.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    private final NewsService newsService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<NewsResponseDto>> createNewsMultipart(
            @RequestPart("news") String newsJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        NewsDto newsDto = objectMapper.readValue(newsJson, NewsDto.class);
        return newsService.saveNewsWithImage(newsDto, image)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<NewsResponseDto>> createNewsJson(
            @RequestBody NewsDto newsDto
    ) {
        return newsService.saveNewsWithoutImage(newsDto)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<Page<NewsResponseDto>>> getNewsList(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsService.getNewsPage(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<NewsResponseDto>> getNewsById(@PathVariable Long id) {
        return newsService.getNewsById(id)
                .thenApply(news -> {
                    if (news == null) {
                        return ResponseEntity.notFound().<NewsResponseDto>build();
                    }
                    return ResponseEntity.ok(news);
                });
    }

    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getNewsImage(@PathVariable String filename) throws IOException {
        Resource resource = newsService.getImageResource(filename);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        
        String contentType = newsService.getImageContentType(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}