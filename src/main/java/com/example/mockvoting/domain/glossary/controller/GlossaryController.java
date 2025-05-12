package com.example.mockvoting.domain.glossary.controller;



import com.example.mockvoting.domain.glossary.dto.GlossaryDTO;
import com.example.mockvoting.domain.glossary.service.GlossaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glossary")
@CrossOrigin(origins = "*")
public class GlossaryController {

    private final GlossaryService service;

    public GlossaryController(GlossaryService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String q) {
        GlossaryDTO dto = service.search(q.trim());
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(404)
                .body("용어를 찾을 수 없습니다: " + q);
    }
}