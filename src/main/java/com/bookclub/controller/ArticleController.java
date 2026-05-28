package com.bookclub.controller;

import com.bookclub.dto.ArticleDto;
import com.bookclub.entity.*;
import com.bookclub.entity.Article.ArticleStatus;
import com.bookclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleRepository articleRepository;

    @GetMapping("/published")
    public Page<ArticleDto.Response> published(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable)
                                .map(ArticleDto.Response::from);
    }

    @GetMapping("/my")
    public List<ArticleDto.Response> myArticles(Authentication auth) {
        User author = (User) auth.getPrincipal();
        return articleRepository.findByCreatedBy(author).stream()
                                .map(ArticleDto.Response::from).toList();
    }

    @PostMapping
    public ResponseEntity<ArticleDto.Response> create(
            @Valid @RequestBody ArticleDto.CreateRequest req,
            Authentication auth) {
        User author = (User) auth.getPrincipal();
        Article article = Article.builder()
            .title(req.getTitle())
            .content(req.getContent())
            .status(req.isPublish() ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT)
            .createdBy(author)
            .build();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ArticleDto.Response.from(articleRepository.save(article)));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ArticleDto.Response> publish(@PathVariable Long id, Authentication auth) {
        User author = (User) auth.getPrincipal();
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null || !article.getCreatedBy().getId().equals(author.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        article.setStatus(ArticleStatus.PUBLISHED);
        return ResponseEntity.ok(ArticleDto.Response.from(articleRepository.save(article)));
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<ArticleDto.Response> submit(@PathVariable Long id, Authentication auth) {
        User author = (User) auth.getPrincipal();
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null || !article.getCreatedBy().getId().equals(author.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (article.getStatus() != ArticleStatus.DRAFT)
            return ResponseEntity.badRequest().build();
        article.setStatus(ArticleStatus.PUBLISHED);
        return ResponseEntity.ok(ArticleDto.Response.from(articleRepository.save(article)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        User author = (User) auth.getPrincipal();
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null || !article.getCreatedBy().getId().equals(author.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (article.getStatus() != ArticleStatus.DRAFT)
            return ResponseEntity.badRequest().build();
        articleRepository.delete(article);
        return ResponseEntity.noContent().build();
    }
}