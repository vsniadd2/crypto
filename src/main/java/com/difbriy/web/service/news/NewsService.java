package com.difbriy.web.service.news;

import com.difbriy.web.dto.news.NewsRequest;
import com.difbriy.web.dto.news.NewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface NewsService {
    CompletableFuture<NewsResponse> saveNews(NewsRequest newsDto, MultipartFile image) throws IOException;

    CompletableFuture<Page<NewsResponse>> getNewsPage(Pageable pageable);

    NewsResponse getNewsById(Long id);

    CompletableFuture<Void> deleteNewsById(Long newsId);

    CompletableFuture<NewsResponse> updateNews(Long newsId, NewsRequest request, MultipartFile image);
}
