package com.bookclub.dto;

import com.bookclub.entity.Article;
import com.bookclub.entity.Article.ArticleStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class ArticleDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 200)
        private String title;
        @NotBlank
        private String content;
        private Long bookId;  // ссылка на книгу (опционально)
        private boolean publish = false;  // сразу опубликовать или черновик
    }

    @Getter @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private ArticleStatus status;
        private int views;
        private String createdBy;
        private String bookTitle;  // название привязанной книги
        private LocalDateTime createdAt;

        public static Response from(Article a) {
            return new Response(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getStatus(),
                a.getViews(),
                a.getCreatedBy().getUsername(),
                null,  // bookTitle — можно добавить связь с Book позже
                a.getCreatedAt()
            );
        }
    }
}