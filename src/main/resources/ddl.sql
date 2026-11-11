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

-- USER_GROUP
CREATE TABLE dundun.user_group
(
    group_id                 VARCHAR(50) PRIMARY KEY,
    name                     VARCHAR(100) NOT NULL,
    invite_code              VARCHAR(10)  NOT NULL UNIQUE,
    invite_code_refresh_date DATE         NOT NULL,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- USER_GROUP_MEMBER
CREATE TABLE dundun.user_group_member
(
    group_id   VARCHAR(50) NOT NULL,
    user_id    VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_group_member PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_user_group_member_user_id ON dundun.user_group_member (user_id);

-- IMAGE_FILE
CREATE TABLE dundun.image_file
(
    file_id            VARCHAR(50)  NOT NULL,
    file_seq_no        INTEGER      NOT NULL,
    user_id            VARCHAR(50)  NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    s3_key             VARCHAR(500) NOT NULL UNIQUE,
    size               BIGINT       NOT NULL,
    content_type       VARCHAR(100) NOT NULL,
    identifier         VARCHAR(100) NOT NULL, -- USER_IMAGE, RECORD_IMAGE
    is_first_image     BOOLEAN      NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_image_file PRIMARY KEY (file_id, file_seq_no),
    CONSTRAINT fk_image_file_user FOREIGN KEY (user_id) REFERENCES dundun.user (user_id)
);