package com.manga.library.controller;

import com.manga.library.model.Chapter;
import com.manga.library.service.ChapterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChapterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Підміняємо реальний сервіс на "заглушку", щоб не зберігати файли на диск під час тестів
    @MockBean
    private ChapterService chapterService;

    // ==========================================
    // ПОЗИТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "AUTHOR")
    void shouldUploadChapterWhenUserIsAuthor() throws Exception {
        // Given: Створюємо фейковий ZIP-файл
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "chapter1.zip",
                "application/zip",
                "dummy zip content".getBytes()
        );

        // Налаштовуємо поведінку Mock-сервісу
        Chapter mockChapter = new Chapter();
        mockChapter.setChapterNumber(1.0);
        mockChapter.setTitle("Початок");
        mockChapter.setPages(new ArrayList<>());

        // Вказуємо, що коли контролер викличе сервіс, треба повернути наш mockChapter
        when(chapterService.createChapterWithZip(eq(1L), eq(1.0), eq("Початок"), any()))
                .thenReturn(mockChapter);

        // When & Then: Відправляємо multipart-запит
        // ЗВЕРНИ УВАГУ: тут URL має збігатися з тим, що у твоєму ChapterController
        mockMvc.perform(multipart("/api/chapters") // або "/api/chapters/upload", залежно від твого контролера
                        .file(file)
                        .param("mangaId", "1")
                        .param("chapterNumber", "1.0")
                        .param("title", "Початок"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteChapterWhenUserIsAdmin() throws Exception {
        mockMvc.perform(delete("/api/chapters/1"))
                .andExpect(status().isNoContent()); // Очікуємо 204 No Content замість 200 OK
    }

    // ==========================================
    // НЕГАТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenSimpleUserTriesToUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.zip", "application/zip", "content".getBytes()
        );

        mockMvc.perform(multipart("/api/chapters")
                        .file(file)
                        .param("mangaId", "1")
                        .param("chapterNumber", "2.0"))
                .andExpect(status().isForbidden()); // Звичайний користувач отримає 403
    }

    @Test
    void shouldReturnRedirectWhenGuestTriesToUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.zip", "application/zip", "content".getBytes()
        );

        // Без анотації @WithMockUser це запит від неавторизованого гостя
        mockMvc.perform(multipart("/api/chapters")
                        .file(file)
                        .param("mangaId", "1")
                        .param("chapterNumber", "2.0"))
                .andExpect(status().is3xxRedirection()) // Очікуємо 302 редирект замість 403
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google")); // Перевіряємо перенаправлення на логін
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void shouldReturnBadRequestWhenFileIsNotZip() throws Exception {
        // Завантажуємо TXT замість ZIP
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "document.txt", "text/plain", "text".getBytes()
        );

        mockMvc.perform(multipart("/api/chapters")
                        .file(invalidFile)
                        .param("mangaId", "1")
                        .param("chapterNumber", "3.0"))
                .andExpect(status().isBadRequest()); // Контролер має перевіряти розширення файлу і кидати 400
    }

    @Test
    @WithMockUser(username = "hacker_author", roles = "AUTHOR")
    void shouldReturnForbiddenWhenAuthorTriesToUploadToOthersManga() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hack.zip", "application/zip", "content".getBytes());

        // Навчаємо Mock-сервіс імітувати відмову в доступі, якщо mangaId = 100
        when(chapterService.createChapterWithZip(eq(100L), any(), any(), any()))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Це не ваша манга!"));

        // Спроба завантажити розділ до манги, яка належить іншому автору
        mockMvc.perform(multipart("/api/chapters")
                        .file(file)
                        .param("mangaId", "100")
                        .param("chapterNumber", "1.0"))
                .andExpect(status().isForbidden()); // Тепер тест успішно отримає 403
    }


}