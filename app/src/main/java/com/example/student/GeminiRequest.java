package com.example.student;

import java.util.List;
import java.util.ArrayList;

public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(String query) {
        this.contents = new ArrayList<>();
        Content content = new Content();
        Part part = new Part();
        part.setText(query);
        content.setParts(List.of(part)); // Use Arrays.asList(part) for lower API levels
        contents.add(content);
    }

    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}