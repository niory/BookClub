package com.bookclub.dto;

import com.bookclub.entity.Shelf;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class ShelfDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 100)
        private String name;
    }

    @Getter @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private boolean favorite;
        private int bookCount;
        private List<BookDto.Response> books;
        private LocalDateTime createdAt;

        public static Response from(Shelf s) {
            return new Response(
                s.getId(),
                s.getName(),
                s.isFavorite(),
                s.getBooks().size(),
                s.getBooks().stream().map(BookDto.Response::from).toList(),
                s.getCreatedAt()
            );
        }
    }
}