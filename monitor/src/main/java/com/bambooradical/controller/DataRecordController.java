/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.repository.DataRecordRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @created: 19/11/2016 23:48:12
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
@Controller
public class DataRecordController {

    @Autowired
    DataRecordRepository dataRecordRepository;

    @RequestMapping("/add")
    public @ResponseBody
    DataRecord addRecord(
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
    public @ResponseBody
    List<DataRecord> listRecords() {
        return dataRecordRepository.findAll();
    }
}
