package uk.ac.ed.acp.cw2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryAttribute {
    private String attribute;
    private String operator;
    private String value;
}
