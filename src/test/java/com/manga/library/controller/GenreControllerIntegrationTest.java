package com.manga.library.controller;

import com.manga.library.model.Genre;
import com.manga.library.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Імпортуємо методи для зручності читання
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // Дозволяє нам відправляти фейкові HTTP запити
@ActiveProfiles("test") // Каже Spring використовувати application-test.properties
public class GenreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenreRepository genreRepository;

    // Цей метод виконується ПЕРЕД кожним тестом, щоб база завжди була чистою
    @BeforeEach
    void setUp() {
        genreRepository.deleteAll();
    }

    // ==========================================
    // ПОЗИТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    void shouldReturnAllGenresForPublicUser() throws Exception {
        // Given (Готуємо дані: зберігаємо два жанри в БД)
        genreRepository.save(Genre.builder().name("Ісекай").build());
        genreRepository.save(Genre.builder().name("Фентезі").build());

        // When & Then (Робимо GET запит і перевіряємо результат)
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk()) // Очікуємо статус 200
                .andExpect(jsonPath("$", hasSize(2))) // Очікуємо, що в масиві 2 елементи
                .andExpect(jsonPath("$[0].name").value("Ісекай"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Імітуємо, що запит робить юзер з роллю ADMIN
    void shouldCreateGenreWhenUserIsAdmin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/genres")
                        .param("name", "Романтика"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Романтика"));
    }

    // ==========================================
    // НЕГАТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "AUTHOR") // Імітуємо запит від Автора (який не є адміном)
    void shouldReturnForbiddenWhenAuthorTriesToCreateGenre() throws Exception {
        // Автор намагається створити жанр, але доступ має бути заборонено (403)
        mockMvc.perform(post("/api/genres")
                        .param("name", "Детектив"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnRedirectionWhenGuestTriesToCreateGenre() throws Exception {
        // Гість (без анотації @WithMockUser) намагається зробити POST запит
        // Оскільки у нас OAuth2, Spring Security перенаправить його на сторінку логіну (302)
        mockMvc.perform(post("/api/genres")
                        .param("name", "Меха"))
                .andExpect(status().is3xxRedirection());
    }
}