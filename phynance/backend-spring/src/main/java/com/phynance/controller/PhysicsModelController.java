package com.phynance.controller;

import com.phynance.model.HarmonicOscillatorRequest;
import com.phynance.model.PhysicsModelResult;
import com.phynance.service.PhysicsModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for financial harmonic oscillator analysis.
 */
@RestController
@RequestMapping("/api/v1/physics")
public class PhysicsModelController {
    private final PhysicsModelService physicsModelService;

    @Autowired
    public PhysicsModelController(PhysicsModelService physicsModelService) {
        this.physicsModelService = physicsModelService;
    }

    /**
     * Analyze stock price data using a damped harmonic oscillator model.
     * @param request HarmonicOscillatorRequest with OHLCV data
     * @return PhysicsModelResult with predictions and signals
     */
    @PostMapping("/harmonic-oscillator")
    public ResponseEntity<?> analyze(@RequestBody HarmonicOscillatorRequest request) {
        try {
            PhysicsModelResult result = physicsModelService.analyze(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }
} 