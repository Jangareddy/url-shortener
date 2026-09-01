package com.donuru.url_shortener.service;

import com.donuru.url_shortener.dto.CreateShortUrlRequest;
import com.donuru.url_shortener.exception.InvalidUrlException;
import com.donuru.url_shortener.repository.ShortUrlRepository;
import com.donuru.url_shortener.util.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private CachedUrlLookupService cachedUrlLookupService;

    @Test
    void shouldRejectInvalidUrl() {

        UrlShortenerService service =
                new UrlShortenerService(
                        repository,
                        shortCodeGenerator,
                        cachedUrlLookupService
                );

        CreateShortUrlRequest request =
                new CreateShortUrlRequest(
                        "not-a-valid-url",
                        null
                );

        assertThrows(
                InvalidUrlException.class,
                () -> service.createShortUrl(request)
        );
    }
}