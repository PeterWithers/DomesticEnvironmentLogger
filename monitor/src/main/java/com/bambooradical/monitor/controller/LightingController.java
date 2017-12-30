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
@RequestMapping("/lighting")
public class LightingController {

    @Autowired
    LightingService lightingService;

    static String valueRGB = "";

    @RequestMapping("/currentRGB")
    public String currentRGB(
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "value", required = false, defaultValue = "") String value
    ) {
        if (value != null && !value.isEmpty()) {
            valueRGB = (value.length() > 14) ? value : "";
        }
        if (valueRGB.isEmpty()) {
            DateTime dateTime = new DateTime(DateTimeZone.forOffsetHours(+1));
            int hourOfDay = dateTime.getHourOfDay();
            if (hourOfDay > 23) {
                return "550055:000000;550000T000100;555500T000200;005500T000300;005555T000400;000055T000500;550055T000600;";
            } else if (hourOfDay > 10) {
                return "000000:000000;550000T000100;005500:000200;000055:000300;";
            } else if (hourOfDay > 12) {
                return "000000:000000;ff0000T000100;00ff00:000200;0000ff:000300;000000:000400;ffffffT000500;000000T000600;ffffffT000700;";
            } else {
                return "000000:000000;000000T000100;000000:000200;";
            }
        } else {
            return valueRGB;
        }
    }
}
