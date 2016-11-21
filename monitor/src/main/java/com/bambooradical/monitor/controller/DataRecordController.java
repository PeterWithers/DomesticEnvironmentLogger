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
            voltageBuilder.append("{ x: ");
            voltageBuilder.append(record.getRecordDate().getTime());
            voltageBuilder.append(", y: ");
            voltageBuilder.append(record.getVoltage());
            voltageBuilder.append("},");
            humidityBuilder.append("{ x: ");
            humidityBuilder.append(record.getRecordDate().getTime());
            humidityBuilder.append(", y: ");
            humidityBuilder.append(record.getHumidity());
            humidityBuilder.append("},");
            temperatureBuilder.append("{ x: ");
            temperatureBuilder.append(record.getRecordDate().getTime());
            temperatureBuilder.append(", y: ");
            temperatureBuilder.append(record.getTemperature());
            temperatureBuilder.append("},");
        }
        String chartJs = "$(document).ready(function(){\n"
                + "var chart = new CanvasJS.Chart(\"chartContainer\",\n"
                + "    {\n"
                + "      title:{\n"
                + "      text: \"Temperature and Humidity\"  \n"
                + "      },\n"
                + "      data: [\n"
                + "      {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n"
                + temperatureBuilder.toString()
                + "      \n"
                + "        ]\n"
                + "      },\n"
                + "        {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n"
                + humidityBuilder.toString()
                + "      \n"
                + "        ]\n"
                + "      },\n"
                + "        {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n"
                + voltageBuilder.toString()
                + "      \n"
                + "        ]\n"
                + "      },\n"
                + "      ]\n"
                + "    });\n"
                + "\n"
                + "    chart.render();"
                + "});";
        return "<head>"
                + "<script src=\"webjars/jquery/jquery.min.js\"></script>"
                + "<script type=\"text/javascript\" src=\"js/canvasjs.min.js\"></script>\n"
                + "<script type=\"text/javascript\">" + chartJs + "</script></head>"
                + "<body>"
//                + "<a href=\"add?temperature=0&humidity=0&voltage=0&location=test\">add</a>"
                + "<div id=\"chartContainer\"></div>"
                + "</body>";
    }
}
