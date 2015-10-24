package org.ofbiz.chargeinterest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.AmortizationServices;
import org.ofbiz.loans.LoanServices;

//org.ofbiz.chargeinterest.ChargeInterestServices.chargeStationInterest
public class ChargeInterestServices {
	public static Logger log = Logger.getLogger(ChargeInterestServices.class);

	public static synchronized String chargeStationInterest(
			Long stationMonthInterestManagementId, Map<String, String> userLogin) {

		GenericValue stationMonthInterestManagement = LoanUtilities
				.getEntityValue("StationMonthInterestManagement",
						"stationMonthInterestManagementId",
						stationMonthInterestManagementId);

		Long month = stationMonthInterestManagement.getLong("month");
		Long year = stationMonthInterestManagement.getLong("year");

		String monthYear = month.toString() + year.toString();
		String stationId = stationMonthInterestManagement
				.getString("stationId");

		// Get Station Code
		GenericValue station = LoanUtilities.getEntityValue("Station",
				"stationId", stationId);
		String employerCode = station.getString("employerCode");
		// Check if station has already been charged.
		if (alreadyCharged(stationMonthInterestManagementId))
			return station.getString("name")
					+ " has already been charged for the " + monthYear;

		List<String> stationIdList = LoanUtilities.getStationIds(employerCode
				.trim());
		// DisbursedLoansForActiveMembers

		ExpectTotal expectTotal = null;
		BigDecimal insuranceTotalAmount = BigDecimal.ZERO;
		BigDecimal interestTotalAmount = BigDecimal.ZERO;

		for (String currentStationId : stationIdList) {
			expectTotal = processCurrentStation(currentStationId, employerCode,
					monthYear, userLogin, stationMonthInterestManagementId);
			interestTotalAmount = interestTotalAmount.add(expectTotal
					.getInterestTotalAmount());
			insuranceTotalAmount = insuranceTotalAmount.add(expectTotal
					.getInsuranceTotalAmount());
		}

		// Post Insurance end Interest Accrued

		// update the expectation with transaction id
		String branchId = station.getString("branchId");
				
				//AccHolderTransactionServices
				//.getEmployeeBranch(userLogin.get("partyId"));
		String acctgTransType = "INTEREST_RECEIVABLE";
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanExpectation = delegator.makeValidValue(
				"LoanExpectation",
				UtilMisc.toMap("partyId", Long.valueOf(branchId)));

		String acctgTransId = LoanRepayments.createAccountingTransaction(
				loanExpectation, acctgTransType, userLogin, delegator);

		log.info("WWWWWWWWWWWWWWWW Will now attempt to post interest ... ");
		String setupType = "INTERESTACCRUAL";
		GenericValue accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord(setupType, delegator);
		// LoanAccounting.postDisbursement(loanApplication, userLogin)
		Long sequence = 0L;
		sequence = postInterestAccrued(interestTotalAmount, delegator,
				userLogin, accountHolderTransactionSetup, acctgTransId,
				acctgTransType, branchId, sequence);

		// Post Insuranc
		setupType = "INSURANCEACCRUAL";
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord(setupType, delegator);
		postInsuranceCharge(insuranceTotalAmount, delegator, userLogin,
				accountHolderTransactionSetup, acctgTransId, acctgTransType,
				branchId, sequence);

		// Update the charges or expectation with acctgTransId
		// stationMonthInterestManagementId
		updateChargesWithTransactionId(stationMonthInterestManagementId,
				acctgTransId);
		
		
		//Log station in Charged Interest Log
		Long stationInterestChargeLogId = delegator.getNextSeqIdLong("StationInterestChargeLog");
		GenericValue stationInterestChargeLog = delegator.makeValidValue(
				"StationInterestChargeLog", UtilMisc.toMap(
						"stationInterestChargeLogId",
						stationInterestChargeLogId,

						"createdBy", userLogin.get("userLoginId"),

						"month", month,

						"year", year,

						"stationId", stationId,

						"employerCode", employerCode,

						"employerName", LoanUtilities.getStation(stationId)
								.getString("employerName"),

						"chargedNotCharged", "CHARGED"

				));
		
		try {
			delegator.createOrStore(stationInterestChargeLog);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static boolean alreadyCharged(Long stationMonthInterestManagementId) {
		EntityConditionList<EntityExpr> expectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("stationMonthInterestManagementId",
								EntityOperator.EQUALS,
								stationMonthInterestManagementId)

				), EntityOperator.AND);

		List<GenericValue> expectationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectationELI = delegator.findList("LoanExpectation",
					expectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((expectationELI != null) && (expectationELI.size() > 0))
			return true;

		return false;
	}

	private static boolean alreadyChargedForMonth(
			Long stationMonthInterestManagementId) {

		GenericValue stationMonthInterestManagement = LoanUtilities
				.getEntityValue("StationMonthInterestManagement",
						"stationMonthInterestManagementId",
						stationMonthInterestManagementId);
		String monthYear = stationMonthInterestManagement.getLong("month")
				.toString()
				+ stationMonthInterestManagement.getLong("year").toString();

		EntityConditionList<EntityExpr> expectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("stationMonthInterestManagementId",
								EntityOperator.EQUALS,
								stationMonthInterestManagementId)

				), EntityOperator.AND);

		List<GenericValue> expectationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectationELI = delegator.findList("LoanExpectation",
					expectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((expectationELI != null) && (expectationELI.size() > 0))
			return true;

		return false;
	}

	private static void updateChargesWithTransactionId(
			Long stationMonthInterestManagementId, String acctgTransId) {
		EntityConditionList<EntityExpr> expectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("stationMonthInterestManagementId",
								EntityOperator.EQUALS,
								stationMonthInterestManagementId)

				), EntityOperator.AND);

		List<GenericValue> expectationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectationELI = delegator.findList("LoanExpectation",
					expectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : expectationELI) {
			genericValue.set("acctgTransId", acctgTransId);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//update Station Month Interest Management postingacctgTransId
		
		GenericValue stationMonthInterestManagement = LoanUtilities.getEntityValue("StationMonthInterestManagement", "stationMonthInterestManagementId", stationMonthInterestManagementId);
		stationMonthInterestManagement.set("postingacctgTransId", acctgTransId);
		try {
			delegator.createOrStore(stationMonthInterestManagement);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static ExpectTotal processCurrentStation(String currentStationId,
			String employerCode, String monthYear,
			Map<String, String> userLogin, Long stationMonthInterestManagementId) {

		Calendar cal = Calendar.getInstance();

		Long year = Long.valueOf(monthYear.substring((monthYear.length() - 4),
				monthYear.length()));
		Long month = Long
				.valueOf(monthYear.substring(0, monthYear.length() - 4));
		System.out.println(" The year " + year);
		System.out.println(" The Month " + month);

		cal.set(Calendar.MONTH, month.intValue() - 1);
		cal.set(Calendar.DATE, 16);

		cal.set(Calendar.YEAR, year.intValue());

		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Timestamp interestChargeDate = new Timestamp(cal.getTimeInMillis());

		// now convert GregorianCalendar object to Timestamp object
		// return new Timestamp(cal.getTimeInMillis());
		// System.out.println(" The time will be  llll "+new
		// Timestamp(cal.getTimeInMillis()));

		// Get all the loans for this station
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationId", EntityOperator.EQUALS,
						Long.valueOf(currentStationId)),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, 6L),

				EntityCondition.makeCondition("disbursementDate",
						EntityOperator.LESS_THAN, interestChargeDate)

				), EntityOperator.AND);

		// EntityCondition.makeCondition(
		// "memberStatusId", EntityOperator.EQUALS,
		// 1L)

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList(
					"DisbursedLoansForActiveMembers",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue currStation = LoanUtilities.getEntityValue("Station",
				"stationId", currentStationId);
		log.info("..................... Station ID " + currentStationId
				+ " station name " + currStation.getString("name") + " size "
				+ loanApplicationELI.size());

		ExpectItem expectItem = null;
		ExpectTotal expectTotal = null;
		expectTotal = new ExpectTotal();
		expectTotal.setInsuranceTotalAmount(BigDecimal.ZERO);
		expectTotal.setInterestTotalAmount(BigDecimal.ZERO);
		for (GenericValue genericValue : loanApplicationELI) {
			expectItem = createLoanExpectation(genericValue, delegator,
					monthYear, userLogin, stationMonthInterestManagementId);

			expectTotal.setInsuranceTotalAmount(expectTotal
					.getInsuranceTotalAmount().add(
							expectItem.getInsuranceAmount()));
			expectTotal.setInterestTotalAmount(expectTotal
					.getInterestTotalAmount().add(
							expectItem.getInterestAmount()));
		}

		return expectTotal;
	}

	private static ExpectItem createLoanExpectation(
			GenericValue disbursedLoanView, Delegator delegator,
			String monthYear, Map<String, String> userLogin,
			Long stationMonthInterestManagementId) {

		GenericValue loanExpectation = null;
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		Long loanApplicationId = disbursedLoanView.getLong("loanApplicationId");
		String employeeNo = LoanRepayments.getEmployeeNumber(
				String.valueOf(loanApplicationId), delegator);

		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();

		String loanNo = LoanRepayments.getLoanNo(
				String.valueOf(loanApplicationId), delegator);

		GenericValue member = LoanRepayments.getMember(
				String.valueOf(loanApplicationId), delegator);
		GenericValue loanApplication = LoanRepayments.getLoanApplication(
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
				"interestRatePM").divide(
				new BigDecimal(AccHolderTransactionServices.ONEHUNDRED));
		Long repaymentPeriod = loanApplication.getLong("repaymentPeriod");

		BigDecimal monthlyPayable = AmortizationServices
				.calculateReducingBalancePaymentAmount(bdLoanAmt,
						bdInterestRatePM, repaymentPeriod.intValue());

		BigDecimal bdLoanBalance = bdLoanAmt.subtract(LoanServices
				.getLoansRepaidByLoanApplicationId(loanApplicationId));

		// calculateLoanBalance(
		// loanApplication.getString("partyId"),
		// loanApplication.getString("loanApplicationId"), bdLoanAmt);

		// String monthYear = monthPadded + String.valueOf(year);
		BigDecimal bdInsuranceAccrued = BigDecimal.ZERO;
		BigDecimal bdInterestAccrued = BigDecimal.ZERO;

		if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 1) {

			// INTEREST
			// BigDecimal bdInterestAccrued = loanAmortization
			// .getBigDecimal("interestAmount");
			bdInterestAccrued = bdLoanBalance.multiply(bdInterestRatePM);

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
			bdInsuranceAccrued = bdInsuranceRate.multiply(
					bdLoanBalance.setScale(6, RoundingMode.HALF_UP)).divide(
					new BigDecimal(100), 6, RoundingMode.HALF_UP);

			// Adding Principal
			// loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
			// .toMap("loanExpectationId", loanExpectationId, "loanNo",
			// loanNo, "loanApplicationId", loanApplicationId,
			// "employeeNo", employeeNo, "repaymentName",
			// "PRINCIPAL", "employeeNames", employeeNames,
			// "dateAccrued", new Timestamp(Calendar.getInstance()
			// .getTimeInMillis()), "isPaid", "N",
			// "isPosted", "N",
			//
			// "amountDue", bdPrincipalAccrued, "amountAccrued",
			// bdPrincipalAccrued,
			//
			// "month", monthYear,
			//
			// "partyId", member.getLong("partyId"), "loanAmt",
			// bdLoanAmt));
			//
			// listTobeStored.add(loanExpectation);

			// Add Interest
			loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
					1L);
			bdInterestAccrued = bdInterestAccrued.setScale(4,
					RoundingMode.HALF_UP);
			if (bdInterestAccrued.compareTo(BigDecimal.ZERO) < 1) {
				bdInterestAccrued = BigDecimal.ZERO;
			}

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
							"loanAmt", bdLoanAmt,
							"stationMonthInterestManagementId",
							stationMonthInterestManagementId));
			listTobeStored.add(loanExpectation);

			loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
					1L);
			bdInsuranceAccrued = bdInsuranceAccrued.setScale(4,
					RoundingMode.HALF_UP);

			if (bdInsuranceAccrued.compareTo(BigDecimal.ZERO) < 1)
				bdInsuranceAccrued = BigDecimal.ZERO;

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
									.getLong("partyId"), "loanAmt", bdLoanAmt,
							"stationMonthInterestManagementId",
							stationMonthInterestManagementId));
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

		ExpectItem expectItem = new ExpectItem();
		expectItem.setInsuranceAmount(bdInsuranceAccrued);
		expectItem.setInterestAmount(bdInterestAccrued);

		return expectItem;

	}

	private static Long postInterestAccrued(BigDecimal amount,
			Delegator delegator, Map<String, String> userLogin,
			GenericValue accountHolderTransactionSetup, String acctgTransId,
			String acctgTransType, String branchId, Long sequence) {

		// Post a Debit Entry to accountId
		String postingType = "D";
		String accountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		sequence = sequence + 1;
		String entrySequenceId = sequence.toString();
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = branchId;
		// LoanRepayments.getMember(
		// loanExpectation.getString("loanApplicationId"), delegator)
		// .getString("branchId");

		log.info(" ####### Party or Branch or Company in Intrest Accrued is ###### "
				+ partyId);
		LoanRepayments.postTransactionEntry(delegator, amount, partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin, branchId);

		// Post a Credit Entry to accountId
		postingType = "C";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		LoanRepayments.postTransactionEntry(delegator, amount, partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin, branchId);
		// log.info("### Transaction ID## "+acctgTransId);
		// Creates a record in AcctgTransEntry for Member Deposit Account
		// createMemberDepositEntry(loanExpectation, acctgTransId, userLogin,
		// glAcctTypeIdMemberDepo, delegator);
		return sequence;
	}

	private static Long postInsuranceCharge(BigDecimal amount,
			Delegator delegator, Map<String, String> userLogin,
			GenericValue accountHolderTransactionSetup, String acctgTransId,
			String acctgTransType, String branchId, Long sequence) {
		// String acctgTransType = "CHARGE_RECEIVABLE";
		// String branchId = AccHolderTransactionServices
		// .getEmployeeBranch(userLogin.get("partyId"));
		// GenericValue loanExpectation =
		// delegator.makeValidValue("LoanExpectation", UtilMisc.toMap(
		// "partyId", branchId));
		// String acctgTransId =
		// LoanRepayments.createAccountingTransaction(loanExpectation,
		// acctgTransType, userLogin, delegator);

		// Post a Debit Entry to accountId
		String postingType = "D";
		String accountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		sequence = sequence + 1;
		String entrySequenceId = sequence.toString();
		// String partyId =
		// getLoanApplication(loanExpectation.getString("loanApplicationId"),
		// delegator).getString("partyId");
		String partyId = branchId;

		log.info(" ####### Party or Branch or Company in Insurance Charge is  ###### "
				+ partyId);
		LoanRepayments.postTransactionEntry(delegator, amount, partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin, branchId);

		// Post a Credit Entry to accountId
		postingType = "C";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);
		LoanRepayments.postTransactionEntry(delegator, amount, partyId,
				accountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin, branchId);
		return sequence;
	}

	public static synchronized Boolean stationAlreadyCharged(Long month,
			Long year, String stationId) {
		GenericValue station = LoanUtilities.getEntityValue("Station",
				"stationId", stationId);
		String employerCode = station.getString("employerCode");
		List<String> stationIds = LoanUtilities.getStationIds(employerCode);

		// Check that none of these station ids is in the
		// StationMonthInterestManagment
		Boolean alreadyCharged = false;
		for (String id : stationIds) {
			if (alreadyCharged(month, year, id)) {
				alreadyCharged = true;
			}
		}

		return alreadyCharged;
	}

	private static Boolean alreadyCharged(Long month, Long year, String id) {
		EntityConditionList<EntityExpr> stationMonthInterestManagementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("year", EntityOperator.EQUALS,
						year),

				EntityCondition.makeCondition("stationId",
						EntityOperator.EQUALS, id)

				), EntityOperator.AND);

		List<GenericValue> stationMonthInterestManagementELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationMonthInterestManagementELI = delegator.findList(
					"StationMonthInterestManagement",
					stationMonthInterestManagementConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((stationMonthInterestManagementELI != null)
				&& (stationMonthInterestManagementELI.size() > 0))
			return true;

		return false;
	}

	public static synchronized String processChargeInterestAllStations() {

		// Get all the stations

		List<GenericValue> employerListELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			employerListELI = delegator.findList("EmployerCodeListing", null,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		int count = 0;
		GenericValue station = null;
		Calendar calCurrent = Calendar.getInstance();

		Long month = new Long(calCurrent.get(calCurrent.MONTH)) + 1;
		Long year = new Long(calCurrent.get(calCurrent.YEAR));

		System.out.println("MMMMMMMMMM The month is --- " + month);
		System.out.println("YYYYYYYYYY The year is --- " + year);

		String employerCode = "";
		String stationId = "";
		Boolean allCharged = true;
		
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "system");

		for (GenericValue genericValue : employerListELI) {
			count = count + 1;

			employerCode = genericValue.getString("employerCode");
			stationId = LoanUtilities.getStationId(employerCode);
			System.out.println(count + " ############ Code " + employerCode
					+ " Name " + LoanUtilities.getStationName(employerCode));

			// Check if station has been charged
			station = LoanUtilities.getStation(stationId);

			if (alreadyCharged(month, year, station.getString("stationId"))) {
				System.out.println(" YYYYYYYYY Has been charged already");

			} else {
				System.out.println(" NNNNNNNNN Not been charged");
				allCharged = false;
				// Create the StationMonthInterestManagement record and charge

				Long stationMonthInterestManagementId = addMonthInterestManagement(
						month, year, employerCode, stationId);
				
				
				chargeStationInterest(stationMonthInterestManagementId, userLogin);
				

			}
		}

		if (allCharged)
			return "All stations are already charged ";

		return "success";
	}

	private static Long addMonthInterestManagement(Long month,
			Long year, String employerCode, String stationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		Long stationMonthInterestManagementId = delegator
				.getNextSeqIdLong("StationMonthInterestManagement");

		GenericValue stationMonthInterestManagement = delegator.makeValidValue(
				"StationMonthInterestManagement", UtilMisc.toMap(
						"stationMonthInterestManagementId",
						stationMonthInterestManagementId,

						"isActive", "Y",

						"createdBy", "system",

						"month", month,

						"year", year,

						"stationId", stationId,

						"employerCode", employerCode,

						"employerName", LoanUtilities.getStation(stationId)
								.getString("employerName"),

						"Onlinecode", LoanUtilities.getStation(stationId)
								.getString("Onlinecode")

				));

		try {
			delegator.createOrStore(stationMonthInterestManagement);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stationMonthInterestManagementId;
	}
	
	//ChargeInterestServices
	public static synchronized String resolveLoanClearing() {

		//Get all the loans amounts to be credited and updated
		//For each loan in LoansToResolve get the last transaction id, which is the transaction on which 
		//clearance happened
		List<GenericValue> loansToResolveELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loansToResolveELI = delegator.findList("LoansToResolve",
					null, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		Long loanApplicationId = null;
		
		for (GenericValue genericValue : loansToResolveELI) {
			
			//Get all the loan clear costing
			loanApplicationId = genericValue.getLong("loanApplicationId");
			
			//Get last transaction id
			String acctgTransId = getLastTransactionId(loanApplicationId);
			
			//Count Transactions
			Long transactionCount = null;
			
			if (acctgTransId != null)
			 transactionCount =	countTransactions(acctgTransId);
			
			genericValue.set("acctgTransId", acctgTransId);
			genericValue.set("transactionCount", transactionCount);
			
			
			
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		   
			
		}
		
		return "success";
	}

	private static Long countTransactions(String acctgTransId) {
		EntityConditionList<EntityExpr> expectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("acctgTransId",
								EntityOperator.EQUALS,
								acctgTransId)

				), EntityOperator.AND);

		List<GenericValue> acctgTransEntryELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		try {
			acctgTransEntryELI = delegator.findList("AcctgTransEntry",
					expectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		return Long.valueOf(acctgTransEntryELI.size());
	}

	private static String getLastTransactionId(Long loanApplicationId) {
		EntityConditionList<EntityExpr> expectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("loanApplicationId",
								EntityOperator.EQUALS,
								loanApplicationId)

				), EntityOperator.AND);

		List<GenericValue> loanRepaymentELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> orderList = new ArrayList<String>();
		orderList.add("-createdStamp");
		try {
			loanRepaymentELI = delegator.findList("LoanRepayment",
					expectationConditions, null, orderList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((loanRepaymentELI == null) || (loanRepaymentELI.size() < 1))
			return null;
		
		GenericValue loanRepayment = loanRepaymentELI.get(0);

		return loanRepayment.getString("acctgTransId");
	}

}
