package org.ofbiz.interestmanagement;

import java.sql.Timestamp;

public class EarningPeriod {
	private Timestamp startDate;
	private Timestamp endDate;
	public Timestamp getStartDate() {
		return startDate;
	}
	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}
	public Timestamp getEndDate() {
		return endDate;
	}
	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}
}
