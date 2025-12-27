package com.difbriy.web.controller.favorite;

import com.difbriy.web.dto.favorite.AddFavoriteRequest;
import com.difbriy.web.dto.favorite.FavoriteDto;
import com.difbriy.web.dto.favorite.FavoriteStatusDto;
import com.difbriy.web.service.favorite.FavoriteService;
import com.difbriy.web.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<FavoriteDto>> getUserFavoritesCoins(Authentication authentication) {
        log.info("Getting list of favorite coins for user");
        Long id = getUserIdFromAuthentication(authentication).join();
        return ResponseEntity.ok(favoriteService.getUserFavorites(id));
    }

    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<FavoriteDto>> addFavorite(
            @Valid @RequestBody AddFavoriteRequest request,
            Authentication authentication
    ) {
        log.info("Adding coin to favorites. Coin ID: {}", request.coinId());
        return getUserIdFromAuthentication(authentication)
                .thenCompose(userId -> {
                    log.debug("User ID extracted from authentication: {}", userId);
                    return favoriteService.addFavoriteAsync(userId, request.coinId());
                })
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.warn("Error adding coin to favorites. Coin ID: {}, Error: {}", request.coinId(), ex.getMessage());
                    throw new IllegalArgumentException(ex.getMessage(), ex);
                });
    }

    @DeleteMapping("/{coinId}")
    public CompletableFuture<ResponseEntity<Void>> removeFavorite(
            @PathVariable String coinId,
            Authentication authentication) {
        log.info("Removing coin from favorites. Coin ID: {}", coinId);
        return getUserIdFromAuthentication(authentication)
                .thenCompose(userId -> {
                    log.debug("User ID extracted from authentication: {}", userId);
                    return favoriteService.removeFavoriteAsync(userId, coinId)
                            .thenApply(v -> {
                                log.info("Coin with ID {} successfully removed from favorites for user with ID: {}",
                                        coinId, userId);
                                return ResponseEntity.noContent().build();
                            });
                });
    }

    @GetMapping("/{coinId}")
    public ResponseEntity<FavoriteStatusDto> checkFavorite(
            @PathVariable String coinId,
            Authentication authentication) {
        log.debug("Checking favorite status for coin. Coin ID: {}", coinId);
        Long id = getUserIdFromAuthentication(authentication).join();
        boolean isFav = favoriteService.isFavorite(id, coinId);
        FavoriteStatusDto responseDto = FavoriteStatusDto.builder()
                .isFavorite(isFav)
                .coinId(coinId)
                .build();
        return ResponseEntity.ok(responseDto);
    }

    //todo Убрать ебанный CompletableFuture Отсюда
    private CompletableFuture<Long> getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        return CompletableFuture.completedFuture(userService.findByEmail(email).orElseThrow().getId());

    }
}
