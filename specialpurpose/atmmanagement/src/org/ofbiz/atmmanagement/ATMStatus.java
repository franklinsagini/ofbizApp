package org.ofbiz.atmmanagement;

import org.ofbiz.entity.GenericValue;

public class ATMStatus {
	private String status;
	private GenericValue cardApplication;
	
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public GenericValue getCardApplication() {
		return cardApplication;
	}
	public void setCardApplication(GenericValue cardApplication) {
		this.cardApplication = cardApplication;
	}
	
	
	
}
