package com.castlabs.model;

public class Box {
    private int length;
    private long offset;
    private String name;

    public Box(int length, long offset, String name) {
        this.length = length;
        this.offset = offset;
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public long getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Box Type : " + name + " Size : " + length;
    }
}