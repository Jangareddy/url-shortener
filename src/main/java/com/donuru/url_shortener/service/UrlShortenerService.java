package com.donuru.url_shortener.service;

import com.donuru.url_shortener.dto.CreateShortUrlRequest;
import com.donuru.url_shortener.dto.ShortUrlResponse;
import com.donuru.url_shortener.entity.ShortUrl;
import com.donuru.url_shortener.exception.InvalidUrlException;
import com.donuru.url_shortener.exception.ShortUrlNotFoundException;
import com.donuru.url_shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final CachedUrlLookupService cachedUrlLookupService;
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        validateUrl(request.url());
        String shortCode = generateUniqueShortCode();
        LocalDateTime expiresAt = null;
        if (request.expirationDays() != null) {
            if (request.expirationDays() <= 0) {
                throw new InvalidUrlException("Expiration days must be greater than zero");
            }
            expiresAt = LocalDateTime.now().plusDays(request.expirationDays());
        }
        ShortUrl shortUrl = ShortUrl.builder().shortCode(shortCode).originalUrl(request.url()).expiresAt(expiresAt).active(true).build();
        ShortUrl saved = repository.save(shortUrl);
        return toResponse(saved);
    }

    @Transactional
    public String resolveOriginalUrl(String shortCode) {
        String originalUrl = cachedUrlLookupService.getOriginalUrl(shortCode);
        repository.recordClick(shortCode, LocalDateTime.now());
        return originalUrl;
    }

    @Transactional(readOnly = true)
    public ShortUrlResponse getShortUrl(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode).orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        return toResponse(shortUrl);
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();
            if (!repository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique short code");
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidUrlException("Only HTTP and HTTPS URLs are supported");
            }
            if (uri.getHost() == null) {
                throw new InvalidUrlException("Invalid URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidUrlException("Invalid URL");
        }
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(shortUrl.getShortCode(), baseUrl + "/" + shortUrl.getShortCode(), shortUrl.getOriginalUrl(), shortUrl.getCreatedAt(), shortUrl.getExpiresAt(), shortUrl.getClickCount(), shortUrl.getLastAccessedAt());
    }
}