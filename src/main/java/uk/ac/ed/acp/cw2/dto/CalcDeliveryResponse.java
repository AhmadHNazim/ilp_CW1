package uk.ac.ed.acp.cw2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.ac.ed.acp.cw2.dto.Position;

import java.util.List;

@Data
public class CalcDeliveryResponse {
    private double totalCost;
    private int totalMoves;
    private List<DronePath> dronePaths;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DronePath {
        private int droneId;
        private List<DeliveryPath> deliveries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryPath {
        private Integer deliveryId; // id of MedDispatchRec, null for return-only segment if you prefer
        private List<Position> flightPath;
    }
}