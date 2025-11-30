package com.difbriy.web.mapper;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.entity.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public Contact toEntity(ContactRequest request) {
        return Contact.builder()
                .name(request.name())
                .email(request.email())
                .subject(request.subject())
                .message(request.message())
                .build();
    }

    public ContactDto toDto(Contact contact) {
        return ContactDto.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .timestamp(contact.getTimestamp())
                .build();
    }
}
