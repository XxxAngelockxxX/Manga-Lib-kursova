CREATE TABLE chapters (
                          id BIGSERIAL PRIMARY KEY,
                          manga_id BIGINT NOT NULL,
                          chapter_number DOUBLE PRECISION NOT NULL,
                          title VARCHAR(255),
                          FOREIGN KEY (manga_id) REFERENCES mangas(id) ON DELETE CASCADE
);

CREATE TABLE pages (
                       id BIGSERIAL PRIMARY KEY,
                       chapter_id BIGINT NOT NULL,
                       page_number INT NOT NULL,
                       file_path VARCHAR(255) NOT NULL, -- Тепер тут шлях до файлу замість URL
                       FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);