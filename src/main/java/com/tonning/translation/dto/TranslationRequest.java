package com.tonning.translation.dto;

// Java object to deserialise the request JSON

public class TranslationRequest {
    private String text;

    public TranslationRequest() {}

    public TranslationRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}