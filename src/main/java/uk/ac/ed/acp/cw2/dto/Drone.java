package uk.ac.ed.acp.cw2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Drone {
    // Getters and setters
    private int id;
    private String name;
    private DroneCapability capability;

}