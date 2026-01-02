package com.difbriy.web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "images")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "content_type")
    String contentType;

    @Column(name = "file_name")
    String fileName;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "content")
    byte[] content;
}
