package com.example.bookmarkr.repository;

import com.example.bookmarkr.model.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookmarkRepository extends MongoRepository<Bookmark, String> {

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(String userId);

    Bookmark findByIdAndUserId(String id, String userId);

}
