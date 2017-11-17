/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.repository.DataRecordService;
import com.google.cloud.Timestamp;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @since Nov 17, 2017 21:48:01 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RestController
@RequestMapping("/monitor")
public class KnmiDataController {

    @Autowired
    DataRecordService dataRecordService;

    @RequestMapping("/import")
    public String addEnergyRecord(@RequestParam(value = "startDate", required = true) /*@DateTimeFormat(pattern = "yyyyMMdd") Date*/ String startDate) throws IOException, NumberFormatException, ParseException {
        final StringBuffer response = new StringBuffer();
//        http://www.knmi.nl/kennis-en-datacentrum/achtergrond/data-ophalen-vanuit-een-script
        String serviceUrlString = "http://projects.knmi.nl/klimatologie/daggegevens/getdata_dag.cgi";
        URL obj = new URL(serviceUrlString);
        HttpURLConnection httpsURLConnection = (HttpURLConnection) obj.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        String postData = "start=" + startDate /*20171111*/ + "&vars=TN:TX:UN:UX&stns=275"; //&end=20170101
        httpsURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
        dataOutputStream.writeBytes(postData);
        dataOutputStream.flush();
        dataOutputStream.close();

        int responseCode = httpsURLConnection.getResponseCode();
        response.append("URL : ").append(serviceUrlString);
        response.append("<br/>");
        response.append("Post: ").append(postData);
        response.append("<br/>");
        response.append("Response: ").append(responseCode);
        response.append("<br/>");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
            response.append("<br/>");
            if (!inputLine.startsWith("#")) {
                final String location = "Deelen";
                final String[] splitLine = inputLine.split(",");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
                Date date = formatter.parse(splitLine[1] + "12");
                final Timestamp timestamp = Timestamp.of(date);
                final Integer stationId = Integer.valueOf(splitLine[0].trim());
                final Float temperatureMin = Float.valueOf(splitLine[2]) / 10;
                final Float humidityMin = Float.valueOf(splitLine[4]);
                final Float temperatureMax = Float.valueOf(splitLine[3]) / 10;
                final Float humidityMax = Float.valueOf(splitLine[5]);
                final String keyStringMin = location + "-" + stationId + "-" + splitLine[1] + "-min";
                final String keyStringMax = location + "-" + stationId + "-" + splitLine[1] + "-max";
                dataRecordService.save(location, timestamp, temperatureMin, humidityMin, keyStringMin);
                dataRecordService.save(location, timestamp, temperatureMax, humidityMax, keyStringMax);
            }
        }
        bufferedReader.close();
        return response.toString();
    }
}

