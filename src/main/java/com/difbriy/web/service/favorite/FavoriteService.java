package com.difbriy.web.service.favorite;

import com.difbriy.web.dto.favorite.FavoriteDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FavoriteService {
    boolean isFavorite(Long userId, String coinId);

    CompletableFuture<Void> removeFavoriteAsync(Long userId, String coinId);

    CompletableFuture<FavoriteDto> addFavorite(Long userId, String coinId);

    List<FavoriteDto>getUserFavorites(Long userId);
}
