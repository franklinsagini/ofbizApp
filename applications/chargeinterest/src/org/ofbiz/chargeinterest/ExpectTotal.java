package org.ofbiz.chargeinterest;

import java.math.BigDecimal;

public class ExpectTotal {
	
	private BigDecimal interestTotalAmount;
	private BigDecimal insuranceTotalAmount;
	public BigDecimal getInterestTotalAmount() {
		return interestTotalAmount;
	}
	public void setInterestTotalAmount(BigDecimal interestTotalAmount) {
		this.interestTotalAmount = interestTotalAmount;
	}
	public BigDecimal getInsuranceTotalAmount() {
		return insuranceTotalAmount;
	}
	public void setInsuranceTotalAmount(BigDecimal insuranceTotalAmount) {
		this.insuranceTotalAmount = insuranceTotalAmount;
	}
	
	

}
