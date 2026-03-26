package com.example.linkshortener.service;

import com.example.linkshortener.dto.UrlRequest;
import com.example.linkshortener.dto.UrlResponse;
import com.example.linkshortener.entity.Url;
import com.example.linkshortener.exception.AliasAlreadyExistException;
import com.example.linkshortener.exception.UrlExpiredException;
import com.example.linkshortener.exception.UrlNotFoundException;
import com.example.linkshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void createShortUrl_withAlias_shouldUseAlias() {
        UrlRequest request = new UrlRequest("https://google.com", "mygoogle", null);
        when(urlRepository.existsByShortUrl("mygoogle")).thenReturn(false);
        when(urlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UrlResponse response = urlService.createShortUrl(request);

        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/mygoogle");
        assertThat(response.getOriginalUrl()).isEqualTo("https://google.com");
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void createShortUrl_withDuplicateAlias_shouldThrowAliasAlreadyExistException() {
        UrlRequest request = new UrlRequest("https://google.com", "mygoogle", null);
        when(urlRepository.existsByShortUrl("mygoogle")).thenReturn(true);

        assertThatThrownBy(() -> urlService.createShortUrl(request))
                .isInstanceOf(AliasAlreadyExistException.class)
                .hasMessageContaining("mygoogle");

        verify(urlRepository, never()).save(any());
    }

    @Test
    void createShortUrl_withoutAlias_shouldGenerateCode() {
        UrlRequest request = new UrlRequest("https://google.com", null, null);
        when(urlRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UrlResponse response = urlService.createShortUrl(request);

        assertThat(response.getShortUrl()).startsWith("http://localhost:8080/");
        assertThat(response.getOriginalUrl()).isEqualTo("https://google.com");
        String code = response.getShortUrl().replace("http://localhost:8080/", "");
        assertThat(code).hasSize(8);
    }

    @Test
    void createShortUrl_withBlankAlias_shouldGenerateCode() {
        UrlRequest request = new UrlRequest("https://google.com", "   ", null);
        when(urlRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UrlResponse response = urlService.createShortUrl(request);

        assertThat(response.getShortUrl()).startsWith("http://localhost:8080/");
        String code = response.getShortUrl().replace("http://localhost:8080/", "");
        assertThat(code).hasSize(8);
    }

    @Test
    void createShortUrl_withTtl_shouldSetExpiresAt() {
        UrlRequest request = new UrlRequest("https://google.com", null, 3600L);
        when(urlRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        urlService.createShortUrl(request);

        verify(urlRepository).save(argThat(url ->
                url.getExpiresAt() != null &&
                        url.getExpiresAt().isAfter(LocalDateTime.now())
        ));
    }

    @Test
    void createShortUrl_withoutTtl_shouldLeaveExpiresAtNull() {
        UrlRequest request = new UrlRequest("https://google.com", null, null);
        when(urlRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        urlService.createShortUrl(request);

        verify(urlRepository).save(argThat(url -> url.getExpiresAt() == null));
    }

    @Test
    void getOriginalUrl_whenExists_shouldReturnOriginalUrl() {
        Url url = Url.builder()
                .shortUrl("abc12345")
                .originalUrl("https://google.com")
                .expiresAt(null)
                .build();
        when(urlRepository.findByShortUrl("abc12345")).thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl("abc12345");

        assertThat(result).isEqualTo("https://google.com");
    }

    @Test
    void getOriginalUrl_whenNotFound_shouldThrowUrlNotFoundException() {
        when(urlRepository.findByShortUrl("notexist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getOriginalUrl("notexist"))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessageContaining("notexist");
    }

    @Test
    void getOriginalUrl_whenExpired_shouldThrowUrlExpiredException() {
        Url url = Url.builder()
                .shortUrl("expired")
                .originalUrl("https://google.com")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        when(urlRepository.findByShortUrl("expired")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.getOriginalUrl("expired"))
                .isInstanceOf(UrlExpiredException.class)
                .hasMessageContaining("expired");
    }
}