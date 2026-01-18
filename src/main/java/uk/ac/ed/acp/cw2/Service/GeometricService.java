package uk.ac.ed.acp.cw2.Service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.*;

import java.util.List;
import java.util.Objects;

@Service
public class GeometricService {

    private static final Logger logger = LoggerFactory.getLogger(GeometricService.class);

    private static final double STEP_SIZE = 0.00015;
    private static final double COMPASS_DEGREE = 22.5;
    private static final double EPSILON = 1e-9;

    public double calculateDistance(DistanceRequest request) {
        Position p1 = request.getPosition1();
        Position p2 = request.getPosition2();

        validateCoordinate(p1.getLat(), p1.getLng());
        validateCoordinate(p2.getLat(), p2.getLng());

        double dx = p1.getLng() - p2.getLng();
        double dy = p1.getLat() - p2.getLat();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isCloseTo(DistanceRequest request) {
        return calculateDistance(request) < STEP_SIZE;
    }

    public Position nextPosition(NextPositionRequest request) {
        Position start = request.getStart();
        double angle = request.getAngle();

        if (start == null) throw new IllegalArgumentException();

        validateCoordinate(start.getLat(), start.getLng());
        validateAngle(angle);

        double rad = Math.toRadians(angle);
        double newLat = start.getLat() + STEP_SIZE * Math.sin(rad);
        double newLng = start.getLng() + STEP_SIZE * Math.cos(rad);

        Position newPosition = new Position();
        newPosition.setLng(newLng);
        newPosition.setLat(newLat);

        return newPosition;
    }

    public boolean isInRegion(IsInRegionRequest request) {
        Position point = request.getPosition();
        Region region = request.getRegion();

        if (point == null || region == null || region.getVertices() == null || region.getVertices().size() < 4) {
            throw new IllegalArgumentException();
        }

        validateCoordinate(point.getLat(), point.getLng());

        List<Position> vertices = region.getVertices();

        for (Position v : vertices) {
            validateCoordinate(v.getLat(), v.getLng());
        }

        Position first = vertices.getFirst();
        Position last = vertices.getLast();

        if (!Objects.equals(first.getLat(), last.getLat()) || !Objects.equals(first.getLng(), last.getLng())) {
            throw new IllegalArgumentException();
        }

        return isPointInPolygon(point, vertices);
    }

    private boolean isPointInPolygon(Position point, List<Position> vertices) {
        int crossings = 0;
        // REQ-GEO-04 Instrumentation: Initial state visibility
        logger.debug("Safety Check: Starting ray-cast for point [{}, {}] against polygon with {} vertices.",
                point.getLng(), point.getLat(), vertices.size());

        for (int i = 0; i < vertices.size() - 1; i++) {
            Position v1 = vertices.get(i);
            Position v2 = vertices.get(i + 1);

            boolean condY = (v1.getLat() > point.getLat()) != (v2.getLat() > point.getLat());
            if (condY) {
                double slope = (v2.getLng() - v1.getLng()) *
                        (point.getLat() - v1.getLat()) /
                        (v2.getLat() - v1.getLat()) + v1.getLng();
                if (point.getLng() < slope) {
                    crossings++;
                    // REQ-GEO-04 Instrumentation: Track internal crossing logic
                    logger.debug("Boundary Hit: Ray intersected edge between vertex {} and {}. Current crossing count: {}", i, i+1, crossings);
                }
            }
        }

        boolean isInside = crossings % 2 == 1;
        // REQ-GEO-04 Instrumentation: Final result visibility
        logger.info("Safety Result: Point is {} restricted area (Total Crossings: {}).",
                isInside ? "INSIDE" : "OUTSIDE", crossings);

        return isInside;
    }

    public Position makePos(double lng, double lat) {
        Position p = new Position();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }

    private void validateCoordinate(double lat, double lng) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException();
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException();
        }
    }

    private void validateAngle(double angle) {
        if (angle < 0 || angle >= 360) {
            throw new IllegalArgumentException();
        }

        double quotient = angle / COMPASS_DEGREE;
        if (Math.abs(quotient - Math.round(quotient)) > EPSILON) {
            throw new IllegalArgumentException();
        }
    }
}
