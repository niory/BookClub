package com.bookclub.controller;

import com.bookclub.dto.AuthDto.*;
import com.bookclub.entity.*;
import com.bookclub.repository.*;
import com.bookclub.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            return ResponseEntity.badRequest().body("Username already taken");
        if (userRepository.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body("Email already registered");

        User user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole())
            .build();

        userRepository.save(user);
        
        // Создаём полку "Избранное"
        Shelf favorites = Shelf.builder()
            .name("Избранное")
            .favorite(true)
            .user(user)
            .build();
        shelfRepository.save(favorites);

        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new TokenResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (user.isBlocked())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is blocked");

        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new TokenResponse(token, user.getUsername(), user.getRole().name()));
    }
}