-- USER
CREATE TABLE dundun.user
(
    user_id        VARCHAR(50) PRIMARY KEY,
    status         VARCHAR(20)  NOT NULL, -- ACTIVE, DELETED
    role           VARCHAR(20)  NOT NULL, -- USER, ADMIN
    oauth_id       VARCHAR(255) NOT NULL,
    oauth_provider VARCHAR(50)  NOT NULL, -- KAKAO, GOOGLE
    email          VARCHAR(100),
    nickname       VARCHAR(20)  NOT NULL UNIQUE,
    file_id        VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at     TIMESTAMP,
    refresh_token  TEXT,

    CONSTRAINT uk_user_oauth UNIQUE (oauth_id, oauth_provider)
);