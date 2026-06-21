package com.reportsystem.restaurant.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PosCart {
    private UUID id;
    private UUID tenantId;
    private UUID branchId;
    private UUID outletId;
    private UUID tableId;
    private String customerName;
    private List<PosCartItem> items = new ArrayList<>();
    private BigDecimal discount = BigDecimal.ZERO;
    private String discountType;
    private BigDecimal serviceCharge = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private String status;
    private UUID createdBy;

    public PosCart(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.branchId = branchId;
        this.outletId = outletId;
        this.tableId = tableId;
        this.status = "active";
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getBranchId() { return branchId; }
    public UUID getOutletId() { return outletId; }
    public UUID getTableId() { return tableId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<PosCartItem> getItems() { return items; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getCreatedBy() { return createdBy; }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        BigDecimal subtotal = getSubtotal();
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        return afterDiscount.add(serviceCharge).add(taxAmount);
    }

    public void addItem(UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, String modifiers) {
        items.stream()
                .filter(i -> i.getMenuItemId().equals(menuItemId) && (modifiers == null ? i.getModifiers() == null : modifiers.equals(i.getModifiers())))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(i.getQuantity() + quantity),
                        () -> items.add(new PosCartItem(UUID.randomUUID(), menuItemId, itemName, unitPrice, quantity, modifiers))
                );
    }

    public void removeItem(UUID itemId) {
        items.removeIf(i -> i.getId().equals(itemId));
    }

    public void clearItems() {
        items.clear();
        discount = BigDecimal.ZERO;
        serviceCharge = BigDecimal.ZERO;
        taxAmount = BigDecimal.ZERO;
    }

    public static class PosCartItem {
        private UUID id;
        private UUID menuItemId;
        private String itemName;
        private BigDecimal unitPrice;
        private int quantity;
        private String modifiers;

        public PosCartItem(UUID id, UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, String modifiers) {
            this.id = id;
            this.menuItemId = menuItemId;
            this.itemName = itemName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.modifiers = modifiers;
        }

        public UUID getId() { return id; }
        public UUID getMenuItemId() { return menuItemId; }
        public String getItemName() { return itemName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getModifiers() { return modifiers; }
        public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }
}
