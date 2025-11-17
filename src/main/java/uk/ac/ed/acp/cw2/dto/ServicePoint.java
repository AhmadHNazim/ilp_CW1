package uk.ac.ed.acp.cw2.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ServicePoint {
    private int id;
    private String name;
    private Position location;
}
