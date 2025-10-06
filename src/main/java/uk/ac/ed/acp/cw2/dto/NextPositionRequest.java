package uk.ac.ed.acp.cw2.dto;

public class NextPositionRequest {
    private Position start;
    private double angle;

    public Position getStart() {
        return start;
    }

    public void setStart(Position start) {
        this.start = start;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
