package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Employee {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String code;
    private String firstName;
    private String lastName;
    private String khmerName;
    private String gender;
    private java.time.LocalDate birthDate;
    private String phone;
    private String email;
    private String idType;
    private String idNumber;
    private String position;
    private String department;
    private java.time.LocalDate hireDate;
    private java.time.LocalDate terminationDate;
    private String status;
    private BigDecimal baseSalary;
    private String bankAccount;
    private String bankName;
    private String nssfNumber;
    private int taxDependents;
    private final Instant createdAt;
    private Instant updatedAt;
}