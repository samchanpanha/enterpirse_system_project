package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceItemRepository {
    InvoiceItem save(InvoiceItem i);
    List<InvoiceItem> findByInvoiceId(UUID invoiceId);
}
