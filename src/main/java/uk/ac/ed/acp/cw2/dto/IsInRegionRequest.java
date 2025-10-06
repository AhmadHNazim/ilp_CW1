package uk.ac.ed.acp.cw2.dto;

public class IsInRegionRequest {
    private Position position;
    private Region region;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
