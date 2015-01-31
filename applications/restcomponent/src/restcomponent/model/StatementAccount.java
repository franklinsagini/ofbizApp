package restcomponent.model;

import java.math.BigDecimal;

public class StatementAccount {
	private Long memberAccountId;
	private String productName;
	private String accountCode;
	private String accountName;
	private BigDecimal bdAvailableBalance;
	private BigDecimal bdBookBalance;
	private String isLoan;
	
	public Long getMemberAccountId() {
		return memberAccountId;
	}
	public void setMemberAccountId(Long memberAccountId) {
		this.memberAccountId = memberAccountId;
	}
	public String getAccountCode() {
		return accountCode;
	}
	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public BigDecimal getBdAvailableBalance() {
		return bdAvailableBalance;
	}
	public void setBdAvailableBalance(BigDecimal bdAvailableBalance) {
		this.bdAvailableBalance = bdAvailableBalance;
	}
	public BigDecimal getBdBookBalance() {
		return bdBookBalance;
	}
	public void setBdBookBalance(BigDecimal bdBookBalance) {
		this.bdBookBalance = bdBookBalance;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getIsLoan() {
		return isLoan;
	}
	public void setIsLoan(String isLoan) {
		this.isLoan = isLoan;
	}

}
