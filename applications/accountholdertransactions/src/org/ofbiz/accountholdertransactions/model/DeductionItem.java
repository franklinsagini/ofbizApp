package org.ofbiz.accountholdertransactions.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DeductionItem {
	private String code;
	private BigDecimal bdAmount;
	private BigDecimal bdBalance;
	private Timestamp deductionDate;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public BigDecimal getBdAmount() {
		return bdAmount;
	}
	public void setBdAmount(BigDecimal bdAmount) {
		this.bdAmount = bdAmount;
	}
	public BigDecimal getBdBalance() {
		return bdBalance;
	}
	public void setBdBalance(BigDecimal bdBalance) {
		this.bdBalance = bdBalance;
	}
	public Timestamp getDeductionDate() {
		return deductionDate;
	}
	public void setDeductionDate(Timestamp deductionDate) {
		this.deductionDate = deductionDate;
	}
	
	

}
