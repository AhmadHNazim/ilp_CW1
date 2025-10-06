package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.dto.Position;

import java.net.URL;
import java.time.Instant;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;


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
    public ResponseEntity<Double> calculateDistance(@RequestBody DistanceRequest request) {
        try {
            double dx = request.getPosition1().getLng() - request.getPosition2().getLng();
            double dy = request.getPosition1().getLat() - request.getPosition2().getLat();
            double distance = Math.sqrt(dx * dx + dy * dy);
            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody DistanceRequest request) {
        try {
            double dx = request.getPosition1().getLng() - request.getPosition2().getLng();
            double dy = request.getPosition1().getLat() - request.getPosition2().getLat();
            double distance = Math.sqrt(dx * dx + dy * dy);

            boolean close = distance < 0.00015;
            return ResponseEntity.ok(close);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(@RequestBody NextPositionRequest request) {
        try {
            Position start = request.getStart();
            if (start == null) {
                return ResponseEntity.badRequest().build();
            }

            double angle = request.getAngle();
            double step = 0.00015;
            double rad = Math.toRadians(angle);

            double newLat = start.getLat() + step * Math.sin(rad);
            double newLng = start.getLng() + step * Math.cos(rad);

            Position next = new Position();
            next.setLat(newLat);
            next.setLng(newLng);

            return ResponseEntity.ok(next);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
