package com.example.linkshortener.controller;

import com.example.linkshortener.dto.UrlRequest;
import com.example.linkshortener.entity.Url;
import com.example.linkshortener.repository.UrlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UrlControllerIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("urlshortener")
            .withUsername("postgres")
            .withPassword("postgres");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UrlRepository urlRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        urlRepository.deleteAll();
    }

    @Test
    void createShortUrl_shouldReturn200() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://google.com");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"));
    }

    @Test
    void createShortUrl_withAlias_shouldUseAlias() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://google.com");
        request.setAlias("mygoogle");

        mockMvc.perform(post("/shorten")
                        .header("Host", "localhost:8080")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/mygoogle"));
    }

    @Test
    void createShortUrl_withDuplicateAlias_shouldReturnConflict() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://google.com");
        request.setAlias("mygoogle");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createShortUrl_withInvalidUrl_shouldReturnBadRequest() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("not-a-url");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_shouldReturnFoundAndRedirect() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://google.com");
        request.setAlias("mygoogle");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/mygoogle"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    void redirect_whenExpired_shouldReturn410() throws Exception {
        Url expiredUrl = Url.builder()
                .shortCode("expired")
                .originalUrl("https://expired.com")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        urlRepository.save(expiredUrl);

        mockMvc.perform(get("/expired"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Short code has expired: expired"));
    }

    @Test
    void redirect_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/notexist"))
                .andExpect(status().isNotFound());
    }
}