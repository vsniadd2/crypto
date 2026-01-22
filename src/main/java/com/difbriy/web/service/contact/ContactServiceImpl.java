package com.difbriy.web.service.contact;

import com.difbriy.web.dto.contact.ContactDto;
import com.difbriy.web.dto.contact.ContactRequest;
import com.difbriy.web.entity.Contact;
import com.difbriy.web.mapper.ContactMapper;
import com.difbriy.web.repository.ContactRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactServiceImpl implements ContactService {
    ContactRepository contactRepository;
    ContactMapper mapper;
    TransactionTemplate transactionTemplate;

    @Async("taskExecutor")
    @Override
    public CompletableFuture<ContactDto> saveContact(ContactRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            return transactionTemplate.execute(status -> {
                try {
                    log.info("Start saving contact: {}", request);

                    Contact contact = mapper.toEntity(request);
                    Contact savedContact = contactRepository.save(contact);

                    log.info("Contact saved successfully with id={}", savedContact.getId());
                    return mapper.toDto(savedContact);
                } catch (Exception e) {
                    log.error("Error while saving contact: {}", request, e);
                    status.setRollbackOnly();
                    throw new RuntimeException("Failed to save contact", e);
                }
            });
        });
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
