package com.manga.library.service;

import com.manga.library.model.Genre;
import com.manga.library.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre createGenre(String name) {
        // Перевіряємо, чи немає вже такого жанру
        if (genreRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Жанр з такою назвою вже існує!");
        }

        Genre genre = Genre.builder()
                .name(name)
                .build();

        return genreRepository.save(genre);
    }
    @Transactional
    public void deleteGenre(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new RuntimeException("Жанр не знайдено");
        }
        genreRepository.deleteById(id);
    }
}