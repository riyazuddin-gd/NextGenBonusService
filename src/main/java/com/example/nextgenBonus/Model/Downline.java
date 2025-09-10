package com.example.nextgenBonus.Model;

public class Downline {
    private String name;

    private String level;

    public Downline() {
    }

    public String getName() {
        return name;
    }

    public Downline(String level, String name) {
        this.level = level;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
