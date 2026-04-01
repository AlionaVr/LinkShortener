## URL Shortener Service

A simple URL shortening service similar to TinyURL, built with Spring Boot and PostgreSQL, providing only an HTTP API.

### Features

* Accepts long URLs and returns shortened versions.
* Redirects users from a short URL to the original long URL.
* Custom alias support for readable URLs.
* TTL (time-to-live) support for URLs — links can expire after a set duration or remain permanent (`if ttl == null`).
* Persistent storage using `PostgreSQL` with `Hibernate/JPA`.
* Database migrations handled via `Liquibase`.
* Development environment setup with `Docker` .
* Unit and integration tests included.
* Build system: `Gradle`.

### Technologies Used

* Java 17
* Spring Boot 3.2
* PostgreSQL
* Hibernate/JPA
* Liquibase
* Docker
* Gradle
* JUnit & Mockito for testing
* Testcontainers for integration testing
* Lombok for boilerplate code reduction

### API Endpoints

* `POST /shorten` - Create a shortened URL.
    * Request Body:
      ```json
      {
        "originalUrl": "https://www.google.com",
        "alias": "mygoogle3",
        "ttlSeconds": 1
      }
      ```
    * Response:
      ```json
        {
        "originalUrl": "https://www.google.com",
        "shortUrl": "http://localhost:8080/mygoogle3"
        }
        ```
* `GET /{shortCode}` - Redirect to the original long URL.

