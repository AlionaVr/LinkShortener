package com.example.linkshortener.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Size(min = 5, max = 16, message = "Alias must be between 5 and 16 characters")
    private String alias;

    @Min(value = 1, message = "TTL must be at least 1 second")
    private Long ttlSeconds;

}
