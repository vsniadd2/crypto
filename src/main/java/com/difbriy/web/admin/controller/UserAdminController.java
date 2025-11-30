package com.difbriy.web.admin.controller;

import com.difbriy.web.admin.service.UserAdminService;
import com.difbriy.web.dto.admin.UserAdminDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<Page<UserAdminDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<UserAdminDto> users = userAdminService.getAllUsersWithPagination(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    public ResponseEntity<?> countUsers() {
        long count = userAdminService.countUsers();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{userId}/block")
    public ResponseEntity<UserAdminDto> blockUser(@PathVariable Long userId) {
        UserAdminDto blockedUser = userAdminService.blockUser(userId);
        return ResponseEntity.ok(blockedUser);
    }

    @PutMapping("/{userId}/unblock")
    public ResponseEntity<UserAdminDto> unblockUser(@PathVariable Long userId) {
        UserAdminDto unblockedUser = userAdminService.unblockUser(userId);
        return ResponseEntity.ok(unblockedUser);
    }
}
