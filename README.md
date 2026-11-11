<div align="center">

# 🍽️ 둔둔 (dundun)

**사진 한 장에서 되살아나는, 우리 둘의 맛집 기억**

연인과 함께 다닌 맛집의 기록을 남기는 **커플 맛집 다이어리** 백엔드 서버

![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white)

</div>

---

## 📌 개요

### 기획 배경

맛집 투어를 다니며 음식 사진을 많이 찍지만, 시간이 지나 사진만 보면 **"여기가 어디였지?"** 하고 기억나지 않는 경우가 많습니다.
이 불편함을 직접 겪은 경험에서 출발해, 사진에 담긴 정보로 방문 장소를 되짚어 주고 그날의 추억까지 함께 남길 수 있는 서비스를 만들고 있습니다.

### 설계 원칙

- 🎯 도메인 범위를 **"개인 기록 + 소규모 그룹(최대 10명) 공유"** 로 한정합니다.
- 💸 배포·운영 비용을 최소화합니다.
- 📜 카카오 정책에 따라 **카카오 플레이스 검색 결과는 DB에 저장하지 않습니다.**
- 📦 중요한 파라미터는 쿼리스트링이 아닌 **요청 바디**로 전달합니다.
- 🔒 사진은 **비공개 AWS S3 버킷**에 저장하고, 접근할 때마다 만료되는 **임시 URL**로만 주고받습니다.

---

## ✨ 주요 기능

| 기능              | 설명                                               | 상태    |
|-------------------|----------------------------------------------------|---------|
| 🔐 인증/인가      | 소셜 로그인, JWT 토큰 재발급, 로그아웃             | ✅ 완료 |
| ⚙️ 공통 인프라    | DB 세팅, 로깅, 전역 예외 처리, 공통 코드 관리      | ✅ 완료 |
| 👥 그룹           | 그룹 생성, 초대 코드 참여, 멤버 목록, 탈퇴         | ✅ 완료 |
| 🖼️ 파일 업로드    | S3 Presigned URL 직접 업로드, 고아 파일 정리       | ✅ 완료 |
| 👤 마이페이지     | 프로필 조회, 프로필 사진·닉네임 변경, 회원탈퇴     | ✅ 완료 |
| 🔎 EXIF 추출      | 프론트에서 촬영 시각·위치 추출 후 서버로 전달      | 🚧 예정 |
| 📍 방문 장소 제안 | 카카오맵 API로 "이 장소를 방문하셨나요?" 후보 제안 | 🚧 예정 |
| 📝 추억 기록      | 방문 장소별 메모와 그날의 분위기 기록              | 🚧 예정 |

### 🔐 인증/인가

카카오·구글 **소셜 로그인**과 **JWT** 기반의 무상태(STATELESS) 인증입니다.
access token은 응답 body로, refresh token은 **HttpOnly 쿠키**로 전달합니다.

```mermaid
sequenceDiagram
    participant FE as 프론트
    participant BE as 백엔드
    participant OP as 카카오/구글
    FE ->> BE: GET /api/login/{provider}/url
    BE -->> FE: 로그인 URL, state (state는 HttpOnly 쿠키로도 저장)
    FE ->> OP: 로그인 페이지로 이동
    OP -->> FE: redirect (code, state)
    FE ->> BE: POST /api/login/{provider} (code, state)
    BE ->> BE: 쿠키의 state와 비교 (CSRF 방지)
    BE ->> OP: 인가 코드로 토큰 발급 → 사용자 정보 조회
    BE ->> BE: 신규면 가입 / 기존이면 탈퇴 여부 확인
    BE -->> FE: access token + user, refresh token은 HttpOnly 쿠키
```

**소셜 로그인:** 카카오와 구글 모두 인가 코드 방식으로 구현했습니다. 백엔드가 로그인 URL과 `state`를 발급하면서 `state`를 HttpOnly 쿠키로도 저장해 두고, 로그인 요청 시 쿠키 값과 비교해
CSRF를 방지합니다. 토큰 교환에 쓰는 redirect URI는 클라이언트 입력이 아닌 서버 설정값만 사용합니다. 신규 사용자는 소셜 계정 정보로 자동 가입되며 닉네임은 중복을 검사한 랜덤 값으로 생성하고, 탈퇴한
사용자는 탈퇴 후 60일 동안 같은 계정으로 로그인할 수 없고, 정리 스케줄러가 계정을 완전히 삭제한 뒤에는 새 사용자로 다시 가입할 수 있습니다.

**토큰 발급과 저장:** access token은 15분, refresh token은 2일 동안 유효하며, `type` 클레임으로 용도를 구분하고 서명과 issuer를 검증합니다. access token은 응답
body로 내려주고, refresh token은 HttpOnly 쿠키로 전달합니다. 쿠키는 운영 환경에서 `Secure` + `SameSite=None`, 로컬에서는 `Lax`로 설정합니다. DB에는 refresh
token 원본이 아닌 **SHA-256 해시만** 저장해, DB가 유출되어도 토큰을 바로 쓸 수 없게 했습니다.

**토큰 재발급:** 재발급할 때마다 access token과 refresh token을 모두 새로 발급하는 **Refresh Token Rotation** 방식입니다. 쿠키로 받은 refresh token의 해시가
DB 값과 다르면 탈취된 토큰으로 간주해 저장된 토큰을 폐기하고 쿠키를 만료시킵니다. 이 폐기는 별도 트랜잭션으로 분리해, 예외가 발생해도 롤백되지 않고 DB에 반영됩니다.

**로그아웃:** access token이 만료된 상태에서도 로그아웃할 수 있도록 인증 제외 경로로 두고, refresh token 쿠키로 사용자를 식별합니다. 사용자 식별이나 DB 처리 성공 여부와 관계없이 쿠키는
항상 만료시킵니다.

**운영 안전장치:** 요청 body는 로그에 남기지 않습니다. 카카오·구글 등 외부 API 호출에는 connect/read 타임아웃을 적용했고, 호출이 실패하면 도메인 예외로 변환해 일관된 에러 응답을 내려줍니다.

### 👥 그룹

사용자가 그룹에 참여해 방문 기록과 추억을 함께 쌓는 공간입니다. 커플뿐 아니라 친구·동료도 쓸 수 있도록 **한 그룹에 최대 10명**, **한 사용자는 최대 5개 그룹**까지 참여할 수 있고, 그룹 안의 모든
멤버는 동일한 권한을 가집니다. (그룹장 없음)

**초대 코드:** 그룹마다 영문 대소문자와 숫자 10자로 된 코드가 하나씩 있고, 멤버라면 누구나 조회할 수 있습니다. 코드는 `SecureRandom`으로 생성하며 3일마다 바뀝니다. 별도 스케줄러 없이 **코드
조회 시점에 만료 여부를 확인해 갱신**하고, 참여 시에는 만료된 코드를 거부합니다.

**동시성 처리:** 참여, 탈퇴, 초대 코드 갱신은 그룹 행에 **비관적 락(`PESSIMISTIC_WRITE`)** 을 걸고 처리해, 동시에 참여해도 인원 상한(10명)을 넘지 않고 마지막 멤버의 탈퇴 판단도
어긋나지 않게 했습니다. 멤버 수는 별도 카운터 컬럼을 두지 않고 `user_group_member`의 행 수로 계산해 데이터 불일치 가능성을 없앴습니다.

**탈퇴:** 멤버 누구나 그룹에서 나갈 수 있고, 마지막 멤버가 나가면 그룹도 함께 삭제됩니다. 탈퇴한 사용자는 멤버 목록에서 제외됩니다.

---

### 🖼️ 파일 업로드

사진은 서버를 거치지 않고 프론트가 **S3에 직접 업로드**합니다. 서버는 업로드 권한이 담긴 임시 URL(Presigned URL)만 발급해서 트래픽과 비용을 줄였습니다.

```mermaid
sequenceDiagram
    participant FE as 프론트
    participant BE as 백엔드
    participant S3 as AWS S3
    FE ->> FE: EXIF 추출 → webp 압축
    FE ->> BE: POST /api/files/presigned-url (파일 정보)
    BE ->> BE: 개수·크기·형식 검증, image_file 저장
    BE -->> FE: fileId, 업로드용 Presigned URL (10분)
    FE ->> S3: PUT (압축된 파일 직접 업로드)
    FE ->> BE: 메인 API 호출 (fileId 전달)
    BE ->> S3: 업로드 완료 여부 확인 (HeadObject)
    BE -->> FE: 처리 결과
```

**업로드 보안과 비용 통제:** 서버는 URL을 발급하기 전에 파일 개수(용도별 상한), 크기, 허용된 이미지 형식을 검증합니다. 발급하는 URL에는 `Content-Type`과 `Content-Length`가
서명에 포함되어, 신고한 값과 다른 파일은 S3가 거부합니다. S3 키는 사용자가 정한 파일명이 아니라 `fileId`, 순번, 검증된 형식에서 정한 확장자로만 만들어 경로 조작과 특수문자 문제를 막았습니다.

**비공개 저장과 조회:** 버킷은 퍼블릭 액세스를 모두 차단하고, 백엔드 전용 IAM 사용자에게 필요한 경로의 최소 권한만 부여했습니다. DB에는 URL이 아닌 `s3_key`만 저장하고, 조회할 때마다 10분짜리
Presigned GET URL을 새로 만들어 내려줍니다. 메인 로직(예: 프로필 사진 변경)은 DB 행이 아니라 **S3에 객체가 실제로 존재하는지**까지 확인한 뒤에 반영합니다.

**고아 파일 정리:** Presigned URL을 발급한 뒤 업로드하지 않았거나 어디에서도 사용하지 않는 파일은 스케줄러(30분 간격)가 정리합니다. 생성 후 1시간이 지났고 어디에서도 참조되지 않는 이미지만
대상으로 하고, **S3 삭제가 성공한 경우에만 DB 행을 삭제**해 객체가 추적 불가능하게 남지 않도록 했습니다. S3 삭제는 이미 없는 객체에도 성공으로 응답하므로 재시도해도 안전하며, 파일마다 트랜잭션을 분리해
한 건의 실패가 나머지에 영향을 주지 않습니다.

### 👤 마이페이지와 회원탈퇴

**마이페이지:** 프로필 조회, 프로필 사진 변경, 닉네임 변경(중복 불가, 2~10자)을 제공합니다. 프로필 사진 URL은 조회 시점에 임시 URL로 만들어 내려주며, 로그인·재발급 응답에도 같은 방식으로
포함됩니다.

**회원탈퇴:** 하나의 트랜잭션에서 참여 중인 모든 그룹에서 탈퇴하고(마지막 멤버였던 그룹은 함께 삭제), 계정을 탈퇴 상태로 바꾸고 refresh token을 폐기합니다. 이후 **60일이 지나면 스케줄러(매일
새벽 4시)** 가 사용자가 올린 S3 객체와 이미지 행을 지운 뒤 계정을 완전히 삭제합니다. 하나라도 삭제에 실패하면 계정을 지우지 않고 다음 실행에서 다시 시도합니다.

---

## 🛠️ 기술 스택

| 구분          | 기술                                                     |
|---------------|----------------------------------------------------------|
| Language      | Kotlin 2.3                                               |
| Framework     | Spring Boot 4.1 (WebMVC, Data JPA, Security, Validation) |
| Database      | MySQL                                                    |
| Auth          | jjwt                                                     |
| 외부 API 연동 | Spring HTTP Interface (RestClient)                       |
| Logging       | kotlin-logging                                           |
| Storage       | AWS S3 (AWS SDK v2, Presigned URL)                       |
| Scheduling    | Spring `@Scheduled`                                      |

---

## 🗂️ 테이블 목록

### `user`

| 컬럼                                     | 설명                               |
|------------------------------------------|------------------------------------|
| `user_id` (PK)                           | 사용자 ID                          |
| `status`                                 | `ACTIVE` / `DELETED`               |
| `role`                                   | `USER` / `ADMIN`                   |
| `oauth_id`, `oauth_provider`             | 소셜 계정 식별자 (복합 UNIQUE)     |
| `email`                                  | 이메일                             |
| `nickname`                               | 닉네임 (UNIQUE, 가입 시 랜덤 생성) |
| `file_id`                                | 프로필 이미지                      |
| `refresh_token`                          | refresh token의 SHA-256 해시       |
| `created_at`, `updated_at`, `deleted_at` | 생성 / 수정 / 탈퇴 시각            |

### `user_group`

| 컬럼                       | 설명                                      |
|----------------------------|-------------------------------------------|
| `group_id` (PK)            | 그룹 ID                                   |
| `name`                     | 그룹 이름                                 |
| `invite_code`              | 초대 코드 (UNIQUE, 10자)                  |
| `invite_code_refresh_date` | 초대 코드 갱신 기준일 (생성/갱신일 + 3일) |
| `created_at`, `updated_at` | 생성 / 수정 시각                          |

### `user_group_member`

| 컬럼                       | 설명                                                |
|----------------------------|-----------------------------------------------------|
| `group_id`, `user_id` (PK) | 그룹 ID, 사용자 ID (복합 PK, `user_id` 별도 인덱스) |
| `created_at`, `updated_at` | 그룹 참여 / 수정 시각                               |

### `image_file`

| 컬럼                          | 설명                                                        |
|-------------------------------|-------------------------------------------------------------|
| `file_id`, `file_seq_no` (PK) | 파일 묶음 ID, 묶음 안의 순번 (복합 PK)                      |
| `user_id` (FK)                | 업로드한 사용자                                             |
| `original_file_name`          | 사용자가 올린 원본 파일명 (표시용, S3 키에는 사용하지 않음) |
| `s3_key`                      | S3 오브젝트 키                                              |
| `size`, `content_type`        | 파일 크기, 형식                                             |
| `identifier`                  | 용도 (`USER_IMAGE` 최대 1장 / `RECORD_IMAGE` 최대 5장)      |
| `is_first_image`              | 대표 이미지 여부                                            |
| `created_at`, `updated_at`    | 생성 / 수정 시각                                            |

---

### ERD

```mermaid
erDiagram
    user ||--o{ user_group_member: "참여"
    user_group ||--o{ user_group_member: "구성"
    user ||--o{ image_file: "업로드 (FK)"
    user }o..o| image_file: "프로필 사진 (file_id, 논리적 참조)"

    user {
        varchar user_id PK
        varchar status
        varchar oauth_provider
        varchar nickname
        varchar file_id
    }
    user_group {
        varchar group_id PK
        varchar name
        varchar invite_code
    }
    user_group_member {
        varchar group_id PK
        varchar user_id PK
    }
    image_file {
        varchar file_id PK
        int file_seq_no PK
        varchar user_id FK
        varchar s3_key
        varchar identifier
    }
```

---

## 🔌 API 목록

| Method | Path                                | 인증                  | 설명                                  |
|--------|-------------------------------------|-----------------------|---------------------------------------|
| GET    | `/api/login/kakao/url`              | ❌                    | 카카오 로그인 URL 및 state 발급       |
| POST   | `/api/login/kakao`                  | ❌                    | 카카오 로그인                         |
| GET    | `/api/login/google/url`             | ❌                    | 구글 로그인 URL 및 state 발급         |
| POST   | `/api/login/google`                 | ❌                    | 구글 로그인                           |
| POST   | `/api/token/reissue`                | 🍪 refresh token 쿠키 | access/refresh token 재발급           |
| POST   | `/api/logout`                       | 🍪 refresh token 쿠키 | 로그아웃                              |
| POST   | `/api/groups`                       | 🔑 access token       | 그룹 생성                             |
| GET    | `/api/groups`                       | 🔑 access token       | 내가 참여한 그룹 목록 조회            |
| PATCH  | `/api/groups/{groupId}`             | 🔑 access token       | 그룹 이름 수정                        |
| DELETE | `/api/groups/{groupId}`             | 🔑 access token       | 그룹 탈퇴 (마지막 멤버면 그룹도 삭제) |
| GET    | `/api/groups/{groupId}/members`     | 🔑 access token       | 그룹 멤버 목록 조회                   |
| GET    | `/api/groups/{groupId}/invite-code` | 🔑 access token       | 그룹 초대 코드 조회 (만료 시 갱신)    |
| POST   | `/api/groups/join`                  | 🔑 access token       | 초대 코드로 그룹 참여                 |
| POST   | `/api/files/presigned-url`          | 🔑 access token       | 업로드용 Presigned URL 발급           |
| GET    | `/api/me`                           | 🔑 access token       | 마이페이지 조회                       |
| PATCH  | `/api/me/image`                     | 🔑 access token       | 프로필 사진 변경                      |
| PATCH  | `/api/me/nickname`                  | 🔑 access token       | 닉네임 변경                           |
| DELETE | `/api/me`                           | 🔑 access token       | 회원탈퇴 (60일 후 완전 삭제)          |

- **에러 응답**: `{ errCode, errName, errMsg, time }`
- 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더 사용