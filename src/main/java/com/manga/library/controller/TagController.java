package com.manga.library.controller;

import com.manga.library.model.Tag;
import com.manga.library.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
// Використовуємо повне ім'я для анотації Swagger, щоб не плутати з моделлю Tag
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Керування тегами (мітки для манги)")
public class TagController {

    private final TagRepository tagRepository;

    @Operation(summary = "Створити новий тег", description = "Доступно тільки для ROLE_ADMIN")
    @PostMapping
    // Обмежуємо доступ тільки для адмінів
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public com.manga.library.model.Tag createTag(
            @Parameter(description = "Назва тегу (напр. Ісекай, Романтика)") @RequestParam String name) {

        com.manga.library.model.Tag tag = com.manga.library.model.Tag.builder()
                .name(name)
                .build();
        return tagRepository.save(tag);
    }

    @Operation(summary = "Отримати всі теги", description = "Доступно всім користувачам")
    @GetMapping
    public List<com.manga.library.model.Tag> getAllTags() {
        return tagRepository.findAll();
    }
    @Operation(summary = "Видалити тег", description = "Тільки для ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}