package com.difbriy.web.controller.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.service.contact.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDto> saveContact(@Valid @RequestBody ContactRequest request) {
        ContactDto response = contactService.saveContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
