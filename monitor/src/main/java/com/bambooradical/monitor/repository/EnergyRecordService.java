/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.EnergyRecord;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * @since Oct 14, 2017 1:31:49 PM (creation date)
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
public class EnergyRecordService {

    public List<EnergyRecord> findAll() {
        throw new UnsupportedOperationException();
    }

    public long count() {
        throw new UnsupportedOperationException();
    }

    public EnergyRecord save(EnergyRecord energyRecord) {
        throw new UnsupportedOperationException();
    }

    public void delete(EnergyRecord energyRecord) {
        throw new UnsupportedOperationException();
    }

    public List<EnergyRecord> findByMeterLocationOrderByRecordDateAsc(String meterLocation, final Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    public List<EnergyRecord> findByMeterLocationAndRecordDate(String meterLocation, Date recordDate) {
        throw new UnsupportedOperationException();
    }
}
