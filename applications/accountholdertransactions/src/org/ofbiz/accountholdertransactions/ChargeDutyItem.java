package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;

public class ChargeDutyItem {
	
	private BigDecimal chargeAmount;
	private BigDecimal dutyAmount;
	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}
	public void setChargeAmount(BigDecimal chargeAmount) {
		this.chargeAmount = chargeAmount;
	}
	public BigDecimal getDutyAmount() {
		return dutyAmount;
	}
	public void setDutyAmount(BigDecimal dutyAmount) {
		this.dutyAmount = dutyAmount;
	}
}
