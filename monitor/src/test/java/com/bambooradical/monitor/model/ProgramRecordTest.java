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
        ProgramRecord instance1 = new ProgramRecord("location", 123456, "abcdef", true);
        ProgramRecord instance2 = new ProgramRecord(instance1.getProgramCode());
        assertEquals(instance1.getColour(), instance2.getColour());
        assertEquals(instance1.getTime(), instance2.getTime());
        assertEquals(instance1.isTween(), instance2.isTween());
        assertEquals(instance1.getProgramCode(), instance2.getProgramCode());
        for (String exampleCode : new String[]{"ff0000T0000000;", "00ff00T0000100;", "0000ffT0000200;", "ff0000T0000300;", "000000:0000000;", "000000T0000100;", "000000:0000200;"}) {
            ProgramRecord instance3 = new ProgramRecord(exampleCode);
            ProgramRecord instance4 = new ProgramRecord(instance3.getProgramCode());
            assertEquals(instance3.getColour(), instance4.getColour());
            assertEquals(instance3.getTime(), instance4.getTime());
            assertEquals(instance3.isTween(), instance4.isTween());
            assertEquals(instance3.getProgramCode(), instance4.getProgramCode());
            assertEquals(exampleCode, instance4.getProgramCode());
        }
    }
}
