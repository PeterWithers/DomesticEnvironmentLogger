/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.ProgramRecord;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @since 6 July 2019 22:14 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class LightingRepository {

    private static final List<ProgramRecord> PROGRAM_RECORDS = new ArrayList<>();

    public List<ProgramRecord> getProgramRecords() throws ParseException {
        if (PROGRAM_RECORDS.isEmpty()) {
        }
        return PROGRAM_RECORDS;
    }

//    private String encodeHour(int milliseconds) {
//        return "ms_" + milliseconds;
    private String encodeHour(int hour) {
        return "hour_" + hour;
    }

    public void deleteProgram(final ProgramRecord programRecord) {
        PROGRAM_RECORDS.clear();
    }

    public void updateProgram(final ProgramRecord programRecord) {
        PROGRAM_RECORDS.clear();
    }

    public void addProgram(final ProgramRecord programRecord) {
        PROGRAM_RECORDS.clear();
    }

    public List<ProgramRecord> findProgram(final int millisOfDay) {
        try {
            getProgramRecords();
        } catch (ParseException parseException) {
            System.err.print(parseException.getMessage());
        }
        for (ProgramRecord programRecord : PROGRAM_RECORDS) {
            programRecord.setOffset(millisOfDay);
        }
        Collections.sort(PROGRAM_RECORDS);
        return PROGRAM_RECORDS;
    }
}
