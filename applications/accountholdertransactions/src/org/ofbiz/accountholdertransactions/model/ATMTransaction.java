package org.ofbiz.accountholdertransactions.model;

import java.math.BigDecimal;

public class ATMTransaction {
	private String cardNumber;
	private BigDecimal amount;
	
	private Long transactionId;
	private BigDecimal availableBalance;
	private BigDecimal bookBalance;
	private BigDecimal chargeAmount;
	private BigDecimal commissionAmount;
	
	private String status;
	
	private Long cardStatusId;
	private String cardStatus;
	
	public Long getCardStatusId() {
		return cardStatusId;
	}
	public void setCardStatusId(Long cardStatusId) {
		this.cardStatusId = cardStatusId;
	}
	public String getCardStatus() {
		return cardStatus;
	}
	public void setCardStatus(String cardStatus) {
		this.cardStatus = cardStatus;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	public BigDecimal getAvailableBalance() {
		return availableBalance;
	}
	public void setAvailableBalance(BigDecimal availableBalance) {
		this.availableBalance = availableBalance;
	}
	public BigDecimal getBookBalance() {
		return bookBalance;
	}
	public void setBookBalance(BigDecimal bookBalance) {
		this.bookBalance = bookBalance;
	}
	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}
	public void setChargeAmount(BigDecimal chargeAmount) {
		this.chargeAmount = chargeAmount;
	}
	public BigDecimal getCommissionAmount() {
		return commissionAmount;
	}
	public void setCommissionAmount(BigDecimal commissionAmount) {
		this.commissionAmount = commissionAmount;
	}
	@Override
	public String toString() {
		return "ATMTransaction [cardNumber=" + cardNumber + ", amount="
				+ amount + ", transactionId=" + transactionId
				+ ", availableBalance=" + availableBalance + ", bookBalance="
				+ bookBalance + ", chargeAmount=" + chargeAmount
				+ ", commissionAmount=" + commissionAmount + ", status="
				+ status + "]";
	}
	
	
}
