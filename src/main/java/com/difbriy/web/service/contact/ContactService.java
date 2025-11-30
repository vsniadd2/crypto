package com.difbriy.web.service.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.entity.Contact;
import com.difbriy.web.mapper.ContactMapper;
import com.difbriy.web.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    private final ContactRepository contactRepository;
    private final ContactMapper mapper;

    public ContactDto saveContact(ContactRequest request) {
        Contact contact = mapper.toEntity(request);
        contactRepository.save(contact);

        ContactDto response = mapper.toDto(contact);
        return response;
    }
}
