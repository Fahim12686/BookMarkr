package com.example.bookmarkr.controller;

import com.example.bookmarkr.model.Bookmark;
import com.example.bookmarkr.model.User;
import com.example.bookmarkr.service.BookmarkService;
import com.example.bookmarkr.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String tag,
                             Authentication authentication,
                             Model model) {
        User user = currentUser(authentication);

        List<Bookmark> bookmarks;
        boolean isFiltering = (query != null && !query.isBlank()) || (tag != null && !tag.isBlank() && !tag.equalsIgnoreCase("all"));

        if (isFiltering) {
            bookmarks = bookmarkService.search(user.getId(), query, tag);
        } else {
            bookmarks = bookmarkService.getAllForUser(user.getId());
        }

        Set<String> allTags = bookmarkService.getDistinctTags(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("allTags", allTags);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("activeTag", tag == null ? "all" : tag);

        return "index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("bookmarkForm", new BookmarkForm());
        model.addAttribute("editMode", false);
        return "bookmark-form";
    }

    @PostMapping
    public String create(@Validated @ModelAttribute("bookmarkForm") BookmarkForm form,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);
            return "bookmark-form";
        }

        User user = currentUser(authentication);
        bookmarkService.create(form.getTitle(), form.getUrl(), form.getDescription(), form.getTags(), user.getId());

        return "redirect:/bookmarks";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Authentication authentication, Model model) {
        User user = currentUser(authentication);
        Bookmark bookmark = bookmarkService.getOwnedBookmarkOrThrow(id, user.getId());

        BookmarkForm form = new BookmarkForm();
        form.setTitle(bookmark.getTitle());
        form.setUrl(bookmark.getUrl());
        form.setDescription(bookmark.getDescription());
        form.setTags(bookmark.getTags() == null ? "" : String.join(", ", bookmark.getTags()));

        model.addAttribute("bookmarkForm", form);
        model.addAttribute("editMode", true);
        model.addAttribute("bookmarkId", id);
        return "bookmark-form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable String id,
                          @Validated @ModelAttribute("bookmarkForm") BookmarkForm form,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("bookmarkId", id);
            return "bookmark-form";
        }

        User user = currentUser(authentication);
        bookmarkService.update(id, form.getTitle(), form.getUrl(), form.getDescription(), form.getTags(), user.getId());

        return "redirect:/bookmarks";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable String id, Authentication authentication) {
        User user = currentUser(authentication);
        bookmarkService.delete(id, user.getId());
        return "redirect:/bookmarks";
    }

    @Data
    public static class BookmarkForm {

        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "URL is required")
        private String url;

        private String description;

        private String tags;

    }

}
