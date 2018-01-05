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
        model.addAttribute("programStyle", getCssGradient());
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
        return "redirect:showProgram";
    }

    private String getCssGradient() {
        DateTime dateTime = new DateTime(DateTimeZone.forOffsetHours(+1));
        int millisOfDay = dateTime.getMillisOfDay();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("height: 864px; background: linear-gradient(");
        boolean isFirst = true;
        double previousY = 0;
        String previousColour = "";
        for (ProgramRecord currentProgram : lightingService.findProgram(millisOfDay)) {
            final double currentY = currentProgram.getOffsetMilliseconds() / 100000.0;
            if (!isFirst) {
                stringBuilder.append(", ");
                if (!currentProgram.isTween() && currentY - previousY > 1) {
                    stringBuilder.append("#");
                    stringBuilder.append(previousColour);
                    stringBuilder.append(" ");
                    stringBuilder.append(previousY + 1);
                    stringBuilder.append("px, ");
                }
            }
            isFirst = false;
            stringBuilder.append("#");
            stringBuilder.append(currentProgram.getColour());
            stringBuilder.append(" ");
            stringBuilder.append(currentY);
            stringBuilder.append("px");
            previousY = currentY;
            previousColour = currentProgram.getColour();
        }
        stringBuilder.append(");");
        return stringBuilder.toString();
    }

    @RequestMapping(value = "/currentRGB",
            method = RequestMethod.GET,
            produces = {MediaType.TEXT_PLAIN_VALUE})
    @ResponseBody
    public String currentRGB(
            @RequestParam(value = "location", required = true) String location
    ) {
        DateTime dateTime = new DateTime(DateTimeZone.forOffsetHours(+1));
        int millisOfDay = dateTime.getMillisOfDay();
        StringBuilder stringBuilder = new StringBuilder();
        for (ProgramRecord currentProgram : lightingService.findProgram(millisOfDay)) {
            stringBuilder.append(currentProgram.getProgramCode());
        }
        return stringBuilder.toString();
    }
}
