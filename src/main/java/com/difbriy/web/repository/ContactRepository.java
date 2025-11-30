package com.difbriy.web.repository;

import com.difbriy.web.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Long> {
    Optional<Contact> findByEmail(String email);
}
