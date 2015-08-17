package org.ofbiz.loans;

public class UnpaidLoan {
	private Boolean unpaid;
	private String loanNos;
	public Boolean getUnpaid() {
		return unpaid;
	}
	public void setUnpaid(Boolean unpaid) {
		this.unpaid = unpaid;
	}
	public String getLoanNos() {
		return loanNos;
	}
	public void setLoanNos(String loanNos) {
		this.loanNos = loanNos;
	}
}
