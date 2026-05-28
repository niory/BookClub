package com.bookclub.repository;

import com.bookclub.entity.Book;
import com.bookclub.entity.Book.BookStatus;
import com.bookclub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    List<Book> findByCreatedBy(User author);

    @Query("SELECT b FROM Book b WHERE b.status = 'APPROVED' AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(b.authorName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(b.genres) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Book> search(@Param("q") String query, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b WHERE b.status = 'APPROVED' AND b.id NOT IN :excludeIds AND " +
           "(LOWER(b.genres) LIKE LOWER(CONCAT('%', :genre1, '%')) OR " +
           "LOWER(b.genres) LIKE LOWER(CONCAT('%', :genre2, '%')) OR " +
           "LOWER(b.genres) LIKE LOWER(CONCAT('%', :genre3, '%')))")
    List<Book> findRecommended(@Param("genre1") String g1, 
                               @Param("genre2") String g2, 
                               @Param("genre3") String g3, 
                               @Param("excludeIds") List<Long> excludeIds);
}