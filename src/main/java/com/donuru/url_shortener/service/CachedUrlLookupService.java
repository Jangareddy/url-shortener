package com.donuru.url_shortener.service;

import com.donuru.url_shortener.entity.ShortUrl;
import com.donuru.url_shortener.exception.ShortUrlNotFoundException;
import com.donuru.url_shortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CachedUrlLookupService {

    private final ShortUrlRepository repository;

    @Cacheable(value = "shortUrls", key = "#shortCode")
    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode).orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
        if (!shortUrl.isActive()) {
            throw new ShortUrlNotFoundException(shortCode);
        }
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShortUrlNotFoundException(shortCode);
        }
        return shortUrl.getOriginalUrl();
    }
}