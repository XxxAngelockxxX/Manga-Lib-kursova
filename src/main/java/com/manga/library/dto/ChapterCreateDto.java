package com.manga.library.dto;

import lombok.Data;

@Data
public class ChapterCreateDto {
    private Long mangaId; // ID манги, до якої кріпимо розділ
    private Double chapterNumber;
    private String title;
}