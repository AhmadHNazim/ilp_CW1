package uk.ac.ed.acp.cw2.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw2.dto.*;

import java.util.Arrays;


@Service
public class IlpClient {

    private final String ilpEndpoint = System.getenv().getOrDefault(
            "ILP_ENDPOINT",
            "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net"
    );

    private final RestTemplate restTemplate = new RestTemplate();

    public Drone[] getAllDrones() {
        return restTemplate.getForObject(ilpEndpoint + "/drones", Drone[].class);
    }

    public Drone getDroneById(int id) {
        return Arrays.stream(getAllDrones()).filter(drone -> drone.getId() == id).findFirst().orElse(null);
    }

    // Fetch restricted areas
    public RestrictedArea[] getRestrictedAreas() {
        return restTemplate.getForObject(
                ilpEndpoint + "/restricted-areas",
                RestrictedArea[].class
        );
    }

    // Fetch service points
    public ServicePoint[] getAllServicePoints() {
        return restTemplate.getForObject(
                ilpEndpoint + "/service-points",
                ServicePoint[].class
        );
    }

    public ServicePoint getServicePointById(int id) {
        return Arrays.stream(getAllServicePoints()).filter(servicePoint -> servicePoint.getId() == id).findFirst().orElse(null);
    }

    // Fetch drones for service points
    public DroneForServicePoint[] getAllDronesForServicePoints() {
        return restTemplate.getForObject(
                ilpEndpoint + "/drones-for-service-points",
                DroneForServicePoint[].class
        );
    }

    public MedDispatchRec[] getAllDispatches() {
        return restTemplate.getForObject(
                ilpEndpoint + "/api/v1/dispatches",
                MedDispatchRec[].class
        );
    }

    public MedDispatchRec getDispatchById(int id) {
        return restTemplate.getForObject(
                ilpEndpoint + "/api/v1/dispatches/" + id,
                MedDispatchRec.class
        );
    }
}