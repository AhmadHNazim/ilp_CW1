package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.Service.DroneService;
import uk.ac.ed.acp.cw2.Service.IlpClient;
import uk.ac.ed.acp.cw2.dto.Drone;
import uk.ac.ed.acp.cw2.dto.DroneCapability;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.when;

@SpringBootTest
class DroneServiceTests {

    // Inject the Real Service (The System Under Test)
    @Autowired
    private DroneService droneService;

    // Mock the Client (The External Dependency)
    // This stops the test from hitting the real internet/Azure
    @MockBean
    private IlpClient ilpClient;

    // Helper to create dummy drone objects quickly
    private Drone createMockDrone(int id, boolean cooling) {
        Drone d = new Drone();
        d.setId(id);

        DroneCapability cap = new DroneCapability();
        cap.setCooling(cooling);
        d.setCapability(cap);

        return d;
    }

    @Test
    void testGetDronesWithCoolingTrue() {
        // DATA: Create fake drones
        Drone coolDrone = createMockDrone(1, true);
        Drone hotDrone = createMockDrone(2, false);

        // STUB: "When asked for all drones, return this mixed list"
        // Note: getAllDrones returns an array [] in Service code, so we mock an array.
        when(ilpClient.getAllDrones()).thenReturn(new Drone[]{coolDrone, hotDrone});

        // STUB: "When asked for specific details, return the object"
        // (Used inside the loop in your original test logic)
        when(ilpClient.getDroneById(1)).thenReturn(coolDrone);

        // ACTION
        List<Integer> coolingDrones = droneService.getDronesWithCooling(true);

        // ASSERT
        assertNotNull(coolingDrones);
        assertFalse(coolingDrones.isEmpty());
        assertTrue(coolingDrones.contains(1)); // ID 1 has cooling
        assertFalse(coolingDrones.contains(2)); // ID 2 does not

        for (Integer id : coolingDrones) {
            Drone d = droneService.getDroneDetails(id);
            assertTrue(d.getCapability().isCooling());
        }
    }

    @Test
    void testGetDronesWithCoolingFalse() {
        // DATA
        Drone coolDrone = createMockDrone(1, true);
        Drone hotDrone = createMockDrone(2, false);

        // STUB
        when(ilpClient.getAllDrones()).thenReturn(new Drone[]{coolDrone, hotDrone});
        when(ilpClient.getDroneById(2)).thenReturn(hotDrone);

        // ACTION
        List<Integer> nonCoolingDrones = droneService.getDronesWithCooling(false);

        // ASSERT
        assertNotNull(nonCoolingDrones);
        assertFalse(nonCoolingDrones.isEmpty());
        assertTrue(nonCoolingDrones.contains(2)); // ID 2 has NO cooling
        assertFalse(nonCoolingDrones.contains(1)); // ID 1 has cooling

        for (Integer id : nonCoolingDrones) {
            Drone d = droneService.getDroneDetails(id);
            assertFalse(d.getCapability().isCooling());
        }
    }

    @Test
    void testGetDroneByIdExists() {
        // DATA
        Drone expectedDrone = createMockDrone(10, true);

        // STUB
        when(ilpClient.getDroneById(10)).thenReturn(expectedDrone);

        // ACTION
        Drone drone = droneService.getDroneDetails(10);

        // ASSERT
        assertNotNull(drone);
        assertEquals(10, drone.getId());
    }

    @Test
    void testGetDroneByIdNotFound() {
        // STUB: Return null (Client usually returns null on 404, or throws exception)
        // Service code checks "if (drone==null) throw ResponseStatusException"
        when(ilpClient.getDroneById(999)).thenReturn(null);

        // ASSERT
        // Use ResponseStatusException.class because that is exactly what Service throws
        assertThrows(ResponseStatusException.class, () -> droneService.getDroneDetails(999));
    }
}