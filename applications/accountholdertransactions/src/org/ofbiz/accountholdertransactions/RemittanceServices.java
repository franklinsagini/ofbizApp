package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

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
//import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

/**
 * @author Japheth Odonya @when Sep 18, 2014 12:39:40 PM
 * 
 *         Remittance Operations
 * 
 *         RemittanceServices.generateExpectedPaymentStations
 * **/
public class RemittanceServices {

	public static Logger log = Logger.getLogger(RemittanceServices.class);
	public static String MEMBER_DEPOSIT_CODE = "901";
	public static String SHARE_CAPITAL_CODE = "902";
	public static String FOSA_SAVINGS_CODE = "999";
	
	public static Long LOANFORMULAYEAR = 2015L;
	public static Long LOANFORMULAMONTH = 4L;
	public static Long countActions;

	public static HttpSession session;

	static {
		countActions = 0L;
	}

	public static String generateExpectedPaymentStations(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		List<GenericValue> memberStationELI = null;
		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");

		// Long memberStatusId = getMemberStatusId("ACTIVE");
		// EntityConditionList<EntityExpr> memberConditions = EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition
		// .makeCondition("memberStatusId", EntityOperator.EQUALS,
		// memberStatusId)
		//
		// ), EntityOperator.AND);
		//
		// try {
		// memberELI = delegator.findList("Member", memberConditions, null,
		// null, null, false);
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }
		// MemberStationList
		try {
			memberStationELI = delegator.findList("MemberStationList", null,
					null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		Set<String> setMemberStations = new HashSet<String>();
		Set<String> setEmployerCode = new HashSet<String>();
		Long stationId = null;
		for (GenericValue memberStationItem : memberStationELI) {
			// Add station Id to set
			stationId = memberStationItem.getLong("stationId");
			if (stationId != null) {
				setMemberStations.add(stationId.toString());
				System.out.println(" SSSSSSSSSSSSSSSS " + stationId
						+ " IIIIIII " + stationId);

				String theEmpCode = LoanUtilities
						.getStationEmployerCode(stationId.toString());
				if (theEmpCode != null)
					setEmployerCode.add(theEmpCode.trim());
			}
		}

		log.info("SSSSSSSSSSSS The Stations Size is ######### "
				+ setMemberStations.size());
		String createdBy = (String) request.getAttribute("userLoginId");
		String month = getCurrentMonth();
		// With the set IDs create ExpectatedStation
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (String employerCode : setEmployerCode) {
			String theStationId = LoanUtilities.getStationId(employerCode);
			// getStationName(employerCode);
			createExpectedStation(theStationId, employerCode.trim(), month,
					createdBy);
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// System.exit(0);

		// Add shares - Member Deposits
		String shareCode = getMemberDepositsCode();
		// getShareCode();
		// addExpectedShares(shareCode);

		// Add Accounts Contributions
		addAccountContributions();

		// Add Loans Expected - Principal, Interest, Insurance

		addLoanExpected();

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
	 * Add Expectation
	 * 
	 * @author Japheth Odonya @when Sep 22, 2014 5:33:04 PM
	 * 
	 * */
	private static void addLoanExpected() {
		// Get all the expected loan repayments that are unpaid

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanExpectationELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPaid", EntityOperator.EQUALS, "N")

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue loanExpectation : loanExpectationELI) {
			addExpectedLoanRepayment(loanExpectation);
		}
	}

	/**
	 * @author Japheth Odonya @when Sep 22, 2014 5:44:32 PM
	 * 
	 *         Add Expected Loan Repayment - can either be Principal Repayment,
	 *         Interest or Insurance
	 * 
	 * **/
	private static void addExpectedLoanRepayment(GenericValue loanExpectation) {
		GenericValue member = findMember(loanExpectation.getString("partyId"));

		Long activeMemberStatusId = getMemberStatusId("ACTIVE");
		if (member.getLong("memberStatusId").equals(activeMemberStatusId)) {

			GenericValue loanApplication = findLoanApplication(loanExpectation
					.getString("loanApplicationId"));
			GenericValue loanProduct = findLoanProduct(loanApplication
					.getString("loanProductId"));
			GenericValue station = findStation(member.getLong("stationId")
					.toString());

			String month = getCurrentMonth();

			String employerName = "";

			String stationNumber = "";
			String stationName = "";
			String employerCode = "";

			if (station != null) {
				employerName = station.getString("name");// getEmployer(station.getString("employerId"));
				stationNumber = station.getString("stationNumber").trim();
				;
				stationName = station.getString("name");
				employerCode = station.getString("employerCode").trim();
			}
			// String employerName = station.getString("name");
			// getEmployer(station.getString("employerId"));

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			// Create an expectation
			GenericValue expectedPaymentSent = null;

			String employeeNames = getNames(member);

			String remitanceCode = "";
			String expectationType = "";
			String remitanceDescription = loanProduct.getString("name");

			String remitanceCodeBal = "";
			String expectationTypeBal = "";
			String remitanceDescriptionBal = "";

			if (loanExpectation.getString("repaymentName").equals("PRINCIPAL")) {
				remitanceCode = loanProduct.getString("code") + "A";
				remitanceDescription = remitanceDescription + " PRINCIPAL";
				expectationType = "PRINCIPAL";

				// Add Balance (Loan Balance)
				remitanceCodeBal = loanProduct.getString("code") + "D";
				expectationTypeBal = "BALANCE";
				remitanceDescriptionBal = loanProduct.getString("name")
						+ " BALANCE";

				addExpectationBalance(remitanceCodeBal, expectationTypeBal,
						remitanceDescriptionBal, member, loanApplication,
						loanExpectation, station, month, stationNumber,
						stationName, employerCode, employerName, employeeNames);

			} else if (loanExpectation.getString("repaymentName").equals(
					"INTEREST")) {
				remitanceCode = loanProduct.getString("code") + "B";
				remitanceDescription = remitanceDescription + " INTEREST";
				expectationType = "INTEREST";
			} else if (loanExpectation.getString("repaymentName").equals(
					"INSURANCE")) {
				remitanceCode = loanProduct.getString("code") + "C";
				remitanceDescription = remitanceDescription + " INSURANCE";
				expectationType = "INSURANCE";
			}

			// = accountProduct.getString("code")+String.valueOf(sequence);

			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
			}

			expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
					UtilMisc.toMap("isActive", "Y", "branchId",
							member.getString("branchId"), "remitanceCode",
							remitanceCode, "stationNumber", stationNumber,
							"stationName", stationName,

							"payrollNo", member.getString("payrollNumber"),
							"employerCode", employerCode,

							"employeeNumber",
							member.getString("employeeNumber"), "memberNumber",
							member.getString("memberNumber"),

							"loanNo", loanApplication.getString("loanNo"),
							"employerNo", employerName, "amount",
							loanExpectation.getBigDecimal("amountAccrued"),
							"remitanceDescription", remitanceDescription,
							"employeeName", employeeNames, "expectationType",
							expectationType, "month", month));
			try {
				delegator.createOrStore(expectedPaymentSent);
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

	private static void addExpectationBalance(String remitanceCodeBal,
			String expectationTypeBal, String remitanceDescriptionBal,
			GenericValue member, GenericValue loanApplication,
			GenericValue loanExpectation, GenericValue station, String month,
			String stationNumber, String stationName, String employerCode,
			String employerName, String employeeNames) {

		BigDecimal bdLoanBalance = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplication
						.getLong("loanApplicationId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue expectedPaymentSent = null;
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						remitanceCodeBal, "stationNumber", stationNumber,
						"stationName", stationName,

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", employerCode,

						"employeeNumber", member.getString("employeeNumber"),
						"memberNumber", member.getString("memberNumber"),

						"loanNo", loanApplication.getString("loanNo"),
						"employerNo", employerName, "amount", bdLoanBalance,
						"remitanceDescription", remitanceDescriptionBal,
						"employeeName", employeeNames, "expectationType",
						expectationTypeBal, "month", month));

		try {
			delegator.createOrStore(expectedPaymentSent);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***
	 * @author Japheth Odonya @when Sep 22, 2014 3:35:42 PM Add Account
	 *         Contributions
	 * **/
	private static void addAccountContributions() {
		List<GenericValue> memeberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memeberELI = delegator.findList("Member", EntityCondition
					.makeCondition("memberStatusId",
							getMemberStatusId("ACTIVE")), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue member : memeberELI) {

			addMemberExpectedAccountContributions(member, null);
		}
	}

	/****
	 * Add Account Contributions for this member
	 * 
	 * @author Japheth Odonya @when Sep 22, 2014 3:40:45 PM
	 * */
	public static void addMemberExpectedAccountContributions(
			GenericValue member, Long pushMonthYearStationId) {
		// Get from MemberAccount - accounts that are contributing and belong to
		// this member
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();

		List<String> orderByList = new LinkedList<String>();
		orderByList.add("accountProductId");
		// String accountProductId =
		// getShareDepositAccountId(MEMBER_DEPOSIT_CODE);
		// accountProductId = accountProductId.replaceAll(",", "");
		// Long accountProductIdLong = Long.valueOf(accountProductId);
		// And accountProductId not equal to memberDeposit, not equal to share
		// capital and not equal to
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"contributing", EntityOperator.EQUALS, "YES"),
				//
				// EntityCondition.makeCondition(
				// "accountProductId", EntityOperator.NOT_EQUAL,
				// accountProductIdLong),

						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS,
								member.getLong("partyId"))

				), EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long previousAccountProductId = null;
		Long currentAccountProduct = null;
		int sequence = 1;
		for (GenericValue memberAccount : memberAccountELI) {
			// Add an expectation based on this member
			currentAccountProduct = memberAccount.getLong("accountProductId");
			if (currentAccountProduct.equals(previousAccountProductId)) {
				sequence = sequence + 1;
			} else {
				sequence = 1;
			}
			addExpectedAccountContribution(memberAccount, member, sequence, pushMonthYearStationId);

			previousAccountProductId = currentAccountProduct;
		}
	}

	/****
	 * Create Expectation
	 * **/
	private static void addExpectedAccountContribution(
			GenericValue memberAccount, GenericValue member, int sequence, Long pushMonthYearStationId) {
		GenericValue station = findStation(member.getString("stationId"));
		String month = getPushMonthYearMonth(pushMonthYearStationId);

		String employerName = "";

		String stationNumber = "";
		String stationName = "";
		String employerCode = "";

		if (station != null) {
			employerName = station.getString("name");// getEmployer(station.getString("employerId"));
			stationNumber = station.getString("stationNumber").trim();
			;
			stationName = station.getString("name");
			employerCode = station.getString("employerCode").trim();
		}
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Create an expectation
		GenericValue expectedPaymentSent = null;

		String employeeNames = getNames(member);

		GenericValue accountProduct = findAccountProduct(memberAccount.getLong(
				"accountProductId").toString());

		String remitanceCode = accountProduct.getString("code");
		// + String.valueOf(sequence);

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}

		// Get Contributing Amount
		BigDecimal bdContributingAmt = BigDecimal.ZERO;

		if (accountProduct.getString("code").equals(MEMBER_DEPOSIT_CODE)) {
			// Calculate Contribution based on graduated scale this is for
			// Member Deposits
			
			//If member has taken a loan after april 1st then use this new method
			//GenericValue pushMonthYearStation = LoanUtilities.getEntityValue("PushMonthYearStation", "pushMonthYearStationId", pushMonthYearStationId);
			//disbursementDate
			Calendar cal = Calendar.getInstance();
			
			Long year = LOANFORMULAYEAR;//Long.valueOf(monthYear.substring((monthYear.length() - 4), monthYear.length()));
			Long themonth = LOANFORMULAMONTH;//Long.valueOf(monthYear.substring(0, monthYear.length() - 4));
			System.out.println(" The year "+year);
			System.out.println(" The Month "+themonth);
		    
		    cal.set(Calendar.MONTH, themonth.intValue() - 1);
		    cal.set(Calendar.DATE, 1);
		    
		    cal.set(Calendar.YEAR, year.intValue());
		    
		    
		    cal.set(Calendar.HOUR, 0);
		    cal.set(Calendar.MINUTE, 0);
		    cal.set(Calendar.SECOND, 0);
		    cal.set(Calendar.MILLISECOND, 0);
		    
		    Timestamp loanFormularChangeDate = new Timestamp(cal.getTimeInMillis());
		    
		    List<Long> loanApplicationIdsAfterChange = LoansProcessingServices.getDisbursedLoanApplicationListAfterFormularChange(member.getLong("partyId"), loanFormularChangeDate);
 
		    if ((loanApplicationIdsAfterChange != null) && (loanApplicationIdsAfterChange.size() > 0)){
				bdContributingAmt = LoansProcessingServices
						.getLoanCurrentContributionAmount(member.getLong("partyId"));
	
				BigDecimal bdSpecifiedAmount = memberAccount
						.getBigDecimal("contributingAmount");
	
				if ((bdSpecifiedAmount != null)
						&& (bdSpecifiedAmount.compareTo(bdContributingAmt) == 1)) {
					bdContributingAmt = bdSpecifiedAmount;
				}
		    } 
			//Else use old graduated scale
		    else{
		    	BigDecimal bdAmount = LoansProcessingServices.getTotalDisbursedLoans(member.getLong("partyId"));
		    	bdContributingAmt = LoansProcessingServices.getGruaduatedScaleContributionOld(bdAmount);
						
	
				BigDecimal bdSpecifiedAmount = memberAccount
						.getBigDecimal("contributingAmount");
	
				if ((bdSpecifiedAmount != null)
						&& (bdSpecifiedAmount.compareTo(bdContributingAmt) == 1)) {
					bdContributingAmt = bdSpecifiedAmount;
				}
		    }

		} else {
			if (memberAccount.getBigDecimal("contributingAmount") != null) {
				bdContributingAmt = memberAccount
						.getBigDecimal("contributingAmount");
			} 
			
//			else {
//				bdContributingAmt = accountProduct
//						.getBigDecimal("minSavingsAmt");
//			}
		}
		
		if (bdContributingAmt.compareTo(BigDecimal.ZERO) == 1){
		Long expectedPaymentSentId = delegator.getNextSeqIdLong("ExpectedPaymentSent");
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("expectedPaymentSentId", expectedPaymentSentId, "isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						remitanceCode, "stationNumber", stationNumber,
						"stationName", stationName,

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", employerCode, "employeeNumber",
						member.getString("employeeNumber"), "memberNumber",
						member.getString("memberNumber"),

						"loanNo", "0", "employerNo", employerName, "amount",
						bdContributingAmt, "remitanceDescription",
						accountProduct.getString("name"), "employeeName",
						employeeNames, "expectationType",
						accountProduct.getString("code"), "month", month, "pushMonthYearStationId", pushMonthYearStationId));
		try {
			delegator.createOrStore(expectedPaymentSent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		}

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

	}

	/***
	 * Get shares code
	 * */
	private static String getShareCode() {
		GenericValue codesSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			codesSetup = delegator.findOne("CodesSetup",
					UtilMisc.toMap("name", "SHARES"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get CodesSetup  ");
		}
		return codesSetup.getString("code");
	}

	private static String getMemberDepositsCode() {
		return MEMBER_DEPOSIT_CODE;
	}

	private static void addExpectedShares(String shareCode) {
		// Long memberStatusId =
		// for each member save a share expected
		List<GenericValue> memeberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memeberELI = delegator.findList("Member", EntityCondition
					.makeCondition("memberStatusId",
							getMemberStatusId("ACTIVE")), null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue member : memeberELI) {

			addMemberExpectedShares(shareCode, member);
		}

	}

	private static void addMemberExpectedShares(String shareCode,
			GenericValue member) {
		GenericValue station = findStation(member.getString("stationId"));
		String month = getCurrentMonth();
		String employerName = station.getString("name");
		// getEmployer(station.getString("employerId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Create an expectation
		GenericValue expectedPaymentSent = null;
		BigDecimal bdShareAmount = BigDecimal.ZERO;

		bdShareAmount = getMemberShareContribution(member.getLong("partyId"));

		if (bdShareAmount == null) {
			bdShareAmount = getMinimumShareContribution(MEMBER_DEPOSIT_CODE);
		}

		String employeeNames = getNames(member);
		if (memberHasLoan(member.getLong("partyId"))) {
			BigDecimal bdGraduatedAmount = getGraduatedScaleShareContribution(member
					.getLong("partyId"));
			if (bdGraduatedAmount.compareTo(bdShareAmount) == 1)
				bdShareAmount = bdGraduatedAmount;
			// bdShareAmount =
			// getGraduatedScaleShareContribution(member.getLong("partyId"));
		}

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("isActive", "Y", "branchId", member
						.getString("branchId"), "remitanceCode", shareCode,
						"stationNumber", station.getString("stationNumber")
								.trim(), "stationName", station
								.getString("name"),

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", station.getString("employerCode")
								.trim(), "employeeNumber", member
								.getString("employeeNumber"),

						"memberNumber", member.getString("memberNumber"),

						"loanNo", "0", "employerNo", employerName, "amount",
						bdShareAmount, "remitanceDescription",
						"Member deposits", "employeeName", employeeNames,
						"expectationType", "MEMBERDEPOSITS",

						"month", month));
		try {
			delegator.createOrStore(expectedPaymentSent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

	}

	private static BigDecimal getMemberShareContribution(Long memberId) {
		String accountProductId = getShareDepositAccountId(MEMBER_DEPOSIT_CODE);
		BigDecimal bdShareAmount = null;

		accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS,
								Long.valueOf(accountProductId))

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		for (GenericValue genericValue : loanApplicationELI) {
			bdShareAmount = genericValue.getBigDecimal("contributingAmount");
		}

		return bdShareAmount;
	}

	private static BigDecimal getMinimumShareContribution(String code) {
		List<GenericValue> accountProductELI = null; // =
		BigDecimal minSavingsAmt = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : accountProductELI) {
			minSavingsAmt = genericValue.getBigDecimal("minSavingsAmt");
		}
		return minSavingsAmt;
	}

	private static boolean memberHasLoan(Long memberId) {

		log.info(" MMMMMMMMMMM " + memberId + " Has a loan");

		Long disbursedLoanStatusId = LoansProcessingServices
				.getLoanStatus("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, memberId),
						EntityCondition.makeCondition("loanStatusId",
								EntityOperator.EQUALS, disbursedLoanStatusId)

				), EntityOperator.AND);

		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (loanApplicationELI.size() > 0) {
			return true;
		}
		return false;
	}

	private static BigDecimal getGraduatedScaleShareContribution(Long memberId) {
		// TODO Auto-generated method stub
		// LoanServices.
		return LoansProcessingServices.getLoanCurrentContributionAmount(
				memberId, null);
	}

	private static String getEmployert(String employerId) {
		GenericValue employer = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			employer = delegator.findOne("Employer",
					UtilMisc.toMap("employerId", employerId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Employer  ");
		}
		return employer.getString("name");
	}

	private static String getCurrentMonth() {
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();

		String monthPadded = String.valueOf(month);// paddString(2,
													// String.valueOf(month));
		String monthYear = monthPadded + String.valueOf(year);

		return monthYear;
	}

	public static String paddString(int padDigits, String count) {
		String padded = String.format("%" + padDigits + "s", count).replace(
				' ', '0');
		return padded;
	}

	/***
	 * @author Japheth Odonya @when Sep 18, 2014 1:02:52 PM
	 * 
	 *         Create StationExpectation
	 * 
	 * */
	private static void createExpectedStation(String theStationId,
			String employerCode, String month, String createdBy) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue station = findStation(theStationId);
		/***
		 * <field name="isActive" type="indicator"></field> <field
		 * name="createdBy" type="name"></field> <field name="updatedBy"
		 * type="name"></field> <field name="branchId" type="name"></field>
		 * <field name="stationNumber" type="name"></field> <field
		 * name="stationName" type="name"></field>
		 * 
		 * <field name="month" type="name"></field>
		 * 
		 * **/
		String branchId = station.getString("branchId");
		String stationNumber = station.getString("stationNumber").trim();
		String stationName = station.getString("name");

		// String employerCode = station.getString("employerCode");
		String employerName = station.getString("employerName").trim();

		GenericValue stationExpectation = null;
		stationExpectation = delegator.makeValue("StationExpectation", UtilMisc
				.toMap("isActive", "Y", "createdBy", createdBy, "branchId",
						branchId,

						"employerCode", employerCode.trim(), "employerName",
						employerName.trim(),

						"stationNumber", stationNumber, "Onlinecode",
						station.getString("Onlinecode"),

						"stationName", stationName, "month", month));
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		try {
			delegator.createOrStore(stationExpectation);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
	}

	private static GenericValue findStation(String tempStationId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue station = null;
		try {
			station = delegator.findOne("Station",
					UtilMisc.toMap("stationId", tempStationId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return station;
	}

	private static String getNames(GenericValue member) {
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

		return employeeNames;
	}

	public static GenericValue findAccountProduct(String accountProductId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue accountProduct = null;
		accountProductId = accountProductId.replaceFirst(",", "");
		try {
			accountProduct = delegator.findOne(
					"AccountProduct",
					UtilMisc.toMap("accountProductId",
							Long.valueOf(accountProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct;
	}

	private static GenericValue findMember(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue member = null;
		partyId = partyId.replaceAll(",", "");
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return member;
	}

	private static GenericValue findLoanProduct(String loanProductId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanProduct = null;
		loanProductId = loanProductId.replaceAll(",", "");
		try {
			loanProduct = delegator.findOne("LoanProduct", UtilMisc.toMap(
					"loanProductId", Long.valueOf(loanProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct;
	}

	private static GenericValue findLoanApplication(String loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanApplication;
	}

	public static String getStationName(String stationNumber) {
		String stationName = "";
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator.findList("Station", EntityCondition
					.makeCondition("stationNumber", stationNumber), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue station = null;
		for (GenericValue genericValue : stationELI) {
			station = genericValue;
		}
		stationName = station.getString("name");
		return stationName;
	}

	public static String getEmployerName(String employerCode) {
		String employerName = "";
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator.findList(
					"Station",
					EntityCondition.makeCondition("employerCode",
							employerCode.trim()), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue station = null;
		for (GenericValue genericValue : stationELI) {
			station = genericValue;
		}
		employerName = station.getString("employerName");
		return employerName;
	}

	/***
	 * Get total expected for station and month
	 * */
	public static BigDecimal getTotalExpected(String employerCode, String month) {
		BigDecimal totalExpected = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("expectationType",
						EntityOperator.NOT_EQUAL, "BALANCE")

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedPaymentReceivedConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				totalExpected = totalExpected.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return totalExpected;
	}
	
	
	
	public static BigDecimal getTotalExpected(String employerCode, String month, Long pushMonthYearStationId) {
		BigDecimal totalExpected = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		
//		//EntityCondition.makeCondition(
//		"employerCode", EntityOperator.EQUALS,
//		employerCode.trim()), EntityCondition.makeCondition(
//		"month", EntityOperator.EQUALS, month),
		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						//pushMonthYearStationId
						EntityCondition.makeCondition(
								"pushMonthYearStationId", EntityOperator.EQUALS, pushMonthYearStationId),	

				EntityCondition.makeCondition("expectationType",
						EntityOperator.NOT_EQUAL, "BALANCE")

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedPaymentReceivedConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				totalExpected = totalExpected.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return totalExpected;
	}

	/***
	 * Get total remitted by station
	 * */
	public static BigDecimal getTotalRemitted(String employerCode, String month) {
		BigDecimal totalRemitted = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				totalRemitted = totalRemitted.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		// totalRemitted = totalRemitted.setScale(newScale)

		return totalRemitted;
	}
	
	//pushMonthYearStationId
	public static BigDecimal getTotalRemitted(String employerCode, String month, Long pushMonthYearStationId) {
		BigDecimal totalRemitted = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList( EntityCondition.makeCondition(
						"pushMonthYearStationId", EntityOperator.EQUALS, pushMonthYearStationId)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				totalRemitted = totalRemitted.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		// totalRemitted = totalRemitted.setScale(newScale)

		return totalRemitted;
	}

	public static String isRemitanceEnough(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String employerCode = (String) request.getParameter("employerCode")
				.trim();
		String month = (String) request.getParameter("month").trim();

		// GenericValue station = findStationGivenStationNumber(stationNumber);

		// Get
		List<GenericValue> stationAccountTransactionELI = null;

		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"monthyear", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			stationAccountTransactionELI = delegator.findList(
					"StationAccountTransaction",
					stationAccountTransactionConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// TransactionAmount
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (GenericValue stationAccountTransaction : stationAccountTransactionELI) {
			if (stationAccountTransaction.getBigDecimal("transactionAmount") != null) {
				totalAmount = totalAmount.add(stationAccountTransaction
						.getBigDecimal("transactionAmount"));
			}
		}

		// Get total submitted
		BigDecimal totalSubmitted = getTotalExpected(employerCode.trim(), month);

		if (totalSubmitted.compareTo(totalAmount) == -1) {
			result.put("REMITANCEENOUGH", "NO");
		} else {
			result.put("REMITANCEENOUGH", "YES");
		}

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

	private static GenericValue findStationGivenStationNumber(
			String stationNumber) {
		List<GenericValue> stationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			stationELI = delegator.findList("Station", EntityCondition
					.makeCondition("stationNumber", stationNumber), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue station = null;
		for (GenericValue genericValue : stationELI) {
			station = genericValue;
		}
		return station;
	}

	public static BigDecimal getTotalRemittedChequeAmount(String employerCode,
			String month) {

		// GenericValue station = findStationGivenStationNumber(stationNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get
		List<GenericValue> stationAccountTransactionELI = null;

		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"monthyear", EntityOperator.EQUALS, month)

				), EntityOperator.AND);

		try {
			stationAccountTransactionELI = delegator.findList(
					"StationAccountTransaction",
					stationAccountTransactionConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// TransactionAmount
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (GenericValue stationAccountTransaction : stationAccountTransactionELI) {
			if (stationAccountTransaction.getBigDecimal("transactionAmount") != null) {
				totalAmount = totalAmount.add(stationAccountTransaction
						.getBigDecimal("transactionAmount"));
			}
		}

		return totalAmount;
	}
	
	//getTotalRemittedChequeAmount

	/***
	 * getTotalRemittedChequeAmountAvailable
	 * 
	 * */
	public static BigDecimal getTotalRemittedChequeAmountAvailable(
			String employerCode, String month, String year) {
		
		log.info("CCCCCCCCCCCCCC Correct employerCode "+employerCode);
		log.info("CCCCCCCCCCCCCC Correct month "+month);
		log.info("CCCCCCCCCCCCCC Correct year "+year);

		// GenericValue station = findStationGivenStationNumber(stationNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get
		List<GenericValue> stationAccountTransactionELI = null;
		String monthYear = month + year;
		monthYear = monthYear.toString();
		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"monthyear", EntityOperator.EQUALS, monthYear)

				), EntityOperator.AND);

		try {
			stationAccountTransactionELI = delegator.findList(
					"StationAccountTransaction",
					stationAccountTransactionConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// TransactionAmount
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (GenericValue stationAccountTransaction : stationAccountTransactionELI) {
			if (stationAccountTransaction.getBigDecimal("transactionAmount") != null) {
				totalAmount = totalAmount.add(stationAccountTransaction
						.getBigDecimal("transactionAmount"));
			}
		}

		BigDecimal bdTotalRemittanceProcessedAmt = BigDecimal.ZERO;

		bdTotalRemittanceProcessedAmt = getTotalRemittanceProcessed(
				employerCode, monthYear);
		log.info("TTTTTTTTTT totalAmount " + totalAmount);
		log.info("TTTTTTTTTT bdTotalRemittanceProcessedAmt "
				+ bdTotalRemittanceProcessedAmt);
		BigDecimal bdTotalSalaryProcessedAmt = BigDecimal.ZERO;

		log.info("EEEEEEEE employerCode == " + employerCode);
		log.info("EEEEEEEE month == " + month);
		bdTotalSalaryProcessedAmt = getTotalSalaryProcessed(employerCode, month);
		log.info("TTTTTTTTTT bdTotalSalaryProcessedAmt "
				+ bdTotalSalaryProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalRemittanceProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalSalaryProcessedAmt);
		log.info("TTTTTTTTTT totalAmount after " + totalAmount);
		return totalAmount;
	}
	
	
	//CHeque available where month is actually Month + Year
	public static BigDecimal getTotalRemittedChequeAmountAvailable(
			String employerCode, String month) {
		
		log.info("CCCCCCCCCCCCCC Correct employerCode "+employerCode);
		log.info("CCCCCCCCCCCCCC Correct month "+month);

		// GenericValue station = findStationGivenStationNumber(stationNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get
		List<GenericValue> stationAccountTransactionELI = null;
		String monthYear = month;
		monthYear = monthYear.toString();
		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"monthyear", EntityOperator.EQUALS, monthYear)

				), EntityOperator.AND);

		try {
			stationAccountTransactionELI = delegator.findList(
					"StationAccountTransaction",
					stationAccountTransactionConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// TransactionAmount
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (GenericValue stationAccountTransaction : stationAccountTransactionELI) {
			if (stationAccountTransaction.getBigDecimal("transactionAmount") != null) {
				totalAmount = totalAmount.add(stationAccountTransaction
						.getBigDecimal("transactionAmount"));
			}
		}

		BigDecimal bdTotalRemittanceProcessedAmt = BigDecimal.ZERO;

		bdTotalRemittanceProcessedAmt = getTotalRemittanceProcessed(
				employerCode, monthYear);
		log.info("TTTTTTTTTT totalAmount " + totalAmount);
		log.info("TTTTTTTTTT bdTotalRemittanceProcessedAmt "
				+ bdTotalRemittanceProcessedAmt);
		BigDecimal bdTotalSalaryProcessedAmt = BigDecimal.ZERO;

		log.info("EEEEEEEE employerCode == " + employerCode);
		log.info("EEEEEEEE month == " + month);
		
		String realMonthRemovedYear = month;
				
		if (month.length() > 4){		
			realMonthRemovedYear = month.substring(0, (month.length()-4));
		}
		bdTotalSalaryProcessedAmt = getTotalSalaryProcessed(employerCode, realMonthRemovedYear);
		log.info("TTTTTTTTTT bdTotalSalaryProcessedAmt "
				+ bdTotalSalaryProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalRemittanceProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalSalaryProcessedAmt);
		log.info("TTTTTTTTTT totalAmount after " + totalAmount);
		return totalAmount;
	}
	
	
	//Show Cheque Available
	public static BigDecimal getTotalRemittedChequeAmountAvailable(
			String employerCode, String month, String year, Long pushMonthYearStationId) {
		
		GenericValue pushMonthYearStation = LoanUtilities.getEntityValue("PushMonthYearStation", "pushMonthYearStationId", pushMonthYearStationId);
		//pushMonthYearStationId
		
		month = pushMonthYearStation.getLong("month").toString();
		year = pushMonthYearStation.getLong("year").toString();
		log.info("CCCCCCCCCCCCCC Correct employerCode "+employerCode);
		log.info("CCCCCCCCCCCCCC Correct month "+month);
		log.info("CCCCCCCCCCCCCC Correct year "+year);
		
		GenericValue station = LoanUtilities.getEntityValue("Station", "stationId", pushMonthYearStation.getString("stationId"));
		
		employerCode = station.getString("employerCode");

		// GenericValue station = findStationGivenStationNumber(stationNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get
		List<GenericValue> stationAccountTransactionELI = null;
		String monthYear = month + year;
		monthYear = monthYear.toString();
		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"monthyear", EntityOperator.EQUALS, monthYear)

				), EntityOperator.AND);

		try {
			stationAccountTransactionELI = delegator.findList(
					"StationAccountTransaction",
					stationAccountTransactionConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// TransactionAmount
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (GenericValue stationAccountTransaction : stationAccountTransactionELI) {
			if (stationAccountTransaction.getBigDecimal("transactionAmount") != null) {
				totalAmount = totalAmount.add(stationAccountTransaction
						.getBigDecimal("transactionAmount"));
			}
		}

		BigDecimal bdTotalRemittanceProcessedAmt = BigDecimal.ZERO;

		bdTotalRemittanceProcessedAmt = getTotalRemittanceProcessed(
				employerCode, monthYear);
		log.info("TTTTTTTTTT totalAmount " + totalAmount);
		log.info("TTTTTTTTTT bdTotalRemittanceProcessedAmt "
				+ bdTotalRemittanceProcessedAmt);
		BigDecimal bdTotalSalaryProcessedAmt = BigDecimal.ZERO;

		log.info("EEEEEEEE employerCode == " + employerCode);
		log.info("EEEEEEEE month == " + month);
		bdTotalSalaryProcessedAmt = getTotalSalaryProcessed(employerCode, month);
		log.info("TTTTTTTTTT bdTotalSalaryProcessedAmt "
				+ bdTotalSalaryProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalRemittanceProcessedAmt);
		totalAmount = totalAmount.subtract(bdTotalSalaryProcessedAmt);
		log.info("TTTTTTTTTT totalAmount after " + totalAmount);
		return totalAmount;
	}

	private static BigDecimal getTotalSalaryProcessed(String employerCode,
			String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month), EntityCondition
						.makeCondition("processed", EntityOperator.EQUALS, "Y")

				), EntityOperator.AND);

		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();
		try {
			expectedPaymentReceivedELI = delegator.findList("MemberSalary",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdTotal = BigDecimal.ZERO;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			bdTotal = bdTotal.add(genericValue.getBigDecimal("netSalary"));
		}

		return bdTotal;
	}

	private static BigDecimal getTotalRemittanceProcessed(String employerCode,
			String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month), EntityCondition
						.makeCondition("processed", EntityOperator.EQUALS, "Y")

				), EntityOperator.AND);

		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdTotal = BigDecimal.ZERO;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			bdTotal = bdTotal.add(genericValue.getBigDecimal("amount"));
		}

		return bdTotal;
	}

	/****
	 * @author Japheth Odonya @when May 30, 2015 11:43:49 PM
	 * 
	 *         Check that the station has already been processed
	 * 
	 * */
	public static synchronized String checkStationAlreadyProcessed(
			Map<String, String> userLogin, String employerCode, String month) {
		employerCode = employerCode.trim();
		month = month.trim();

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, null)

				), EntityOperator.AND);

		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((expectedPaymentReceivedELI == null)
				|| (expectedPaymentReceivedELI.size() < 1)) {
			log.info("NNNNNNNN  processed already");
			return "processed";
		}

		log.info("NNNNNNNN Not processed yet");
		return "notprocessed";
	}

	/***
	 * @author Japheth Odonya @when May 30, 2015 11:25:45 PM Check that all the
	 *         Payroll Numbers in the Received actually exist in the system,
	 *         otherwise complain and dont continue
	 * */
	public static synchronized String checkMembersPayrollNumbersExist(
			Map<String, String> userLogin, String employerCode, String month) {

		employerCode = employerCode.trim();
		// (String) request.getParameter("employerCode")
		// .trim();
		month = month.trim();

		log.info("SSSSSSSSSSSSSSS  Employer Code " + employerCode);
		log.info("SSSSSSSSSSSSSSS  Month " + month);

		if (!AllPayrollCodesExist(employerCode, month)) {
			return "fail";
		}

		return "success";
	}

	/***
	 * @author Japheth Odonya @when May 30, 2015 11:27:22 PM Check that all
	 *         members being processed actually have FOSA Savings Account 999
	 *         account code, if any of them does not have then do not continue
	 * 
	 * */
	public static synchronized String checkMembersHaveFosaSavingsAccount(
			Map<String, String> userLogin, String employerCode, String month) {

		log.info("FFFFFFFFFFFF Checking FOSA Savings!!!!!!!!!!!!!! ");

		// Get all payroll numbers and make sure the members have FOSA Savings
		// Account
		List<GenericValue> receivedPayrollELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedPayrollsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month)

				), EntityOperator.AND);
		try {
			receivedPayrollELI = delegator.findList(
					"ExpectedPaymentReceivedPayrolls",
					receivedPayrollsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Boolean failed = false;
		// Clear the missing log - delete everything from it
		clearMissingMember(month, employerCode);
		String payrollNo = "";
		Long count = 0L;
		List<GenericValue> listMissingMemberLogELI = new ArrayList<GenericValue>();
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNo");
			log.info(++count
					+ "FFFFFFFFFFFF Checking FOSA Savings!!!!!!!!!!!!!! for "
					+ payrollNo);
			if (!hasAccount(FOSA_SAVINGS_CODE, payrollNo.trim())) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				addMissingMemberLog(userLogin, payrollNo, month, employerCode,
						FOSA_SAVINGS_CODE, null, null);
			}

		}

		if (failed) {
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				delegator.storeAll(listMissingMemberLogELI);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "failed";
		}

		log.info(" RRRRRRRR Received Payrolls Count is --- "
				+ receivedPayrollELI.size());

		return "success";
	}

	/***
	 * @author Japheth Odonya @when Jun 1, 2015 1:31:56 PM
	 * 
	 *         Clear the Missing Member Log of the records for the Month and
	 *         Employer Code
	 * */
	public static void clearMissingMember(String month, String employerCode) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> receivedPayrollsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month)

				), EntityOperator.AND);

		try {
			delegator.removeByCondition("MissingMemberLog",
					receivedPayrollsConditions);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/****
	 * @author Japheth Odonya @when Jun 1, 2015 1:29:50 PM Adding a member to
	 *         the missing member log, it could include the members missing FOSA
	 *         Savings Account, Payroll Numbers, Member Deposits or Share
	 *         Capital
	 * */
	public static void addMissingMemberLog(Map<String, String> userLogin,
			String payrollNo, String month, String employerCode,
			String productCode, String loanNo, String employeeNumber) {
		GenericValue missingMemberLog = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long missingMemberLogId = delegator
				.getNextSeqIdLong("MissingMemberLog");

		String names = "";
		if ((payrollNo != null) && (!payrollNo.equals("")))
			names = LoanUtilities.getMemberName(payrollNo);

		GenericValue station = LoanUtilities.getStation(LoanUtilities
				.getStationId(employerCode));

		String productName = "";

		if (productCode != null) {
			GenericValue product = LoanUtilities
					.getAccountProductGivenCodeId(productCode);

			if (product != null)
				productName = product.getString("name");
		}

		if (loanNo != null) {
			log.info("WWWWWW Will get loan WWWWWWWW");

			GenericValue loanApplication = LoanUtilities
					.getLoanApplicationEntityGivenLoanNo(loanNo);
			if (loanApplication != null) {
				GenericValue loanProduct = LoanUtilities
						.getLoanProduct(loanApplication
								.getLong("loanProductId"));

				if (loanProduct != null)
					productName = loanProduct.getString("name");
			}

		}

		missingMemberLog = delegator.makeValue("MissingMemberLog", UtilMisc
				.toMap("missingMemberLogId", missingMemberLogId, "isActive",
						"Y", "createdBy", userLogin.get("userLoginId"),
						"employerCode", employerCode.trim(),

						"payrollNumber", payrollNo,

						"employerName", station.getString("employerName"),

						"names", names,

						"loanNo", loanNo,

						"code", productCode,

						"employeeNumber", employeeNumber,

						"productName", productName,

						"month", month.trim()));

		log.info(" for Reall .... FFFFFFF Just added a Missing Member Log");

		try {
			TransactionUtil.begin();
			delegator.create(missingMemberLog);
			TransactionUtil.commit();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean hasAccount(String accountCode, String payrollNo) {
		// TODO Check of a member , given payroll number has an account of the
		// product given
		Long accountProductId = LoanUtilities.getAccountProductGivenCodeId(
				accountCode).getLong("accountProductId");

		Long partyId = LoanUtilities.getMemberId(payrollNo);

		Long memberAccountId = LoanUtilities
				.getMemberAccountIdFromMemberAccount(partyId, accountProductId);

		if (memberAccountId != null)
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when May 30, 2015 11:27:33 PM Check that all
	 *         members do have a Member Deposit Account, (code 901) if any of
	 *         them does not have then do not continue
	 * */
	public static synchronized String checkMembersHaveMemberDepositAccount(
			Map<String, String> userLogin, String employerCode, String month) {
		List<GenericValue> receivedPayrollELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedPayrollsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month)

				), EntityOperator.AND);
		try {
			receivedPayrollELI = delegator.findList(
					"ExpectedPaymentReceivedPayrolls",
					receivedPayrollsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Boolean failed = false;
		// Clear the missing log - delete everything from it
		clearMissingMember(month, employerCode);
		String payrollNo = "";
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNo");
			if (!hasAccount(MEMBER_DEPOSIT_CODE, payrollNo.trim())) {
				failed = true;

				// Add the member to the missing log
				addMissingMemberLog(userLogin, payrollNo, month, employerCode,
						MEMBER_DEPOSIT_CODE, null, null);
			}

		}

		if (failed) {
			return "failed";
		}
		log.info(" RRRRRRRR Received Payrolls Count is --- "
				+ receivedPayrollELI.size());

		return "success";
	}

	/***
	 * @author Japheth Odonya @when May 30, 2015 11:28:15 PM Check that each of
	 *         the members has a Share Capital Account, if any of the members
	 *         does not have a share capital account (code 902) then do not
	 *         procede
	 * */
	public static synchronized String checkMembersHaveShareCapitalAccount(
			Map<String, String> userLogin, String employerCode, String month) {
		List<GenericValue> receivedPayrollELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedPayrollsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month)

				), EntityOperator.AND);
		try {
			receivedPayrollELI = delegator.findList(
					"ExpectedPaymentReceivedPayrolls",
					receivedPayrollsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		Boolean failed = false;
		// Clear the missing log - delete everything from it
		clearMissingMember(month, employerCode);
		String payrollNo = "";
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNo");
			if (!hasAccount(SHARE_CAPITAL_CODE, payrollNo.trim())) {
				failed = true;

				// Add the member to the missing log
				addMissingMemberLog(userLogin, payrollNo, month, employerCode,
						SHARE_CAPITAL_CODE, null, null);
			}

		}

		if (failed) {
			return "failed";
		}

		log.info(" RRRRRRRR Received Payrolls Count is --- "
				+ receivedPayrollELI.size());

		return "success";
	}

	/****
	 * @author Japheth Odonya @when Sep 23, 2014 8:40:10 AM
	 * 
	 *         Update Process Received Payments
	 * 
	 * */
	// HttpServletRequest request, HttpServletResponse response
	public static synchronized String processReceivedPaymentBreakdown(
			Map<String, String> userLogin, String employerCode, String month) {
		countActions++;

		// Update Receipts to show generated and post
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// (Delegator) request.getAttribute("delegator");
		employerCode = employerCode.trim();
		// (String) request.getParameter("employerCode")
		// .trim();
		month = month.trim();

		log.info("SSSSSSSSSSSSSSS  Employer Code " + employerCode);
		log.info("SSSSSSSSSSSSSSS  Month " + month);

		List<ProductTotal> listAccountProductRemitted = getAccountProductsRemittedList(
				employerCode, month);
		Boolean accountProductMissGLAccount = false;
		Boolean accountProductNotMapped = false;

		String branchId = LoanUtilities.getBranchId(employerCode);

		if (branchId == null) {
			log.info(" MMMMMMM Missing Branch ######");
			return "stationmusthavebranch";
		} else {
			log.info(" MMMMMMM Branch is  ######" + branchId);

		}

		String missingProductNames = "";
		String missingProductMapped = "";
		List<String> listRemittedAccountProductList = new ArrayList<String>();
		for (ProductTotal productTotal : listAccountProductRemitted) {
			log.info("PPPPPPPPPPPP  Product Code " + productTotal.getCode()
					+ "  total " + productTotal.getAmount());
			listRemittedAccountProductList.add(productTotal.getCode().trim());
			if (LoanUtilities.missingGLAccount(productTotal.getCode().trim())) {
				accountProductMissGLAccount = true;

				if (missingProductNames.equals("")) {
					missingProductNames = " Missing accounts for "
							+ productTotal.getName();
				} else {
					missingProductNames = missingProductNames + ", "
							+ productTotal.getName();
				}
			}

			if (!accountProductMissGLAccount)
				if (LoanUtilities.notAccountsNotMapped(branchId,
						productTotal.getCode())) {
					accountProductNotMapped = true;

					if (missingProductMapped.equals("")) {
						missingProductMapped = " Missing account mappings to "
								+ LoanUtilities.getBranchName(branchId)
								+ " for " + productTotal.getName();
					} else {
						missingProductMapped = missingProductMapped + ", "
								+ productTotal.getName();
					}
				}
		}

		if (accountProductMissGLAccount) {

			log.info(" MMMMMMM Missing GL Account on Account Product  ######");
			return missingProductNames;
			// "accountproductmissingglaccount";
		} else {
			log.info(" MMMMMMM  GL Account on Account Product Ok  ######");
		}

		if (accountProductNotMapped) {
			log.info(" MMMMMMM  GL Account on Account Product Not mapped  ######");

			return missingProductMapped;

			// "accountproductglaccountnotmappedtobranch";
		} else {
			log.info(" MMMMMMM  GL Account on Account Product All mapped  ######");

		}

		/****
		 * Check for PRINCIPALPAYMENT INTERESTPAYMENT INSURANCEPAYMENT
		 * 
		 * */

		// Station Account Payment
		String loanRelatedAccountId = null;
		GenericValue loanRelatedAccounts = null;
		loanRelatedAccounts = LoanRepayments
				.getAccountHolderTransactionSetupRecord(
						"STATIONACCOUNTPAYMENT", delegator);

		if (loanRelatedAccounts == null) {
			return "Please ensure that STATION Payment Account is set in the Accounts Setup and mapped properly";
		}

		loanRelatedAccountId = loanRelatedAccounts
				.getString("memberDepositAccId");

		if ((loanRelatedAccountId == null) || (loanRelatedAccountId.equals(""))) {
			return "Please ensure that STATION Payment Account is set in the Accounts Setup and mapped properly";
		}

		if (!LoanUtilities.organizationAccountMapped(loanRelatedAccountId,
				branchId)) {
			return "Please ensure that STATION Payment Account has a mapping for the branch "
					+ LoanUtilities.getBranchName(branchId)
					+ " in the General Ledger ";

		}

		// Principal
		loanRelatedAccountId = null;
		loanRelatedAccounts = null;
		loanRelatedAccounts = LoanRepayments
				.getAccountHolderTransactionSetupRecord("PRINCIPALPAYMENT",
						delegator);

		if (loanRelatedAccounts == null) {
			return "Please ensure that Principal Payment Account is set in the Accounts Setup and mapped properly";
		}

		loanRelatedAccountId = loanRelatedAccounts
				.getString("memberDepositAccId");

		if ((loanRelatedAccountId == null) || (loanRelatedAccountId.equals(""))) {
			return "Please ensure that Principal Payment Account is set in the Accounts Setup and mapped properly";
		}

		if (!LoanUtilities.organizationAccountMapped(loanRelatedAccountId,
				branchId)) {
			return "Please ensure that Principal Payment Account has a mapping for the branch "
					+ LoanUtilities.getBranchName(branchId)
					+ " in the General Ledger ";

		}

		// Interest
		loanRelatedAccountId = null;
		loanRelatedAccounts = null;
		loanRelatedAccounts = LoanRepayments
				.getAccountHolderTransactionSetupRecord("INTERESTPAYMENT",
						delegator);

		if (loanRelatedAccounts == null) {
			return "Please ensure that Interest Payment Account is set in the Accounts Setup and mapped properly";
		}

		loanRelatedAccountId = loanRelatedAccounts
				.getString("memberDepositAccId");

		if ((loanRelatedAccountId == null) || (loanRelatedAccountId.equals(""))) {
			return "Please ensure that Interest Payment Account is set in the Accounts Setup and mapped properly";
		}

		if (!LoanUtilities.organizationAccountMapped(loanRelatedAccountId,
				branchId)) {
			return "Please ensure that Interest Payment Account has a mapping for the branch "
					+ LoanUtilities.getBranchName(branchId)
					+ " in the General Ledger ";

		}

		// Insurance
		loanRelatedAccountId = null;
		loanRelatedAccounts = null;
		loanRelatedAccounts = LoanRepayments
				.getAccountHolderTransactionSetupRecord("INSURANCEPAYMENT",
						delegator);

		if (loanRelatedAccounts == null) {
			return "Please ensure that Insurance Payment Account is set in the Accounts Setup and mapped properly";
		}

		loanRelatedAccountId = loanRelatedAccounts
				.getString("memberDepositAccId");

		if ((loanRelatedAccountId == null) || (loanRelatedAccountId.equals(""))) {
			return "Please ensure that Insurance Payment Account is set in the Accounts Setup and mapped properly";
		}

		if (!LoanUtilities.organizationAccountMapped(loanRelatedAccountId,
				branchId)) {
			return "Please ensure that Insurance Payment Account has a mapping for the branch "
					+ LoanUtilities.getBranchName(branchId)
					+ " in the General Ledger ";

		}

		// if (!AllPayrollCodesExist(employerCode, month)) {
		// return "fail";
		// }
		/**
		 * <field name="processed" type="indicator"></field> <field
		 * name="dateProcessed" type="date-time"></field>
		 **/
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, null)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdSharesTotal = BigDecimal.ZERO;
		BigDecimal bdPrincipal = BigDecimal.ZERO;
		BigDecimal bdInterest = BigDecimal.ZERO;
		BigDecimal bdInsurance = BigDecimal.ZERO;
		BigDecimal bdExcessToSavings = BigDecimal.ZERO;
		
		BigDecimal bdAccount = BigDecimal.ZERO;
		BigDecimal bdTotal = BigDecimal.ZERO;

		GenericValue accountHolderTransactionSetup = null;
		// Get Account to debit - the Station Debit Account
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord(
						"STATIONACCOUNTPAYMENT", delegator);

		String acctgTransType = "STATION_DEPOSIT";
		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(
				accountHolderTransactionSetup, acctgTransType, branchId);

		List<String> accountProductCodesList = getAccountProductCodesList();
		
		Set<String> loansRemittedSet = new HashSet<String>();

		// String branchId = "";
		log.info(" SSSSSSSSSSSSSS Number of Records is "
				+ expectedPaymentReceivedELI.size());
		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {

			/***
			 * Can either be an account, an INTEREST INSURANCE PRINCIPAL
			 * */

			// if (branchId.equals("")) {
			// branchId = getMemberByPayrollNo(
			// expectedPaymentReceived.getString("payrollNo"))
			// .getString("branchId");
			// }

			/**
			 * PRINCIPAL INTEREST INSURANCE ACCOUNT SHARES
			 * 
			 * */

			// for (String code : accountProductCodesList) {
			for (String code : listRemittedAccountProductList) {

				if (expectedPaymentReceived.getString("remitanceCode").equals(
						code)) {
					// && !(code.equals(SHARE_CAPITAL_CODE))
					// Add a member account transaction from this expectation to
					// the account of this code
					BigDecimal transactionAmount = expectedPaymentReceived
							.getBigDecimal("amount");

					// Get Member Account ID given product code and payrollNO
					Long memberAccountId = getMemberAccountId(code,
							expectedPaymentReceived.getString("payrollNo"));

					System.out
							.println(" The Payroll No in Question ##### PPPPPPPP "
									+ expectedPaymentReceived
											.getString("payrollNo"));
					System.out
							.println(" The Payroll No in Question ##### PPPPPPPP "
									+ memberAccountId);

					// AccHolderTransactionServices.cashDepositt(transactionAmount,
					// memberAccountId, userLogin, withdrawalType)
					log.info("PPPPPPPP Product Code is " + code
							+ " Payroll Number is "
							+ expectedPaymentReceived.getString("payrollNo"));
					AccHolderTransactionServices
							.cashDepositFromStationProcessing(
									transactionAmount, memberAccountId,
									userLogin, month + " Remittance",
									acctgTransId);
					// Increment bdAccount with this amount
					bdAccount = bdAccount.add(expectedPaymentReceived
							.getBigDecimal("amount"));
				}

				// else if (expectedPaymentReceived.getString("remitanceCode")
				// .equals(code) && (code.equals(SHARE_CAPITAL_CODE))) {
				// // Add member account transaction from this expection to the
				// // account of this code
				// BigDecimal transactionAmount = expectedPaymentReceived
				// .getBigDecimal("amount");
				// Long memberAccountId = getMemberAccountId(code,
				// expectedPaymentReceived.getString("payrollNo"));
				// AccHolderTransactionServices.cashDeposit(transactionAmount,
				// memberAccountId, userLogin, month + " Remittance");
				//
				// // Increment Share Total
				// bdSharesTotal = bdSharesTotal.add(expectedPaymentReceived
				// .getBigDecimal("amount"));
				// }
			}

			// if (expectedPaymentReceived.getString("expectationType").equals(
			// "MEMBERDEPOSITS")) {
			// bdSharesTotal = bdSharesTotal.add(expectedPaymentReceived
			// .getBigDecimal("amount"));
			// } else if (expectedPaymentReceived.getString("expectationType")
			// .equals("ACCOUNT")) {
			// bdAccount = bdAccount.add(expectedPaymentReceived
			// .getBigDecimal("amount"));
			// } else

			// Principal = Loan Product Code + A
			// Interest = Loan Product Code + B
			// Insurance = LoanProduct Code + C
			String loanProductCode = LoanUtilities
					.getLoanProductCodeGivenLoanNo(expectedPaymentReceived
							.getString("loanNo"));

			
			if (loanProductCode != null) {
				//Add the Loan No to a set
				loansRemittedSet.add(expectedPaymentReceived.getString("loanNo"));
			}

			bdTotal = bdTotal.add(expectedPaymentReceived
					.getBigDecimal("amount"));

			// Update expectedPaymentReceived to processed
			expectedPaymentReceived.set("processed", "Y");
			expectedPaymentReceived.set("dateProcessed", new Timestamp(Calendar
					.getInstance().getTimeInMillis()));

			try {
				delegator.createOrStore(expectedPaymentReceived);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		
		for (String loanNo : loansRemittedSet) {
			
//			String remittanceCodePrincipal = loanProductCode + "A";
//			String remittanceCodeInterest = loanProductCode + "B";
//			String remittanceCodeInsurance = loanProductCode + "C";
			
			//remittanceCodePrincipal
			//remittanceCodeInterest
			//remittanceCodeInsurance
			BigDecimal bdTotalAmountRemitted = BigDecimal.ZERO;
			
			//Given Loan No , get total Remitted Amount
			bdTotalAmountRemitted = getTotalRemittedGivenLoanNo(loanNo, employerCode, month);


			PrincipalInterestInsurance principalInterestInsurance = null;
					
			//if (principalExpectedRecived != null){
				principalInterestInsurance = saveLoanRepaymentRemittance(
						null, acctgTransId, "REMITTANCE",
						bdTotalAmountRemitted, userLogin, month, loanNo);

				bdPrincipal = bdPrincipal.add(principalInterestInsurance
						.getPrincipalAmt());
				bdInterest = bdInterest.add(principalInterestInsurance
						.getInterestAmt());
				bdInsurance = bdInsurance.add(principalInterestInsurance
						.getInsuranceAmt());
				
				if (principalInterestInsurance.getExcessToSavingsAmt().compareTo(BigDecimal.ZERO) > 0){
					bdExcessToSavings = bdExcessToSavings.add(principalInterestInsurance.getExcessToSavingsAmt());
				}
			
				//principalInterestInsurance = null;
			//}
		}

		// Ommit Shares and Member Deposits from the Debit because they are
		// being posted individually - per
		// transaction
		// bdTotal = bdTotal.subtract(bdAccount);
		// bdTotal = bdTotal.subtract(bdSharesTotal);

		String postingType = "D";
		String debitAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		// Do the posting
		String entrySequenceId = "00001";
		// postTransaction(debitAccountId, postingType, entrySequenceId,
		// bdTotal);
		if (bdTotal.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(debitAccountId, postingType, entrySequenceId,
					bdTotal, branchId, acctgTransId, acctgTransType);
		}
		String creditAccountId;
		/***
		 * SHAREDEPOSITACCOUNT - credit member shares MEMBERTRANSACTIONACCOUNT -
		 * member deposit PRINCIPALPAYMENT INTERESTPAYMENT INSURANCEPAYMENT
		 * 
		 * */

		// // SHAREDEPOSITACCOUNT
		// accountHolderTransactionSetup = LoanRepayments
		// .getAccountHolderTransactionSetupRecord("SHAREDEPOSITACCOUNT",
		// delegator);
		// creditAccountId = accountHolderTransactionSetup
		// .getString("memberDepositAccId");
		// postingType = "C";
		// entrySequenceId = "00002";
		// if (bdSharesTotal.compareTo(BigDecimal.ZERO) == 1) {
		// postTransaction(creditAccountId, postingType, entrySequenceId,
		// bdSharesTotal, branchId, acctgTransId, acctgTransType);
		// }
		// // MEMBERTRANSACTIONACCOUNT - Account
		// accountHolderTransactionSetup = LoanRepayments
		// .getAccountHolderTransactionSetupRecord(
		// "MEMBERTRANSACTIONACCOUNT", delegator);
		// creditAccountId = accountHolderTransactionSetup
		// .getString("memberDepositAccId");
		// postingType = "C";
		// entrySequenceId = "00003";
		// if (bdAccount.compareTo(BigDecimal.ZERO) == 1) {
		// postTransaction(creditAccountId, postingType, entrySequenceId,
		// bdAccount, branchId, acctgTransId, acctgTransType);
		// }

		// Credit each of the account products
		int sequenceId = 1;
		String sequenceString = "";

		for (ProductTotal productEnt : listAccountProductRemitted) {
			sequenceId = sequenceId + 1;

			sequenceString = "0000" + sequenceId;
			entrySequenceId = sequenceString;
			postingType = "C";
			creditAccountId = LoanUtilities
					.getGLAccountIDForAccountProduct(productEnt.getCode()
							.trim());

			if (productEnt.getAmount().compareTo(BigDecimal.ZERO) == 1) {
				postTransaction(creditAccountId, postingType, entrySequenceId,
						productEnt.getAmount(), branchId, acctgTransId,
						acctgTransType);
			}

		}

		// PRINCIPALPAYMENT
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord("PRINCIPALPAYMENT",
						delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";

		sequenceId = sequenceId + 1;
		sequenceString = "0000" + sequenceId;

		entrySequenceId = sequenceString;
		if (bdPrincipal.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdPrincipal, branchId, acctgTransId, acctgTransType);
		}
		// INTERESTPAYMENT
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord("INTERESTPAYMENT",
						delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";

		sequenceId = sequenceId + 1;
		sequenceString = "0000" + sequenceId;

		entrySequenceId = sequenceString;

		if (bdInterest.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdInterest, branchId, acctgTransId, acctgTransType);
		}
		// INSURANCEPAYMENT
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord("INSURANCEPAYMENT",
						delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";

		sequenceId = sequenceId + 1;
		sequenceString = "0000" + sequenceId;

		entrySequenceId = sequenceString;

		if (bdInsurance.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdInsurance, branchId, acctgTransId, acctgTransType);
		}
		
		//Credit Savings with excess from Loan Remittances
		//bdExcessToSavings
		
		sequenceId = sequenceId + 1;

		sequenceString = "0000" + sequenceId;
		entrySequenceId = sequenceString;
		postingType = "C";
		creditAccountId = LoanUtilities
				.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);

		if (bdExcessToSavings.compareTo(BigDecimal.ZERO) > 0) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdExcessToSavings, branchId, acctgTransId,
					acctgTransType);
		}

		// Writer out;
		// try {
		// out = response.getWriter();
		// out.write("");
		// out.flush();
		// } catch (IOException e) {
		// try {
		// throw new EventHandlerException(
		// "Unable to get response writer", e);
		// } catch (EventHandlerException e1) {
		// e1.printStackTrace();
		// }
		// }
		// return
		// "Successufully Processed remittance for "+bdTotal+" shillings, the transaction ID is "+acctgTransId;
		return "Successufully Processed remittance";
	}

	private static BigDecimal getTotalRemittedGivenLoanNo(String loanNo,
			String employerCode, String month) {
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),
						EntityCondition.makeCondition("loanNo",
								EntityOperator.EQUALS, loanNo)

				), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		BigDecimal bdTotal = BigDecimal.ZERO;
		
		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			bdTotal = bdTotal.add(genericValue.getBigDecimal("amount"));
		}

		return bdTotal;
	}

	/****
	 * @author Japheth Odonya @when Jun 4, 2015 12:38:50 AM
	 * 
	 *         Returns a list of account product codes in the remitted data
	 * 
	 * */
	private static List<ProductTotal> getAccountProductsRemittedList(
			String employerCode, String month) {
		// TODO Auto-generated method stub
		List<GenericValue> receivedProductsELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedProductsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month),

				EntityCondition.makeCondition("loanNo", EntityOperator.EQUALS,
						"0")

				), EntityOperator.AND);
		try {
			receivedProductsELI = delegator.findList(
					"ExpectedPaymentReceivedProductTotal",
					receivedProductsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		log.info(" AAAAAAAAA All the Products Received .... Count is #### "
				+ receivedProductsELI.size());

		List<ProductTotal> productCodeList = new ArrayList<ProductTotal>();
		ProductTotal productTotal;
		for (GenericValue genericValue : receivedProductsELI) {
			productTotal = new ProductTotal();
			productTotal
					.setCode(genericValue.getString("remitanceCode").trim());
			productTotal.setAmount(genericValue.getBigDecimal("amount"));
			productTotal.setName(LoanUtilities.getAccountProductGivenCodeId(
					genericValue.getString("remitanceCode").trim()).getString(
					"name"));
			productCodeList.add(productTotal);
		}

		return productCodeList;
	}

	private static boolean AllPayrollCodesExist(String employerCode,
			String month) {
		Boolean exists = true;

		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, null)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue missingMemberLog = null;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {

			String payrollNo = genericValue.getString("payrollNo");
			Boolean payrollExists = LoanUtilities
					.payrollNumberExists(payrollNo);

			if (!payrollExists) {
				exists = payrollExists;

				// Save the Payroll Number Missing
				Long missingMemberLogId = delegator.getNextSeqIdLong(
						"MissingMemberLog", 1);
				missingMemberLog = delegator.makeValue("MissingMemberLog",
						UtilMisc.toMap("missingMemberLogId",
								missingMemberLogId, "isActive", "Y",
								"createdBy", "admin", "employerCode",
								employerCode, "payrollNumber", payrollNo,

								"month", month));
				try {
					delegator.createOrStore(missingMemberLog);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}

		}

		return exists;
	}

	// missingPayrolls(employerCode, month)
	public static Boolean missingPayrolls(String employerCode, String month) {

		/***
		 * Check if Payroll Number exists
		 * */
		List<GenericValue> missingMemberLogELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> missingMemberLogConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			missingMemberLogELI = delegator.findList("MissingMemberLog",
					missingMemberLogConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (missingMemberLogELI.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private static Long getMemberAccountId(String code, String payrollNo) {
		// Get Party ID given payrollNo
		Long partyId = null;
		GenericValue member = getMemberByPayrollNo(payrollNo);
		if (member != null)
			partyId = member.getLong("partyId");

		// AccHolderTransactionServices.getM
		// Get Member Account Id given partyId and account code
		Long memberAccountId = null;
		memberAccountId = getMemberAccountId(code, partyId);
		return memberAccountId;
	}

	private static Long getMemberAccountId(String code, Long partyId) {
		// TODO Auto-generated method stub
		// Long accountProductId = AccHolderTransactionServices.get
		List<GenericValue> accountProductELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long accountProductId = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProductId = genericValue.getLong("accountProductId");
		}

		// Get Member Account
		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long memberAccountId = null;

		for (GenericValue memberAccount : memberAccountELI) {
			memberAccountId = memberAccount.getLong("memberAccountId");
		}

		return memberAccountId;
	}

	private static List<String> getAccountProductCodesList() {
		List<GenericValue> accountProductELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<String> accountCodesList = new ArrayList<String>();

		for (GenericValue genericValue : accountProductELI) {
			accountCodesList.add(genericValue.getString("code"));
		}

		return accountCodesList;
	}

	private static void saveLoanRepayment(GenericValue expectedPaymentReceived) {
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;

		// Loan Principal
		loanPrincipal = expectedPaymentReceived.getBigDecimal("amount");
		// Get This Loan's Interest

		String loanProductCode = LoanUtilities
				.getLoanProductCodeGivenLoanNo(expectedPaymentReceived
						.getString("loanNo"));

		String remittanceCodePrincipal = loanProductCode + "A";
		String remittanceCodeInterest = loanProductCode + "B";
		String remittanceCodeInsurance = loanProductCode + "C";

		loanInterest = getLoanInterestOrInsurance(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInterest);
		// Get This Loan's Insurance
		loanInsurance = getLoanInterestOrInsurance(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInsurance);
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = loanPrincipal.add(loanInterest).add(
				loanInsurance);

		BigDecimal bdLoanAmt = getLoanAmount(expectedPaymentReceived
				.getString("loanNo"));

		BigDecimal totalInterestDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInterest);
		BigDecimal totalInsuranceDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInsurance);
		BigDecimal totalPrincipalDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodePrincipal);
		BigDecimal totalLoanDue = totalInterestDue.add(totalInsuranceDue).add(
				totalPrincipalDue);

		Long loanApplicationId = getLoanApplicationId(expectedPaymentReceived
				.getString("loanNo"));
		Long partyId = getLoanPartyId(expectedPaymentReceived
				.getString("loanNo"));
		GenericValue loanRepayment = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", "admin", "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", expectedPaymentReceived.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// delegator.removeAll(dummyPKs)

	}

	// acctgTransId
	private static void saveLoanRepayment(GenericValue expectedPaymentReceived,
			String acctgTransId) {
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;

		// Loan Principal
		loanPrincipal = expectedPaymentReceived.getBigDecimal("amount");
		// Get This Loan's Interest

		String loanProductCode = LoanUtilities
				.getLoanProductCodeGivenLoanNo(expectedPaymentReceived
						.getString("loanNo"));

		String remittanceCodePrincipal = loanProductCode + "A";
		String remittanceCodeInterest = loanProductCode + "B";
		String remittanceCodeInsurance = loanProductCode + "C";

		loanInterest = getLoanInterestOrInsurance(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInterest);
		// Get This Loan's Insurance
		loanInsurance = getLoanInterestOrInsurance(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInsurance);
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = loanPrincipal.add(loanInterest).add(
				loanInsurance);

		BigDecimal bdLoanAmt = getLoanAmount(expectedPaymentReceived
				.getString("loanNo"));

		BigDecimal totalInterestDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInterest);
		BigDecimal totalInsuranceDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodeInsurance);
		BigDecimal totalPrincipalDue = getLoanInterestOrInsuranceDue(
				expectedPaymentReceived.getString("loanNo"),
				expectedPaymentReceived.getString("month"),
				remittanceCodePrincipal);
		BigDecimal totalLoanDue = totalInterestDue.add(totalInsuranceDue).add(
				totalPrincipalDue);

		Long loanApplicationId = getLoanApplicationId(expectedPaymentReceived
				.getString("loanNo"));
		Long partyId = getLoanPartyId(expectedPaymentReceived
				.getString("loanNo"));
		GenericValue loanRepayment = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", "admin", "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", expectedPaymentReceived.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount, "acctgTransId", acctgTransId));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// delegator.removeAll(dummyPKs)

	}

	// saveLoanRepaymentRemittance
	private static PrincipalInterestInsurance saveLoanRepaymentRemittance(
			GenericValue expectedPaymentReceived, String acctgTransId,
			String repaymentMode, BigDecimal bdTotalAmountRemitted, Map<String, String> userLogin, String month, String loanNo) {
		BigDecimal principalAmount = BigDecimal.ZERO;
		BigDecimal interestAmount = BigDecimal.ZERO;
		BigDecimal insuranceAmount = BigDecimal.ZERO;
		BigDecimal excessAmount = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		PrincipalInterestInsurance principalInterestInsurance = new PrincipalInterestInsurance();
		principalInterestInsurance.setExcessToSavingsAmt(BigDecimal.ZERO);

		// Loan Principal
		// loanPrincipal = expectedPaymentReceived.getBigDecimal("amount");
		// Get This Loan's Interest

		//String loanProductCode = LoanUtilities
		//		.getLoanProductCodeGivenLoanNo(expectedPaymentReceived
		//				.getString("loanNo"));

		//String remittanceCodePrincipal = loanProductCode + "A";
		//String remittanceCodeInterest = loanProductCode + "B";
		//String remittanceCodeInsurance = loanProductCode + "C";

		// loanInterest = getLoanInterestOrInsurance(
		// expectedPaymentReceived.getString("loanNo"),
		// expectedPaymentReceived.getString("month"),
		// remittanceCodeInterest);
		// // Get This Loan's Insurance
		// loanInsurance = getLoanInterestOrInsurance(
		// expectedPaymentReceived.getString("loanNo"),
		// expectedPaymentReceived.getString("month"),
		// remittanceCodeInsurance);
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = bdTotalAmountRemitted;
		// loanPrincipal.add(loanInterest).add(
		// loanInsurance);

		BigDecimal bdLoanAmt = getLoanAmount(loanNo);
		Long loanApplicationId = getLoanApplicationId(loanNo);
		Long partyId = LoanUtilities.getEntityValue("LoanApplication",
				"loanApplicationId", loanApplicationId).getLong("partyId");
		BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
				
				//getTotalInsuranceDue(
				//partyId.toString(), loanApplicationId.toString());

		// getLoanInterestOrInsuranceDue(
		// expectedPaymentReceived.getString("loanNo"),
		// expectedPaymentReceived.getString("month"),
		// remittanceCodeInterest);

		BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
				
				//.getTotalInsuranceDue(
				//partyId.toString(), loanApplicationId.toString());

		// getLoanInterestOrInsuranceDue(
		// expectedPaymentReceived.getString("loanNo"),
		// expectedPaymentReceived.getString("month"),
		// remittanceCodeInsurance);
		BigDecimal totalPrincipalDue = LoanRepayments
				.getTotalPrincipalDue(loanApplicationId);

		// getLoanInterestOrInsuranceDue(
		// expectedPaymentReceived.getString("loanNo"),
		// expectedPaymentReceived.getString("month"),
		// remittanceCodePrincipal);

		
		BigDecimal totalLoanDue = BigDecimal.ZERO;

		if (totalInterestDue != null)
		totalLoanDue = totalLoanDue.add(totalInterestDue);
		
		if (totalInsuranceDue != null)
			totalLoanDue = totalLoanDue.add(totalInsuranceDue);
		
		if (totalPrincipalDue != null)
			totalLoanDue = totalLoanDue.add(totalPrincipalDue);

		// Long loanApplicationId = getLoanApplicationId(expectedPaymentReceived
		// .getString("loanNo"));
		// Long partyId = getLoanPartyId(expectedPaymentReceived
		// .getString("loanNo"));
		GenericValue loanRepayment = null;

		// Process the repayment or repaid amounts
		BigDecimal amountRemaining = bdTotalAmountRemitted;
		// BigDecimal bdTotalLoanRepaid = BigDecimal.ZERO;
		BigDecimal bdLoanBalance = BigDecimal.ZERO;
		
		//Long loanApplicationId = LoanUtilities.getLoanApplicationEntityGivenLoanNo(expectedPaymentReceived.getString("loanNo")).getLong("loanApplicationId");
		bdLoanBalance = LoanServices.getLoanRemainingBalance(loanApplicationId);
		// BigDecimal bdLoanAmt = loanRepayment.getBigDecimal("loanAmt");
		
		principalInterestInsurance.setInsuranceAmt(BigDecimal.ZERO);
		principalInterestInsurance.setInterestAmt(BigDecimal.ZERO);
		principalInterestInsurance.setPrincipalAmt(BigDecimal.ZERO);

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
				
				principalInterestInsurance.setInsuranceAmt(insuranceAmount);
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
				
				principalInterestInsurance.setInterestAmt(interestAmount);
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
									loanApplicationId);
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
				
				principalInterestInsurance.setPrincipalAmt(principalAmount);
			}

			if (amountRemaining.compareTo(BigDecimal.ZERO) > 0) {
				excessAmount = amountRemaining;
				principalInterestInsurance.setExcessToSavingsAmt(amountRemaining);
				// Deposit Excess to Savings Account
				GenericValue loanApplication = LoanUtilities.getEntityValue(
						"LoanApplication", "loanApplicationId",
						loanApplicationId);
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

		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", "admin", "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", loanNo,
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				interestAmount, "insuranceAmount", insuranceAmount,
				
				"principalAmount", principalAmount,
				"excessAmount", excessAmount,
				"transactionAmount", transactionAmount,
				"acctgTransId", acctgTransId,
				"month", month,
				"repaymentMode", repaymentMode));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// delegator.removeAll(dummyPKs)
		return principalInterestInsurance;

	}

	private static Long getLoanPartyId(String loanNo) {
		List<GenericValue> loanApplicationELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long partyId = 0L;
		for (GenericValue genericValue : loanApplicationELI) {
			partyId = genericValue.getLong("partyId");
		}

		return partyId;
	}

	private static BigDecimal getLoanInterestOrInsuranceDue(String loanNo,
			String month, String interestorinsurance) {
		BigDecimal total = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanNo", EntityOperator.EQUALS, loanNo),
						EntityCondition.makeCondition("month",
								EntityOperator.EQUALS, month),

						EntityCondition.makeCondition("remitanceCode",
								EntityOperator.EQUALS, interestorinsurance)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentSent", expectedPaymentReceivedConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				total = total.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return total;
	}

	private static BigDecimal getLoanAmount(String loanNo) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal loanAmt = BigDecimal.ZERO;
		for (GenericValue genericValue : loanStatusELI) {
			loanAmt = genericValue.getBigDecimal("loanAmt");
		}

		return loanAmt;
	}

	private static BigDecimal getLoanInterestOrInsurance(String loanNo,
			String month, String interestorinsurance) {
		BigDecimal total = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanNo", EntityOperator.EQUALS, loanNo),
						EntityCondition.makeCondition("month",
								EntityOperator.EQUALS, month),

						EntityCondition.makeCondition("remitanceCode",
								EntityOperator.EQUALS, interestorinsurance)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				total = total.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return total;
	}

	private static void postTransaction(String debitAccountId,
			String postingType, String entrySequenceId, BigDecimal bdTotal,
			String branchId, String acctgTransId, String acctgTransType) {
		// Debit Cash/Bank Account
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntryVersion2(delegator, bdTotal, branchId,
				debitAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

	}

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

	public static String getPayrollNumber(String partyId) {
		String payrollNo = "";

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue member = null;

		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}

		payrollNo = member.getString("payrollNumber");
		return payrollNo;
	}

	public static String getMemberNames(String partyId) {
		String names = "";

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue member = null;

		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}

		names = getNames(member);

		return names;
	}

	public static BigDecimal getTotalByPayrollNo(String payrollNo) {
		BigDecimal total = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"payrollNo", EntityOperator.EQUALS, payrollNo)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {
			if (expectedPaymentReceived.getBigDecimal("amount") != null) {
				total = total.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return total;
	}

	public static GenericValue getMemberByPayrollNo(String payrollNo) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"payrollNumber", EntityOperator.EQUALS, payrollNo)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList("Member",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		GenericValue member = null;
		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			member = genericValue;
		}

		return member;
	}

	public static String createAccountingTransaction(
			GenericValue accountTransaction, String acctgTransType,
			String partyId) {

		GenericValue acctgTrans;
		String acctgTransId;
		Delegator delegator = accountTransaction.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

		String createdBy = "admin";

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

	public static Long getMemberStatusId(String name) {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberStatusId = 0L;
		for (GenericValue genericValue : memberStatusELI) {
			memberStatusId = genericValue.getLong("memberStatusId");
		}

		String memberStatusIdString = String.valueOf(memberStatusId);
		memberStatusIdString = memberStatusIdString.replaceAll(",", "");
		memberStatusId = Long.valueOf(memberStatusIdString);
		return memberStatusId;
	}

	public static String getShareDepositAccountId(String code) {
		// TODO Auto-generated method stub
		List<GenericValue> accountProductELI = null; // =
		String accountProductId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : accountProductELI) {
			accountProductId = String.valueOf(genericValue
					.getLong("accountProductId"));
		}

		return accountProductId;
	}

	public static Long getLoanApplicationId(String loanNo) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("loanNo", loanNo), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long loanApplicationId = 0L;
		for (GenericValue genericValue : loanStatusELI) {
			loanApplicationId = genericValue.getLong("loanApplicationId");
		}

		String loanApplicationIdString = String.valueOf(loanApplicationId);
		loanApplicationIdString = loanApplicationIdString.replaceAll(",", "");
		loanApplicationId = Long.valueOf(loanApplicationIdString);
		return loanApplicationId;
	}

	public static String removeImportedPaymentBreakdown(
			HttpServletRequest request, HttpServletResponse response) {

		// Update Receipts to show generated and post
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String employerCode = (String) request.getParameter("employerCode")
				.trim();
		String month = (String) request.getParameter("month");

		log.info("SSSSSSSSSSSSSSS  Employer Code " + employerCode);
		log.info("SSSSSSSSSSSSSSS  Month " + month);

		// Get Received Records and Delete them
		// removeReceivedRecords(employerCode, month);

		// Get the Missing Records and Delete them
		removeMissingLog(employerCode, month);

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
		return "success";
	}

	private static void removeMissingLog(String employerCode, String month) {
		List<GenericValue> missingMemberLogELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> missingMemberLogConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			missingMemberLogELI = delegator.findList("MissingMemberLog",
					missingMemberLogConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		try {
			delegator.removeAll(missingMemberLogELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/***
	 * Remove the received Records
	 * 
	 * */
	private static void removeReceivedRecords(String employerCode, String month) {
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		try {
			delegator.removeAll(expectedPaymentReceivedELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Boolean stationProcessed(String employerCode, String month) {

		/***
		 * Check Station Processed
		 * */
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, null)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (expectedPaymentReceivedELI.size() > 0) {
			return false;
		} else {
			return true;
		}
	}

	public static Boolean remittedEqualsCheque(String employerCode, String month) {
		BigDecimal totalRemitted = BigDecimal.ZERO;
		BigDecimal chequeAmount = BigDecimal.ZERO;

		totalRemitted = getTotalRemitted(employerCode, month);
		
		chequeAmount = getTotalRemittedChequeAmountAvailable(employerCode, month);
				
				//getTotalRemittedChequeAmount(employerCode, month);

		totalRemitted = totalRemitted.setScale(0, RoundingMode.FLOOR);
		chequeAmount = chequeAmount.setScale(0, RoundingMode.FLOOR);

		// return totalRemitted.setScale(0).compareTo(chequeAmount.setScale(0));
		log.info(" ############## Total Remitted Scaled " + totalRemitted);
		log.info(" ############## Total Cheque Scaled " + chequeAmount);

		if (totalRemitted.compareTo(BigDecimal.ZERO) == 0)
			return false;

		return (totalRemitted.compareTo(chequeAmount) == 0);

	}

	/****
	 * @author Japheth Odonya @when Jun 1, 2015 2:25:37 PM
	 * 
	 *         Deleting the Records for Received expectations for the specified
	 *         month and employercode
	 * 
	 * */
	public static String deleteReceivedPaymentBreakdown(
			Map<String, String> userLogin, String employerCode, String month) {

		// DELETE FROM ExpectedPaymentReceived ALL THE DATA MONTH AND EMPLOYER
		// CODE
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> receivedPayrollsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month)

				), EntityOperator.AND);

		try {
			delegator.removeByCondition("ExpectedPaymentReceived",
					receivedPayrollsConditions);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "fail";
		}

		return "success";
	}

	/***
	 * @author Japheth Odonya @when Jun 1, 2015 7:51:43 PM
	 * 
	 *         Get all existing products in the remittance and for each if the
	 *         member is not subscribed to the product then dont proceede
	 ****/
	public static synchronized String checkAnyProductWithMissingProductOnMember(
			Map<String, String> userLogin, String employerCode, String month) {
		clearMissingMember(month, employerCode);
		/***
		 * Get list of products
		 * 
		 * */
		List<GenericValue> receivedProductsELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedProductsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month),

				EntityCondition.makeCondition("loanNo", EntityOperator.EQUALS,
						"0")

				), EntityOperator.AND);
		try {
			receivedProductsELI = delegator.findList(
					"ExpectedPaymentReceivedProducts",
					receivedProductsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		log.info(" AAAAAAAAA All the Products Received .... Count is #### "
				+ receivedProductsELI.size());
		Long count = 0L;
		String productCode = "";
		List<String> productCodeList = new ArrayList<String>();
		for (GenericValue genericValue : receivedProductsELI) {
			productCode = genericValue.getString("remitanceCode");

			if (!(productCode.equals(FOSA_SAVINGS_CODE.trim()))
					&& !(productCode.equals(MEMBER_DEPOSIT_CODE.trim()))
					&& !(productCode.equals(SHARE_CAPITAL_CODE.trim()))) {
				productCodeList.add(productCode);
			}

		}

		// Check that anyone remitting this product has an account for the
		// product, otherwise
		// Add him to the mising list
		log.info(" LLLLLLL List to process is LLLLLLLLLLLLLLLL ");
		Long prodCount = 0L;
		Boolean failed = false;
		for (String code : productCodeList) {
			log.info(++prodCount + " PPPPPPPP --- " + code);
			if (checkAllUsersRemittingProductMissAccount(userLogin, code,
					month, employerCode))
				failed = true;
		}

		if (failed)
			return "failed";

		return "success";
	}

	/***
	 * Check all members remitting for product code code, that each actually has
	 * an account for that product
	 * */
	private static boolean checkAllUsersRemittingProductMissAccount(
			Map<String, String> userLogin, String code, String month,
			String employerCode) {

		// Get all members remitting for remittance code code
		List<GenericValue> receivedExpectedELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedExpectedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month),

				EntityCondition.makeCondition("remitanceCode",
						EntityOperator.EQUALS, code)

				), EntityOperator.AND);
		try {
			receivedExpectedELI = delegator.findList("ExpectedPaymentReceived",
					receivedExpectedConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String payrollNo = "";
		Long count = 0L;
		Boolean failed = false;
		for (GenericValue genericValue : receivedExpectedELI) {
			// Check that this member is subscribed to this product
			payrollNo = genericValue.getString("payrollNo");
			log.info(++count + "FFFFFFFFFFFF " + code + " !!!!!!!!!!!!!! for "
					+ payrollNo);
			if (!hasAccount(code, payrollNo.trim())) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				addMissingMemberLog(userLogin, payrollNo, month, employerCode,
						code, null, null);
			}
		}
		return failed;
	}

	/****
	 * @author Japheth Odonya @when Jun 1, 2015 10:26:15 PM Ensure that all
	 *         loans remitted with data do exist in the system
	 * 
	 * */
	public static synchronized String checkAllLoansRemittedExist(
			Map<String, String> userLogin, String employerCode, String month) {
		// clearMissingMember(month, employerCode);
		/***
		 * Get list of products
		 ***/
		log.info("CCCCCCCCC To check loans ");
		List<GenericValue> receivedLoansELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> receivedLoansConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS, employerCode),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month),

				EntityCondition.makeCondition("loanNo",
						EntityOperator.NOT_EQUAL, "0")

				), EntityOperator.AND);
		try {
			receivedLoansELI = delegator.findList(
					"ExpectedPaymentReceivedLoans", receivedLoansConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		log.info("CCCCCCCCC Loans Count " + receivedLoansELI.size());
		String loanNo = "";
		Boolean missingLoan = false;
		for (GenericValue genericValue : receivedLoansELI) {
			// Check that loanNo exists in the system

			loanNo = genericValue.getString("loanNo").trim();
			log.info("CCCCCCCCC Loans Checking " + loanNo);
			if (loanIsMissing(userLogin, loanNo, employerCode, month)) {
				missingLoan = true;
			}

		}

		if (missingLoan) {
			return "failed";
		}

		return "success";
	}

	/****
	 * @author Japheth Odonya @when Jun 1, 2015 10:40:28 PM
	 * 
	 *         Return true is loan exists , false otherwise
	 * 
	 * */
	private static boolean loanIsMissing(Map<String, String> userLogin,
			String loanNo, String employerCode, String month) {

		loanNo = loanNo.trim();
		List<GenericValue> loanApplicationELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanNo", EntityOperator.EQUALS, loanNo.trim())

				), EntityOperator.AND);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String payrollNo = getPayrollNumber(loanNo, employerCode, month);

		log.info(" SSSSSSSSSSS THE SIZE " + loanApplicationELI.size());

		if ((loanApplicationELI == null) || (loanApplicationELI.size() < 1)) {
			log.info("TTTTT To add Loan TTTTTTTT to the missing");
			addMissingMemberLog(userLogin, payrollNo, month, employerCode,
					null, loanNo, null);

			return true;
		}

		return false;
	}

	private static String getPayrollNumber(String loanNo, String employerCode,
			String month) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()), EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("loanNo", EntityOperator.EQUALS,
						loanNo)

				), EntityOperator.AND);

		try {
			expectedPaymentReceivedELI = delegator.findList(
					"ExpectedPaymentReceived",
					expectedPaymentReceivedConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue expectedPaymentReceived = null;

		for (GenericValue genericValue : expectedPaymentReceivedELI) {
			expectedPaymentReceived = genericValue;
		}

		if (expectedPaymentReceived != null)
			return expectedPaymentReceived.getString("payrollNo");

		return null;
	}
	
	
	/****
	 * Add Loan Expectation
	 * @author Japheth Odonya
	 * */
	public static void addLoanExpectation(Long loanApplicationId, Long pushMonthYearStationId) {
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		
		GenericValue member = findMember(loanApplication.getLong("partyId").toString());

		Long activeMemberStatusId = getMemberStatusId("ACTIVE");
		//if (member.getLong("memberStatusId").equals(activeMemberStatusId)) {

			GenericValue loanProduct = findLoanProduct(loanApplication
					.getString("loanProductId"));
			GenericValue station = findStation(member.getLong("stationId")
					.toString());

			//String month = getCurrentMonth();
			String month = getPushMonthYearMonth(pushMonthYearStationId);
			String employerName = "";

			String stationNumber = "";
			String stationName = "";
			String employerCode = "";

			if (station != null) {
				employerName = station.getString("name");// getEmployer(station.getString("employerId"));
				stationNumber = station.getString("stationNumber").trim();
				;
				stationName = station.getString("name");
				employerCode = station.getString("employerCode").trim();
			}
			// String employerName = station.getString("name");
			// getEmployer(station.getString("employerId"));

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			// Create an expectation
			GenericValue expectedPaymentSent = null;

			String employeeNames = getNames(member);

			String remitanceCode = "";
			String expectationType = "";
			String remitanceDescription = loanProduct.getString("name");

			String remitanceCodeBal = "";
			String expectationTypeBal = "";
			String remitanceDescriptionBal = "";
			
			//Add Expectation Balance
			remitanceCodeBal = loanProduct.getString("code") + "D";
			expectationTypeBal = "BALANCE";
			remitanceDescriptionBal = loanProduct.getString("name")
					+ " BALANCE";
			
			addExpectationBalanceWithPushId(remitanceCodeBal, expectationTypeBal,
					remitanceDescriptionBal, member, loanApplication,
					null, station, month, stationNumber,
					stationName, employerCode, employerName, employeeNames, pushMonthYearStationId);
			
			//Add Principal
			remitanceCode = loanProduct.getString("code") + "A";
			remitanceDescription = remitanceDescription + " PRINCIPAL";
			expectationType = "PRINCIPAL";
			BigDecimal bdPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
			if (bdPrincipalDue.compareTo(BigDecimal.ZERO) < 1)
			{
				bdPrincipalDue = BigDecimal.ZERO;
			}
			Long expectedPaymentSentId = delegator.getNextSeqIdLong("ExpectedPaymentSent");
			expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
					UtilMisc.toMap("expectedPaymentSentId", expectedPaymentSentId, "isActive", "Y", "branchId",
							member.getString("branchId"), "remitanceCode",
							remitanceCode, "stationNumber", stationNumber,
							"stationName", stationName,

							"payrollNo", member.getString("payrollNumber"),
							"employerCode", employerCode,

							"employeeNumber",
							member.getString("employeeNumber"), "memberNumber",
							member.getString("memberNumber"),

							"loanNo", loanApplication.getString("loanNo"),
							"employerNo", employerName, "amount",
							bdPrincipalDue,
							"remitanceDescription", remitanceDescription,
							"employeeName", employeeNames, "expectationType",
							expectationType, "month", month, "pushMonthYearStationId", pushMonthYearStationId));
			try {
				delegator.createOrStore(expectedPaymentSent);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			
			//Add Interest
			remitanceCode = loanProduct.getString("code") + "B";
			remitanceDescription = loanProduct.getString("name") + " INTEREST";
			expectationType = "INTEREST";
//			BigDecimal bdInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
//			if (bdInterestDue.compareTo(BigDecimal.ZERO) < 1)
//			{
//				bdInterestDue = BigDecimal.ZERO;
//			}
			
			BigDecimal bdInterestDue = LoanRepayments.getInterestOnSchedule(loanApplicationId);
			
			//	LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
				
				//LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
			if (bdInterestDue.compareTo(BigDecimal.ZERO) < 1)
			{
				bdInterestDue = BigDecimal.ZERO;
			}
			
			expectedPaymentSentId = delegator.getNextSeqIdLong("ExpectedPaymentSent");
			expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
					UtilMisc.toMap("expectedPaymentSentId", expectedPaymentSentId, "isActive", "Y", "branchId",
							member.getString("branchId"), "remitanceCode",
							remitanceCode, "stationNumber", stationNumber,
							"stationName", stationName,

							"payrollNo", member.getString("payrollNumber"),
							"employerCode", employerCode,

							"employeeNumber",
							member.getString("employeeNumber"), "memberNumber",
							member.getString("memberNumber"),

							"loanNo", loanApplication.getString("loanNo"),
							"employerNo", employerName, "amount",
							bdInterestDue,
							"remitanceDescription", remitanceDescription,
							"employeeName", employeeNames, "expectationType",
							expectationType, "month", month, "pushMonthYearStationId", pushMonthYearStationId));
			try {
				delegator.createOrStore(expectedPaymentSent);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			
			//Add Insurance
			remitanceCode = loanProduct.getString("code") + "C";
			remitanceDescription = loanProduct.getString("name") + " INSURANCE";
			expectationType = "INSURANCE";
			BigDecimal bdInsuranceDue = LoanRepayments.getInsuranceOnSchedule(loanApplicationId);
					
					//LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
			
			if (bdInsuranceDue.compareTo(BigDecimal.ZERO) < 1)
			{
				bdInsuranceDue = BigDecimal.ZERO;
			}
			

			
			
			expectedPaymentSentId = delegator.getNextSeqIdLong("ExpectedPaymentSent");
			expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
					UtilMisc.toMap("expectedPaymentSentId", expectedPaymentSentId, "isActive", "Y", "branchId",
							member.getString("branchId"), "remitanceCode",
							remitanceCode, "stationNumber", stationNumber,
							"stationName", stationName,

							"payrollNo", member.getString("payrollNumber"),
							"employerCode", employerCode,

							"employeeNumber",
							member.getString("employeeNumber"), "memberNumber",
							member.getString("memberNumber"),

							"loanNo", loanApplication.getString("loanNo"),
							"employerNo", employerName, "amount",
							bdInsuranceDue,
							"remitanceDescription", remitanceDescription,
							"employeeName", employeeNames, "expectationType",
							expectationType, "month", month, "pushMonthYearStationId", pushMonthYearStationId));
			try {
				delegator.createOrStore(expectedPaymentSent);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}


			

		//}

	}
	
	
	
	private static String getPushMonthYearMonth(Long pushMonthYearStationId) {
		GenericValue pushMonthYearStation = LoanUtilities.getEntityValue("PushMonthYearStation", "pushMonthYearStationId", pushMonthYearStationId);
		String month = pushMonthYearStation.getLong("month").toString()+pushMonthYearStation.getLong("year").toString();
		
		return month;
	}

	private static void addExpectationBalanceWithPushId(String remitanceCodeBal,
			String expectationTypeBal, String remitanceDescriptionBal,
			GenericValue member, GenericValue loanApplication,
			GenericValue loanExpectation, GenericValue station, String month,
			String stationNumber, String stationName, String employerCode,
			String employerName, String employeeNames, Long pushMonthYearStationId) {

		BigDecimal bdLoanBalance = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplication
						.getLong("loanApplicationId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue expectedPaymentSent = null;
		Long expectedPaymentSentId = delegator.getNextSeqIdLong("ExpectedPaymentSent");
		
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("expectedPaymentSentId", expectedPaymentSentId, "isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						remitanceCodeBal, "stationNumber", stationNumber,
						"stationName", stationName,

						"payrollNo", member.getString("payrollNumber"),
						"employerCode", employerCode,

						"employeeNumber", member.getString("employeeNumber"),
						"memberNumber", member.getString("memberNumber"),

						"loanNo", loanApplication.getString("loanNo"),
						"employerNo", employerName, "amount", bdLoanBalance,
						"remitanceDescription", remitanceDescriptionBal,
						"employeeName", employeeNames, "expectationType",
						expectationTypeBal, "month", month, "pushMonthYearStationId", pushMonthYearStationId));

		try {
			delegator.createOrStore(expectedPaymentSent);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
