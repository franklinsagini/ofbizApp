package org.ofbiz.loanreports;

import java.sql.Timestamp;

import org.joda.time.DateTime;

public class LoanReportsService {

	public static Timestamp getYearStartDate(){
		
		DateTime date = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
		//DateTime date = new DateTime().dayOf
		
		Timestamp startDate = new Timestamp(date.toDate().getTime());
		return startDate;
	}
	
	
	public static Timestamp getYearEndDate(){
		
		DateTime date = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
		date = date.plusYears(1).minusMillis(1);
		
		Timestamp startDate = new Timestamp(date.toDate().getTime());
		
		return startDate;
	}
	
	
}
