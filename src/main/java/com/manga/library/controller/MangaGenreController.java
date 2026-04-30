package com.manga.library.controller;

import com.manga.library.model.Genre;
import com.manga.library.model.Manga;
import com.manga.library.repository.GenreRepository;
import com.manga.library.repository.MangaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mangas/{mangaId}/genres")
@RequiredArgsConstructor
@Tag(name = "Manga Genres", description = "Керування жанрами конкретної манги")
public class MangaGenreController {

    private final MangaRepository mangaRepository;
    private final GenreRepository genreRepository;

    @Operation(summary = "Додати жанр до манги", description = "Доступно для ROLE_ADMIN та ROLE_AUTHOR")
    @PostMapping("/{genreId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public ResponseEntity<String> addGenreToManga(@PathVariable Long mangaId, @PathVariable Long genreId) {

        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Мангу з ID " + mangaId + " не знайдено"));

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Жанр з ID " + genreId + " не знайдено"));

        manga.addGenre(genre);
        mangaRepository.save(manga);

        return ResponseEntity.ok("Жанр '" + genre.getName() + "' успішно додано до манги '" + manga.getTitle() + "'");
    }
}