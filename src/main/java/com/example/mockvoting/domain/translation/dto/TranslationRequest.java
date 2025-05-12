package com.example.mockvoting.domain.translation.dto;

import java.util.List;

public class TranslationRequest {
    private List<String> texts;
    private String targetLanguage;

    // Getters and Setters
    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
