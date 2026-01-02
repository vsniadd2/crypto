package com.difbriy.web.service.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.entity.Contact;
import com.difbriy.web.mapper.ContactMapper;
import com.difbriy.web.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final ContactMapper mapper;

    @Transactional
    @Async("taskExecutor")
    @Override
    public CompletableFuture<ContactDto> saveContact(ContactRequest request) {

        log.info("Start saving contact: {}", request);

        Contact contact = mapper.toEntity(request);
        contactRepository.save(contact);

        log.info("Contact saved successfully with id={}", contact.getId());
        ContactDto response = mapper.toDto(contact);

        return CompletableFuture.completedFuture(response);
    }


    @Transactional(readOnly = true)
    @Override
    public ContactDto getContactByEmail(String email) {
        log.info("Fetching contact by email={}", email);

        Contact contact = contactRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Contact not found with email={}", email);
                    return new IllegalArgumentException(
                            String.format("Contact not found with email: %s", email)
                    );
                });

        ContactDto dto = mapper.toDto(contact);
        log.info("Successfully fetched contact id={} for email={}", contact.getId(), email);
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public ContactDto getContactById(Long id) {
        log.info("Fetching contact by id={}", id);

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Contact not found with id={}", id);
                    return new IllegalArgumentException(
                            String.format("Contact not found with id: %d", id)
                    );
                });

        ContactDto dto = mapper.toDto(contact);
        log.info("Successfully fetched contact id={} for id={}", contact.getId(), id);
        return dto;
    }

}
