package com.donuru.url_shortener.repository;

import com.donuru.url_shortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("""
            update ShortUrl s
            set s.clickCount = s.clickCount + 1,
                s.lastAccessedAt = :accessedAt
            where s.shortCode = :shortCode
            """)
    int recordClick(@Param("shortCode") String shortCode, @Param("accessedAt") LocalDateTime accessedAt);
}