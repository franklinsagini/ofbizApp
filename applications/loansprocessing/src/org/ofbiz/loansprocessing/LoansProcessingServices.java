package org.ofbiz.loansprocessing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import org.ofbiz.loans.AmortizationServices;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;
import com.ibm.icu.util.Calendar;

/***
 * @author Japheth Odonya @when Nov 7, 2014 5:30:47 PM
 * 
 *         Loans Processing Methods
 * */
public class LoansProcessingServices {

	public static Logger log = Logger.getLogger(LoansProcessingServices.class);
	public static String DEFAULTER_LOAN_CODE = "D330";
	public static BigDecimal MAXCONTRIBUTIONAMOUNT = new BigDecimal(12500);
	public static BigDecimal LOANPERCENTAGE = new BigDecimal(0.25);
	public static Long ONEHUNDRED = 100L;

	public static BigDecimal getMonthlyLoanRepayment(String loanApplicationId) {
		BigDecimal monthlyRepayment = BigDecimal.ZERO;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		// Get LoanApplication
		GenericValue loanApplication = getLoanApplication(Long
				.valueOf(loanApplicationId));

		// Get LoanAmount
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		BigDecimal bdmaxLoanAmt = loanApplication.getBigDecimal("maxLoanAmt");

		if (loanApplication.getBigDecimal("maxLoanAmt") != null)
			if (bdLoanAmt.compareTo(bdmaxLoanAmt) == 1) {
				bdLoanAmt = bdmaxLoanAmt;
			}

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

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		BigDecimal bdmaxLoanAmt = loanApplication.getBigDecimal("maxLoanAmt");

		if (loanApplication.getBigDecimal("maxLoanAmt") != null)
			if (bdLoanAmt.compareTo(bdmaxLoanAmt) == 1) {
				bdLoanAmt = bdmaxLoanAmt;
			}

		bdInsuranceAmount = bdInsuranceRate.multiply(
				bdLoanAmt.setScale(6, RoundingMode.HALF_UP)).divide(
				new BigDecimal(100), 6, RoundingMode.HALF_UP);
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

		// BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices
		// .calculateExistingAccountLessLoansTotal(
		// String.valueOf(memberId),
		// String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansAmount = LoanServices
				.calculateExistingLoansTotal(String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);

		// BigDecimal bdLoansRepaidAmount = getLoansRepaid(memberId);
		// .subtract(bdLoansRepaidAmount)
		bdLoansBalance = bdTotalLoansAmount;
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
		// BigDecimal bdTotalLoansWithoutAccountAmount = LoanServices
		// .calculateExistingAccountLessLoansTotal(
		// String.valueOf(memberId),
		// String.valueOf(loanProductId), delegator);
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices
				.calculateExistingLoansTotal(String.valueOf(memberId),
						String.valueOf(loanProductId), delegator);

		return bdTotalLoansWithAccountAmount;
	}

	public static BigDecimal getTotalLoansRunning(Long memberId) {
		BigDecimal bdTotalLoansWithAccountAmount = LoanServices
				.calculateExistingLoansTotal(memberId);

		return bdTotalLoansWithAccountAmount;
	}

	public static BigDecimal getGruaduatedScaleContribution(BigDecimal bdAmount, Long memberId) {
		BigDecimal bdContributedAmount = BigDecimal.ZERO;
		
		GenericValue member = LoanUtilities.getMember(memberId);
		
		Long memberClassId = member.getLong("memberClassId");
		
		if (memberClassId == null){
			memberClassId = LoanUtilities.getMemberClassId("Class A");
		}
		
		BigDecimal bdClassMinimumContribution = getMinimumClassContributionAmount(memberClassId);
		
		bdContributedAmount = bdAmount.multiply(LOANPERCENTAGE).divide(new BigDecimal(ONEHUNDRED), 4, RoundingMode.HALF_EVEN);
		
		bdContributedAmount = bdClassMinimumContribution.add(bdContributedAmount);
		
		if (bdContributedAmount.compareTo(MAXCONTRIBUTIONAMOUNT) == 1)
		{
			return MAXCONTRIBUTIONAMOUNT;
		}
		
		
		return bdContributedAmount;
	}
	
	private static BigDecimal getMinimumClassContributionAmount(
			Long memberClassId) {
		GenericValue shareMinimum = LoanUtilities.getShareMinimumEntity(memberClassId);
		
		if (shareMinimum == null)
			return null;
		
		return shareMinimum.getBigDecimal("minMemberDepositContributionAmount");
	}

	public static BigDecimal getGruaduatedScaleContributionOld(BigDecimal bdAmount) {
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
			log.info("TTTTTTTTT The Amount is AAAAAAAAAA " + bdAmount);
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
		BigDecimal bdTotalDisbursed = LoanServices
				.getTotalDisbursedLoans(memberId);
		// getTotalLoanBalances(memberId,
		// loanProductId);
		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(bdTotalDisbursed, memberId);
		return bdContributionAmount;
	}

	public static BigDecimal getLoanCurrentContributionAmount(Long memberId) {
//		BigDecimal bdTotalDisbursedLoans = LoanServices
//				.getTotalDisbursedLoans(memberId);
		BigDecimal bdTotalDisbursedLoans = LoanServices
				.getTotalDisbursedLoansBalance(memberId);
		
		// getTotalLoansRunning(memberId);
		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(bdTotalDisbursedLoans, memberId);
		return bdContributionAmount;
	}

	public static BigDecimal getLoanNewContributionAmount(Long memberId,
			Long loanProductId, BigDecimal loanAmt) {
		
		//getTotalDisbursedLoans
		BigDecimal bdTotalDisbursedLoans = LoanServices
				.getTotalDisbursedLoansBalance(memberId);
		// getTotalLoanBalances(memberId,
		// loanProductId);

		BigDecimal newLoansTotal = bdTotalDisbursedLoans.add(loanAmt);

		BigDecimal bdContributionAmount = getGruaduatedScaleContribution(newLoansTotal, memberId);

		// Get Contribution Amount of Loan Product Affect Member Deposits e.g.
		// Super Loan
		GenericValue loanProduct = getLoanProduct(loanProductId);

		if ((loanProduct.getString("affectMemberDeposit") != null)
				&& (loanProduct.getString("affectMemberDeposit").equals("Yes"))
				&& (loanProduct.getBigDecimal("affectMemberDepositPercentage") != null)) {
			BigDecimal bdContributionAffectedByProductAmt = getNewContributionWhenAffectedByProductType(
					loanProductId, loanAmt);

			if (bdContributionAffectedByProductAmt
					.compareTo(bdContributionAmount) == 1) {
				bdContributionAmount = bdContributionAffectedByProductAmt;
			}
		}
		return bdContributionAmount;
	}

	private static BigDecimal getNewContributionWhenAffectedByProductType(
			Long loanProductId, BigDecimal loanAmt) {

		GenericValue loanProduct = getLoanProduct(loanProductId);

		BigDecimal affectMemberDepositPercentage = loanProduct
				.getBigDecimal("affectMemberDepositPercentage");

		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId("901");
		BigDecimal bdMinimumDepositAmount = BigDecimal.ZERO;

		BigDecimal contributionAmount = loanAmt.multiply(
				affectMemberDepositPercentage).divide(new BigDecimal(100), 4,
				RoundingMode.HALF_UP);
		contributionAmount.setScale(4, RoundingMode.HALF_UP);

		if (accountProduct != null) {
			bdMinimumDepositAmount = accountProduct
					.getBigDecimal("minSavingsAmt");
		}

		contributionAmount = contributionAmount.add(bdMinimumDepositAmount);

		return contributionAmount;
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

	public static Timestamp getLastRepaymentDate(Long loanApplicationId) {
		
		//Get the record from loanApplication
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		Timestamp lastRepaymentDate = loanApplication.getTimestamp("lastRepaymentDate");
		//Get Last Repayment Date
		List<GenericValue> loanRepaymentELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("-loanRepaymentId");
		
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId), EntityCondition.makeCondition(
						"principalAmount", EntityOperator.GREATER_THAN, BigDecimal.ZERO)),
						EntityOperator.AND);
		
		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, listOrder, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if ((loanRepaymentELI != null) && (loanRepaymentELI.size() > 0)){
			lastRepaymentDate = loanRepaymentELI.get(0).getTimestamp("createdStamp");
		}
		
		
		return lastRepaymentDate;
	}
	
	public static String lastRepaymentDurationToDate(Long loanApplicationId) {
		
		//Get Last Repayment Date
		Timestamp lastRepaymentDate = getLastRepaymentDate(loanApplicationId);
		
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
	
	
	public static String lastRepaymentDurationToDate(Timestamp lastRepaymentDate) {
		
		//Get Last Repayment Date
		
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

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static void createGuarantorLoans(BigDecimal bdGuarantorLoanAmount,
			GenericValue loanGuarantor, GenericValue loanApplication,
			String userLoginId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		Long loanApplicationId = delegator.getNextSeqIdLong("LoanApplication",
				1);
		GenericValue newLoanApplication;

		Long defaulterLoanProductId = LoanUtilities.getLoanProductGivenCode(
				DEFAULTER_LOAN_CODE).getLong("loanProductId");

		newLoanApplication = delegator.makeValue("LoanApplication", UtilMisc
				.toMap("loanApplicationId", loanApplicationId,
						"parentLoanApplicationId",
						loanApplication.getLong("loanApplicationId"), "loanNo",
						String.valueOf(loanApplicationId), "createdBy",
						userLoginId, "isActive", "Y", "partyId",
						loanGuarantor.getLong("guarantorId"),

						"loanProductId", defaulterLoanProductId,
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

						"originalLoanProductId",
						loanApplication.getLong("loanProductId"),

						"accountProductId",
						loanApplication.getLong("accountProductId")

				));
		try {
			delegator.createOrStore(newLoanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

	}

	public static List<GenericValue> getNumberOfGuarantors(
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

	public static Boolean loanReadyForAppraisal(Long loanApplicationId) {
		/***
		 * The two ready statuses are FORWARDEDLOANS RETURNEDTOAPPRAISAL
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong(
				"loanStatusId");

		if ((getLoanStatus("FORWARDEDLOANS").equals(loanStatusId))
				|| (getLoanStatus("RETURNEDTOAPPRAISAL").equals(loanStatusId))

		) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean loanReadyForApproval(Long loanApplicationId) {
		/***
		 * The two ready statuses are
		 * 
		 * APPRAISED
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong(
				"loanStatusId");

		if ((getLoanStatus("APPRAISED") == loanStatusId)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean loanReadyForDisbursement(Long loanApplicationId) {
		/***
		 * The Status that readies DISBURSEMENT
		 * 
		 * APPROVED
		 * 
		 * */
		Long loanStatusId = getLoanApplication(loanApplicationId).getLong(
				"loanStatusId");

		if ((getLoanStatus("APPROVED") == loanStatusId)) {
			return true;
		} else {
			return false;
		}
	}

	public static String validateGuarantor(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Long loanApplicationId = Long.valueOf((String) request
				.getParameter("loanApplicationId"));
		Long guarantorId = Long.valueOf((String) request
				.getParameter("guarantorId"));

		/***
		 * 
		 * isEmployee = data.isEmployee;
		 * 
		 * isSelf = data.isSelf; isOldEnough = data.isOldEnough; hasDeposits =
		 * data.hasDeposits;
		 * 
		 * 
		 * **/
		Boolean isEmployee = guarantorIsAnEmployee(guarantorId);
		
		if (!isEmployee){
			isEmployee = guarantorIsSaccoEmployee(guarantorId);
		}
		
		Boolean isStaffLoan = isStaffLoan(loanApplicationId);

		Boolean isSelf = guarantorIsSelf(guarantorId, loanApplicationId);

		Boolean alreadyAdded = memberAlreadyGuaranteedTheLoan(guarantorId,
				loanApplicationId);

		Boolean isOldEnough = LoanUtilities.isOldEnough(guarantorId.toString());

		Boolean hasDeposits = guarantorHasDeposits(guarantorId);

		Boolean isBoardMember = guarantorIsBoardMember(guarantorId);

		result.put("isEmployee", isEmployee);
		result.put("isSelf", isSelf);

		result.put("isOldEnough", isOldEnough);
		result.put("hasDeposits", hasDeposits);

		result.put("alreadyAdded", alreadyAdded);

		result.put("isBoardMember", isBoardMember);
		result.put("isStaffLoan", isStaffLoan);
		

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
	
	
	private static Boolean isStaffLoan(Long loanApplicationId) {
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		
		if (loanApplication == null)
			return false;
		
		if (loanApplication.getLong("loanProductId") == null)
			return false;
		
		GenericValue loanProduct = LoanUtilities.getEntityValue("LoanProduct", "loanProductId", loanApplication.getLong("loanProductId"));
		
		if (loanProduct.getString("isStaffLoan") == null)
			return false;
		
		if (loanProduct.getString("isStaffLoan").trim().equals("Y"))
			return true;
		
		return false;
	}

	/*****
	 * Delete Guarantor Record
	 * */
	public static String deleteGuarantor(HttpServletRequest request,
			HttpServletResponse response) {
		Long loanGuarantorId = Long.valueOf((String) request.getParameter("loanGuarantorId"));
		Long loanApplicationId = Long.valueOf((String) request.getParameter("loanApplicationId"));
		
		log.info(" LLLLLLL Loan Guarantor ID "+loanGuarantorId);
		//Find and delete loan guarantor record
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.removeByCondition("LoanGuarantor", EntityCondition.makeCondition("loanGuarantorId", loanGuarantorId));
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//Update Guarantors
		LoanServices.generateGuarantorPercentages(loanApplicationId);
		
		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}	
	/***
	 * Check if the member trying to guarantee is a Board Member
	 * */
	private static Boolean guarantorIsBoardMember(Long guarantorId) {
		GenericValue member = LoanUtilities.getMember(guarantorId);

		if (member == null)
			return false;
		
		if (member.getString("memberCategory") == null)
			return false;

		if (member.getString("memberCategory").equals("BOARDMEMBER"))
			return true;

		return false;
	}
	
	private static Boolean guarantorIsSaccoEmployee(Long guarantorId) {
		
		GenericValue member = LoanUtilities.getMember(guarantorId);

		if (member == null)
			return false;
		
		if (member.getString("memberCategory") == null)
			return false;

		if (member.getString("memberCategory").equals("EMPLOYEE"))
			return true;

		return false;
	}

	private static Boolean memberAlreadyGuaranteedTheLoan(Long guarantorId,
			Long loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> loanGuarantorConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId), EntityCondition.makeCondition(
						"guarantorId", EntityOperator.EQUALS, guarantorId)),
						EntityOperator.AND);

		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					loanGuarantorConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// return loanGuarantorELI;
		if (loanGuarantorELI.size() > 0)
			return true;

		return false;
	}

	private static Boolean guarantorHasDeposits(Long guarantorId) {
		// TODO Auto-generated method stub
		// LoanServices.
		// AccHolderTransactionServices.MEMBER_DEPOSIT_CODE
		Long accountProductId = LoanUtilities
				.getMemberDepositsAccountId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE);
		BigDecimal bdTotalAmount = AccHolderTransactionServices
				.getAccountTotalBalance(accountProductId, guarantorId);

		if (bdTotalAmount.compareTo(BigDecimal.ZERO) == 1)
			return true;

		return false;
	}

	private static Boolean guarantorIsSelf(Long guarantorId,
			Long loanApplicationId) {

		// GenericValue guarantorMember =
		// LoanUtilities.getMember(guarantorId.toString());
		GenericValue loanApplyingMember = LoanUtilities
				.getMemberGiveLoanApplicationId(loanApplicationId);

		Long memberPartyId = loanApplyingMember.getLong("partyId");

		// TODO Auto-generated method stub

		if (guarantorId.compareTo(memberPartyId) == 0)
			return true;

		return false;
	}

	/****
	 * Check that Guarantor is Employee
	 * **/
	private static Boolean guarantorIsAnEmployee(Long partyId) {
		// TODO Auto-generated method stub
		GenericValue member = LoanUtilities.getMember(partyId);
		String payrollNumber = member.getString("payrollNumber");

		// Find Employee Given Payroll Number
		List<GenericValue> personELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> personConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"employeeNumber", EntityOperator.EQUALS,
								payrollNumber)), EntityOperator.AND);
		try {
			personELI = delegator.findList("Person", personConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (personELI.size() > 0)
			return true;

		return false;
	}
	
	/****
	 * @author Japheth Odonya  @when Jun 23, 2015 7:12:00 PM
	 * 
	 * Total Disbursed Loan Balances given partyId
	 * 
	 * */
	public static BigDecimal getTotalDisbursedLoanBalances(Long partyId){
		BigDecimal bdTotalBalance = BigDecimal.ZERO;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanApplicationELI) {
			bdTotalBalance = bdTotalBalance.add(getTotalLoanBalancesByLoanApplicationId(genericValue.getLong("loanApplicationId")));
		}

		return bdTotalBalance;
	}
	
	/***
	 * Disbursed Loans of specific CLASS - BOSA or FOSA
	 * */
	public static BigDecimal getTotalDisbursedLoanBalancesGivenClass(Long partyId, String loanClass){
		BigDecimal bdTotalBalance = BigDecimal.ZERO;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		GenericValue loanProduct = null;
		for (GenericValue genericValue : loanApplicationELI) {
			//if ()
			loanProduct = LoanUtilities.getEntityValue("LoanProduct", "loanProductId", genericValue.getLong("loanProductId"));
			if (loanProduct.getString("fosaOrBosa").trim().equals(loanClass.trim())){
				bdTotalBalance = bdTotalBalance.add(getTotalLoanBalancesByLoanApplicationId(genericValue.getLong("loanApplicationId")));
				bdTotalBalance = bdTotalBalance.add(LoanRepayments.getTotalInterestByLoanDue(genericValue.getLong("loanApplicationId").toString()));
				bdTotalBalance = bdTotalBalance.add(LoanRepayments.getTotalInsurancByLoanDue(genericValue.getLong("loanApplicationId").toString()));
			}
		}

		return bdTotalBalance;
	}

	public static List<String> getLoanApplicationList(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> listApplicationIds = new ArrayList<String>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId").toString());
		}

		return listApplicationIds;
	}
	
	public static List<String> getLoanApplicationListClearedLoans(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										LoanUtilities.getLoanStatusId("CLEARED"))
								
								
								), EntityOperator.AND);
		
		
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> listApplicationIds = new ArrayList<String>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId").toString());
		}

		return listApplicationIds;
	}
	
	
	/****
	 * Get disbursed loans list
	 * */
	public static List<Long> getDisbursedLoanApplicationList(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<Long> listApplicationIds = new ArrayList<Long>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId"));
		}

		return listApplicationIds;
	}
	
	
	public static List<Long> getDisbursedLoanApplicationListBeforeInterestChargeDate(Long partyId, Timestamp interestChargeDate) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L),
										
								EntityCondition.makeCondition(
												"disbursementDate", EntityOperator.LESS_THAN,
												interestChargeDate)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<Long> listApplicationIds = new ArrayList<Long>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId"));
		}

		return listApplicationIds;
	}
	
	
	public static List<Long> getDisbursedLoanApplicationListAfterFormularChange(Long partyId, Timestamp formularChangeDate) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L),
										
								EntityCondition.makeCondition(
												"disbursementDate", EntityOperator.GREATER_THAN_EQUAL_TO,
												formularChangeDate)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<Long> listApplicationIds = new ArrayList<Long>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId"));
		}

		return listApplicationIds;
	}
	
	
	/****
	 * Get disbursed loans list
	 * */
	public static BigDecimal getTotalDisbursedLoans(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		//List<Long> listApplicationIds = new ArrayList<Long>();
		BigDecimal bdAmountTotal = BigDecimal.ZERO;
		for (GenericValue genericValue : loanApplicationELI) {
			//listApplicationIds.add(genericValue.getLong("loanApplicationId"));
			bdAmountTotal = bdAmountTotal.add(genericValue.getBigDecimal("loanAmt"));
		}

		return bdAmountTotal;
	}
	
	
	
	public static List<String> getDefaultedLoanApplicationList(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Long defaultedLoanStatusId = LoanUtilities.getLoanStatusId("DEFAULTED");
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										defaultedLoanStatusId)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> listApplicationIds = new ArrayList<String>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId").toString());
		}

		return listApplicationIds;
	}

	public static List<String> getLoansGuaranteedList(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"guarantorId", EntityOperator.EQUALS,
								partyId),
								
								EntityCondition.makeCondition(
										"loanStatusId", EntityOperator.EQUALS,
										6L)
								
								
								), EntityOperator.AND);
		
		try {
			loanApplicationELI = delegator.findList("LoanGuarantorDisbursedLoan",
					loanApplicationConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> listApplicationIds = new ArrayList<String>();
		for (GenericValue genericValue : loanApplicationELI) {
			listApplicationIds.add(genericValue.getLong("loanApplicationId").toString());
		}

		return listApplicationIds;
	}
	

}
