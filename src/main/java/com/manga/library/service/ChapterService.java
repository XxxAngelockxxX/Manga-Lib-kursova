package com.manga.library.service;

import com.manga.library.model.Chapter;
import com.manga.library.model.Manga;
import com.manga.library.model.Page;
import com.manga.library.repository.ChapterRepository;
import com.manga.library.repository.MangaRepository;
import com.manga.library.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final MangaRepository mangaRepository;
    private final PageRepository pageRepository;
    private final FileStorageService fileStorageService;

    // Рекомендую винести цей корінь у константу або config
    private static final String UPLOAD_ROOT = "uploads";

    @Transactional
    public Chapter createChapterWithZip(Long mangaId, Double chapterNumber, String title, MultipartFile zipFile) {
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Мангу не знайдено"));

        // 1. Створюємо і зберігаємо порожній розділ
        Chapter chapter = Chapter.builder()
                .chapterNumber(chapterNumber)
                .title(title)
                .manga(manga)
                .pages(new ArrayList<>())
                .build();

        // Зберігаємо відразу, щоб отримати ID для формування шляху видалення пізніше
        final Chapter savedChapter = chapterRepository.save(chapter);

        // 2. Розпаковуємо файли (передаємо ID розділу для унікальності папки)
        List<String> relativePaths = fileStorageService.extractZipAndSaveImages(zipFile, mangaId, chapterNumber);

        // 3. Створюємо сутності сторінок
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < relativePaths.size(); i++) {
            Page page = Page.builder()
                    .pageNumber(i + 1)
                    .filePath("/" + UPLOAD_ROOT + "/" + relativePaths.get(i)) // Шлях для фронтенда
                    .chapter(savedChapter)
                    .build();
            pages.add(page);
        }

        // 4. Зберігаємо сторінки в базу
        pageRepository.saveAll(pages);
        savedChapter.setPages(pages);

        return savedChapter;
    }

    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Розділ не знайдено"));

        // Зберігаємо шлях перед видаленням з БД
        Long mangaId = chapter.getManga().getId();
        Double chapterNum = chapter.getChapterNumber();

        // 1. Видаляємо з БД
        chapterRepository.delete(chapter);

        // 2. Видаляємо папку з картинками
        // Шлях має бути синхронізований з тим, як зберігає FileStorageService
        File chapterFolder = Paths.get(UPLOAD_ROOT, "manga", mangaId.toString(), "chapters", chapterNum.toString()).toFile();

        if (chapterFolder.exists()) {
            org.springframework.util.FileSystemUtils.deleteRecursively(chapterFolder);
        }
    }
}