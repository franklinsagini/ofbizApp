package org.ofbiz.accountholdertransactions.model;


public class ATMStatus {
	
	private String status;
	private Long memberAccountId;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getMemberAccountId() {
		return memberAccountId;
	}
	public void setMemberAccountId(Long memberAccountId) {
		this.memberAccountId = memberAccountId;
	}
}
