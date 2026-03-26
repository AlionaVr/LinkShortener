package com.example.linkshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlResponse {

    private String originalUrl;
    private String shortUrl;
}
