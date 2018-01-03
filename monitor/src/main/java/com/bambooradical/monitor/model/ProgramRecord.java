/*
 * Copyright (C) 2018 Peter Withers
 */
package com.bambooradical.monitor.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @since Jan 2, 2018 20:55:48 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class ProgramRecord {

    final private String location;
    final private int milliseconds;
    final private String colour;
    final private boolean tween;

    public ProgramRecord(String programCode) throws ParseException {
        this.location = "FromProgramCode";
        this.milliseconds = Integer.parseInt(programCode.substring(7, 13), 16);
        this.colour = programCode.substring(0, 6);
        this.tween = "T".equals(programCode.substring(6, 7));
    }

    public ProgramRecord(String location, final String programTime, String colour, boolean tween) throws ParseException {
        this.location = location;
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        this.milliseconds = (int) formatter.parse(programTime).toInstant().toEpochMilli();
        this.colour = colour;
        this.tween = tween;
    }

    public ProgramRecord(String location, int milliseconds, String colour, boolean tween) {
        this.location = location;
        this.milliseconds = milliseconds;
        this.colour = colour;
        this.tween = tween;
    }

    public String getLocation() {
        return location;
    }

    public String getKey() {
        return String.format("%06x", milliseconds);
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

