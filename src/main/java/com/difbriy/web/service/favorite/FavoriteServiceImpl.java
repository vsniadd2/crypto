package com.difbriy.web.service.favorite;

import com.difbriy.web.dto.favorite.FavoriteDto;
import com.difbriy.web.entity.Favorite;
import com.difbriy.web.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    @Override
    public List<FavoriteDto> getUserFavorites(Long userId) {
        log.debug("Getting list of favorite coins from DB for user with ID: {}", userId);
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        log.debug("Found {} favorite coins for user with ID: {}", favorites.size(), userId);

        List<FavoriteDto> result = favorites.stream()
                .map(favorite -> new FavoriteDto(favorite.getCoinId(), favorite.getId()))
                .collect(Collectors.toList());

        log.debug("Converted to DTO: {} records", result.size());
        return result;
    }

    @Transactional
    @Override
    public FavoriteDto addFavorite(Long userId, String coinId) {
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
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<FavoriteDto> addFavoriteAsync(Long userId, String coinId) {
        return CompletableFuture.supplyAsync(() -> addFavorite(userId, coinId));
    }


    @Transactional
    void removeFavoriteInternal(Long userId, String coinId) {
        log.debug("Checking if coin exists in favorites before deletion. User ID: {}, Coin ID: {}", userId, coinId);
        if (!favoriteRepository.existsByUserIdAndCoinId(userId, coinId)) {
            log.warn("Attempt to remove coin that is not in favorites. User ID: {}, Coin ID: {}", userId, coinId);
            throw new IllegalArgumentException("Coin not found in favorites");
        }

        log.debug("Removing coin from favorites. User ID: {}, Coin ID: {}", userId, coinId);
        favoriteRepository.deleteByUserIdAndCoinId(userId, coinId);
        log.info("Coin successfully removed from favorites. User ID: {}, Coin ID: {}", userId, coinId);
    }

    @Override
    public void removeFavorite(Long userId, String coinId) {
       removeFavoriteInternal(userId, coinId);
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<Void> removeFavoriteAsync(Long userId, String coinId) {
        removeFavoriteInternal(userId, coinId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, String coinId) {

        log.debug("Checking favorite status in DB. User ID: {}, Coin ID: {}", userId, coinId);
        boolean exists = favoriteRepository.existsByUserIdAndCoinId(userId, coinId);
        log.debug("Favorite status check result. User ID: {}, Coin ID: {}, Is Favorite: {}",
                userId, coinId, exists);
        return exists;

    }
}
