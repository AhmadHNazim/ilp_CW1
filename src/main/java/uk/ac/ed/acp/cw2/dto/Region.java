package uk.ac.ed.acp.cw2.dto;

import java.util.List;

public class Region {
    private String name;
    private List<Position> vertices;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Position> getVertices() {
        return vertices;
    }

    public void setVertices(List<Position> vertices) {
        this.vertices = vertices;
    }
}
