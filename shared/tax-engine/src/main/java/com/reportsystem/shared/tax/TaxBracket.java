package com.reportsystem.shared.tax;

import java.math.BigDecimal;

public record TaxBracket(
    BigDecimal lowerBound,
    BigDecimal upperBound,
    BigDecimal rate,
    BigDecimal baseTax
) {

    public boolean contains(BigDecimal amount) {
        return amount.compareTo(lowerBound) >= 0
            && (upperBound == null || amount.compareTo(upperBound) < 0);
    }

    public BigDecimal calculateTax(BigDecimal amount) {
        if (!contains(amount)) {
            return BigDecimal.ZERO;
        }
        BigDecimal taxable = amount.subtract(lowerBound);
        return taxable.multiply(rate).add(baseTax);
    }
}
