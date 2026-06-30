package com.wonju.bus.infrastructure.gemini.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) return "";
        Candidate first = candidates.getFirst();
        if (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty()) return "";
        return first.content().parts().getFirst().text();
    }

    public record Candidate(Content content) {}

    public record Content(List<Part> parts, String role) {}

    public record Part(String text) {}
}
