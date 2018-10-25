/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.model.EnergyRecord;
import com.bambooradical.monitor.repository.DataRecordRepository;
import com.bambooradical.monitor.repository.DataRecordService;
import com.bambooradical.monitor.repository.EnergyRecordRepository;
import com.bambooradical.monitor.repository.EnergyRecordService;
import com.bambooradical.monitor.repository.MagnitudeRecordService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @created: 19/11/2016 23:48:12
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@RestController
@RequestMapping("/monitor")
public class DataRecordController {

    @Autowired
    DataRecordRepository dataRecordRepository;
    @Autowired
    EnergyRecordRepository energyRecordRepository;
    @Autowired
    DataRecordService dataRecordService;
    @Autowired
    EnergyRecordService energyRecordService;
    @Autowired
    MagnitudeRecordService magnitudeRecordService;

    @RequestMapping("/add")
    public List<DataRecord> addRecord(
            @RequestParam(value = "temperature", required = false) Float[] temperature,
            @RequestParam(value = "humidity", required = false) Float[] humidity,
            @RequestParam(value = "voltage", required = true) Float voltage,
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "magnitudes", required = false, defaultValue = "") String magnitudes,
            @RequestParam(value = "maxMsError", required = false, defaultValue = "") String maxMsError,
            @RequestParam(value = "error", required = false) String error
    ) {
        List<DataRecord> returnRecords = new ArrayList<>();
        if (temperature != null) {
            for (int index = 0; index < temperature.length; index++) {
                final DataRecord dataRecord = new DataRecord(temperature[index], (humidity != null && humidity.length > index) ? humidity[index] : null, voltage, location + returnRecords.size(), error, new Date());
                dataRecordRepository.save(dataRecord);
                dataRecordService.save(dataRecord);
                returnRecords.add(dataRecord);
            }
        }
        if (humidity != null) {
            for (int index = returnRecords.size(); index < humidity.length; index++) {
                final DataRecord dataRecord = new DataRecord(null, humidity[index], voltage, location + returnRecords.size(), error, new Date());
                dataRecordRepository.save(dataRecord);
                dataRecordService.save(dataRecord);
                returnRecords.add(dataRecord);
            }
        }
        if (magnitudes != null && !magnitudes.isEmpty()) {
            magnitudeRecordService.save(magnitudes, maxMsError, location, new Date());
        }
        return returnRecords;
    }

    @RequestMapping("/addEnergy")
    public List<EnergyRecord> addEnergyRecord(
            @RequestParam(value = "meterLocation", required = true) String meterLocation,
            @RequestParam(value = "meterValue", required = true) double meterValue,
            @RequestParam(value = "readingDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date readingDate
    ) {
        final EnergyRecord energyRecord = new EnergyRecord(meterLocation, meterValue, readingDate);
        energyRecordRepository.save(energyRecord);
        energyRecordService.save(energyRecord);
        return energyRecordRepository.findAll();
    }

    /*    @RequestMapping("/addList")
    public long addRecordList(@RequestBody List<DataRecord> recordList) {
        dataRecordRepository.save(recordList);
        return dataRecordRepository.count();
    }

    @RequestMapping("/addEnergyList")
    public long addEnergyRecordList(@RequestBody List<EnergyRecord> energyRecordList) {
        energyRecordRepository.save(energyRecordList);
        return energyRecordRepository.count();
    }*/
    @RequestMapping("/migrateRecords")
    public String migrateRecords(@RequestParam(value = "start", required = false, defaultValue = "0") int startRecord, @RequestParam(value = "count", required = false, defaultValue = "10") int recordsToDo) {
        int addedCounter = 0;
        final PageRequest pageRequest = new PageRequest(startRecord, recordsToDo);
        final Page<DataRecord> recordsToMigrate = dataRecordRepository.findAll(pageRequest);
        for (DataRecord dataRecord : recordsToMigrate) {
            final List<DataRecord> existingRecords = dataRecordService.findByLocationAndRecordDate(dataRecord.getLocation(), dataRecord.getRecordDate());
            if (existingRecords.isEmpty()) {
                dataRecordService.save(dataRecord);
                addedCounter++;
            }
        }
        return "Added " + addedCounter + " DataRecords<br/><a href=\"?start=" + (startRecord + 1) + "&count=" + recordsToDo + "\">next</a>";
    }

    @RequestMapping("/migrateEnergy")
    public String migrateEnergy(@RequestParam(value = "start", required = false, defaultValue = "0") int startRecord, @RequestParam(value = "count", required = false, defaultValue = "10") int recordsToDo) {
        final PageRequest pageRequest = new PageRequest(startRecord, recordsToDo);
        final Page<EnergyRecord> recordsToMigrate = energyRecordRepository.findAll(pageRequest);
        for (EnergyRecord energyRecord : recordsToMigrate) {
            final List<EnergyRecord> existingEnergy = energyRecordService.findByMeterLocationAndRecordDate(energyRecord.getMeterLocation(), energyRecord.getRecordDate());
//            while (existingEnergy.size() > 1) {
//                energyRecordService.delete(existingEnergy.remove(0));
//            }
            if (existingEnergy.isEmpty()) {
                energyRecordService.save(energyRecord);
            }
        }
        return "Found " + energyRecordService.count() + " EnergyRecords out of " + energyRecordRepository.count() + " uploaded<br/><a href=\"?start=" + (startRecord + 1) + "&count=" + recordsToDo + "\">next</a>";
    }

    @RequestMapping("/addList")
    public String addRecordList(@RequestBody List<DataRecord> recordList, @RequestParam(value = "start", required = false, defaultValue = "0") int startRecord, @RequestParam(value = "count", required = false, defaultValue = "10") int recordsToDo) {
        for (long currentIndex = (startRecord * recordsToDo); currentIndex < recordList.size() && currentIndex < ((startRecord * recordsToDo) + recordsToDo); currentIndex++) {
            DataRecord dataRecord = recordList.get((int) currentIndex);
            //for (DataRecord dataRecord : recordList) {
            final List<DataRecord> existingRecords = dataRecordService.findByLocationAndRecordDate(dataRecord.getLocation(), dataRecord.getRecordDate());
            while (existingRecords.size() > 1) {
                dataRecordService.delete(existingRecords.remove(0));
            }
            if (existingRecords.isEmpty()) {
                dataRecordService.save(dataRecord);
            }
        }
//        return "Found " + dataRecordService.count() + " DataRecords";
        return "Found " + dataRecordService.count() + " DataRecords out of " + recordList.size() + " uploaded";
    }

    @RequestMapping("/addEnergyList")
    public String addEnergyRecordList(@RequestBody List<EnergyRecord> energyRecordList) {
        for (EnergyRecord energyRecord : energyRecordList) {
            final List<EnergyRecord> existingRecords = energyRecordService.findByMeterLocationAndRecordDate(energyRecord.getMeterLocation(), energyRecord.getRecordDate());
            while (existingRecords.size() > 1) {
                energyRecordService.delete(existingRecords.remove(0));
            }
            if (existingRecords.isEmpty()) {
                energyRecordService.save(energyRecord);
            }
        }
        return "Found " + energyRecordService.count() + " EnergyRecords";
    }

    @RequestMapping("/list")
    public List<DataRecord> listRecords() {
        return dataRecordRepository.findAll();
    }

    @RequestMapping(value = "/magnitudes", produces = {"application/JSON"})
    //@RequestMapping("/magnitudes")
    public String getMagnitudes(@RequestParam(value = "location", required = true) String location, @RequestParam(value = "date", required = false, defaultValue = "") String dateString) {
        return magnitudeRecordService.getDay(location, dateString);
    }

    @RequestMapping("/listEnergy")
    public List<EnergyRecord> listEnergyRecords() {
        return energyRecordRepository.findAll();
    }

    private String getTemperatureArray(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder temperatureBuilder = new StringBuilder();
        temperatureBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
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

    private String getHumidityArray(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder humidityBuilder = new StringBuilder();
        humidityBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
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

    private String getVoltageArray(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder voltageBuilder = new StringBuilder();
        voltageBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
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
        for (final DataRecord record : dataRecordService.findAll()) {
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
        for (final DataRecord record : dataRecordService.findAll()) {
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
        for (final DataRecord record : dataRecordService.findAll()) {
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

    @RequestMapping("/load")
    public String loadData(@RequestParam(value = "start", required = false, defaultValue = "0") int startDay, @RequestParam(value = "span", required = false, defaultValue = "14") int spanDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, startDay);
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -spanDays - 1);
        Date startDate = calendar.getTime();
        dataRecordService.loadDayOfData(startDate, endDate);
        return getCharts(startDay, spanDays);
    }

    @RequestMapping("/charts")
    public String getCharts(@RequestParam(value = "start", required = false, defaultValue = "0") int startDay, @RequestParam(value = "span", required = false, defaultValue = "14") int spanDays) {
//        long totalRecords = dataRecordRepository.count();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, startDay);
        Date endDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -spanDays - 1);
        Date startDate = calendar.getTime();

        final String pagebleMenu = ""
		+ "<a href=\"load?start=" + startDay + "&span=" + spanDays + "\">load current</a>&nbsp;&nbsp;&nbsp;"
                + "<a href=\"charts?start=" + startDay + "&span=" + (spanDays * 2) + "\">zoom-</a>&nbsp;"
                + "<a href=\"charts?start=" + startDay + "&span=" + (spanDays / 2) + "\">zoom+</a>&nbsp;"
                + "<a href=\"charts?start=" + (startDay - spanDays) + "&span=" + spanDays + "\">prev</a>&nbsp;"
                + "<a href=\"charts?start=" + (startDay + spanDays) + "&span=" + spanDays + "\">next</a><br/>"
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
                + getTemperatureArray("s", startDate, endDate)
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
                + getTemperatureArray("te", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'Temperature 3',\n"
                + "            backgroundColor: \"rgba(75, 192, 192, 0.2)\",\n"
                + "            borderColor: \"rgba(75, 192, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(75, 192, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(75, 192, 192, 1)\","
                + "            data: "
                + getTemperatureArray("th", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'Temperature 4',\n"
                + "            backgroundColor: \"rgba(75, 92, 192, 0.2)\",\n"
                + "            borderColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(75, 92, 192, 1)\","
                + "            data: "
                + getTemperatureArray("aquariumA0", startDate, endDate)
                + "        },"
	        + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'Aquarium',\n"
                + "            backgroundColor: \"rgba(75, 92, 192, 0.0)\",\n"
                + "            borderColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(75, 92, 192, 1)\","
                + "            data: "
                + getTemperatureArray("aquariumA1", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'rearwall0',\n"
                + "            backgroundColor: \"rgba(200,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(200,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200,100,200, 1)\","
                + "            data: "
                + getTemperatureArray("rearwall top floor0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'rearwall1',\n"
                + "            backgroundColor: \"rgba(180,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(180,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(180,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(180,100,200, 1)\","
                + "            data: "
                + getTemperatureArray("rearwall top floor1", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'rearwall2',\n"
                + "            backgroundColor: \"rgba(160,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(160,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(160,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(160,100,200, 1)\","
                + "            data: "
                + getTemperatureArray("rearwall top floor2", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'frontwall0',\n"
                + "            backgroundColor: \"rgba(200,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200,200,100, 1)\","
                + "            data: "
                + getTemperatureArray("frontwall top floor0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'frontwall1',\n"
                + "            backgroundColor: \"rgba(180,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(180,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(180,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(180,200,100, 1)\","
                + "            data: "
                + getTemperatureArray("frontwall top floor1", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'frontwall2',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(160,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(160,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(160,200,100, 1)\","
                + "            data: "
                + getTemperatureArray("frontwall top floor2", startDate, endDate)
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
                + "                        quarter: 'YYYY MMM D H:mm:ss'\n"
                + "                    },\n"
                + "                    tooltipFormat: 'YYYY MMM D H:mm:ss'\n"
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
                + getHumidityArray("s", startDate, endDate)
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
                + getHumidityArray("te", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'Humidity 3',\n"
                + "            backgroundColor: \"rgba(75, 192, 192, 0.2)\",\n"
                + "            borderColor: \"rgba(75, 192, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(75, 192, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(75, 192, 192, 1)\","
                + "            data: "
                + getHumidityArray("th", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'Humidity 4',\n"
                + "            backgroundColor: \"rgba(75, 92, 192, 0.2)\",\n"
                + "            borderColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(75, 92, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(75, 92, 192, 1)\","
                + "            data: "
                + getHumidityArray("aqu", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'rearwall0',\n"
                + "            backgroundColor: \"rgba(200,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(200,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200,100,200, 1)\","
                + "            data: "
                + getHumidityArray("rearwall top floor0", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'rearwall1',\n"
                + "            backgroundColor: \"rgba(180,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(180,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(180,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(180,100,200, 1)\","
                + "            data: "
                + getHumidityArray("rearwall top floor1", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'rearwall2',\n"
                + "            backgroundColor: \"rgba(160,100,200, 0.2)\",\n"
                + "            borderColor: \"rgba(160,100,200, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(160,100,200, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(160,100,200, 1)\","
                + "            data: "
                + getHumidityArray("rearwall top floor2", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'frontwall0',\n"
                + "            backgroundColor: \"rgba(200,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200,200,100, 1)\","
                + "            data: "
                + getHumidityArray("frontwall top floor0", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'frontwall1',\n"
                + "            backgroundColor: \"rgba(180,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(180,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(180,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(180,200,100, 1)\","
                + "            data: "
                + getHumidityArray("frontwall top floor1", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'frontwall2',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(160,200,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(160,200,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(160,200,100, 1)\","
                + "            data: "
                + getHumidityArray("frontwall top floor2", startDate, endDate)
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
                + "                        quarter: 'YYYY MMM D H:mm:ss'\n"
                + "                    },\n"
                + "                    tooltipFormat: 'YYYY MMM D H:mm:ss'\n"
                + "                }\n"
                + "            }]"
                + "        }\n"
                + "    }"
                + "});"
//                + "var voltageContainer = $(\"#voltageContainer\");\n"
//                + "var voltageChart = new Chart(voltageContainer, {\n"
//                + "    type: 'line',\n"
//                + "    data: {\n"
//                + "        datasets: ["
//                + "{\n"
//                + "            label: 'Voltage 2',\n"
//                + "            backgroundColor: \"rgba(179,181,198,0.2)\",\n"
//                + "            borderColor: \"rgba(179,181,198,1)\",\n"
//                + "            pointBackgroundColor: \"rgba(179,181,198,1)\",\n"
//                + "            pointBorderColor: \"#fff\",\n"
//                + "            pointHoverBackgroundColor: \"#fff\",\n"
//                + "            pointHoverBorderColor: \"rgba(179,181,198,1)\","
//                + "            data: "
//                + getVoltageArray("s", startDate, endDate)
//                + "        },"
//                + "{\n"
//                + "            label: 'Voltage 1',\n"
//                + "            backgroundColor: \"rgba(255,99,132,0.2)\",\n"
//                + "            borderColor: \"rgba(255,99,132,1)\",\n"
//                + "            pointBackgroundColor: \"rgba(255,99,132,1)\",\n"
//                + "            pointBorderColor: \"#fff\",\n"
//                + "            pointHoverBackgroundColor: \"#fff\",\n"
//                + "            pointHoverBorderColor: \"rgba(255,99,132,1)\","
//                + "            data: "
//                + getVoltageArray("te", startDate, endDate)
//                + "        },"
//                + "{\n"
//                + "            label: 'Voltage 3',\n"
//                + "            backgroundColor: \"rgba(75, 192, 192, 0.2)\",\n"
//                + "            borderColor: \"rgba(75, 192, 192, 1)\",\n"
//                + "            pointBackgroundColor: \"rgba(75, 192, 192, 1)\",\n"
//                + "            pointBorderColor: \"#fff\",\n"
//                + "            pointHoverBackgroundColor: \"#fff\",\n"
//                + "            pointHoverBorderColor: \"rgba(75, 192, 192, 1)\","
//                + "            data: "
//                + getVoltageArray("th", startDate, endDate)
//                + "        }"
//                + "]\n"
//                + "    },\n"
//                + "    options: {\n"
//                + "        bezierCurve : false,\n"
//                + "        responsive: true,\n"
//                + "        maintainAspectRatio: true,\n"
//                + "        scales: {\n"
//                + "            xAxes: [{\n"
//                + "                type: 'time',\n"
//                + "                time: {\n"
//                + "                    displayFormats: {\n"
//                + "                        quarter: 'YYYY MMM D H:mm:ss'\n"
//                + "                    },\n"
//                + "                    tooltipFormat: 'YYYY MMM D H:mm:ss'\n"
//                + "                }\n"
//                + "            }]"
//                + "        }\n"
//                + "    }"
//                + "});"
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
                + "<br/><a href=\"/spectrogram.html\">spectrogram</a>"
                + "<br/>"
                + "<a href=\"energy\">energy graph</a>"
                + "<br/>"
                + "<a href=\"energy?add=true\">add energy</a>"
                + "<br/>"
                + "<a href=\"import?startDate=20171116\">import weather</a>"
                + "<br/>"
                + "<a href=\"listEnergy\">energy JSON</a>"
                + "<br/>"
                + pagebleMenu
                + "<canvas id=\"temperatureContainer\" width=\"800px\" height=\"400px\"></canvas>"
                + "<br/>"
                + "<canvas id=\"humidityContainer\" width=\"800px\" height=\"400px\"></canvas>"
//                + "<br/>"
//                + "<canvas id=\"voltageContainer\" width=\"800px\" height=\"400px\"></canvas>"
                + "</body>";
    }
}
