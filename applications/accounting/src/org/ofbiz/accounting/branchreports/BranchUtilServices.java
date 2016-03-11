/**
 * 
 */
package org.ofbiz.accounting.branchreports;

import java.sql.Timestamp;
import java.util.List;

import org.ofbiz.accounting.ledger.CrbReportServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

/**
 * @author samoei
 *
 */
public class BranchUtilServices {
	public static final String module = BranchUtilServices.class.getName();

	// Given LoanApplicationId get members Branch

	public static String getMembersBranch(Delegator delegator, GenericValue loan) {
		String branchId = null;

		Long memberpartyId = loan.getLong("partyId");

		branchId = getMembersBranch(delegator, memberpartyId);

		return branchId;

	}

	private static String getMembersBranch(Delegator delegator, Long memberpartyId) {
		GenericValue member = null;

		try {
			member = delegator.findOne("Member", UtilMisc.toMap("partyId", memberpartyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return member.getString("branchId");
	}

	public static String getMembersStations(Delegator delegator, Long memberpartyId) {
		GenericValue member = null;

		try {
			member = delegator.findOne("Member", UtilMisc.toMap("partyId", memberpartyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return member.getString("stationId");
	}

	public static List<GenericValue> getLoansForPeriod(Delegator delegator, Timestamp startDate, Timestamp endDate, Long loanStatusId) {
		List<GenericValue> loansList = null;

		EntityConditionList<EntityExpr> loansConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("disbursementDate", EntityOperator.GREATER_THAN_EQUAL_TO, startDate),
				EntityCondition.makeCondition("disbursementDate", EntityOperator.LESS_THAN_EQUAL_TO, endDate),
				EntityCondition.makeCondition("loanStatusId", EntityOperator.EQUALS, loanStatusId)
				), EntityOperator.AND);
		try {
			loansList = delegator.findList("LoanApplication", loansConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return loansList;
	}

	public static List<GenericValue> getRepaymentForPeriod(Delegator delegator, Timestamp startDate, Timestamp endDate) {
		List<GenericValue> repaymentsList = null;

		EntityConditionList<EntityExpr> repaymentConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate),
				EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate)
				), EntityOperator.AND);
		try {
			repaymentsList = delegator.findList("LoanRepayment", repaymentConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return repaymentsList;
	}

	public static Boolean isDeliquentLoan(Long loanApplicationId) {
		boolean isDeliquent = false;

		int daysInArrears = CrbReportServices.lastRepaymentDurationToDateInDays(loanApplicationId);

		if (daysInArrears > 30) {
			isDeliquent = true;
		}

		return isDeliquent;
	}

	public static List<GenericValue> getBranchAccountProductTotals(Delegator delegator, Timestamp startDate, Timestamp endDate, Long accountProductId) {
		List<GenericValue> accountProductList = null;
		

		EntityConditionList<EntityExpr> productTotalsConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDate),
				EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDate),
				EntityCondition.makeCondition("accountProductId", EntityOperator.EQUALS, accountProductId)
				), EntityOperator.AND);
		try {
			accountProductList = delegator.findList("AccountContributionCreditAmounts", productTotalsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return accountProductList;
	}
	
	public static List<GenericValue> getBranchAccountProductTotalsTotal(Delegator delegator,Timestamp startDate, Long accountProductId) {
		List<GenericValue> accountProductList = null;
		

		EntityConditionList<EntityExpr> productTotalsConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN, startDate),
				EntityCondition.makeCondition("accountProductId", EntityOperator.EQUALS, accountProductId)
				), EntityOperator.AND);
		try {
			accountProductList = delegator.findList("AccountContributionCreditAmounts", productTotalsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return accountProductList;
	}
	
	public static List<GenericValue> getBranchAccountProductTotals(Delegator delegator, Timestamp startDate, Timestamp endDate) {
		List<GenericValue> accountProductList = null;
		
		
		
		
		
		return accountProductList;
	}

}
