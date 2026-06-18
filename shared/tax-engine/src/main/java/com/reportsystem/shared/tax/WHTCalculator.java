package com.reportsystem.shared.tax;

import java.math.BigDecimal;

public class WHTCalculator implements TaxCalculator {

    private static final BigDecimal WHT_RATE = BigDecimal.valueOf(15);
    private static final String TAX_TYPE = "WHT";

    public enum WhtCategory {
        RENT(BigDecimal.valueOf(15)),
        INTEREST(BigDecimal.valueOf(15)),
        ROYALTIES(BigDecimal.valueOf(15)),
        MANAGEMENT_FEES(BigDecimal.valueOf(10)),
        GOODS_SERVICES_NON_RESIDENT(BigDecimal.valueOf(2)),
        CONSTRUCTION(BigDecimal.valueOf(2));

        private final BigDecimal rate;

        WhtCategory(BigDecimal rate) {
            this.rate = rate;
        }

        public BigDecimal getRate() {
            return rate;
        }
    }

    @Override
    public BigDecimal calculate(BigDecimal taxableAmount) {
        return TaxCalculator.percentageOf(taxableAmount, WHT_RATE);
    }

    public BigDecimal calculate(BigDecimal taxableAmount, WhtCategory category) {
        return TaxCalculator.percentageOf(taxableAmount, category.getRate());
    }

    @Override
    public String getTaxType() {
        return TAX_TYPE;
    }

    @Override
    public BigDecimal getRate() {
        return WHT_RATE;
    }
}
