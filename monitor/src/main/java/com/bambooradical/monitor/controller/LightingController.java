/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.repository.LightingService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @since Dec 30, 2017 11:31:41 AM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@RestController
@RequestMapping("/monitor")
public class LightingController {

    @Autowired
    LightingService lightingService;

    @RequestMapping("/showProgram")
    public String showProgram() {
        String resultValue = "";
        for (int index = 0; index < 24; index++) {
            resultValue += "<a href=\"setProgram?location=&hour=" + index + "&value=" + lightingService.findProgram(index) + "\">" + index + ":" + lightingService.findProgram(index) + "</a><br/>";
        }
        resultValue += "<a href=\"currentRGB\">currentRGB</a><br/>";
        return resultValue;
    }

    @RequestMapping("/setProgram")
    public String currentRGB(
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "hour", required = true) int hour,
            @RequestParam(value = "value", required = false, defaultValue = "") String value
    ) {
        lightingService.updateProgram(hour, value);
        String resultValue = "";
        for (int index = 0; index < 24; index++) {
            resultValue += "<a href=\"setProgram?location=&hour=" + index + "&value=" + lightingService.findProgram(index) + "\">" + index + ":" + lightingService.findProgram(index) + "</a><br/>";
        }
        return resultValue;
    }

    @RequestMapping("/currentRGB")
    public String currentRGB(
            @RequestParam(value = "location", required = true) String location
    ) {
        DateTime dateTime = new DateTime(DateTimeZone.forOffsetHours(+1));
        int hourOfDay = dateTime.getHourOfDay();
        int minuteOfHour = dateTime.getMinuteOfHour();
        String currentProgram = lightingService.findProgram(hourOfDay);
        if (currentProgram == null || currentProgram.isEmpty()) {
            int redValue = 0;
            int greenValue = 0;
            int blueValue = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (int minuteCounter = 0; minuteCounter < 60; minuteCounter++) {
                redValue = ((minuteCounter + minuteOfHour) % 2 == 0) ? 0x00 : 0xff;
                greenValue = 0xff / 5 * ((minuteCounter + minuteOfHour) % 5);
                blueValue = 0xff / 15 * ((minuteCounter + minuteOfHour) % 15);
                stringBuilder.append(String.format("%02x", redValue));
                stringBuilder.append(String.format("%02x", greenValue));
                stringBuilder.append(String.format("%02x", blueValue));
                stringBuilder.append("T");
                stringBuilder.append(String.format("%06x", minuteCounter * 60 * 1000));
                stringBuilder.append(";");
            }
            return stringBuilder.toString();
        } else {
            return currentProgram;
        }
    }
}
