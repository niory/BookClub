package com.bookclub.controller;

import com.bookclub.dto.BookDto;
import com.bookclub.entity.*;
import com.bookclub.entity.Book.BookStatus;
import com.bookclub.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;

    @GetMapping
    public Page<BookDto.Response> catalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        return bookRepository.findByStatus(BookStatus.APPROVED, pageable)
                             .map(BookDto.Response::from);
    }

    @GetMapping("/search")
    public Page<BookDto.Response> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookRepository.search(q, PageRequest.of(page, size)).map(BookDto.Response::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto.Response> getById(@PathVariable Long id) {
        return bookRepository.findById(id)
            .filter(b -> b.getStatus() == BookStatus.APPROVED)
            .map(b -> ResponseEntity.ok(BookDto.Response.from(b)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookDto.Response> create(
            @Valid @RequestBody BookDto.CreateRequest req,
            Authentication auth) {

        User author = (User) auth.getPrincipal();

        BookStatus status;
        if (author.getRole() == User.Role.MODERATOR) {
            status = BookStatus.APPROVED;
        } else {
            status = req.isSubmitForModeration() ? BookStatus.PENDING : BookStatus.DRAFT;
        }

        Book book = Book.builder()
            .title(req.getTitle())
            .authorName(req.getAuthorName())
            .genres(req.getGenres())
            .description(req.getDescription())
            .coverUrl(req.getCoverUrl())
            .status(status)
            .createdBy(author)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BookDto.Response.from(bookRepository.save(book)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody BookDto.UpdateRequest req,
            Authentication auth) {

        User author = (User) auth.getPrincipal();

        Book book = bookRepository.findById(id)
            .filter(b -> b.getCreatedBy().getId().equals(author.getId()))
            .orElse(null);
        if (book == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (book.getStatus() == BookStatus.APPROVED)
            return ResponseEntity.badRequest().build();

        if (req.getTitle() != null) book.setTitle(req.getTitle());
        if (req.getAuthorName() != null) book.setAuthorName(req.getAuthorName());
        if (req.getGenres() != null) book.setGenres(req.getGenres());
        if (req.getDescription() != null) book.setDescription(req.getDescription());
        if (req.getCoverUrl() != null) book.setCoverUrl(req.getCoverUrl());

        return ResponseEntity.ok(BookDto.Response.from(bookRepository.save(book)));
    }

    @GetMapping("/my")
    public List<BookDto.Response> myBooks(Authentication auth) {
        User author = (User) auth.getPrincipal();
        return bookRepository.findByCreatedBy(author).stream()
                             .map(BookDto.Response::from).toList();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BookDto.Response> approve(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        if (book.getStatus() != BookStatus.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        book.setStatus(BookStatus.APPROVED);
        return ResponseEntity.ok(BookDto.Response.from(bookRepository.save(book)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookDto.Response> reject(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        if (book.getStatus() != BookStatus.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        book.setStatus(BookStatus.REJECTED);
        return ResponseEntity.ok(BookDto.Response.from(bookRepository.save(book)));
    }

    @GetMapping("/pending")
    public Page<BookDto.Response> pendingBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookRepository.findByStatus(BookStatus.PENDING, pageable)
                            .map(BookDto.Response::from);
    }

    @GetMapping("/recommended")
    public List<BookDto.Response> recommended(Authentication auth) {
        User user = (User) auth.getPrincipal();
        
        Shelf favorites = shelfRepository.findByUserAndFavorite(user, true).orElse(null);
        if (favorites == null || favorites.getBooks().isEmpty()) {
            return List.of();
        }
        
        Set<String> allGenres = new HashSet<>();
        List<Long> favoriteIds = new ArrayList<>();
        for (Book book : favorites.getBooks()) {
            favoriteIds.add(book.getId());
            if (book.getGenres() != null) {
                for (String genre : book.getGenres().split(",\\s*")) {
                    allGenres.add(genre.trim().toLowerCase());
                }
            }
        }
        
        if (allGenres.isEmpty()) return List.of();
        
        List<String> genreList = new ArrayList<>(allGenres);
        String g1 = genreList.size() > 0 ? genreList.get(0) : "";
        String g2 = genreList.size() > 1 ? genreList.get(1) : "";
        String g3 = genreList.size() > 2 ? genreList.get(2) : "";
        
        return bookRepository.findRecommended(g1, g2, g3, favoriteIds)
            .stream()
            .map(BookDto.Response::from)
            .limit(20)
            .toList();
    }
}