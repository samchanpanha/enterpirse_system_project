package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.Customer;
import com.reportsystem.restaurant.domain.service.CustomerServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerServiceImpl customerService;
    public CustomerController(CustomerServiceImpl customerService) { this.customerService = customerService; }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                           @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                customerService.createCustomer(
                        UUID.fromString(b.get("tenantId")),
                        bid,
                        UUID.fromString(b.get("outletId")),
                        b.get("name"), b.get("phone")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable UUID id) {
        return customerService.getCustomerById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-phone/{tenantId}/{phone}")
    public ResponseEntity<Customer> findByPhone(@PathVariable UUID tenantId, @PathVariable String phone) {
        return customerService.findCustomerByPhone(tenantId, phone)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-outlet/{outletId}")
    public ResponseEntity<List<Customer>> getByOutlet(@PathVariable UUID outletId) {
        return ResponseEntity.ok(customerService.getCustomersByOutlet(outletId));
    }
}
