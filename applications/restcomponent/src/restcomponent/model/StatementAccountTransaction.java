package restcomponent.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class StatementAccountTransaction {
	private Long memberAccountId;
	private String transactionDate;
	private String description;
	private BigDecimal amount;
	private String accountNo;
	private String accountName;
	private BigDecimal bdBalanceAmount;
	private String debitCredit;
	public String getDebitCredit() {
		return debitCredit;
	}
	public void setDebitCredit(String debitCredit) {
		this.debitCredit = debitCredit;
	}
	public Long getMemberAccountId() {
		return memberAccountId;
	}
	public void setMemberAccountId(Long memberAccountId) {
		this.memberAccountId = memberAccountId;
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
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public BigDecimal getBdBalanceAmount() {
		return bdBalanceAmount;
	}
	public void setBdBalanceAmount(BigDecimal bdBalanceAmount) {
		this.bdBalanceAmount = bdBalanceAmount;
	}
}
