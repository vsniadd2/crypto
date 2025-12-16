package com.difbriy.web.service.favorite;

import com.difbriy.web.dto.favorite.FavoriteDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FavoriteService {
    CompletableFuture<Boolean> isFavorite(Long userId, String coinId);
    CompletableFuture<Void> removeFavorite(Long userId, String coinId);
    CompletableFuture<FavoriteDto> addFavorite(Long userId, String coinId);
    CompletableFuture<List<FavoriteDto>> getUserFavorites(Long userId);
}
