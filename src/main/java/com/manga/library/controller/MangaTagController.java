package com.manga.library.controller;

import com.manga.library.model.Manga;
import com.manga.library.repository.MangaRepository;
import com.manga.library.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mangas/{mangaId}/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Manga Tags", description = "Керування тегами конкретної манги")
public class MangaTagController {

    private final MangaRepository mangaRepository;
    private final TagRepository tagRepository;

    @Operation(summary = "Додати тег до манги", description = "Доступно для ROLE_ADMIN та ROLE_AUTHOR")
    @PostMapping("/{tagId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public ResponseEntity<String> addTagToManga(@PathVariable Long mangaId, @PathVariable Long tagId) {

        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Мангу з ID " + mangaId + " не знайдено"));

        com.manga.library.model.Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Тег з ID " + tagId + " не знайдено"));

        manga.addTag(tag);
        mangaRepository.save(manga);

        return ResponseEntity.ok("Тег '" + tag.getName() + "' успішно додано до манги '" + manga.getTitle() + "'");
    }

}