package model;

import lombok.Getter;

@Getter
public class Station {
    private Line line;
    private String name;

    public Station(String name, Line line) {
        this.name = name;
        this.line = line;
    }


    public String getName() {
        return name;
    }


}