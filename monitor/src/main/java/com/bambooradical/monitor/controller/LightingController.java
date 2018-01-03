/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.ProgramRecord;
import com.bambooradical.monitor.repository.LightingService;
import java.text.ParseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @since Dec 30, 2017 11:31:41 AM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Controller
@RequestMapping("/monitor")
public class LightingController {

    @Autowired
    LightingService lightingService;

    @RequestMapping("/showProgram")
    public String showProgram(Model model) throws ParseException {
        model.addAttribute("programData", lightingService.getProgramRecords());
        return "programviewer";
    }

    @RequestMapping("/setProgram")
    public String currentRGB(
            Model model,
            @RequestParam(value = "action", required = true) final String action,
            @RequestParam(value = "location", required = true) final String location,
            @RequestParam(value = "tween", required = true) final boolean tween,
            @RequestParam(value = "programTime", required = true) final String programTime,
            @RequestParam(value = "programColour", required = true) final String programColour
    ) throws ParseException {
        final ProgramRecord programRecord = new ProgramRecord(location, programTime + "00:00:00".substring(programTime.length()), programColour.substring(1), tween);
        switch (action) {
            case "deleteProgram":
                lightingService.deleteProgram(programRecord);
                break;
            case "updateProgram":
                lightingService.updateProgram(programRecord);
                break;
            case "addProgram":
                lightingService.addProgram(programRecord);
                break;
        }
        model.addAttribute("programData", lightingService.getProgramRecords());
        return "programviewer";
    }

    @RequestMapping(value = "/currentRGB",
            method = RequestMethod.GET,
            produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
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
