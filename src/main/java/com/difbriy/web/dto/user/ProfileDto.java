package com.difbriy.web.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProfileDto(
        Long id,
        String username,
        String email,
        LocalDateTime dateTimeOfCreated
) {

}
