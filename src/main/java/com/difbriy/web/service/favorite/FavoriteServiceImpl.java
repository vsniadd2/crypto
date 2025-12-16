package com.difbriy.web.service.favorite;

import com.difbriy.web.dto.favorite.FavoriteDto;
import com.difbriy.web.entity.Favorite;
import com.difbriy.web.repository.FavoriteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final Executor executor;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, @Qualifier("taskExecutor") Executor executor) {
        this.favoriteRepository = favoriteRepository;
        this.executor = executor;
    }

    @Transactional(readOnly = true)
    @Override
    public CompletableFuture<List<FavoriteDto>> getUserFavorites(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Getting list of favorite coins from DB for user with ID: {}", userId);
            List<Favorite> favorites = favoriteRepository.findByUserId(userId);
            log.debug("Found {} favorite coins for user with ID: {}", favorites.size(), userId);

            List<FavoriteDto> result = favorites.stream()
                    .map(favorite -> new FavoriteDto(favorite.getCoinId(), favorite.getId()))
                    .collect(Collectors.toList());

            log.debug("Converted to DTO: {} records", result.size());
            return result;
        }, executor);
    }

    @Override
    public CompletableFuture<FavoriteDto> addFavorite(Long userId, String coinId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Checking if coin exists in favorites. User ID: {}, Coin ID: {}", userId, coinId);
            if (favoriteRepository.existsByUserIdAndCoinId(userId, coinId)) {
                log.warn("Attempt to add coin that is already in favorites. User ID: {}, Coin ID: {}", userId, coinId);
                throw new IllegalArgumentException("The coin has already been added to your favorites.");
            }

            log.debug("Creating new favorite record. User ID: {}, Coin ID: {}", userId, coinId);
            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .coinId(coinId)
                    .build();

            favoriteRepository.save(favorite);
            log.info("Coin successfully saved to favorites. User ID: {}, Coin ID: {}, Favorite ID: {}",
                    userId, coinId, favorite.getId());

            FavoriteDto result = new FavoriteDto(coinId, favorite.getId());
            log.debug("Created DTO for response: coinId={}, id={}", result.coinId(), result.id());
            return result;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> removeFavorite(Long userId, String coinId) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Checking if coin exists in favorites before deletion. User ID: {}, Coin ID: {}", userId, coinId);
            if (!favoriteRepository.existsByUserIdAndCoinId(userId, coinId)) {
                log.warn("Attempt to remove coin that is not in favorites. User ID: {}, Coin ID: {}", userId, coinId);
                throw new IllegalArgumentException("Coin not found in favorites");
            }

            log.debug("Removing coin from favorites. User ID: {}, Coin ID: {}", userId, coinId);
            favoriteRepository.deleteByUserIdAndCoinId(userId, coinId);
            log.info("Coin successfully removed from favorites. User ID: {}, Coin ID: {}", userId, coinId);
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> isFavorite(Long userId, String coinId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Checking favorite status in DB. User ID: {}, Coin ID: {}", userId, coinId);
            boolean exists = favoriteRepository.existsByUserIdAndCoinId(userId, coinId);
            log.debug("Favorite status check result. User ID: {}, Coin ID: {}, Is Favorite: {}",
                    userId, coinId, exists);
            return exists;
        }, executor);
    }

}
