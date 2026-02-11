package com.difbriy.web.dto.event;

import lombok.Builder;

@Builder
public record UserRegisteredEvent(
        String email
) {
}
