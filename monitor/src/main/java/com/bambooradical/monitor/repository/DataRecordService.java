/*
 * Copyright (C) 2017 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DataRecord;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * @since Oct 14, 2017 1:32:09 PM (creation date)
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
public class DataRecordService {

    public List<DataRecord> findAll() {
        throw new UnsupportedOperationException();
    }

    public long count() {
        throw new UnsupportedOperationException();
    }

    public DataRecord save(DataRecord dataRecord) {
        throw new UnsupportedOperationException();
    }

    public void delete(DataRecord dataRecord) {
        throw new UnsupportedOperationException();
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseOrderByRecordDateAsc(String location, final Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    public List<DataRecord> findByLocationAndRecordDate(String location, Date recordDate) {
        throw new UnsupportedOperationException();
    }

    public List<DataRecord> findByLocationStartsWithIgnoreCaseAndRecordDateBetweenOrderByRecordDateAsc(String location, Date startDate, Date endDate) {
        throw new UnsupportedOperationException();
    }
}
