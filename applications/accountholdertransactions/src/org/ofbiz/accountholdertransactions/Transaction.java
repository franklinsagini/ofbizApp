package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {

	public String transactionType;
	public BigDecimal transactionAmount;
	public String createdStamp;

	public String accountNo;
	public String accountName;

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(String createdStamp) {
		this.createdStamp = createdStamp;
	}

	// public Timestamp getCreatedStamp() {
	// return createdStamp;
	// }
	// public void setCreatedStamp(Timestamp createdStamp) {
	// this.createdStamp = createdStamp;
	// }
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

}
