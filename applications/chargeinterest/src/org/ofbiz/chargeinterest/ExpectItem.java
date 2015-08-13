package org.ofbiz.chargeinterest;

import java.math.BigDecimal;

public class ExpectItem {
	
	private BigDecimal insuranceAmount;
	private BigDecimal interestAmount;
	public BigDecimal getInsuranceAmount() {
		return insuranceAmount;
	}
	public void setInsuranceAmount(BigDecimal insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}
	public BigDecimal getInterestAmount() {
		return interestAmount;
	}
	public void setInterestAmount(BigDecimal interestAmount) {
		this.interestAmount = interestAmount;
	}
	
	

}
