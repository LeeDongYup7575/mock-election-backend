package com.example.mockvoting.domain.translation.dto;

import java.util.List;

public class TranslationResponse {
    private List<Translation> translations;

    public TranslationResponse() {
    }

    public TranslationResponse(List<Translation> translations) {
        this.translations = translations;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public static class Translation {
        private String translatedText;

        public Translation() {
        }

        public Translation(String translatedText) {
            this.translatedText = translatedText;
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }
    }
}
