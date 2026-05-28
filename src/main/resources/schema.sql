-- schema.sql — Книжный уголок, MariaDB
-- Запуск: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS bookclub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookclub;

-- Пользователи (все роли в одной таблице)
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('READER', 'AUTHOR', 'REVIEWER', 'MODERATOR') NOT NULL DEFAULT 'READER',
    blocked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Книги
CREATE TABLE IF NOT EXISTS books (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    author_name VARCHAR(150) NOT NULL,
    genres      VARCHAR(300),
    description TEXT,
    cover_url   VARCHAR(500),
    status      ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'DRAFT',
    created_by  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_user FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Полки читателя
CREATE TABLE IF NOT EXISTS shelves (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shelves_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- M:N книги ↔ полки
CREATE TABLE IF NOT EXISTS shelf_books (
    shelf_id BIGINT NOT NULL,
    book_id  BIGINT NOT NULL,
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (shelf_id, book_id),
    CONSTRAINT fk_sb_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id) ON DELETE CASCADE,
    CONSTRAINT fk_sb_book  FOREIGN KEY (book_id)  REFERENCES books(id)  ON DELETE CASCADE
);

-- Статьи рецензентов
CREATE TABLE IF NOT EXISTS articles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    status      ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    views       INT NOT NULL DEFAULT 0,
    created_by  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_articles_user FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Отзывы
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    rating     TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text       TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books(id)  ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uq_one_review_per_user (book_id, user_id)
);

-- Жалобы
CREATE TABLE IF NOT EXISTS complaints (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type ENUM('BOOK', 'REVIEW', 'ARTICLE') NOT NULL,
    target_id   BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason      VARCHAR(500) NOT NULL,
    resolved    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

-- Тестовые данные
INSERT INTO users (username, email, password, role) VALUES
    ('reader1',    'reader@test.com',    '$2a$12$placeholderHashHere1', 'READER'),
    ('author1',    'author@test.com',    '$2a$12$placeholderHashHere2', 'AUTHOR'),
    ('reviewer1',  'reviewer@test.com',  '$2a$12$placeholderHashHere3', 'REVIEWER'),
    ('moderator1', 'moderator@test.com', '$2a$12$placeholderHashHere4', 'MODERATOR');

CREATE USER IF NOT EXISTS 'bookclub_user'@'localhost' IDENTIFIED BY 'bookclub_pass';
GRANT ALL PRIVILEGES ON bookclub.* TO 'bookclub_user'@'localhost';
FLUSH PRIVILEGES;
