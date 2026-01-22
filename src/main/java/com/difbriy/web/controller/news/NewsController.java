package com.difbriy.web.controller.news;

import com.difbriy.web.dto.news.NewsRequest;
import com.difbriy.web.dto.news.NewsResponse;
import com.difbriy.web.entity.Image;
import com.difbriy.web.repository.ImageRepository;
import com.difbriy.web.repository.NewsRepository;
import com.difbriy.web.service.news.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NewsController {
    NewsService newsService;
    ImageRepository imageRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<NewsResponse>> createNews(
            @RequestPart("news") NewsRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        return newsService.saveNews(request, image)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<Page<NewsResponse>>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return newsService.getNewsPage(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable Long id) {
        NewsResponse response = newsService.getNewsById(id);
        return ResponseEntity.ok(response);

    }

    @PutMapping(value = "/{newsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<NewsResponse>> updateNews(
            @PathVariable Long newsId,
            @RequestPart("news") @Valid NewsRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        return newsService.updateNews(newsId, request, image)
                .thenApply(ResponseEntity::ok);
    }


    @DeleteMapping("/delete/{newsId}")
    public CompletableFuture<ResponseEntity<Void>> deleteNews(@PathVariable Long newsId) {
       return newsService.deleteNewsById(newsId)
               .thenApply(ResponseEntity::ok);
    }

    //test
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Image image = imageRepository.findById(id).get();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.getContentType()))
                .body(image.getContent());
    }

}