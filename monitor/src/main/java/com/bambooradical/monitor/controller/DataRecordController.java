/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.repository.DataRecordRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private String getTemperatureArray(final String sensorLocation, final Pageable pageable) {
        StringBuilder temperatureBuilder = new StringBuilder();
        temperatureBuilder.append("[\n");
        for (final DataRecord record : dataRecordRepository.findByLocationStartsWithIgnoreCase(sensorLocation, pageable)) {
            final Float temperature = record.getTemperature();
            if (temperature != null) {
                temperatureBuilder.append("{ x: ");
                temperatureBuilder.append(record.getRecordDate().getTime());
                temperatureBuilder.append(", y: ");
                temperatureBuilder.append(temperature);
                temperatureBuilder.append("},");
            }
        }
        temperatureBuilder.append("]");
        return temperatureBuilder.toString();
    }

    private String getHumidityArray(final String sensorLocation, final Pageable pageable) {
        StringBuilder humidityBuilder = new StringBuilder();
        humidityBuilder.append("[\n");
        for (final DataRecord record : dataRecordRepository.findByLocationStartsWithIgnoreCase(sensorLocation, pageable)) {
            final Float humidity = record.getHumidity();
            if (humidity != null) {
                humidityBuilder.append("{ x: ");
                humidityBuilder.append(record.getRecordDate().getTime());
                humidityBuilder.append(", y: ");
                humidityBuilder.append(humidity);
                humidityBuilder.append("},");
            }
        }
        humidityBuilder.append("]");
        return humidityBuilder.toString();
    }

    private String getVoltageArray(final String sensorLocation, final Pageable pageable) {
        StringBuilder voltageBuilder = new StringBuilder();
        voltageBuilder.append("[\n");
        for (final DataRecord record : dataRecordRepository.findByLocationStartsWithIgnoreCase(sensorLocation, pageable)) {
            if (record.getVoltage() < 100) { //@todo: remove this < once the invalid data is removed
                voltageBuilder.append("{ x: ");
                voltageBuilder.append(record.getRecordDate().getTime());
                voltageBuilder.append(", y: ");
                voltageBuilder.append(record.getVoltage());
                voltageBuilder.append("},");
            }
        }
        voltageBuilder.append("]");
        return voltageBuilder.toString();
    }

    private String getVoltageData() {
        StringBuilder voltageBuilder = new StringBuilder();
        voltageBuilder.append("        {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n");
        for (final DataRecord record : dataRecordRepository.findAll()) {
            voltageBuilder.append("{ x: ");
            voltageBuilder.append(record.getRecordDate().getTime());
            voltageBuilder.append(", y: ");
            voltageBuilder.append(record.getVoltage());
            voltageBuilder.append("},");
        }
        voltageBuilder.append("      \n"
                + "      ]\n"
                + "    }");
        return voltageBuilder.toString();
    }

    private String getHumidityData() {
        StringBuilder humidityBuilder = new StringBuilder();
        humidityBuilder.append("        {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n");
        for (final DataRecord record : dataRecordRepository.findAll()) {
            humidityBuilder.append("{ x: ");
            humidityBuilder.append(record.getRecordDate().getTime());
            humidityBuilder.append(", y: ");
            humidityBuilder.append(record.getHumidity());
            humidityBuilder.append("},");
        }
        humidityBuilder.append("      \n"
                + "        ]\n"
                + "      }\n");
        return humidityBuilder.toString();
    }

    private String getTemperatureData() {
        StringBuilder temperatureBuilder = new StringBuilder();
        temperatureBuilder.append("      {        \n"
                + "        type: \"line\",\n"
                + "xValueType: \"dateTime\","
                + "        dataPoints: [\n");
        for (final DataRecord record : dataRecordRepository.findAll()) {
            temperatureBuilder.append("{ x: ");
            temperatureBuilder.append(record.getRecordDate().getTime());
            temperatureBuilder.append(", y: ");
            temperatureBuilder.append(record.getTemperature());
            temperatureBuilder.append("},");
        }
        temperatureBuilder.append("      \n"
                + "        ]\n"
                + "      }\n");
        return temperatureBuilder.toString();
    }

    @RequestMapping("/chart")
    public String getChart() {
        String chartJs = "$(document).ready(function(){\n"
                + "var chart = new CanvasJS.Chart(\"chartContainer\",\n"
                + "    {\n"
                + "      title:{\n"
                + "      text: \"Temperature and Humidity\"  \n"
                + "      },\n"
                + "      data: [\n"
                + getTemperatureData()
                + ","
                + getHumidityData()
                + ","
                + getVoltageData()
                + "]});\n"
                + "\n"
                + "    chart.render();"
                + "});";
        return "<head>"
                + "<script src=\"/js/jquery.min.js\"></script>"
                + "<script type=\"text/javascript\" src=\"/js/canvasjs.min.js\"></script>\n"
                + "<script type=\"text/javascript\">" + chartJs + "</script></head>"
                + "<a href=\"chart\">combined</a>"
                + "<br/>"
                + "<a href=\"charts\">separate</a>"
                + "<br/>"
                + "<a href=\"list\">list</a>"
                + "<br/>"
                + "<div id=\"chartContainer\"></div>"
                + "</body>";
    }

    @RequestMapping("/charts")
    public String getCharts(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false, defaultValue = "1000") int size) {
        long totalRecords = dataRecordRepository.count();
        if (size > totalRecords) {
            size = (int) totalRecords;
            page = 0;
        }
        if (page == null) {
            page = (int) (totalRecords / size);
        }
        if (page < 0) {
            page = 0;
        }
        while (size * (page) > totalRecords) {
            page--;
        }
        final Pageable pageable = new PageRequest(page, size);
        final String pagebleMenu = ""
                + "<a href=\"charts?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize() * 2 + "\">zoom-</a>&nbsp;"
                + "<a href=\"charts?page=" + pageable.getPageNumber() + "&size=" + pageable.getPageSize() / 2 + "\">zoom+</a>&nbsp;"
                + "<a href=\"charts?page=" + (pageable.getPageNumber() - 1) + "&size=" + pageable.getPageSize() + "\">prev</a>&nbsp;"
                + "<a href=\"charts?page=" + (pageable.getPageNumber() + 1) + "&size=" + pageable.getPageSize() + "\">next</a><br/>"
                + "";
        String chartJs = "$(document).ready(function(){\n"
                //                + "var temperatureChart = new CanvasJS.Chart(\"temperatureContainer\",\n"
                //                + "    {\n"
                //                + "      title:{\n"
                //                + "      text: \"Temperature\"  \n"
                //                + "      },\n"
                //                + "      data: [\n"
                //                + getTemperatureData()
                //                + "]});\n"
                //                + "\n"
                //                + "    temperatureChart.render();"
                //                + "var humidityChart = new CanvasJS.Chart(\"humidityContainer\",\n"
                //                + "    {\n"
                //                + "      title:{\n"
                //                + "      text: \"Humidity\"  \n"
                //                + "      },\n"
                //                + "      data: [\n"
                //                + getHumidityData()
                //                + "]});\n"
                //                + "\n"
                //                + "    humidityChart.render();"
                //                + "var voltageChart = new CanvasJS.Chart(\"voltageContainer\",\n"
                //                + "    {\n"
                //                + "      title:{\n"
                //                + "      text: \"Voltage\"  \n"
                //                + "      },\n"
                //                + "      data: [\n"
                //                + getVoltageData()
                //                + "]});\n"
                //                + "\n"
                //                + "    voltageChart.render();"
                + "\n"
                + "var temperatureContainer = $(\"#temperatureContainer\");\n"
                + "var temperatureChart = new Chart(temperatureContainer, {\n"
                + "    type: 'line',\n"
                + "    data: {\n"
                + "        datasets: ["
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'Temperature 2',\n"
                + "            backgroundColor: \"rgba(179,181,198,0.2)\",\n"
                + "            borderColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBackgroundColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(179,181,198,1)\","
                + "            data: "
                + getTemperatureArray("s", pageable)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'Temperature 1',\n"
                + "            backgroundColor: \"rgba(255,99,132,0.2)\",\n"
                + "            borderColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBackgroundColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(255,99,132,1)\","
                + "            data: "
                + getTemperatureArray("t", pageable)
                + "        }"
                + "]\n"
                + "    },\n"
                + "    options: {\n"
                + "        bezierCurve : false,\n"
                + "        responsive: true,\n"
                + "        maintainAspectRatio: true,\n"
                + "        scales: {\n"
                + "            xAxes: [{\n"
                + "                type: 'time',\n"
                + "                time: {\n"
                + "                    displayFormats: {\n"
                + "                        quarter: 'YYYY MMM D h:mm:ss'\n"
                + "                    }\n"
                + "                }\n"
                + "            }]"
                + "        }\n"
                + "    }"
                + "});"
                + "var humidityContainer = $(\"#humidityContainer\");\n"
                + "var humidityChart = new Chart(humidityContainer, {\n"
                + "    type: 'line',\n"
                + "    data: {\n"
                + "        datasets: ["
                + "{\n"
                + "            label: 'Humidity 2',\n"
                + "            backgroundColor: \"rgba(179,181,198,0.2)\",\n"
                + "            borderColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBackgroundColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(179,181,198,1)\","
                + "            data: "
                + getHumidityArray("s", pageable)
                + "        },"
                + "{\n"
                + "            label: 'Humidity 1',\n"
                + "            backgroundColor: \"rgba(255,99,132,0.2)\",\n"
                + "            borderColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBackgroundColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(255,99,132,1)\","
                + "            data: "
                + getHumidityArray("t", pageable)
                + "        }"
                + "]\n"
                + "    },\n"
                + "    options: {\n"
                + "        bezierCurve : false,\n"
                + "        responsive: true,\n"
                + "        maintainAspectRatio: true,\n"
                + "        scales: {\n"
                + "            xAxes: [{\n"
                + "                type: 'time',\n"
                + "                time: {\n"
                + "                    displayFormats: {\n"
                + "                        quarter: 'YYYY MMM D h:mm:ss'\n"
                + "                    }\n"
                + "                }\n"
                + "            }]"
                + "        }\n"
                + "    }"
                + "});"
                + "var voltageContainer = $(\"#voltageContainer\");\n"
                + "var voltageChart = new Chart(voltageContainer, {\n"
                + "    type: 'line',\n"
                + "    data: {\n"
                + "        datasets: ["
                + "{\n"
                + "            label: 'Voltage 2',\n"
                + "            backgroundColor: \"rgba(179,181,198,0.2)\",\n"
                + "            borderColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBackgroundColor: \"rgba(179,181,198,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(179,181,198,1)\","
                + "            data: "
                + getVoltageArray("s", pageable)
                + "        },"
                + "{\n"
                + "            label: 'Voltage 1',\n"
                + "            backgroundColor: \"rgba(255,99,132,0.2)\",\n"
                + "            borderColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBackgroundColor: \"rgba(255,99,132,1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(255,99,132,1)\","
                + "            data: "
                + getVoltageArray("t", pageable)
                + "        }"
                + "]\n"
                + "    },\n"
                + "    options: {\n"
                + "        bezierCurve : false,\n"
                + "        responsive: true,\n"
                + "        maintainAspectRatio: true,\n"
                + "        scales: {\n"
                + "            xAxes: [{\n"
                + "                type: 'time',\n"
                + "                time: {\n"
                + "                    displayFormats: {\n"
                + "                        quarter: 'YYYY MMM D h:mm:ss'\n"
                + "                    }\n"
                + "                }\n"
                + "            }]"
                + "        }\n"
                + "    }"
                + "});"
                + "});";
        return "<head>"
                + "<script src=\"/js/jquery.min.js\"></script>"
                + "<script src=\"/js/moment.js\"></script>"
                + "<script src=\"/js/Chart.min.js\"></script>"
                //                + "<script type=\"text/javascript\" src=\"/js/canvasjs.min.js\"></script>\n"
                + "<script type=\"text/javascript\">" + chartJs + "</script></head>"
                + "<a href=\"chart\">combined</a>"
                + "<br/>"
                + "<a href=\"charts\">separate</a>"
                + "<br/>"
                + "<a href=\"list\">list</a>"
                + "<br/>"
                + pagebleMenu
                + "<canvas id=\"temperatureContainer\" width=\"800px\" height=\"400px\"></canvas>"
                + "<br/>"
                + "<canvas id=\"humidityContainer\" width=\"800px\" height=\"400px\"></canvas>"
                + "<br/>"
                + "<canvas id=\"voltageContainer\" width=\"800px\" height=\"400px\"></canvas>"
                + "</body>";
    }
}
