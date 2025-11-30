package com.difbriy.web.service.news;

import com.difbriy.web.dto.news.NewsDto;
import com.difbriy.web.dto.news.NewsResponseDto;
import com.difbriy.web.entity.News;
import com.difbriy.web.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final NewsRepository newsRepository;


    public NewsResponseDto saveNews(NewsDto newsDto, String imagePath) {

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
    }

    public Page<NewsResponseDto> getNewsPage(Pageable pageable) {
        Page<News> newsPage = newsRepository.findAll(pageable);
        return newsPage.map(NewsResponseDto::new);
    }

    public NewsResponseDto getNewsById(Long id) {
        Optional<News> newsOpt = newsRepository.findById(id);
        return newsOpt.map(NewsResponseDto::new).orElse(null);
    }
}