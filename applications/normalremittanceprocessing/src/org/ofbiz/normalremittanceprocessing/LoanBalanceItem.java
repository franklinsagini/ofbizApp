package org.ofbiz.normalremittanceprocessing;

import java.math.BigDecimal;

public class LoanBalanceItem {
	private BigDecimal principalAmount;
	private BigDecimal interestAmount;
	private BigDecimal insuranceAmount;
	private BigDecimal fosaSavingAmount;
	private String sequence;
	
	
	public BigDecimal getFosaSavingAmount() {
		return fosaSavingAmount;
	}
	public void setFosaSavingAmount(BigDecimal fosaSavingAmount) {
		this.fosaSavingAmount = fosaSavingAmount;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public BigDecimal getPrincipalAmount() {
		return principalAmount;
	}
	public void setPrincipalAmount(BigDecimal principalAmount) {
		this.principalAmount = principalAmount;
	}
	public BigDecimal getInterestAmount() {
		return interestAmount;
	}
	public void setInterestAmount(BigDecimal interestAmount) {
		this.interestAmount = interestAmount;
	}
	public BigDecimal getInsuranceAmount() {
		return insuranceAmount;
	}
	public void setInsuranceAmount(BigDecimal insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}
	
	

}
