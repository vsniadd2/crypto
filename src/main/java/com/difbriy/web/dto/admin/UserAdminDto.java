package com.difbriy.web.dto.admin;

import com.difbriy.web.roles.Role;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserAdminDto(
        Long id,
        String username,
        String email,
        List<Role> roles,
        LocalDateTime timestamp,
        boolean isActive
) {
}
