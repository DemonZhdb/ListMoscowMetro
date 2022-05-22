package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

@Getter
public class Metro {
    private List<Line> lines;
    private HashSet<Station> stations;
    private HashSet<TreeMap> connections;

    public Metro() {
        lines = new ArrayList<>();
        stations = new HashSet<>();
        connections = new HashSet<>();
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public void addStation(Station station) {
        stations.add(station);
    }

    public void addConnections(TreeMap connection) {
        connections.add(connection);

    }

    public Line getLineByName(String lineName) {
        for (Line line : lines) {
            if (lineName.equalsIgnoreCase(line.getName())) {
                return line;
            }
        }
        return null;
    }

}
