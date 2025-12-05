package com.difbriy.web.admin.service;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.entity.News;
import com.difbriy.web.mapper.NewsMapper;
import com.difbriy.web.repository.NewsRepository;
import com.difbriy.web.service.news.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NewsAdminService {
    private final NewsRepository newsRepository;
    private final NewsService newsService;
    private final NewsMapper mapper;

    @Transactional
    @SuppressWarnings("unused")
    public NewsResponseDto deleteNewsById(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("News not found with id: %d", newsId)));
        NewsResponseDto response = mapper.toDto(news);
        newsRepository.deleteById(newsId);
        log.info("News with id {} has been deleted successfully", newsId);
        return response;
    }

    public NewsResponseDto editingNewsById(Long newsId, NewsDto request) {
        log.info("Editing news with id: {}", newsId);

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> {
                    log.warn("News with id {} not found", newsId);
                    return new IllegalArgumentException(String.format("News not found with id: %d", newsId));
                });

        news.setTitle(request.getTitle());
        news.setDescription(request.getDescription());
        news.setContent(request.getContent());
        news.setCategory(request.getCategory());
        news.setAuthor(request.getAuthor());
        news.setPublishedAt(request.getPublishedAt());

        News savedNews = newsRepository.save(news);
        log.info("News with id {} has been updated successfully", newsId);

        return mapper.toDto(savedNews);
    }

    public CompletableFuture<NewsResponseDto> saveNews(NewsDto newsDto, String imagePath) {
        log.info("Creating new news with title: {}", newsDto.getTitle());
        return newsService.saveNews(newsDto, imagePath)
                .thenApply(savedNews -> {
                    if (savedNews != null && savedNews.getId() != null) {
                        log.info("News with id {} has been created successfully", savedNews.getId());
                    } else {
                        log.warn("News created but id is null");
                    }
                    return savedNews;
                });
    }


    @Transactional(readOnly = true)
    @SuppressWarnings("unused")
    public Long countNews() {
        long count = newsRepository.count();
        log.info("Total news in system: {}", count);
        return count;
    }
}
