-- Таблиця ролей
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

-- Вставка базових ролей
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_AUTHOR'), ('ROLE_ADMIN');

-- Таблиця користувачів
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role_id BIGINT NOT NULL REFERENCES roles(id),
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця манги
CREATE TABLE mangas (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        author_id BIGINT NOT NULL REFERENCES users(id),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- Довідник жанрів


-- Довідник тегів
CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(100) UNIQUE NOT NULL
);

-- Проміжна таблиця: Манга - Жанри

-- Проміжна таблиця: Манга - Теги
CREATE TABLE manga_tags (
                            manga_id BIGINT NOT NULL,
                            tag_id BIGINT NOT NULL,
                            PRIMARY KEY (manga_id, tag_id),
                            FOREIGN KEY (manga_id) REFERENCES mangas(id),
                            FOREIGN KEY (tag_id) REFERENCES tags(id)
);

-- Проміжна таблиця: Улюблене
CREATE TABLE favorites (
                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           manga_id BIGINT NOT NULL REFERENCES mangas(id) ON DELETE CASCADE,
                           added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, manga_id)
);
-- Таблиця для жанрів
CREATE TABLE genres (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблиця для зв'язку "Багато-до-багатьох" (Манга <-> Жанри)
CREATE TABLE manga_genres (
                              manga_id BIGINT NOT NULL,
                              genre_id BIGINT NOT NULL,
                              PRIMARY KEY (manga_id, genre_id),
                              FOREIGN KEY (manga_id) REFERENCES mangas(id),
                              FOREIGN KEY (genre_id) REFERENCES genres(id)
);

