package uk.ac.ed.acp.cw2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.ac.ed.acp.cw2.dto.CalcDeliveryResponse;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DroneService {

    // Create a logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(DroneService.class);

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
        // REQ-NFR-05: Robustness Instrumentation
        if (query.getAttribute() == null || query.getOperator() == null || query.getValue() == null) {
            // We log at DEBUG or WARN level so the tester can see WHY the match failed
            logger.warn("Robustness Check: Skipping query constraint due to null field(s). Attribute: {}, Op: {}, Value: {}",
                    query.getAttribute(), query.getOperator(), query.getValue());

            return false; // Graceful fallback
        }

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
            // --- FIX: Robustness Check for REQ-NFR-05 ---
            // Verify critical data exists before accessing it to prevent NPE (500 error)
            if (request.getRequirements() == null) {
                // Log the warning so we have an audit trail for why this was skipped
                logger.warn("Robustness: Skipping dispatch {} due to missing requirements.", request.getId());
                return false;
            }

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

    private static final double STEP_SIZE = 0.00015;
    private static final double[] ALLOWED_ANGLES = new double[16];
    static {
        for (int i = 0; i < 16; i++) ALLOWED_ANGLES[i] = i * 22.5;
    }

    /**
     * Main entry for calcDeliveryPath using A* on 16-direction grid.
     */
    public CalcDeliveryResponse calcDeliveryPath(List<MedDispatchRec> dispatches) {
        // fetch ILP resources
        Drone[] allDrones = ilpClient.getAllDrones();
        Map<Integer, Drone> droneMap = Arrays.stream(allDrones)
                .collect(Collectors.toMap(Drone::getId, d -> d));

        DroneForServicePoint[] dfspArray = ilpClient.getAllDronesForServicePoints();
        List<DroneForServicePoint> dfsps = dfspArray == null ? List.of() : Arrays.asList(dfspArray);

        ServicePoint[] servicePointsArr = ilpClient.getAllServicePoints();
        Map<Integer, ServicePoint> servicePointById = servicePointsArr == null
                ? Map.of()
                : Arrays.stream(servicePointsArr).collect(Collectors.toMap(ServicePoint::getId, sp -> sp));

        RestrictedArea[] raArray = ilpClient.getRestrictedAreas();
        List<RestrictedArea> restrictedAreas = raArray == null ? List.of() : Arrays.asList(raArray);

        // pending dispatch map id -> MedDispatchRec (retain insertion order)
        Map<Integer, MedDispatchRec> pending = new LinkedHashMap<>();
        for (MedDispatchRec m : dispatches) pending.put(m.getId(), m);

        List<CalcDeliveryResponse.DronePath> resultDronePaths = new ArrayList<>();
        int totalMoves = 0;
        double totalCost = 0.0;

        // iterate over service points and available drones greedily
        outerServicePointLoop:
        for (DroneForServicePoint spEntry : dfsps) {
            ServicePoint sp = servicePointById.get(spEntry.getServicePointId());
            if (sp == null) continue;

            // for each drone availability at this service point
            for (DroneForServicePoint.DroneAvailability avail : spEntry.getDrones()) {
                Drone drone = droneMap.get(avail.getId());
                if (drone == null) continue;

                // attempt to build a route for this drone starting/ending at sp
                List<CalcDeliveryResponse.DeliveryPath> deliveriesForDrone = new ArrayList<>();
                Position startPos = sp.getLocation();
                Position currentPos = makePos(startPos.getLng(), startPos.getLat());

                int movesUsedForDrone = 0;

                // Keep attempting to take the best reachable pending dispatch until none left or drone exhausted
                boolean didProgress;
                do {
                    didProgress = false;
                    // choose candidate that leads to minimal extra moves (heuristic)
                    Integer chosenId = null;
                    List<Position> chosenPathToDelivery = null;
                    List<Position> chosenReturnPath = null;
                    int chosenExtraMoves = Integer.MAX_VALUE;
                    double chosenFlightCost = 0.0;

                    for (MedDispatchRec candidate : List.copyOf(pending.values())) {
                        Position delivery = candidate.getDelivery();
                        if (delivery == null) continue; // skip invalid

                        // availability check for this drone at this service point/time
                        if (!isDroneAvailableAtSlot(avail, candidate.getDate(), candidate.getTime())) {
                            continue;
                        }

                        // A* from currentPos -> delivery
                        List<Position> pathToDelivery = aStarPath(currentPos, delivery, restrictedAreas, drone.getCapability().getMaxMoves());
                        if (pathToDelivery == null) continue; // unreachable

                        // A* from delivery -> service point (return)
                        List<Position> pathReturn = aStarPath(delivery, startPos, restrictedAreas, drone.getCapability().getMaxMoves());
                        if (pathReturn == null) continue; // cannot return => invalid

                        // compute moves: moves are edges = nodes-1
                        int movesTo = Math.max(0, pathToDelivery.size() - 1);
                        // hover represented by adding an identical position => +1 move for hover
                        int hoverMoves = 1;
                        int movesReturn = Math.max(0, pathReturn.size() - 1);
                        int extraMoves = movesTo + hoverMoves + movesReturn;

                        // ensure drone does not exceed maxMoves
                        if (movesUsedForDrone + extraMoves > drone.getCapability().getMaxMoves()) {
                            continue;
                        }

                        // compute flight cost for this flight (start -> delivery -> return)
                        double flightCost = drone.getCapability().getCostInitial()
                                + drone.getCapability().getCostFinal()
                                + (extraMoves) * drone.getCapability().getCostPerMove();

                        // respect candidate maxCost if present
                        Double maxCost = candidate.getRequirements() == null ? null : candidate.getRequirements().getMaxCost();
                        if (maxCost != null && flightCost > maxCost) continue;

                        // pick candidate with smallest extraMoves (tie-breaker: smaller flightCost)
                        if (extraMoves < chosenExtraMoves || (extraMoves == chosenExtraMoves && flightCost < chosenFlightCost)) {
                            chosenId = candidate.getId();
                            chosenPathToDelivery = pathToDelivery;
                            chosenReturnPath = pathReturn;
                            chosenExtraMoves = extraMoves;
                            chosenFlightCost = flightCost;
                        }
                    }

                    if (chosenId != null) {
                        // we will commit chosen candidate
                        didProgress = true;

                        // build flightPath segment: pathToDelivery + hover (duplicate last position)
                        List<Position> flightToDelivery = new ArrayList<>(chosenPathToDelivery);
                        // append hover duplicate
                        flightToDelivery.add(flightToDelivery.get(flightToDelivery.size() - 1));

                        // add as delivery segment
                        deliveriesForDrone.add(new CalcDeliveryResponse.DeliveryPath(chosenId, flightToDelivery));

                        // update counters and pending set
                        movesUsedForDrone += chosenExtraMoves;
                        totalMoves += chosenExtraMoves;
                        totalCost += chosenFlightCost;

                        // mark delivered and remove
                        pending.remove(chosenId);

                        // update current position to delivery (the last real position)
                        MedDispatchRec finished = null;
                        Position deliveryPos = flightToDelivery.get(flightToDelivery.size() - 1); // the hover duplicate
                        currentPos = makePos(deliveryPos.getLng(), deliveryPos.getLat());

                    }
                } while (didProgress && !pending.isEmpty());

                // If this drone delivered any, append final return-to-base path as a delivery-like segment (deliveryId = null)
                if (!deliveriesForDrone.isEmpty()) {
                    // compute return path from currentPos -> startPos
                    List<Position> returnPath = aStarPath(currentPos, startPos, restrictedAreas, drone.getCapability().getMaxMoves());
                    if (returnPath != null && returnPath.size() > 0) {
                        deliveriesForDrone.add(new CalcDeliveryResponse.DeliveryPath(null, returnPath));
                        int returnMoves = Math.max(0, returnPath.size() - 1);
                        totalMoves += returnMoves;
                        movesUsedForDrone += returnMoves;
                        // cost for the return-only segment: we already charged costInitial+costFinal when choosing the first delivery.
                        // To keep costs consistent, we will **not** double-charge initial+final for each segment; instead costs were added per chosen candidate above (which included full flight start->return).
                    }

                    resultDronePaths.add(new CalcDeliveryResponse.DronePath(drone.getId(), deliveriesForDrone));
                }

                if (pending.isEmpty()) break outerServicePointLoop;
            }
        }

        // Build and return response
        CalcDeliveryResponse resp = new CalcDeliveryResponse();
        resp.setTotalCost(totalCost);
        resp.setTotalMoves(totalMoves);
        resp.setDronePaths(resultDronePaths);
        return resp;
    }


    private List<Position> aStarPath(Position start, Position goal, List<RestrictedArea> restrictedAreas, int maxMovesLimit) {
        if (start == null || goal == null) return null;

        // trivial close check
        DistanceRequest closeCheck = new DistanceRequest();
        closeCheck.setPosition1(start);
        closeCheck.setPosition2(goal);
        if (geometricService.calculateDistance(closeCheck) <= STEP_SIZE / 2) {
            // already at target: return single node (caller may append hover)
            List<Position> p = new ArrayList<>();
            p.add(makePos(start.getLng(), start.getLat()));
            return p;
        }

        // node key formatting to stable string to avoid floating noise
        var df = new DecimalFormat("0.0000000000", DecimalFormatSymbols.getInstance(Locale.US));

        Function<Position, String> keyOf = pos -> df.format(pos.getLat()) + "," + df.format(pos.getLng());

        class Node implements Comparable<Node> {
            Position pos;
            int g; // moves so far
            double f; // g + heuristic
            Node(Position pos, int g, double f) { this.pos = pos; this.g = g; this.f = f; }
            @Override public int compareTo(Node o) { return Double.compare(this.f, o.f); }
        }

        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<String, Integer> gScore = new HashMap<>();
        Map<String, Position> cameFrom = new HashMap<>();

        Position startCopy = makePos(start.getLng(), start.getLat());
        Position goalCopy = makePos(goal.getLng(), goal.getLat());
        String startKey = keyOf.apply(startCopy);
        String goalKey = keyOf.apply(goalCopy);

        // heuristic: Euclidean distance / STEP_SIZE -> estimated moves
        DistanceRequest hdr = new DistanceRequest();
        hdr.setPosition1(startCopy);
        hdr.setPosition2(goalCopy);
        double hStart = geometricService.calculateDistance(hdr) / STEP_SIZE;

        open.add(new Node(startCopy, 0, hStart));
        gScore.put(startKey, 0);

        int expansions = 0;
        int maxExpansions = (maxMovesLimit > 0) ? Math.min(maxMovesLimit * 5, 50000) : 50000; // safety cap

        while (!open.isEmpty() && expansions++ < maxExpansions) {
            Node current = open.poll();
            String currKey = keyOf.apply(current.pos);

            // stop if within STEP_SIZE/2 of goal
            DistanceRequest stopCheck = new DistanceRequest();
            stopCheck.setPosition1(current.pos);
            stopCheck.setPosition2(goalCopy);
            if (geometricService.calculateDistance(stopCheck) <= STEP_SIZE / 2) {
                // reconstruct path from startKey -> current.pos -> goal (append goal as exact)
                List<Position> path = reconstructPath(cameFrom, keyOf, startKey, currKey);
                // append final exact goal position (so return path ends at the delivery coordinates)
                path.add(makePos(goalCopy.getLng(), goalCopy.getLat()));
                return path;
            }

            // generate neighbors
            for (double angle : ALLOWED_ANGLES) {
                NextPositionRequest npr = new NextPositionRequest();
                npr.setStart(current.pos);
                npr.setAngle(angle);
                Position neighbor;
                try {
                    neighbor = geometricService.nextPosition(npr);
                } catch (Exception e) {
                    continue; // invalid angle or coords
                }
                if (neighbor == null) continue;

                // skip if neighbor is inside restricted area (we treat a restricted area with missing limits as NO-FLY if limits absent)
                boolean blocked = false;
                for (RestrictedArea ra : restrictedAreas) {
                    try {
                        if (ra.getVertices() != null && !ra.getVertices().isEmpty()) {
                            IsInRegionRequest irr = new IsInRegionRequest();
                            irr.setPosition(neighbor);
                            irr.setRegion(toRegion(ra));
                            if (geometricService.isInRegion(irr)) {
                                blocked = true;
                                break;
                            }
                        } else {
                            // limits missing -> whole area is no-fly; but if vertices empty skip
                        }
                    } catch (Exception ignored) {}
                }
                if (blocked) continue;

                String neighKey = keyOf.apply(neighbor);
                int tentativeG = current.g + 1; // one additional move

                Integer prevG = gScore.get(neighKey);
                if (prevG == null || tentativeG < prevG) {
                    cameFrom.put(neighKey, current.pos);
                    gScore.put(neighKey, tentativeG);
                    // heuristic from neighbor -> goal
                    DistanceRequest hdr2 = new DistanceRequest();
                    hdr2.setPosition1(neighbor);
                    hdr2.setPosition2(goalCopy);
                    double h = geometricService.calculateDistance(hdr2) / STEP_SIZE;
                    double f = tentativeG + h;
                    open.add(new Node(neighbor, tentativeG, f));
                }
            }
        }

        // failed to find path within expansion limits
        return null;
    }

    private List<Position> reconstructPath(Map<String, Position> cameFrom, Function<Position, String> keyOf,
                                           String startKey, String currentKey) {
        LinkedList<Position> path = new LinkedList<>();
        // currentKey corresponds to some Position; we need its Position. cameFrom stores mapping from childKey -> parentPos (not parentKey)
        // find current position by parsing key
        String[] parts = currentKey.split(",");
        double lat = Double.parseDouble(parts[0]);
        double lng = Double.parseDouble(parts[1]);
        Position curr = makePos(lng, lat);
        path.addFirst(curr);
        String key = currentKey;
        while (!key.equals(startKey)) {
            Position parent = cameFrom.get(key);
            if (parent == null) break;
            path.addFirst(makePos(parent.getLng(), parent.getLat()));
            key = keyOf.apply(parent);
        }
        // ensure start at startKey position as first element (might be identical)
        return new ArrayList<>(path);
    }

    private Position makePos(double lng, double lat) {
        Position p = new Position();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }

    /* Availability check used earlier (keeps unchanged logic) */
    private boolean isDroneAvailableAtSlot(DroneForServicePoint.DroneAvailability availability, String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return true;

        LocalDate d;
        LocalTime t;
        try {
            d = LocalDate.parse(dateStr);
            t = LocalTime.parse(timeStr);
        } catch (Exception e) {
            return false;
        }

        String dow = d.getDayOfWeek().toString();

        for (DroneForServicePoint.DroneAvailability.Availability slot : availability.getAvailability()) {
            if (!slot.getDayOfWeek().equalsIgnoreCase(dow)) continue;

            LocalTime from = LocalTime.parse(slot.getFrom());
            LocalTime until = LocalTime.parse(slot.getUntil());

            if ((t.equals(from) || t.isAfter(from)) && (t.equals(until) || t.isBefore(until))) {
                return true;
            }
        }

        return false;
    }

    private Region toRegion(RestrictedArea ra) {
        Region region = new Region();
        region.setName(ra.getName());
        region.setVertices(ra.getVertices()); // same type, so OK
        return region;
    }

    public Map<String, Object> calcDeliveryPathAsGeoJson(List<MedDispatchRec> dispatches) {
        CalcDeliveryResponse response = calcDeliveryPath(dispatches);

        // Flatten all flight positions for the first drone (assumes single drone can deliver all)
        List<Position> fullPath = new ArrayList<>();
        if (!response.getDronePaths().isEmpty()) {
            CalcDeliveryResponse.DronePath dronePath = response.getDronePaths().get(0);
            for (CalcDeliveryResponse.DeliveryPath delivery : dronePath.getDeliveries()) {
                fullPath.addAll(delivery.getFlightPath());
            }
        }

        // Build GeoJSON LineString
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "Feature");
        geoJson.put("properties", Map.of("droneId", response.getDronePaths().isEmpty() ? null : response.getDronePaths().get(0).getDroneId()));

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", fullPath.stream()
                .map(p -> List.of(p.getLng(), p.getLat()))
                .toList());

        geoJson.put("geometry", geometry);
        return geoJson;
    }

}