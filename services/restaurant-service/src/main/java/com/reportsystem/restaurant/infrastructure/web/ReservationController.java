package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.Reservation;
import com.reportsystem.restaurant.domain.service.ReservationServiceImpl;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationServiceImpl reservationService;
    public ReservationController(ReservationServiceImpl reservationService) { this.reservationService = reservationService; }

    @PostMapping
    public ResponseEntity<Reservation> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                               @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reservationService.createReservation(
                        UUID.fromString(b.get("tenantId")),
                        bid,
                        UUID.fromString(b.get("outletId")),
                        b.get("tableId") != null ? UUID.fromString(b.get("tableId")) : null,
                        b.get("guestName"), b.get("guestPhone"),
                        Integer.parseInt(b.get("guestCount")),
                        Instant.parse(b.get("reservationTime"))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> get(@PathVariable UUID id) {
        return reservationService.getReservationById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-outlet/{outletId}")
    public ResponseEntity<List<Reservation>> getByDate(@PathVariable UUID outletId, @RequestParam Instant date) {
        return ResponseEntity.ok(reservationService.getReservationsByOutlet(outletId, date));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(reservationService.updateStatus(id, b.get("status")));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }
}
