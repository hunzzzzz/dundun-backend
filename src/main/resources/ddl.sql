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
    group_id     VARCHAR(50) PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    member_count INT          NOT NULL CHECK (member_count BETWEEN 0 AND 10),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
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