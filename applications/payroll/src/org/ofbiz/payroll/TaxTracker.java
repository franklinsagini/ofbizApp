package org.ofbiz.payroll;

import java.math.BigDecimal;
/**
 * @author charles
 * Keep Tax Base and Percentages for later use
 * */
public class TaxTracker {
	private Long count;
	private BigDecimal taxBase;
	private BigDecimal taxPercent;
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public BigDecimal getTaxBase() {
		return taxBase;
	}
	public void setTaxBase(BigDecimal taxBase) {
		this.taxBase = taxBase;
	}
	public BigDecimal getTaxPercent() {
		return taxPercent;
	}
	public void setTaxPercent(BigDecimal taxPercent) {
		this.taxPercent = taxPercent;
	}
}
