package org.ofbiz.loanclearing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
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
import org.ofbiz.loans.LoanAccounting;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class LoanClearingServices {

	public static Logger log = Logger.getLogger(LoanAccounting.class);

	public static String MEMBERDEPOSITCODE = "901";
	public static String FOSASAVINGSCODE = "999";

	public static BigDecimal getTotalAmountToClear(Long loanClearId) {
		BigDecimal totalToClear = BigDecimal.ZERO;

		List<GenericValue> loanClearItemELI = null; // =
		EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanClearId", EntityOperator.EQUALS, loanClearId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearItemELI = delegator.findList("LoanClearItem",
					loanClearItemConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanClearItemELI) {
			totalToClear = totalToClear.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		return totalToClear;
	}

	public static List<Long> getLoanApplicationIDsCleared(Long loanClearId) {
		List<Long> listLoanApplicationIds = new ArrayList<Long>();

		List<GenericValue> loanClearItemELI = null; // =
		EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanClearId", EntityOperator.EQUALS, loanClearId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearItemELI = delegator.findList("LoanClearItem",
					loanClearItemConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanClearItemELI) {
			listLoanApplicationIds.add(genericValue
					.getLong("loanApplicationId"));
		}

		return listLoanApplicationIds;
	}

	public static String hasNewLoan(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Long loanClearId = Long.valueOf((String) request
				.getParameter("loanClearId"));

		result.put("hasNewLoan", hasNewLoan(loanClearId));

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	private static Boolean hasNewLoan(Long loanClearId) {
		List<GenericValue> loanClearELI = null; // =
		EntityConditionList<EntityExpr> loanClearConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanClearId", EntityOperator.EQUALS, loanClearId),

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.NOT_EQUAL, null)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", loanClearConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((loanClearELI != null) && (loanClearELI.size() > 0))
			return true;

		return false;
	}

	public static Boolean loanAppliedAmountIsCorrect(Long loanClearId,
			Long loanApplicationId) {
		if (loanApplicationId == null)
			return false;

		// if (loanApp)

		// On one side the total loan applied
		BigDecimal bdTotalLoanApplied = LoanUtilities
				.getLoanAmount(loanApplicationId);

		log.info("IIIIIIIIIIIIIIII  Clear ID " + loanClearId);
		log.info("IIIIIIIIIIIIIIII Loan Application ID " + loanApplicationId);
		// on the second side - the total loans being cleared, their total
		// accrued interests
		// and their total accrued insurances and their total charges
		BigDecimal bdTotalClearanceCost = getTotalClearanceCost(loanClearId,
				loanApplicationId);

		log.info(" Amount applied is :: " + bdTotalLoanApplied
				+ " Clearance cost is " + bdTotalClearanceCost);

		if (bdTotalLoanApplied.compareTo(bdTotalClearanceCost) == 1)
			return true;

		return false;
	}

	private static BigDecimal getTotalClearanceCost(Long loanClearId,
			Long loanApplicationId) {

		log.info(" Numbers IN Clear ID " + loanClearId
				+ " Loan Application ID " + loanApplicationId);
		BigDecimal bdTotalClearAmount = getTotalAmountToClear(loanClearId);

		// LoanAccounting.getTotalClearedAmount(loanApplicationId);
		log.info(" Numbers IN bdTotalClearAmount " + bdTotalClearAmount);

		// Get total charges based on rates
		BigDecimal bdTotalLoanCost = BigDecimal.ZERO;
		bdTotalLoanCost = bdTotalLoanCost.add(bdTotalClearAmount);

		List<Long> listLoanApplicationIDs = getLoanApplicationIDsCleared(loanClearId);

		BigDecimal bdTotalCharge = BigDecimal.ZERO;
		for (Long clearedLoanApplicationId : listLoanApplicationIDs) {

			BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices
					.getTotalLoanBalancesByLoanApplicationId(clearedLoanApplicationId);
			BigDecimal bdInterestAmount = LoanRepayments
					.getTotalInterestByLoanDue(clearedLoanApplicationId
							.toString());
			BigDecimal bdTotalInsuranceAmount = LoanRepayments
					.getTotalInsurancByLoanDue(clearedLoanApplicationId
							.toString());
			log.info(" LLLLLLLL Loan Amount to offset AAAAAAAAAA"
					+ bdTotalLoanBalanceAmount);
			// LoansProcessingServices.get
			bdTotalLoanCost = bdTotalLoanCost.add(LoanRepayments
					.getTotalInterestByLoanDue(clearedLoanApplicationId
							.toString()));
			bdTotalLoanCost = bdTotalLoanCost.add(LoanRepayments
					.getTotalInsurancByLoanDue(clearedLoanApplicationId
							.toString()));

			BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount)
					.add(bdTotalInsuranceAmount);
			log.info("BBBBBB Total Balance " + bdTotal);
			bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices
					.getLoanClearingCharge(clearedLoanApplicationId, bdTotal));
			log.info("CCCCCC Total Charge " + bdTotalCharge);

		}

		return bdTotalLoanCost;
	}

	public static BigDecimal getTotalAccruedInterest(Long loanClearItemId) {
		// List<Long> listLoanApplicationIDs =
		// getLoanApplicationIDsCleared(loanClearId);
		Long loanApplicationId = getLoanApplicationIdGiveLoanClearItemId(loanClearItemId);

		BigDecimal bdTotalCharge = BigDecimal.ZERO;
		BigDecimal bdTotalInterestAmount = BigDecimal.ZERO;

		BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal bdInterestAmount = LoanRepayments
				.getTotalInterestByLoanDue(loanApplicationId.toString());

		bdTotalInterestAmount = bdTotalInterestAmount.add(bdInterestAmount);

		BigDecimal bdTotalInsuranceAmount = LoanRepayments
				.getTotalInsurancByLoanDue(loanApplicationId.toString());
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString()));
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString()));

		BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount)
				.add(bdTotalInsuranceAmount);
		bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices
				.getLoanClearingCharge(loanApplicationId, bdTotal));
		return bdTotalInterestAmount;
	}

	public static Long getLoanApplicationIdGiveLoanClearItemId(
			Long loanClearItemId) {

		GenericValue loanClearItem = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanClearItem = delegator.findOne("LoanClearItem",
					UtilMisc.toMap("loanClearItemId", loanClearItemId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanClearItem != null)
			return loanClearItem.getLong("loanApplicationId");

		return null;
	}

	public static BigDecimal getChargeRate(Long loanClearItemId) {
		// List<Long> listLoanApplicationIDs =
		// getLoanApplicationIDsCleared(loanClearId);
		Long loanApplicationId = getLoanApplicationIdGiveLoanClearItemId(loanClearItemId);
		BigDecimal bdTotalCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSumInsuranceAmount = BigDecimal.ZERO;

		BigDecimal bdChargeRate = BigDecimal.ZERO;

		BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal bdInterestAmount = LoanRepayments
				.getTotalInterestByLoanDue(loanApplicationId.toString());

		BigDecimal bdTotalInsuranceAmount = LoanRepayments
				.getTotalInsurancByLoanDue(loanApplicationId.toString());
		bdTotalSumInsuranceAmount = bdTotalSumInsuranceAmount
				.add(bdTotalInsuranceAmount);
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString()));
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString()));

		BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount)
				.add(bdTotalInsuranceAmount);
		bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices
				.getLoanClearingCharge(loanApplicationId, bdTotal));
		bdChargeRate = getLoanClearingChargeRate(loanApplicationId, bdTotal);

		return bdChargeRate;
	}

	public static BigDecimal getTotalChargeAmount(Long loanClearItemId) {
		// List<Long> listLoanApplicationIDs =
		// getLoanApplicationIDsCleared(loanClearId);
		Long loanApplicationId = getLoanApplicationIdGiveLoanClearItemId(loanClearItemId);
		BigDecimal bdTotalCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSumInsuranceAmount = BigDecimal.ZERO;

		BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal bdInterestAmount = LoanRepayments
				.getTotalInterestByLoanDue(loanApplicationId.toString());

		BigDecimal bdTotalInsuranceAmount = LoanRepayments
				.getTotalInsurancByLoanDue(loanApplicationId.toString());
		bdTotalSumInsuranceAmount = bdTotalSumInsuranceAmount
				.add(bdTotalInsuranceAmount);
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString()));
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString()));

		BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount)
				.add(bdTotalInsuranceAmount);
		bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices
				.getLoanClearingCharge(loanApplicationId, bdTotal));

		return bdTotalCharge;
	}

	public static BigDecimal getTotalAccruedInsurance(Long loanClearItemId) {
		// List<Long> listLoanApplicationIDs =
		// getLoanApplicationIDsCleared(loanClearId);
		Long loanApplicationId = getLoanApplicationIdGiveLoanClearItemId(loanClearItemId);
		BigDecimal bdTotalCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSumInsuranceAmount = BigDecimal.ZERO;

		BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal bdInterestAmount = LoanRepayments
				.getTotalInterestByLoanDue(loanApplicationId.toString());

		BigDecimal bdTotalInsuranceAmount = LoanRepayments
				.getTotalInsurancByLoanDue(loanApplicationId.toString());
		bdTotalSumInsuranceAmount = bdTotalSumInsuranceAmount
				.add(bdTotalInsuranceAmount);
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString()));
		// bdTotalLoanCost =
		// bdTotalLoanCost.add(LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString()));

		BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount)
				.add(bdTotalInsuranceAmount);
		bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices
				.getLoanClearingCharge(loanApplicationId, bdTotal));

		return bdTotalSumInsuranceAmount;
	}

	public static BigDecimal getTotalAmountToClearByItemId(Long loanClearItemId) {
		BigDecimal totalToClear = BigDecimal.ZERO;

		List<GenericValue> loanClearItemELI = null; // =
		EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanClearItemId", EntityOperator.EQUALS,
						loanClearItemId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearItemELI = delegator.findList("LoanClearItem",
					loanClearItemConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanClearItemELI) {
			totalToClear = totalToClear.add(genericValue
					.getBigDecimal("loanAmt"));
		}

		return totalToClear;
	}

	public static BigDecimal getLoanClearingChargeRate(Long loanApplicationId,
			BigDecimal bdAmount) {
		BigDecimal bdPercentagePaid = LoanServices
				.getLoanPercentageRepaidValue(loanApplicationId);

		BigDecimal bdChargeRate = BigDecimal.ZERO;
		EntityConditionList<EntityExpr> loanClearRateConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"lowerLimit", EntityOperator.LESS_THAN_EQUAL_TO,
						bdPercentagePaid), EntityCondition.makeCondition(
						"upperLimit", EntityOperator.GREATER_THAN_EQUAL_TO,
						bdPercentagePaid)), EntityOperator.AND);

		List<GenericValue> loanClearRateELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearRateELI = delegator.findList("LoanClearRate",
					loanClearRateConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : loanClearRateELI) {
			bdChargeRate = genericValue.getBigDecimal("chargeRate");
		}

		return bdChargeRate;
	}

	// Get Account Name given memberId
	public static String getMemberDepositsAccountNumber(Long memberId) {

		return getAccountNo(memberId, MEMBERDEPOSITCODE);

	}

	public static String getMemberDepositsAccountName(Long memberId) {
		return getAccountName(memberId, MEMBERDEPOSITCODE);
	}

	public static String getFosaSavingsAccountNumber(Long memberId) {
		return getAccountNo(memberId, FOSASAVINGSCODE);
	}

	public static String getFosaSavingsAccountName(Long memberId) {
		return getAccountName(memberId, MEMBERDEPOSITCODE);
	}

	private static String getAccountNo(Long memberId, String code) {

		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(code);

		if (accountProduct == null)
			return null;

		Long accountProductId = accountProduct.getLong("accountProductId");

		if (accountProductId == null)
			return null;

		List<GenericValue> memberAccountELI = null; // =
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String accountNo = "";
		for (GenericValue genericValue : memberAccountELI) {
			accountNo = genericValue.getString("accountNo");
		}
		return accountNo;
	}

	private static String getAccountName(Long memberId, String code) {
		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(code);

		if (accountProduct == null)
			return null;

		Long accountProductId = accountProduct.getLong("accountProductId");

		if (accountProductId == null)
			return null;

		List<GenericValue> memberAccountELI = null; // =
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String accountName = "";
		for (GenericValue genericValue : memberAccountELI) {
			accountName = genericValue.getString("accountName");
		}
		return accountName;
	}
	
	private static Long getMemberAccountId(Long memberId, String code) {
		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(code);

		if (accountProduct == null)
			return null;

		Long accountProductId = accountProduct.getLong("accountProductId");

		if (accountProductId == null)
			return null;

		List<GenericValue> memberAccountELI = null; // =
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberAccountId = null;
		for (GenericValue genericValue : memberAccountELI) {
			memberAccountId = genericValue.getLong("memberAccountId");
		}
		return memberAccountId;
	}
	
	public static BigDecimal getMemberDepositsAccountBalance(Long memberId){
		
		BigDecimal bdAccountBalance = null;
		Long memberAccountId = getMemberAccountId(memberId, MEMBERDEPOSITCODE);
		
		//org.ofbiz.accountholdertransactions
		bdAccountBalance = AccHolderTransactionServices.getBookBalanceNow(memberAccountId.toString());
		
		return bdAccountBalance;
	}

}
