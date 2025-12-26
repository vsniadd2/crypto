package com.difbriy.web.dto.favorite;

import lombok.Builder;

@Builder
public record FavoriteStatusDto(
        boolean isFavorite,
        String coinId
) {
}