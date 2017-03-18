/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.EnergyRecord;
import com.bambooradical.monitor.model.GraphPoint;
import com.bambooradical.monitor.repository.DataRecordRepository;
import com.bambooradical.monitor.repository.EnergyRecordRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @RequestMapping("/monitor")
    public String getChart(Model model) {
        return "dataviewer";
    }

    private List<GraphPoint> getGraphPointList(String meterLocation, final Pageable pageable) {
        List<GraphPoint> returnList = new ArrayList<>();
        final List<EnergyRecord> meterLocationsRecords = energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc(meterLocation, pageable);
        EnergyRecord previousEnergyRecord = null;
        for (EnergyRecord energyRecord : meterLocationsRecords) {
            if (previousEnergyRecord != null) {
                returnList.add(new GraphPoint(previousEnergyRecord, energyRecord));
            }
            previousEnergyRecord = energyRecord;
        }
        return returnList;
    }

    @RequestMapping("/monitor/energy")
    public String getEnergy(Model model) {
        final PageRequest pageRequest = new PageRequest(0, 1000);
        model.addAttribute("energyDataG3a", getGraphPointList("G3a", pageRequest));
        model.addAttribute("energyDataG4", getGraphPointList("G4", pageRequest));
        model.addAttribute("energyDataW3", getGraphPointList("W3", pageRequest));
        model.addAttribute("energyDataW3a", getGraphPointList("W3a", pageRequest));
        model.addAttribute("energyDataW4", getGraphPointList("W4", pageRequest));
        model.addAttribute("energyDataE3a", getGraphPointList("E3a", pageRequest));
        model.addAttribute("energyDataE4", getGraphPointList("E4", pageRequest));
        return "energyviewer";
    }
}
