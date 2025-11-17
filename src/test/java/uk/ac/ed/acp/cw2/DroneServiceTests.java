package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.Service.DroneService;
import uk.ac.ed.acp.cw2.Service.GeometricService;
import uk.ac.ed.acp.cw2.Service.IlpClient;
import uk.ac.ed.acp.cw2.dto.Drone;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DroneServiceTests {

    private DroneService droneService;

    @BeforeEach
    void setUp() {
        droneService = new DroneService(new IlpClient(), new GeometricService());
    }

    @Test
    void testGetDronesWithCoolingTrue() {
        List<Integer> coolingDrones = droneService.getDronesWithCooling(true);

        assertNotNull(coolingDrones);
        assertFalse(coolingDrones.isEmpty());

        for (Integer id : coolingDrones) {
            Drone d = droneService.getDroneDetails(id);
            assertTrue(d.getCapability().isCooling());
        }
    }

    @Test
    void testGetDronesWithCoolingFalse() {
        List<Integer> nonCoolingDrones = droneService.getDronesWithCooling(false);

        assertNotNull(nonCoolingDrones);
        assertFalse(nonCoolingDrones.isEmpty());

        // Optional: check all returned drones actually do NOT have cooling
        for (Integer id : nonCoolingDrones) {
            Drone d = droneService.getDroneDetails(id);
            assertFalse(d.getCapability().isCooling());
        }
    }

    @Test
    void testGetDroneByIdExists() {
        Drone drone = droneService.getDroneDetails(1);
        assertNotNull(drone);
        assertEquals(1, drone.getId());
    }

    @Test
    void testGetDroneByIdNotFound() {
        assertThrows(RuntimeException.class, () -> droneService.getDroneDetails(999));
    }
}
