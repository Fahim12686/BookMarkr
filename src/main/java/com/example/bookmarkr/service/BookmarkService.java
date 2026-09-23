package com.example.bookmarkr.service;

import com.example.bookmarkr.model.Bookmark;
import com.example.bookmarkr.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public List<Bookmark> getAllForUser(String userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Bookmark getOwnedBookmarkOrThrow(String id, String userId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(id, userId);
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark not found or access denied");
        }
        return bookmark;
    }

    public Bookmark create(String title, String url, String description, String tagsRaw, String userId) {
        Bookmark bookmark = new Bookmark();
        bookmark.setTitle(title);
        bookmark.setUrl(url);
        bookmark.setDescription(description);
        bookmark.setTags(parseTags(tagsRaw));
        bookmark.setCreatedAt(LocalDateTime.now());
        bookmark.setUserId(userId);
        return bookmarkRepository.save(bookmark);
    }

    public Bookmark update(String id, String title, String url, String description, String tagsRaw, String userId) {
        Bookmark bookmark = getOwnedBookmarkOrThrow(id, userId);
        bookmark.setTitle(title);
        bookmark.setUrl(url);
        bookmark.setDescription(description);
        bookmark.setTags(parseTags(tagsRaw));
        return bookmarkRepository.save(bookmark);
    }

    public void delete(String id, String userId) {
        Bookmark bookmark = getOwnedBookmarkOrThrow(id, userId);
        bookmarkRepository.delete(bookmark);
    }

    public List<Bookmark> search(String userId, String query, String tag) {
        List<Bookmark> bookmarks = getAllForUser(userId);

        if (tag != null && !tag.isBlank() && !tag.equalsIgnoreCase("all")) {
            bookmarks = bookmarks.stream()
                    .filter(b -> b.getTags() != null && b.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
                    .toList();
        }

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            bookmarks = bookmarks.stream()
                    .filter(b ->
                            (b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) ||
                            (b.getDescription() != null && b.getDescription().toLowerCase().contains(q)) ||
                            (b.getUrl() != null && b.getUrl().toLowerCase().contains(q)))
                    .toList();
        }

        return bookmarks;
    }

    public Set<String> getDistinctTags(String userId) {
        Set<String> tags = new LinkedHashSet<>();
        for (Bookmark b : getAllForUser(userId)) {
            if (b.getTags() != null) {
                tags.addAll(b.getTags());
            }
        }
        return tags;
    }

    private List<String> parseTags(String tagsRaw) {
        if (tagsRaw == null || tagsRaw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }

}
