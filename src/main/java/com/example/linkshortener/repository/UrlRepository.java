package com.example.linkshortener.repository;

import com.example.linkshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    public Optional<Url> findByShortUrl(String shortUrl);

    public boolean existsByShortUrl(String shortCode);
}
