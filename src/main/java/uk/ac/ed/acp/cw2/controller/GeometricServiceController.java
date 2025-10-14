package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import uk.ac.ed.acp.cw2.Service.GeometricService;
import uk.ac.ed.acp.cw2.dto.*;

import java.net.URL;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GeometricServiceController {

    private static final Logger logger = LoggerFactory.getLogger(GeometricServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    private final GeometricService geometricService;

    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    @GetMapping("/uid")
    public String uid() {
        return "s2556257";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody DistanceRequest request) {
        try {
            return ResponseEntity.ok(geometricService.calculateDistance(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody DistanceRequest request) {
        try {
            return ResponseEntity.ok(geometricService.isCloseTo(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(@RequestBody NextPositionRequest request) {
        try {
            return ResponseEntity.ok(geometricService.nextPosition(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody IsInRegionRequest request) {
        try {
            return ResponseEntity.ok(geometricService.isInRegion(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}