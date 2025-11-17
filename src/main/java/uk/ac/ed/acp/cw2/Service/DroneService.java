package uk.ac.ed.acp.cw2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DroneService {

    private final IlpClient ilpClient;
    private final GeometricService geometricService;

    public List<Integer> getDronesWithCooling(boolean state) {
        return Arrays.stream(ilpClient.getAllDrones())
                .filter(drone -> drone.getCapability().isCooling() == state)
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    public Drone getDroneDetails(int id) {
        Drone drone = ilpClient.getDroneById(id);

        if (drone==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return drone;
    }

    public List<Integer> queryAsPath(String attributeName, String attributeValue) {
        List<Drone> drones = Arrays.asList(ilpClient.getAllDrones());

        return drones.stream()
                .filter(drone -> matchesAttribute(drone, attributeName, attributeValue))
                .map(Drone::getId)
                .toList();
    }

    private boolean matchesAttribute(Drone drone, String attributeName, String attributeValue) {
        switch (attributeName.toLowerCase()) {
            case "id":
                try {
                    return drone.getId() == Integer.parseInt(attributeValue);
                } catch (NumberFormatException e) {
                    return false;
                }

            case "name":
                return drone.getName().equalsIgnoreCase(attributeValue);

            case "capacity":
                try {
                    double val = Double.parseDouble(attributeValue);
                    return drone.getCapability().getCapacity() == val;
                } catch (NumberFormatException e) {
                    return false;
                }

            case "cooling":
                return Boolean.toString(drone.getCapability().isCooling()).equalsIgnoreCase(attributeValue);

            case "heating":
                return Boolean.toString(drone.getCapability().isHeating()).equalsIgnoreCase(attributeValue);

            case "costpermove":
                try {
                    double val = Double.parseDouble(attributeValue);
                    return drone.getCapability().getCostPerMove() == val;
                } catch (NumberFormatException e) {
                    return false;
                }

            case "costinitial":
                try {
                    double val = Double.parseDouble(attributeValue);
                    return drone.getCapability().getCostInitial() == val;
                } catch (NumberFormatException e) {
                    return false;
                }

            case "costfinal":
                try {
                    double val = Double.parseDouble(attributeValue);
                    return drone.getCapability().getCostFinal() == val;
                } catch (NumberFormatException e) {
                    return false;
                }

            case "maxmoves":
                try {
                    return drone.getCapability().getMaxMoves() == Integer.parseInt(attributeValue);
                } catch (NumberFormatException e) {
                    return false;
                }

            default:
                return false; // unknown attribute
        }
    }

    public List<Integer> query(List<QueryAttribute> queries) {
        List<Drone> drones = Arrays.asList(ilpClient.getAllDrones());

        return drones.stream()
                .filter(drone -> queriesMatchDrone(drone, queries))
                .map(Drone::getId)
                .toList();
    }

    private boolean queriesMatchDrone(Drone drone, List<QueryAttribute> queries) {
        for (QueryAttribute query : queries) {
            if (!matchesAttribute2(drone, query)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAttribute2(Drone drone, QueryAttribute query) {
        String attr = query.getAttribute().toLowerCase();
        String op = query.getOperator();
        String valueStr = query.getValue();

        try {
            switch (attr) {
                case "id": {
                    int val = Integer.parseInt(valueStr);
                    return compareInt(drone.getId(), op, val);
                }
                case "capacity": {
                    double val = Double.parseDouble(valueStr);
                    return compareDouble(drone.getCapability().getCapacity(), op, val);
                }
                case "maxmoves": {
                    int val = Integer.parseInt(valueStr);
                    return compareInt(drone.getCapability().getMaxMoves(), op, val);
                }
                case "costpermove": {
                    double val = Double.parseDouble(valueStr);
                    return compareDouble(drone.getCapability().getCostPerMove(), op, val);
                }
                case "costinitial": {
                    double val = Double.parseDouble(valueStr);
                    return compareDouble(drone.getCapability().getCostInitial(), op, val);
                }
                case "costfinal": {
                    double val = Double.parseDouble(valueStr);
                    return compareDouble(drone.getCapability().getCostFinal(), op, val);
                }
                case "cooling": {
                    boolean val = Boolean.parseBoolean(valueStr);
                    return compareBoolean(drone.getCapability().isCooling(), op, val);
                }
                case "heating": {
                    boolean val = Boolean.parseBoolean(valueStr);
                    return compareBoolean(drone.getCapability().isHeating(), op, val);
                }
                case "name": {
                    return compareString(drone.getName(), op, valueStr);
                }
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false; // invalid input value
        }
    }

    // Helper methods
    private boolean compareInt(int droneVal, String op, int val) {
        return switch (op) {
            case "=" -> droneVal == val;
            case "!=" -> droneVal != val;
            case "<" -> droneVal < val;
            case ">" -> droneVal > val;
            default -> false;
        };
    }

    private boolean compareDouble(double droneVal, String op, double val) {
        return switch (op) {
            case "=" -> droneVal == val;
            case "!=" -> droneVal != val;
            case "<" -> droneVal < val;
            case ">" -> droneVal > val;
            default -> false;
        };
    }

    private boolean compareBoolean(boolean droneVal, String op, boolean val) {
        return switch (op) {
            case "=" -> droneVal == val;
            case "!=" -> droneVal != val;
            default -> false;
        };
    }

    private boolean compareString(String droneVal, String op, String val) {
        return switch (op) {
            case "=" -> droneVal.equals(val);
            case "!=" -> !droneVal.equals(val);
            default -> false;
        };
    }

    public List<Integer> queryAvailableDrones(List<MedDispatchRec> dispatchRequests) {
        List<Drone> drones = Arrays.asList(ilpClient.getAllDrones());
        List<DroneForServicePoint> dronesForServicePoints = Arrays.asList(ilpClient.getAllDronesForServicePoints());

        return drones.stream()
                .filter(drone -> canServeAllDispatches(drone, dispatchRequests, dronesForServicePoints))
                .map(Drone::getId)
                .toList();
    }

    private boolean canServeAllDispatches(Drone drone, List<MedDispatchRec> dispatchRequests, List<DroneForServicePoint> droneServicePoints) {
        for (MedDispatchRec request : dispatchRequests) {
            MedDispatchRequirements requirements = request.getRequirements();

            // Capability checks
            if (drone.getCapability().getCapacity() < requirements.getCapacity()) return false;
            if (Boolean.TRUE.equals(requirements.getCooling()) && !drone.getCapability().isCooling()) return false;
            if (Boolean.TRUE.equals(requirements.getHeating()) && !drone.getCapability().isHeating()) return false;

            // Availability and cost checks
            if (!isDroneAvailableForDispatch(drone, request, droneServicePoints)) return false;
        }
        return true;
    }

    private boolean isDroneAvailableForDispatch(Drone drone, MedDispatchRec request, List<DroneForServicePoint> droneServicePoints) {
        LocalDate dispatchDate = LocalDate.parse(request.getDate());
        LocalTime dispatchTime = LocalTime.parse(request.getTime());

        for (DroneForServicePoint sp : droneServicePoints) {
            for (DroneForServicePoint.DroneAvailability availability : sp.getDrones()) {
                if (availability.getId() != drone.getId()) continue;

                // Check if the drone is available at this service point at the given date/time
                boolean isAvailableNow = availability.getAvailability().stream()
                        .anyMatch(slot ->
                                slot.getDayOfWeek().equalsIgnoreCase(dispatchDate.getDayOfWeek().toString()) &&
                                        !dispatchTime.isBefore(LocalTime.parse(slot.getFrom())) &&
                                        !dispatchTime.isAfter(LocalTime.parse(slot.getUntil()))
                        );

                if (!isAvailableNow) continue;

                // Estimate cost
                int servicePointID = sp.getServicePointId();
                Position servicePointPos = ilpClient.getServicePointById(servicePointID).getLocation();
                Position deliveryPos = request.getDelivery();

                DistanceRequest distanceRequest = new DistanceRequest();
                distanceRequest.setPosition1(servicePointPos);
                distanceRequest.setPosition2(deliveryPos);
                double distance = geometricService.calculateDistance(distanceRequest);

                double estimatedCost = drone.getCapability().getCostInitial()
                        + drone.getCapability().getCostFinal()
                        + distance * drone.getCapability().getCostPerMove();

                if (request.getRequirements().getMaxCost() != null &&
                        estimatedCost > request.getRequirements().getMaxCost()) continue;

                // Drone works for this dispatch at this service point
                return true;
            }
        }

        return false; // No service point satisfies this dispatch
    }
}