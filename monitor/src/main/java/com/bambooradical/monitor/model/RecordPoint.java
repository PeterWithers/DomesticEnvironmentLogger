/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.model;

/**
 * @since Mar 23, 2017 7:48:12 PM (creation date)
 * @author Peter Withers <peter@gthb-bambooradical.com>
 */
public class RecordPoint {

    long x;
    float y;

    public RecordPoint(long date, float value) {
        this.x = date;
        this.y = value;
    }

    public long getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
