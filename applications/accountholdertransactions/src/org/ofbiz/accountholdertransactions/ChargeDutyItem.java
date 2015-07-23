package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;

public class ChargeDutyItem {
	private Long productChargeId;
	private Long exciseDutyChargeId;
	private String chargeAccountId;
	private String dutyAccountId;
	private BigDecimal chargeAmount;
	private BigDecimal dutyAmount;
	
	public Long getProductChargeId() {
		return productChargeId;
	}
	public void setProductChargeId(Long productChargeId) {
		this.productChargeId = productChargeId;
	}
	public Long getExciseDutyChargeId() {
		return exciseDutyChargeId;
	}
	public void setExciseDutyChargeId(Long exciseDutyChargeId) {
		this.exciseDutyChargeId = exciseDutyChargeId;
	}
	public String getChargeAccountId() {
		return chargeAccountId;
	}
	public void setChargeAccountId(String chargeAccountId) {
		this.chargeAccountId = chargeAccountId;
	}
	public String getDutyAccountId() {
		return dutyAccountId;
	}
	public void setDutyAccountId(String dutyAccountId) {
		this.dutyAccountId = dutyAccountId;
	}
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
