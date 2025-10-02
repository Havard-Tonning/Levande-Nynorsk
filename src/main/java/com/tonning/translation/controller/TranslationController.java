package com.tonning.translation.controller;

import com.tonning.translation.dto.TranslationRequest;
import com.tonning.translation.dto.TranslationResponse;
import com.tonning.translation.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") // Allow CORS
@RequestMapping("/api/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<TranslationResponse> translate(@RequestBody TranslationRequest request) {
        String translated = translationService.Translate(request.getText());
        return ResponseEntity.ok(new TranslationResponse(translated));
    }
}