package com.difbriy.web.service.news;

import com.difbriy.web.dto.news.ImageResponse;
import com.difbriy.web.dto.news.NewsRequest;
import com.difbriy.web.dto.news.NewsResponse;
import com.difbriy.web.entity.Image;
import com.difbriy.web.entity.News;
import com.difbriy.web.exception.custom.ImageProcessingException;
import com.difbriy.web.exception.custom.NewsNotFoundException;
import com.difbriy.web.repository.ImageRepository;
import com.difbriy.web.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {

    private final ImageRepository imageRepository;
    private final NewsRepository newsRepository;
    private final Executor executor;

    //todo а также 47 строчку переписать на mapstruct
    @Transactional
    @Async("taskExecutor")
    @Override
    public CompletableFuture<NewsResponse> saveNews(NewsRequest newsRequest, MultipartFile imageRequest) {
        log.info("Trying to save new news...");
        try {
            News news = News.builder()
                    .title(newsRequest.title())
                    .description(newsRequest.description())
                    .content(newsRequest.content())
                    .category(newsRequest.category())
                    .author(newsRequest.author())
                    .publishedAt(LocalDateTime.now())
                    .image(null)
                    .build();

            ImageResponse imageResponse = null;
            if (imageRequest != null && !imageRequest.isEmpty()) {
                String uniqueFileName = String.format("%s-%s",
                        UUID.randomUUID().toString(),
                        imageRequest.getOriginalFilename());

                Image image = Image.builder()
                        .contentType(imageRequest.getContentType())
                        .fileName(uniqueFileName)
                        .content(imageRequest.getBytes())
                        .build();

                news.setImage(image);

                imageResponse = ImageResponse.builder()
                        .contentType(image.getContentType())
                        .fileName(image.getFileName())
                        .content(image.getContent())
                        .build();
            }

            News savedNews = newsRepository.save(news);

            NewsResponse response = NewsResponse.builder()
                    .id(savedNews.getId())
                    .title(savedNews.getTitle())
                    .description(savedNews.getDescription())
                    .content(savedNews.getContent())
                    .category(savedNews.getCategory())
                    .author(savedNews.getAuthor())
                    .publishedAt(savedNews.getPublishedAt())
                    .image(imageResponse)
                    .build();
            log.info("News saved successfully!");
            return CompletableFuture.completedFuture(response);
        } catch (IOException e) {
            log.error("Failed to process image for news", e);
            throw new ImageProcessingException("Image processing failed");
        }
    }

    @Override
    public CompletableFuture<Page<NewsResponse>> getNewsPage(Pageable pageable) {
        Page<News> news = newsRepository.findAll(pageable);

        Page<NewsResponse> response = news.map(this::convertToResponse);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public NewsResponse getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException(String.format("News with id %d not found", id)));

        ImageResponse imageResponse = null;
        if (news.getImage() != null) {
            imageResponse = ImageResponse.builder()
                    .id(news.getImage().getId())
                    .contentType(news.getImage().getContentType())
                    .fileName(news.getImage().getFileName())
                    .content(news.getImage().getContent())
                    .build();
        }

        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .content(news.getContent())
                .category(news.getCategory())
                .author(news.getAuthor())
                .publishedAt(news.getPublishedAt())
                .image(imageResponse)
                .build();
    }

    @Transactional
    @Async("taskExecutor")
    @Override
    public CompletableFuture<Void> deleteNewsById(Long newsId) {
        log.info("Deleting news with id: {}", newsId);

        newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News not found with id: " + newsId));

        newsRepository.deleteById(newsId);
        log.info("News with id {} deleted successfully", newsId);

        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    @Async("taskExecutor")
    @Override
    public CompletableFuture<NewsResponse> updateNews(Long newsId, NewsRequest request, MultipartFile imageRequest) {
        log.info("Updating news {}", newsId);

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsNotFoundException("News not found: " + newsId));

        news.setTitle(request.title());
        news.setDescription(request.description());
        news.setContent(request.content());
        news.setCategory(request.category());
        news.setAuthor(request.author());

        if (imageRequest != null && !imageRequest.isEmpty()) {
            try {
                Image image = news.getImage();
                if (image == null) {
                    image = Image.builder().build();
                    news.setImage(image);
                }
                image.setFileName("uuid-" + imageRequest.getOriginalFilename());
                image.setContentType(imageRequest.getContentType());
                image.setContent(imageRequest.getBytes());
            } catch (IOException e) {
                log.error("Image update failed for news {}", newsId, e);
                throw new ImageProcessingException("Failed to process image");
            }
        }

        News savedNews = newsRepository.save(news);
        log.info("News {} updated successfully", newsId);
        return CompletableFuture.completedFuture(NewsResponse.toDto(savedNews));
    }
    
    private NewsResponse convertToResponse(News news) {
        ImageResponse imageResponse = null;
        if (news.getImage() != null) {
            imageResponse = ImageResponse.builder()
                    .id(news.getImage().getId())
                    .contentType(news.getImage().getContentType())
                    .fileName(news.getImage().getFileName())
                    .content(news.getImage().getContent())
                    .build();
        }

        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .content(news.getContent())
                .category(news.getCategory())
                .author(news.getAuthor())
                .publishedAt(news.getPublishedAt())
                .image(imageResponse)
                .build();
    }
}