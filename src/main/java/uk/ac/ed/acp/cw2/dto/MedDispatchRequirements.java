package uk.ac.ed.acp.cw2.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class MedDispatchRequirements {
    private double capacity;     // required
    private Boolean cooling;     // optional
    private Boolean heating;     // optional
    private Double maxCost;      // optional
}

