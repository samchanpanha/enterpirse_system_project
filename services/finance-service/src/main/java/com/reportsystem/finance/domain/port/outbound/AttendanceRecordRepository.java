package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository {
    AttendanceRecord save(AttendanceRecord a);
    List<AttendanceRecord> findByEmployeeIdAndDateBetween(UUID employeeId, java.time.LocalDate from, java.time.LocalDate to);
}
