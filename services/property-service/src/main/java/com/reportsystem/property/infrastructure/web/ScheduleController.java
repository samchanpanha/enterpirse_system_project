package com.reportsystem.property.infrastructure.web;

import com.reportsystem.property.domain.model.Schedule;
import com.reportsystem.property.domain.service.ScheduleService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<Schedule> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                           @RequestBody Map<String, String> body) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        Schedule schedule = scheduleService.createSchedule(
                UUID.fromString(body.get("tenantId")),
                bid,
                UUID.fromString(body.get("unitId")),
                body.get("title"), body.get("type"), body.get("intervalType"),
                Instant.parse(body.get("startTime")),
                body.get("endTime") != null ? Instant.parse(body.get("endTime")) : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getById(@PathVariable UUID id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<Schedule>> getByUnit(@PathVariable UUID unitId,
                                                     @RequestParam(required = false) Instant from,
                                                     @RequestParam(required = false) Instant to) {
        Instant start = from != null ? from : Instant.EPOCH;
        Instant end = to != null ? to : Instant.parse("2099-12-31T23:59:59Z");
        return ResponseEntity.ok(scheduleService.getSchedulesByUnit(unitId, start, end));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<Schedule>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByProperty(propertyId));
    }

    @GetMapping("/available/{unitId}")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(@PathVariable UUID unitId,
                                                                   @RequestParam Instant startTime,
                                                                   @RequestParam Instant endTime) {
        boolean available = scheduleService.isUnitAvailable(unitId, startTime, endTime);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Schedule schedule = scheduleService.updateSchedule(id, body.get("title"), body.get("description"),
                body.get("startTime") != null ? Instant.parse(body.get("startTime")) : null,
                body.get("endTime") != null ? Instant.parse(body.get("endTime")) : null);
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
