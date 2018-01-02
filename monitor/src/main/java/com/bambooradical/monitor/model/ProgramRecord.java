/*
 * Copyright (C) 2018 Peter Withers
 */
package com.bambooradical.monitor.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @since Jan 2, 2018 20:55:48 PM (creation date)
 * @author : Peter Withers <peter-gthb@gthb-bambooradical.com>
 */
public class ProgramRecord {

    final private String location;
    final private int milliseconds;
    final private String colour;
    final private boolean tween;

    public ProgramRecord(String location, int milliseconds, String colour, boolean tween) {
        this.location = location;
        this.milliseconds = milliseconds;
        this.colour = colour;
        this.tween = tween;
    }

    public String getLocation() {
        return location;
    }

    public String getColour() {
        return colour;
    }

    public boolean isTween() {
        return tween;
    }

    public String getTime() {
        Date date = new Date(milliseconds);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }

    public String getProgramCode() {
        return colour + ((tween) ? "T" : ":") + String.format("%06x", milliseconds) + ";";
    }
}

