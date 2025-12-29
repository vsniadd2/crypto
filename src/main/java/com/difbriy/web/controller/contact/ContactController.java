package com.difbriy.web.controller.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.service.contact.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactServiceImpl;

    @PostMapping
    public CompletableFuture<ResponseEntity<ContactDto>> saveContact(@Valid @RequestBody ContactRequest request) {
        return contactServiceImpl.saveContactAsync(request)
                .thenApply(ResponseEntity::ok);
    }
}
