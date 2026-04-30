package com.manga.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mangas")
@Getter // Замінили @Data на @Getter та @Setter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "manga_genres",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default // Щоб Builder не перезаписав цей Set на null
    private java.util.Set<Genre> genres = new java.util.HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "manga_tags",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default // Щоб Builder не перезаписав цей Set на null
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Те саме для chapters
    private List<Chapter> chapters = new ArrayList<>();

    // --- Допоміжні методи для контролерів ---

    public void addGenre(Genre genre) {
        this.genres.add(genre);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
}