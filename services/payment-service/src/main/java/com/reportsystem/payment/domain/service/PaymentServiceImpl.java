package com.reportsystem.payment.domain.service;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.inbound.*;
import com.reportsystem.payment.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class PaymentServiceImpl implements PaymentService {
    private final TransactionRepository txRepo;
    private final RefundRepository refundRepo;
    private final GatewayLogRepository logRepo;
    private final PaymentGatewayRouter router;
    public PaymentServiceImpl(TransactionRepository txRepo, RefundRepository refundRepo, GatewayLogRepository logRepo, PaymentGatewayRouter router) {
        this.txRepo = txRepo; this.refundRepo = refundRepo; this.logRepo = logRepo; this.router = router;
    }
    @Override public Transaction createTransaction(UUID tenantId, UUID branchId, UUID invoiceId, BigDecimal amount, String currency, String gateway, String method, String customerName, String customerPhone, String sourceType, UUID sourceId) {
        return txRepo.save(Transaction.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).transactionId(txRepo.generateTransactionId(tenantId)).invoiceId(invoiceId).amount(amount).currency(currency).gateway(gateway).method(method).status("pending").customerName(customerName).customerPhone(customerPhone).sourceType(sourceType).sourceId(sourceId).metadata("{}").createdAt(Instant.now()).build());
    }
    @Override public Optional<Transaction> getTransactionById(UUID id) { return txRepo.findById(id); }
    @Override public List<Transaction> getTransactionsByTenant(UUID tenantId) { return txRepo.findByTenantId(tenantId); }
    @Override public List<Transaction> getTransactionsByTenantAndBranch(UUID tenantId, UUID branchId) { return txRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public Transaction processPayment(UUID transactionId) {
        Transaction tx = txRepo.findById(transactionId).orElseThrow();
        PaymentGatewayPort gatewayAdapter = router.route(tx.getGateway());
        PaymentGatewayPort.GatewayResult result = gatewayAdapter.process(tx);
        String status = result.success() ? "completed" : "failed";
        String gatewayRef = result.gatewayRef();
        logRepo.save(GatewayLog.builder().id(UUID.randomUUID()).transactionId(tx.getId()).branchId(tx.getBranchId()).gateway(tx.getGateway()).requestBody("{}").responseBody(result.rawResponse()).success(result.success()).errorMessage(result.errorMessage()).createdAt(Instant.now()).build());
        return txRepo.save(Transaction.builder().id(tx.getId()).tenantId(tx.getTenantId()).branchId(tx.getBranchId()).transactionId(tx.getTransactionId()).invoiceId(tx.getInvoiceId()).amount(tx.getAmount()).currency(tx.getCurrency()).gateway(tx.getGateway()).gatewayRef(gatewayRef).method(tx.getMethod()).status(status).customerName(tx.getCustomerName()).customerPhone(tx.getCustomerPhone()).sourceType(tx.getSourceType()).sourceId(tx.getSourceId()).metadata(tx.getMetadata()).paidAt(result.success() ? Instant.now() : null).createdAt(tx.getCreatedAt()).updatedAt(Instant.now()).build());
    }
    @Override public Transaction refundTransaction(UUID transactionId, BigDecimal amount, String reason, UUID createdBy) {
        Transaction tx = txRepo.findById(transactionId).orElseThrow();
        PaymentGatewayPort gatewayAdapter = router.route(tx.getGateway());
        PaymentGatewayPort.GatewayResult result = gatewayAdapter.refund(tx.getGatewayRef(), amount);
        refundRepo.save(Refund.builder().id(UUID.randomUUID()).tenantId(tx.getTenantId()).branchId(tx.getBranchId()).transactionId(transactionId).amount(amount).reason(reason).gatewayRef(result.gatewayRef()).status(result.success() ? "completed" : "failed").processedAt(result.success() ? Instant.now() : null).createdBy(createdBy).createdAt(Instant.now()).build());
        return txRepo.save(Transaction.builder().id(tx.getId()).tenantId(tx.getTenantId()).branchId(tx.getBranchId()).transactionId(tx.getTransactionId()).invoiceId(tx.getInvoiceId()).amount(tx.getAmount()).currency(tx.getCurrency()).gateway(tx.getGateway()).gatewayRef(tx.getGatewayRef()).method(tx.getMethod()).status("refunded").customerName(tx.getCustomerName()).customerPhone(tx.getCustomerPhone()).sourceType(tx.getSourceType()).sourceId(tx.getSourceId()).metadata(tx.getMetadata()).paidAt(tx.getPaidAt()).createdAt(tx.getCreatedAt()).updatedAt(Instant.now()).build());
    }
}