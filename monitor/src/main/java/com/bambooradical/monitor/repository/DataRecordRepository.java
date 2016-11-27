/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.DataRecord;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @created: 19/11/2016 22:31:32
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    List<DataRecord> findByLocationStartsWithIgnoreCase(String location, final Pageable pageable);
}
