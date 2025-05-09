package com.example.mockvoting.domain.translation.controller;


import com.example.mockvoting.domain.translation.dto.TranslationRequest;
import com.example.mockvoting.domain.translation.dto.TranslationResponse;
import com.example.mockvoting.domain.translation.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> translateTexts(@RequestBody TranslationRequest request) {
        TranslationResponse response = translationService.translateTexts(request.getTexts(), request.getTargetLanguage());
        return ResponseEntity.ok(response);
    }
}