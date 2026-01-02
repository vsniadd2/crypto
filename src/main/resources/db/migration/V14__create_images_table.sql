CREATE TABLE images (
                        id           bigserial PRIMARY KEY,
                        content_type varchar(255),
                        file_name    varchar(255),
                        content      bytea
);
