/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since Mar 26, 2019 22:22 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DailyOverviewTest {

    public DailyOverviewTest() {
    }
    private final String expectedJson = "{\"location\": {\"temperature\":{\"2019-01\": {a:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F],p:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F],m:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F],q1:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F],q2:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F],q3:[1.1F,1.1F,1.1F,1.1F,1.1F,1.1F]}}}}";

    private DailyOverview getDailyOverview() {
        final DailyOverview dailyOverview = new DailyOverview();
        dailyOverview.addRecord("2019-01-03", "location", "temperature", 1.1F);
        dailyOverview.addRecord("2019-02-06", "location", "temperature", 9.1F);
        dailyOverview.addRecord("2019-03-01", "location", "temperature", 6.1F);
        dailyOverview.addRecord("2011-11-30", "location", "temperature", 4.1F);
        dailyOverview.addRecord("2011-01-03", "location", "temperature", 2.1F);
        dailyOverview.addRecord("2019-02-03", "location", "temperature", 1.9F);
        dailyOverview.addRecord("2019-02-03", "location", "temperature", 1.9F);
        return dailyOverview;
    }

    /**
     * Test of hasDate method, of class DailyOverview.
     */
    @Test
    public void testHasDate() {
        System.out.println("hasDate");
        DailyOverview instance = getDailyOverview();
        instance.calculateSummaryData();
        assertEquals(true, instance.hasDate("2019-01-03"));
        assertEquals(false, instance.hasDate("2019-01-02"));
        assertEquals(true, instance.hasDate("2011-11-30"));
        assertEquals(false, instance.hasDate("2011-11-02"));
    }

    /**
     * Test of addRecord method, of class DailyOverview.
     */
    @Test
    public void testAddRecord() {
        System.out.println("addRecord");
        DailyOverview instance = new DailyOverview();
        instance.addRecord("2016-06-06", new DataRecord(1.9F, 1F, null, "location", null, null));
        instance.calculateSummaryData();
        assertEquals(true, instance.hasDate("2016-06-06"));
        assertEquals(false, instance.hasDate("2011-11-02"));
        // todo: add internal checks here
    }

    /**
     * Test of getDaySummaryData method, of class DailyOverview.
     */
    @Test
    public void testGetDaySummaryData() {
        System.out.println("getDaySummaryData");
        DailyOverview instance = getDailyOverview();
        instance.calculateSummaryData();
        DailyOverview.DaySummaryData result = instance.getDaySummaryData("2019-01-03", "location", "temperature");
        assertEquals(0, result.average[3], 0);
        assertEquals(0, result.minimum[3], 0);
        assertEquals(0, result.lowerQuartile[3], 0);
        assertEquals(0, result.middleQuartile[3], 0);
        assertEquals(0, result.upperQuartile[3], 0);
        assertEquals(0, result.maximum[3], 0);
    }

    /**
     * Test of serialisation, of class DailyOverview.
     */
    @Test
    public void testSerialisationDaySummaryData() throws IOException {
        System.out.println("Serialisation of DaySummaryData");
        DailyOverview instance = getDailyOverview();
        instance.calculateSummaryData();
        ObjectMapper outputMapper = new ObjectMapper();
        outputMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = outputMapper.writeValueAsString(instance);
        System.out.println(jsonString);
        assertEquals(expectedJson, jsonString);
    }
}
