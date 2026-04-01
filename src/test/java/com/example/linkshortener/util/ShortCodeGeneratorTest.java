package com.example.linkshortener.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generate_shouldReturnCodeOfLength8() {
        assertThat(generator.generate()).hasSize(8);
    }

    @Test
    void generate_shouldContainOnlyAllowedCharacters() {
        String code = generator.generate();
        assertThat(code).matches("[a-zA-Z0-9]+");
    }

    @RepeatedTest(5)
    void generate_repeatedCalls_shouldProduceDifferentCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(generator.generate());
        }
        assertThat(codes.size()).isGreaterThan(95);
    }

    @Test
    void generate_shouldNeverReturnNullOrBlank() {
        for (int i = 0; i < 50; i++) {
            assertThat(generator.generate()).isNotNull().isNotBlank();
        }
    }
}