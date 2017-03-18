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

    public GraphPoint(EnergyRecord previousEnergyRecord, EnergyRecord currentEnergyRecord) {
        int daysBetween = (int) ((currentEnergyRecord.getRecordDate().getTime() - previousEnergyRecord.getRecordDate().getTime()) / (1000 * 60 * 60 * 24));
        this.x = currentEnergyRecord.getRecordDate().getTime();
        this.y = (currentEnergyRecord.getMeterValue() - previousEnergyRecord.getMeterValue()) / daysBetween;
    }

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
