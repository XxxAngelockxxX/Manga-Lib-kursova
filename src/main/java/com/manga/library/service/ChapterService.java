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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final MangaRepository mangaRepository;
    private final PageRepository pageRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Chapter createChapterWithZip(Long mangaId, Double chapterNumber, String title, MultipartFile zipFile) {
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Мангу не знайдено"));

        // 1. Створюємо і зберігаємо порожній розділ
        Chapter chapter = Chapter.builder()
                .chapterNumber(chapterNumber)
                .title(title)
                .manga(manga)
                .build();
        chapter = chapterRepository.save(chapter);

        // 2. Розпаковуємо файли
        List<String> imagePaths = fileStorageService.extractZipAndSaveImages(zipFile, mangaId, chapterNumber);

        // 3. Створюємо сутності сторінок
        List<Page> pages = new ArrayList<>();
        int pageNum = 1;
        for (String path : imagePaths) {
            Page page = Page.builder()
                    .pageNumber(pageNum++)
                    .filePath("/uploads/" + path) // Формуємо шлях для вебу
                    .chapter(chapter)
                    .build();
            pages.add(page);
        }

        // 4. Зберігаємо сторінки в базу
        pageRepository.saveAll(pages);
        chapter.setPages(pages);

        return chapter;
    }
    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Розділ не знайдено"));

        Long mangaId = chapter.getManga().getId();

        // 1. Видаляємо з БД
        chapterRepository.delete(chapter);

        // 2. Видаляємо папку з картинками
        String path = "uploads/mangas/" + mangaId + "/chapters/" + chapterId + "/";
        org.springframework.util.FileSystemUtils.deleteRecursively(new java.io.File(path));
    }
}