package com.example.linkshortener.service;

import com.example.linkshortener.dto.UrlRequest;
import com.example.linkshortener.dto.UrlResponse;
import com.example.linkshortener.entity.Url;
import com.example.linkshortener.exception.AliasAlreadyExistException;
import com.example.linkshortener.exception.GenerationShortCodeException;
import com.example.linkshortener.exception.UrlExpiredException;
import com.example.linkshortener.exception.UrlNotFoundException;
import com.example.linkshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UrlResponse createShortUrl(UrlRequest request) {
        String shortUrl;

        if (request.getAlias() != null && !request.getAlias().isBlank()) {
            if (urlRepository.existsByShortUrl(request.getAlias())) {
                throw new AliasAlreadyExistException("Alias already exist: " + request.getAlias());
            }
            shortUrl = request.getAlias();
        } else {
            shortUrl = generateUniqueCode();
        }

        Url url = Url.builder()
                .shortUrl(shortUrl)
                .originalUrl(request.getOriginalUrl())
                .expiresAt(request.getTtlSeconds() != null
                        ? LocalDateTime.now().plusSeconds(request.getTtlSeconds())
                        : null)
                .build();

        urlRepository.save(url);

        return UrlResponse.builder()
                .shortUrl(baseUrl + "/" + shortUrl)
                .originalUrl(request.getOriginalUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortUrl));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("Short URL has expired: " + shortUrl);
        }
        return url.getOriginalUrl();
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = generateCode();
            attempts++;
            if (attempts > 10) {
                throw new GenerationShortCodeException("Failed to generate unique short code after 10 attempts");
            }
        } while (urlRepository.existsByShortUrl(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
