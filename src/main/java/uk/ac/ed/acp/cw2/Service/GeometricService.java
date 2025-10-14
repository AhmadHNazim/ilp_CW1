package uk.ac.ed.acp.cw2.Service;

import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.*;

import java.util.List;
import java.util.Objects;

@Service
public class GeometricService {

    private static final double STEP_SIZE = 0.00015;
    private static final double COMPASS_DEGREE = 22.5;
    private static final double EPSILON = 1e-9;

    public double calculateDistance(DistanceRequest request) {
        double dx = request.getPosition1().getLng() - request.getPosition2().getLng();
        double dy = request.getPosition1().getLat() - request.getPosition2().getLat();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isCloseTo(DistanceRequest request) {
        return calculateDistance(request) < STEP_SIZE;
    }

    public Position nextPosition(NextPositionRequest request) {
        Position start = request.getStart();
        double angle = request.getAngle();

        double quotient = angle / COMPASS_DEGREE;
        if (start == null || (Math.abs(quotient - Math.round(quotient)) > EPSILON)) {
            throw new IllegalArgumentException();
        }

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

        List<Position> vertices = region.getVertices();
        Position first = vertices.getFirst();
        Position last = vertices.getLast();

        if (!Objects.equals(first.getLat(), last.getLat()) || !Objects.equals(first.getLng(), last.getLng())) {
            throw new IllegalArgumentException();
        }

        return isPointInPolygon(point, vertices);
    }

    private boolean isPointInPolygon(Position point, List<Position> vertices) {
        int crossings = 0;
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
                }
            }
        }
        return crossings % 2 == 1;
    }

    public Position makePos(double lng, double lat) {
        Position p = new Position();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }
}
