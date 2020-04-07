/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.controller;

import com.bambooradical.monitor.model.DataRecord;
import com.bambooradical.monitor.repository.DataRecordService;
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

    public void save(final String location, final Date date, final Float temperature, final Float humidity, final String keyString) {
        dataRecordService.save(new DataRecord(temperature, humidity, null, null, null, null, null, null, null, 0.0f, location, keyString, date));
    }

    @RequestMapping("/import")
    public String addEnergyRecord(@RequestParam(value = "startDate", required = true) /*@DateTimeFormat(pattern = "yyyyMMdd") Date*/ String startDate) throws IOException, NumberFormatException, ParseException {
        final StringBuffer response = new StringBuffer();
//        http://www.knmi.nl/kennis-en-datacentrum/achtergrond/data-ophalen-vanuit-een-script
        String serviceUrlString = "http://projects.knmi.nl/klimatologie/daggegevens/getdata_dag.cgi";
        URL obj = new URL(serviceUrlString);
        HttpURLConnection httpsURLConnection = (HttpURLConnection) obj.openConnection();
        httpsURLConnection.setRequestMethod("POST");
        int startYear = Integer.parseInt(startDate.substring(0, 4));
        int startMonth = Integer.parseInt(startDate.substring(4, 6));
        final String endDate;
        if (startMonth > 11) {
            endDate = String.format("%04d%02d01", startYear + 1, 1);
        } else {
            endDate = String.format("%04d%02d01", startYear, startMonth + 1);
        }
        String postData = "start=" + startDate /*20171111*/ + "&end=" + endDate + "&vars=TN:TX:UN:UX:RH:EV24:FG:DDVEC&stns=275";
//        RH       = Daily precipitation amount (in 0.1 mm) (-1 for <0.05 mm); 
//        EV24     = Potential evapotranspiration (Makkink) (in 0.1 mm); 
//        FG       = Daily mean windspeed (in 0.1 m/s); 
//        DDVEC    = Vector mean wind direction in degrees (360=north, 90=east, 180=south, 270=west, 0=calm/variable); 
        httpsURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
        dataOutputStream.writeBytes(postData);
        dataOutputStream.flush();
        dataOutputStream.close();

        response.append("<br/><a href=\"/monitor/import?startDate=").append(endDate).append("\">import?startDate=").append(endDate).append("</a><br/>");
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
                final Integer stationId = Integer.valueOf(splitLine[0].trim());
                try {
                    final Float temperatureMin = Float.valueOf(splitLine[2]) / 10;
                    final Float humidityMin = Float.valueOf(splitLine[4]);
                    final String keyStringMin = location + "-" + stationId + "-" + splitLine[1] + "-min";
                    save(location, date, temperatureMin, humidityMin, keyStringMin);
                } catch (NumberFormatException exception) {
                    System.out.println(exception.getMessage());
                    response.append("Invalid temperatureMin/humidityMin<br/>");
                }
                try {
                    final Float temperatureMax = Float.valueOf(splitLine[3]) / 10;
                    final Float humidityMax = Float.valueOf(splitLine[5]);
                    final String keyStringMax = location + "-" + stationId + "-" + splitLine[1] + "-max";
                    save(location, date, temperatureMax, humidityMax, keyStringMax);
                } catch (NumberFormatException exception) {
                    System.out.println(exception.getMessage());
                    response.append("Invalid temperatureMax/humidityMax<br/>");
                }
                try {
                    final Float precipitation = Float.valueOf(splitLine[6]);
                    final Float evapotranspiration = Float.valueOf(splitLine[7]);
                    final String keyStringPrecipitation = "precipitation" + "-" + stationId + "-" + splitLine[1];
                    save("precipitationDeelen", date, precipitation, evapotranspiration, keyStringPrecipitation);
                } catch (NumberFormatException exception) {
                    System.out.println(exception.getMessage());
                    response.append("Invalid precipitation/evapotranspiration<br/>");
                }
                try {
                    final Float windspeed = Float.valueOf(splitLine[8]);
                    final Float meanWindDirection = Float.valueOf(splitLine[9]);
                    final String keyStringWindspeed = "windspeed" + "-" + stationId + "-" + splitLine[1];
                    save("windspeedDeelen", date, windspeed, meanWindDirection, keyStringWindspeed);
                } catch (NumberFormatException exception) {
                    System.out.println(exception.getMessage());
                    response.append("Invalid windspeed/meanWindDirection<br/>");
                }
            }
        }
        bufferedReader.close();
//        response.append("<br/><a href=\"/monitor/clear\">clear cache</a><br/>");
        response.append("<br/><a href=\"/monitor/energy\">energy viewer</a><br/>");
        //dataRecordService.clearCachedData(); // todo: it is better to update rather than clear this
        return response.toString();
    }
}
