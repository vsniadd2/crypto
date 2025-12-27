package com.difbriy.web.service.news;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface NewsService {
    CompletableFuture<NewsResponseDto> saveNews(NewsDto newsDto, String imagePath);

    CompletableFuture<NewsResponseDto> saveNewsWithImage(NewsDto newsDto, MultipartFile image);

    CompletableFuture<NewsResponseDto> saveNewsWithoutImage(NewsDto newsDto);

    Resource getImageResource(String filename) throws IOException;

    String getImageContentType(String filename) throws IOException;

    CompletableFuture<Page<NewsResponseDto>> getNewsPage(Pageable pageable);

    CompletableFuture<NewsResponseDto> getNewsById(Long id);
}
