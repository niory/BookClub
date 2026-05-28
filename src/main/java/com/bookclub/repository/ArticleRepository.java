package com.bookclub.repository;

import com.bookclub.entity.Article;
import com.bookclub.entity.Article.ArticleStatus;
import com.bookclub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    List<Article> findByCreatedBy(User author);
}