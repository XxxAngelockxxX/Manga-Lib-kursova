package com.manga.library.controller;

import com.manga.library.dto.MangaCreateDto;
import com.manga.library.dto.MangaDto;
import com.manga.library.model.Manga;
import com.manga.library.model.Tag;
import com.manga.library.repository.MangaRepository;
import com.manga.library.service.MangaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mangas")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Manga", description = "Управління каталогом манги")
public class MangaController {

    private final MangaService mangaService;
    private final MangaRepository mangaRepository;

    @Operation(summary = "Створити нову мангу",
            description = "Дозволяє зареєструвати нову мангу в системі. Доступно тільки для ADMIN та AUTHOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Мангу успішно створено"),
            @ApiResponse(responseCode = "403", description = "Недостатньо прав для створення манги")
    })
    @PostMapping
    // Встановлюємо охоронця: створювати картку манги можуть автори або адміни
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public MangaDto createManga(@RequestBody MangaCreateDto createDto) {
        Manga manga = mangaService.createManga(
                createDto.getTitle(),
                createDto.getDescription(),
                createDto.getGenreIds(),
                createDto.getTagIds(),
                createDto.getAuthorId()
        );
        return convertToDto(manga);
    }

    @Operation(summary = "Отримати всю мангу", description = "Повертає повний список манги. Доступно всім.")
    @GetMapping
    public List<MangaDto> getAllMangas() {
        return mangaService.getAllManga().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MangaDto convertToDto(Manga manga) {
        List<String> genreNames = manga.getGenres().stream()
                .map(genre -> genre.getName())
                .collect(Collectors.toList());
        List<String> tagNames = manga.getTags().stream()
                .map(com.manga.library.model.Tag::getName) // Явно вказуємо модель, щоб не плутати з Swagger Tag
                .collect(Collectors.toList());

        return MangaDto.builder()
                .id(manga.getId())
                .title(manga.getTitle())
                .description(manga.getDescription())
                .genres(genreNames)
                .tags(tagNames)
                .build();
    }
    @Operation(summary = "Видалити мангу", description = "Доступно для ROLE_ADMIN та ROLE_AUTHOR")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AUTHOR')")
    public ResponseEntity<Void> deleteManga(@PathVariable Long id) {
        mangaService.deleteManga(id);
        return ResponseEntity.noContent().build();
    }
}