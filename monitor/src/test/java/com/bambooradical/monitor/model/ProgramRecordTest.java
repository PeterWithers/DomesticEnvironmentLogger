/*
 * Copyright (C) 2018 Peter Withers
 */
package com.bambooradical.monitor.model;

import java.text.ParseException;
import static org.junit.Assert.*;

/**
 * @since Jan 3, 2018 20:46:11 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class ProgramRecordTest {

    @org.junit.Test
    public void testParseProgramRecord() throws ParseException {
        System.out.println("testParseProgramRecord");
        ProgramRecord instance1 = new ProgramRecord("location", 123456789, "abcdef", true);
        ProgramRecord instance2 = new ProgramRecord(instance1.getProgramCode());
        assertEquals(instance1.getColour(), instance2.getColour());
        assertEquals(instance1.getTime(), instance2.getTime());
        assertEquals(instance1.isTween(), instance2.isTween());
        assertEquals(instance1.getProgramCode(), instance2.getProgramCode());
    }
}

