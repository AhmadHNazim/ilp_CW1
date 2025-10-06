package uk.ac.ed.acp.cw2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.acp.cw2.controller.ServiceController;
import uk.ac.ed.acp.cw2.dto.DistanceRequest;
import uk.ac.ed.acp.cw2.dto.Position;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUidEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2556257"));
    }

    @Test
    public void testValidDistanceRequestDynamic() throws Exception {
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

        // calculate Euclidean distance
        double dx = p1.getLng() - p2.getLng();
        double dy = p1.getLat() - p2.getLat();
        double expectedDistance = Math.sqrt(dx * dx + dy * dy);

        // Perform POST request and check response
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedDistance)));
    }

    @Test
    public void testInvalidDistanceRequest() throws Exception {
        String invalidJson = "{ \"position1\": { \"lat\": 55.946233 } }"; // missing lng and position2

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

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsCloseTo_InvalidJson() throws Exception {
        String invalidJson = "{ \"position1\": { \"lat\": 55.946233 } }"; // Missing fields

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

}
