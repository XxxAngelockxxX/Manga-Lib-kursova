package com.manga.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class MangaCreateDto {
    private String title;
    private String description;
    private List<Long> genreIds;
    private Long authorId; // <--- Додали поле для автора
    private List<Long> tagIds; // Додали поле для тегів
}