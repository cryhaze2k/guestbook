package ua.com.lab.guestbook.db;

import java.time.Instant;

// Використовуємо 'record' для простого DTO (Data Transfer Object)
public record Comment(
        long id,
        String author,
        String text,
        Instant createdAt
) {}