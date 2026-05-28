package com.bookclub.repository;

import com.bookclub.entity.Shelf;
import com.bookclub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    List<Shelf> findByUser(User user);
    Optional<Shelf> findByUserAndFavorite(User user, boolean favorite);
    boolean existsByUserAndName(User user, String name);
}