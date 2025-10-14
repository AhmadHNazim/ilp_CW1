package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.Service.GeometricService;
import uk.ac.ed.acp.cw2.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeometricServiceTests {

    private final GeometricService geometricService = new GeometricService();

    @Test
    void testCalculateDistance_Valid() {
        Position p1 = geometricService.makePos(-3.192473, 55.946233);

        Position p2 = geometricService.makePos(-3.192473, 55.942617);

        DistanceRequest req = new DistanceRequest();
        req.setPosition1(p1);
        req.setPosition2(p2);

        double result = geometricService.calculateDistance(req);
        assertEquals(0.003616, result, 1e-6);
    }

    @Test
    void testIsCloseTo_True() {
        Position p1 = geometricService.makePos(0.0, 0.0);

        Position p2 = geometricService.makePos(0.0001, 0.0001);

        DistanceRequest req = new DistanceRequest();
        req.setPosition1(p1);
        req.setPosition2(p2);

        assertTrue(geometricService.isCloseTo(req));
    }

    @Test
    void testIsCloseTo_False() {
        Position p1 = geometricService.makePos(0.0, 0.0);

        Position p2 = geometricService.makePos(0.001, 0.001);

        DistanceRequest req = new DistanceRequest();
        req.setPosition1(p1);
        req.setPosition2(p2);

        assertFalse(geometricService.isCloseTo(req));
    }

    @Test
    void testNextPosition_ValidAngle() {
        Position start = geometricService.makePos(-3.192473, 55.946233);

        NextPositionRequest req = new NextPositionRequest();
        req.setStart(start);
        req.setAngle(45.0);

        Position result = geometricService.nextPosition(req);
        assertNotNull(result);
        assertNotEquals(start.getLat(), result.getLat());
        assertNotEquals(start.getLng(), result.getLng());
    }

    @Test
    void testNextPosition_InvalidAngle() {
        Position start = geometricService.makePos(-3.192473, 55.946233);

        NextPositionRequest req = new NextPositionRequest();
        req.setStart(start);
        req.setAngle(46.0);

        assertThrows(IllegalArgumentException.class, () -> geometricService.nextPosition(req));
    }

    @Test
    void testIsInRegion_ValidInside() {
        Position point = geometricService.makePos(-3.188, 55.944);

        Region region = new Region();
        region.setName("central");
        region.setVertices(List.of(
                geometricService.makePos(-3.192473, 55.946233),
                geometricService.makePos(-3.192473, 55.942617),
                geometricService.makePos(-3.184319, 55.942617),
                geometricService.makePos(-3.184319, 55.946233),
                geometricService.makePos(-3.192473, 55.946233)
        ));

        IsInRegionRequest req = new IsInRegionRequest();
        req.setPosition(point);
        req.setRegion(region);

        assertTrue(geometricService.isInRegion(req));
    }

    @Test
    void testIsInRegion_InvalidUnclosedPolygon() {
        Position point = geometricService.makePos(-3.188, 55.944);

        Region region = new Region();
        region.setName("central");
        region.setVertices(List.of(
                geometricService.makePos(-3.192473, 55.946233),
                geometricService.makePos(-3.192473, 55.942617),
                geometricService.makePos(-3.184319, 55.942617),
                geometricService.makePos(-3.184319, 55.946233)
                // missing closing vertex
        ));

        IsInRegionRequest req = new IsInRegionRequest();
        req.setPosition(point);
        req.setRegion(region);

        assertThrows(IllegalArgumentException.class, () -> geometricService.isInRegion(req));
    }
}