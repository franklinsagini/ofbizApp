package org.ofbiz.accountholdertransactions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanServices;

public class LoanUtilities {

	public static Long getMemberId(String payrollNo){
		Long memberId = null;
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("payrollNumber", payrollNo), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : memberELI) {
			memberId = genericValue.getLong("partyId");
		}

		
		return memberId;
	}
	
	
	public static List<Long> getLoanApplicationIds(Long memberId){
		List<Long> loanApplicationIds = new ArrayList<Long>();
		
		Long disbursedLoansStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						disbursedLoansStatusId),
						
						EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								memberId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : loanApplicationELI) {
			loanApplicationIds.add(genericValue.getLong("loanApplicationId"));
		}
		
		return loanApplicationIds;
 	}
	
	public static String getLoanProductCode(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct.getString("code");
	}

	public static GenericValue getLoanApplicationEntity(Long loanApplicationId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanApplication;
	}
	
	public static GenericValue getLoanProduct(Long loanProductId){
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct;
	}
	
	
	/***
	 * Get Loan Status
	 * */

	public static Long getLoanStatusId(String name) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long loanStatusId = 0L;
		for (GenericValue genericValue : loanStatusELI) {
			loanStatusId = genericValue.getLong("loanStatusId");
		}

		String statusIdString = String.valueOf(loanStatusId);
		statusIdString = statusIdString.replaceAll(",", "");
		loanStatusId = Long.valueOf(statusIdString);
		return loanStatusId;
	}
	
	//Count Loan Guarantors
	
	public static Long getGuarantorsCount(Long loanApplicationId) {
		List<GenericValue> guaranteedLoanELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			guaranteedLoanELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId", loanApplicationId), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Long guarantorCount = 0L;
		
		guarantorCount = new Long(guaranteedLoanELI.size());

		return guarantorCount;
	}
	
	public static GenericValue getMember(String partyId){
		
		partyId = partyId.replaceAll(",", "");
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return member;
	}

	public static int getMemberDurations(Date joinDate) {

		LocalDateTime stJoinDate = new LocalDateTime(joinDate.getTime());
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());

		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(stJoinDate, stCurrentDate, monthDay);

		int months = difference.getMonths();

		return months;

	}
	
	public static boolean isOldEnough(String partyId){
		
		Boolean oldEnough = false;
		
		GenericValue member = getMember(partyId);
		
		int duration = getMemberDurations(member.getDate("joinDate"));
		
		if (duration < 6)
		{
			oldEnough = false;
		} else{
			oldEnough = true;
		}
		
		return oldEnough;
	}
	
	public static boolean isFromAnotherSacco(String partyId){
		
		Boolean fromAnotherSacco = false;
		
		GenericValue member = getMember(partyId);
		
		String membershipofOtherSacco = member.getString("membershipofOtherSacco");
		
		if ((membershipofOtherSacco != null) && (membershipofOtherSacco.equals("Y")))
		{
			fromAnotherSacco = true;
		} else{
			fromAnotherSacco = false;
		}
		
		return fromAnotherSacco;
	}
	
	public static GenericValue getAccountProductGivenCodeId(String code) {

		List<GenericValue> accountProductELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> accountProductConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"code", EntityOperator.EQUALS,
						code)),
						EntityOperator.AND);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					accountProductConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue accountProduct = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProduct = genericValue;
		}
		
		

		return accountProduct;
	}

}
