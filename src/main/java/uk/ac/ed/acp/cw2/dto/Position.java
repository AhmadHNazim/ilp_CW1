package uk.ac.ed.acp.cw2.dto;

import jakarta.validation.constraints.NotNull;

public class Position {
    @NotNull
    private Double lng;

    @NotNull
    private Double lat;

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
}
