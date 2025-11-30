package com.difbriy.web.controller.favorite;

import com.difbriy.web.dto.favorite.AddFavoriteRequest;
import com.difbriy.web.dto.favorite.FavoriteDto;
import com.difbriy.web.dto.favorite.FavoriteStatusDto;
import com.difbriy.web.entity.User;
import com.difbriy.web.service.favorite.FavoriteService;
import com.difbriy.web.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Long userId = getUserIdFromAuthentication(authentication);
        log.debug("User ID extracted from authentication: {}", userId);

        List<FavoriteDto> response = favoriteService.getUserFavorites(userId);
        log.info("Successfully retrieved {} favorite coins for user with ID: {}", response.size(), userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<FavoriteDto> addFavorite(
            @Valid @RequestBody AddFavoriteRequest request,
            Authentication authentication
    ) {
        log.info("Adding coin to favorites. Coin ID: {}", request.coinId());
        Long userId = getUserIdFromAuthentication(authentication);
        log.debug("User ID extracted from authentication: {}", userId);

        try {
            FavoriteDto response = favoriteService.addFavorite(userId, request.coinId());
            log.info("Coin with ID {} successfully added to favorites for user with ID: {}", 
                    request.coinId(), userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error adding coin to favorites. User ID: {}, Coin ID: {}, Error: {}", 
                    userId, request.coinId(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{coinId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String coinId,
            Authentication authentication) {
        log.info("Removing coin from favorites. Coin ID: {}", coinId);
        Long userId = getUserIdFromAuthentication(authentication);
        log.debug("User ID extracted from authentication: {}", userId);

        try {
            favoriteService.removeFavorite(userId, coinId);
            log.info("Coin with ID {} successfully removed from favorites for user with ID: {}", 
                    coinId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error removing coin from favorites. User ID: {}, Coin ID: {}, Error: {}", 
                    userId, coinId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{coinId}")
    public ResponseEntity<FavoriteStatusDto> checkFavorite(
            @PathVariable String coinId,
            Authentication authentication) {
        log.debug("Checking favorite status for coin. Coin ID: {}", coinId);
        Long userId = getUserIdFromAuthentication(authentication);
        log.debug("User ID extracted from authentication: {}", userId);

        boolean isFavorite = favoriteService.isFavorite(userId, coinId);
        log.debug("Favorite status for coin with ID {} and user with ID {}: {}", 
                coinId, userId, isFavorite);
        FavoriteStatusDto status = new FavoriteStatusDto(isFavorite, coinId);
        return ResponseEntity.ok(status);
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User not found with email: %s", email))
                );
        return user.getId();
    }
}
