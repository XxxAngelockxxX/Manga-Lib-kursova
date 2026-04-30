package com.manga.library.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MangaDto {
    private Long id;
    private String title;
    private String description;
    private List<String> genres; // Віддаємо просто назви жанрів для зручності
    private List<String> tags; // Додали поле для назв тегів
}