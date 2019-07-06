/*
 * Copyright (C) 2019 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DailyOverview;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @since 6 July 2019 4:23:27 PM (creation date)
 * @author : Peter Withers <peter-gthb@bambooradical.com>
 */
public class DayOfDataFileStore {

    public InputStream getOverviewStream() {
        return null;
    }

    public InputStream getDayOfDataStream(int yyyy, int MM, int dd) {
        return null;
    }

    public void loadDayOfData(Date startDate, Date endDate) {

    }

//    public void saveDailyOverview(DailyOverview dailyOverview) throws IOException {
//    }
    public DailyOverview loadDailyOverview() {
        return null;
    }
}
