package com.reportsystem.shared.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface TaxCalculator {
    BigDecimal calculate(BigDecimal taxableAmount);
    String getTaxType();
    BigDecimal getRate();

    static BigDecimal percentageOf(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    static BigDecimal progressiveTax(BigDecimal amount, List<TaxBracket> brackets) {
        for (TaxBracket bracket : brackets) {
            if (bracket.contains(amount)) {
                return bracket.calculateTax(amount);
            }
        }
        return BigDecimal.ZERO;
    }
}
