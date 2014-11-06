package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
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

import javolution.util.FastMap;

import org.apache.log4j.Logger;
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

	public static String generateExpectedPaymentStations(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		List<GenericValue> memberELI = null;
		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");

		try {
			memberELI = delegator.findList("Member", null, null, null, null,
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Set<String> setMemberStations = new HashSet<String>();
		String stationId = null;
		for (GenericValue member : memberELI) {
			// Add station Id to set
			stationId = member.getString("stationId");
			if (stationId != null) {
				setMemberStations.add(stationId);
			}
		}
		String createdBy = (String) request.getAttribute("userLoginId");
		String month = getCurrentMonth();
		// With the set IDs create ExpectatedStation
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (String tempStationId : setMemberStations) {
			createExpectedStation(tempStationId, month, createdBy);
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Add shares
		String shareCode = getShareCode();
		addExpectedShares(shareCode);

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
		GenericValue loanApplication = findLoanApplication(loanExpectation
				.getString("loanApplicationId"));
		GenericValue loanProduct = findLoanProduct(loanApplication
				.getString("loanProductId"));
		GenericValue station = findStation(member.getString("stationId"));

		String month = getCurrentMonth();
		String employerName = getEmployer(station.getString("employerId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Create an expectation
		GenericValue expectedPaymentSent = null;

		String employeeNames = getNames(member);

		String remitanceCode = "";
		String expectationType = "";
		String remitanceDescription = loanProduct.getString("name");

		if (loanExpectation.getString("repaymentName").equals("PRINCIPAL")) {
			remitanceCode = loanProduct.getString("code") + "A";
			remitanceDescription = remitanceDescription + " PRINCIPAL";
			expectationType = "PRINCIPAL";
		} else if (loanExpectation.getString("repaymentName")
				.equals("INTEREST")) {
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
						remitanceCode, "stationNumber",
						station.getString("stationNumber"), "stationName",
						station.getString("name"),

						"payrollNo", member.getString("payrollNumber"),
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

	/***
	 * @author Japheth Odonya @when Sep 22, 2014 3:35:42 PM Add Account
	 *         Contributions
	 * **/
	private static void addAccountContributions() {
		List<GenericValue> memeberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memeberELI = delegator.findList("Member",
					EntityCondition.makeCondition("memberStatus", "ACTIVE"),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue member : memeberELI) {

			addMemberExpectedAccountContributions(member);
		}
	}

	/****
	 * Add Account Contributions for this member
	 * 
	 * @author Japheth Odonya @when Sep 22, 2014 3:40:45 PM
	 * */
	private static void addMemberExpectedAccountContributions(
			GenericValue member) {
		// Get from MemberAccount - accounts that are contributing and belong to
		// this member
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();

		List<String> orderByList = new LinkedList<String>();
		orderByList.add("accountProductId");

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"contributing", EntityOperator.EQUALS, "YES"),
						EntityCondition.makeCondition("partyId",
								EntityOperator.EQUALS,
								member.getString("partyId"))

				), EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String previousAccountProductId = "";
		String currentAccountProduct = "";
		int sequence = 1;
		for (GenericValue memberAccount : memberAccountELI) {
			// Add an expectation based on this member
			currentAccountProduct = memberAccount.getString("accountProductId");
			if (currentAccountProduct.equals(previousAccountProductId)) {
				sequence = sequence + 1;
			} else {
				sequence = 1;
			}
			addExpectedAccountContribution(memberAccount, member, sequence);

			previousAccountProductId = currentAccountProduct;
		}
	}

	/****
	 * Create Expectation
	 * **/
	private static void addExpectedAccountContribution(
			GenericValue memberAccount, GenericValue member, int sequence) {
		GenericValue station = findStation(member.getString("stationId"));
		String month = getCurrentMonth();
		String employerName = getEmployer(station.getString("employerId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Create an expectation
		GenericValue expectedPaymentSent = null;

		String employeeNames = getNames(member);

		GenericValue accountProduct = findAccountProduct(memberAccount
				.getString("accountProductId"));

		String remitanceCode = accountProduct.getString("code")
				+ String.valueOf(sequence);

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						remitanceCode, "stationNumber",
						station.getString("stationNumber"), "stationName",
						station.getString("name"),

						"payrollNo", member.getString("payrollNumber"),
						"loanNo", "0", "employerNo", employerName, "amount",
						memberAccount.getBigDecimal("contributingAmount"),
						"remitanceDescription",
						accountProduct.getString("name"), "employeeName",
						employeeNames, "expectationType", "ACCOUNT", "month",
						month));
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

	private static void addExpectedShares(String shareCode) {

		// for each member save a share expected
		List<GenericValue> memeberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memeberELI = delegator.findList("Member",
					EntityCondition.makeCondition("memberStatus", "ACTIVE"),
					null, null, null, false);
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
		String employerName = getEmployer(station.getString("employerId"));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Create an expectation
		GenericValue expectedPaymentSent = null;

		String employeeNames = getNames(member);

		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		expectedPaymentSent = delegator.makeValue("ExpectedPaymentSent",
				UtilMisc.toMap("isActive", "Y", "branchId",
						member.getString("branchId"), "remitanceCode",
						shareCode, "stationNumber",
						station.getString("stationNumber"), "stationName",
						station.getString("name"),

						"payrollNo", member.getString("payrollNumber"),
						"loanNo", "0", "employerNo", employerName, "amount",
						member.getBigDecimal("shareAmount"),
						"remitanceDescription", "SHARES", "employeeName",
						employeeNames, "expectationType", "SHARES",

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

	private static String getEmployer(String employerId) {
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
		// TODO Auto-generated method stub
		Calendar now = Calendar.getInstance();

		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);

		month = month + 1;

		String monthName = "";
		// if (month < 10)
		// {
		// monthName = "0"+month;
		// } else{
		monthName = String.valueOf(month);
		// }
		String currentMonth = monthName + String.valueOf(year);
		return currentMonth;
	}

	/***
	 * @author Japheth Odonya @when Sep 18, 2014 1:02:52 PM
	 * 
	 *         Create StationExpectation
	 * 
	 * */
	private static void createExpectedStation(String tempStationId,
			String month, String createdBy) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue station = findStation(tempStationId);
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
		String stationNumber = station.getString("stationNumber");
		String stationName = station.getString("name");
		GenericValue stationExpectation = null;
		stationExpectation = delegator.makeValue("StationExpectation", UtilMisc
				.toMap("isActive", "Y", "createdBy", createdBy, "branchId",
						branchId, "stationNumber", stationNumber,
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

	private static GenericValue findAccountProduct(String accountProductId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue accountProduct = null;
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct;
	}

	private static GenericValue findMember(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue member = null;
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return member;
	}

	private static GenericValue findLoanProduct(String loanProductId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct;
	}

	private static GenericValue findLoanApplication(String loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue loanApplication = null;
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
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

	/***
	 * Get total expected for station and month
	 * */
	public static BigDecimal getTotalExpected(String stationNumber, String month) {
		BigDecimal totalExpected = BigDecimal.ZERO;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationNumber", EntityOperator.EQUALS, stationNumber),
						EntityCondition.makeCondition("month",
								EntityOperator.EQUALS, month)

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
				totalExpected = totalExpected.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			}
		}

		return totalExpected;
	}

	public static String isRemitanceEnough(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String stationNumber = (String) request.getParameter("stationNumber");
		String month = (String) request.getParameter("month");

		GenericValue station = findStationGivenStationNumber(stationNumber);

		// Get
		List<GenericValue> stationAccountTransactionELI = null;

		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationId", EntityOperator.EQUALS,
						station.getString("stationId")), EntityCondition
						.makeCondition("monthyear", EntityOperator.EQUALS,
								month)

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
		BigDecimal totalSubmitted = getTotalExpected(stationNumber, month);

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

	public static BigDecimal getTotalRemittedChequeAmount(String stationNumber,
			String month) {

		GenericValue station = findStationGivenStationNumber(stationNumber);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get
		List<GenericValue> stationAccountTransactionELI = null;

		// Get total amount given station and month
		EntityConditionList<EntityExpr> stationAccountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationId", EntityOperator.EQUALS,
						station.getString("stationId")), EntityCondition
						.makeCondition("monthyear", EntityOperator.EQUALS,
								month)

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

	/****
	 * @author Japheth Odonya @when Sep 23, 2014 8:40:10 AM
	 * 
	 *         Update Process Received Payments
	 * 
	 * */
	public static String processReceivedPaymentBreakdown(
			HttpServletRequest request, HttpServletResponse response) {

		// Update Receipts to show generated and post
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String stationNumber = (String) request.getParameter("stationNumber");
		String month = (String) request.getParameter("month");
		
		log.info("SSSSSSSSSSSSSSS  Station Number "+stationNumber);
		log.info("SSSSSSSSSSSSSSS  Month "+month);
		/**
		 * <field name="processed" type="indicator"></field> <field
		 * name="dateProcessed" type="date-time"></field>
		 **/
		List<GenericValue> expectedPaymentReceivedELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> expectedPaymentReceivedConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"stationNumber", EntityOperator.EQUALS, stationNumber),
						EntityCondition.makeCondition("month",
								EntityOperator.EQUALS, month),
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
		BigDecimal bdAccount = BigDecimal.ZERO;
		BigDecimal bdTotal = BigDecimal.ZERO;

		String branchId = "";
		log.info(" SSSSSSSSSSSSSS Number of Records is "+expectedPaymentReceivedELI.size());
		for (GenericValue expectedPaymentReceived : expectedPaymentReceivedELI) {

			if (branchId.equals("")) {
				branchId = getMemberByPayrollNo(
						expectedPaymentReceived.getString("payrollNo"))
						.getString("branchId");
			}

			/**
			 * PRINCIPAL INTEREST INSURANCE ACCOUNT SHARES
			 * 
			 * */
			if (expectedPaymentReceived.getString("expectationType").equals(
					"SHARES")) {
				bdSharesTotal = bdSharesTotal.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			} else if (expectedPaymentReceived.getString("expectationType")
					.equals("ACCOUNT")) {
				bdAccount = bdAccount.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			} else if (expectedPaymentReceived.getString("expectationType")
					.equals("PRINCIPAL")) {
				bdPrincipal = bdPrincipal.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			} else if (expectedPaymentReceived.getString("expectationType")
					.equals("INTEREST")) {
				bdInterest = bdInterest.add(expectedPaymentReceived
						.getBigDecimal("amount"));
			} else if (expectedPaymentReceived.getString("expectationType")
					.equals("INSURANCE")) {
				bdInsurance = bdInsurance.add(expectedPaymentReceived
						.getBigDecimal("amount"));
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

		String postingType = "D";
		GenericValue accountHolderTransactionSetup = null;
		// Get Account to debit - the Station Debit Account
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord(
						"STATIONACCOUNTPAYMENT", delegator);
		String debitAccountId = accountHolderTransactionSetup
				.getString("cashAccountId");

		String acctgTransType = "STATION_DEPOSIT";

		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(
				accountHolderTransactionSetup, acctgTransType, branchId);
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

		// SHAREDEPOSITACCOUNT
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord("SHAREDEPOSITACCOUNT",
						delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";
		entrySequenceId = "00002";
		if (bdSharesTotal.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdSharesTotal, branchId, acctgTransId, acctgTransType);
		}
		// MEMBERTRANSACTIONACCOUNT - Account
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord(
						"MEMBERTRANSACTIONACCOUNT", delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";
		entrySequenceId = "00003";
		if (bdAccount.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdAccount, branchId, acctgTransId, acctgTransType);
		}
		// PRINCIPALPAYMENT
		accountHolderTransactionSetup = LoanRepayments
				.getAccountHolderTransactionSetupRecord("PRINCIPALPAYMENT",
						delegator);
		creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		postingType = "C";
		entrySequenceId = "00004";
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
		entrySequenceId = "00005";

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
		entrySequenceId = "00006";
		if (bdInsurance.compareTo(BigDecimal.ZERO) == 1) {
			postTransaction(creditAccountId, postingType, entrySequenceId,
					bdInsurance, branchId, acctgTransId, acctgTransType);
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
		LoanRepayments.postTransactionEntry(delegator, bdTotal, branchId,
				debitAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
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

}