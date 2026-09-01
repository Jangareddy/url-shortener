package com.donuru.url_shortener.dto;

import java.time.LocalDateTime;

public record ShortUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        long clickCount,
        LocalDateTime lastAccessedAt
) {
}

