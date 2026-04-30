package com.manga.library.controller;

import com.manga.library.model.Chapter;
import com.manga.library.repository.ChapterRepository;
import com.manga.library.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "Керування розділами манги")
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;

    @Operation(summary = "Завантажити ZIP-архів з розділом",
            description = "Доступно тільки для користувачів з ролями ADMIN або AUTHOR")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // ОСЬ НАШ ОХОРОНЕЦЬ:
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public ResponseEntity<String> uploadChapterArchive(
            @Parameter(description = "ID манги") @RequestParam("mangaId") Long mangaId,
            @Parameter(description = "Номер розділу (напр. 1.0)") @RequestParam("chapterNumber") Double chapterNumber,
            @Parameter(description = "Назва розділу (опціонально)") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "ZIP файл із зображеннями") @RequestParam("file") MultipartFile file) {

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            return ResponseEntity.badRequest().body("Будь ласка, завантажте валідний ZIP архів");
        }

        Chapter chapter = chapterService.createChapterWithZip(mangaId, chapterNumber, title, file);
        return ResponseEntity.ok("Розділ " + chapter.getChapterNumber() + " успішно створено. Додано сторінок: " + chapter.getPages().size());
    }
    @Operation(summary = "Видалити розділ", description = "Доступно для ROLE_ADMIN та ROLE_AUTHOR")
    @DeleteMapping("/{chapterId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        // Якщо тут світиться червоним - це нормально, ми виправимо це в Кроці 2
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }

}