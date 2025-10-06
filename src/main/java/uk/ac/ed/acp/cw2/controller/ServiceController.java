package uk.ac.ed.acp.cw2.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import uk.ac.ed.acp.cw2.dto.*;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

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

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody IsInRegionRequest request) {
        try {
            Position point = request.getPosition();
            Region region = request.getRegion();

            if (point == null || region == null || region.getVertices() == null || region.getVertices().size() < 4) {
                return ResponseEntity.badRequest().build();
            }

            // Ensure the polygon is closed
            Position first = region.getVertices().getFirst();
            Position last = region.getVertices().getLast();
            if (!Objects.equals(first.getLat(), last.getLat()) || !Objects.equals(first.getLng(), last.getLng())) {
                return ResponseEntity.badRequest().build();
            }

            boolean inside = isPointInPolygon(point, region.getVertices());
            return ResponseEntity.ok(inside);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ray-casting algorithm
    private boolean isPointInPolygon(Position point, List<Position> vertices) {
        int crossings = 0;
        int count = vertices.size();

        for (int i = 0; i < count - 1; i++) {
            Position v1 = vertices.get(i);
            Position v2 = vertices.get(i + 1);

            boolean condY = (v1.getLat() > point.getLat()) != (v2.getLat() > point.getLat());
            if (condY) {
                double slope = (v2.getLng() - v1.getLng()) *
                        (point.getLat() - v1.getLat()) /
                        (v2.getLat() - v1.getLat()) + v1.getLng();
                if (point.getLng() < slope) {
                    crossings++;
                }
            }
        }

        return (crossings % 2 == 1); // odd = inside, even = outside
    }

}
