package org.ofbiz.accounting.ledger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loansprocessing.LoansProcessingServices;

import com.ibm.icu.util.Calendar;

public class CrbReportServices {
	public static final String module = CrbReportServices.class.getName();
	private static BigDecimal ZERO = BigDecimal.ZERO;

	public static BigDecimal getLastRepaymentAmount(Delegator delegator, Long loanApplicationId) {
		BigDecimal lastRepaymentAmount = ZERO;

		GenericValue repayment = getLastRepayment(delegator, loanApplicationId);
		if (repayment != null) {
			lastRepaymentAmount = repayment.getBigDecimal("totalPrincipalDue");
			System.out.println("THIS IS THE LAST REPAYMENT: " + repayment.getLong("loanRepaymentId"));
		}

		return lastRepaymentAmount;
	}

	public static GenericValue getLastRepayment(Delegator delegator, Long loanApplicationId) {
		GenericValue repayment = null;
		List<GenericValue> loanRepayments = null;
		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, loanApplicationId)
				));

		try {
			loanRepayments = delegator.findList("LoanRepayment", cond, null, UtilMisc.toList("createdStamp"), null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int repaymentSize = 0;
		if (loanRepayments.size() > 0) {
			repaymentSize = loanRepayments.size();
			repayment = loanRepayments.get(repaymentSize - 1);
		}
		return repayment;
	}

	public static int lastRepaymentDurationToDateInDays(Long loanApplicationId) {

		// Get Last Repayment Date
		Timestamp lastRepaymentDate = LoansProcessingServices.getLastRepaymentDate(loanApplicationId);

		int days = 0;
		if (lastRepaymentDate == null)
			return days;

		DateTime startDate = new DateTime(lastRepaymentDate.getTime());
		DateTime endDate = new DateTime(Calendar.getInstance()
				.getTimeInMillis());

		Days noOfDays = Days.daysBetween(startDate, endDate);

		return days = noOfDays.get(DurationFieldType.days());

	}

	public static String getCRBDateFormat(Timestamp date) {
		String formattedDate = "";
		String stringDate = date.toString();
		String newDate = stringDate.replaceAll("\\D", "");
		formattedDate = newDate.substring(0, 8);
		return formattedDate;
	}
	
	public static String getCRBDateFormat(String date) {
		String formattedDate = "";
		String newDate = date.replaceAll("\\D", "");
		formattedDate = newDate.substring(0, 8);
		return formattedDate;
	}

	public static String getCRBDateFormat(java.sql.Date date) {
		String formattedDate = "";
		String stringDate = date.toString();
		String newDate = stringDate.replaceAll("\\D", "");
		formattedDate = newDate.substring(0, 8);
		return formattedDate;
	}

	public static String getCRBPhoneFormat(String phone) {
		String trimmedPhone = phone.trim();
		String formattedPhone = "";

		if (trimmedPhone.length()>0) {
			char c = trimmedPhone.charAt(0);
		if (c == '+') {
			formattedPhone = trimmedPhone.substring(trimmedPhone.indexOf(c) + 1);
		}
		}
		
		return formattedPhone;
	}
	
	public static String getCRBAmountFormat(BigDecimal amount) {
		String formattedAmount = "";
		String stringAmount = amount.toString();
		if (stringAmount.contains(".")) {
			String newAmount = stringAmount.substring(0, stringAmount.indexOf(".")+3);
			formattedAmount = newAmount.replaceAll("\\D", "");
		}else {
			formattedAmount = stringAmount+"00";
		}
		return formattedAmount;
	}

}
