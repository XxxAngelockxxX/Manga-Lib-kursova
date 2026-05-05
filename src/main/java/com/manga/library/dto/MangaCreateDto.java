package com.manga.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class MangaCreateDto {
    @NotBlank(message = "Назва манги не може бути порожньою")
    private String title;
    private String description;
    private List<Long> genreIds;
    @NotNull(message = "ID автора є обов'язковим")
    private Long authorId;
    private List<Long> tagIds; // Додали поле для тегів
}