package restcomponent.model;

import java.math.BigDecimal;

public class LoanAccountTransaction {
	private Long loanApplicationId;
	private String transactionDate;
	private String description;
	private BigDecimal dbAmount;
	private BigDecimal bdBalanceAmount;
	private String debitCredit;
	public Long getLoanApplicationId() {
		return loanApplicationId;
	}
	public void setLoanApplicationId(Long loanApplicationId) {
		this.loanApplicationId = loanApplicationId;
	}
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getDbAmount() {
		return dbAmount;
	}
	public void setDbAmount(BigDecimal dbAmount) {
		this.dbAmount = dbAmount;
	}
	public BigDecimal getBdBalanceAmount() {
		return bdBalanceAmount;
	}
	public void setBdBalanceAmount(BigDecimal bdBalanceAmount) {
		this.bdBalanceAmount = bdBalanceAmount;
	}
	public String getDebitCredit() {
		return debitCredit;
	}
	public void setDebitCredit(String debitCredit) {
		this.debitCredit = debitCredit;
	}
	
}
