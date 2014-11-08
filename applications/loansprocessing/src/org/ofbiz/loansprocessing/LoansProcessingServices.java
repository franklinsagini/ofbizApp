package org.ofbiz.loansprocessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.loans.AmortizationServices;
import org.ofbiz.loans.LoanServices;

/***
 * @author Japheth Odonya  @when Nov 7, 2014 5:30:47 PM
 * 
 * Loans Processing Methods
 * */
public class LoansProcessingServices {
	
	public static Logger log = Logger.getLogger(LoansProcessingServices.class);
	
	public static BigDecimal getMonthlyLoanRepayment(String loanApplicationId){
		BigDecimal monthlyRepayment = BigDecimal.ZERO;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		//Get LoanApplication
		GenericValue loanApplication = getLoanApplication(Long.valueOf(loanApplicationId));
		
		//Get LoanAmount
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		
		Long repaymentPeriod = loanApplication.getLong("repaymentPeriod");
		
		BigDecimal interestRatePM = loanApplication.getBigDecimal("interestRatePM");
		interestRatePM = interestRatePM.divide(new BigDecimal(AmortizationServices.ONEHUNDRED));
		
		GenericValue loanProduct = getLoanProduct(loanApplication.getLong("loanProductId"));
		String deductionType = loanProduct.getString("deductionType");
		
		if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
			monthlyRepayment = AmortizationServices.calculateReducingBalancePaymentAmount(bdLoanAmt,
					interestRatePM, repaymentPeriod.intValue());
		} else {
			monthlyRepayment = AmortizationServices.calculateFlatRatePaymentAmount(bdLoanAmt,
					interestRatePM, repaymentPeriod.intValue());
		}
		//Compute Monthly Repayment
		return monthlyRepayment.setScale(2, RoundingMode.HALF_UP);
	}

	/***
	 * @author Japheth Odonya  @when Nov 7, 2014 6:14:47 PM
	 * 
	 * Get Insurance Amount
	 * */
	public static BigDecimal getInsuranceAmount(String loanApplicationId){
		BigDecimal bdInsuranceAmount = BigDecimal.ZERO;
		
		GenericValue loanApplication = getLoanApplication(Long.valueOf(loanApplicationId));
		BigDecimal bdInsuranceRate = AmortizationServices.getInsuranceRate(loanApplication);
		
		bdInsuranceAmount = bdInsuranceRate.multiply(loanApplication.getBigDecimal("loanAmt").setScale(6, RoundingMode.HALF_UP)).divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
		return bdInsuranceAmount;
	}
	private static GenericValue getLoanApplication(Long loanApplicationId) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							loanApplicationId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Find Application");
		}

		
		return loanApplication;
	}
	
	private static GenericValue getLoanProduct(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne(
					"LoanProduct",
					UtilMisc.toMap("loanProductId",
							loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Loan Product");
		}

		
		return loanProduct;
	}
	
	
	public static BigDecimal getTotalLoanRepayment(BigDecimal repaymentAmount, BigDecimal insuranceAmount){
		BigDecimal bdTotal = BigDecimal.ZERO;
		bdTotal = repaymentAmount.add(insuranceAmount).setScale(2, RoundingMode.HALF_UP);
		return bdTotal; 
	}

	
	public static BigDecimal getLoansRepaid(Long memberId){
		return LoanServices.getLoansRepaid(memberId);
	}
	
	/****
	 * @author Japheth Odonya  @when Nov 8, 2014 9:09:48 PM
	 * 
	 * 
	 * */
	public static BigDecimal getTotalLoanBalances(Long memberId, Long loanProductId){
		BigDecimal bdLoansBalance = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices.calculateExistingAccountLessLoansTotal(String.valueOf(memberId), String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices.calculateExistingLoansTotal(String.valueOf(memberId), String.valueOf(loanProductId), delegator);
		
		BigDecimal bdLoansRepaidAmount = getLoansRepaid(memberId);
		
		bdLoansBalance = bdTotalLoansWithoutAccountAmount.add(bdTotalLoansWithAccountAmount).subtract(bdLoansRepaidAmount);
		return bdLoansBalance;
	}
	
	public static BigDecimal getTotalLoansRunning(Long memberId, Long loanProductId){
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices.calculateExistingAccountLessLoansTotal(String.valueOf(memberId), String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices.calculateExistingLoansTotal(String.valueOf(memberId), String.valueOf(loanProductId), delegator);

		return bdTotalLoansWithoutAccountAmount.add(bdTotalLoansWithAccountAmount);
	}
	
	public static BigDecimal getGruaduatedScaleContribution(BigDecimal bdAmount){
		List<GenericValue> graduatedScaleELI = null; // =
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("lowerValue");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			graduatedScaleELI = delegator.findList("GraduatedScale",
					null, null, listOrder, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		GenericValue graduatedScale = null;
		
		for (GenericValue genericValue : graduatedScaleELI) {
			//bdTotalRepayment = bdTotalRepayment.add(genericValue.getBigDecimal("principalAmount"));
			if (!(bdAmount.compareTo(genericValue.getBigDecimal("lowerValue")) == -1) && !(bdAmount.compareTo(genericValue.getBigDecimal("upperValue")) == 1)){
				graduatedScale = genericValue;
			}
		}
		
		BigDecimal bdContributedAmount = BigDecimal.ZERO;
		//Use the graduated scale to compute the contribution
		if (graduatedScale.getString("isPercent").equals("Yes")){
			bdContributedAmount = bdAmount.multiply(graduatedScale.getBigDecimal("depositPercent")).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
		} else{
			bdContributedAmount = graduatedScale.getBigDecimal("depositAmount");
		}
		
		return bdContributedAmount;
	}
	
	public static BigDecimal getLoanCurrentContributionAmount(Long memberId, Long loanProductId){
		BigDecimal bdTotalBalances = getTotalLoanBalances(memberId, loanProductId);
		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(bdTotalBalances);
		return bdContributionAmount;
	}
	
	public static BigDecimal getLoanNewContributionAmount(Long memberId, Long loanProductId, BigDecimal loanAmt){
		BigDecimal bdTotalBalances = getTotalLoanBalances(memberId, loanProductId);
		
		BigDecimal newLoansTotal = bdTotalBalances.add(loanAmt);
		
		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(newLoansTotal);
		return bdContributionAmount;
	}



}
