package com.bookclub.controller;

import com.bookclub.dto.BookDto;
import com.bookclub.entity.*;
import com.bookclub.entity.Book.BookStatus;
import com.bookclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MODERATOR')")
public class ModeratorController {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // Очередь книг на модерацию
    @GetMapping("/books")
    public List<BookDto.Response> pending() {
        return bookRepository.findByStatus(BookStatus.PENDING,
            org.springframework.data.domain.Pageable.unpaged())
            .stream().map(BookDto.Response::from).toList();
    }

    // Одобрить книгу
    @PostMapping("/books/{id}/approve")
    public ResponseEntity<BookDto.Response> approve(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null || book.getStatus() != BookStatus.PENDING)
            return ResponseEntity.notFound().build();
        book.setStatus(BookStatus.APPROVED);
        return ResponseEntity.ok(BookDto.Response.from(bookRepository.save(book)));
    }

    // Отклонить книгу
    @PostMapping("/books/{id}/reject")
    public ResponseEntity<BookDto.Response> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        book.setStatus(BookStatus.REJECTED);
        // reason можно сохранить в отдельном поле при необходимости
        return ResponseEntity.ok(BookDto.Response.from(bookRepository.save(book)));
    }

    // Удалить отзыв
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) return ResponseEntity.notFound().build();
        reviewRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Заблокировать пользователя
    @PostMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setBlocked(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("blocked", true, "userId", id));
    }

    // Разблокировать пользователя
    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setBlocked(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("blocked", false, "userId", id));
    }
}
