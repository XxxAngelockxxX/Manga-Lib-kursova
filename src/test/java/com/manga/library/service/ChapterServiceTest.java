package com.manga.library.service;

import com.manga.library.repository.ChapterRepository;
import com.manga.library.repository.MangaRepository;
import com.manga.library.repository.PageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// Кажемо JUnit, що ми будемо використовувати Mockito для цього тесту
@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    // @Mock створює "фейкові" репозиторії. Вони нічого не пишуть в базу.
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private MangaRepository mangaRepository;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private FileStorageService fileStorageService;

    // @InjectMocks створює реальний ChapterService і підставляє в нього наші "фейкові" репозиторії
    @InjectMocks
    private ChapterService chapterService;

    @Test
    void shouldThrowExceptionWhenMangaNotFound() {
        // 1. GIVEN (Дано) - підготовка даних для тесту
        Long notExistingMangaId = 999L;
        Double chapterNumber = 1.0;
        String title = "Тестовий розділ";
        // Створюємо фейковий файл для тесту
        MockMultipartFile fakeZip = new MockMultipartFile("file", "test.zip", "application/zip", "dummy data".getBytes());

        // Навчаємо наш "манекен": якщо хтось викличе mangaRepository.findById(999), поверни "нічого" (Optional.empty)
        when(mangaRepository.findById(notExistingMangaId)).thenReturn(Optional.empty());

        // 2. WHEN & THEN (Коли та Тоді) - виконуємо дію і перевіряємо результат
        // Ми очікуємо, що при виклику методу вилетить RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chapterService.createChapterWithZip(notExistingMangaId, chapterNumber, title, fakeZip);
        });

        // Перевіряємо, чи правильний текст помилки ми отримали
        assertEquals("Мангу не знайдено", exception.getMessage());
    }
}