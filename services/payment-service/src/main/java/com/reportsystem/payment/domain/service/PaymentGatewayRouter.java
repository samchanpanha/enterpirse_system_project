package com.reportsystem.payment.domain.service;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.inbound.*;
import com.reportsystem.payment.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class PaymentGatewayRouter {
    private final java.util.Map<String, PaymentGatewayPort> gateways;
    public PaymentGatewayRouter(java.util.Map<String, PaymentGatewayPort> gateways) { this.gateways = gateways; }
    public PaymentGatewayPort route(String gateway) {
        PaymentGatewayPort adapter = gateways.get(gateway.toLowerCase());
        if (adapter == null) throw new IllegalArgumentException("Unknown gateway: " + gateway);
        return adapter;
    }
}