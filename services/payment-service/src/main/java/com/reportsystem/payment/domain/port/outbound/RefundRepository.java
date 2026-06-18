package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository {
    Refund save(Refund r);
    List<Refund> findByTransactionId(UUID transactionId);
}
