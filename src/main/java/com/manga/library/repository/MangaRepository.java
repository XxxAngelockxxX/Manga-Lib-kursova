package com.manga.library.repository;

import com.manga.library.model.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    List<Manga> findByAuthorId(Long authorId);
    List<Manga> findByTitleContainingIgnoreCase(String title);
}