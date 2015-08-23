package org.ofbiz.accounting.trialbalance;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import org.ofbiz.accounting.util.UtilAccounting;

public class TrialBalanceServices {
	public static final String module = TrialBalanceServices.class.getName();
	public static final String resourceError = "AccountingErrorUiLabels";

	public static Map<String, Object> runTrialBalance(DispatchContext dctx, Map<String, Object> context) {

		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String organizationPartyId = (String) context.get("organizationPartyId");
		Timestamp fromDate = (Timestamp) context.get("fromDate");
		Timestamp thruDate = (Timestamp) context.get("thruDate");
		String glAccountId = (String) context.get("glAccountId");

		System.out.println("#################################### organizationPartyId: " + organizationPartyId);

		BigDecimal totalDebitsToOpeningDate = getAcctgTransEntrySum(delegator, organizationPartyId, glAccountId, "D", fromDate);
		BigDecimal totalDebitsToEndingDate = getAcctgTransEntrySum(delegator, organizationPartyId, glAccountId, "D", thruDate);
		BigDecimal totalCreditsToOpeningDate = getAcctgTransEntrySum(delegator, organizationPartyId, glAccountId, "C", fromDate);
		BigDecimal totalCreditsToEndingDate = getAcctgTransEntrySum(delegator, organizationPartyId, glAccountId, "C", thruDate);

		if (totalDebitsToOpeningDate == null) {
			totalDebitsToOpeningDate = BigDecimal.ZERO;
		}
		if (totalDebitsToEndingDate == null) {
			totalDebitsToEndingDate = BigDecimal.ZERO;
		}
		if (totalCreditsToOpeningDate == null) {
			totalCreditsToOpeningDate = BigDecimal.ZERO;
		}
		if (totalCreditsToEndingDate == null) {
			totalCreditsToEndingDate = BigDecimal.ZERO;
		}

		Boolean isDebit = null;
		GenericValue glAccount = null;

		try {
			glAccount = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", glAccountId), false);
			isDebit = UtilAccounting.isDebitAccount(glAccount);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		BigDecimal endingBalanceCredit = BigDecimal.ZERO;
		BigDecimal endingBalanceDebit = BigDecimal.ZERO;
		if (isDebit) {
			endingBalanceDebit = totalDebitsToEndingDate.subtract(totalCreditsToEndingDate);
		}else {
			endingBalanceCredit = totalCreditsToEndingDate.subtract(totalDebitsToEndingDate);
		}

		System.out.println("#################################### totalDebitsToEndingDate: " + totalDebitsToEndingDate);
		System.out.println("#################################### totalDebitsToOpeningDate: " + totalDebitsToOpeningDate);
		BigDecimal postedDebits = totalDebitsToEndingDate.subtract(totalDebitsToOpeningDate);
		BigDecimal postedCredits = totalCreditsToEndingDate.subtract(totalCreditsToOpeningDate);

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("postedDebits", postedDebits);
		result.put("postedCredits", postedCredits);
		result.put("endingBalanceCredit", endingBalanceCredit);
		result.put("endingBalanceDebit", endingBalanceDebit);
		return result;
	}

	private static BigDecimal getAcctgTransEntrySum(Delegator delegator, String organizationPartyId, String glAccountId, String debitCreditFlag, Timestamp transactionDate) {
		BigDecimal amount = null;
		List<GenericValue> acctgTransEntrySums = null;
		EntityConditionList<EntityExpr> acctgTransEntrySumsCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId),
				EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId),
//				EntityCondition.makeCondition("isPosted", EntityOperator.EQUALS, "Y"),
				EntityCondition.makeCondition("debitCreditFlag", EntityOperator.EQUALS, debitCreditFlag),
//				EntityCondition.makeCondition("glFiscalTypeId", EntityOperator.EQUALS, "ACTUAL"),
				EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN_EQUAL_TO, transactionDate)
				), EntityOperator.AND);

		try {
			acctgTransEntrySums = delegator.findList("AcctgTransEntry", acctgTransEntrySumsCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (acctgTransEntrySums != null) {

			for (GenericValue acctgTransEntrySum : acctgTransEntrySums) {
				amount = acctgTransEntrySum.getBigDecimal("amount");
			}
			System.out.println("#################################### SIZE OF acctgTransEntrySums.size(): " + acctgTransEntrySums.size());
		} else {

		}
		System.out.println("#################################### AMOUNT TO RETRIEVE: " + amount);
		return amount;
	}
}
