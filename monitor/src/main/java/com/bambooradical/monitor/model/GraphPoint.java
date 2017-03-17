/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.model;

/**
 * @since Mar 18, 2017 12:29:29 AM (creation date)
 * @author @author : Peter Withers <peter@gthb-bambooradical.com>
 */
public class GraphPoint {

    long x;
    double y;

    public GraphPoint(long date, double value) {
        this.x = date;
        this.y = value;
    }

    public long getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
