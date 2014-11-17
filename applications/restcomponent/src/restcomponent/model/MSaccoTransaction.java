package restcomponent.model;

import java.math.BigDecimal;

public class MSaccoTransaction {
	
	public MSaccoTransaction(){}
	
	private String phoneNumber;
	private BigDecimal amount;
	private String status;
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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
	
	

}
