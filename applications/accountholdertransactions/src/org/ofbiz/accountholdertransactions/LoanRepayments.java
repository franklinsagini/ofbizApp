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
import org.apache.velocity.runtime.parser.node.GetExecutor;
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
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.treasurymanagement.TreasuryUtility;
import org.ofbiz.webapp.event.EventHandlerException;

/***
 * @author Japheth Odonya @when Sep 10, 2014 7:14:36 PM
 * 
 *         Loan Repayments Processing
 * **/
public class LoanRepayments {
	public static Logger log = Logger.getLogger(LoanRepayments.class);
	private static int ONEHUNDRED = 100;
	public static String REDUCING_BALANCE = "REDUCING_BALANCE";

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

		// if ((localDisbursementDate.getMonthOfYear() == localCurrentDate
		// .getMonthOfYear())
		// && (localDisbursementDate.getYear() == localCurrentDate
		// .getYear())) {
		if (localDisbursementDate.getDayOfMonth() < 15) {
			// repaymentStartDate = new Timestamp(localDisbursementDate.)
			localRepaymentStartDate = localDisbursementDate.plusMonths(1)
					.withDayOfMonth(1);
		} else {
			localRepaymentStartDate = localDisbursementDate.plusMonths(2)
					.withDayOfMonth(1);
		}

		// } else
		// // else repayment start date is first of next month
		// {
		// localRepaymentStartDate = localCurrentDate.plusMonths(1)
		// .withDayOfMonth(1);
		//
		// int repaidPeriods;
		// // = localCurrentDate.m
		// // localDisbursementDate.
		// repaidPeriods = Months.monthsBetween(localDisbursementDate,
		// localCurrentDate).getMonths();
		//
		// if (localDisbursementDate.getDayOfMonth() < 15) {
		// // repaymentStartDate = new Timestamp(localDisbursementDate.)
		// repaidPeriods = repaidPeriods - 1;
		// } else {
		// repaidPeriods = repaidPeriods - 2;
		// }
		//
		// repaymentPeriod = repaymentPeriod - repaidPeriods;
		// }

		if (repaymentPeriod > 0) {
			loanApplication.set("repaymentStartDate", new Timestamp(
					localRepaymentStartDate.toDate().getTime()));
			// loanApplication.set("repaymentPeriod", new
			// Long(repaymentPeriod));
			// loanApplication.set("openingRepaymentPeriod", new
			// Long(repaymentPeriod));
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

		if (repaymentPeriod > 0) {
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

		// Timestamp currentDate = new Timestamp(Calendar.getInstance()
		// .getTimeInMillis());
		Timestamp lastDateOfExpectation = new Timestamp(lastdayOfNextMonth
				.toDate().getTime());

		if (expectationAlreadyGenerated())
			return "";

		// EntityConditionList<EntityExpr> loanRepaymentConditions =
		// EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		// "isAccrued", EntityOperator.EQUALS, "N"),
		// EntityCondition.makeCondition("expectedPaymentDate",
		// EntityOperator.LESS_THAN_EQUAL_TO, lastDateOfExpectation)
		//
		// ), EntityOperator.AND);
		//
		// try {
		// loanAmortizationELI = delegator.findList("LoanAmortization",
		// loanRepaymentConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		// log.info(" ######### Looking for Amortizations that are due #########");
		// if (loanAmortizationELI == null) {
		// log.info(" ######### No Amortizations Due #########");
		// } else {
		// log.info(" ######### Total Number of Amortizations Due is   #########"
		// + loanAmortizationELI.size());
		// }

		Long loanStatusDisbursedId = new Long(6);

		// Get all the ids for disbursed loans
		EntityConditionList<EntityExpr> disbursedLoanIdsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanStatusId", EntityOperator.EQUALS,
						loanStatusDisbursedId)

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
		// lastdayOfNextMonth = lastdayOfNextMonth.minusDays(1);

		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("dateAccrued",
						EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
								firstDayOfThisMonth.toDate().getTime())),
						EntityCondition.makeCondition("dateAccrued",
								EntityOperator.LESS_THAN_EQUAL_TO,
								new Timestamp(lastdayOfthisMonth.toDate()
										.getTime()))

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
				entrySequenceId, userLogin);

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
				entrySequenceId, userLogin);
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
				entrySequenceId, userLogin);

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
				entrySequenceId, userLogin);

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
				entrySequenceId, userLogin);

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
				entrySequenceId, userLogin);
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

		BigDecimal bdLoanBalance = bdLoanAmt.subtract(LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationId));

		// calculateLoanBalance(
		// loanApplication.getString("partyId"),
		// loanApplication.getString("loanApplicationId"), bdLoanAmt);

		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();

		String monthPadded = String.valueOf(month);// paddString(2,
													// String.valueOf(month));
		String monthYear = monthPadded + String.valueOf(year);

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

			// Return the total unpaid or monthly expected based on interest
			// rate and period whichever is
			// greater of the two values

			// bdPrincipalAccrued = getUnpaidPrincipalTotal(bdPrincipalAccrued,
			// loanApplicationId);
			bdPrincipalAccrued = bdPrincipalAccrued.setScale(4,
					RoundingMode.HALF_UP);

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
			bdInterestAccrued = bdInterestAccrued.setScale(4,
					RoundingMode.HALF_UP);
			loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
					.toMap("loanExpectationId", loanExpectationId, "loanNo",
							loanNo, "loanApplicationId", loanApplicationId,
							"employeeNo", employeeNo, "repaymentName",
							"INTEREST", "employeeNames", employeeNames,
							"dateAccrued", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()), "isPaid", "N",
							"isPosted", "N", "amountDue", bdInterestAccrued,
							"amountAccrued", bdInterestAccrued, "month",
							monthYear, "partyId", member.getLong("partyId"),
							"loanAmt", bdLoanAmt));
			listTobeStored.add(loanExpectation);

			loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
					1L);
			bdInsuranceAccrued = bdInsuranceAccrued.setScale(4,
					RoundingMode.HALF_UP);
			loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
					.toMap("loanExpectationId", loanExpectationId, "loanNo",
							loanNo, "loanApplicationId", loanApplicationId,
							"employeeNo", employeeNo, "repaymentName",
							"INSURANCE", "employeeNames", employeeNames,
							"dateAccrued", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()), "isPaid", "N",
							"isPosted", "N", "amountDue", bdInsuranceAccrued,
							"amountAccrued", bdInsuranceAccrued,

							"month", monthYear, "partyId", member
									.getLong("partyId"), "loanAmt", bdLoanAmt));
			listTobeStored.add(loanExpectation);

			// Update Amortization
			// loanAmortization.set("isAccrued", "Y");
			// loanAmortization.set("dateAccrued", new Timestamp(Calendar
			// .getInstance().getTimeInMillis()));

			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
			}
			try {
				delegator.storeAll(listTobeStored);
				// delegator.createOrStore(loanAmortization);
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

		// Get the total principal due based on armotization
		BigDecimal dbTotalDueFromSchedule = BigDecimal.ZERO;
		dbTotalDueFromSchedule = getTotalPrincipalDueFromAmortization(loanApplicationId);
		// Get the total repaid based on repayment
		BigDecimal bdTotalRepaidBasedOnRepayment = BigDecimal.ZERO;
		bdTotalRepaidBasedOnRepayment = LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationId);

		// Get the difference between total due and total repaid
		BigDecimal bdTotalDifference = BigDecimal.ZERO;

		bdTotalDifference = dbTotalDueFromSchedule
				.subtract(bdTotalRepaidBasedOnRepayment);

		// if the difference is greater than bdPrincipalAccrued then
		// return the difference if it is less then return the
		// bdPrincipalAccrued
		// as the expected principal
		if (bdTotalDifference.compareTo(bdPrincipalAccrued) == 1) {
			return bdTotalDifference;
		} else {
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
			String acctgTransId, String acctgTransType, String entrySequenceId,
			Map<String, String> userLogin) {
		GenericValue acctgTransEntry;

		String employeeBranchId = null;
		if ((userLogin != null)
				&& (!userLogin.get("userLoginId").equals("admin"))) {
			employeeBranchId = AccHolderTransactionServices
					.getEmployeeBranch(userLogin.get("partyId"));
		} else {
			employeeBranchId = AccHolderTransactionServices.HQBRANCH;
		}

		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", partyId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", employeeBranchId, "amount",
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
	 * @author Japheth Odonya @when Jun 4, 2015 12:14:36 PM
	 * 
	 *         Updated Posting Method, factors in the branch
	 * */
	public static void postTransactionEntryVersion2(Delegator delegator,
			BigDecimal bdLoanAmount, String branchId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", branchId,
						"glAccountTypeId", acctgTransType, "glAccountId",
						loanReceivableAccount,

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

		// EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"),
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "INTEREST"),
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

		// Get total Opening Insurance Balance
		// for each of his loans
		// add opening interest due
		BigDecimal bdOpeningInterestBalance = getOpeningInterestDue(Long
				.valueOf(partyId));
		totalInterestDue = totalInterestDue.add(bdOpeningInterestBalance);

		// Subtract Interest Repaid
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))

						), EntityOperator.AND);

		List<GenericValue> loanApplicationList = null;
		try {
			loanApplicationList = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		for (GenericValue genericValue : loanApplicationList) {
			totalInterestDue = totalInterestDue
					.subtract(getTotalInterestPaid(genericValue.getLong(
							"loanApplicationId").toString()));
		}

		return totalInterestDue;
	}

	private static BigDecimal getOpeningInterestDue(Long partyId) {
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))

						), EntityOperator.AND);

		List<GenericValue> listLoanApplication = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			listLoanApplication = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		BigDecimal interestOpeningTotal = BigDecimal.ZERO;
		for (GenericValue genericValue : listLoanApplication) {
			if (genericValue.getBigDecimal("interestDue") != null) {
				interestOpeningTotal = interestOpeningTotal.add(genericValue
						.getBigDecimal("interestDue"));
			}
		}

		return interestOpeningTotal;
	}

	/***
	 * Add Opening Insurance Due
	 * */
	private static BigDecimal getOpeningInsuranceDue(Long partyId) {
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))

						), EntityOperator.AND);

		List<GenericValue> listLoanApplication = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			listLoanApplication = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		BigDecimal insuranceOpeningTotal = BigDecimal.ZERO;
		for (GenericValue genericValue : listLoanApplication) {
			if (genericValue.getBigDecimal("insuranceDue") != null) {
				insuranceOpeningTotal = insuranceOpeningTotal.add(genericValue
						.getBigDecimal("insuranceDue"));
			}
		}

		return insuranceOpeningTotal;
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
						"repaymentName", EntityOperator.EQUALS, "INSURANCE"),
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

		totalInsuranceDue = totalInsuranceDue.add(getOpeningInsuranceDue(Long
				.valueOf(partyId)));

		// Subtract Insurance Payments
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"partyId", EntityOperator.EQUALS,
								Long.valueOf(partyId))

						), EntityOperator.AND);

		List<GenericValue> loanApplicationList = null;
		try {
			loanApplicationList = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// BigDecimal totalI
		for (GenericValue genericValue : loanApplicationList) {
			totalInsuranceDue = totalInsuranceDue
					.subtract(getTotalInsurancePaid(genericValue.getLong(
							"loanApplicationId").toString()));
		}

		return totalInsuranceDue;
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 7:03:21 PM
	 * 
	 *         Get Total Principal
	 * **/
//	public static BigDecimal getTotalPrincipalDue(String partyId) {
//		BigDecimal totalPrincipalDue = BigDecimal.ZERO;
//
//		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
//		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
//		partyId = partyId.replaceAll(",", "");
//		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
//				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
//						"isPaid", EntityOperator.EQUALS, "N"), EntityCondition
//						.makeCondition("repaymentName", EntityOperator.EQUALS,
//								"PRINCIPAL"),
//						EntityCondition.makeCondition("partyId",
//								EntityOperator.EQUALS, Long.valueOf(partyId))
//
//				), EntityOperator.AND);
//
//		try {
//			loanExpectationELI = delegator.findList("LoanExpectation",
//					loanExpectationConditions, null, null, null, false);
//
//		} catch (GenericEntityException e2) {
//			e2.printStackTrace();
//		}
//
//		for (GenericValue loanExpectation : loanExpectationELI) {
//			totalPrincipalDue = totalPrincipalDue.add(loanExpectation
//					.getBigDecimal("amountAccrued"));
//		}
//
//		return totalPrincipalDue;
//	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 6:51:10 PM Totals Due By Loan
	 * */
	public static BigDecimal getTotalLoanByLoanDue(String loanApplicationId) {
		BigDecimal totalLoanDue = BigDecimal.ZERO;
		
		totalLoanDue = getTotalExpectedPrincipalAmountByLoanApplicationId(Long.valueOf(loanApplicationId));
//		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
//		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
//		loanApplicationId = loanApplicationId.replaceAll(",", "");
//		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
//				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
//						"isPaid", EntityOperator.EQUALS, "N"),
//
//				EntityCondition.makeCondition("loanApplicationId",
//						EntityOperator.EQUALS, Long.valueOf(loanApplicationId))
//
//				), EntityOperator.AND);
//
//		try {
//			loanExpectationELI = delegator.findList("LoanExpectation",
//					loanExpectationConditions, null, null, null, false);
//
//		} catch (GenericEntityException e2) {
//			e2.printStackTrace();
//		}
//
//		for (GenericValue loanExpectation : loanExpectationELI) {
//			totalLoanDue = totalLoanDue.add(loanExpectation
//					.getBigDecimal("amountAccrued"));
//		}
//
		return totalLoanDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 7:13:14 PM
	 * 
	 *         Get total interest accrued for specific loan
	 * */
	public static BigDecimal getTotalInterestByLoanDue(String loanApplicationId) {
		BigDecimal totalInterestDue = BigDecimal.ZERO;
		// EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"),

		// List<GenericValue> loanExpectationELI = new
		// ArrayList<GenericValue>();
		// loanApplicationId = loanApplicationId.replaceAll(",", "");
		// EntityConditionList<EntityExpr> loanExpectationConditions =
		// EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition
		// .makeCondition("repaymentName", EntityOperator.EQUALS,
		// "INTEREST"), EntityCondition.makeCondition(
		// "loanApplicationId", EntityOperator.EQUALS,
		// Long.valueOf(loanApplicationId))
		//
		// ), EntityOperator.AND);
		//
		// try {
		// loanExpectationELI = delegator.findList("LoanExpectation",
		// loanExpectationConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		//
		// for (GenericValue loanExpectation : loanExpectationELI) {
		// totalInterestDue = totalInterestDue.add(loanExpectation
		// .getBigDecimal("amountAccrued"));
		// }
		//
		// totalInterestDue =
		// totalInterestDue.subtract(getTotalInterestPaid(loanApplicationId));

		// Get total interest expected to have been paid by now
		BigDecimal bdTotalInterestExpectedToDate = getTotalExpectedInterestAmount(loanApplicationId);
		// getTotalExpectedInterestAmountByLoanApplicationId(Long.valueOf(loanApplicationId));
		// Get total interest paid to date
		BigDecimal bdTotalInterestPaidToDate = getTotalInterestPaid(loanApplicationId);
		// Get the difference
		totalInterestDue = bdTotalInterestExpectedToDate
				.subtract(bdTotalInterestPaidToDate);
		return totalInterestDue;
	}

	private static BigDecimal getTotalExpectedInterestAmount(
			String loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "INTEREST"),
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
		BigDecimal totalInterestDue = BigDecimal.ZERO;
		for (GenericValue loanExpectation : loanExpectationELI) {
			totalInterestDue = totalInterestDue.add(loanExpectation
					.getBigDecimal("amountAccrued"));
		}

		// Add opening
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId",
				Long.valueOf(loanApplicationId));

		bdOpeningBalance = loanApplication.getBigDecimal("interestDue");

		if (bdOpeningBalance != null) {
			totalInterestDue = totalInterestDue.add(bdOpeningBalance);
		}
		// totalInterestDue =
		// totalInterestDue.subtract(getTotalInterestPaid(loanApplicationId));
		return totalInterestDue;
	}

	/***
	 * @author Japheth Odonya @when Sep 11, 2014 7:12:00 PM Get total insurance
	 *         accrued for specific loan
	 * */
	public static BigDecimal getTotalInsurancByLoanDue(String loanApplicationId) {
		BigDecimal totalInsuranceDue = BigDecimal.ZERO;

		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// List<GenericValue> loanExpectationELI = new
		// ArrayList<GenericValue>();
		// loanApplicationId = loanApplicationId.replaceAll(",", "");
		// EntityConditionList<EntityExpr> loanExpectationConditions =
		// EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition
		// .makeCondition("repaymentName", EntityOperator.EQUALS,
		// "INSURANCE"), EntityCondition.makeCondition(
		// "loanApplicationId", EntityOperator.EQUALS,
		// Long.valueOf(loanApplicationId))
		//
		// ), EntityOperator.AND);
		//
		// try {
		// loanExpectationELI = delegator.findList("LoanExpectation",
		// loanExpectationConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		//
		// for (GenericValue loanExpectation : loanExpectationELI) {
		// totalInsuranceDue = totalInsuranceDue.add(loanExpectation
		// .getBigDecimal("amountAccrued"));
		// }

		// Total Insurance Expected to date
		BigDecimal bdTotalInsuranceExpectedToDate = getTotalInsuranceByLoanExpected(loanApplicationId);
		// getTotalExpectedInsuranceAmountByLoanApplicationId(Long.valueOf(loanApplicationId));
		// Total Insurance Paid to Date
		BigDecimal bdTotalInsurancePaidToDate = getTotalInsurancePaid(loanApplicationId);
		// Get the difference

		// totalInsuranceDue =
		// totalInsuranceDue.subtract(getTotalInsurancePaid(loanApplicationId));
		totalInsuranceDue = bdTotalInsuranceExpectedToDate
				.subtract(bdTotalInsurancePaidToDate);

		return totalInsuranceDue;
	}

	private static BigDecimal getTotalInsuranceByLoanExpected(
			String loanApplicationId) {

		BigDecimal bdTotalInsuranceExpected = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"repaymentName", EntityOperator.EQUALS, "INSURANCE"),
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
			bdTotalInsuranceExpected = bdTotalInsuranceExpected
					.add(loanExpectation.getBigDecimal("amountAccrued"));
		}

		// Get Insurance opening balance
		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId",
				Long.valueOf(loanApplicationId));

		BigDecimal bdInsuranceOpening = loanApplication
				.getBigDecimal("insuranceDue");

		if (bdInsuranceOpening != null) {
			bdTotalInsuranceExpected = bdTotalInsuranceExpected
					.add(bdInsuranceOpening);
		}
		return bdTotalInsuranceExpected;
	}

	/**
	 * @author Japheth Odonya @when Sep 11, 2014 7:11:46 PM Get total principal
	 *         accrued for specific loan
	 * **/
	public static BigDecimal getTotalPrincipaByLoanDue(String loanApplicationId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		log.info("REEEEEEEEEEEEEEEEEEEEEL ---- totalPrincipalDue "+totalPrincipalDue);
		totalPrincipalDue = getTotalPrincipalDue(Long.valueOf(loanApplicationId));
				
//				getTotalExpectedPrincipalAmountByLoanApplicationId(Long
//				.valueOf(loanApplicationId));
		
		log.info("REEEEEEEEEEEEEEEEEEEEEL ---- totalPrincipalDue "+totalPrincipalDue+" loanApplicationId "+loanApplicationId);

		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// List<GenericValue> loanExpectationELI = new
		// ArrayList<GenericValue>();
		// loanApplicationId = loanApplicationId.replaceAll(",", "");
		// EntityConditionList<EntityExpr> loanExpectationConditions =
		// EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		// "isPaid", EntityOperator.EQUALS, "N"), EntityCondition
		// .makeCondition("repaymentName", EntityOperator.EQUALS,
		// "PRINCIPAL"), EntityCondition.makeCondition(
		// "loanApplicationId", EntityOperator.EQUALS,
		// Long.valueOf(loanApplicationId))
		//
		// ), EntityOperator.AND);
		//
		// try {
		// loanExpectationELI = delegator.findList("LoanExpectation",
		// loanExpectationConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		//
		// for (GenericValue loanExpectation : loanExpectationELI) {
		// totalPrincipalDue = totalPrincipalDue.add(loanExpectation
		// .getBigDecimal("amountAccrued"));
		// }

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
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String acctgTransType = "LOAN_RECEIVABLE";
		String acctgTransId = createAccountingTransaction(loanRepayment,
				acctgTransType, userLogin, delegator);

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

					// Set loan as cleared

					Long loanStatusId = LoanUtilities
							.getLoanStatusId("CLEARED");
					GenericValue loanApplication = LoanUtilities
							.getEntityValue("LoanApplication",
									"loanApplicationId",
									loanRepayment.getLong("loanApplicationId"));
					loanApplication.set("loanStatusId", loanStatusId);
					try {
						delegator.createOrStore(loanApplication);
					} catch (GenericEntityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					principalAmount = amountRemaining;
					amountRemaining = BigDecimal.ZERO;

				}
			}

			if (amountRemaining.compareTo(BigDecimal.ZERO) > 0) {
				excessAmount = amountRemaining;

				// Deposit Excess to Savings Account
				GenericValue loanApplication = LoanUtilities.getEntityValue(
						"LoanApplication", "loanApplicationId",
						loanRepayment.getLong("loanApplicationId"));
				String memberAccountId = LoanUtilities
						.getMemberAccountIdGivenMemberAndAccountCode(
								loanApplication.getLong("partyId"),
								AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
				AccHolderTransactionServices.cashDepositFromStationProcessing(
						excessAmount, Long.valueOf(memberAccountId), userLogin,
						"DEPOSITFROMEXCESS", acctgTransId);

				// TODO
			}

		}

		// principalAmount = principalAmount.add(excessAmount);

		loanRepayment.set("interestAmount", interestAmount);
		loanRepayment.set("insuranceAmount", insuranceAmount);
		loanRepayment.set("principalAmount", principalAmount);
		loanRepayment.set("acctgTransId", acctgTransId);
		loanRepayment.set("excessAmount", excessAmount);

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
		String employeeBranchId = AccHolderTransactionServices
				.getEmployeeBranch(userLogin.get("partyId"));
		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"MEMBERTRANSACTIONACCOUNT", delegator);
		// String memberDepositAccountId = accountHolderTransactionSetup
		// .getString("memberDepositAccId");
		String accountToDebit = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");

		// if CASH then its teller else its Bank/Cheque Account ID
		if (loanRepayment.getString("repaymentMode").equals("CASH")) {
			accountToDebit = TreasuryUtility.getTellerAccountId(userLogin);

			log.info("CCCCCCC Cash Loan Repayment");
		} else {
			// Get the Branch Bank Account Id
			GenericValue branch = LoanUtilities.getEntityValue("PartyGroup",
					"partyId", employeeBranchId);
			// chequeAccountId
			accountToDebit = branch.getString("chequeAccountId");
			log.info("CCCCCCC Cheque Loan Repayment");
		}

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
				accountToDebit, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin);

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
					postingType, acctgTransId, acctgTransType, entrySequenceId,
					userLogin);
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
					entrySequenceId, userLogin);
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
					entrySequenceId, userLogin);
		}

		// Mark the loan expectation as paid
		updateExpactationAsPaid(loanRepayment.getString("partyId"));
		log.info("EEEEEEEEE end loan repayment EEEEEEEEE");

		// Add to AccHolderTransactions repayLoan
		// AccHolderTransactionServices.cashDepositLoan(transactionAmount,
		// loanRepayment.getLong("loanApplicationId"), userLogin,
		// "LOANCASHDEPOSIT", acctgTransId);
		// AccHolderTransactionServices.cashDeposit(transactionAmount,
		// memberAccountId, userLogin, "LOANCASHDEPOSIT");

		if (loanRepayment.getString("repaymentMode").equals("CASH")) {
			AccHolderTransactionServices.cashDepositLoan(transactionAmount,
					loanRepayment.getLong("loanApplicationId"), userLogin,
					"LOANCASHPAY", acctgTransId);
		} else {
			AccHolderTransactionServices.cashDepositLoan(transactionAmount,
					loanRepayment.getLong("loanApplicationId"), userLogin,
					"LOANCHEQUEPAY", acctgTransId);

		}
		// getTotalCashWithdrawalToday
		return "";
	}

	/***
	 * Post Loan Repayment without Debiting Cash Like in Salary Processing or C7
	 * Processing
	 * */
	public static Long repayLoanWithoutDebitingCash(GenericValue loanRepayment,
			Map<String, String> userLogin, Long entrySequence) {

		// Long entrySequenceId = entrySequence + 1;
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

			if (amountRemaining.compareTo(BigDecimal.ZERO) > 0) {
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
		// String entrySequenceId = "00001";
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = getMemberGivenParty(
				loanRepayment.getString("partyId"), delegator).getString(
				"branchId");

		// log.info(" ####### Party or Branch or Company in Loan Repayment is ###### "
		// + partyId);
		// postTransactionEntry(delegator,
		// loanRepayment.getBigDecimal("transactionAmount"), partyId,
		// memberDepositAccountId, postingType, acctgTransId,
		// acctgTransType, entrySequenceId);

		// Credit Interest_receivable
		// INTERESTPAYMENT
		postingType = "C";
		// entrySequenceId = "00002";
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INTERESTPAYMENT", delegator);
		String accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (interestAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, interestAmount, partyId, accountId,
					postingType, acctgTransId, acctgTransType,
					entrySequence.toString(), userLogin);
		}

		// Credit Insurance_receivable
		// INSURANCEPAYMENT
		postingType = "C";
		// entrySequenceId = "00003";
		entrySequence = entrySequence + 1;
		accountHolderTransactionSetup = getAccountHolderTransactionSetupRecord(
				"INSURANCEPAYMENT", delegator);
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		if (insuranceAmount.compareTo(BigDecimal.ZERO) == 1) {
			postTransactionEntry(delegator, insuranceAmount, partyId,
					accountId, postingType, acctgTransId, acctgTransType,
					entrySequence.toString(), userLogin);
		}
		// Credit Principal Receivable
		// PRINCIPALPAYMENT
		postingType = "C";
		// entrySequenceId = "00004";
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
					entrySequence.toString(), userLogin);
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

		GenericValue loanAppEntity = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId",
				Long.valueOf(loanApplicationId));

		if (loanAppEntity.getBigDecimal("interestDue") != null) {
			totalInterestDue = totalInterestDue.add(loanAppEntity
					.getBigDecimal("interestDue"));
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

		GenericValue loanAppEntity = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId",
				Long.valueOf(loanApplicationId));

		if (loanAppEntity.getBigDecimal("insuranceDue") != null) {
			totalInsuranceDue = totalInsuranceDue.add(loanAppEntity
					.getBigDecimal("insuranceDue"));
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

		totalPrincipalDue = getTotalExpectedPrincipalAmountByLoanApplicationId(Long.valueOf(loanApplicationId));

		return (totalPrincipalDue.subtract(getTotalPrincipalPaid(partyId,
				loanApplicationId))).setScale(2, RoundingMode.HALF_UP);
		//return totalPrincipalDue;
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
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
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
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
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

	/****
	 * @author Japheth Odonya @when Jul 11, 2015 1:03:03 PM
	 * 
	 *         Get total principal paid between @startDate and @endDate
	 * 
	 *         This is important for getting remittance payments
	 * */
	public static BigDecimal getTotalPrincipalPaid(String loanApplicationId,
			Timestamp startDate, Timestamp endDate) {
		BigDecimal totalPrincipalPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId)),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.GREATER_THAN_EQUAL_TO, startDate),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.LESS_THAN_EQUAL_TO, endDate)

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

	/****
	 * @author Japheth Odonya @when Jul 11, 2015 1:03:45 PM
	 * 
	 *         Get total Interest Paid between startDate and endDate
	 * 
	 *         For Remittance Repayment values
	 * */
	public static BigDecimal getTotalInterestPaid(String loanApplicationId,
			Timestamp startDate, Timestamp endDate) {
		BigDecimal interestAmount = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId)),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.GREATER_THAN_EQUAL_TO, startDate),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.LESS_THAN_EQUAL_TO, endDate)

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {

			if (loanRepayment.getBigDecimal("interestAmount") != null) {

				interestAmount = interestAmount.add(loanRepayment
						.getBigDecimal("interestAmount"));
			}
		}

		return interestAmount;
	}

	/***
	 * @author Japheth Odonya @when Jul 11, 2015 1:04:13 PM Get total Insurance
	 *         Paid between @startDate and @endDate
	 * 
	 *         For Remittance Processing
	 * */
	public static BigDecimal getTotalInsurancePaid(String loanApplicationId,
			Timestamp startDate, Timestamp endDate) {
		BigDecimal insuranceAmount = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();
		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId)),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.GREATER_THAN_EQUAL_TO, startDate),

				EntityCondition.makeCondition("createdStamp",
						EntityOperator.LESS_THAN_EQUAL_TO, endDate)

				), EntityOperator.AND);

		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanRepayment : loanRepaymentELI) {

			if (loanRepayment.getBigDecimal("insuranceAmount") != null) {

				insuranceAmount = insuranceAmount.add(loanRepayment
						.getBigDecimal("insuranceAmount"));
			}
		}
		return insuranceAmount;
	}

	/****
	 * @author Japheth Odonya @when Jul 10, 2015 12:17:34 AM Total Principal
	 *         Paid
	 * 
	 * */
	public static BigDecimal getTotalPrincipalPaid(Long loanApplicationId) {
		BigDecimal totalPrincipalPaid = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.EQUALS, loanApplicationId)

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

		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId", loanApplicationId);
		BigDecimal outstandingBalance = loanApplication
				.getBigDecimal("outstandingBalance");

		if (outstandingBalance != null) {
			BigDecimal repaidAmount = loanApplication.getBigDecimal("loanAmt")
					.subtract(outstandingBalance);
			totalPrincipalPaid = totalPrincipalPaid.add(repaidAmount);
		}
		return totalPrincipalPaid;
	}

	/***
	 * Get total principal due from LoanArmotization
	 * */
	public static BigDecimal getTotalPrincipalDueFromAmortization(
			Long loanApplicationId) {
		BigDecimal totalPrincipalDue = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanAmortizationELI = new ArrayList<GenericValue>();

		LocalDate today = new LocalDate();
		LocalDate lastdayOfNextMonth = (today.plusMonths(2).withDayOfMonth(1));
		lastdayOfNextMonth = lastdayOfNextMonth.minusDays(1);

		// EntityCondition.makeCondition( "isPaid", EntityOperator.EQUALS, "N"),
		EntityConditionList<EntityExpr> loanAmortizationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						loanApplicationId), EntityCondition.makeCondition(
						"expectedPaymentDate",
						EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(
								lastdayOfNextMonth.toDate().getTime()))),
						EntityOperator.AND);

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

			// if (loanRepayment.getBigDecimal("insuranceAmount") != null) {
			// insuranceAmount = loanRepayment
			// .getBigDecimal("insuranceAmount");
			// }
			// totalLoanPaid = totalLoanPaid.add(insuranceAmount);
			//
			// if (loanRepayment.getBigDecimal("interestAmount") != null) {
			// interestAmount = loanRepayment.getBigDecimal("interestAmount");
			// }
			// totalLoanPaid = totalLoanPaid.add(interestAmount);
		}

		return totalLoanPaid;
	}

	/******
	 * getTransactionId
	 * */
	public static String getTransactionId(GenericValue loanRepayment) {
		String acctgTransId = "";
		Long loanRepaymentId = loanRepayment.getLong("loanRepaymentId");

		GenericValue repayment = getLoanRepayment(loanRepaymentId);
		acctgTransId = repayment.getString("acctgTransId");
		return acctgTransId;
	}

	private static GenericValue getLoanRepayment(Long loanRepaymentId) {
		GenericValue loanRepayment = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanRepayment = delegator.findOne("LoanRepayment",
					UtilMisc.toMap("loanRepaymentId", loanRepaymentId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Loan Repayment  ");
		}

		return loanRepayment;
	}

	public static BigDecimal getTotalPrincipalDue(Long loanApplicationId) {

		
		
		// Get total principal that should have been paid from disbursement date
		// to now
//		BigDecimal bdTotalExpectedPrincipalAmountByToday = getTotalExpectedPrincipalAmountByLoanApplicationId(loanApplicationId);
//		log.info("TTTTTTTTTTTTTTTT PPPPPPPPPPP Due bdTotalExpectedPrincipalAmountByToday "+bdTotalExpectedPrincipalAmountByToday);
//		// Get total principal amount paid from disbursement date to now
//		BigDecimal bdTotalRepaidPrincipalAmountByToday = getTotalPrincipalPaid(loanApplicationId);
//		log.info("TTTTTTTTTTTTTTTT PPPPPPPPPPP What Should be paid Due bdTotalExpectedPrincipalAmountByToday "+bdTotalExpectedPrincipalAmountByToday);
//		log.info("TTTTTTTTTTTTTTTT PPPPPPPPPPP What has been paid Due bdTotalExpectedPrincipalAmountByToday "+bdTotalRepaidPrincipalAmountByToday);
//		// Return the difference as the principal due
//		BigDecimal principalDue = bdTotalExpectedPrincipalAmountByToday
//				.subtract(bdTotalRepaidPrincipalAmountByToday);
//		log.info("TTTTTTTTTTTTTTTT PPPPPPPPPPP The difference is Due bdTotalExpectedPrincipalAmountByToday "+principalDue);
//
//		log.info("TTTTTTTTTTTTTTTT PPPPPPPPPPP  principalDue "+principalDue);
		
		/***
		 * The above piece of code was tried but failed because of the dates issue.
		 * 
		 * Using advice from Kiptoo I will now just compute loan balances using 
		 * current balance
		 * */
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		BigDecimal paymentAmount;
		BigDecimal bdRepaymentInterestAmt = BigDecimal.ZERO;
		/***
		 * Get Loan Product or Loan Type
		 * */
		GenericValue loanProduct = null;
		Long loanProductId = loanApplication.getLong("loanProductId");
		//loanProductId = loanProductId.replaceAll(",", "");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Determine the Deduction Type
		String deductionType = null;
		deductionType = loanProduct.getString("deductionType");
		
		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		//.subtract(bdTotalRepaidLoan);
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
		"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		//openingRepaymentPeriod
		int iRepaymentPeriod;
		//if (loanApplication.getLong("openingRepaymentPeriod") != null){
		//	iRepaymentPeriod = loanApplication.getLong("openingRepaymentPeriod").intValue();
		//} else{
		iRepaymentPeriod = loanApplication.getLong("repaymentPeriod").intValue();
		

		if (deductionType.equals(REDUCING_BALANCE)) {
			paymentAmount =AmortizationServices.calculateReducingBalancePaymentAmount(dbLoanAmt,
					bdInterestRatePM, iRepaymentPeriod);
		} else {
			paymentAmount = AmortizationServices.calculateFlatRatePaymentAmount(dbLoanAmt,
					bdInterestRatePM, iRepaymentPeriod);
		}
		
		//Get Interest
		BigDecimal bdInterestAmt = BigDecimal.ZERO;
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		if (deductionType.equals(REDUCING_BALANCE)){
			bdRepaymentInterestAmt = bdLoanBalance.multiply(bdInterestRatePM);
		} else{
			bdRepaymentInterestAmt = dbLoanAmt
					.multiply(bdInterestRatePM);
		}
				
		//getTotalInterestByLoanDue(loanApplicationId.toString());
		
		BigDecimal principalDue = paymentAmount.subtract(bdInterestAmt);
		
		return principalDue;
	}

	/****
	 * @author Japheth Odonya @when Jul 9, 2015 10:56:40 PM
	 * 
	 *         Total Expected Principal Amount
	 * */
	private static BigDecimal getTotalExpectedPrincipalAmountByLoanApplicationId(
			Long loanApplicationId) {
		Long loanApplicationIdLog = loanApplicationId;

		BigDecimal bdTotalExpectedPrincipalAmount = BigDecimal.ZERO;

		BigDecimal bdTotalRepaidLoan = LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationIdLog);
		
		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId", loanApplicationId);
		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		// .subtract(bdTotalRepaidLoan);
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		// openingRepaymentPeriod
		int iRepaymentPeriod;
		// if (loanApplication.getLong("openingRepaymentPeriod") != null){
		// iRepaymentPeriod =
		// loanApplication.getLong("openingRepaymentPeriod").intValue();
		// } else{
		iRepaymentPeriod = loanApplication.getLong("repaymentPeriod")
				.intValue();
		// }

		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount;

		/***
		 * Get Loan Product or Loan Type
		 * */
		GenericValue loanProduct = null;
		String loanProductId = loanApplication.getString("loanProductId");
		loanProductId = loanProductId.replaceAll(",", "");

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Determine the Deduction Type
		String deductionType = null;
		deductionType = loanProduct.getString("deductionType");

		if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
			paymentAmount = AmortizationServices
					.calculateReducingBalancePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		} else {
			paymentAmount = AmortizationServices
					.calculateFlatRatePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		}
		// This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;

		int iAmortizationCount = 0;

		Timestamp repaymentDate = null;
		repaymentDate = loanApplication.getTimestamp("repaymentStartDate");

		if (repaymentDate == null) {
			
			log.info("RRRRRRRRRRRRRRRRRRRR repaymentStartDate is null getting a new one ...");
			
			repaymentDate = LoanServices
					.calculateLoanRepaymentStartDate(loanApplication);
			log.info("RRRRRRRRRRRRRRRRRRRR repaymentStartDate we got is ..."+repaymentDate);
		}
		
		log.info(" Repayment start date "+repaymentDate+"  loanApplicationId "+loanApplicationId);

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		// Get Insurance Rate
		BigDecimal bdInsuranceRate = AmortizationServices
				.getInsuranceRate(loanApplication);
		BigDecimal bdInsuranceAmount;

		while ((repaymentDate.compareTo(currentDate) <= 0)
				&& (iAmortizationCount < iRepaymentPeriod)) {
			
			iAmortizationCount++;

			if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
				bdRepaymentInterestAmt = bdPreviousBalance
						.multiply(bdInterestRatePM);
			} else {
				bdRepaymentInterestAmt = dbLoanAmt.multiply(bdInterestRatePM);
			}

			dbRepaymentPrincipalAmt = paymentAmount
					.subtract(bdRepaymentInterestAmt);

			bdTotalExpectedPrincipalAmount = bdTotalExpectedPrincipalAmount
					.add(dbRepaymentPrincipalAmt);

			bdPreviousBalance = bdPreviousBalance
					.subtract(dbRepaymentPrincipalAmt);

			// Insurance Amount = insuranceRate times balance divide by 100
			bdInsuranceAmount = bdInsuranceRate.multiply(
					bdPreviousBalance.setScale(6, RoundingMode.HALF_UP))
					.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			// loanApplicationId = loanApplicationId.replaceAll(",", "");

			repaymentDate = AmortizationServices
					.calculateNextPaymentDate(repaymentDate);
			
			log.info(" RRRRRRRRRRRRRRRRRRREEEEEEEEEEEAL bdTotalExpectedPrincipalAmount "+bdTotalExpectedPrincipalAmount+"  loanApplicationId "+loanApplicationId);
		}
		return bdTotalExpectedPrincipalAmount;
	}

	/****
	 * @author Japheth Odonya @when Jul 9, 2015 10:58:09 PM Total Interested
	 *         Expected
	 * */
	private static BigDecimal getTotalExpectedInterestAmountByLoanApplicationId(
			Long loanApplicationId) {
		// Long loanApplicationIdLog = loanApplicationId;

		BigDecimal bdTotalExpectedInterestAmount = BigDecimal.ZERO;

		// BigDecimal bdTotalRepaidLoan =
		// LoanServices.getLoansRepaidByLoanApplicationId(loanApplicationIdLog);
		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId", loanApplicationId);

		// if loan is cleared return zero
		Long loanClearedStatusId = LoanUtilities.getLoanStatusId("CLEARED");

		Long loanDefaultedStatusId = LoanUtilities.getLoanStatusId("DEFAULTED");

		if (loanClearedStatusId == loanApplication.getLong("loanStatusId")) {
			return BigDecimal.ZERO;
		}

		if (loanDefaultedStatusId == loanApplication.getLong("loanStatusId")) {
			return BigDecimal.ZERO;
		}

		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		// .subtract(bdTotalRepaidLoan);
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		// openingRepaymentPeriod
		int iRepaymentPeriod;
		// if (loanApplication.getLong("openingRepaymentPeriod") != null){
		// iRepaymentPeriod =
		// loanApplication.getLong("openingRepaymentPeriod").intValue();
		// } else{
		iRepaymentPeriod = loanApplication.getLong("repaymentPeriod")
				.intValue();
		// }

		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount;

		/***
		 * Get Loan Product or Loan Type
		 * */
		GenericValue loanProduct = null;
		String loanProductId = loanApplication.getString("loanProductId");
		loanProductId = loanProductId.replaceAll(",", "");

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Determine the Deduction Type
		String deductionType = null;
		deductionType = loanProduct.getString("deductionType");

		if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
			paymentAmount = AmortizationServices
					.calculateReducingBalancePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		} else {
			paymentAmount = AmortizationServices
					.calculateFlatRatePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		}
		// This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;

		int iAmortizationCount = 0;

		Timestamp repaymentDate = null;
		repaymentDate = loanApplication.getTimestamp("repaymentStartDate");

		if (repaymentDate == null) {
			repaymentDate = LoanServices
					.calculateLoanRepaymentStartDate(loanApplication);
		}

		log.info("OOOOOOOO Old repayment start date ::::: " + repaymentDate);

		if (loanApplication.getBigDecimal("interestDue") != null) {
			// Update repayment date to when import occurred
			Timestamp importDate = loanApplication.getTimestamp("createdStamp");
			Calendar importCal = Calendar.getInstance();
			importCal.setTimeInMillis(importDate.getTime());

			Calendar calRepayment = Calendar.getInstance();
			calRepayment.setTimeInMillis(repaymentDate.getTime());
			calRepayment.set(Calendar.YEAR, importCal.get(Calendar.YEAR));
			calRepayment.set(Calendar.MONTH, importCal.get(Calendar.MONTH));
			// Date repaymentDateDate = calR

			repaymentDate = new Timestamp(calRepayment.getTimeInMillis());
			log.info("NNNNNNNNNN New repayment start date ::::: "
					+ repaymentDate);
		}

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		// Get Insurance Rate
		BigDecimal bdInsuranceRate = AmortizationServices
				.getInsuranceRate(loanApplication);
		BigDecimal bdInsuranceAmount;

		while ((repaymentDate.compareTo(currentDate) <= 0)
				&& (iAmortizationCount < iRepaymentPeriod)) {
			iAmortizationCount++;

			if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
				bdRepaymentInterestAmt = bdPreviousBalance
						.multiply(bdInterestRatePM);
			} else {
				bdRepaymentInterestAmt = dbLoanAmt.multiply(bdInterestRatePM);
			}

			dbRepaymentPrincipalAmt = paymentAmount
					.subtract(bdRepaymentInterestAmt);

			bdTotalExpectedInterestAmount = bdTotalExpectedInterestAmount
					.add(bdRepaymentInterestAmt);

			bdPreviousBalance = bdPreviousBalance
					.subtract(dbRepaymentPrincipalAmt);

			// Insurance Amount = insuranceRate times balance divide by 100
			bdInsuranceAmount = bdInsuranceRate.multiply(
					bdPreviousBalance.setScale(6, RoundingMode.HALF_UP))
					.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			// loanApplicationId = loanApplicationId.replaceAll(",", "");

			repaymentDate = AmortizationServices
					.calculateNextPaymentDate(repaymentDate);
		}

		if (loanApplication.getBigDecimal("interestDue") != null) {
			bdTotalExpectedInterestAmount = bdTotalExpectedInterestAmount
					.add(loanApplication.getBigDecimal("interestDue"));
		}
		return bdTotalExpectedInterestAmount;
	}

	/****
	 * @author Japheth Odonya @when Jul 9, 2015 10:58:26 PM Total Insurance
	 *         Expected
	 * 
	 * */
	private static BigDecimal getTotalExpectedInsuranceAmountByLoanApplicationId(
			Long loanApplicationId) {
		BigDecimal bdTotalExpectedInsuranceAmount = BigDecimal.ZERO;

		BigDecimal bdTotalRepaidLoan = LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationId);
		GenericValue loanApplication = LoanUtilities.getEntityValue(
				"LoanApplication", "loanApplicationId", loanApplicationId);

		// if loan is cleared return zero
		Long loanClearedStatusId = LoanUtilities.getLoanStatusId("CLEARED");
		Long loanDefaultedStatusId = LoanUtilities.getLoanStatusId("DEFAULTED");

		if (loanClearedStatusId == loanApplication.getLong("loanStatusId")) {
			return BigDecimal.ZERO;
		}

		if (loanDefaultedStatusId == loanApplication.getLong("loanStatusId")) {
			return BigDecimal.ZERO;
		}

		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		// .subtract(bdTotalRepaidLoan);
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		// openingRepaymentPeriod
		int iRepaymentPeriod;
		// if (loanApplication.getLong("openingRepaymentPeriod") != null){
		// iRepaymentPeriod =
		// loanApplication.getLong("openingRepaymentPeriod").intValue();
		// } else{
		iRepaymentPeriod = loanApplication.getLong("repaymentPeriod")
				.intValue();
		// }

		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount;

		/***
		 * Get Loan Product or Loan Type
		 * */
		GenericValue loanProduct = null;
		String loanProductId = loanApplication.getString("loanProductId");
		loanProductId = loanProductId.replaceAll(",", "");

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Determine the Deduction Type
		String deductionType = null;
		deductionType = loanProduct.getString("deductionType");

		if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
			paymentAmount = AmortizationServices
					.calculateReducingBalancePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		} else {
			paymentAmount = AmortizationServices
					.calculateFlatRatePaymentAmount(dbLoanAmt,
							bdInterestRatePM, iRepaymentPeriod);
		}
		// This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;

		int iAmortizationCount = 0;

		Timestamp repaymentDate = null;
		repaymentDate = loanApplication.getTimestamp("repaymentStartDate");

		if (repaymentDate == null) {
			repaymentDate = LoanServices
					.calculateLoanRepaymentStartDate(loanApplication);
		}

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		log.info("OOOOOOOO Old repayment start date ::::: " + repaymentDate);

		if (loanApplication.getBigDecimal("insuranceDue") != null) {
			// Update repayment date to when import occurred
			Timestamp importDate = loanApplication.getTimestamp("createdStamp");
			Calendar importCal = Calendar.getInstance();
			importCal.setTimeInMillis(importDate.getTime());

			Calendar calRepayment = Calendar.getInstance();
			calRepayment.setTimeInMillis(repaymentDate.getTime());
			calRepayment.set(Calendar.YEAR, importCal.get(Calendar.YEAR));
			calRepayment.set(Calendar.MONTH, importCal.get(Calendar.MONTH));
			// Date repaymentDateDate = calR

			repaymentDate = new Timestamp(calRepayment.getTimeInMillis());
			log.info("NNNNNNNNNN New repayment start date ::::: "
					+ repaymentDate);
		}
		// repaymentDate.se

		// Get Insurance Rate
		BigDecimal bdInsuranceRate = AmortizationServices
				.getInsuranceRate(loanApplication);
		BigDecimal bdInsuranceAmount;

		while ((repaymentDate.compareTo(currentDate) <= 0)
				&& (iAmortizationCount < iRepaymentPeriod)) {
			iAmortizationCount++;

			if (deductionType.equals(AmortizationServices.REDUCING_BALANCE)) {
				bdRepaymentInterestAmt = bdPreviousBalance
						.multiply(bdInterestRatePM);
			} else {
				bdRepaymentInterestAmt = dbLoanAmt.multiply(bdInterestRatePM);
			}

			dbRepaymentPrincipalAmt = paymentAmount
					.subtract(bdRepaymentInterestAmt);

			bdPreviousBalance = bdPreviousBalance
					.subtract(dbRepaymentPrincipalAmt);

			// Insurance Amount = insuranceRate times balance divide by 100
			bdInsuranceAmount = bdInsuranceRate.multiply(
					bdPreviousBalance.setScale(6, RoundingMode.HALF_UP))
					.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			// loanApplicationId = loanApplicationId.replaceAll(",", "");
			bdTotalExpectedInsuranceAmount = bdTotalExpectedInsuranceAmount
					.add(bdInsuranceAmount);

			repaymentDate = AmortizationServices
					.calculateNextPaymentDate(repaymentDate);
		}

		if (loanApplication.getBigDecimal("insuranceDue") != null) {
			bdTotalExpectedInsuranceAmount = bdTotalExpectedInsuranceAmount
					.add(bdTotalExpectedInsuranceAmount);
		}
		return bdTotalExpectedInsuranceAmount;
	}

	public static BigDecimal getRepaidAmount(String code, String month,
			String remittanceType, String payrollNumber, String loanNo) {
		// Remittance type can be
		// ACCOUNT
		// INSURANCE
		// INTEREST
		// PRINCIPAL

		Timestamp startDate = getStartDateGivenMonth(month, payrollNumber); // the
																			// day
																			// remittance
																			// was
																			// generated
																			// or
																			// expected
		Timestamp endDate = getEndDateGivenMonth(month, payrollNumber); // the
																		// day
																		// of
																		// the
																		// next
																		// expectation
																		// or
																		// today
																		// of
																		// the
																		// next
																		// expectation
																		// is
																		// missing
		Long memberId = LoanUtilities.getMemberId(payrollNumber);
		if (remittanceType.equals("ACCOUNT")) {
			// Get amount deposited given
			// start date
			// end date
			// and account code
			return AccHolderTransactionServices.getTotalDeposits(code,
					memberId, startDate, endDate);
		} else if (remittanceType.equals("LOAN")) {

			// Split the code to determine if we doing PRINCIPAL (A), INTEREST
			// (B), INSURANCE (C) or BALANCE(D)
			String itemCode = code.substring(code.length() - 1, code.length());
			// System.out.println(" And the code is : "+itemCode);
			// System.out.println(" The loan in question is LLLLLLLLLLLLLLL :::  "+loanNo);
			String loanApplicationId = LoanUtilities
					.getLoanApplicationEntityGivenLoanNo(loanNo)
					.getLong("loanApplicationId").toString();

			if (itemCode.equals("A")) {
				// Doing Principal
				return AccHolderTransactionServices.getTotalPrincipalPaid(
						loanApplicationId, memberId, startDate, endDate);
			} else if (itemCode.equals("B")) {
				// Doing Interest
				return AccHolderTransactionServices.getTotalInterestPaid(
						loanApplicationId, memberId, startDate, endDate);
			} else if (itemCode.equals("C")) {
				// Doing Insurance
				return AccHolderTransactionServices.getTotalInsurancePaid(
						loanApplicationId, memberId, startDate, endDate);
			} else if (itemCode.equals("D")) {
				// Doing Balance
				return BigDecimal.ZERO;
			} else {
				return BigDecimal.ZERO;
			}

			// Get principal paid given
			// start date
			// end date
			// and loan no. The code here is loanNo

		}

		return BigDecimal.ZERO;
	}

	/****
	 * @author Japheth Odonya @when Jul 10, 2015 6:10:02 PM Get end date -
	 *         should be the next expecation date or today if there is no next
	 *         expectation date
	 * */
	private static Timestamp getEndDateGivenMonth(String month,
			String payrollNumber) {
		// Build next month by stripping last 4 digits from month then
		// incrementing the first part and
		// combining again
		String monthValue = month.substring(0, month.length() - 4);
		String yearValue = month.substring(month.length() - 4, month.length());

		System.out.println(" Month --- " + monthValue);
		System.out.println(" Year --- " + yearValue);

		Long longMonthValue = Long.valueOf(monthValue);
		Long longYearValue = Long.valueOf(yearValue);

		Long nextMonthValue = 0L;

		if (longMonthValue.equals(new Long(12))) {
			nextMonthValue = 1L;
			longYearValue = longYearValue + 1;
		} else {
			nextMonthValue = longMonthValue + 1;
		}

		month = nextMonthValue.toString() + longYearValue.toString();

		EntityConditionList<EntityExpr> expectedSentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("payrollNo",
						EntityOperator.EQUALS, payrollNumber)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedSentConditions, null, null,
					null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((expectedPaymentReceivedELI == null)
				|| (expectedPaymentReceivedELI.size() < 1)) {
			return new Timestamp(Calendar.getInstance().getTimeInMillis());
		}

		GenericValue expectedPaymentReceived = null;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			expectedPaymentReceived = genericValue;
		}
		return expectedPaymentReceived.getTimestamp("createdStamp");
	}

	/***
	 * @author Japheth Odonya @when Jul 10, 2015 6:09:27 PM Get start date for
	 *         computing payments
	 * 
	 * */
	private static Timestamp getStartDateGivenMonth(String month,
			String payrollNumber) {

		// startDate = new Timestamp(time)
		// Get the
		EntityConditionList<EntityExpr> expectedSentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("payrollNo",
						EntityOperator.EQUALS, payrollNumber)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedSentConditions, null, null,
					null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((expectedPaymentReceivedELI == null)
				|| (expectedPaymentReceivedELI.size() < 1)) {
			// Return beginning of the month
			// ExpectedPaymentSent
			// DateMidnight first = new DateMidnight().withDayOfMonth(1);
			org.joda.time.DateTime startOfThisMonth = new org.joda.time.DateTime()
					.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
			return new Timestamp(startOfThisMonth.getMillis());
		}

		GenericValue expectedPaymentReceived = null;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			expectedPaymentReceived = genericValue;
		}
		return expectedPaymentReceived.getTimestamp("createdStamp");
	}

}
