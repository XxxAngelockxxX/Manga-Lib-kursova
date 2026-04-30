package com.manga.library.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChapterDto {
    private Long id;
    private Double chapterNumber;
    private String title;
    private Long mangaId;
}