package uk.ac.ed.acp.cw2.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class DroneForServicePoint {
    private int servicePointId;
    private List<DroneAvailability> drones;

    @Data
    @Getter
    @Setter
    public static class DroneAvailability {
        private int id;
        private List<Availability> availability;

        @Data
        @Getter
        @Setter
        public static class Availability {
            private String dayOfWeek; // e.g., "MONDAY"
            private String from;      // e.g., "00:00:00"
            private String until;     // e.g., "23:59:59"
        }
    }
}
