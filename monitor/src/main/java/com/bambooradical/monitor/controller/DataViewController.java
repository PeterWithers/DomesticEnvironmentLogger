/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.repository.DataRecordRepository;
import com.bambooradical.monitor.repository.EnergyRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @since Nov 21, 2016 8:01:11 PM (creation date)
 * @author @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Controller
public class DataViewController {

    @Autowired
    DataRecordRepository dataRecordRepository;
    @Autowired
    EnergyRecordRepository energyRecordRepository;

    @RequestMapping("/")
    public String getChart(Model model) {
        return "dataviewer";
    }

    @RequestMapping("/energy")
    public String getEnergy(Model model) {
        model.addAttribute("energyData", energyRecordRepository.findAll());
        return "energyviewer";
    }
}
