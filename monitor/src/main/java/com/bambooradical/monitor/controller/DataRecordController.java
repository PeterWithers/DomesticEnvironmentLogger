/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.model.EnergyRecord;
import com.bambooradical.monitor.model.RadioData;
import com.bambooradical.monitor.repository.DataRecordService;
import com.bambooradical.monitor.repository.DayOfDataGcsFileStore;
import com.bambooradical.monitor.repository.EnergyRecordService;
import com.bambooradical.monitor.repository.RadioDataService;
import com.bambooradical.monitor.repository.MagnitudeRecordService;
import com.google.cloud.Timestamp;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * @created: 19/11/2016 23:48:12
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@RestController
@RequestMapping("/monitor")
public class DataRecordController {

//    @Autowired
//    DataRecordRepository dataRecordRepository;
//    @Autowired
//    EnergyRecordRepository energyRecordRepository;
    @Autowired
    DataRecordService dataRecordService;
    @Autowired
    EnergyRecordService energyRecordService;
//    @Autowired
//    RadioDataRepository radioDataRepository;
    @Autowired
    RadioDataService radioDataService;
    @Autowired
    MagnitudeRecordService magnitudeRecordService;
    //@Autowired
    //DayOfDataFileStore dataFileStore;
    @Autowired
    DayOfDataGcsFileStore dataFileStore;

    @RequestMapping("/add")
    public List<DataRecord> addRecord(
            @RequestParam(value = "temperature", required = false) Float[] temperature,
            @RequestParam(value = "humidity", required = false) Float[] humidity,
            @RequestParam(value = "voltage", required = false) Float voltage,
            @RequestParam(value = "tvocMin", required = false) Float tvocMin,
            @RequestParam(value = "tvocAvg", required = false) Float tvocAvg,
            @RequestParam(value = "tvocMax", required = false) Float tvocMax,
            @RequestParam(value = "co2Min", required = false) Float co2Min,
            @RequestParam(value = "co2Avg", required = false) Float co2Avg,
            @RequestParam(value = "co2Max", required = false) Float co2Max,
            @RequestParam(value = "dustAvg", required = false) Float dustAvg,
            @RequestParam(value = "dustQ1", required = false) Float dustQ1,
            @RequestParam(value = "dustQ2", required = false) Float dustQ2,
            @RequestParam(value = "dustQ3", required = false) Float dustQ3,
            @RequestParam(value = "dustOutliers", required = false) Float dustOutliers,
            @RequestParam(value = "hPaMin", required = false) Float hPaMin,
            @RequestParam(value = "hPaAvg", required = false) Float hPaAvg,
            @RequestParam(value = "hPaMax", required = false) Float hPaMax,
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "magnitudes", required = false, defaultValue = "") String magnitudes,
            @RequestParam(value = "maxMsError", required = false, defaultValue = "") String maxMsError,
            @RequestParam(value = "error", required = false) String error
    ) {
        List<DataRecord> returnRecords = new ArrayList<>();
        if (temperature != null) {
            for (int index = 0; index < temperature.length; index++) {
                final DataRecord dataRecord = new DataRecord(temperature[index], (humidity != null && humidity.length > index) ? humidity[index] : null, tvocAvg, co2Avg, null, null, null, null, null, hPaAvg, voltage, location + returnRecords.size(), error, new Date());
//                dataRecordRepository.save(dataRecord);
                dataRecordService.save(dataRecord);
                returnRecords.add(dataRecord);
            }
        }
        if (humidity != null) {
            for (int index = returnRecords.size(); index < humidity.length; index++) {
                final DataRecord dataRecord = new DataRecord(null, humidity[index], null, null, null, null, null, null, null, null, voltage, location + returnRecords.size(), error, new Date());
//                dataRecordRepository.save(dataRecord);
                dataRecordService.save(dataRecord);
                returnRecords.add(dataRecord);
            }
        }
//        if (tvocMin != null) {
//            final DataRecord dataMinRecord = new DataRecord(null, null, tvocMin, co2Min, null, null, null, null, null, paMin, null, location + "Min", error, new Date());
        ////                dataRecordRepository.save(dataRecord);
//            dataRecordService.save(dataMinRecord);
//            returnRecords.add(dataMinRecord);
//            final DataRecord dataAvgRecord = new DataRecord(null, null, tvocAvg, co2Avg, null, null, null, null, null, paAvg, null, location + "Avg", error, new Date());
////                dataRecordRepository.save(dataRecord);
//            dataRecordService.save(dataAvgRecord);
//            returnRecords.add(dataAvgRecord);
//            final DataRecord dataMaxRecord = new DataRecord(null, null, tvocMax, co2Max, null, null, null, null, null, paMax, null, location + "Max", error, new Date());
////                dataRecordRepository.save(dataRecord);
//            dataRecordService.save(dataMaxRecord);
//            returnRecords.add(dataMaxRecord);
//        }
        if (dustAvg != null) {
            final DataRecord dustRecord = new DataRecord(null, null, null, null, dustAvg, dustQ1, dustQ2, dustQ3, dustOutliers, null, null, location, error, new Date());
//                dataRecordRepository.save(dataRecord);
            dataRecordService.save(dustRecord);
            returnRecords.add(dustRecord);
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
//        energyRecordRepository.save(energyRecord);
        energyRecordService.save(energyRecord);
        return energyRecordService.findAll();
    }

    @RequestMapping("/addRadioData")
    public List<RadioData> addRadioData(
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value = "dataValues", required = true) String[] dataValuesArray
    ) {
        final List<RadioData> radioDataRecords = new ArrayList<>();
        for (String dataValues : dataValuesArray) {
            final RadioData radioData = new RadioData(location, dataValues, new Date());
//            radioDataRepository.save(radioData);
            radioDataService.save(radioData);
            radioDataRecords.add(radioData);
        }
        return radioDataRecords;
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
 /*@RequestMapping("/migrateRecords")
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
    }*/

 /*@RequestMapping("/migrateEnergy")
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
    }*/
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

    /*@RequestMapping("/list")
    public List<DataRecord> listRecords() {
        return dataRecordRepository.findAll();
    }*/
    @RequestMapping(value = "/magnitudes", produces = {"application/JSON"})
    //@RequestMapping("/magnitudes")
    public String getMagnitudes(@RequestParam(value = "location", required = true) String location, @RequestParam(value = "date", required = false, defaultValue = "") String dateString) {
        return magnitudeRecordService.getDay(location, dateString);
    }

    @RequestMapping("/listEnergy")
    public List<EnergyRecord> listEnergyRecords() {
        return energyRecordService.findAll();
    }

    @RequestMapping("/listRadioData")
    public List<RadioData> listRadioData(@RequestParam(value = "location", required = true) String location, @RequestParam(value = "date", required = false, defaultValue = "") String dateString) {
        return radioDataService.findByLocationOrderByRecordDateAsc(location, null);
//        return radioDataRepository.findAll();
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

    private String getDustAvgArray(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder dustBuilder = new StringBuilder();
        dustBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
            final Float dust = record.getDustAvg();
            if (dust != null) {
                dustBuilder.append("{ x: ");
                dustBuilder.append(record.getRecordDate().getTime());
                dustBuilder.append(", y: ");
                dustBuilder.append(dust);
                dustBuilder.append("},");
            }
        }
        dustBuilder.append("]");
        return dustBuilder.toString();
    }

    private String getDustQ1Array(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder dustBuilder = new StringBuilder();
        dustBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
            final Float dust = record.getDustQ1();
            if (dust != null) {
                dustBuilder.append("{ x: ");
                dustBuilder.append(record.getRecordDate().getTime());
                dustBuilder.append(", y: ");
                dustBuilder.append(dust);
                dustBuilder.append("},");
            }
        }
        dustBuilder.append("]");
        return dustBuilder.toString();
    }

    private String getDustQ2Array(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder dustBuilder = new StringBuilder();
        dustBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
            final Float dust = record.getDustQ2();
            if (dust != null) {
                dustBuilder.append("{ x: ");
                dustBuilder.append(record.getRecordDate().getTime());
                dustBuilder.append(", y: ");
                dustBuilder.append(dust);
                dustBuilder.append("},");
            }
        }
        dustBuilder.append("]");
        return dustBuilder.toString();
    }

    private String getDustQ3Array(final String sensorLocation, Date startDate, Date endDate) {
        StringBuilder dustBuilder = new StringBuilder();
        dustBuilder.append("[\n");
        for (final DataRecord record : dataRecordService.findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(sensorLocation, startDate, endDate)) {
            final Float dust = record.getDustQ3();
            if (dust != null) {
                dustBuilder.append("{ x: ");
                dustBuilder.append(record.getRecordDate().getTime());
                dustBuilder.append(", y: ");
                dustBuilder.append(dust);
                dustBuilder.append("},");
            }
        }
        dustBuilder.append("]");
        return dustBuilder.toString();
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
    public String loadData(@RequestParam(value = "start", required = false, defaultValue = "0") int startDay, @RequestParam(value = "span", required = false, defaultValue = "365") int spanDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, startDay);
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -spanDays - 1);
        Date startDate = calendar.getTime();
        dataFileStore.loadDayOfData(startDate, endDate);
        return getCharts(startDay, spanDays);
    }

    @RequestMapping("/overview")
    public void getOverview(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader("Content-Transfer-Encoding", "text");
        try (OutputStream outputStream = response.getOutputStream()) {
            final InputStream overviewStream = dataFileStore.getOverviewStream();
            byte[] bytes = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = overviewStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, bytesRead);
            }
        }
    }

    @GetMapping(value = "/DayOfData{yyyy}-{MM}-{dd}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> getDayOfData(@PathVariable int yyyy, @PathVariable int MM, @PathVariable int dd) throws IOException {
        InputStream dataStream = dataFileStore.getDayOfDataStream(yyyy, MM, dd);
        StreamingResponseBody stream = outputStream -> {
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(outputStream); InputStream in = dataStream) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, bytesRead);
                }
                gzipOut.finish();
            }
        };
        return ResponseEntity.ok()
                .header("Content-Encoding", "gzip")
                .header("Content-Transfer-Encoding", "binary")
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }

    @GetMapping(value = "/DayOfEntities{yyyy}-{MM}-{dd}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> getDayOfEntities(
            @PathVariable int yyyy,
            @PathVariable int MM,
            @PathVariable int dd) {

        try {
            // Construct UTC start and end of day
            ZonedDateTime startUtc = ZonedDateTime.of(yyyy, MM, dd, 0, 0, 0, 0, ZoneOffset.UTC);
            ZonedDateTime endUtc = startUtc.plusDays(1);

            Timestamp startTs = Timestamp.of(Date.from(startUtc.toInstant()));
            Timestamp endTs = Timestamp.of(Date.from(endUtc.toInstant()));

            StreamingResponseBody stream = dataFileStore.loadDayOfEntities(startTs, endTs);

            return ResponseEntity.ok()
                    .header("Content-Encoding", "gzip")
                    .header("Content-Transfer-Encoding", "binary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stream);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(outputStream -> {
                outputStream.write(("{\"error\":\"Failed to fetch entities\"}").getBytes());
            });
        }
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
                + "            label: 'AquariumA',\n"
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
                + "            label: 'upstairs_bathroom0',\n"
                + "            backgroundColor: \"rgba(255, 99, 193, 0.0)\",\n"
                + "            borderColor: \"rgba(255, 99, 193, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(255, 99, 193, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(255, 99, 193, 1)\","
                + "            data: "
                + getTemperatureArray("upstairs_bathroom0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'AquariumB',\n"
                + "            backgroundColor: \"rgba(95, 52, 192, 0.0)\",\n"
                + "            borderColor: \"rgba(95, 52, 192, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(95, 52, 192, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(95, 52, 192, 1)\","
                + "            data: "
                + getTemperatureArray("aquariumB0", startDate, endDate)
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
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'kp56_shed',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getTemperatureArray("kp56_shed0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'kp56_bathroom',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getTemperatureArray("kp56_bathroom0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'ground_ksp56_vibration',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getTemperatureArray("ksp56_vibration0", startDate, endDate)
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
                + "        },"
                + "{\n"
                + "            label: 'dust avg',\n"
                + "            backgroundColor: \"rgba(211,158,100, 0.2)\",\n"
                + "            borderColor: \"rgba(211,158,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(211,158,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(211,158,100, 1)\","
                + "            data: "
                + getDustAvgArray("air_monitor_01", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'upstairs_bathroom0',\n"
                + "            backgroundColor: \"rgba(255, 99, 193, 0.0)\",\n"
                + "            borderColor: \"rgba(255, 99, 193, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(255, 99, 193, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(255, 99, 193, 1)\","
                + "            data: "
                + getHumidityArray("upstairs_bathroom0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'kp56_shed',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getHumidityArray("kp56_shed0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'kp56_bathroom',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getHumidityArray("kp56_bathroom0", startDate, endDate)
                + "        },"
                + "{\n"
                //                + "        lineTension: 0\n"
                + "            label: 'ground_ksp56_vibration',\n"
                + "            backgroundColor: \"rgba(160,200,100, 0.2)\",\n"
                + "            borderColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(200, 100, 168, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(200, 100, 168, 1)\","
                + "            data: "
                + getHumidityArray("ksp56_vibration0", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'dust Q1',\n"
                + "            backgroundColor: \"rgba(211,138,100, 0.2)\",\n"
                + "            borderColor: \"rgba(211,138,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(211,138,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(211,158,100, 1)\","
                + "            data: "
                + getDustQ1Array("air_monitor_01", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'dust Q2',\n"
                + "            backgroundColor: \"rgba(211,118,100, 0.2)\",\n"
                + "            borderColor: \"rgba(211,118,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(211,118,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(211,158,100, 1)\","
                + "            data: "
                + getDustQ2Array("air_monitor_01", startDate, endDate)
                + "        },"
                + "{\n"
                + "            label: 'dust Q3',\n"
                + "            backgroundColor: \"rgba(211,98,100, 0.2)\",\n"
                + "            borderColor: \"rgba(211,98,100, 1)\",\n"
                + "            pointBackgroundColor: \"rgba(211,98,100, 1)\",\n"
                + "            pointBorderColor: \"#fff\",\n"
                + "            pointHoverBackgroundColor: \"#fff\",\n"
                + "            pointHoverBorderColor: \"rgba(211,158,200, 1)\","
                + "            data: "
                + getDustQ3Array("air_monitor_01", startDate, endDate)
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
                + "<br/><a href=\"/overview.html\">overview</a>"
                + "<br/>"
                + "<br/><a href=\"/spectrogram.html\">spectrogram</a>"
                + "<br/>"
                + "<br/><a href=\"/radiodata.html\">radio</a>"
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
