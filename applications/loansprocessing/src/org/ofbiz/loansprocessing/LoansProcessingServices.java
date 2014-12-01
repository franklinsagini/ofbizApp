package org.ofbiz.loansprocessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Months;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.loans.AmortizationServices;
import org.ofbiz.loans.LoanServices;

import com.ibm.icu.util.Calendar;

/***
 * @author Japheth Odonya @when Nov 7, 2014 5:30:47 PM
 * 
 *         Loans Processing Methods
 * */
public class LoansProcessingServices {

	public static Logger log = Logger.getLogger(LoansProcessingServices.class);

	public static BigDecimal getMonthlyLoanRepayment(String loanApplicationId) {
		BigDecimal monthlyRepayment = BigDecimal.ZERO;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		// Get LoanApplication
		GenericValue loanApplication = getLoanApplication(Long
				.valueOf(loanApplicationId));

		// Get LoanAmount
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		Long repaymentPeriod = loanApplication.getLong("repaymentPeriod");

		BigDecimal interestRatePM = loanApplication
				.getBigDecimal("interestRatePM");
		interestRatePM = interestRatePM.divide(new BigDecimal(
				AmortizationServices.ONEHUNDRED));

		GenericValue loanProduct = getLoanProduct(loanApplication
				.getLong("loanProductId"));
		String deductionType = loanProduct.getString("deductionType");

		if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
			monthlyRepayment = AmortizationServices
					.calculateReducingBalancePaymentAmount(bdLoanAmt,
							interestRatePM, repaymentPeriod.intValue());
		} else {
			monthlyRepayment = AmortizationServices
					.calculateFlatRatePaymentAmount(bdLoanAmt, interestRatePM,
							repaymentPeriod.intValue());
		}
		// Compute Monthly Repayment
		return monthlyRepayment.setScale(2, RoundingMode.HALF_UP);
	}

	/***
	 * @author Japheth Odonya @when Nov 7, 2014 6:14:47 PM
	 * 
	 *         Get Insurance Amount
	 * */
	public static BigDecimal getInsuranceAmount(String loanApplicationId) {
		BigDecimal bdInsuranceAmount = BigDecimal.ZERO;

		GenericValue loanApplication = getLoanApplication(Long
				.valueOf(loanApplicationId));
		BigDecimal bdInsuranceRate = AmortizationServices
				.getInsuranceRate(loanApplication);

		bdInsuranceAmount = bdInsuranceRate.multiply(
				loanApplication.getBigDecimal("loanAmt").setScale(6,
						RoundingMode.HALF_UP)).divide(new BigDecimal(100), 6,
				RoundingMode.HALF_UP);
		return bdInsuranceAmount;
	}

	private static GenericValue getLoanApplication(Long loanApplicationId) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
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
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Loan Product");
		}

		return loanProduct;
	}

	public static BigDecimal getTotalLoanRepayment(BigDecimal repaymentAmount,
			BigDecimal insuranceAmount) {
		BigDecimal bdTotal = BigDecimal.ZERO;
		bdTotal = repaymentAmount.add(insuranceAmount).setScale(2,
				RoundingMode.HALF_UP);
		return bdTotal;
	}

	public static BigDecimal getLoansRepaid(Long memberId) {
		return LoanServices.getLoansRepaid(memberId);
	}

	public static BigDecimal getLoansRepaidByLoanApplicationId(
			Long loanApplicationId) {
		return LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationId);
	}

	/****
	 * @author Japheth Odonya @when Nov 8, 2014 9:09:48 PM
	 * 
	 * 
	 * */
	public static BigDecimal getTotalLoanBalances(Long memberId,
			Long loanProductId) {
		BigDecimal bdLoansBalance = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices
				.calculateExistingAccountLessLoansTotal(
						String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices
				.calculateExistingLoansTotal(String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);

		BigDecimal bdLoansRepaidAmount = getLoansRepaid(memberId);

		bdLoansBalance = bdTotalLoansWithoutAccountAmount.add(
				bdTotalLoansWithAccountAmount).subtract(bdLoansRepaidAmount);
		return bdLoansBalance;
	}

	/***
	 * Getting Balance for specific Loan
	 * */
	public static BigDecimal getTotalLoanBalancesByLoanApplicationId(
			Long loanApplicationId) {
		BigDecimal bdLoansBalance = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdTotalLoanAmount = getLoanAmount(delegator,
				loanApplicationId);

		// =
		// LoanServices.calculateExistingAccountLessLoansTotal(String.valueOf(memberId),
		// String.valueOf(loanProductId), delegator);
		// BigDecimal bdTotalLoansWithAccountAmount =
		// LoanServices.calculateExistingLoansTotal(String.valueOf(memberId),
		// String.valueOf(loanProductId), delegator);

		BigDecimal bdLoansRepaidAmount = getLoansRepaidByLoanApplicationId(loanApplicationId);

		bdLoansBalance = bdTotalLoanAmount.subtract(bdLoansRepaidAmount);
		return bdLoansBalance;
	}

	public static BigDecimal getTotalLoansRunning(Long memberId,
			Long loanProductId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices
				.calculateExistingAccountLessLoansTotal(
						String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices
				.calculateExistingLoansTotal(String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);

		return bdTotalLoansWithoutAccountAmount
				.add(bdTotalLoansWithAccountAmount);
	}

	public static BigDecimal getGruaduatedScaleContribution(BigDecimal bdAmount) {
		List<GenericValue> graduatedScaleELI = null; // =
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("lowerValue");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			graduatedScaleELI = delegator.findList("GraduatedScale", null,
					null, listOrder, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue graduatedScale = null;
		
		if (bdAmount.compareTo(BigDecimal.ZERO) == -1)
			bdAmount = new BigDecimal(1);

		for (GenericValue genericValue : graduatedScaleELI) {
			log.info("TTTTTTTTT The Amount is AAAAAAAAAA "+bdAmount);
			// bdTotalRepayment =
			// bdTotalRepayment.add(genericValue.getBigDecimal("principalAmount"));
			if (!(bdAmount.compareTo(genericValue.getBigDecimal("lowerValue")) == -1)
					&& !(bdAmount.compareTo(genericValue
							.getBigDecimal("upperValue")) == 1)) {
				graduatedScale = genericValue;
			}
		}
		

		BigDecimal bdContributedAmount = BigDecimal.ZERO;
		// Use the graduated scale to compute the contribution
		if (graduatedScale.getString("isPercent").equals("Yes")) {
			bdContributedAmount = bdAmount.multiply(
					graduatedScale.getBigDecimal("depositPercent")).divide(
					new BigDecimal(100), 2, RoundingMode.HALF_UP);
		} else {
			bdContributedAmount = graduatedScale.getBigDecimal("depositAmount");
		}

		return bdContributedAmount;
	}

	public static BigDecimal getLoanCurrentContributionAmount(Long memberId,
			Long loanProductId) {
		BigDecimal bdTotalBalances = getTotalLoanBalances(memberId,
				loanProductId);
		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(bdTotalBalances);
		return bdContributionAmount;
	}

	public static BigDecimal getLoanNewContributionAmount(Long memberId,
			Long loanProductId, BigDecimal loanAmt) {
		BigDecimal bdTotalBalances = getTotalLoanBalances(memberId,
				loanProductId);

		BigDecimal newLoansTotal = bdTotalBalances.add(loanAmt);

		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(newLoansTotal);
		return bdContributionAmount;
	}

	public static Long getLoanStatus(String name) {
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
		return loanStatusId;
	}

	public static String lastRepaymentDurationToDate(Timestamp lastRepaymentDate) {
		String days = "";
		if (lastRepaymentDate == null)
			return days;

		DateTime startDate = new DateTime(lastRepaymentDate.getTime());
		DateTime endDate = new DateTime(Calendar.getInstance()
				.getTimeInMillis());

		Days noOfDays = Days.daysBetween(startDate, endDate);
		Months noOfMonths = Months.monthsBetween(startDate, endDate);
		if (noOfDays.get(DurationFieldType.days()) <= 60) {
			days = noOfDays.get(DurationFieldType.days()) + " days ago";
		} else {
			days = noOfMonths.getMonths() + " months ago";
		}
		return days;
	}

	// transferToGuarantors
	public static String transferToGuarantors(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue loanApplication = null;

		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String statusName = "DEFAULTED";
		Long loanStatusId = LoanServices.getLoanStatusId(statusName);

		loanApplication.set("loanStatusId", loanStatusId);

		// Updates the loan to defaulted
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// Create a Log
		GenericValue loanStatusLog;
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "forwarded to Loans"));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Create New Loans and Attach them to the Guarantors
		BigDecimal bdLoanBalance = getTotalLoanBalancesByLoanApplicationId(Long
				.valueOf(loanApplicationId));
		List<GenericValue> loanGuarantorELI = getNumberOfGuarantors(Long
				.valueOf(loanApplicationId));

		int noOfGuarantors = loanGuarantorELI.size();

		if (noOfGuarantors <= 0)
			return "success";
		BigDecimal bdGuarantorLoanAmount = bdLoanBalance.divide(new BigDecimal(
				noOfGuarantors), 6, RoundingMode.HALF_UP);

		for (GenericValue loanGuarantor : loanGuarantorELI) {
			createGuarantorLoans(bdGuarantorLoanAmount, loanGuarantor,
					loanApplication, userLoginId);
		}

		return "success";
	}

	private static void createGuarantorLoans(BigDecimal bdGuarantorLoanAmount,
			GenericValue loanGuarantor, GenericValue loanApplication,
			String userLoginId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusId = LoanServices.getLoanStatusId("GUARANTORLOAN");
		Long loanApplicationId = delegator.getNextSeqIdLong("LoanApplication",
				1);
		GenericValue newLoanApplication;
		newLoanApplication = delegator.makeValue("LoanApplication", UtilMisc
				.toMap("loanApplicationId", loanApplicationId,
						"parentLoanApplicationId",
						loanApplication.getLong("loanApplicationId"), "loanNo",
						String.valueOf(loanApplicationId), "createdBy",
						userLoginId, "isActive", "Y", "partyId",
						loanGuarantor.getLong("guarantorId"), "loanProductId",
						loanApplication.getLong("loanProductId"),
						"interestRatePM",
						loanApplication.getBigDecimal("interestRatePM")

						, "repaymentPeriod",
						loanApplication.getLong("repaymentPeriod")

						, "loanAmt", bdGuarantorLoanAmount, "appliedAmt",
						bdGuarantorLoanAmount, "appraisedAmt",
						bdGuarantorLoanAmount, "approvedAmt",
						bdGuarantorLoanAmount, "loanStatusId", loanStatusId,
						"deductionType",
						loanApplication.getString("deductionType"),

						"accountProductId",
						loanApplication.getLong("accountProductId")

				));
		try {
			delegator.createOrStore(newLoanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

	}

	private static List<GenericValue> getNumberOfGuarantors(
			Long loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return loanGuarantorELI;
	}

	private static BigDecimal getLoanAmount(Delegator delegator,
			Long loanApplicationId) {
		GenericValue loanApplication = null;

		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplication == null)
			return BigDecimal.ZERO;
		return loanApplication.getBigDecimal("loanAmt");
	}
	
	public static Boolean loanReadyForAppraisal(Long loanApplicationId){
		/***
		 * The two ready statuses are
		 * FORWARDEDLOANS
		 * RETURNEDTOAPPRAISAL
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong("loanStatusId");
		
		if ((getLoanStatus("FORWARDEDLOANS") == loanStatusId) || (getLoanStatus("RETURNEDTOAPPRAISAL") == loanStatusId))
		{
			return true;
		} else{
			return false;
		}
	}
	
	public static Boolean loanReadyForApproval(Long loanApplicationId){
		/***
		 * The two ready statuses are
		 * 
		 * APPRAISED
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong("loanStatusId");
		
		if ((getLoanStatus("APPRAISED") == loanStatusId))
		{
			return true;
		} else{
			return false;
		}
	}
	
	public static Boolean loanReadyForDisbursement(Long loanApplicationId){
		/***
		 * The Status that readies DISBURSEMENT
		 * 
		 * APPROVED
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong("loanStatusId");
		
		if ((getLoanStatus("APPROVED") == loanStatusId))
		{
			return true;
		} else{
			return false;
		}
	}

}
