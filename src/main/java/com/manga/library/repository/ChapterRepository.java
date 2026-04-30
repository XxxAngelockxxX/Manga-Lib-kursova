package com.manga.library.repository;

import com.manga.library.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    // Корисний метод, щоб шукати всі розділи конкретної манги
    List<Chapter> findAllByMangaIdOrderByChapterNumberAsc(Long mangaId);
}