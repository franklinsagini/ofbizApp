package org.ofbiz.interestmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.calendar.RecurrenceRule;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class InterestManagementServices {
	public static Logger log = Logger
			.getLogger(InterestManagementServices.class);
	public static String priorTransactions = "NO";

	public static String getFixedDepositContractDuration(
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		Date startDate = null;
		try {
			startDate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("startDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Date endDate = null;
		try {
			endDate = (Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
					.getParameter("endDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		Logger log = Logger.getLogger(InterestManagementServices.class);
		log.info("LLLLLLLLL FROM : " + startDate);
		log.info("LLLLLLLLL TO : " + endDate);

		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		int periodInMonths = Months.monthsBetween(localDateStartDate,
				localDateEndDate).getMonths();

		result.put("periodInMonths", periodInMonths);

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
				// TODO Auto-generated catch block
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

	public static String getFixedDepositContractEndDate(
			HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Date startDate = null;

		try {
			startDate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("startDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		int periodInMonths = new Integer(request.getParameter("periodInMonths"))
				.intValue();

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());
		localDateEndDate = localDateEndDate.plusMonths(periodInMonths);
		// Date endDate =
		// AccHolderTransactionServices.calculateEndWorkingDay(fromDate,
		// leaveDuration);

		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

		String i18EndDate = sdfDisplayDate.format(localDateEndDate.toDate());
		String endDate = sdfDate.format(localDateEndDate.toDate());

		result.put("endDate_i18n", i18EndDate);
		result.put("endDate", endDate);
		Gson gson = new Gson();
		String json = gson.toJson(result);

		response.setContentType("application/x-json");
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
				e1.printStackTrace();
			}
		}
		return json;
	}

	public static Map<String, Object> calculateFixedDepositInterest(
			DispatchContext context, Map<String, String> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		System.out
				.println("############## Attempting to calculate Fixed Deposit Interest ... "
						+ Calendar.getInstance().getTime());

		// Get all fixed deposit active contracts
		// FixedDepositContract
		processFixedDepositContracts();
		addSavingsTypeContracts();
		processSavingsTypeInterestContracts();

		return result;
	}

	/***
	 * Get all Savings Type Contracts
	 * 
	 * - earns interest - has minimum interest earning amount
	 * 
	 * */
	public static void addSavingsTypeContracts() {
		List<Long> listSavingsTypeAccount = new ArrayList<Long>();

		/***
		 * Should return the IDs for Savings Account and other accounts that 1)
		 * Earn Interest and 2) Have a minimum interest earning amount - this
		 * excludes Fixed Deposit
		 **/
		listSavingsTypeAccount = getSavingsTypeAccountProductId();

		/***
		 * Create SavingTypeContract based on the Savings Account List
		 * */
		for (Long accountProductId : listSavingsTypeAccount) {

			createSavingsTypeAccounts(accountProductId);

		}
	}

	private static void createSavingsTypeAccounts(Long accountProductId) {
		// Get all MemberAccounts for this AccountProduct
		//
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId)), EntityOperator.AND);
		List<GenericValue> memberAccountELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if (memberAccountELI == null) {
			log.info(" ######### The are no Accounts for this Product to Add #########");
		}
		for (GenericValue genericValue : memberAccountELI) {
			createNewSavingsTypeContract(genericValue);
		}
	}

	private static void createNewSavingsTypeContract(GenericValue memberAccount) {
		// Check if memberAccountId is not already added to SavingsTypeContract
		// and Add it
		if (!savingsTypeContractExists(memberAccount.getLong("memberAccountId"))
				&& (accountOpeningBalanceExists(memberAccount
						.getLong("memberAccountId")) || accountTransactionExists(memberAccount
						.getLong("memberAccountId")))) {
			addContract(memberAccount);
		}

	}

	/*****
	 * Adds a contract for this MemberAccount typically for this savings account
	 * 
	 * */
	private static void addContract(GenericValue memberAccount) {
		// savingsTypeContractId - from sequence
		// partyId - from memberAccount
		// memberAccountId from memberAccount
		// interestRatePA from AccountProduct - through MemberAccount
		// startDate - either openingDate, firstTransactionDate or startOfYear
		// if the previous two dates are
		// are greater than start of the year
		// endDate lastDate of the year

		// closed - NO
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long savingsTypeContractId = delegator.getNextSeqIdLong(
				"SavingsTypeContract", 1);
		Long partyId = memberAccount.getLong("partyId");
		Long memberAccountId = memberAccount.getLong("memberAccountId");
		BigDecimal interestRatePa = getAccountInterestRatePA(memberAccount
				.getLong("accountProductId"));
		Timestamp startDate = getAccountStartDate(memberAccount
				.getLong("memberAccountId"));
		Timestamp lastDate = getLastDateOfTheYear();

		GenericValue savingsTypeContract = null;

		savingsTypeContract = delegator
				.makeValue("SavingsTypeContract", UtilMisc.toMap(
						"savingsTypeContractId", savingsTypeContractId,
						"partyId", partyId, "isActive", "Y", "createdBy",
						"admin", "memberAccountId", memberAccountId,
						"interestRatePA", interestRatePa,

						"startDate", startDate, "endDate", lastDate,
						"priorTransactions", priorTransactions, "closed", "NO"));

		try {
			delegator.createOrStore(savingsTypeContract);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/****
	 * Account Start Date is either 1) Account Opening Date if exists and is
	 * greater that first Date of this year or 2) First Transaction Date if
	 * exists and is greater than first date of this year or 3) First Date of
	 * this year
	 * 
	 * */
	private static Timestamp getAccountStartDate(Long memberAccountId) {
		// TODO Auto-generated method stub
		priorTransactions = "NO";
		if (accountOpeningBalanceExists(memberAccountId)
				&& (getOpeningDate(memberAccountId)
						.after(getFirstDateOfTheYear()))) {
			return getOpeningDate(memberAccountId);
		}
		{
			if (accountOpeningBalanceExists(memberAccountId)) {
				priorTransactions = "YES";
			}
		}

		if ((accountFirstTransactionExists(memberAccountId))) {
			return getFirstTransactionDate(memberAccountId);
		} else {
			if (accountTransactionExists(memberAccountId)) {
				priorTransactions = "YES";
			}
		}

		return getFirstDateOfTheYear();
	}

	private static Timestamp getFirstTransactionDate(Long memberAccountId) {
		EntityConditionList<EntityExpr> accountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN,
						getFirstDateOfTheYear())), EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		List<String> listAccountOrder = new ArrayList<String>();
		listAccountOrder.add("accountTransactionId");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			accountTransactionELI = delegator.findList("AccountTransaction",
					accountTransactionConditions, null, listAccountOrder, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((accountTransactionELI == null)
				|| (accountTransactionELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN No Transaction !!!!!!!!!!!!! ");
			return null;
		} else {
			log.info("GGGGGGGGGGGGGGG There are Transactions that fit ");
			return accountTransactionELI.get(0).getTimestamp("createdStamp");
		}

	}

	private static boolean accountFirstTransactionExists(Long memberAccountId) {
		EntityConditionList<EntityExpr> accountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN,
						getFirstDateOfTheYear())), EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			accountTransactionELI = delegator.findList("AccountTransaction",
					accountTransactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((accountTransactionELI == null)
				|| (accountTransactionELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN No Transaction !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG There are Transactions that fit ");
		}

		return true;

	}

	private static boolean accountTransactionExists(Long memberAccountId) {
		EntityConditionList<EntityExpr> accountTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId)), EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			accountTransactionELI = delegator.findList("AccountTransaction",
					accountTransactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((accountTransactionELI == null)
				|| (accountTransactionELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN No Transaction !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG There are Transactions that fit ");
		}

		return true;

	}

	private static Timestamp getOpeningDate(Long memberAccountId) {
		EntityConditionList<EntityExpr> openingBalanceContractConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId)), EntityOperator.AND);
		List<GenericValue> memberAccountDetailsELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> memberAccountDetailsIdList = new ArrayList<String>();
		memberAccountDetailsIdList.add("-memberAccountDetailsId");
		try {

			memberAccountDetailsELI = delegator.findList(
					"MemberAccountDetails", openingBalanceContractConditions,
					null, memberAccountDetailsIdList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((memberAccountDetailsELI == null)
				|| (memberAccountDetailsELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN No Balance !!!!!!!!!!!!! ");
			return null;
		} else {
			log.info("GGGGGGGGGGGGGGG There is Member Opening Account ");
			return memberAccountDetailsELI.get(0).getTimestamp(
					"openingBalanceDate");
		}

	}

	private static boolean accountOpeningBalanceExists(Long memberAccountId) {

		EntityConditionList<EntityExpr> openingBalanceContractConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId)), EntityOperator.AND);
		List<GenericValue> memberAccountDetailsELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			memberAccountDetailsELI = delegator.findList(
					"MemberAccountDetails", openingBalanceContractConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((memberAccountDetailsELI == null)
				|| (memberAccountDetailsELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN No Balance !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG There is Member Opening Account ");
		}

		return true;

	}

	private static Timestamp getFirstDateOfTheYear() {
		LocalDateTime startOfYear = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		startOfYear = startOfYear.withMonthOfYear(1).withDayOfMonth(1)
				.withTime(0, 0, 0, 0);
		return new Timestamp(startOfYear.toDate().getTime());
	}

	private static Timestamp getLastDateOfTheYear() {
		LocalDateTime startOfYear = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		startOfYear = startOfYear.withMonthOfYear(1).withDayOfMonth(1)
				.withTime(0, 0, 0, 0);
		LocalDateTime endOfYear = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		endOfYear = startOfYear.plusYears(1);
		endOfYear = endOfYear.minusSeconds(1);
		Timestamp lastDate = new Timestamp(endOfYear.toDate().getTime());
		return lastDate;
	}

	private static BigDecimal getAccountInterestRatePA(Long accountProductId) {
		// Get Interest Rate PA for Account
		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct.getBigDecimal("interestPerAnum");
	}

	private static boolean savingsTypeContractExists(Long memberAccountId) {

		LocalDateTime startOfYear = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		startOfYear = startOfYear.withMonthOfYear(1).withDayOfMonth(1)
				.withTime(0, 0, 0, 0);
		LocalDateTime endOfYear = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		endOfYear = startOfYear.plusYears(1);

		log.info(" TTTTTTTTTTT First Day of the Year ... "
				+ startOfYear.toDate() + " TTTTTT First Date of Next Year "
				+ endOfYear.toDate());

		EntityConditionList<EntityExpr> savingTypeContractConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"startDate", EntityOperator.GREATER_THAN_EQUAL_TO,
						new Timestamp(startOfYear.toDate().getTime())),
						EntityCondition.makeCondition("endDate",
								EntityOperator.LESS_THAN, new Timestamp(
										endOfYear.toDate().getTime()))),
						EntityOperator.AND);
		List<GenericValue> savingsTypeContractELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			savingsTypeContractELI = delegator.findList("SavingsTypeContract",
					savingTypeContractConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((savingsTypeContractELI == null)
				|| (savingsTypeContractELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN This Years Contract not added, am going to add it !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG This Year Contract already added Nothing to do ");
		}

		return true;
	}

	private static List<Long> getSavingsTypeAccountProductId() {
		// TODO Auto-generated method stub
		List<Long> listSavingsTypeAccount = new ArrayList<Long>();
		EntityConditionList<EntityExpr> savingTypeContractConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"earnsInterest", EntityOperator.EQUALS, "YES"),
						EntityCondition.makeCondition(
								"hasMinimumInterestEarningAmount",
								EntityOperator.EQUALS, "YES")),
						EntityOperator.AND);
		List<GenericValue> accountProductELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			accountProductELI = delegator.findList("AccountProduct",
					savingTypeContractConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if (accountProductELI == null) {
			log.info(" ######### The are no Savings Type Accounts to Add #########");
		}
		for (GenericValue genericValue : accountProductELI) {
			listSavingsTypeAccount
					.add(genericValue.getLong("accountProductId"));
		}
		return listSavingsTypeAccount;
	}

	private static void processFixedDepositContracts() {
		// Get all fixed deposit contracts not closed
		EntityConditionList<EntityExpr> contractsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"closed", EntityOperator.EQUALS, "NO")),
						EntityOperator.AND);
		List<GenericValue> fixedDepositContractELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			fixedDepositContractELI = delegator.findList(
					"FixedDepositContract", contractsConditions, null, null,
					null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (fixedDepositContractELI == null) {
			log.info(" ######### There are no fixed deposit contracts #########");
		}

		for (GenericValue fixedDepositContract : fixedDepositContractELI) {
			// for each fixed deposit contract, check if there is a run for this
			// month and add an earning if there is none
			computeInterest(fixedDepositContract);
		}

	}

	private static void computeInterest(GenericValue fixedDepositContract) {
		BigDecimal bdAmount = fixedDepositContract.getBigDecimal("amount");
		BigDecimal bdInterestRatePA = fixedDepositContract
				.getBigDecimal("interestRatePA");
		Long periodInMonths = fixedDepositContract.getLong("periodInMonths");
		Timestamp startDate = fixedDepositContract.getTimestamp("startDate");
		Timestamp endDate = fixedDepositContract.getTimestamp("endDate");

		Long fixedDepositContractId = fixedDepositContract
				.getLong("fixedDepositContractId");
		Long memberAccountId = fixedDepositContract.getLong("memberAccountId");
		// Check if there is a run for this day and add interest if there is
		// none

		Boolean earningExist = false;

		earningExist = checkEarningExist(fixedDepositContractId,
				memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long fixedDepositContractEarningId = delegator.getNextSeqIdLong(
				"FixedDepositContractEarning", 1);
		GenericValue fixedDepositContractEarning = null;
		BigDecimal bdAmountEarned = BigDecimal.ZERO;

		if (!earningExist) {
			log.info("AAAAAAAAAA Adding !!");
			bdAmountEarned = calculateFixedDepositEarning(bdAmount,
					bdInterestRatePA, periodInMonths);
			// Add earning
			fixedDepositContractEarning = delegator.makeValue(
					"FixedDepositContractEarning", UtilMisc.toMap(
							"fixedDepositContractEarningId",
							fixedDepositContractEarningId,
							"fixedDepositContractId", fixedDepositContractId,
							"isActive", "Y", "createdBy", "admin",
							"dateEarned", new Timestamp(Calendar.getInstance()
									.getTimeInMillis()),

							"amount", bdAmountEarned.setScale(6,
									RoundingMode.HALF_UP)));
			try {
				delegator.createOrStore(fixedDepositContractEarning);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

		}

	}

	/***
	 * Compute daily Fixed Deposit Earning
	 * */
	private static BigDecimal calculateFixedDepositEarning(BigDecimal bdAmount,
			BigDecimal bdInterestRatePA, Long periodInMonths) {
		// INTEREST = P * R
		// amount = P = bdAmount
		// T is 1/365
		// Interest
		BigDecimal bdDailyInterestRate = bdInterestRatePA.divide(
				new BigDecimal(100), 6, RoundingMode.HALF_UP).divide(
				new BigDecimal(365), 6, RoundingMode.HALF_UP);

		BigDecimal bdEarnedAmount = bdAmount.multiply(bdDailyInterestRate);
		return bdEarnedAmount;
	}

	private static Boolean checkEarningExist(Long fixedDepositContractId,
			Long memberAccountId) {

		LocalDateTime startOfDayDateTime = new LocalDateTime(Calendar
				.getInstance().getTimeInMillis());
		startOfDayDateTime = startOfDayDateTime.withTime(0, 0, 0, 0);
		LocalDateTime endOfDayDateTime = new LocalDateTime(Calendar
				.getInstance().getTimeInMillis());
		endOfDayDateTime = endOfDayDateTime.plusDays(1).withTime(0, 0, 0, 0);
		EntityConditionList<EntityExpr> earningConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"fixedDepositContractId", EntityOperator.EQUALS,
						fixedDepositContractId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
						new Timestamp(startOfDayDateTime.toDate().getTime())),
						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN_EQUAL_TO,
								new Timestamp(endOfDayDateTime.toDate()
										.getTime()))), EntityOperator.AND);
		List<GenericValue> fixedDepositContractEarningELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			fixedDepositContractEarningELI = delegator.findList(
					"FixedDepositContractEarning", earningConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((fixedDepositContractEarningELI == null)
				|| (fixedDepositContractEarningELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN Today Earning not added, am going to add it !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG Today earning already added Nothing to do ");
		}

		return true;
	}

	public static String scheduleInterestServices(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory())
				.createLocalDispatcher("interestcalculations", delegator);
		
		// HttpSession session = request.getSession();
		// Map<String, String> userLogin = (HashMap)
		// session.getAttribute("userLogin");
		// userLogin.get("userLoginId")
		

		Map<String, String> context = UtilMisc.toMap("message",
				"Interest Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			// result = dispatcher.runSync("calculateInterestEarned", context);
			// dispatcher.schedule("calculateInterestEarned", startTime,
			// context);
			int frequency = RecurrenceRule.SECONDLY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule("calculateInterestEarned", context, startTime,
					frequency, interval, count);
		} catch (GenericServiceException e) {
			e.printStackTrace();
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

	/***
	 * Calculate Savings Type Interest
	 * 
	 * Get Last Month - start and end dates Get Savings Contracts Check if
	 * earnings have been calculated already For each Contract, get the least
	 * amount in the accounts for the month and calculate interest on it if it
	 * is greater than the minimum interest earning amount.
	 * 
	 * 
	 * */
	public static void processSavingsTypeInterestContracts() {
		List<Long> listSavingContractIds = getSavingContractId();

		for (Long savingsTypeContractId : listSavingContractIds) {
			addEarningsUpToLastMonth(savingsTypeContractId);
		}
	}

	/***
	 * Compute and Add Earnings for this Contract
	 * */
	private static void addEarningsUpToLastMonth(Long savingsTypeContractId) {
		log.info("CCCCCCCCCCCCC Computing Earnings for "
				+ savingsTypeContractId);
		Timestamp earningStartDate = getEarningStartDate(savingsTypeContractId);
		Timestamp earningEndDate = getEarningEndDate(savingsTypeContractId);
		log.info("DDDDDDDD the dates will run from " + earningStartDate
				+ " to " + earningEndDate);
		List<EarningPeriod> listEarningPeriod = getEarningPeriods(
				earningStartDate, earningEndDate);

		log.info("PPPPPPPPPPPP The periods will be PPPPPPPPPPPPPPP");
		for (EarningPeriod earningPeriod : listEarningPeriod) {
			log.info("Start :::: " + earningPeriod.getStartDate() + " To ::: "
					+ earningPeriod.getEndDate());
			log.info("------- Will be computing Earning for this period ---------");

			if (!existingsSavingsEarning(earningPeriod, savingsTypeContractId)) {
				log.info(" WWWWWWWWW Will now compute earnings WWWW");
				computeEarningBetweenDates(earningPeriod, savingsTypeContractId);
			}
		}
	}

	private static boolean existingsSavingsEarning(EarningPeriod earningPeriod,
			Long savingsTypeContractId) {
		// TODO Auto-generated method stub
		EntityConditionList<EntityExpr> savingsContractEarningConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"savingsTypeContractId", EntityOperator.EQUALS,
						savingsTypeContractId), EntityCondition.makeCondition(
						"startDate", EntityOperator.GREATER_THAN_EQUAL_TO,
						earningPeriod.getStartDate()), EntityCondition
						.makeCondition("endDate",
								EntityOperator.LESS_THAN_EQUAL_TO,
								earningPeriod.getEndDate())),
						EntityOperator.AND);
		List<GenericValue> savingsTypeContractEarningELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			savingsTypeContractEarningELI = delegator.findList(
					"SavingsTypeContractEarning",
					savingsContractEarningConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((savingsTypeContractEarningELI == null)
				|| (savingsTypeContractEarningELI.size() <= 0)) {
			log.info("NNNNNNNNNNNNNNNN Month Saving Earning not added, am going to add it !!!!!!!!!!!!! ");
			return false;
		} else {
			log.info("GGGGGGGGGGGGGGG Month Saving earning already added Nothing to do ");
		}

		return true;
	}

	/****
	 * @author Japheth Odonya @when Nov 11, 2014 6:51:13 PM
	 * 
	 *         The really computation of Earnings for the Savings Type Accounts
	 *         Happens Here
	 * */
	private static void computeEarningBetweenDates(EarningPeriod earningPeriod,
			Long savingsTypeContractId) {
		BigDecimal bdMinimumInterestAmount = getProductMinimumInterestAmount(savingsTypeContractId);
		BigDecimal bdBaseAmount = getBaseAmount(earningPeriod,
				savingsTypeContractId);

		/***
		 * skip if amount is less than minimum interest earning amount
		 * */
		if ((bdMinimumInterestAmount != null)
				&& (bdBaseAmount.compareTo(bdMinimumInterestAmount) != -1)) {
			log.info("SSSSSSSSSSSSSSSSSS Computing Interest Now !!! The Balance Amount is more than the Minimum Interest Earning Amount");
			Timestamp dateEarned = earningPeriod.getEndDate();
			BigDecimal bdInterestRatePerAnnum = getInterestRatePerAnnum(savingsTypeContractId);

			BigDecimal earnedAmount = bdBaseAmount
					.multiply(bdInterestRatePerAnnum.divide(new BigDecimal(12),
							6, RoundingMode.HALF_UP));
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			Long savingsTypeContractEarningId = delegator
					.getNextSeqIdLong("SavingsTypeContractEarning");
			// Save the Earning
			GenericValue savingsTypeContractEarning = null;
			savingsTypeContractEarning = delegator.makeValue(
					"SavingsTypeContractEarning", UtilMisc.toMap(
							"savingsTypeContractEarningId",
							savingsTypeContractEarningId,
							"savingsTypeContractId", savingsTypeContractId,
							"isActive", "Y", "createdBy", "admin", "startDate",
							earningPeriod.getStartDate(), "endDate",
							earningPeriod.getEndDate(), "dateEarned",
							dateEarned, "baseAmount", bdBaseAmount,
							"earnedAmount", earnedAmount

					));
			try {
				delegator.createOrStore(savingsTypeContractEarning);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		} else {
			log.info("SSSSSSSSSSSS Skipped computing Earning, the balance must have been less than the minimum interest earning amount");
		}
	}

	private static BigDecimal getInterestRatePerAnnum(Long savingsTypeContractId) {
		// TODO Auto-generated method stub
		GenericValue savingsTypeContract = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			savingsTypeContract = delegator.findOne("SavingsTypeContract",
					UtilMisc.toMap("savingsTypeContractId",
							savingsTypeContractId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return savingsTypeContract.getBigDecimal("interestRatePA").divide(
				new BigDecimal(100), 6, RoundingMode.HALF_UP);
	}

	private static BigDecimal getBaseAmount(EarningPeriod earningPeriod,
			Long savingsTypeContractId) {
		Long memberAccountId = getMemberAccountId(savingsTypeContractId);

		BigDecimal bdBalanceAmount = AccHolderTransactionServices
				.getTotalBalance(String.valueOf(memberAccountId),
						earningPeriod.getStartDate());
		// getAccountBalanceBeforeStart(memberAccountId,
		// earningPeriod.getStartDate() );
		List<GenericValue> listTransactionsInPeriod = getTransactionInPeriod(
				memberAccountId, earningPeriod);
		BigDecimal bdMovingTotal = bdBalanceAmount;
		// Keep the least value
		for (GenericValue genericValue : listTransactionsInPeriod) {
			if (genericValue.getString("increaseDecrease").equals("I")) {
				// Increment the Totals
				bdMovingTotal = bdMovingTotal.add(genericValue
						.getBigDecimal("transactionAmount"));
			} else {
				// Decrement the Totals
				bdMovingTotal = bdMovingTotal.subtract(genericValue
						.getBigDecimal("transactionAmount"));
				// bdBalanceAmount = bdMovingTotal;
			}

			if (bdMovingTotal.compareTo(bdBalanceAmount) == -1) {
				bdBalanceAmount = bdMovingTotal;
			}
		}

		return bdBalanceAmount;
	}

	private static List<GenericValue> getTransactionInPeriod(
			Long memberAccountId, EarningPeriod earningPeriod) {
		List<GenericValue> accountTransactionELI = null;
		List<String> listOrderBy = new ArrayList<String>();
		listOrderBy.add("accountTransactionId");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
						earningPeriod.getStartDate()), EntityCondition
						.makeCondition("createdStamp",
								EntityOperator.LESS_THAN_EQUAL_TO,
								earningPeriod.getEndDate())),
						EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, listOrderBy, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		log.info("Number of transactions is  ----------- "
				+ accountTransactionELI.size() + " Records !!!");

		return accountTransactionELI;
	}

	private static Long getMemberAccountId(Long savingsTypeContractId) {
		GenericValue savingsTypeContract = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			savingsTypeContract = delegator.findOne("SavingsTypeContract",
					UtilMisc.toMap("savingsTypeContractId",
							savingsTypeContractId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return savingsTypeContract.getLong("memberAccountId");
	}

	private static BigDecimal getProductMinimumInterestAmount(
			Long savingsTypeContractId) {

		GenericValue savingsTypeContract = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			savingsTypeContract = delegator.findOne("SavingsTypeContract",
					UtilMisc.toMap("savingsTypeContractId",
							savingsTypeContractId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long memberAccountId = savingsTypeContract.getLong("memberAccountId");

		GenericValue memberAccount = null;
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		Long accountProductId = memberAccount.getLong("accountProductId");

		GenericValue accountProduct = null;
		try {
			accountProduct = delegator
					.findOne("AccountProduct", UtilMisc.toMap(
							"accountProductId", accountProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountProduct.getBigDecimal("minInterestEarningAmt");
	}

	private static List<EarningPeriod> getEarningPeriods(
			Timestamp earningStartDate, Timestamp earningEndDate) {

		List<EarningPeriod> listEarningPeriod = new ArrayList<EarningPeriod>();
		EarningPeriod period = null;
		while (earningStartDate.before(earningEndDate)) {
			period = new EarningPeriod();
			period.setStartDate(earningStartDate);
			LocalDateTime localEndDate = new LocalDateTime(earningStartDate);
			localEndDate = localEndDate.plusMonths(1);
			localEndDate = localEndDate.withTime(0, 0, 0, 0).minusSeconds(1);
			period.setEndDate(new Timestamp(localEndDate.toDate().getTime()));

			localEndDate = localEndDate.plusSeconds(1);
			earningStartDate = new Timestamp(localEndDate.toDate().getTime());

			listEarningPeriod.add(period);
		}
		return listEarningPeriod;
	}

	private static Timestamp getEarningEndDate(Long savingsTypeContractId) {
		LocalDateTime localEndDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		localEndDate = localEndDate.minusMonths(1);
		localEndDate = localEndDate.withDayOfMonth(localEndDate.dayOfMonth()
				.getMaximumValue());
		localEndDate = localEndDate.plusDays(1).withTime(0, 0, 0, 0);
		localEndDate = localEndDate.minusSeconds(1);
		return new Timestamp(localEndDate.toDate().getTime());
	}

	private static Timestamp getEarningStartDate(Long savingsTypeContractId) {
		// TODO Auto-generated method stub
		GenericValue savingsTypeContract = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			savingsTypeContract = delegator.findOne("SavingsTypeContract",
					UtilMisc.toMap("savingsTypeContractId",
							savingsTypeContractId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (savingsTypeContract.getString("priorTransactions").equals("YES")) {
			return savingsTypeContract.getTimestamp("startDate");
		} else {
			LocalDateTime localStartDate = new LocalDateTime(
					savingsTypeContract.getTimestamp("startDate"));
			localStartDate = localStartDate.plusMonths(1).withDayOfMonth(1);
			return new Timestamp(localStartDate.toDate().getTime());
		}
	}

	private static List<Long> getSavingContractId() {
		List<Long> listSavingsContractId = new ArrayList<Long>();
		List<GenericValue> savingsTypeContractELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			savingsTypeContractELI = delegator.findList("SavingsTypeContract",
					null, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : savingsTypeContractELI) {
			listSavingsContractId.add(genericValue
					.getLong("savingsTypeContractId"));
		}

		savingsTypeContractELI = null;

		return listSavingsContractId;
	}

}
