DROP TABLE IF EXISTS movie_actor;
DROP TABLE IF EXISTS reviews; -- movies, users를 참조하므로 먼저 삭제
DROP TABLE IF EXISTS movies;  -- directors를 참조하므로 그 다음 삭제
DROP TABLE IF EXISTS actors;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS users;

CREATE TABLE actors (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY, 
    name VARCHAR(255) NOT NULL          
);

CREATE TABLE directors (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY, 
    name VARCHAR(255) NOT NULL          
);

CREATE TABLE movies (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    director_id        BIGINT NOT NULL,           -- Director 객체 참조 (FK)
    genre              VARCHAR(50),               -- Genre Enum 저장
    description        TEXT,
    release_date       DATE,
    rating             DOUBLE DEFAULT 0.0,
    CONSTRAINT fk_movie_director FOREIGN KEY (director_id) REFERENCES directors(id)
);


CREATE TABLE movie_actor (
    movie_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    PRIMARY KEY (movie_id, actor_id),
    CONSTRAINT fk_ma_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_ma_actor FOREIGN KEY (actor_id) REFERENCES actors(id) ON DELETE CASCADE
);  


CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- User 객체의 Long id
    user_name  VARCHAR(100) NOT NULL UNIQUE,      -- String userName (중복 방지 UNIQUE 추가)
    password   VARCHAR(255) NOT NULL,             -- String password (암호화 대비 길게 설정)
    role       VARCHAR(20) NOT NULL,              -- Role Enum (USER, ADMIN 등 문자열 저장)
    deleted    BOOLEAN DEFAULT FALSE,     -- 0: 활동중, 1: 탈퇴 (논리 삭제 플래그)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 가입 시간 (추천)
);

CREATE TABLE reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY, -- Review 객체의 Long id
    content     TEXT,                              -- String content (긴 글일 수 있으므로 TEXT)
    rating      INT NOT NULL,                      -- Integer rating (1~5점 등)
    movie_id    BIGINT NOT NULL,                   -- Movie 객체의 식별자 (FK)
    user_id     BIGINT NOT NULL,                   -- User 객체의 식별자 (FK)
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- (권장) 작성 시간
    
    -- 외래키 설정
    CONSTRAINT fk_review_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user  FOREIGN KEY (user_id)  REFERENCES users(id)
);