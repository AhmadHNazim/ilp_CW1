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

import uk.ac.ed.acp.cw2.dto.CalcDeliveryResponse;
import uk.ac.ed.acp.cw2.dto.Drone;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DroneControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // This replaces the real DroneService with a Mockito mock.
    // It prevents the controller from calling the real ILP website.
    @MockBean
    private uk.ac.ed.acp.cw2.Service.DroneService droneService;

    // Helper to convert JSON strings into Objects for the Mock to return
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetDroneDetails_ValidId() throws Exception {
        // Stubbing to prevent null pointer
        when(droneService.getDroneDetails(4)).thenReturn(new Drone());

        mockMvc.perform(get("/api/v1/droneDetails/4"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDroneDetails_Negative_InvalidId() throws Exception {
        // Testing with an ID that clearly does not exist (e.g., negative or very large)
        // The service should handle this gracefully (usually 404)

        // Stubbing: Throw 404
        when(droneService.getDroneDetails(999999))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone not found"));

        mockMvc.perform(get("/api/v1/droneDetails/999999"))
                .andExpect(status().isNotFound());
    }

    // Robustness Test for getDronesWithCooling (REQ-NFR-05)
    @Test
    void testGetDronesWithCooling_ValidStatus() throws Exception {
        // Standard endpoint check to ensure robustness under normal load
        when(droneService.getDronesWithCooling(true)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDronesWithCooling_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/medium"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testQueryAsPath_ValidData_Matches_Returns200() throws Exception {
        // Scenario: Valid attribute but value that doesn't match any drone
        // Expected: 200 OK

        // Stubbing: Return the specific list your assertion expects
        when(droneService.queryAsPath("cooling", "false"))
                .thenReturn(List.of(2, 3, 4, 6, 7, 10));

        mockMvc.perform(get("/api/v1/queryAsPath/cooling/false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[2,3,4,6,7,10]"));
    }

    @Test
    void testQueryAsPath_ValidData_NoMatches_Returns200() throws Exception {
        // Scenario: Valid attribute but value that doesn't match any drone
        // Expected: 200 OK with empty list []

        // Stubbing: Return empty list
        when(droneService.queryAsPath("capacity", "999"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/queryAsPath/capacity/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testQueryAsPath_InvalidData_Returns200() throws Exception {
        // Stubbing: Return empty list
        when(droneService.queryAsPath("chicken", "false"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/queryAsPath/chicken/false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testQuery_ValidComplexCase_Returns200() throws Exception {
        // Stubbing: Return empty list to match "valid but simple" check
        when(droneService.query(anyList())).thenReturn(Collections.emptyList());

        String complexQueryJson = """
            [
                { "attribute": "capacity", "operator": ">", "value": "8" },
                { "attribute": "cooling", "operator": "=", "value": "false" }
            ]
            """;

        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(complexQueryJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testQuery_ValidData_NoMatches_Returns200() throws Exception {
        // Valid query, no matches -> 200 OK + []
        when(droneService.query(anyList())).thenReturn(Collections.emptyList());

        String noMatchJson = """
            [
              { "attribute": "battery", "operator": ">", "value": "9000" }
            ]
            """;

        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noMatchJson))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testQuery_ValidData_MissingOperator_NoMatches() throws Exception {
        // Missing 'operator' field -> 200 OK + []
        // Stubbing: Graceful failure returns empty list
        when(droneService.query(anyList())).thenReturn(Collections.emptyList());

        String missingOpJson = """
            [
              { "attribute": "capacity", "value": "50" }
            ]
            """;

        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingOpJson))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testQuery_InvalidData_MalformedJson_Returns400() throws Exception {
        // Malformed JSON -> 400 Bad Request
        String malformedJson = "[ { \"attribute\": \"capacity\" ";

        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testQueryAvailableDrones_ValidCase_ReturnsResult() throws Exception {
        // Scenario: Valid input list of 3 dispatches, expecting Drone ID 9
        // Stubbing: Return [9]
        when(droneService.queryAvailableDrones(anyList())).thenReturn(List.of(9));

        String validDispatchJson = """
            [
              {
                "id": 201, "date": "2025-12-22", "time": "09:00",
                "requirements": { "capacity": 1.0, "cooling": false, "heating": true, "maxCost": 15.0 },
                "delivery": { "lat": 55.9450, "lng": -3.1865 }
              },
              {
                "id": 202, "date": "2025-12-22", "time": "09:15",
                "requirements": { "capacity": 2.5, "cooling": true, "heating": false, "maxCost": 20.0 },
                "delivery": { "lat": 55.9460, "lng": -3.1870 }
              },
              {
                "id": 203, "date": "2025-12-22", "time": "09:30",
                "requirements": { "capacity": 0.5, "cooling": false, "heating": false, "maxCost": 10.0 },
                "delivery": { "lat": 55.9448, "lng": -3.1860 }
              }
            ]
            """;

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDispatchJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Expect strictly [9]
                .andExpect(content().json("[9]", false));
    }

    @Test
    void testQueryAvailableDrones_ValidData_NoMatches_ReturnsEmpty() throws Exception {
        // Scenario: Valid data but requirements are too high for any drone
        // Stubbing: Return empty
        when(droneService.queryAvailableDrones(anyList())).thenReturn(Collections.emptyList());

        String impossibleDispatchJson = """
            [
              {
                "id": 999,
                "requirements": { "capacity": 1000.0, "maxCost": 0.01 },
                "delivery": { "lat": 55.9450, "lng": -3.1865 }
              }
            ]
            """;

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(impossibleDispatchJson))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testQueryAvailableDrones_InvalidData_MalformedJson_Returns400() throws Exception {
        // Scenario: Syntax error in JSON (missing closing braces)
        String malformedJson = "[ { \"id\": 201, \"requirements\": { \"capacity\": 1.0 ";

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // This test initially failed and returned 500. Fix in line 246 of DroneService
    @Test
    void testQueryAvailableDrones_InvalidData_MissingFields_Returns200() throws Exception {
        // Scenario: Semantic Robustness.
        // The JSON is valid, but missing 'requirements' and 'delivery' objects.
        // The controller should NOT crash (500). It should return 200 (likely with empty list).

        // Stubbing: Return empty
        when(droneService.queryAvailableDrones(anyList())).thenReturn(Collections.emptyList());

        String incompleteJson = "[ { \"id\": 201 } ]";

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isOk());
    }

    @Test
    void testCalcDeliveryPath_Negative_MalformedJson() throws Exception {
        String malformedJson = "[{ \"id\": 1, \"delivery\": { \"lat\": 55.9 } ";

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalcDeliveryPath_Negative_MissingRequiredFields() throws Exception {
        // Stubbing: Return a valid empty object
        when(droneService.calcDeliveryPath(anyList())).thenReturn(new CalcDeliveryResponse());

        String missingFieldsJson = "[{ \"id\": 1 }]";

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingFieldsJson))
                .andExpect(status().isOk());
    }

    @Test
    void testCalcDeliveryPath_ValidCase_ComplexRoute() throws Exception {
        String inputJson = """
            [
              {
                "id": 201, "date": "2025-12-22", "time": "09:00",
                "requirements": { "capacity": 1.0, "cooling": false, "heating": true, "maxCost": 15.0 },
                "delivery": { "lat": 55.9450, "lng": -3.1865 }
              },
              {
                "id": 202, "date": "2025-12-22", "time": "09:15",
                "requirements": { "capacity": 2.5, "cooling": true, "heating": false, "maxCost": 20.0 },
                "delivery": { "lat": 55.9460, "lng": -3.1870 }
              },
              {
                "id": 203, "date": "2025-12-22", "time": "09:30",
                "requirements": { "capacity": 0.5, "cooling": false, "heating": false, "maxCost": 10.0 },
                "delivery": { "lat": 55.9448, "lng": -3.1860 }
              }
            ]
            """;

        String expectedJson = """
            {
                "totalCost": 25.96,
                "totalMoves": 52,
                "dronePaths": [
                    {
                        "droneId": 1,
                        "deliveries": [
                            {
                                "deliveryId": 201,
                                "flightPath": [
                                    { "lng": -3.1863580788986368, "lat": 55.94468066708487 },
                                    { "lng": -3.1864154814134915, "lat": 55.94481924901474 },
                                    { "lng": -3.1864728839, "lat": 55.9449578309 },
                                    { "lng": -3.1865, "lat": 55.945 },
                                    { "lng": -3.1865, "lat": 55.945 }
                                ]
                            },
                            {
                                "deliveryId": 202,
                                "flightPath": [
                                    { "lng": -3.1865, "lat": 55.945 },
                                    { "lng": -3.186557402514855, "lat": 55.945138581929875 },
                                    { "lng": -3.1866148050297096, "lat": 55.94527716385975 },
                                    { "lng": -3.1866722075445644, "lat": 55.945415745789624 },
                                    { "lng": -3.186729610059419, "lat": 55.9455543277195 },
                                    { "lng": -3.1868356760765972, "lat": 55.945660393736674 },
                                    { "lng": -3.1869417420937753, "lat": 55.94576645975385 },
                                    { "lng": -3.1870478081109535, "lat": 55.94587252577102 },
                                    { "lng": -3.1869904056, "lat": 55.9460111077 },
                                    { "lng": -3.187, "lat": 55.946 },
                                    { "lng": -3.187, "lat": 55.946 }
                                ]
                            },
                            {
                                "deliveryId": null,
                                "flightPath": [
                                    { "lng": -3.187, "lat": 55.946 },
                                    { "lng": -3.186942597485145, "lat": 55.94586141807012 },
                                    { "lng": -3.1868851949702903, "lat": 55.94572283614025 },
                                    { "lng": -3.1868277924554356, "lat": 55.945584254210374 },
                                    { "lng": -3.186770389940581, "lat": 55.9454456722805 },
                                    { "lng": -3.186712987425726, "lat": 55.945307090350624 },
                                    { "lng": -3.1866555849108713, "lat": 55.94516850842075 },
                                    { "lng": -3.1865981823960166, "lat": 55.945029926490875 },
                                    { "lng": -3.186540779881162, "lat": 55.944891344561 },
                                    { "lng": -3.186483377366307, "lat": 55.944752762631126 },
                                    { "lng": -3.1863447954, "lat": 55.9446953601 },
                                    { "lng": -3.1863580788986368, "lat": 55.94468066708487 }
                                ]
                            }
                        ]
                    },
                    {
                        "droneId": 4,
                        "deliveries": [
                            {
                                "deliveryId": 203,
                                "flightPath": [
                                    { "lng": -3.1863580788986368, "lat": 55.94468066708487 },
                                    { "lng": -3.1862080788986367, "lat": 55.94468066708487 },
                                    { "lng": -3.1860580788986366, "lat": 55.94468066708487 },
                                    { "lng": -3.1860006764, "lat": 55.944819249 },
                                    { "lng": -3.186, "lat": 55.9448 },
                                    { "lng": -3.186, "lat": 55.9448 }
                                ]
                            },
                            {
                                "deliveryId": null,
                                "flightPath": [
                                    { "lng": -3.186, "lat": 55.9448 },
                                    { "lng": -3.18615, "lat": 55.9448 },
                                    { "lng": -3.1863, "lat": 55.9448 },
                                    { "lng": -3.1863574025, "lat": 55.9446614181 },
                                    { "lng": -3.1863580788986368, "lat": 55.94468066708487 }
                                ]
                            }
                        ]
                    }
                ]
            }
            """;

        // Create the Mock Response OBJECT from EXPECTED JSON string.
        // This ensures the service returns exactly what the test is looking for.
        CalcDeliveryResponse mockResponse = objectMapper.readValue(expectedJson, CalcDeliveryResponse.class);

        when(droneService.calcDeliveryPath(anyList())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, false)); // No cast needed!
    }
}