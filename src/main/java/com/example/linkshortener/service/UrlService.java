package com.example.linkshortener.service;

import com.example.linkshortener.dto.UrlRequest;
import com.example.linkshortener.dto.UrlResponse;
import com.example.linkshortener.entity.Url;
import com.example.linkshortener.exception.AliasAlreadyExistException;
import com.example.linkshortener.exception.GenerationShortCodeException;
import com.example.linkshortener.exception.UrlExpiredException;
import com.example.linkshortener.exception.UrlNotFoundException;
import com.example.linkshortener.repository.UrlRepository;
import com.example.linkshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final int MAX_ATTEMPTS = 10;

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("Short code has expired: " + shortCode);
        }
        return url.getOriginalUrl();
    }

    @Transactional
    public UrlResponse createShortUrl(UrlRequest request) {
        if (request.getAlias() != null && !request.getAlias().isBlank()) {
            return createWithAlias(request);
        }
        return createWithGeneratedCode(request);
    }

    private UrlResponse createWithAlias(UrlRequest request) {
        try {
            return saveAndBuildResponse(request.getAlias(), request);
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateKeyViolation(e)) {
                throw new AliasAlreadyExistException("Alias already exist: " + request.getAlias());
            }
            throw e; // NOT NULL, FK violation
        }
    }

    private UrlResponse createWithGeneratedCode(UrlRequest request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return saveAndBuildResponse(shortCodeGenerator.generate(), request);
            } catch (DataIntegrityViolationException e) {
                if (!isDuplicateKeyViolation(e)) {
                    throw e;
                }
                if (attempt == MAX_ATTEMPTS) {
                    throw new GenerationShortCodeException(
                            "Failed to generate unique short code after " + MAX_ATTEMPTS + " attempts"
                    );
                }
            }
        }
        throw new GenerationShortCodeException("Failed to generate unique short code");
    }

    private UrlResponse saveAndBuildResponse(String shortCode,
                                             UrlRequest request) {

        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .expiresAt(request.getTtlSeconds() != null
                        ? LocalDateTime.now().plusSeconds(request.getTtlSeconds())
                        : null)
                .build();

        urlRepository.saveAndFlush(url);

        return UrlResponse.builder()
                .shortUrl(buildShortUrl(shortCode))
                .originalUrl(request.getOriginalUrl())
                .build();
    }

    protected String buildShortUrl(String shortCode) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath(null)
                .pathSegment(shortCode)
                .toUriString();
    }

    private boolean isDuplicateKeyViolation(DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause().getMessage();
        return msg != null && msg.toLowerCase().contains("duplicate");
    }
}