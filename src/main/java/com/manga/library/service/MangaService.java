package com.manga.library.service;

import com.manga.library.model.Genre;
import com.manga.library.model.Manga;
import com.manga.library.model.User; // Або Author
import com.manga.library.repository.GenreRepository;
import com.manga.library.repository.MangaRepository;
import com.manga.library.repository.TagRepository;
import com.manga.library.repository.UserRepository; // Додали репозиторій авторів
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MangaService {

    private final MangaRepository mangaRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    // <--- Інжектимо репозиторій

    public List<Manga> getAllManga() {
        return mangaRepository.findAll();
    }

    public Manga getMangaById(Long id) {
        return mangaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Мангу з ID " + id + " не знайдено"));
    }

    @Transactional
    public Manga createManga(String title, String description, List<Long> genreIds, Long authorId) {
        // Шукаємо автора в базі
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автора з ID " + authorId + " не знайдено"));

        Manga manga = Manga.builder()
                .title(title)
                .description(description)
                .author(author) // <--- Додаємо автора до манги!
                .genres(new HashSet<>())
                .build();

        if (genreIds != null && !genreIds.isEmpty()) {
            List<Genre> foundGenres = genreRepository.findAllById(genreIds);
            manga.getGenres().addAll(foundGenres);
        }

        return mangaRepository.save(manga);
    }
    @Transactional
    public Manga createManga(String title, String description, List<Long> genreIds, List<Long> tagIds, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Автора не знайдено"));

        Manga manga = Manga.builder()
                .title(title)
                .description(description)
                .author(author)
                .genres(new HashSet<>())
                .tags(new HashSet<>()) // Ініціалізуємо список тегів
                .build();

        // Додаємо жанри
        if (genreIds != null && !genreIds.isEmpty()) {
            manga.getGenres().addAll(genreRepository.findAllById(genreIds));
        }

        // Додаємо теги
        if (tagIds != null && !tagIds.isEmpty()) {
            manga.getTags().addAll(tagRepository.findAllById(tagIds));
        }

        return mangaRepository.save(manga);
    }
    @Transactional
    public void deleteManga(Long id) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Мангу не знайдено"));

        // 1. Видаляємо з БД (це також каскадно видалить chapters і pages)
        mangaRepository.delete(manga);

        // 2. Видаляємо головну папку цієї манги з диска (uploads/mangas/{id})
        String path = "uploads/mangas/" + id + "/";
        org.springframework.util.FileSystemUtils.deleteRecursively(new java.io.File(path));
    }
    public List<Manga> searchMangaByTitle(String title) {
        return mangaRepository.findByTitleContainingIgnoreCase(title);
    }
}