package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GatewayLogRepository {
    GatewayLog save(GatewayLog log);
}
