package com.reportsystem.finance.infrastructure.persistence.adapter;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.outbound.*;
import com.reportsystem.finance.infrastructure.persistence.entity.*;
import com.reportsystem.finance.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaAttendanceRecordAdapter implements AttendanceRecordRepository {
    private final JpaAttendanceRecordRepository repo;
    public JpaAttendanceRecordAdapter(JpaAttendanceRecordRepository r) { repo = r; }
    @Override public AttendanceRecord save(AttendanceRecord a) {
        AttendanceRecordEntity e = new AttendanceRecordEntity(); e.setId(a.getId()); e.setTenantId(a.getTenantId());
        e.setEmployeeId(a.getEmployeeId()); e.setDate(a.getDate()); e.setClockIn(a.getClockIn()); e.setClockOut(a.getClockOut());
        e.setBreakStart(a.getBreakStart()); e.setBreakEnd(a.getBreakEnd()); e.setTotalHours(a.getTotalHours());
        e.setOvertimeHours(a.getOvertimeHours()); e.setStatus(a.getStatus()); e.setNotes(a.getNotes()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<AttendanceRecord> findByEmployeeIdAndDateBetween(UUID e, LocalDate f, LocalDate t) {
        return repo.findByEmployeeIdAndDateBetween(e, f, t).stream().map(this::toDomain).toList(); }
    private AttendanceRecord toDomain(AttendanceRecordEntity e) { return AttendanceRecord.builder().id(e.getId()).tenantId(e.getTenantId()).employeeId(e.getEmployeeId()).date(e.getDate()).clockIn(e.getClockIn()).clockOut(e.getClockOut()).breakStart(e.getBreakStart()).breakEnd(e.getBreakEnd()).totalHours(e.getTotalHours()).overtimeHours(e.getOvertimeHours()).status(e.getStatus()).notes(e.getNotes()).createdAt(e.getCreatedAt()).build(); }
}