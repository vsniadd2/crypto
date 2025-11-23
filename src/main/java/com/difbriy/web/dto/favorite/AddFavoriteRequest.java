package com.difbriy.web.dto.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddFavoriteRequest(
        @JsonProperty("coinId")
        @NotNull(message = "coinId is required")
        @NotBlank(message = "coinId cannot be blank")
        String coinId
) {
}
