package restcomponent.model;

import java.math.BigDecimal;

public class LoanAccount {
	private Long loanApplicationId;
	private String loanNo;
	private String loanType;
	private String dateDisbursed;
	private BigDecimal loanAmt;
	private BigDecimal loanBalance;
	
	public Long getLoanApplicationId() {
		return loanApplicationId;
	}
	public void setLoanApplicationId(Long loanApplicationId) {
		this.loanApplicationId = loanApplicationId;
	}
	public String getLoanNo() {
		return loanNo;
	}
	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}
	public String getLoanType() {
		return loanType;
	}
	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}
	public String getDateDisbursed() {
		return dateDisbursed;
	}
	public void setDateDisbursed(String dateDisbursed) {
		this.dateDisbursed = dateDisbursed;
	}
	public BigDecimal getLoanAmt() {
		return loanAmt;
	}
	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	}
	public BigDecimal getLoanBalance() {
		return loanBalance;
	}
	public void setLoanBalance(BigDecimal loanBalance) {
		this.loanBalance = loanBalance;
	}

}
