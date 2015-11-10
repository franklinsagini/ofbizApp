package org.ofbiz.accounting.ledger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Months;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;

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

	public static String getDaysSinceLastRepayment(Delegator delegator, Long loanApplicationId) {

		GenericValue repayment = getLastRepayment(delegator, loanApplicationId);
		if (repayment != null) {
			return lastRepaymentDurationToDateInDays(repayment.getTimestamp("createdStamp"));
		} else {
			return "0";
		}

	}

	public static String lastRepaymentDurationToDateInDays(Timestamp lastRepaymentDate) {
		String days = "";
		if (lastRepaymentDate == null)
			return days;

		DateTime startDate = new DateTime(lastRepaymentDate.getTime());
		DateTime endDate = new DateTime(Calendar.getInstance()
				.getTimeInMillis());

		Days noOfDays = Days.daysBetween(startDate, endDate);
		days = noOfDays.get(DurationFieldType.days()) + " days ago";
		return days;
	}

}
