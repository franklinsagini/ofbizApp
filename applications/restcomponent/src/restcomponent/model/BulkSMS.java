package restcomponent.model;

public class BulkSMS {
	private String entryNo;
	private String phone;
	private String text;
	private String status;
	private Double balance;
	
	private Results results;

	public String getEntryNo() {
		return entryNo;
	}

	public void setEntryNo(String entryNo) {
		this.entryNo = entryNo;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Results getResults() {
		return results;
	}

	public void setResults(Results results) {
		this.results = results;
	}

}
