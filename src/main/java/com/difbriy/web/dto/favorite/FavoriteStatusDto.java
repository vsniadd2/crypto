package com.difbriy.web.dto.favorite;

public record FavoriteStatusDto(
        boolean isFavorite,
        String coinId
) {
}