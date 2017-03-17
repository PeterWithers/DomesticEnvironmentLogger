/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.GraphPoint;
import com.bambooradical.monitor.repository.DataRecordRepository;
import com.bambooradical.monitor.repository.EnergyRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    @RequestMapping("/monitor/energy")
    public String getEnergy(Model model) {
        final PageRequest pageRequest = new PageRequest(0, 1000);
        model.addAttribute("energyDataG3a", new GraphPoint[]{new GraphPoint(1489683773795l, 18.4), new GraphPoint(1489684975414l, 18.4)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("G3a", pageRequest));
        model.addAttribute("energyDataG4", new GraphPoint[]{new GraphPoint(1489684284340l, 19.4), new GraphPoint(1489685485920l, 19.5)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("G4", pageRequest));
        model.addAttribute("energyDataW3", new GraphPoint[]{new GraphPoint(1489684284340l, 17.4), new GraphPoint(1489685485920l, 11.5)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("W3", pageRequest));
        model.addAttribute("energyDataW3a", new GraphPoint[]{new GraphPoint(1489684284340l, 12.4), new GraphPoint(1489685485920l, 12.5)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("W3a", pageRequest));
        model.addAttribute("energyDataW4", new GraphPoint[]{new GraphPoint(1489684284340l, 15.4), new GraphPoint(1489685485920l, 13.5)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("W4", pageRequest));
        model.addAttribute("energyDataE3a", new GraphPoint[]{new GraphPoint(1489684284340l, 11.4), new GraphPoint(1489685485920l, 14.5)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("E3a", pageRequest));
        model.addAttribute("energyDataE4", new GraphPoint[]{new GraphPoint(1489683681559l, 12.8), new GraphPoint(1489684878915l, 15.7)}); //energyRecordRepository.findByMeterLocationStartsWithIgnoreCaseOrderByRecordDateAsc("E4", pageRequest));
        return "energyviewer";
    }
}
