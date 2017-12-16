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

    public GraphPoint(EnergyRecord currentEnergyRecord) {
        this.x = currentEnergyRecord.getRecordDate().getTime();
        this.y = 0;
    }

    public GraphPoint(EnergyRecord previousEnergyRecord, GraphPoint graphPoint) {
        final long currentX = previousEnergyRecord.getRecordDate().getTime();
        this.x = currentX;// + (graphPoint.x - currentX) / 2;
        this.y = graphPoint.getY();
    }

    public GraphPoint(EnergyRecord previousEnergyRecord, EnergyRecord currentEnergyRecord, final boolean linear, final double offset, final float pricePerUnit) {
        int daysBetween = (int) ((currentEnergyRecord.getRecordDate().getTime() - previousEnergyRecord.getRecordDate().getTime()) / (1000 * 60 * 60 * 24));
        this.x = currentEnergyRecord.getRecordDate().getTime();
        this.y = ((linear) ? currentEnergyRecord.getMeterValue() - offset : (currentEnergyRecord.getMeterValue() - previousEnergyRecord.getMeterValue()) / daysBetween) * pricePerUnit;
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
