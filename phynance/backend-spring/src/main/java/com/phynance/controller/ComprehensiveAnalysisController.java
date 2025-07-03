package com.phynance.controller;

import com.phynance.model.ComprehensiveAnalysisRequest;
import com.phynance.model.ComprehensiveAnalysisResponse;
import com.phynance.service.ComprehensiveAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class ComprehensiveAnalysisController {
    @Autowired
    private ComprehensiveAnalysisService comprehensiveAnalysisService;

    @PostMapping("/comprehensive")
    public ResponseEntity<ComprehensiveAnalysisResponse> analyze(@RequestBody ComprehensiveAnalysisRequest request) {
        ComprehensiveAnalysisResponse response = comprehensiveAnalysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
} 