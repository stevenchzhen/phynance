package com.phynance.controller;

import com.phynance.model.WavePhysicsAnalysisRequest;
import com.phynance.model.WavePhysicsAnalysisResponse;
import com.phynance.service.WavePhysicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class WavePhysicsAnalysisController {
    @Autowired
    private WavePhysicsService wavePhysicsService;

    @PostMapping("/wave-physics")
    public ResponseEntity<WavePhysicsAnalysisResponse> analyze(@RequestBody WavePhysicsAnalysisRequest request) {
        WavePhysicsAnalysisResponse response = wavePhysicsService.analyze(request);
        return ResponseEntity.ok(response);
    }
} 