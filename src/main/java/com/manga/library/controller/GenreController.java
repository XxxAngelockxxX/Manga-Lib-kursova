package com.manga.library.controller;

import com.manga.library.dto.GenreDto;
import com.manga.library.model.Genre;
import com.manga.library.repository.GenreRepository;
import com.manga.library.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Управління жанрами манги")
public class GenreController {

    private final GenreService genreService;
    private final GenreRepository genreRepository;

    @Operation(summary = "Отримати список усіх жанрів", description = "Доступно всім користувачам без авторизації")
    @GetMapping
    public List<GenreDto> getAllGenres() {
        return genreService.getAllGenres().stream()
                .map(genre -> GenreDto.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Operation(summary = "Створити новий жанр", description = "Доступно тільки для ROLE_ADMIN")
    @PostMapping
    // Ставимо охоронця.
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public GenreDto createGenre(
            @Parameter(description = "Назва жанру") @RequestParam String name) {

        Genre genre = genreService.createGenre(name);

        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
    @Operation(summary = "Видалити жанр", description = "Тільки для ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }

}