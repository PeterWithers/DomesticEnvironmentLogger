/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @since Mar 26, 2019 22:22 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DailyOverviewTest {

    public DailyOverviewTest() {
    }
    private final String expectedJson = "{\"location\":{\"temperature\":{"
            + "\"2019-02\":{"
            + "\"avg\":[0.0,0.0,1.9,0.0,0.0,9.1],"
            + "\"max\":[0.0,0.0,1.9,0.0,0.0,9.1],"
            + "\"Q3\":[0.0,0.0,1.9,0.0,0.0,9.1],"
            + "\"Q2\":[0.0,0.0,1.9,0.0,0.0,9.1],"
            + "\"Q1\":[0.0,0.0,1.9,0.0,0.0,9.1],"
            + "\"min\":[0.0,0.0,1.9,0.0,0.0,9.1]},"
            + "\"2019-01\":{"
            + "\"avg\":[0.0,0.0,1.1],"
            + "\"max\":[0.0,0.0,1.1],"
            + "\"Q3\":[0.0,0.0,1.1],"
            + "\"Q2\":[0.0,0.0,1.1],"
            + "\"Q1\":[0.0,0.0,1.1],"
            + "\"min\":[0.0,0.0,1.1]},"
            + "\"2011-11\":{"
            + "\"avg\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1],"
            + "\"max\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1],"
            + "\"Q3\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1],"
            + "\"Q2\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1],"
            + "\"Q1\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1],"
            + "\"min\":[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,4.1]},"
            + "\"2019-03\":{"
            + "\"avg\":[6.1],"
            + "\"max\":[6.1],"
            + "\"Q3\":[6.1],"
            + "\"Q2\":[6.1],"
            + "\"Q1\":[6.1],"
            + "\"min\":[6.1]},"
            + "\"2011-01\":{"
            + "\"avg\":[0.0,0.0,2.1],"
            + "\"max\":[0.0,0.0,2.1],"
            + "\"Q3\":[0.0,0.0,2.1],"
            + "\"Q2\":[0.0,0.0,2.1],"
            + "\"Q1\":[0.0,0.0,2.1],"
            + "\"min\":[0.0,0.0,2.1]"
            + "}}}}";

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
        instance.addRecord("2016-06-06", new DataRecord(1.9F, 1F, null, null, null, null, null, null, null, null, "location", null, null));
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
        DailyOverview instance = new DailyOverview();
        instance.addRecord("2019-03-03", "location", "channel", 41F);
        instance.addRecord("2019-03-03", "location", "channel", 6F);
        instance.addRecord("2019-03-03", "location", "channel", 47F);
        instance.addRecord("2019-03-03", "location", "channel", 7F);
        instance.addRecord("2019-03-03", "location", "channel", 36F);
        instance.addRecord("2019-03-03", "location", "channel", 43F);
        instance.addRecord("2019-03-03", "location", "channel", 40F);
        instance.addRecord("2019-03-03", "location", "channel", 42F);
        instance.addRecord("2019-03-03", "location", "channel", 39F);
        instance.addRecord("2019-03-03", "location", "channel", 49F);
        instance.addRecord("2019-03-03", "location", "channel", 15F);
        instance.calculateSummaryData();
        DailyOverview.DaySummaryData result = instance.getDaySummaryData("2019-03-03", "location", "channel");
        assertEquals(33.1, result.avg[3 - 1], 0.5);
        assertEquals(6, result.min[3 - 1], 0);
        assertEquals(15, result.Q1[3 - 1], 0);
        assertEquals(40, result.Q2[3 - 1], 0);
        assertEquals(43, result.Q3[3 - 1], 0);
        assertEquals(49, result.max[3 - 1], 0);
//      Method 1	Method 2	Method 3
//Q1	15	25.5	20.25
//Q2	40	40	40
//Q3	43	42.5	42.75
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

    /**
     * Test of deserialisation, of class DailyOverview.
     */
    @Test
    public void testDeserialisationDaySummaryData() throws IOException {
        System.out.println("Deserialisation of DaySummaryData");
        ObjectMapper outputMapper = new ObjectMapper();
        outputMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Map<String, Map<String, Map<String, LinkedHashMap<String, float[]>>>> instanceMap = outputMapper.readValue(expectedJson, new TypeReference<Map<String, Map<String, Map<String, LinkedHashMap<String, float[]>>>>>() {
        });
        DailyOverview instance = new DailyOverview();
        instance.addData(instanceMap);
        String jsonString = outputMapper.writeValueAsString(instance);
        System.out.println(jsonString);
        assertEquals(expectedJson, jsonString);
    }
}
