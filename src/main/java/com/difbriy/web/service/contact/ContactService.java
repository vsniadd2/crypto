package com.difbriy.web.service.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;

import java.util.concurrent.CompletableFuture;

public interface ContactService {
    CompletableFuture<ContactDto> saveContact(ContactRequest request);
    ContactDto getContactByEmail(String email);
    ContactDto getContactById(Long id);
}
