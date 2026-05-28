package com.bookclub.controller;

import com.bookclub.dto.ShelfDto;
import com.bookclub.entity.*;
import com.bookclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        // Пробуем получить User напрямую из principal
        if (auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        // Если там UserDetails, то ищем по username
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    // GET /api/shelves
    @GetMapping
    public List<ShelfDto.Response> myShelves(Authentication auth) {
        User user = getCurrentUser(auth);
        return shelfRepository.findByUser(user).stream()
            .map(ShelfDto.Response::from)
            .toList();
    }

    // POST /api/shelves
    @PostMapping
    public ResponseEntity<ShelfDto.Response> create(
            @Valid @RequestBody ShelfDto.CreateRequest req,
            Authentication auth) {
        User user = getCurrentUser(auth);
        
        if (shelfRepository.existsByUserAndName(user, req.getName())) {
            return ResponseEntity.badRequest().body(null);
        }

        Shelf shelf = Shelf.builder()
            .name(req.getName())
            .user(user)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ShelfDto.Response.from(shelfRepository.save(shelf)));
    }

    // DELETE /api/shelves/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User user = getCurrentUser(auth);
        Shelf shelf = shelfRepository.findById(id).orElse(null);
        if (shelf == null || !shelf.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (shelf.isFavorite()) {
            return ResponseEntity.badRequest().body("Cannot delete favorites shelf");
        }
        shelfRepository.delete(shelf);
        return ResponseEntity.noContent().build();
    }

    // POST /api/shelves/{shelfId}/books/{bookId}
    @PostMapping("/{shelfId}/books/{bookId}")
    public ResponseEntity<?> addBook(
            @PathVariable Long shelfId,
            @PathVariable Long bookId,
            Authentication auth) {
        User user = getCurrentUser(auth);
        Shelf shelf = shelfRepository.findById(shelfId).orElse(null);
        if (shelf == null || !shelf.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        if (shelf.getBooks().contains(book)) {
            return ResponseEntity.badRequest().body("Book already in shelf");
        }
        shelf.getBooks().add(book);
        shelfRepository.save(shelf);
        return ResponseEntity.ok(ShelfDto.Response.from(shelf));
    }

    // DELETE /api/shelves/{shelfId}/books/{bookId}
    @DeleteMapping("/{shelfId}/books/{bookId}")
    public ResponseEntity<?> removeBook(
            @PathVariable Long shelfId,
            @PathVariable Long bookId,
            Authentication auth) {
        User user = getCurrentUser(auth);
        Shelf shelf = shelfRepository.findById(shelfId).orElse(null);
        if (shelf == null || !shelf.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.notFound().build();
        shelf.getBooks().remove(book);
        shelfRepository.save(shelf);
        return ResponseEntity.noContent().build();
    }

}