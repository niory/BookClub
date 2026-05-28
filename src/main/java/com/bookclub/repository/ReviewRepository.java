package com.bookclub.repository;

import com.bookclub.entity.Review;
import com.bookclub.entity.Book;
import com.bookclub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBook(Book book);
    Optional<Review> findByBookAndUser(Book book, User user);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double avgRatingByBook(@Param("book") Book book);
}
