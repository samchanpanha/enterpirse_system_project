package com.reportsystem.shared.tax;

import java.math.BigDecimal;
import java.util.List;

public class ToSCalculator implements TaxCalculator {

    private static final BigDecimal TOS_RATE = BigDecimal.valueOf(20);
    private static final String TAX_TYPE = "TOS";

    private static final List<TaxBracket> TOS_BRACKETS = List.of(
        new TaxBracket(BigDecimal.ZERO, new BigDecimal("1_300_000"), BigDecimal.ZERO, BigDecimal.ZERO),
        new TaxBracket(new BigDecimal("1_300_000"), new BigDecimal("2_000_000"), new BigDecimal("0.05"), BigDecimal.ZERO),
        new TaxBracket(new BigDecimal("2_000_000"), new BigDecimal("8_500_000"), new BigDecimal("0.10"), new BigDecimal("35_000")),
        new TaxBracket(new BigDecimal("8_500_000"), new BigDecimal("12_500_000"), new BigDecimal("0.15"), new BigDecimal("685_000")),
        new TaxBracket(new BigDecimal("12_500_000"), null, new BigDecimal("0.20"), new BigDecimal("1_285_000"))
    );

    @Override
    public BigDecimal calculate(BigDecimal taxableAmount) {
        return TaxCalculator.progressiveTax(taxableAmount, TOS_BRACKETS);
    }

    @Override
    public String getTaxType() {
        return TAX_TYPE;
    }

    @Override
    public BigDecimal getRate() {
        return TOS_RATE;
    }

    public List<TaxBracket> getBrackets() {
        return TOS_BRACKETS;
    }
}
