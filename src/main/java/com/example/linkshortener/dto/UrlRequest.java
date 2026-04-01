package com.example.linkshortener.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^(https?://).*",
            message = "URL must start with http:// or https://")
    private String originalUrl;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{5,16}$",
            message = "Alias must be 5-16 chars and contain only letters, numbers, _ or -"
    )
    private String alias;

    @Min(value = 1, message = "TTL must be at least 1 second")
    private Long ttlSeconds;

}