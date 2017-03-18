/*
 * Copyright (C) 2016 Peter Withers
 */
package com.bambooradical.monitor.repository;

import com.bambooradical.monitor.model.EnergyRecord;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @created: 5/12/2016 22:31:32
 * @author : Peter Withers <peter@gthb-bambooradical.com>
 */
public interface EnergyRecordRepository extends JpaRepository<EnergyRecord, Long> {

    List<EnergyRecord> findByMeterLocationOrderByRecordDateAsc(String meterLocation, final Pageable pageable);
}
