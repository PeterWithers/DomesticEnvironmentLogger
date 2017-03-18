/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.EnergyRecord;
import com.bambooradical.monitor.model.GraphPoint;
import com.bambooradical.monitor.repository.DataRecordRepository;
import com.bambooradical.monitor.repository.EnergyRecordRepository;
import java.util.ArrayList;
import java.util.Date;
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
        final List<EnergyRecord> meterLocationsRecords = energyRecordRepository.findByMeterLocationOrderByRecordDateAsc(meterLocation, pageable);
        EnergyRecord previousEnergyRecord = null;
        for (EnergyRecord energyRecord : meterLocationsRecords) {
            if (previousEnergyRecord != null) {
                returnList.add(new GraphPoint(previousEnergyRecord, energyRecord));
            } else {
                returnList.add(new GraphPoint(energyRecord));
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

    @RequestMapping("/monitor/energy/insertdata")
    public String insertData(Model model) {
        energyRecordRepository.deleteAll();
        energyRecordRepository.save(new EnergyRecord("E3a", 29122.0, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 568.442017, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1763.0, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 630.0, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 2977.43896, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10624.4639, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 228.699997, new Date(1472702400000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1796.0, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 615.643982, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3072.28198, new Date(1480741200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3067.77295, new Date(1480395600000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10826.3447, new Date(1480395600000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3062.95996, new Date(1480222800000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3014.09692, new Date(1477627200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3005.94189, new Date(1477195200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3081.79199, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1481173200000l)));
//        energyRecordRepository.save(new EnergyRecord("W3a", 615.645996, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29257.0, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10841.4609, new Date(1480741200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3068.66992, new Date(1480482000000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10831.0898, new Date(1480482000000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10816.8223, new Date(1480222800000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10706.5342, new Date(1477713600000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10686.5996, new Date(1477195200000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 461.100006, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 441.399994, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10866.6504, new Date(1481173200000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10882.9365, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29268.0, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1798.0, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 618.116028, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3087.65308, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1481605200000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 539.900024, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10888.8174, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3093.68604, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1810.0, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 629.546021, new Date(1484629200000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1812.0, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 633.187988, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3127.46289, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 558.599976, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10921.4756, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29315.0, new Date(1485234000000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 571.400024, new Date(1485579600000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10945.8291, new Date(1485579600000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3152.71411, new Date(1485579600000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1485579600000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 577.400024, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 10974.6387, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3182.5481, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29356.0, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 641.185974, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1817.0, new Date(1486184400000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 11049.2725, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 610.0, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3260.10498, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29435.0, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1823.0, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 653.495972, new Date(1487653200000l)));
        energyRecordRepository.save(new EnergyRecord("E4", 618.5, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("G4", 11091.9033, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("G3a", 3304.12891, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("W4", 631.0, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("E3a", 29480.0, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("W3", 1825.0, new Date(1488258000000l)));
        energyRecordRepository.save(new EnergyRecord("W3a", 659.390015, new Date(1488258000000l)));
        return "redirect:/monitor/energy";
    }
}
