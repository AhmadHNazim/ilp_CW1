package uk.ac.ed.acp.cw2.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class RestrictedArea {
    private int id;
    private String name;
    private Limits limits;          // optional, if missing -> no-fly zone
    private List<Position> vertices;

    @Data
    @Getter
    @Setter
    public static class Limits {
        private double lower;
        private double upper;
    }
}
