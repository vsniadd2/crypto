package com.difbriy.web.service.news;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.entity.News;
import com.difbriy.web.repository.NewsRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@Transactional
public class NewsService {
    private static final String UPLOAD_DIR = "uploads/news-images/";
    
    private final NewsRepository newsRepository;
    private final Executor executor;

    public NewsService(NewsRepository newsRepository, @Qualifier("taskExecutor") Executor executor) {
        this.newsRepository = newsRepository;
        this.executor = executor;
    }

    public CompletableFuture<NewsResponseDto> saveNews(NewsDto newsDto, String imagePath) {
        return CompletableFuture.supplyAsync(() -> {
            var news = News.builder()
                    .title(newsDto.getTitle())
                    .description(newsDto.getDescription())
                    .content(newsDto.getContent())
                    .category(newsDto.getCategory())
                    .author(newsDto.getAuthor())
                    .publishedAt(newsDto.getPublishedAt())
                    .imagePath(imagePath)
                    .build();

            News savedNews = newsRepository.save(news);
            return new NewsResponseDto(savedNews);
        }, executor);
    }

    public CompletableFuture<NewsResponseDto> saveNewsWithImage(NewsDto newsDto, MultipartFile image) {
        if (newsDto.getPublishedAt() == null) {
            newsDto.setPublishedAt(LocalDateTime.now());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return saveImage(image);
            } catch (IOException e) {
                log.error("Error saving image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
            }
        }, executor)
        .thenCompose(imagePath -> saveNews(newsDto, imagePath));
    }

    public CompletableFuture<NewsResponseDto> saveNewsWithoutImage(NewsDto newsDto) {
        if (newsDto.getPublishedAt() == null) {
            newsDto.setPublishedAt(LocalDateTime.now());
        }
        
        return saveNews(newsDto, null);
    }

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        image.transferTo(filePath);
        log.info("Image saved: {}", filePath);

        return fileName;
    }

    public Resource getImageResource(String filename) throws IOException {
        Path imagePath = Paths.get(UPLOAD_DIR, filename);
        Resource resource = new UrlResource(imagePath.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("Image not found or not readable: {}", filename);
            return null;
        }
        
        return resource;
    }

    public String getImageContentType(String filename) throws IOException {
        Path imagePath = Paths.get(UPLOAD_DIR, filename);
        String contentType = Files.probeContentType(imagePath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    @Transactional(readOnly = true)
    public CompletableFuture<Page<NewsResponseDto>> getNewsPage(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            Page<News> newsPage = newsRepository.findAll(pageable);
            return newsPage.map(NewsResponseDto::new);
        }, executor);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<NewsResponseDto> getNewsById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<News> newsOpt = newsRepository.findById(id);
            return newsOpt.map(NewsResponseDto::new).orElse(null);
        }, executor);
    }
}