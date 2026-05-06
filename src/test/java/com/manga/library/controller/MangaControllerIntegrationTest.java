package com.manga.library.controller;

import com.manga.library.model.Manga;
import com.manga.library.model.Role;
import com.manga.library.model.User;
import com.manga.library.repository.MangaRepository;
import com.manga.library.repository.RoleRepository;
import com.manga.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Відкочує зміни в БД після кожного тесту, щоб вони не впливали один на одного
public class MangaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testAuthor;

    @BeforeEach
    void setUp() {
        // Готуємо автора в базі даних перед кожним тестом
        Role authorRole = roleRepository.save(Role.builder().name("ROLE_AUTHOR").build());
        testAuthor = userRepository.save(User.builder()
                .username("test_author")
                .email("author@test.com")
                .role(authorRole)
                .build());
    }

    // ==========================================
    // ПОЗИТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(username = "test_author", roles = "AUTHOR")
    void shouldCreateMangaWhenUserIsAuthor() throws Exception {
        // Уявимо, що фронтенд відправляє такий JSON для створення манги
        String mangaJson = """
                {
                    "title": "Нова крута манга",
                    "description": "Опис цієї манги",
                    "authorId": %d
                }
                """.formatted(testAuthor.getId());

        mockMvc.perform(post("/api/mangas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mangaJson))
                .andExpect(status().isOk()) // Або isCreated(), залежно від твого контролера
                .andExpect(jsonPath("$.title").value("Нова крута манга"));

        // Перевіряємо, чи дійсно манга з'явилася в базі
        assertEquals(1, mangaRepository.findAll().size());
    }

    @Test
    void shouldFindMangaByTitleWhenUserIsGuest() throws Exception {
        // Згідно з нашим SecurityConfig, GET /api/** дозволено всім
        // Зберігаємо дві манги в базу
        mangaRepository.save(Manga.builder().title("Naruto").author(testAuthor).build());
        mangaRepository.save(Manga.builder().title("Boruto").author(testAuthor).build());

        // Робимо пошук за запитом "nar" (без анотації @WithMockUser, тобто як гість)
        mockMvc.perform(get("/api/mangas/search")
                        .param("title", "nar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Очікуємо 1 результат
                .andExpect(jsonPath("$[0].title").value("Naruto"));
    }

    // ==========================================
    // НЕГАТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenSimpleUserTriesToCreateManga() throws Exception {
        String mangaJson = """
                {
                    "title": "Манга звичайного юзера",
                    "description": "Спроба хаку"
                }
                """;

        mockMvc.perform(post("/api/mangas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mangaJson))
                .andExpect(status().isForbidden()); // Очікуємо 403, бо тільки автори можуть створювати
    }

    @Test
    void shouldReturnRedirectWhenGuestTriesToCreateManga() throws Exception {
        // Запит без авторизації взагалі
        String mangaJson = """
                {
                    "title": "Манга гостя"
                }
                """;

        mockMvc.perform(post("/api/mangas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mangaJson))
                .andExpect(status().is3xxRedirection()) // Очікуємо 302 редирект
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google")); // Перевіряємо, що перенаправляє на Google
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void shouldReturnBadRequestWhenMangaTitleIsEmpty() throws Exception {
        // Перевірка валідації (потрібна анотація @Valid у MangaController)
        String invalidMangaJson = """
                {
                    "title": "",
                    "description": "Манга без назви"
                }
                """;

        mockMvc.perform(post("/api/mangas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMangaJson))
                .andExpect(status().isBadRequest()); // Очікуємо 400 Bad Request
    }

}