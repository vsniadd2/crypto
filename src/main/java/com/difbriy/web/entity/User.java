package com.difbriy.web.entity;

import com.difbriy.web.roles.Role;
import com.difbriy.web.token.Token;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "username", unique = true, nullable = false)
    String username;

    @Column(name = "email", unique = true, nullable = false)
    String email;

    @Column(name = "password", nullable = false)
    String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    List<Role> roles;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    @Column(name = "reset_token")
    String resetToken;

    @Column(name = "reset_token_expiry")
    LocalDateTime resetTokenExpiry;

    @JsonFormat(pattern = "yyyy-MM-dd:mm:ss")
    @Column(name = "dateTimeOfCreated")
    @CreationTimestamp
    LocalDateTime dateTimeOfCreated;

    @Column(name = "isActive")
    boolean isActive;

    //todo что-то решить с этой дичкой
    @PrePersist
    private void init() {
        if (roles == null || roles.isEmpty()) {
            roles = new ArrayList<>();
            roles.add(Role.ROLE_USER);
        }
        isActive = true;
    }
}

