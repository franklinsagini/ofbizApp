package org.ofbiz.accounting.ledger;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SasraReportsService {
	
	public static BigDecimal getReportItemTotals(Timestamp startDate, Timestamp endDate, String reportId, String reportItemCode){
		
		// TODO Fix the code
		return BigDecimal.ZERO;
	}
	
	
	public static BigDecimal getReportItemRatio(Timestamp startDate, Timestamp endDate, String reportId, String reportItemCodeNumerator, String reportItemCodeDenominator){
		
		// TODO Fix the code
		return BigDecimal.ZERO;
	}
	

	public static BigDecimal getReportItemRatioPercentage(Timestamp startDate, Timestamp endDate, String reportId, String reportItemCodeNumerator, String reportItemCodeDenominator){
		
		// TODO Fix the code return as a percentage * 100
		return BigDecimal.ZERO;
	}
	
	public static BigDecimal getReportItemDifference(Timestamp startDate, Timestamp endDate, String reportId, String reportItemCodeMinuend, String reportItemCodeSubtrahend){
		
		// TODO Fix the code
		
		return BigDecimal.ZERO;
	}
	

	public static BigDecimal getReportItemSum(Timestamp startDate, Timestamp endDate, String reportId, String reportItemCodeFirstAddend, String reportItemCodeSecondAddend){
		
		// TODO Fix the code
		
		return BigDecimal.ZERO;
	}

}
