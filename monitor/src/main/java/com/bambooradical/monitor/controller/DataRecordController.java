/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.repository.DataRecordRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @created: 19/11/2016 23:48:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@RestController
//@RequestMapping("/monitor")
public class DataRecordController {

    @Autowired
    DataRecordRepository dataRecordRepository;

    @RequestMapping("/add")
    public DataRecord addRecord(
            @RequestParam(value = "temperature", required = true) Float temperature,
            @RequestParam(value = "humidity", required = true) Float humidity,
            @RequestParam(value = "voltage", required = true) Float voltage,
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "error", required = false) String error
    ) {
        final DataRecord dataRecord = new DataRecord(temperature, humidity, voltage, location, error, new Date());
        dataRecordRepository.save(dataRecord);
        return dataRecord;
    }

    @RequestMapping("/list")
    public List<DataRecord> listRecords() {
        return dataRecordRepository.findAll();
    }

    @RequestMapping("/chart")
    public String getHtml() {
        StringBuilder voltageBuilder = new StringBuilder();
        StringBuilder humidityBuilder = new StringBuilder();
        StringBuilder temperatureBuilder = new StringBuilder();
        for (final DataRecord record : dataRecordRepository.findAll()) {
            voltageBuilder.append("[\'");
            voltageBuilder.append(record.getRecordDate().toString());
            voltageBuilder.append("\',");
            voltageBuilder.append(record.getVoltage());
            voltageBuilder.append("],");
            humidityBuilder.append("[\'");
            humidityBuilder.append(record.getHumidity().toString());
            humidityBuilder.append("\',");
            humidityBuilder.append(record.getVoltage());
            humidityBuilder.append("],");
            temperatureBuilder.append("[\'");
            temperatureBuilder.append(record.getTemperature().toString());
            temperatureBuilder.append("\',");
            temperatureBuilder.append(record.getVoltage());
            temperatureBuilder.append("],");
        }
        String chartJs = "$(document).ready(function(){\n"
                + "  var plot1 = $.jqplot('chart1', [["
                + temperatureBuilder.toString()
                + "],["
                + humidityBuilder.toString()
                + "],["
                + voltageBuilder.toString()
                + "]], {\n"
                + "    title:'Temperature and Humidity',\n"
                + "    axes:{\n"
                + "        xaxis:{\n"
                + "            renderer:$.jqplot.DateAxisRenderer\n"
                + "        }\n"
                + "    },\n"
                + "    series:[{lineWidth:4, markerOptions:{style:'square'}}]\n"
                + "  });\n"
                + "});";
        return "<head>"
                + "<script src=\"webjars/jquery/jquery.min.js\"></script>"
                + "<script type=\"text/javascript\" src=\"js/jquery.jqplot.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"js/jqplot.dateAxisRenderer.js\"></script>\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"js/jquery.jqplot.css\" />"
                + "<script type=\"text/javascript\">" + chartJs + "</script></head>"
                + "<body><a href=\"add?temperature=0&humidity=0&voltage=0&location=test\">add</a>"
                + "<div id=\"chart1\"></div>"
                + "</body>";
    }
}
