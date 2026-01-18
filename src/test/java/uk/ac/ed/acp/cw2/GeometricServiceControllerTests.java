package uk.ac.ed.acp.cw2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.dto.Position;

import uk.ac.ed.acp.cw2.Service.GeometricService;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GeometricServiceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeometricService geometricService;

    @Test
    public void testUidEndpoint() throws Exception {
        // No stubbing needed as this likely doesn't call the geometric service
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2556257"));
    }

    @Test
    public void testValidDistanceTo() throws Exception {
        // Create positions
        Position p1 = new Position();
        p1.setLat(55.946233);
        p1.setLng(-3.192473);

        Position p2 = new Position();
        p2.setLat(55.942617);
        p2.setLng(-3.192473);

        DistanceRequest request = new DistanceRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        // calculate Euclidean distance (Same logic as original test)
        double dx = p1.getLng() - p2.getLng();
        double dy = p1.getLat() - p2.getLat();
        double expectedDistance = Math.sqrt(dx * dx + dy * dy);

        // STUB: Tell Mock to return the calculated value
        when(geometricService.calculateDistance(any(DistanceRequest.class)))
                .thenReturn(expectedDistance);

        // Perform POST request and check response
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedDistance)));
    }

    @Test
    public void testInvalidDistanceTo() throws Exception {
        String invalidJson = "{ \"position1\": { \"lat\": 55.946233 } }"; // missing lng and position2

        when(geometricService.calculateDistance(argThat(req -> req.getPosition2() == null)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing position2"));

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_True() throws Exception {
        // Points very close to each other
        Position p1 = new Position();
        p1.setLat(55.946233);
        p1.setLng(-3.192473);

        Position p2 = new Position();
        p2.setLat(55.946300);
        p2.setLng(-3.192400);

        DistanceRequest request = new DistanceRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        // STUB: Return true
        when(geometricService.isCloseTo(any(DistanceRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsCloseTo_False() throws Exception {
        // Points further apart
        Position p1 = new Position();
        p1.setLat(55.946233);
        p1.setLng(-3.192473);

        Position p2 = new Position();
        p2.setLat(55.942617);
        p2.setLng(-3.192473);

        DistanceRequest request = new DistanceRequest();
        request.setPosition1(p1);
        request.setPosition2(p2);

        // STUB: Return false
        when(geometricService.isCloseTo(any(DistanceRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsCloseTo_InvalidJson() throws Exception {
        String invalidJson = "{ \"position1\": { \"lat\": 55.946233 } }"; // Missing fields

        when(geometricService.isCloseTo(argThat(req -> req.getPosition2() == null)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing position2"));

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testNextPosition_Valid() throws Exception {
        String json = """
        {
            "start": {"lng": -3.192473, "lat": 55.946233},
            "angle": 90
        }
        """;

        Position nextPos = new Position();
        nextPos.setLng(-3.192473);
        nextPos.setLat(55.946233 + 0.00015); // Adding step size

        // STUB: Return the calculated position object
        when(geometricService.nextPosition(any(NextPositionRequest.class))).thenReturn(nextPos);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(-3.192473))
                .andExpect(jsonPath("$.lat").value(closeTo(55.946233 + 0.00015, 1e-7)));
    }

    @Test
    public void testNextPosition_InvalidAngle() throws Exception {
        String json = """
        {
            "start": {"lng": -3.192473, "lat": 55.946233},
            "angle": 100
        }
        """;

        // STUB: Simulate the service throwing IllegalArgumentException for bad angle
        when(geometricService.nextPosition(any(NextPositionRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid angle"));

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testNextPosition_InvalidJson() throws Exception {
        String json = "{ \"angle\": 45 }"; // Missing start object

        when(geometricService.nextPosition(argThat(req -> req.getStart() == null)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_PointInside() throws Exception {
        String json = """
        {
          "position": { "lng": -3.188, "lat": 55.944 },
          "region": {
            "name": "central",
            "vertices": [
              {"lng": -3.192473, "lat": 55.946233},
              {"lng": -3.192473, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.946233},
              {"lng": -3.192473, "lat": 55.946233}
            ]
          }
        }
        """;

        // STUB: Return true
        when(geometricService.isInRegion(any(IsInRegionRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsInRegion_PointOutside() throws Exception {
        String json = """
        {
          "position": { "lng": -3.200, "lat": 55.950 },
          "region": {
            "name": "central",
            "vertices": [
              {"lng": -3.192473, "lat": 55.946233},
              {"lng": -3.192473, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.946233},
              {"lng": -3.192473, "lat": 55.946233}
            ]
          }
        }
        """;

        // STUB: Return false
        when(geometricService.isInRegion(any(IsInRegionRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_InvalidPolygon_NotClosed() throws Exception {
        String json = """
        {
          "position": { "lng": -3.188, "lat": 55.944 },
          "region": {
            "name": "central",
            "vertices": [
              {"lng": -3.192473, "lat": 55.946233},
              {"lng": -3.192473, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.942617},
              {"lng": -3.184319, "lat": 55.946233}
            ]
          }
        }
        """;

        // STUB: Simulate exception for invalid geometry (not closed loop)
        when(geometricService.isInRegion(any(IsInRegionRequest.class)))
                .thenThrow(new IllegalArgumentException("Polygon not closed"));

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_InvalidJson_MissingFields() throws Exception {
        String json = """
        {
          "position": { "lat": 55.944 },
          "region": null
        }
        """;

        when(geometricService.isInRegion(argThat(req -> req.getRegion() == null)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing region"));

        // No stub needed; Fails at controller level
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}