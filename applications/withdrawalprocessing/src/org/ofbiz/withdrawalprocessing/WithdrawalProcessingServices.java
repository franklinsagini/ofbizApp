package org.ofbiz.withdrawalprocessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/**
 * @author Japheth Odonya  @when Jun 19, 2015 10:27:03 AM
 * 
 * Member Withdrawal Processing
 * 
 * org.ofbiz.withdrawalprocessing.WithdrawalProcessingServices.getLoanBalance
 * */
public class WithdrawalProcessingServices {
	
	private static Logger log = Logger
			.getLogger(WithdrawalProcessingServices.class);
	public static Long PERCENT = 100L;

	
	public static BigDecimal getLoanBalance(Long loanApplicationId){
		
		log.info(" HHHHHHHHHHHHHHH "+loanApplicationId);
		BigDecimal bdBalance = BigDecimal.ZERO;
		bdBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		return bdBalance;
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jun 19, 2015 12:14:35 PM
	 * Get the loan member name
	 * */
	public static String getMemberGuaranteed(Long loanApplicationId){
		String names = "";
		
		names  = LoanUtilities.getMemberNameGivenLoanApplicationId(loanApplicationId);
		
		return names;
	}
	
	/****
	 * @author Japheth Odonya  @when Jun 19, 2015 12:51:03 PM
	 * */
	public static String getMemberNumber(Long loanApplicationId){
		String memberNumber = "";
		
		memberNumber  = LoanUtilities.getMemberNumberGivenLoanApplicationId(loanApplicationId);
		
		return memberNumber;
	}
	
	/***
	 * @author Japheth Odonya  @when Jun 19, 2015 12:48:45 PM
	 * **/
	public static String getPayrollNumber(Long loanApplicationId){
		String payrollNumber = "";
		
		payrollNumber  = LoanUtilities.getPayrollNumberGivenLoanApplicationId(loanApplicationId);
		
		return payrollNumber;
	}
	
	/****
	 * @author Japheth Odonya  @when Jun 19, 2015 12:48:27 PM
	 * */
	public static String getMobileNumber(Long loanApplicationId){
		String mobileNumber = "";
		
		mobileNumber  = LoanUtilities.getMobileNumberGivenLoanApplicationId(loanApplicationId);
		
		return mobileNumber;
	}
	
	//getMemberStationName
	public static String getMemberStationName(Long loanApplicationId){
		String stationName = "";
		
		stationName  = LoanUtilities.getMemberStationName(loanApplicationId);
		
		return stationName;
	}
	
	
	//Get Guaranteed %
	public static BigDecimal getLoanGuarateedPercByGuarantor(Long loanApplicationId, Long guarantorId){
		
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		Long noOfGuarators = new Long(LoansProcessingServices.getNumberOfGuarantors(loanApplicationId).size());
		
		BigDecimal perGuarantorTotal = bdLoanBalance.divide(new BigDecimal(noOfGuarators), 2, RoundingMode.HALF_EVEN);
		
		if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		
		
		BigDecimal bdPercentage = 	perGuarantorTotal.divide(bdLoanBalance, 2, RoundingMode.HALF_EVEN).multiply(new BigDecimal(PERCENT));
		
		return bdPercentage;
	}
	
	//Get Guaranteed Amount
	public static BigDecimal getLoanGuarateedAmountByGuarantor(Long loanApplicationId, Long guarantorId){
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		Long noOfGuarators = new Long(LoansProcessingServices.getNumberOfGuarantors(loanApplicationId).size());
		
		BigDecimal perGuarantorTotal = bdLoanBalance.divide(new BigDecimal(noOfGuarators), 2, RoundingMode.HALF_EVEN);
		
		return perGuarantorTotal;
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jun 19, 2015 1:47:21 PM
	 * 
	 * Get Loan Amount
	 * */
	public static BigDecimal getLoanAmount(Long loanApplicationId){
		BigDecimal loanAmt = BigDecimal.ZERO;
		
		loanAmt  = LoanUtilities.getLoanAmountGivenLoanApplicationId(loanApplicationId);
		
		return loanAmt;
	}
	
	/****
	 * @author Japheth Odonya  @when Jun 19, 2015 2:38:24 PM
	 * 
	 * Get Share Capital Amount
	 * */
	public static BigDecimal getShareCapitalBalance(String partyId){
		
		Long partyIdLong = Long.valueOf(partyId);
		BigDecimal bdShareCapital = BigDecimal.ZERO;
		
		bdShareCapital = LoanUtilities.getShareCapitalAmount(partyIdLong);
		
		return bdShareCapital;
	}
	
	public static BigDecimal getMemberDepositBalance(String partyId){
		BigDecimal bdMemberDeposit = BigDecimal.ZERO;
		Long partyIdLong = Long.valueOf(partyId);
		bdMemberDeposit = LoanUtilities.getMemberDepositAmount(partyIdLong);
		return bdMemberDeposit;
	}
	
	public static String getPartIdGivenMemberWithdrawalId(Long memberWithdrawalId){
		String partyId = null;
		
		GenericValue memberWithdrawal = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberWithdrawal = delegator.findOne("MemberWithdrawal",
					UtilMisc.toMap("memberWithdrawalId", memberWithdrawalId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		partyId = memberWithdrawal.getLong("partyId").toString();
		
		return partyId;
	}
	
	/***
	 * Get Total Loan Balance
	 * */
	public static BigDecimal getLoanTotalLoanBalance(String partyId){
		BigDecimal bdTotalLoanBalance = BigDecimal.ZERO;
		Long partyIdLong = Long.valueOf(partyId);
		bdTotalLoanBalance = LoansProcessingServices.getTotalDisbursedLoanBalances(partyIdLong);
				//LoansProcessingServices.getTotalLoanBalances(memberId, loanProductId);
		return bdTotalLoanBalance;
	}
	
	/****
	 * @author Japheth Odonya  @when Jun 23, 2015 7:36:39 PM
	 * 
	 * Total Guaranteed Amount by Member
	 * */
	public static BigDecimal getTotalGuaranteedAmount(String partyId){
		Long partyIdLong = Long.valueOf(partyId);
		BigDecimal bdTotalGuaranteedAmount = BigDecimal.ZERO;
		
		//Get the list of guarateed loans
		//LoanGuarantorDisbursedLoan
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanGuarantorDisbursedLoanELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanGuarantorDisbursedLoanConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"guarantorId", EntityOperator.EQUALS,
								partyIdLong),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanGuarantorDisbursedLoanELI = delegator.findList("LoanGuarantorDisbursedLoan",
					loanGuarantorDisbursedLoanConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue genericValue : loanGuarantorDisbursedLoanELI) {
			bdTotalGuaranteedAmount = bdTotalGuaranteedAmount.add(getLoanGuarateedAmountByGuarantor(genericValue.getLong("loanApplicationId") , genericValue.getLong("guarantorId")));
		}
		
		
		return bdTotalGuaranteedAmount;
	}
	
	/***
	 * @author Japheth Odonya  @when Jun 23, 2015 7:49:56 PM
	 * 
	 * Share Capital Limit
	 * */
	public static BigDecimal getShareCapitalMinimum(String partyId){
		BigDecimal bdShareCapitalLimit = new BigDecimal(20000);
		return bdShareCapitalLimit;
	}
	
}
