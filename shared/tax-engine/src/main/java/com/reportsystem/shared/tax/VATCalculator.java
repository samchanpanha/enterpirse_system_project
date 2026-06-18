package com.reportsystem.shared.tax;

import java.math.BigDecimal;

public class VATCalculator implements TaxCalculator {

    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(10);
    private static final String TAX_TYPE = "VAT";

    @Override
    public BigDecimal calculate(BigDecimal taxableAmount) {
        return TaxCalculator.percentageOf(taxableAmount, VAT_RATE);
    }

    @Override
    public String getTaxType() {
        return TAX_TYPE;
    }

    @Override
    public BigDecimal getRate() {
        return VAT_RATE;
    }

    public BigDecimal calculateInputVat(BigDecimal purchaseAmount) {
        return TaxCalculator.percentageOf(purchaseAmount, VAT_RATE);
    }

    public BigDecimal calculateOutputVat(BigDecimal salesAmount) {
        return TaxCalculator.percentageOf(salesAmount, VAT_RATE);
    }

    public BigDecimal calculateVatPayable(BigDecimal outputVat, BigDecimal inputVat) {
        return outputVat.subtract(inputVat);
    }
}
