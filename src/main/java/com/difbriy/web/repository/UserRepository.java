package com.difbriy.web.repository;

import com.difbriy.web.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByResetToken(String token);

    Optional<User> findByGithubId(String githubId);


}
