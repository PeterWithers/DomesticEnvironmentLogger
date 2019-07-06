/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.repository;

import java.util.Date;
import org.springframework.stereotype.Service;

/**
 * @since 6 June 2019 19:29 PM (creation date)
 * @author Peter Withers <peter-gthb@bambooradical.com>
 */
@Service
public class MagnitudeRecordRepository {

    public void save(String magnitudes, String maxMsError, String location, Date recordDate) {
    }

    public String getDay(String location, String dateString) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.toString();
    }
}
