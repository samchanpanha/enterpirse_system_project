package com.reportsystem.inventory.infrastructure.config;

import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.domain.service.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import com.reportsystem.inventory.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryConfig {

    private final JpaSupplierRepository supplierRepo;
    private final JpaWarehouseRepository warehouseRepo;
    private final JpaProductCategoryRepository categoryRepo;
    private final JpaProductRepository productRepo;
    private final JpaStockEntryRepository entryRepo;
    private final JpaStockExitRepository exitRepo;
    private final JpaPurchaseOrderRepository poRepo;
    private final JpaPurchaseOrderItemRepository poiRepo;

    public InventoryConfig(JpaSupplierRepository sr, JpaWarehouseRepository wr,
                           JpaProductCategoryRepository cr, JpaProductRepository pr,
                           JpaStockEntryRepository er, JpaStockExitRepository exr,
                           JpaPurchaseOrderRepository por, JpaPurchaseOrderItemRepository poir) {
        supplierRepo = sr; warehouseRepo = wr; categoryRepo = cr; productRepo = pr;
        entryRepo = er; exitRepo = exr; poRepo = por; poiRepo = poir;
    }

    @Bean public SupplierRepository supplierRepository() { return new JpaSupplierAdapter(supplierRepo); }
    @Bean public WarehouseRepository warehouseRepository() { return new JpaWarehouseAdapter(warehouseRepo); }
    @Bean public ProductCategoryRepository productCategoryRepository() { return new JpaProductCategoryAdapter(categoryRepo); }
    @Bean public ProductRepository productRepository() { return new JpaProductAdapter(productRepo); }
    @Bean public StockEntryRepository stockEntryRepository() { return new JpaStockEntryAdapter(entryRepo); }
    @Bean public StockExitRepository stockExitRepository() { return new JpaStockExitAdapter(exitRepo); }
    @Bean public PurchaseOrderRepository purchaseOrderRepository() { return new JpaPurchaseOrderAdapter(poRepo); }
    @Bean public PurchaseOrderItemRepository purchaseOrderItemRepository() { return new JpaPurchaseOrderItemAdapter(poiRepo); }

    @Bean public ProductServiceImpl productService(ProductRepository pr) { return new ProductServiceImpl(pr); }
    @Bean public StockServiceImpl stockService(StockEntryRepository er, StockExitRepository exr, ProductRepository pr) { return new StockServiceImpl(er, exr, pr); }
    @Bean public SupplierServiceImpl supplierService(SupplierRepository sr) { return new SupplierServiceImpl(sr); }
    @Bean public PurchaseOrderServiceImpl purchaseOrderService(PurchaseOrderRepository por, PurchaseOrderItemRepository poir, StockEntryRepository er) { return new PurchaseOrderServiceImpl(por, poir, er); }
}
