package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.AmortizationServices;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.webapp.event.EventHandlerException;

/***
 * @author Japheth Odonya @when Sep 10, 2014 7:14:36 PM
 * 
 *         Loan Repayments Processing
 * **/
public class LoanRepayments {
	public static Logger log = Logger.getLogger(LoanRepayments.class);
	private static int ONEHUNDRED = 100;

	public static String generateLoansAmortizationSchedules(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		/***
		 * Get all disbursed loans
		 * 
		 * status is DISBURSED
		 * 
		 * if a loan is not in LoanAmortization - generate amortization schedule
		 * with 1) Amount - Balance 2) Start Date - Next Month after last
		 * repayment or next month from now if last repayment is null 3 Period -
		 * loan period specified in application
		 * */
		Long disbursedLoansStatusId = LoanServices.getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						disbursedLoansStatusId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long count = 0L;
		for (GenericValue loanApplication : loanApplicationELI) {
			if (!amortizationGeneratedAlready(loanApplication
					.getLong("loanApplicationId"))) {
				System.out.println(++count
						+ "Generating Amortization Schedule for .... Loan No "
						+ loanApplication.getString("loanNo"));
				generateSchedule(loanApplication, count, request, response);
			}

		}
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

	private static void generateSchedule(GenericValue loanApplication,
			Long count, HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		log.info("######### Generating Schedule ######### " + count);
		// Get the repaymentStartDate
		Timestamp disbursementDate = loanApplication
				.getTimestamp("disbursementDate");

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		LocalDate localCurrentDate = new LocalDate(currentDate);

		// if loan disbursement date is in this month then do
		// A

		LocalDate localDisbursementDate = new LocalDate(
				disbursementDate.getTime());
		LocalDate localRepaymentStartDate;

		int repaymentPeriod = loanApplication.getLong("repaymentPeriod")
				.intValue();

//		if ((localDisbursementDate.getMonthOfYear() == localCurrentDate
//				.getMonthOfYear())
//				&& (localDisbursementDate.getYear() == localCurrentDate
//						.getYear())) {
			if (localDisbursementDate.getDayOfMonth() < 15) {
				// repaymentStartDate = new Timestamp(localDisbursementDate.)
				localRepaymentStartDate = localDisbursementDate.plusMonths(1)
						.withDayOfMonth(1);
			} else {
				localRepaymentStartDate = localDisbursementDate.plusMonths(2)
						.withDayOfMonth(1);
			}

//		} else
//		// else repayment start date is first of next month
//		{
//			localRepaymentStartDate = localCurrentDate.plusMonths(1)
//					.withDayOfMonth(1);
//
//			int repaidPeriods;
//			// = localCurrentDate.m
//			// localDisbursementDate.
//			repaidPeriods = Months.monthsBetween(localDisbursementDate,
//					localCurrentDate).getMonths();
//
//			if (localDisbursementDate.getDayOfMonth() < 15) {
//				// repaymentStartDate = new Timestamp(localDisbursementDate.)
//				repaidPeriods = repaidPeriods - 1;
//			} else {
//				repaidPeriods = repaidPeriods - 2;
//			}
//
//			repaymentPeriod = repaymentPeriod - repaidPeriods;
//		}

		if (repaymentPeriod > 0) {
			loanApplication.set("repaymentStartDate", new Timestamp(
					localRepaymentStartDate.toDate().getTime()));
			//loanApplication.set("repaymentPeriod", new Long(repaymentPeriod));
			//loanApplication.set("openingRepaymentPeriod", new Long(repaymentPeriod));
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

			try {
				delegator.createOrStore(loanApplication);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		

		log.info("DDDDDDDDDDDD Disbursement Date "
				+ loanApplication.getTimestamp("disbursementDate")
				+ " Repayment Start Date "
				+ loanApplication.getTimestamp("repaymentStartDate"));

		// .getParameter("loanApplicationId");
		
		if (repaymentPeriod > 0){
		request.setAttribute("loanApplicationId",
				loanApplication.getLong("loanApplicationId"));
		// Map<String, String> params = new HashMap<String, String>();
		// params.put("loanApplicationId",
		// String.valueOf(loanApplication.getLong("loanApplicationId")));
		// request.getParameterMap().p
		AmortizationServices.generateschedule(request, response);
		}
	}

	private static boolean amortizationGeneratedAlready(Long loanApplicationId) {
		List<GenericValue> loanAmortizationELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> amortizationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId)

				), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanAmortizationELI = delegator.findList("LoanAmortization",
					amortizationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanAmortizationELI.size() > 0)
			return true;

		return false;
	}

	public static String generateLoansRepaymentExpected(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		List<GenericValue> disbursedLoansViewELI = null;
		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");
		
		LocalDate today = new LocalDate();
		LocalDate lastdayOfNextMonth = (today.plusMonths(2).withDayOfMonth(1));
		lastdayOfNextMonth = lastdayOfNextMonth.minusDays(1);
		
		//Timestamp currentDate = new Timestamp(Calendar.getInstance()
		//		.getTimeInMillis());
		Timestamp lastDateOfExpectation = new Timestamp(lastdayOfNextMonth.toDate().getTime());
		
		if (expectationAlreadyGenerated())
			return "";

//		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
//				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
//						"isAccrued", EntityOperator.EQUALS, "N"),
//						EntityCondition.makeCondition("expectedPaymentDate",
//								EntityOperator.LESS_THAN_EQUAL_TO, lastDateOfExpectation)
//
//				), EntityOperator.AND);
//
//		try {
//			loanAmortizationELI = delegator.findList("LoanAmortization",
//					loanRepaymentConditions, null, null, null, false);
//
//		} catch (GenericEntityException e2) {
//			e2.printStackTrace();
//		}
//		log.info(" ######### Looking for Amortizations that are due #########");
//		if (loanAmortizationELI == null) {
//			log.info(" ######### No Amortizations Due #########");
//		} else {
//			log.info(" ######### Total Number of Amortizations Due is   #########"
//					+ loanAmortizationELI.size());
//		}

		Long loanStatusDisbursedId = new Long(6);
		
		//Get all the ids for disbursed loans
		EntityConditionList<EntityExpr> disbursedLoanIdsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS, loanStatusDisbursedId)

				), EntityOperator.AND);

		try {
			disbursedLoansViewELI = delegator.findList("DisbursedLoansView",
					disbursedLoanIdsConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ######### Looking for disbursed Loans #########");
		if (disbursedLoansViewELI == null) {
			log.info(" ######### No Disbursed Loans #########");
		} else {
			log.info(" ######### Total Number of Disbursed Loans is   #########"
					+ disbursedLoansViewELI.size());
		}

		

		// for each amortization create an expection (LoanExpectation)
		for (GenericValue disbursedLoanView : disbursedLoansViewELI) {

			// Remember to Update Amortization as isAccrued and with dateAccrued
			createLoanExpectation(disbursedLoanView, delegator);
		}

		// Get Expectations
		List<GenericValue> loanExpectationELI = null;
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPosted", EntityOperator.EQUALS, "N")

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ######### Looking for Loan Expectations not yet posted #########");
		if (loanExpectationELI == null) {
			log.info(" ######### No Expectations Not Yet Posted #########");
		} else {
			log.info(" ######### Total Number of LoanExpectations Not Yet Posted   #########"
					+ loanExpectationELI.size());
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			// Remember to update LoanExpectation as Posted
			postLoanExpectation(loanExpectation, delegator, userLogin);
		}

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

	private static boolean expectationAlreadyGenerated() {
		// TODO Auto-generated method stub
		
		LocalDate thisMonthDay = new LocalDate();
		LocalDate firstDayOfThisMonth = (thisMonthDay.withDayOfMonth(1));

		
		LocalDate today = new LocalDate();
		LocalDate lastdayOfthisMonth = (today.plusMonths(1).withDayOfMonth(1));
		//lastdayOfNextMonth = lastdayOfNextMonth.minusDays(1);
		
					EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
					.makeCondition(UtilMisc.toList(
							
							EntityCondition.makeCondition("dateAccrued",
									EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(firstDayOfThisMonth.toDate().getTime())),
							EntityCondition.makeCondition("dateAccrued",
									EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(lastdayOfthisMonth.toDate().getTime()))
			
					), EntityOperator.AND);
		   List<GenericValue> loanExpectationELI = null;
		   Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			try {
				loanExpectationELI = delegator.findList("LoanExpectation",
						loanExpectationConditions, null, null, null, false);
			
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
			if ((loanExpectationELI == null) || loanExpectationELI.size() <= 0) {
				return false;
			} 
		
		
		return true;
	}

	/***
	 * @author Japheth Odonya @when Sep 10, 2014 8:58:22 PM
	 * 
	 *         Post the items in LoanExpectation Principal Interest Insurance
	 * 
	 *         Set LoanExpectationToPosted
	 * */
	private static void postLoanExpectation(GenericValue loanExpectation,
			Delegator delegator, Map<String, String> userLogin) {

		String repaymentName = loanExpectation.getString("repaymentName");
		GenericValue accountHolderTransactionSetup;
		String setupType;
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		if (repaymentName.equals("INTEREST")) {
			// post interest
			log.info("WWWWWWWWWWWWWWWW Will now attempt to post interest ... ");
			setupType = "INTERESTACCRUAL";
			accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
					setupType, delegator);
			// LoanAccounting.postDisbursement(loanApplication, userLogin)
			postInterestAccrued(loanExpectation, delegator, userLogin,
					accountHolderTransactionSetup);
		}

		else if (repaymentName.equals("INSURANCE")) {
			// post insurance
			setupType = "INSURANCEACCRUAL";
			accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
					setupType, delegator);
			postInsuranceCharge(loanExpectation, delegator, userLogin,
					accountHolderTransactionSetup);
		}
		//
		// else if (repaymentName.equals("PRINCIPAL")) {
		// // post principal
		// setupType = "PRINCIPALACCRUAL";
		// accountHolderTransactionSetup =
		// getAccountHolderTransactionSetupRecord(
		// setupType, delegator);
		// postPrincipalDue(loanExpectation, delegator, userLogin,
		// accountHolderTransactionSetup);
		// }

		loanExpectation.set("isPosted", "Y");
		try {
			delegator.createOrStore(loanExpectation);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 11:37:08 AM Get the Account
	 *         Setup Record
	 * */
	public static GenericValue getAccountHolderTransactionSetupRecord(
			String setupType, Delegator delegator) {
		GenericValue accountHolderTransactionSetup = null;
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId", setupType),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Account Setup, Please check the id  ");
		}

		return accountHolderTransactionSetup;
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 11:54:00 AM
	 * 
	 *         Post Principal Accrued
	 * **/
	private static void postPrincipalDue(GenericValue loanExpectation,
			Delegator delegator, Map<String, String> userLogin,
			GenericValue accountHolderTransactionSetup) {
		String acctgTransType = "LOAN_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanExpectation,
				acctgTransType, userLogin, delegator);

		// Post a Debit Entry to accountId
		String postingType = "D";
		String accountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMember(
				loanExpectation.getString("loanApplicationId"), delegator)
				.getString("branchId");
		log.info(" ####### Party or Branch or Company in Principal is ###### "
				+ partyId);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

		// Post a Credit Entry to accountId
		postingType = "C";
		entrySequenceId = "00002";
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 11:54:38 AM Post Insurance
	 *         Charge Accrued
	 * 
	 * **/
	private static void postInsuranceCharge(GenericValue loanExpectation,
			Delegator delegator, Map<String, String> userLogin,
			GenericValue accountHolderTransactionSetup) {
		String acctgTransType = "CHARGE_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanExpectation,
				acctgTransType, userLogin, delegator);

		// Post a Debit Entry to accountId
		String postingType = "D";
		String accountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMember(
				loanExpectation.getString("loanApplicationId"), delegator)
				.getString("branchId");

		log.info(" ####### Party or Branch or Company in Insurance Charge is  ###### "
				+ partyId);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

		// Post a Credit Entry to accountId
		postingType = "C";
		entrySequenceId = "00002";
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 11:49:21 AM Post Interest
	 *         Accrued
	 * */
	private static void postInterestAccrued(GenericValue loanExpectation,
			Delegator delegator, Map<String, String> userLogin,
			GenericValue accountHolderTransactionSetup) {
		String acctgTransType = "INTEREST_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanExpectation,
				acctgTransType, userLogin, delegator);

		// Post a Debit Entry to accountId
		String postingType = "D";
		String accountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMember(
				loanExpectation.getString("loanApplicationId"), delegator)
				.getString("branchId");

		log.info(" ####### Party or Branch or Company in Intrest Accrued is ###### "
				+ partyId);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

		// Post a Credit Entry to accountId
		postingType = "C";
		entrySequenceId = "00002";
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		postTransactionEntry(delegator,
				loanExpectation.getBigDecimal("amountAccrued"), partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);
		// log.info("### Transaction ID## "+acctgTransId);
		// Creates a record in AcctgTransEntry for Member Deposit Account
		// createMemberDepositEntry(loanExpectation, acctgTransId, userLogin,
		// glAcctTypeIdMemberDepo, delegator);
	}

	/***
	 * Create a LoanExpectation and Update LoanAmortization to isAccrued = Y and
	 * Set dateAccrued/Charged to today/currentDate
	 * 
	 * */
	private static void createLoanExpectation(GenericValue disbursedLoanView,
			Delegator delegator) {

		GenericValue loanExpectation = null;
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		Long loanApplicationId = disbursedLoanView.getLong("loanApplicationId");
		String employeeNo = getEmployeeNumber(
				String.valueOf(loanApplicationId), delegator);

		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();

		String loanNo = getLoanNo(String.valueOf(loanApplicationId), delegator);

		GenericValue member = getMember(String.valueOf(loanApplicationId),
				delegator);
		GenericValue loanApplication = getLoanApplication(
				String.valueOf(loanApplicationId), delegator);

		String employeeNames = "";

		if (member.getString("firstName") != null) {
			employeeNames = employeeNames + member.getString("firstName");
		}

		if (member.getString("middleName") != null) {
			employeeNames = employeeNames + " "
					+ member.getString("middleName");
		}

		if (member.getString("lastName") != null) {
			employeeNames = employeeNames + " " + member.getString("lastName");
		}

		// +" "+member.getString("middleName")+" "+member.getString("lastName");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		// BigDecimal interestRatePM =
		// loanApplication.getBigDecimal("interestRatePM");
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		Long repaymentPeriod = loanApplication.getLong("repaymentPeriod");

		BigDecimal monthlyPayable = AmortizationServices
				.calculateReducingBalancePaymentAmount(bdLoanAmt,
						bdInterestRatePM, repaymentPeriod.intValue());

		BigDecimal bdLoanBalance = bdLoanAmt.subtract(LoanServices.getLoansRepaidByLoanApplicationId(loanApplicationId));
		
//		calculateLoanBalance(
//				loanApplication.getString("partyId"),
//				loanApplication.getString("loanApplicationId"), bdLoanAmt);
		
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();
		
		String monthPadded = String.valueOf(month);//paddString(2, String.valueOf(month));
		String monthYear = monthPadded+String.valueOf(year);

		if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 1) {

			// INTEREST
			// BigDecimal bdInterestAccrued = loanAmortization
			// .getBigDecimal("interestAmount");
			BigDecimal bdInterestAccrued = bdLoanBalance
					.multiply(bdInterestRatePM);

			// Adding PRINCIPAL
			// BigDecimal bdPrincipalAccrued = loanAmortization
			// .getBigDecimal("principalAmount");
			BigDecimal bdPrincipalAccrued = BigDecimal.ZERO;
			bdPrincipalAccrued = monthlyPayable.subtract(bdInterestAccrued);
			
			//Return the total unpaid or monthly expected based on interest rate and period whichever is
			//greater of the two values
			
			//bdPrincipalAccrued = getUnpaidPrincipalTotal(bdPrincipalAccrued, loanApplicationId);
			bdPrincipalAccrued = bdPrincipalAccrued.setScale(4, RoundingMode.HALF_UP);

			// INSURANCE
			// BigDecimal bdInsuranceAccrued = loanAmortization
			// .getBigDecimal("insuranceAmount");
			BigDecimal bdInsuranceRate = AmortizationServices
					.getInsuranceRate(loanApplication);
			BigDecimal bdInsuranceAccrued = bdInsuranceRate.multiply(
					bdLoanBalance.setScale(6, RoundingMode.HALF_UP)).divide(
					new BigDecimal(100), 6, RoundingMode.HALF_UP);

			// Adding Principal
			loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
					.toMap("loanExpectationId", loanExpectationId, "loanNo",
							loanNo, "loanApplicationId", loanApplicationId,
							"employeeNo", employeeNo, "repaymentName",
							"PRINCIPAL", "employeeNames", employeeNames,
							"dateAccrued", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()), "isPaid", "N",
							"isPosted", "N",

							"amountDue", bdPrincipalAccrued, "amountAccrued",
							bdPrincipalAccrued,
							
							"month", monthYear,

							"partyId", member.getLong("partyId"), "loanAmt",
							bdLoanAmt));

			listTobeStored.add(loanExpectation);

			// Add Interest
			loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
					1L);
			bdInterestAccrued = bdInterestAccrued.setScale(4, RoundingMode.HALF_UP);
			loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
					.toMap("loanExpectationId", loanExpectationId, "loanNo",
							loanNo, "loanApplicationId", loanApplicationId,
							"employeeNo", employeeNo, "repaymentName",
							"INTEREST", "employeeNames", employeeNames,
							"dateAccrued", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()), "isPaid", "N",
							"isPosted", "N", "amountDue", bdInterestAccrued,
							"amountAccrued", bdInterestAccrued,
							"month", monthYear,
							"partyId",
							member.getLong("partyId"), "loanAmt", bdLoanAmt));
			listTobeStored.add(loanExpectation);

			loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
					1L);
			bdInsuranceAccrued = bdInsuranceAccrued.setScale(4, RoundingMode.HALF_UP);
			loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
					.toMap("loanExpectationId", loanExpectationId, "loanNo",
							loanNo, "loanApplicationId", loanApplicationId,
							"employeeNo", employeeNo, "repaymentName",
							"INSURANCE", "employeeNames", employeeNames,
							"dateAccrued", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()), "isPaid", "N",
							"isPosted", "N", "amountDue", bdInsuranceAccrued,
							"amountAccrued", bdInsuranceAccrued,
							
							"month", monthYear,
							"partyId",
							member.getLong("partyId"), "loanAmt", bdLoanAmt));
			listTobeStored.add(loanExpectation);

			// Update Amortization
//			loanAmortization.set("isAccrued", "Y");
//			loanAmortization.set("dateAccrued", new Timestamp(Calendar
//					.getInstance().getTimeInMillis()));

			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
			}
			try {
				delegator.storeAll(listTobeStored);
			//	delegator.createOrStore(loanAmortization);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			try {
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static String paddString(int padDigits, String count) {
		String padded = String.format("%" + padDigits + "s", count).replace(
				' ', '0');
		return padded;
	}

	private static BigDecimal getUnpaidPrincipalTotal(
			BigDecimal bdPrincipalAccrued, Long loanApplicationId) {

		//Get the total principal due based on armotization
		BigDecimal dbTotalDueFromSchedule = BigDecimal.ZERO;
		dbTotalDueFromSchedule = getTotalPrincipalDueFromAmortization(loanApplicationId);
		// Get the total repaid based on repayment
		BigDecimal bdTotalRepaidBasedOnRepayment = BigDecimal.ZERO;
		bdTotalRepaidBasedOnRepayment =	LoanServices.getLoansRepaidByLoanApplicationId(loanApplicationId);
		
		//Get the difference between total due and total repaid
		BigDecimal bdTotalDifference = BigDecimal.ZERO;
				
		bdTotalDifference =	dbTotalDueFromSchedule.subtract(bdTotalRepaidBasedOnRepayment);
		
		//if the difference is greater than bdPrincipalAccrued then 
		//return the difference if it is less then return the bdPrincipalAccrued 
		// as the expected principal
		if (bdTotalDifference.compareTo(bdPrincipalAccrued) == 1){
			return bdTotalDifference;
		} else{
			return bdPrincipalAccrued;
		}
	}

	/****
	 * @author Japheth Odonya @when Oct 7, 2014 10:52:06 PM Loan Amount - Total
	 *         Principal Paid
	 * **/
	private static BigDecimal calculateLoanBalance(String partyId,
			String loanApplicationId, BigDecimal loanAmt) {

		BigDecimal bdTotalLoanBalance = BigDecimal.ZERO;
		bdTotalLoanBalance = loanAmt.subtract(getTotalPrincipalPaid(partyId,
				loanApplicationId));
		return bdTotalLoanBalance;
	}

	private static String getEmployeeNumber(String loanApplicationId,
			Delegator delegator) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		GenericValue member = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Loan  ");
		}

		String partyId = "";
		if (loanApplication != null) {
			partyId = loanApplication.getString("partyId");
		} else {
			log.error("######## cannot get Loan Application ");
		}

		// Get Member given partyId
		partyId = partyId.replaceAll(",", "");
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}

		String memberNumber = "";
		if (member != null) {
			memberNumber = member.getString("memberNumber");
		} else {
			log.error("######## cannot get Member ");
		}
		return memberNumber;
	}

	/***
	 * Get Loan Number given Loan Application ID
	 * */
	private static String getLoanNo(String loanApplicationId,
			Delegator delegator) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Loan  ");
		}

		String loanNo = "";
		if (loanApplication != null) {
			loanNo = loanApplication.getString("loanNo");
		} else {
			log.error("######## cannot get Loan Application ");
		}
		return loanNo;
	}

	private static GenericValue getLoanApplication(String loanApplicationId,
			Delegator delegator) {
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get LoanApplication  ");
		}

		return loanApplication;
	}

	private static GenericValue getMember(String loanApplicationId,
			Delegator delegator) {

		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get LoanApplication  ");
		}

		GenericValue member = null;
		try {
			member = delegator.findOne(
					"Member",
					UtilMisc.toMap("partyId",
							loanApplication.getLong("partyId")), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}
		return member;
	}

	private static GenericValue getMemberGivenParty(String partyId,
			Delegator delegator) {

		GenericValue member = null;
		partyId = partyId.replaceAll(",", "");
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}
		return member;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 11:20:40 AM Create an
	 *         AcctgTrans record
	 * */
	private static String createAccountingTransaction(
			GenericValue loanExpectation, String acctgTransType,
			Map<String, String> userLogin, Delegator delegator) {

		GenericValue acctgTrans;
		String acctgTransId;
		// Delegator delegator = loanApplication.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

		// The Member
		String partyId;// = (String) userLogin.get("partyId");
		// loanApplication = getLoanApplication(
		// loanExpectation.getString("loanApplicationId"), delegator);
		// partyId = loanApplication.getString("partyId");
		partyId = loanExpectation.getString("partyId");
		String createdBy = "admin";
		// (String) userLogin.get("userLoginId");

		Timestamp currentDateTime = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		acctgTrans = delegator.makeValidValue("AcctgTrans", UtilMisc.toMap(
				"acctgTransId", acctgTransId, "acctgTransTypeId",
				acctgTransType, "transactionDate", currentDateTime, "isPosted",
				"Y", "postedDate", currentDateTime, "glFiscalTypeId", "ACTUAL",
				"partyId", partyId, "createdByUserLogin", createdBy,
				"createdDate", currentDateTime, "lastModifiedDate",
				currentDateTime, "lastModifiedByUserLogin", createdBy));
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return acctgTransId;
	}

	public static void postTransactionEntry(Delegator delegator,
			BigDecimal bdLoanAmount, String partyId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", partyId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", "Company", "amount",
						bdLoanAmount, "currencyUomId", "KES", "origAmount",
						bdLoanAmount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post an entry");
		}
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jun 4, 2015 12:14:36 PM
	 * 
	 * Updated Posting Method, factors in the branch
	 * */
	public static void postTransactionEntryVersion2(Delegator delegator,
			BigDecimal bdLoanAmount, String branchId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", branchId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", branchId, "amount",
						bdLoanAmount, "currencyUomId", "KES", "origAmount",
						bdLoanAmount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post an entry");
		}
	}


	/***
	 * @author Japheth Odonya @when Sep 11, 2014 6:50:38 PM Totals Due By Member
	 * */
	public static BigDecimal getTotalLoanDue(String partyId) {

		BigDecimal totalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
						.makeCondition("partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalDue = totalDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalDue;
	}

	/****
	 * @author Japheth Odonya @when Sep 11, 2014 7:01:19 PM Get Total Interest
	 *         Due
	 * 
	 * **/
	public static BigDecimal getTotalInterestDue(String partyId) {
		BigDecimal totalInterestDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"INTEREST"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInterestDue = totalInterestDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalInterestDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 7:01:43 PM Get total Insurance
	 *         Due
	 * 
	 * **/
	public static BigDecimal getTotalInsuranceDue(String partyId) {
		BigDecimal totalInsuranceDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"INSURANCE"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInsuranceDue = totalInsuranceDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalInsuranceDue;
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 7:03:21 PM
	 * 
	 *         Get Total Principal
	 * **/
	public static BigDecimal getTotalPrincipalDue(String partyId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"PRINCIPAL"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalPrincipalDue = totalPrincipalDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalPrincipalDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 6:51:10 PM Totals Due By Loan
	 * */
	public static BigDecimal getTotalLoanByLoanDue(String loanApplicationId) {
		BigDecimal totalLoanDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"),

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.EQUALS, Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalLoanDue = totalLoanDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalLoanDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 7:13:14 PM
	 * 
	 *         Get total interest accrued for specific loan
	 * */
	public static BigDecimal getTotalInterestByLoanDue(String loanApplicationId) {
		BigDecimal totalInterestDue = BigDecimal.ZERO;
		//EntityCondition.makeCondition(
		//"isPaid", EntityOperator.EQUALS, "N"), 
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"INTEREST"), EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInterestDue = totalInterestDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}
		
		totalInterestDue = totalInterestDue.subtract(getTotalInterestPaid(loanApplicationId));

		return totalInterestDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 7:12:00 PM Get total insurance
	 *         accrued for specific loan
	 * */
	public static BigDecimal getTotalInsurancByLoanDue(String loanApplicationId) {
		BigDecimal totalInsuranceDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"INSURANCE"), EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInsuranceDue = totalInsuranceDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		totalInsuranceDue = totalInsuranceDue.subtract(getTotalInsurancePaid(loanApplicationId));
		return totalInsuranceDue;
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 7:11:46 PM Get total principal
	 *         accrued for specific loan
	 * **/
	public static BigDecimal getTotalPrincipaByLoanDue(String loanApplicationId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
						.makeCondition("repaymentName", EntityOperator.EQUALS,
								"PRINCIPAL"), EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalPrincipalDue = totalPrincipalDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return totalPrincipalDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 8:35:47 PM
	 * 
	 *         Loan Repayment
	 * 
	 *         Create an Account Transaction of type Loan Repayment
	 * 
	 *         Deduct amount from member deposit (actually debit member
	 *         deposits)
	 * 
	 *         Pay Interest Pay Insurance Pay Principal
	 * 
	 * 
	 * */
	public static String repayLoan(GenericValue loanRepayment,
			Map<String, String> userLogin) {
		log.info("FFFFFFFFF start loan repayment FFFFFFFFFF");

		BigDecimal transactionAmount = loanRepayment
				.getBigDecimal("transactionAmount");
		BigDecimal totalInterestDue = loanRepayment
				.getBigDecimal("totalInterestDue");
		BigDecimal totalInsuranceDue = loanRepayment
				.getBigDecimal("totalInsuranceDue");
		BigDecimal totalPrincipalDue = loanRepayment
				.getBigDecimal("totalPrincipalDue");

		log.info("TTTTTT transactionAmount = " + transactionAmount);
		log.info("TTTTTT totalInterestDue = " + totalInterestDue);
		log.info("TTTTTT totalInsuranceDue = " + totalInsuranceDue);
		log.info("TTTTTT totalPrincipalDue = " + totalPrincipalDue);

		BigDecimal amountRemaining = transactionAmount;

		BigDecimal insuranceAmount = BigDecimal.ZERO;
		BigDecimal interestAmount = BigDecimal.ZERO;
		BigDecimal principalAmount = BigDecimal.ZERO;
		BigDecimal excessAmount = BigDecimal.ZERO;

		// BigDecimal bdTotalLoanRepaid = BigDecimal.ZERO;
		BigDecimal bdLoanBalance = BigDecimal.ZERO;
		bdLoanBalance = LoanServices.getLoanRemainingBalance(loanRepayment
				.getLong("loanApplicationId"));
		// BigDecimal bdLoanAmt = loanRepayment.getBigDecimal("loanAmt");

		if (amountRemaining.compareTo(BigDecimal.ZERO) == 1) {
			// Remove Insurance
			if (totalInsuranceDue.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(totalInsuranceDue) >= 0) {
					insuranceAmount = totalInsuranceDue;
					amountRemaining = amountRemaining
							.subtract(totalInsuranceDue);
				} else {
					insuranceAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			// Remove Interest
			if (totalInterestDue.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(totalInterestDue) >= 0) {
					interestAmount = totalInterestDue;
					amountRemaining = amountRemaining
							.subtract(totalInterestDue);
				} else {
					interestAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			// Remove Principal
			// if (totalPrincipalDue.compareTo(BigDecimal.ZERO) == 1) {
			// if (amountRemaining.compareTo(totalPrincipalDue) >= 0) {
			// principalAmount = totalPrincipalDue;
			// amountRemaining = amountRemaining
			// .subtract(totalPrincipalDue);
			// } else {
			// principalAmount = amountRemaining;
			// amountRemaining = BigDecimal.ZERO;
			//
			// }
			// }

			if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(bdLoanBalance) >= 0) {
					principalAmount = bdLoanBalance;
					amountRemaining = amountRemaining.subtract(bdLoanBalance);
				} else {
					principalAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			if (amountRemaining.compareTo(BigDecimal.ZERO) >= 0) {
				excessAmount = amountRemaining;

				// Deposit Excess to Savings Account

				// TODO
			}

		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		
		String acctgTransType = "LOAN_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanRepayment,
				acctgTransType, userLogin, delegator);

		principalAmount = principalAmount.add(excessAmount);

		loanRepayment.set("interestAmount", interestAmount);
		loanRepayment.set("insuranceAmount", insuranceAmount);
		loanRepayment.set("principalAmount", principalAmount);
		loanRepayment.set("acctgTransId", acctgTransId);
		// loanRepayment.set("excessAmount", excessAmount);

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

		// createLoanRepaymentAccountingTransaction(loanRepayment, userLogin);

		// Return if amount provided is zero
		if (loanRepayment.getBigDecimal("transactionAmount").compareTo(
				BigDecimal.ZERO) != 1) {
			return "";
		}
		// Create Acctg Transa

		// String acctgTransId = createAccountingTransaction(loanRepayment,
		// acctgTransType, userLogin);
		// Debit Member Deposits with repayment amount
		// Get MemberDepositAccount
		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"MEMBERTRANSACTIONACCOUNT", delegator);
		String memberDepositAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		String postingType = "D";
		String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMemberGivenParty(
				loanRepayment.getString("partyId"), delegator).getString(
				"branchId");

		log.info(" ####### Party or Branch or Company in Loan Repayment is ###### "
				+ partyId);
		postTransactionEntry(delegator,
				loanRepayment.getBigDecimal("transactionAmount"), partyId,
				memberDepositAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		// Credit Interest_receivable
		// INTERESTPAYMENT
		postingType = "C";
		entrySequenceId = "00002";
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INTERESTPAYMENT", delegator);
		String accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (interestAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, interestAmount, partyId, accountId,
					postingType, acctgTransId, acctgTransType, entrySequenceId);
		}

		// Credit Insurance_receivable
		// INSURANCEPAYMENT
		postingType = "C";
		entrySequenceId = "00003";
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INSURANCEPAYMENT", delegator);
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (insuranceAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, insuranceAmount, partyId,
					accountId, postingType, acctgTransId, acctgTransType,
					entrySequenceId);
		}
		// Credit Principal Receivable
		// PRINCIPALPAYMENT
		postingType = "C";
		entrySequenceId = "00004";
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"PRINCIPALPAYMENT", delegator);
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);

		if (principalAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, principalAmount, partyId,
					accountId, postingType, acctgTransId, acctgTransType,
					entrySequenceId);
		}

		// Mark the loan expectation as paid
		updateExpactationAsPaid(loanRepayment.getString("partyId"));
		log.info("EEEEEEEEE end loan repayment EEEEEEEEE");

		return "";
	}
	
	/***
	 * Post Loan Repayment without Debiting Cash 
	 *  Like in Salary Processing or C7 Processing
	 * */
	public static Long repayLoanWithoutDebitingCash(GenericValue loanRepayment,
			Map<String, String> userLogin, Long entrySequence) {
		
		//Long entrySequenceId = entrySequence + 1;
		entrySequence = entrySequence + 1;
		log.info("FFFFFFFFF start loan repayment FFFFFFFFFF");

		BigDecimal transactionAmount = loanRepayment
				.getBigDecimal("transactionAmount");
		BigDecimal totalInterestDue = loanRepayment
				.getBigDecimal("totalInterestDue");
		BigDecimal totalInsuranceDue = loanRepayment
				.getBigDecimal("totalInsuranceDue");
		BigDecimal totalPrincipalDue = loanRepayment
				.getBigDecimal("totalPrincipalDue");

		log.info("TTTTTT transactionAmount = " + transactionAmount);
		log.info("TTTTTT totalInterestDue = " + totalInterestDue);
		log.info("TTTTTT totalInsuranceDue = " + totalInsuranceDue);
		log.info("TTTTTT totalPrincipalDue = " + totalPrincipalDue);

		BigDecimal amountRemaining = transactionAmount;

		BigDecimal insuranceAmount = BigDecimal.ZERO;
		BigDecimal interestAmount = BigDecimal.ZERO;
		BigDecimal principalAmount = BigDecimal.ZERO;
		BigDecimal excessAmount = BigDecimal.ZERO;

		// BigDecimal bdTotalLoanRepaid = BigDecimal.ZERO;
		BigDecimal bdLoanBalance = BigDecimal.ZERO;
		bdLoanBalance = LoanServices.getLoanRemainingBalance(loanRepayment
				.getLong("loanApplicationId"));
		// BigDecimal bdLoanAmt = loanRepayment.getBigDecimal("loanAmt");

		if (amountRemaining.compareTo(BigDecimal.ZERO) == 1) {
			// Remove Insurance
			if (totalInsuranceDue.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(totalInsuranceDue) >= 0) {
					insuranceAmount = totalInsuranceDue;
					amountRemaining = amountRemaining
							.subtract(totalInsuranceDue);
				} else {
					insuranceAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			// Remove Interest
			if (totalInterestDue.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(totalInterestDue) >= 0) {
					interestAmount = totalInterestDue;
					amountRemaining = amountRemaining
							.subtract(totalInterestDue);
				} else {
					interestAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			// Remove Principal
			// if (totalPrincipalDue.compareTo(BigDecimal.ZERO) == 1) {
			// if (amountRemaining.compareTo(totalPrincipalDue) >= 0) {
			// principalAmount = totalPrincipalDue;
			// amountRemaining = amountRemaining
			// .subtract(totalPrincipalDue);
			// } else {
			// principalAmount = amountRemaining;
			// amountRemaining = BigDecimal.ZERO;
			//
			// }
			// }

			if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 1) {
				if (amountRemaining.compareTo(bdLoanBalance) >= 0) {
					principalAmount = bdLoanBalance;
					amountRemaining = amountRemaining.subtract(bdLoanBalance);
				} else {
					principalAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			if (amountRemaining.compareTo(BigDecimal.ZERO) >= 0) {
				excessAmount = amountRemaining;

				// Deposit Excess to Savings Account

				// TODO
			}

		}

		principalAmount = principalAmount.add(excessAmount);

		loanRepayment.set("interestAmount", interestAmount);
		loanRepayment.set("insuranceAmount", insuranceAmount);
		loanRepayment.set("principalAmount", principalAmount);
		// loanRepayment.set("excessAmount", excessAmount);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

		// createLoanRepaymentAccountingTransaction(loanRepayment, userLogin);

		// Return if amount provided is zero
		if (loanRepayment.getBigDecimal("transactionAmount").compareTo(
				BigDecimal.ZERO) != 1) {
			return 0L;
		}
		// Create Acctg Transa
		String acctgTransType = "LOAN_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanRepayment,
				acctgTransType, userLogin, delegator);
		// String acctgTransId = createAccountingTransaction(loanRepayment,
		// acctgTransType, userLogin);
		// Debit Member Deposits with repayment amount
		// Get MemberDepositAccount
		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"MEMBERTRANSACTIONACCOUNT", delegator);
		String memberDepositAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		String postingType = "D";
		//String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMemberGivenParty(
				loanRepayment.getString("partyId"), delegator).getString(
				"branchId");

//		log.info(" ####### Party or Branch or Company in Loan Repayment is ###### "
//				+ partyId);
//		postTransactionEntry(delegator,
//				loanRepayment.getBigDecimal("transactionAmount"), partyId,
//				memberDepositAccountId, postingType, acctgTransId,
//				acctgTransType, entrySequenceId);

		// Credit Interest_receivable
		// INTERESTPAYMENT
		postingType = "C";
		//entrySequenceId = "00002";
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INTERESTPAYMENT", delegator);
		String accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (interestAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, interestAmount, partyId, accountId,
					postingType, acctgTransId, acctgTransType, entrySequence.toString());
		}

		// Credit Insurance_receivable
		// INSURANCEPAYMENT
		postingType = "C";
		//entrySequenceId = "00003";
		entrySequence = entrySequence +1;
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INSURANCEPAYMENT", delegator);
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (insuranceAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, insuranceAmount, partyId,
					accountId, postingType, acctgTransId, acctgTransType,
					entrySequence.toString());
		}
		// Credit Principal Receivable
		// PRINCIPALPAYMENT
		postingType = "C";
		//entrySequenceId = "00004";
		entrySequence = entrySequence + 1;
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"PRINCIPALPAYMENT", delegator);
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);

		if (principalAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, principalAmount, partyId,
					accountId, postingType, acctgTransId, acctgTransType,
					entrySequence.toString());
		}

		// Mark the loan expectation as paid
		updateExpactationAsPaid(loanRepayment.getString("partyId"));
		log.info("EEEEEEEEE end loan repayment EEEEEEEEE");

		return entrySequence;
	}

	/***
	 * Now mark expectation for this member as paid
	 * */
	private static void updateExpactationAsPaid(String partyId) {
		// datePaid
		// isPaid
		List<GenericValue> loanExpectationELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),
						EntityCondition.makeCondition("isPaid",
								EntityOperator.EQUALS, "N")

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			loanExpectation.set("isPaid", "Y");
			loanExpectation.set("datePaid", new Timestamp(Calendar
					.getInstance().getTimeInMillis()));
			try {
				delegator.createOrStore(loanExpectation);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void createLoanRepaymentAccountingTransaction(
			GenericValue loanRepayment, Map<String, String> userLogin) {
		// Create an Account Holder Transaction for this disbursement

		BigDecimal transactionAmount = loanRepayment
				.getBigDecimal("transactionAmount");
		// String memberAccountId = getMemberAccountId(loanApplication);
		String memberAccountId = loanRepayment.getString("memberAccountId");
		String transactionType = "LOANREPAYMENT";

		createTransaction(loanRepayment, transactionType, userLogin,
				memberAccountId, transactionAmount, null);
	}

	private static void createTransaction(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId) {
		Delegator delegator = loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = (String) userLogin.get("partyId");
		String partyId = loanApplication.getString("partyId");
		String increaseDecrease;
		increaseDecrease = "D";

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId", partyId,
						"increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountId, "productChargeId",
						productChargeId, "transactionAmount",
						transactionAmount, "transactionType", transactionType));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/****
	 * Get dues by Loan
	 * 
	 * getTotalLoanDue(partyId, loanApplicationId) getTotalInterestDue(partyId,
	 * loanApplicationId) getTotalInsuranceDue(partyId, loanApplicationId)
	 * getTotalPrincipalDue(partyId, loanApplicationId)
	 * */
	/**
	 * @author Japheth Odonya @when Oct 5, 2014 7:00:40 PM
	 * 
	 *         Get Total Loan Due for specific Loan
	 * **/
	public static BigDecimal getTotalLoanDue(String partyId,
			String loanApplicationId) {

		BigDecimal totalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		// loanApplicationId

		// EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalDue = totalDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return (totalDue.subtract(getTotalLoanPaid(partyId, loanApplicationId)))
				.setScale(2, RoundingMode.HALF_UP);
	}

	/***
	 * @author Japheth Odonya @when Oct 5, 2014 7:17:14 PM
	 * 
	 *         Get total interest for the loan
	 * 
	 * */
	public static BigDecimal getTotalInterestDue(String partyId,
			String loanApplicationId) {
		BigDecimal totalInterestDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "INTEREST"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInterestDue = totalInterestDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return (totalInterestDue.subtract(getTotalInterestPaid(partyId,
				loanApplicationId))).setScale(2, RoundingMode.HALF_UP);
	}

	/***
	 * @author Japheth Odonya @when Oct 5, 2014 7:24:17 PM
	 * 
	 *         TotalInsuranceDue for a loanApplication
	 * */
	public static BigDecimal getTotalInsuranceDue(String partyId,
			String loanApplicationId) {
		BigDecimal totalInsuranceDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "INSURANCE"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInsuranceDue = totalInsuranceDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return (totalInsuranceDue.subtract(getTotalInsurancePaid(partyId,
				loanApplicationId))).setScale(2, RoundingMode.HALF_UP);
	}

	/***
	 * @author Japheth Odonya @when Oct 5, 2014 7:27:39 PM
	 * */
	public static BigDecimal getTotalPrincipalDue(String partyId,
			String loanApplicationId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "PRINCIPAL"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			totalPrincipalDue = totalPrincipalDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		return (totalPrincipalDue.subtract(getTotalPrincipalPaid(partyId,
				loanApplicationId))).setScale(2, RoundingMode.HALF_UP);
	}

	/*****
	 * @author Japheth Odonya @when Oct 5, 2014 11:39:00 PM Get Total interest
	 *         paid
	 * */
	public static BigDecimal getTotalInterestPaid(String partyId,
			String loanApplicationId) {
		BigDecimal totalInterestPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {
			if (loanRepayment.getBigDecimal("interestAmount") != null) {
				totalInterestPaid = totalInterestPaid.add(loanRepayment
						.getBigDecimal("interestAmount"));
			}
		}

		return totalInterestPaid;
	}
	
	
	public static BigDecimal getTotalInterestPaid(String loanApplicationId) {
		BigDecimal totalInterestPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
	
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {
			if (loanRepayment.getBigDecimal("interestAmount") != null) {
				totalInterestPaid = totalInterestPaid.add(loanRepayment
						.getBigDecimal("interestAmount"));
			}
		}

		return totalInterestPaid;
	}

	/**
	 * @author Japheth Odonya @when Oct 5, 2014 11:39:19 PM Get Total insurance
	 *         paid
	 * */
	public static BigDecimal getTotalInsurancePaid(String partyId,
			String loanApplicationId) {
		BigDecimal totalInsurancePaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {
			if (loanRepayment.getBigDecimal("insuranceAmount") != null) {
				totalInsurancePaid = totalInsurancePaid.add(loanRepayment
						.getBigDecimal("insuranceAmount"));
			}
		}

		return totalInsurancePaid;
	}
	
	public static BigDecimal getTotalInsurancePaid(String loanApplicationId) {
		BigDecimal totalInsurancePaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),

		loanApplicationId = loanApplicationId.replaceFirst(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {
			if (loanRepayment.getBigDecimal("insuranceAmount") != null) {
				totalInsurancePaid = totalInsurancePaid.add(loanRepayment
						.getBigDecimal("insuranceAmount"));
			}
		}

		return totalInsurancePaid;
	}


	public static BigDecimal getTotalPrincipalPaid(String partyId) {
		BigDecimal bdTotalPrincipalPaid = BigDecimal.ZERO;

		// Get all loans by this member
		List<GenericValue> loanApplicationELI = null; // =
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// List<GenericValue> loansList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanApplicationELI) {
			bdTotalPrincipalPaid = bdTotalPrincipalPaid
					.add(getTotalPrincipalPaid(partyId,
							genericValue.getString("loanApplicationId")));
		}
		return bdTotalPrincipalPaid;
	}

	/***
	 * @author Japheth Odonya @when Oct 5, 2014 11:39:41 PM Get Total Principal
	 *         Paid
	 * */
	public static BigDecimal getTotalPrincipalPaid(String partyId,
			String loanApplicationId) {
		BigDecimal totalPrincipalPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {

			if (loanRepayment.getBigDecimal("principalAmount") != null) {

				totalPrincipalPaid = totalPrincipalPaid.add(loanRepayment
						.getBigDecimal("principalAmount"));
			}
		}

		return totalPrincipalPaid;
	}

	
	
	/***
	 * Get total principal due from 
	 * LoanArmotization
	 * */
	public static BigDecimal getTotalPrincipalDueFromAmortization(Long loanApplicationId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanAmortizationELI = new ArrayList<GenericValue>();
		
		
		LocalDate today = new LocalDate();
		LocalDate lastdayOfNextMonth = (today.plusMonths(2).withDayOfMonth(1));
		lastdayOfNextMonth = lastdayOfNextMonth.minusDays(1);

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		EntityConditionList<EntityExpr> loanAmortizationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								loanApplicationId)
								,
						EntityCondition.makeCondition("expectedPaymentDate",
							EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(lastdayOfNextMonth.toDate().getTime()))
				), EntityOperator.AND);

		try {
			loanAmortizationELI = delegator.findList("LoanAmortization",
					loanAmortizationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanAmortization : loanAmortizationELI) {

			if (loanAmortization.getBigDecimal("principalAmount") != null) {

				totalPrincipalDue = totalPrincipalDue.add(loanAmortization
						.getBigDecimal("principalAmount"));
			}
		}

		return totalPrincipalDue;
	}

	// public static BigDecimal getTotalLoanPaid

	/**
	 * @author Japheth Odonya @when Oct 5, 2014 11:54:49 PM Get total paid
	 * **/
	public static BigDecimal getTotalLoanPaid(String partyId,
			String loanApplicationId) {
		BigDecimal totalLoanPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		partyId = partyId.replaceAll(",", "");
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS, Long.valueOf(partyId)),

						EntityCondition.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								Long.valueOf(loanApplicationId))

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal principalAmount = BigDecimal.ZERO;
		BigDecimal insuranceAmount = BigDecimal.ZERO;
		BigDecimal interestAmount = BigDecimal.ZERO;

		for (GenericValue loanRepayment : loanRepaymentELI) {

			if (loanRepayment.getBigDecimal("principalAmount") != null) {
				principalAmount = loanRepayment
						.getBigDecimal("principalAmount");
			}
			totalLoanPaid = totalLoanPaid.add(principalAmount);

			if (loanRepayment.getBigDecimal("insuranceAmount") != null) {
				insuranceAmount = loanRepayment
						.getBigDecimal("insuranceAmount");
			}
			totalLoanPaid = totalLoanPaid.add(insuranceAmount);

			if (loanRepayment.getBigDecimal("interestAmount") != null) {
				interestAmount = loanRepayment.getBigDecimal("interestAmount");
			}
			totalLoanPaid = totalLoanPaid.add(interestAmount);
		}

		return totalLoanPaid;
	}

}
