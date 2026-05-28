package com.bookclub.controller;

import com.bookclub.entity.*;
import com.bookclub.entity.Book.BookStatus;
import com.bookclub.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @GetMapping
    public List<ReviewResponse> list(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        return reviewRepository.findByBook(book).stream().map(ReviewResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest req,
            @AuthenticationPrincipal User user) {

        Book book = bookRepository.findById(bookId)
            .filter(b -> b.getStatus() == BookStatus.APPROVED).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        if (reviewRepository.findByBookAndUser(book, user).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already reviewed");

        Review review = Review.builder()
            .book(book).user(user)
            .rating((byte) req.getRating())
            .text(req.getText())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ReviewResponse.from(reviewRepository.save(review)));
    }

    @GetMapping("/avg-rating")
    public Map<String, Object> avgRating(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        Double avg = reviewRepository.avgRatingByBook(book);
        return Map.of("bookId", bookId, "avgRating", avg != null ? avg : 0.0);
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    @Getter @Setter
    static class ReviewRequest {
        @Min(1) @Max(5)
        private int rating;
        private String text;
    }

    @Getter @AllArgsConstructor
    static class ReviewResponse {
        private Long id;
        private String username;
        private int rating;
        private String text;
        private LocalDateTime createdAt;

        static ReviewResponse from(Review r) {
            return new ReviewResponse(r.getId(), r.getUser().getUsername(),
                                      r.getRating(), r.getText(), r.getCreatedAt());
        }
    }
}
