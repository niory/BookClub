package com.bookclub.dto;

import com.bookclub.entity.Book;
import com.bookclub.entity.Book.BookStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class BookDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 200)
        private String title;
        @NotBlank @Size(max = 150)
        private String authorName;
        @Size(max = 300)
        private String genres;
        private String description;
        @Size(max = 500)
        private String coverUrl;
        private boolean submitForModeration = false;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 200) private String title;
        @Size(max = 150) private String authorName;
        @Size(max = 300) private String genres;
        private String description;
        @Size(max = 500) private String coverUrl;
    }

    @Getter @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String authorName;
        private String genres;
        private String description;
        private String coverUrl;
        private BookStatus status;
        private String createdBy;
        private LocalDateTime createdAt;

        public static Response from(Book b) {
            return new Response(
                b.getId(), b.getTitle(), b.getAuthorName(),
                b.getGenres(), b.getDescription(), b.getCoverUrl(),
                b.getStatus(), b.getCreatedBy().getUsername(), b.getCreatedAt()
            );
        }
    }
}
