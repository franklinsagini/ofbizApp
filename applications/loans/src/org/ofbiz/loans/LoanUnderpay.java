package org.ofbiz.loans;

public class LoanUnderpay {
	
	private Long loanApplicationId;
	private Boolean underPaid;
	private String loanNo;
	
	public Long getLoanApplicationId() {
		return loanApplicationId;
	}
	public void setLoanApplicationId(Long loanApplicationId) {
		this.loanApplicationId = loanApplicationId;
	}
	public Boolean getUnderPaid() {
		return underPaid;
	}
	public void setUnderPaid(Boolean underPaid) {
		this.underPaid = underPaid;
	}
	public String getLoanNo() {
		return loanNo;
	}
	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}
	
	
}
