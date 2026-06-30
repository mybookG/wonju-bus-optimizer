package com.wonju.bus.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiRequest(
        List<Content> contents,
        @JsonProperty("generationConfig") GenerationConfig generationConfig
) {
    public static GeminiRequest of(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt)))),
                new GenerationConfig("application/json")
        );
    }

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record GenerationConfig(@JsonProperty("responseMimeType") String responseMimeType) {}
}
