package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;

/***
 * @author Japheth Odonya  @when Jul 9, 2015 5:26:55 PM
 * 
 * PrincipalInterestInsurance Class
 * 
 * */
public class PrincipalInterestInsurance {

	private BigDecimal insuranceAmt;
	private BigDecimal interestAmt;
	private BigDecimal principalAmt;
	public BigDecimal getInsuranceAmt() {
		return insuranceAmt;
	}
	public void setInsuranceAmt(BigDecimal insuranceAmt) {
		this.insuranceAmt = insuranceAmt;
	}
	public BigDecimal getInterestAmt() {
		return interestAmt;
	}
	public void setInterestAmt(BigDecimal interestAmt) {
		this.interestAmt = interestAmt;
	}
	public BigDecimal getPrincipalAmt() {
		return principalAmt;
	}
	public void setPrincipalAmt(BigDecimal principalAmt) {
		this.principalAmt = principalAmt;
	}
	
}
