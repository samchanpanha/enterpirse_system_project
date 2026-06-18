package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "employees")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeeEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String code; @Column(name = "first_name") private String firstName;
    @Column(name = "last_name") private String lastName; @Column(name = "khmer_name") private String khmerName;
    private String gender; @Column(name = "birth_date") private LocalDate birthDate;
    private String phone; private String email; @Column(name = "id_type") private String idType;
    @Column(name = "id_number") private String idNumber;
    private String position; private String department;
    @Column(name = "hire_date") private LocalDate hireDate;
    @Column(name = "termination_date") private LocalDate terminationDate;
    private String status; @Column(name = "base_salary") private BigDecimal baseSalary;
    @Column(name = "bank_account") private String bankAccount;
    @Column(name = "bank_name") private String bankName;
    @Column(name = "nssf_number") private String nssfNumber;
    @Column(name = "tax_dependents") private int taxDependents;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}