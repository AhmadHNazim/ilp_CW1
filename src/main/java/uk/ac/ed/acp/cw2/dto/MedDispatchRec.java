package uk.ac.ed.acp.cw2.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class MedDispatchRec {
    private int id;                       // required
    private String date;                  // optional
    private String time;                  // optional
    private MedDispatchRequirements requirements;   // required (but only capacity must be present)
    private Position delivery;
}
