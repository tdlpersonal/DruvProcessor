package com.bridgeit.druv.processor.utils;

import java.text.DecimalFormat;

public class Point {
    public final double x, y;

    public Point(Double x, Double y) {
    	
    	this.x = ((long)(x*1000))/1000.0;
    	this.y = ((long)(y*1000))/1000.0;
    	
    }

    public double distanceFrom(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Point point = (Point) other;
        return x == point.x && y == point.y;
    }

    @Override
    public String toString() {
        return String.format("(%.5f, %.5f)", x, y);
    }
}
