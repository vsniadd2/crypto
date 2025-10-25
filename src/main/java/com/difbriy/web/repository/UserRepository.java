package com.difbriy.web.repository;


import com.difbriy.web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String name);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByResetToken(String token);

    //OAuth2




}
