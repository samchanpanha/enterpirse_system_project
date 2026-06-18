package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollPeriodRepository {
    PayrollPeriod save(PayrollPeriod p);
    Optional<PayrollPeriod> findById(UUID id);
}
