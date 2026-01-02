package com.difbriy.web.mapper;

import com.difbriy.web.dto.news.NewsRequest;
import com.difbriy.web.dto.news.NewsResponse;
import com.difbriy.web.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewsMapper {
    News toEntity(NewsRequest dto);

    NewsResponse toDto(News news);
}
