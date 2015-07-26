package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.ofbiz.accountholdertransactions.model.ATMTransaction;
import org.ofbiz.accountholdertransactions.model.DeductionItem;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.treasurymanagement.TreasuryUtility;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class AccHolderTransactionServices {
	public static String MEMBER_DEPOSIT_CODE = "901";
	public static String SHARE_CAPITAL_CODE = "902";
	public static String SAVINGS_ACCOUNT_CODE = "999";
	public static Long CHEQUEWITHDRAWALID = 10020L;
	public static Long BANKERSWITHDRAWALID = 10000L;
	public static String HQBRANCH = "Company";
	private static Long ONEHUNDRED = 100L;

	//public static String WITHDRAWALOK = "OK";

	private static Logger log = Logger
			.getLogger(AccHolderTransactionServices.class);

	public static String getBranches(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String bankDetailsId = (String) request.getParameter("bankDetailsId");

		List<GenericValue> branchesELI = null;
		try {
			branchesELI = delegator.findList("BankBranch", EntityCondition
					.makeCondition("bankDetailsId", bankDetailsId), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Add Branches to a list
		if (branchesELI == null) {
			result.put("", "No Braches");
		}

		for (GenericValue genericValue : branchesELI) {
			result.put(genericValue.get("bankBranchId").toString(),
					genericValue.get("branchName"));
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

	/****
	 * Get Member Accounts Given a Member
	 * */
	public static String getMemberAccounts(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		List<GenericValue> memberAccountELI = null;
		partyId = partyId.replaceAll(",", "");
		try {
			memberAccountELI = delegator.findList(
					"MemberAccount",
					EntityCondition.makeCondition("partyId",
							Long.valueOf(partyId)), null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (memberAccountELI == null) {
			result.put("", "No Member Accounts");
		}
		String accountDetails;
		for (GenericValue genericValue : memberAccountELI) {
			accountDetails = genericValue.get("accountNo").toString() + " - "
					+ genericValue.get("accountName").toString();
			result.put(genericValue.get("memberAccountId").toString(),
					new String(accountDetails));
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

	public static List<Long> getMemberAccountIds(Long partyId) {
		List<Long> listMemberAccountId = new ArrayList<Long>();

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberAccountELI = null;
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (memberAccountELI == null) {
			return listMemberAccountId;
		}
		// String accountDetails;
		for (GenericValue genericValue : memberAccountELI) {
			listMemberAccountId.add(genericValue.getLong("memberAccountId"));
		}

		return listMemberAccountId;
	}

	/****
	 * Get Account Total Balance Total Opening Account + Total Deposits - Total
	 * Withdrawals
	 * */
	public static String getAccountTotalBalance(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String memberAccountId = (String) request
				.getParameter("memberAccountId");
		log.info(" ######### The Member Account is #########" + memberAccountId);
		memberAccountId = memberAccountId.replaceAll(",", "");

		BigDecimal bdAvailableAmount = getTotalSavings(memberAccountId,
				delegator).subtract(
				getMinimumBalance(Long.valueOf(memberAccountId)));

		BigDecimal bdBookBalanceAmount = getBookBalance(memberAccountId,
				delegator);

		result.put("availableAmount", bdAvailableAmount);
		result.put("bookBalanceAmount", bdBookBalanceAmount);

		result.put("amountInSource", bdAvailableAmount);
		result.put("amountInDestination", bdAvailableAmount);

		log.info(" LOOOOOOOOOOOOOOOOOOOOOOks like work is going on !!!! ");

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
				e1.printStackTrace();
			}
		}
		return json;
	}

	/***
	 * @author jodonya Calculates the Opening Balance
	 * */
	public static BigDecimal calculateOpeningBalance(String memberAccountId,
			Delegator delegator) {
		List<GenericValue> openingBalanceELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		try {
			openingBalanceELI = delegator.findList(
					"MemberAccountDetails",
					EntityCondition.makeCondition("memberAccountId",
							Long.valueOf(memberAccountId)), null, null, null,
					false);
			log.info(" ######### This member has no Opening Balance #########");
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			log.info("BBBBBBBBBBBBBBBB BOOOOOOOM!!!!!!!!!!!");
		}

		if (openingBalanceELI == null) {
			// result.put("", "No Member Accounts");
			log.info(" ######### This member has no Opening Balance #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : openingBalanceELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("savingsOpeningBalance"));

		}
		return bdBalance;
	}

	public static BigDecimal calculateOpeningBalance(Long memberAccountId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		return calculateOpeningBalance(memberAccountId.toString(), delegator);
	}

	private static BigDecimal calculateTotalCashDeposits(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> cashDepositELI = null;

		// Conditions
		// EntityConditionList<EntityCondition> transactionConditions =
		// EntityCondition.makeCond
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT")),
						EntityOperator.AND);

		try {
			// cashDepositELI = delegator.findList("AccountTransaction",
			// EntityCondition.makeCondition("memberAccountId",
			// memberAccountId), null, null, null, false);

			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (cashDepositELI == null) {
			// result.put("", "No Member Accounts");
			log.info(" ######### This member has Cash Deposit #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalIncreaseDecrease(
			String memberAccountId, Delegator delegator, String increaseDecrease) {
		List<GenericValue> cashDepositELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("increaseDecrease",
								EntityOperator.EQUALS, increaseDecrease)),
						EntityOperator.AND);

		try {
			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		BigDecimal bdTransactionAmount = null;
		log.info("Got  ----------- " + cashDepositELI.size() + " Records !!!");
		for (GenericValue genericValue : cashDepositELI) {
			bdTransactionAmount = genericValue
					.getBigDecimal("transactionAmount");
			if (bdTransactionAmount != null) {
				bdBalance = bdBalance.add(bdTransactionAmount);
			}
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalCashWithdrawals(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> cashWithdrawalELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL")),
						EntityOperator.AND);

		try {
			cashWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (cashWithdrawalELI == null) {
			log.info(" ######### This member has Cash Withdrawal #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/**
	 * Cheque Deposit
	 * */
	private static BigDecimal calculateTotalChequeDeposits(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> chequeDepositELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CHEQUEDEPOSIT")),
						EntityOperator.AND);

		try {
			chequeDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (chequeDepositELI == null) {
			log.info(" ######### This member has Cheque Deposit #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : chequeDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/***
	 * Cheque Withdrawal
	 ***/
	private static BigDecimal calculateTotalChequeWithdrawals(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> chequeWithdrawalELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CHEQUEWITHDRAWAL")),
						EntityOperator.AND);

		try {
			chequeWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (chequeWithdrawalELI == null) {
			log.info(" ######### This member has no Cheque Withdrawal #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : chequeWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/***
	 * Cleared Cheque Deposit
	 ***/
	private static BigDecimal calculateTotalClearedChequeDeposits(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> chequeDepositELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CHEQUEDEPOSIT"),
						EntityCondition.makeCondition("clearDate",
								EntityOperator.LESS_THAN_EQUAL_TO,
								new Timestamp(Calendar.getInstance()
										.getTimeInMillis()))),
						EntityOperator.AND);

		try {
			chequeDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (chequeDepositELI == null) {
			log.info(" ######### This member has Cheque Deposit #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : chequeDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/***
	 * The total savings = Opening Balance + Total Cash Deposits + Total Cleared
	 * Cheques - Total Cash Withdrawals - Total Cleared C
	 **/
	public static BigDecimal getTotalSavings(String memberAccountId,
			Delegator delegator) {
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// // Get Total Deposits
		return (getAvailableBalanceVer2(memberAccountId, delegator)
				.add(bdOpeningBalance));
	}

	/***
	 * Book Balance = Total Savings + Total Cleared Chequered - Total Cheques
	 * Deposited
	 **/
	public static BigDecimal getBookBalance(String memberAccountId,
			Delegator delegator) {

		// BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		// BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		// BigDecimal bdTotalSavings = getTotalSavings(memberAccountId,
		// delegator);
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		//
		// bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
		// delegator);
		// bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
		// memberAccountId, delegator);
		//
		// // return
		// // bdOpeningBalance.add(bdTotalCashDeposit).add(bdTotalChequeDeposit)
		// //
		// .subtract(bdTotalCashWithdrawal).subtract(bdTotalChequeWithdrawal);
		// return bdTotalSavings.add(bdTotalChequeDeposit).subtract(
		// bdTotalChequeDepositCleared);
		return (getBookBalanceVer2(memberAccountId, delegator)
				.add(bdOpeningBalance));
	}

	public static BigDecimal getBookBalanceNow(String memberAccountId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		return getBookBalanceVer3(memberAccountId, delegator);
	}

	public static BigDecimal getBookBalanceVer3(String memberAccountId,
			Delegator delegator) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;

		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "I");
		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "D");
		return bdTotalIncrease.subtract(bdTotalDecrease).add(
				calculateOpeningBalance(memberAccountId,
						DelegatorFactoryImpl.getDelegator(null)));
	}

	public static BigDecimal getBookBalanceVer2(String memberAccountId,
			Delegator delegator) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;

		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "I");
		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "D");
		return bdTotalIncrease.subtract(bdTotalDecrease);
	}

	public static BigDecimal getAvailableBalanceVer2(String memberAccountId,
			Delegator delegator) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;
		BigDecimal bdTotalAvailable = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "I");
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is " + bdTotalIncrease);

		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is " + bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				delegator);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "
				+ bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, delegator);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "
				+ bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);

		memberAccountId = memberAccountId.replaceAll(",", "");
		Long lmemberAccountId = Long.valueOf(memberAccountId);
		BigDecimal bdRetailedSavings = BigDecimal.ZERO;
		bdRetailedSavings = getRetainedSavings(lmemberAccountId);
		bdTotalAvailable = bdTotalAvailable.subtract(bdRetailedSavings);

		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is " + bdTotalAvailable);
		// return
		// bdTotalIncrease.add(bdTotalChequeDepositCleared).subtract(bdTotalDecrease).subtract(bdTotalChequeDeposit);
		return bdTotalAvailable;
	}

	/**
	 * Calculate End Date given start date and number of days
	 * **/
	public static Date calculateEndWorkingDay(Date startDate, int noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 1;
		while (count < noOfDays) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}

		return localDateEndDate.toDate();
	}

	/****
	 * @author Japheth Odonya @when Aug 9, 2014 3:29:16 PM Calculate Working
	 *         Days between two dates - startDate and endDate
	 * */
	public static int calculateWorkingDaysBetweenDates(Date startDate,
			Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return daysCount;
	}

	public static Date calculateEndWorkingDayCheque(Date startDate, int noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 0;
		while (count < noOfDays) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}

		return localDateEndDate.toDate();
	}

	/***
	 * @author Japheth Odonya @when Aug 10, 2014 4:01:22 PM Calculate Cheque
	 *         Clearance
	 * */
	// public static Map<String, Object>
	// calculateChequeClearance(DispatchContext ctx,
	// Map<String, ? extends Object> context){
	public static Timestamp calculateChequeClearance(
			GenericValue accountTransaction) {

		Map<String, Object> result = FastMap.newInstance();
		String accountTransactionId = accountTransaction
				.getString("accountTransactionId");// (String)context.get("accountTransactionId");
		log.info("What we got is ############ " + accountTransactionId);

		Delegator delegator;
		// delegator = D
		// ctx.getDelegator();

		// delegator = DelegatorFactoryImpl.getDelegator("delegator");
		delegator = accountTransaction.getDelegator();
		// GenericValue accountTransaction = null;
		accountTransactionId = accountTransactionId.replaceAll(",", "");
		try {
			accountTransaction = delegator
					.findOne("AccountTransaction", UtilMisc.toMap(
							"accountTransactionId", accountTransactionId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get Current Date
		Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());
		// Get Clearance Duration
		int clearDuration = accountTransaction.getLong("clearDuration")
				.intValue();
		// Calculate Cheque Clear Date
		Date clearDate = calculateEndWorkingDayCheque(currentDate,
				clearDuration);

		// loanApplication.set("monthlyRepayment", paymentAmount);
		accountTransaction.set("clearDate", clearDate);
		log.info("##### End Date is ######## " + clearDate);
		log.info("##### ID is  ######## "
				+ accountTransaction.getString("accountTransactionId"));
		log.info("##### transactionAmount is  ######## "
				+ accountTransaction.getString("transactionAmount"));

		// try {
		// delegator.removeValue(accountTransaction);
		// } catch (GenericEntityException e1) {
		// e1.printStackTrace();
		// }
		//
		// try {
		// delegator.createOrStore(accountTransaction);
		// } catch (GenericEntityException e) {
		// e.printStackTrace();
		// }
		result.put("clearDate", clearDate);
		return new Timestamp(clearDate.getTime());
	}

	/***
	 * @author Japheth Odonya @when Aug 24, 2014 7:57:03 PM Creating a Cheque
	 *         Deposit Accounting Transaction
	 * */
	public static String createChequeTransaction(
			GenericValue accountTransaction, Map<String, String> userLogin) {
		// Post the Cash Deposit to the Teller for the logged in user
		/***
		 * Dr to the Teller Account Cr to the Member Deposits
		 * 
		 * */
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		String glLedgerAccountId = null;
		// String tellerAccountId = null;
		String bankglAccountId = null;

		String commissionAccountId = null;
		String exciseDutyAccountId = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");

		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");

		// tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);
		bankglAccountId = getCashAccount(null, "CHEQUEDEPOSITACCOUNT");

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyId = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);

		// LoanUtilities.getE
		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(partyId);

		List<GenericValue> listPostEntity = new ArrayList<GenericValue>();
		log.info("#########3 Employee Branch ID " + employeeBranchId
				+ " Member Branch ID " + memberBranchId);
		Long sequence = 0l;

		// Post memberDeposit
		// This is a withdrawal so we debit the member deposits as
		// specified on the product
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "D", sequence.toString(),
				memberBranchId));

		// Post for Teller
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(bankglAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listPostEntity);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String transactionType = "";

		Long chequeTypeId = accountTransaction.getLong("chequeTypeId");

		if (chequeTypeId.equals(BANKERSWITHDRAWALID)) {
			transactionType = "BANKERSWITHDRAWAL";
		}

		if (chequeTypeId.equals(CHEQUEWITHDRAWALID)) {
			transactionType = "CHEQUEWITHDRAWAL";
		}

		// Update account transactions to reflect the ID in postings
		accountTransaction.set("acctgTransId", acctgTransId);
		accountTransaction.set("transactionType", transactionType);
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addChargesToTransaction(accountTransaction, userLogin, transactionType,
				employeeBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId);

		// tt
		// Add all the charges (C)

		return accountTransaction.getString("accountTransactionParentId");
	}

	// public static String createChequeTransaction(
	// GenericValue accountTransaction, Map<String, String> userLogin) {
	// String acctgTransType = "MEMBER_DEPOSIT";
	//
	// // Create the Account Trans Record
	// String acctgTransId = createAccountingTransaction(accountTransaction,
	// acctgTransType, userLogin);
	// // Do the posting
	// Delegator delegator = accountTransaction.getDelegator();
	// BigDecimal transactionAmount = accountTransaction
	// .getBigDecimal("transactionAmount");
	// String partyId = (String) userLogin.get("partyId");
	//
	// // Debit Member Deposit
	//
	//
	// String memberDepositAccountId = getMemberDepositAccount(
	// accountTransaction, "MEMBERTRANSACTIONACCOUNT");
	// String postingType = "D";
	// String entrySequenceId = "1";
	// try {
	// TransactionUtil.begin();
	// } catch (GenericTransactionException e) {
	// e.printStackTrace();
	// }
	// postTransactionEntry(delegator, transactionAmount, partyId,
	// memberDepositAccountId, postingType, acctgTransId,
	// acctgTransType, entrySequenceId);
	// try {
	// TransactionUtil.commit();
	// } catch (GenericTransactionException e) {
	// e.printStackTrace();
	// }
	// // Credit Cash Account
	// String cashAccountId = getCashAccount(accountTransaction,
	// "MEMBERTRANSACTIONACCOUNT");
	// postingType = "C";
	// entrySequenceId = "00002";
	// try {
	// TransactionUtil.begin();
	// } catch (GenericTransactionException e) {
	// e.printStackTrace();
	// }
	// postTransactionEntry(delegator, transactionAmount, partyId,
	// cashAccountId, postingType, acctgTransId, acctgTransType,
	// entrySequenceId);
	// try {
	// TransactionUtil.commit();
	// } catch (GenericTransactionException e) {
	// e.printStackTrace();
	// }

	// return "POSTED";
	// }

	public static String getCashAccount(GenericValue accountTransaction,
			String setUpId) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup",
					UtilMisc.toMap("accountHolderTransactionSetupId", setUpId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Could not get member deposit account ");
		}

		String cashAccountId = "";
		if (accountHolderTransactionSetup != null) {
			cashAccountId = accountHolderTransactionSetup
					.getString("cashAccountId");
		} else {
			log.error("######## Cannot get Cash Account ");
		}
		return cashAccountId;
	}

	public static String getMemberDepositAccount(
			GenericValue accountTransaction, String setUpId) {

		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup",
					UtilMisc.toMap("accountHolderTransactionSetupId", setUpId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Could not get member deposit account ");
		}

		String memberDepositAccountId = "";
		if (accountHolderTransactionSetup != null) {
			memberDepositAccountId = accountHolderTransactionSetup
					.getString("memberDepositAccId");
		} else {
			log.error("######## Cannot get Member Deposit Account ");
		}
		return memberDepositAccountId;
	}

	/**
	 * AcctgTransEntry
	 * **/
	public static void postTransactionEntry(Delegator delegator,
			BigDecimal bdLoanAmount, String employeeBranchId,
			String memberBranchId, String loanReceivableAccount,
			String postingType, String acctgTransId, String acctgTransType,
			String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId",
						memberBranchId,
						"glAccountTypeId",
						acctgTransType,
						"glAccountId",
						loanReceivableAccount,

						// "organizationPartyId", "Company", "amount",
						"organizationPartyId", employeeBranchId, "amount",
						bdLoanAmount, "currencyUomId", "KES", "origAmount",
						bdLoanAmount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not post a Transaction");
		}
	}

	/***
	 * @author Japheth Odonya @when Aug 24, 2014 8:03:52 PM Create Account Trans
	 *         Type AcctgTrans
	 * */
	public static String createAccountingTransaction(
			GenericValue accountTransaction, String acctgTransType,
			Map<String, String> userLogin) {

		GenericValue acctgTrans;
		String acctgTransId;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// accountTransaction.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

		String partyId = (String) userLogin.get("partyId");
		String createdBy = (String) userLogin.get("userLoginId");

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

	/**
	 * @author Japheth Odonya @when Aug 25, 2014 7:30:31 PM
	 * 
	 *         Add Charges to Transaction Like Cash Withdrawal or Cheque
	 *         Withdrawal
	 * 
	 * **/
	public static String addChargesToTransaction(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence,
			String commissionAccountId, String exciseDutyAccountId) {

		// Get the Product by first accessing the MemberAccount
		String accountProductId = getAccountProduct(accountTransaction);

		// Get the Charges for the Product
		List<GenericValue> accountProductChargeELI = null;
		accountProductChargeELI = getAccountProductCharges(accountTransaction,
				accountProductId, transactionType);
		log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "
				+ accountProductChargeELI.size());
		String chargeAccountId = commissionAccountId;
		// Create a transaction in Account Transaction for each of the Charges
		sequence = sequence + 1;
		for (GenericValue accountProductCharge : accountProductChargeELI) {

			if (accountProductCharge.getLong("parentChargeId") == null) {
				chargeAccountId = commissionAccountId;
			} else {
				chargeAccountId = exciseDutyAccountId;
			}

			// SEQUENCENO = sequence + 1;
			sequence = addChargeVer2(accountProductCharge, accountTransaction,
					userLogin, transactionType, employeeBranchId,
					memberBranchId, acctgTransId, glLedgerAccountId, sequence,
					chargeAccountId);

			// sequence = sequence + 1;
		}
		// Create an Account Transaction for each of the Charges

		return "";
	}
	
	
	/**
	 *@author Japheth Odonya
	 *
	 * **/
	public static String addChargesToTransactionATM(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence,
			String commissionAccountId, String exciseDutyAccountId, String SystemTrace) {

		// Get the Product by first accessing the MemberAccount
		String accountProductId = getAccountProduct(accountTransaction);

		// Get the Charges for the Product
		List<GenericValue> accountProductChargeELI = null;
		accountProductChargeELI = getAccountProductCharges(accountTransaction,
				accountProductId, transactionType);
		log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "
				+ accountProductChargeELI.size());
		String chargeAccountId = commissionAccountId;
		// Create a transaction in Account Transaction for each of the Charges
		sequence = sequence + 1;
		for (GenericValue accountProductCharge : accountProductChargeELI) {

			if (accountProductCharge.getLong("parentChargeId") == null) {
				chargeAccountId = commissionAccountId;
			} else {
				chargeAccountId = exciseDutyAccountId;
			}
			
			// SEQUENCENO = sequence + 1;
			sequence = addChargeVer2ATM(accountProductCharge, accountTransaction,
					userLogin, transactionType, employeeBranchId,
					memberBranchId, acctgTransId, glLedgerAccountId, sequence,
					chargeAccountId, SystemTrace);

			// sequence = sequence + 1;
		}
		// Create an Account Transaction for each of the Charges

		return "";
	}
	
	
	/***
	 *ATM Reversal 
	 **/
	public static String addChargesToTransactionATMReversal(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence,
			String commissionAccountId, String exciseDutyAccountId) {

		// Get the Product by first accessing the MemberAccount
		String accountProductId = getAccountProduct(accountTransaction);

		// Get the Charges for the Product
		List<GenericValue> accountProductChargeELI = null;
		//Set 
		transactionType = "ATMWITHDRAWAL";
		accountProductChargeELI = getAccountProductCharges(accountTransaction,
				accountProductId, transactionType);
		log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "
				+ accountProductChargeELI.size());
		String chargeAccountId = commissionAccountId;
		// Create a transaction in Account Transaction for each of the Charges
		sequence = sequence + 1;
		for (GenericValue accountProductCharge : accountProductChargeELI) {
			
			if (accountProductCharge.getLong("parentChargeId") == null) {
				chargeAccountId = commissionAccountId;
			} else {
				chargeAccountId = exciseDutyAccountId;
			}

			// SEQUENCENO = sequence + 1;
			transactionType = "ATMWITHDRAWALREVERSAL";
			sequence = addChargeVer2Withdrawal(accountProductCharge, accountTransaction,
					userLogin, transactionType, employeeBranchId,
					memberBranchId, acctgTransId, glLedgerAccountId, sequence,
					chargeAccountId);

			// sequence = sequence + 1;
		}
		// Create an Account Transaction for each of the Charges

		return "";
	}

	// public static String addChargesToTransaction(
	// GenericValue accountTransaction, Map<String, String> userLogin,
	// String transactionType, String acctgTransId, String employeeBranchId,
	// String memberBranchId, Long sequence) {
	//
	// // Get the Product by first accessing the MemberAccount
	// String accountProductId = getAccountProduct(accountTransaction);
	//
	// // Get the Charges for the Product
	// List<GenericValue> accountProductChargeELI = null;
	// accountProductChargeELI = getAccountProductCharges(accountTransaction,
	// accountProductId, transactionType);
	// log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "
	// + accountProductChargeELI.size());
	// // Create a transaction in Account Transaction for each of the Charges
	// for (GenericValue accountProductCharge : accountProductChargeELI) {
	// addChargeVer2(accountProductCharge, accountTransaction, userLogin,
	// transactionType, employeeBranchId, memberBranchId, acctgTransId,
	// sequence);
	// }
	// // Create an Account Transaction for each of the Charges
	//
	// return "";
	// }

	/*****
	 * @author Japheth Odonya @when Aug 25, 2014 10:26:34 PM Add Charge to
	 *         AccountTransaction Accounting (POST)
	 * **/
	private static Long addChargeVer2(GenericValue accountProductCharge,
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence, String chargeAccountId) {
		// Add Account Transaction
		BigDecimal bdChargeAmount;
		bdChargeAmount = getChargeAmount(accountProductCharge,
				accountTransaction);
		String chargeName = getChargeName(accountProductCharge);
		// = accountProductCharge.getBigDecimal("");
		String memberAccountId = String.valueOf(accountTransaction
				.getLong("memberAccountId"));
		String productChargeId = String.valueOf(accountProductCharge
				.getLong("productChargeId"));

		String accountTransactionParentId = accountTransaction
				.getString("accountTransactionParentId");
		createTransaction(accountTransaction, chargeName, userLogin,
				memberAccountId, bdChargeAmount, productChargeId,
				accountTransactionParentId, acctgTransId);

		// POST Charge
		String acctgTransType = "OTHER_INCOME";

		// Create the Account Trans Record
		// String acctgTransId = createAccountingTransaction(accountTransaction,
		// acctgTransType, userLogin);

		log.info("##########66666 Posting for " + chargeName);
		log.info("##########66666 Posting for " + chargeName);

		// Debit Member Deposits
		Delegator delegator = accountTransaction.getDelegator();
		// String partyId = accountTransaction.getString("partyId");
		// String memberDepositAccountId = getMemberDepositAccount(
		// accountTransaction, "MEMBERTRANSACTIONCHARGE");
		String postingType = "D";
		sequence = sequence + 1;
		String entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, glLedgerAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		// tttt
		// Credit Charge or Services
		// String chargeAccountId = getCashAccount(accountTransaction,
		// "MEMBERTRANSACTIONCHARGE");
		// commissionAccountId, exciseDutyAccountId
		postingType = "C";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, chargeAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		return sequence;
	}
	
	
	/****
	 * 
	 * */
	private static Long addChargeVer2ATM(GenericValue accountProductCharge,
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence, String chargeAccountId, String SystemTrace) {
		// Add Account Transaction
		BigDecimal bdChargeAmount;
		bdChargeAmount = getChargeAmount(accountProductCharge,
				accountTransaction);
		String chargeName = getChargeName(accountProductCharge);
		// = accountProductCharge.getBigDecimal("");
		String memberAccountId = String.valueOf(accountTransaction
				.getLong("memberAccountId"));
		String productChargeId = String.valueOf(accountProductCharge
				.getLong("productChargeId"));
		
		String accountTransactionParentId = accountTransaction
				.getString("accountTransactionParentId");
		createTransactionATM(accountTransaction, chargeName, userLogin,
				memberAccountId, bdChargeAmount, productChargeId,
				accountTransactionParentId, acctgTransId,SystemTrace);

		// POST Charge
		String acctgTransType = "OTHER_INCOME";

		// Create the Account Trans Record
		// String acctgTransId = createAccountingTransaction(accountTransaction,
		// acctgTransType, userLogin);

		log.info("##########66666 Posting for " + chargeName);
		log.info("##########66666 Posting for " + chargeName);

		// Debit Member Deposits
		Delegator delegator = accountTransaction.getDelegator();
		// String partyId = accountTransaction.getString("partyId");
		// String memberDepositAccountId = getMemberDepositAccount(
		// accountTransaction, "MEMBERTRANSACTIONCHARGE");
		String postingType = "D";
		sequence = sequence + 1;
		String entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, glLedgerAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		// tttt
		// Credit Charge or Services
		// String chargeAccountId = getCashAccount(accountTransaction,
		// "MEMBERTRANSACTIONCHARGE");
		// commissionAccountId, exciseDutyAccountId
		postingType = "C";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, chargeAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		return sequence;
	}
	
	/***
	 * Charge ATM Withdrawal
	 * */
	private static Long addChargeVer2Withdrawal(GenericValue accountProductCharge,
			GenericValue accountTransaction, Map<String, String> userLogin,
			String transactionType, String employeeBranchId,
			String memberBranchId, String acctgTransId,
			String glLedgerAccountId, Long sequence, String chargeAccountId) {
		// Add Account Transaction
		BigDecimal bdChargeAmount;
		bdChargeAmount = getChargeAmount(accountProductCharge,
				accountTransaction);
		String chargeName = getChargeName(accountProductCharge);
		// = accountProductCharge.getBigDecimal("");
		String memberAccountId = String.valueOf(accountTransaction
				.getLong("memberAccountId"));
		String productChargeId = String.valueOf(accountProductCharge
				.getLong("productChargeId"));

		String accountTransactionParentId = accountTransaction
				.getString("accountTransactionParentId");
		createTransaction(accountTransaction, chargeName, userLogin,
				memberAccountId, bdChargeAmount, productChargeId,
				accountTransactionParentId, acctgTransId);

		// POST Charge
		String acctgTransType = "OTHER_INCOME";

		// Create the Account Trans Record
		// String acctgTransId = createAccountingTransaction(accountTransaction,
		// acctgTransType, userLogin);

		log.info("##########66666 Posting for " + chargeName);
		log.info("##########66666 Posting for " + chargeName);

		// Debit Member Deposits
		Delegator delegator = accountTransaction.getDelegator();
		// String partyId = accountTransaction.getString("partyId");
		// String memberDepositAccountId = getMemberDepositAccount(
		// accountTransaction, "MEMBERTRANSACTIONCHARGE");
		String postingType = "C";
		sequence = sequence + 1;
		String entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, glLedgerAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		// tttt
		// Credit Charge or Services
		// String chargeAccountId = getCashAccount(accountTransaction,
		// "MEMBERTRANSACTIONCHARGE");
		// commissionAccountId, exciseDutyAccountId
		postingType = "D";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		postTransactionEntry(delegator, bdChargeAmount, employeeBranchId,
				memberBranchId, chargeAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);

		return sequence;
	}

	/***
	 * Get Product Charge Name - this will be recorded in the transaction
	 * */
	private static String getChargeName(GenericValue accountProductCharge) {
		String productChargeId = accountProductCharge
				.getString("productChargeId");

		Delegator delegator = accountProductCharge.getDelegator();
		GenericValue productCharge = null;
		productChargeId = productChargeId.replaceAll(",", "");
		try {
			productCharge = delegator.findOne(
					"ProductCharge",
					UtilMisc.toMap("productChargeId",
							Long.valueOf(productChargeId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get product charge ");
		}

		String name = "";
		if (productCharge != null) {
			name = productCharge.getString("name");
		} else {
			log.error("######## Cannot get product charge !! ");
		}
		return name;
	}

	private static BigDecimal getChargeAmount(
			GenericValue accountProductCharge, GenericValue accountTransaction) {
		String strFixed = accountProductCharge.getString("isFixed");
		BigDecimal bdChargeAmount;
		String isPercentageOfOtherCharge;
		BigDecimal bdTransactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		// Delegator delegator = accountProductCharge.getDelegator();
		if (strFixed.equals("Y")) {
			bdChargeAmount = accountProductCharge.getBigDecimal("fixedAmount");
		} else {
			// Its not fixed
			// It is a rate/percentage
			// Can either be the rate of parent charge or the rate of the
			// transaction
			isPercentageOfOtherCharge = accountProductCharge
					.getString("isPercentageOfOtherCharge");
			if ((isPercentageOfOtherCharge.equals("Y"))) {
				// Get the value of the charge
				GenericValue parentAccountCharge = getAccoutCharge(
						accountProductCharge.getString("parentChargeId"),
						accountProductCharge);
				BigDecimal bdParentAmount;
				if (parentAccountCharge.getString("isFixed").equals("Y")) {
					bdParentAmount = parentAccountCharge
							.getBigDecimal("fixedAmount");
				} else {
					bdParentAmount = parentAccountCharge
							.getBigDecimal("rateAmount")
							.setScale(6, RoundingMode.HALF_UP)
							.multiply(
									bdTransactionAmount.setScale(6,
											RoundingMode.HALF_UP))
							.divide(new BigDecimal(100), 6,
									RoundingMode.HALF_UP);
				}

				bdChargeAmount = bdParentAmount
						.setScale(6, RoundingMode.HALF_UP)
						.multiply(
								accountProductCharge
										.getBigDecimal("rateAmount").setScale(
												6, RoundingMode.HALF_UP))
						.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);

			} else {
				bdChargeAmount = accountProductCharge
						.getBigDecimal("rateAmount")
						.setScale(6, RoundingMode.HALF_UP)
						.multiply(
								bdTransactionAmount.setScale(6,
										RoundingMode.HALF_UP))
						.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			}
		}
		return bdChargeAmount;
	}

	private static GenericValue getAccoutCharge(String parentChargeId,
			GenericValue accountProductCharge) {
		Delegator delegator = accountProductCharge.getDelegator();
		List<GenericValue> accountProductChargeELI = null;
		parentChargeId = parentChargeId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductCharge.getLong("accountProductId")),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, accountProductCharge
										.getString("transactionType")),
						EntityCondition.makeCondition("productChargeId",
								EntityOperator.EQUALS,
								Long.valueOf(parentChargeId))),
						EntityOperator.AND);

		try {
			accountProductChargeELI = delegator.findList(
					"AccountProductCharge", accountChargeConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue parentAccountProductCharge = null;

		for (GenericValue genericValue : accountProductChargeELI) {
			parentAccountProductCharge = genericValue;
		}

		return parentAccountProductCharge;
	}

	/***
	 * Get Account Charge
	 * */
	private static List<GenericValue> getAccountProductCharges(
			GenericValue accountTransaction, String accountProductId,
			String transactionType) {

		// for the purpuse of getting charges, ATM withdrawal should just use
		// the CASHWITHDRAWAL charges
		if (transactionType.equals("ATMWITHDRAWAL")) {
			transactionType = "CASHWITHDRAWAL";
		}

		Delegator delegator = accountTransaction.getDelegator();
		accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						Long.valueOf(accountProductId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, transactionType)),
						EntityOperator.AND);
		List<GenericValue> accountProductChargeELI = null;
		try {
			accountProductChargeELI = delegator.findList(
					"AccountProductCharge", accountChargeConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if (accountProductChargeELI == null) {
			// result.put("", "No Member Accounts");
			log.info(" ######### The Account Has no Charges #########");
		}
		return accountProductChargeELI;
	}

	/***
	 * Get Account Product from MemberAccount with is in the AccountTransaction
	 * */
	private static String getAccountProduct(GenericValue accountTransaction) {

		String memberAccountId = accountTransaction
				.getString("memberAccountId");
		Delegator delegator = accountTransaction.getDelegator();
		memberAccountId = memberAccountId.replaceAll(",", "");
		GenericValue memberAccount = null;
		try {
			memberAccount = delegator.findOne(
					"MemberAccount",
					UtilMisc.toMap("memberAccountId",
							Long.valueOf(memberAccountId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get AccountProduct
		String accountProductId = memberAccount.getString("accountProductId");
		return accountProductId;
	}

	/****
	 * 
	 * */
	private static void createTransactionMsacco(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId, String partnerTransactionId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId, "partnerTransactionId",
						partnerTransactionId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/**
	 * Create a record in AccountTransaction
	 * */
	private static void createTransaction(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWALREVERSAL")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}
	
	//ATM TRansaction added SystemTrace
	private static void createTransactionATM(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId, String SystemTrace) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWALREVERSAL")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId, 
						"systemtrace", SystemTrace
						));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}


//	/cashWithdrawalATMReversal
	private static void createTransactionATMReversal(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWALREVERSAL")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}
	
	// acctgTransId
	/***
	 * Adding acctgTransId to createTransaction
	 * 
	 * **/
	private static void createTransaction(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId, Long accountProductId, Long loanApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("TRANSFERFROM")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANREPAYMENT")))

					|| ((transactionType != null) && (transactionType
							.equals("TOOTHERACCOUNTS")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))

					|| ((transactionType != null) && (transactionType
							.equals("TRANSFERTO")))

					|| ((transactionType != null) && (transactionType
							.equals("DEPOSITFROMSALARY")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId,

						"accountProductId", accountProductId,

						"loanApplicationId", loanApplicationId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/****
	 * @author Japheth Odonya @when Jun 11, 2015 11:47:08 PM Cash Transaction -
	 *         MPA Version 4
	 * 
	 * */
	private static void createTransactionVersion4(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("TRANSFERFROM")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("TRANSFERTO")))

					|| ((transactionType != null) && (transactionType
							.equals("FROMMEMBERWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	private static void createTransactionVersion2(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			Long memberAccountVoucherId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "memberAccountVoucherId",
						memberAccountVoucherId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/***
	 * createAccountTransactionVer2
	 * **/
	private static GenericValue createTransactionVer2(String transactionType,
			Map<String, String> userLogin, String memberAccountId,
			BigDecimal transactionAmount, String productChargeId,
			String accountTransactionParentId, String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("WITHDRAWALCOMMISSION")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))
					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))
					// DEPOSITFROMEXCESS

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("DEPOSITFROMEXCESS")))

			) {
				increaseDecrease = "I";
			}
		}

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		treasuryId = TreasuryUtility.getTellerTreasuryId(userLogin);

		// loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId));
		// try {
		// delegator.createOrStore(accountTransaction);
		// } catch (GenericEntityException e) {
		// e.printStackTrace();
		// log.error("Could not create Transaction");
		// }
		return accountTransaction;
	}

	/***
	 * Get Party ID give memberAccountId
	 * */
	private static String getMemberPartyId(String memberAccountId) {
		// TODO Auto-generated method stub
		memberAccountId = memberAccountId.replaceAll(",", "");
		Long lmemberAccountId = Long.valueOf(memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Get Member
		GenericValue memberAccount = null;
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", lmemberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String partyId = String.valueOf(memberAccount.getLong("partyId"));
		partyId = partyId.replaceAll(",", "");
		return partyId;
	}

	public static String getLoanApplicationDetails(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Loan Application Details";
		}

		BigDecimal loanBalanceAmt = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(Long
						.valueOf(loanApplicationId));
		if (loanApplication != null) {
			// loanNo
			// loanType
			// loanAmt

			result.put("loanNo", loanApplication.get("loanNo"));
			result.put("loanTypeId", loanApplication.get("loanProductId"));
			result.put("loanAmt", loanApplication.getBigDecimal("loanAmt"));
			result.put("loanBalanceAmt", loanBalanceAmt);

			/***
			 * loanNo loanTypeId totalLoanDue totalInterestDue totalInsuranceDue
			 * totalPrincipalDue transactionAmount
			 * 
			 * getTotalLoanDue(partyId) getTotalInterestDue(partyId)
			 * getTotalInsuranceDue(partyId) getTotalPrincipalDue(partyId)
			 * */
			BigDecimal totalLoanDue = LoanRepayments.getTotalLoanDue(
					loanApplication.getString("partyId"),
					loanApplication.getString("loanApplicationId"));
			result.put("totalLoanDue", totalLoanDue);
			result.put("transactionAmount", totalLoanDue);

			result.put("totalInterestDue", LoanRepayments.getTotalInterestDue(
					loanApplication.getString("partyId"),
					loanApplication.getString("loanApplicationId")));
			result.put("totalInsuranceDue", LoanRepayments
					.getTotalInsuranceDue(loanApplication.getString("partyId"),
							loanApplication.getString("loanApplicationId")));
			result.put("totalPrincipalDue", LoanRepayments
					.getTotalPrincipalDue(loanApplication.getString("partyId"),
							loanApplication.getString("loanApplicationId")));

			// result.put("selectedRepaymentPeriod",
			// saccoProduct.get("selectedRepaymentPeriod"));
		} else {
			System.out
					.println("######## Loan Application details not found #### ");
		}
		// return JSONBuilder.class.
		// JSONObject root = new JSONObject();

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

	public static String postStationTransaction(
			GenericValue stationAccountTransaction,
			Map<String, String> userLogin) {

		String acctgTransType = "STATION_DEPOSIT";

		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(
				stationAccountTransaction, acctgTransType, userLogin);
		// Do the posting
		Delegator delegator = stationAccountTransaction.getDelegator();
		BigDecimal transactionAmount = stationAccountTransaction
				.getBigDecimal("transactionAmount");
		// String partyId = (String) userLogin.get("partyId");

		// Get Member Branch
		// String branchId;
		// branchId = getBranch(partyId);
		Long branchId = stationAccountTransaction.getLong("branchId");

		// Debit Cash/Bank Account

		String memberDepositAccountId = getMemberDepositAccount(
				stationAccountTransaction, "STATIONACCOUNTPAYMENT");
		String cashAccountId = getCashAccount(stationAccountTransaction,
				"STATIONACCOUNTPAYMENT");

		// Check that the two accounts member deposits and cash deposit accounts
		// are mapped to
		// the branch -

		if ((memberDepositAccountId == null)
				|| (memberDepositAccountId.equals("")))
			return "stationdepositaccountnotset";

		if ((cashAccountId == null) || (cashAccountId.equals("")))
			return "stationdepositaccountnotset";

		if (!LoanUtilities.organizationAccountMapped(memberDepositAccountId,
				branchId.toString()))
			return "accountsnotmapped";

		if (!LoanUtilities.organizationAccountMapped(cashAccountId,
				branchId.toString()))
			return "accountsnotmapped";

		String postingType = "C";
		String entrySequenceId = "00001";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, branchId.toString(),
				branchId.toString(), memberDepositAccountId, postingType,
				acctgTransId, acctgTransType, entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		// Credit Station Deposit Account

		postingType = "D";
		entrySequenceId = "00002";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, branchId.toString(),
				branchId.toString(), cashAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

		// Update the station transaction with acctgTransId
		stationAccountTransaction.set("acctgTransId", acctgTransId);
		try {
			delegator.createOrStore(stationAccountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "POSTED";

	}

	private static String getBranch(String partyId) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// GenericValue accountTransaction = null;
		GenericValue person = null;
		try {
			person = delegator.findOne("Person",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if (person == null)
			return null;
		return person.getString("branchId");
	}

	public static String getEmployeeBranch(String partyId) {
		String branchId = "";
		branchId = getBranch(partyId);
		return branchId;
	}

	public static String getMemberBranch(Long memberAccountId) {
		String branchId = "";
		// Get MemberAccount
		GenericValue memberAccount = null;
		// memberAccountId = memberAccountId.replaceAll(",", "");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long partyId = memberAccount.getLong("partyId");

		// Get Member
		GenericValue member = null;
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		branchId = member.getString("branchId");
		return branchId;
	}

	/***
	 * Gets the next slip number
	 * */
	public static String getNextSlipNumber() {
		String slipNumber = "";
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String helperName = delegator.getGroupHelperName("org.ofbiz"); // gets
																		// the
																		// helper
																		// (localderby,
																		// localmysql,
																		// localpostgres,
																		// etc.)
																		// for
																		// your
																		// entity
																		// group
																		// org.ofbiz
		Connection conn = null;
		try {
			conn = ConnectionFactory.getConnection(helperName);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Statement statement = null;
		try {
			statement = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			statement
					.execute("SELECT count(SLIP_NUMBER) as slipnumbercount FROM ACCOUNT_TRANSACTION");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ResultSet results = null;
		try {
			results = statement.getResultSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Long count = 0L;

		try {
			while (results.next()) {
				count = results.getLong("slipnumbercount");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int padDigits = 10;
		count = count + 1;

		slipNumber = paddString(padDigits, count.toString());

		return slipNumber;
	}

	public static String paddString(int padDigits, String count) {
		String padded = String.format("%" + padDigits + "s", count).replace(
				' ', '0');
		return padded;
	}

	/***
	 * Get Balance Given Account and Date
	 * */
	public static BigDecimal getTotalBalance(String memberAccountId,
			Timestamp balanceDate) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// // Get Total Deposits
		if (balanceDate == null)
			balanceDate = new Timestamp(Calendar.getInstance()
					.getTimeInMillis());
		return (getAvailableBalanceVer2(memberAccountId, balanceDate)
				.add(bdOpeningBalance));
	}

	public static BigDecimal getTotalBalanceNow(String memberAccountId) {

		log.info(" ##### Account " + memberAccountId);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// // Get Total Deposits
		Timestamp balanceDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		BigDecimal balance = getAvailableBalanceVer2(memberAccountId,
				balanceDate).add(bdOpeningBalance);

		log.info(" #####BBBBBBB  Balance " + balance);
		return balance;
	}

	public static Map<String, Object> getTotalBalanceNow(DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();

		String memberAccountId = (String) context.get("memberAccountId");

		log.info(" ##### Account " + memberAccountId);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// // Get Total Deposits
		Timestamp balanceDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		BigDecimal balance = getAvailableBalanceVer2(memberAccountId,
				balanceDate).add(bdOpeningBalance);

		log.info(" #####BBBBBBB  Balance " + balance);
		result.put("balance", balance);
		return result;
	}

	public static BigDecimal getAvailableBalanceVer2(String memberAccountId,
			Timestamp balanceDate) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;
		BigDecimal bdTotalAvailable = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "I");
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is " + bdTotalIncrease);

		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is " + bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				balanceDate);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "
				+ bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, balanceDate);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "
				+ bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);

		memberAccountId = memberAccountId.replaceAll(",", "");
		Long lmemberAccountId = Long.valueOf(memberAccountId);

		BigDecimal bdRetailedSavings = BigDecimal.ZERO;
		bdRetailedSavings = getRetainedSavings(lmemberAccountId);

		bdTotalAvailable = bdTotalAvailable.subtract(bdRetailedSavings);
		bdTotalAvailable = bdTotalAvailable
				.subtract(getMinimumBalance(lmemberAccountId));
		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is " + bdTotalAvailable);
		// return
		// bdTotalIncrease.add(bdTotalChequeDepositCleared).subtract(bdTotalDecrease).subtract(bdTotalChequeDeposit);
		return bdTotalAvailable;
	}

	// Give loanApplicationId - get the account balance - Member Deposits if
	// loan product
	// has no multiplier account otherwise use multiplier account specified
	public static BigDecimal getShareSavingsValue(Long loanApplicationId) {
		BigDecimal bdTotalAmount = BigDecimal.ZERO;

		GenericValue loanApplication = getLoanApplicationEntity(loanApplicationId);
		GenericValue loanProduct = getLoanProductEntity(loanApplication
				.getLong("loanProductId"));

		if (loanProduct.getLong("accountProductId") != null) {
			bdTotalAmount = getAccountTotalBalance(
					loanProduct.getLong("accountProductId"),
					loanApplication.getLong("partyId"));
		} else {
			Long accountProductId = getMemberDepositsAccountId(MEMBER_DEPOSIT_CODE);
			bdTotalAmount = getAccountTotalBalance(accountProductId,
					loanApplication.getLong("partyId"));
		}

		return bdTotalAmount;
	}

	public static BigDecimal getAccountTotalBalance(Long accountProductId,
			Long partyId) {
		// TODO Auto-generated method stub
		List<GenericValue> memberAccountELI = null; // =
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);

		// EntityOperator._emptyMap
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberAccountId = null;
		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		for (GenericValue genericValue : memberAccountELI) {
			memberAccountId = genericValue.getLong("memberAccountId");
		}

		BigDecimal bdTotalAmount = getAvailableBalanceVer3(
				memberAccountId.toString(), new Timestamp(Calendar
						.getInstance().getTimeInMillis()));
		return bdTotalAmount;
	}

	private static Long getMemberDepositsAccountId(String code) {
		List<GenericValue> accountProductELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long accountProductId = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProductId = genericValue.getLong("accountProductId");
		}
		return accountProductId;
	}

	public static GenericValue getLoanProductEntity(Long loanProductId) {
		GenericValue loanProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanProduct;
	}

	public static GenericValue getLoanApplicationEntity(Long loanApplicationId) {
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return loanApplication;
	}

	public static BigDecimal getAvailableBalanceVer3(String memberAccountId,
			Timestamp balanceDate) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;
		BigDecimal bdTotalAvailable = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "I");
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is " + bdTotalIncrease);

		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is " + bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				balanceDate);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "
				+ bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, balanceDate);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "
				+ bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);

		memberAccountId = memberAccountId.replaceAll(",", "");
		Long lmemberAccountId = Long.valueOf(memberAccountId);

		BigDecimal bdRetailedSavings = BigDecimal.ZERO;
		bdRetailedSavings = getRetainedSavings(lmemberAccountId);

		bdTotalAvailable = bdTotalAvailable
				.subtract(getMinimumBalance(lmemberAccountId));
		bdTotalAvailable = bdTotalAvailable.subtract(bdRetailedSavings);
		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is " + bdTotalAvailable);
		// return
		// bdTotalIncrease.add(bdTotalChequeDepositCleared).subtract(bdTotalDecrease).subtract(bdTotalChequeDeposit);
		return bdTotalAvailable.add(calculateOpeningBalance(memberAccountId,
				DelegatorFactoryImpl.getDelegator(null)));
	}

	public static BigDecimal getBookBalanceVer3(String memberAccountId,
			Timestamp balanceDate) {
		BigDecimal bdTotalIncrease = BigDecimal.ZERO;
		BigDecimal bdTotalDecrease = BigDecimal.ZERO;
		BigDecimal bdTotalAvailable = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "I");
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is " + bdTotalIncrease);

		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is " + bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				balanceDate);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "
				+ bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, balanceDate);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "
				+ bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);

		memberAccountId = memberAccountId.replaceAll(",", "");
		Long lmemberAccountId = Long.valueOf(memberAccountId);

		BigDecimal bdRetailedSavings = BigDecimal.ZERO;
		bdRetailedSavings = getRetainedSavings(lmemberAccountId);

		// bdTotalAvailable = bdTotalAvailable
		// .subtract(getMinimumBalance(lmemberAccountId));
		bdTotalAvailable = bdTotalAvailable.subtract(bdRetailedSavings);
		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is " + bdTotalAvailable);
		// return
		// bdTotalIncrease.add(bdTotalChequeDepositCleared).subtract(bdTotalDecrease).subtract(bdTotalChequeDeposit);
		return bdTotalAvailable.add(calculateOpeningBalance(memberAccountId,
				DelegatorFactoryImpl.getDelegator(null)));
	}

	private static BigDecimal getRetainedSavings(Long lmemberAccountId) {
		// TODO Auto-generated method stub
		BigDecimal bdRetainedSavingsAmt = BigDecimal.ZERO;

		GenericValue memberAccount = getMemberAccount(lmemberAccountId);
		if (memberAccount == null)
			return bdRetainedSavingsAmt;

		GenericValue accountProduct = getAccountProductEntity(lmemberAccountId);

		if (accountProduct == null)
			return bdRetainedSavingsAmt;

		if (!accountProduct.getString("code").equals("999"))
			return bdRetainedSavingsAmt;

		BigDecimal multipleOfSavingsAmt = getMultipleOfSavingsAmount("D318");

		bdRetainedSavingsAmt = getTotalFosaSavingsBasedLoans(
				accountProduct.getLong("accountProductId"),
				memberAccount.getLong("partyId"), multipleOfSavingsAmt);

		return bdRetainedSavingsAmt;
	}

	private static BigDecimal getMultipleOfSavingsAmount(String code) {
		List<GenericValue> loanProductELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanProductELI = delegator.findList("LoanProduct",
					EntityCondition.makeCondition("code", code), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal multipleOfSavingsAmt = null;
		for (GenericValue genericValue : loanProductELI) {
			multipleOfSavingsAmt = genericValue
					.getBigDecimal("multipleOfSavingsAmt");
		}
		return multipleOfSavingsAmt;
	}

	private static BigDecimal getTotalFosaSavingsBasedLoans(
			Long accountProductId, Long partyId,
			BigDecimal bdMultipleOfSavingsAmt) {
		// TODO Auto-generated method stub
		// BigDecimal bdTotal = BigDecimal.ZERO;
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		List<GenericValue> loanApplicationELI = null; // =
		EntityConditionList<EntityExpr> loanApplicationsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, loanStatusId)

				), EntityOperator.AND);

		// EntityOperator._emptyMap
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdTotalWithheldAmt = BigDecimal.ZERO;
		// List<GenericValue> loansList = new LinkedList<GenericValue>();
		for (GenericValue genericValue : loanApplicationELI) {
			// toDeleteList.add(genericValue);

			// if (genericValue.getLong(""))
			Long loanAccountProductId = null;
			loanAccountProductId = getAccountProductFromLoanProduct(genericValue
					.getLong("loanProductId"));
			if ((loanAccountProductId != null)
					&& (loanAccountProductId.equals(accountProductId))) {

				BigDecimal bdLoanRepaid = LoanServices
						.getLoansRepaidByLoanApplicationId(genericValue
								.getLong("loanApplicationId"));
				BigDecimal bdLoanBalance = genericValue
						.getBigDecimal("loanAmt").subtract(bdLoanRepaid);
				// bdTotal = bdTotal.add(bdLoanBalance);
				// if ()
				BigDecimal dbWithheldBalance = bdLoanBalance.divide(
						bdMultipleOfSavingsAmt, 4, RoundingMode.HALF_UP);

				bdTotalWithheldAmt = bdTotalWithheldAmt.add(dbWithheldBalance);
			}

		}

		return bdTotalWithheldAmt;
	}

	private static BigDecimal calculateTotalIncreaseDecrease(
			String memberAccountId, Timestamp balanceDate,
			String increaseDecrease) {
		List<GenericValue> cashDepositELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"memberAccountId", EntityOperator.EQUALS,
								Long.valueOf(memberAccountId)),
								EntityCondition
										.makeCondition("increaseDecrease",
												EntityOperator.EQUALS,
												increaseDecrease),
								EntityCondition.makeCondition("createdStamp",
										EntityOperator.LESS_THAN_EQUAL_TO,
										balanceDate)), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		BigDecimal bdTransactionAmount = null;
		log.info("Got  ----------- " + cashDepositELI.size() + " Records !!!");
		for (GenericValue genericValue : cashDepositELI) {
			bdTransactionAmount = genericValue
					.getBigDecimal("transactionAmount");
			if (bdTransactionAmount != null) {
				bdBalance = bdBalance.add(bdTransactionAmount);
			}
		}
		return bdBalance;
	}

	/***
	 * @author Japheth Odonya @when Jul 11, 2015 12:56:54 PM Calculating
	 *         Increase amounts in account between dates
	 * */
	private static BigDecimal calculateTotalIncrease(String memberAccountId,
			Timestamp startdDate, Timestamp endDate, String increaseDecrease) {
		List<GenericValue> cashDepositELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition
						.makeCondition("increaseDecrease",
								EntityOperator.EQUALS, increaseDecrease),
						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								startdDate),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN_EQUAL_TO, endDate)),
						EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		BigDecimal bdTransactionAmount = null;
		log.info("Got  ----------- " + cashDepositELI.size() + " Records !!!");
		for (GenericValue genericValue : cashDepositELI) {
			bdTransactionAmount = genericValue
					.getBigDecimal("transactionAmount");
			if (bdTransactionAmount != null) {
				bdBalance = bdBalance.add(bdTransactionAmount);
			}
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalChequeDeposits(
			String memberAccountId, Timestamp balanceDate) {
		List<GenericValue> chequeDepositELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(
								EntityCondition.makeCondition(
										"memberAccountId",
										EntityOperator.EQUALS,
										Long.valueOf(memberAccountId)),
								EntityCondition.makeCondition(
										"transactionType",
										EntityOperator.EQUALS, "CHEQUEDEPOSIT"),
								EntityCondition.makeCondition("createdStamp",
										EntityOperator.LESS_THAN_EQUAL_TO,
										balanceDate)), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			chequeDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (chequeDepositELI == null) {
			log.info(" ######### This member has Cheque Deposit #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : chequeDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalClearedChequeDeposits(
			String memberAccountId, Timestamp balanceDate) {
		List<GenericValue> chequeDepositELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(
								EntityCondition.makeCondition(
										"memberAccountId",
										EntityOperator.EQUALS,
										Long.valueOf(memberAccountId)),
								EntityCondition.makeCondition(
										"transactionType",
										EntityOperator.EQUALS, "CHEQUEDEPOSIT"),
								EntityCondition.makeCondition("clearDate",
										EntityOperator.LESS_THAN_EQUAL_TO,
										balanceDate)), EntityOperator.AND);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			chequeDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (chequeDepositELI == null) {
			log.info(" ######### This member has Cheque Deposit #########"
					+ memberAccountId);
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : chequeDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/***
	 * Cash Withdrawal from a service
	 * 
	 * withdrawalType - CASHWITHDRAWAL, ATMWITHDRAWAL, MSACCOWITHDRAWAL
	 * */
	public static ATMTransaction cashWithdrawal(BigDecimal amount,
			String memberAccountId, String withdrawalType) {
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		memberAccountId = memberAccountId.replaceAll(",", "");
		accountTransaction = delegator.makeValue("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);

		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");

		String transactionId = null;
		// Check if enough amount - amount + charge + excercise duty < available
		// - minimum balance
		Boolean isEnough = isEnoughBalance(Long.valueOf(memberAccountId),
				amount, withdrawalType);
		ATMTransaction transaction = new ATMTransaction();
		BigDecimal dbAvailableBalance = null;
		
	//	LoanUtilities.getMember

		if (isEnough) {
			//if (WITHDRAWALOK.equals("OK")) {
				transactionId = cashWithdrawalATM(accountTransaction, userLogin,
						withdrawalType, null);

				transactionId = transactionId.replaceAll(",", "");

				transaction.setTransactionId(Long.valueOf(transactionId));

			//}
			transaction.setStatus("SUCCESS");
			transaction.setAmount(amount);

			//if (WITHDRAWALOK.equals("OK")) {
				ChargeDutyItem chargeDutyItem = getChargeDuty(transactionId);

				if (chargeDutyItem.getChargeAmount() != null)
					transaction.setChargeAmount(chargeDutyItem
							.getChargeAmount());

				if (chargeDutyItem.getDutyAmount() != null)
					transaction.setCommissionAmount(chargeDutyItem
							.getDutyAmount());
			//}
		} else {
			transaction.setStatus("NOTENOUGHBALANCE");
		}

		dbAvailableBalance = AccHolderTransactionServices
				.getAvailableBalanceVer3(memberAccountId, new Timestamp(
						Calendar.getInstance().getTimeInMillis()));
		transaction.setAvailableBalance(dbAvailableBalance);
		transaction.setBookBalance(AccHolderTransactionServices
				.getBookBalanceVer3(memberAccountId, delegator));
		// transaction.setCardNumber(cardNumber);

		return transaction;
	}
	
	//SystemTrace
	
	//Adding system trace
	public static ATMTransaction cashWithdrawal(BigDecimal amount,
			String memberAccountId, String withdrawalType, String SystemTrace) {
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		memberAccountId = memberAccountId.replaceAll(",", "");
		accountTransaction = delegator.makeValue("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);
		accountTransaction.put("systemtrace", SystemTrace);
		
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");

		String transactionId = null;
		// Check if enough amount - amount + charge + excercise duty < available
		// - minimum balance
		Boolean isEnough = isEnoughBalance(Long.valueOf(memberAccountId),
				amount, withdrawalType);
		ATMTransaction transaction = new ATMTransaction();
		BigDecimal dbAvailableBalance = null;
		
	//	LoanUtilities.getMember

		if (isEnough) {
			//if (WITHDRAWALOK.equals("OK")) {
				transactionId = cashWithdrawalATM(accountTransaction, userLogin,
						withdrawalType, SystemTrace);

				transactionId = transactionId.replaceAll(",", "");

				transaction.setTransactionId(Long.valueOf(transactionId));

			//}
			transaction.setStatus("SUCCESS");
			transaction.setAmount(amount);

			//if (WITHDRAWALOK.equals("OK")) {
				ChargeDutyItem chargeDutyItem = getChargeDuty(transactionId);

				if (chargeDutyItem.getChargeAmount() != null)
					transaction.setChargeAmount(chargeDutyItem
							.getChargeAmount());

				if (chargeDutyItem.getDutyAmount() != null)
					transaction.setCommissionAmount(chargeDutyItem
							.getDutyAmount());
			//}
		} else {
			transaction.setStatus("NOTENOUGHBALANCE");
		}

		dbAvailableBalance = AccHolderTransactionServices
				.getAvailableBalanceVer3(memberAccountId, new Timestamp(
						Calendar.getInstance().getTimeInMillis()));
		transaction.setAvailableBalance(dbAvailableBalance);
		transaction.setBookBalance(AccHolderTransactionServices
				.getBookBalanceVer3(memberAccountId, delegator));
		// transaction.setCardNumber(cardNumber);

		return transaction;
	}
	/***
	 * @author Japheth Odonya @when Jul 21, 2015 12:02:58 PM
	 * 
	 * 
	 *         Withdraw Msacco Changed to withdraw on Request
	 * */

	public static ATMTransaction cashWithdrawal(BigDecimal amount,
			String memberAccountId, String withdrawalType,
			String withdrawalStage, String reference,
			String partnerTransactionId) {

		log.info("PPPPPPP partnerTransactionId 2 " + partnerTransactionId);
		GenericValue accountTransaction = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		memberAccountId = memberAccountId.replaceAll(",", "");
		accountTransaction = delegator.makeValue("AccountTransaction");
		accountTransaction
				.put("memberAccountId", Long.valueOf(memberAccountId));
		accountTransaction.put("transactionAmount", amount);

		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", "admin");

		String transactionId = null;
		// Check if enough amount - amount + charge + excercise duty < available
		// - minimum balance
		ATMTransaction transaction = new ATMTransaction();
		BigDecimal dbAvailableBalance = null;

		dbAvailableBalance = AccHolderTransactionServices
				.getAvailableBalanceVer3(memberAccountId, new Timestamp(
						Calendar.getInstance().getTimeInMillis()));
		transaction.setAvailableBalance(dbAvailableBalance);
		transaction.setBookBalance(AccHolderTransactionServices
				.getBookBalanceVer3(memberAccountId, delegator));

		if (withdrawalStage.equals("Withdrawal_Request")) {
			Boolean isEnough = isEnoughBalance(Long.valueOf(memberAccountId),
					amount, withdrawalType);

			if (isEnough) {
				transactionId = cashWithdrawal(accountTransaction, userLogin,
						withdrawalType, partnerTransactionId);

				transactionId = transactionId.replaceAll(",", "");

				transaction.setTransactionId(Long.valueOf(transactionId));

				transaction.setStatus("SUCCESS");
				transaction.setAmount(amount);

				ChargeDutyItem chargeDutyItem = getChargeDuty(transactionId);

				if (chargeDutyItem.getChargeAmount() != null)
					transaction.setChargeAmount(chargeDutyItem
							.getChargeAmount());

				if (chargeDutyItem.getDutyAmount() != null)
					transaction.setCommissionAmount(chargeDutyItem
							.getDutyAmount());
			} else {
				transaction.setStatus("NOTENOUGHBALANCE");
			}

			// transaction.setCardNumber(cardNumber);

		} else if (withdrawalStage.equals("Withdrawal_Confirm")) {
			// Update transaction reference , update the thetransactionId with
			// reference
			transaction.setTransactionId(Long.valueOf(partnerTransactionId));

			transaction.setStatus("SUCCESS");
			transaction.setAmount(amount);

			AccHolderTransactionServices.updateTransactionWithReferenceNo(
					partnerTransactionId, reference);

		} else if (withdrawalStage.equals("Withdrawal_Decline")) {
			// Reverse transaction already done
			// get theTransactionId and reverse it (set to reversed)
			transaction.setTransactionId(Long.valueOf(partnerTransactionId));

			transaction.setStatus("SUCCESS");
			transaction.setAmount(amount);

			AccHolderTransactionServices
					.reverseMsaccoTransaction(partnerTransactionId);

		} else {
			// Do Nothing
		}

		return transaction;

	}

	private static void reverseMsaccoTransaction(String partnerTransactionId) {
		String newacctgTransId = creatAccountTransRecord(null, null);
		// Get the acctgTransEntry list with thetransactionId
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// Update the source record too (Subsidiary)

		// Get new tras
		// ***
		// Change the transactionType MSACCOWITHDRAWALREV
		// TODO Auto-generated method stub

		String thetransactionId = "";
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> accountTransactionELI = null;
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("partnerTransactionId",
							partnerTransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : accountTransactionELI) {

			if (genericValue.getString("partnerTransactionId") != null) {
				thetransactionId = genericValue.getString("acctgTransId");
			}
		}

		accountTransactionELI = null;
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("acctgTransId",
							thetransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the AccountTransactionReccords
		String accountTransactionId = "";
		for (GenericValue accountTransaction : accountTransactionELI) {

			accountTransactionId = delegator.getNextSeqId("AccountTransaction");
			accountTransaction.setString("transactionType",
					"MSACCOWITHDRAWALREV");
			accountTransaction.setString("increaseDecrease", "I");
			accountTransaction.setString("acctgTransId", newacctgTransId);
			accountTransaction.setString("partnerTransactionId",
					partnerTransactionId);

			accountTransaction.setString("accountTransactionId",
					accountTransactionId);

			try {
				delegator.create(accountTransaction);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// Get the thetransactionId

		List<GenericValue> acctgTransEntryELI = null;
		try {
			acctgTransEntryELI = delegator.findList("AcctgTransEntry",
					EntityCondition.makeCondition("acctgTransId",
							thetransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the acctgTransEntry
		for (GenericValue genericValue : acctgTransEntryELI) {

			if (genericValue.getString("debitCreditFlag").equals("C")) {
				genericValue.setString("debitCreditFlag", "D");
			} else {
				genericValue.setString("debitCreditFlag", "C");
			}
			genericValue.setString("acctgTransId", newacctgTransId);

			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// update the entries reveresed (D to C and C to D)
		// update the transactionId to the new id
		// create the record for each

	}

	private static void updateTransactionWithReferenceNo(
			String partnerTransactionId, String reference) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> accountTransactionELI = null;
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("partnerTransactionId",
							partnerTransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		String acctgTransId = "";
		for (GenericValue genericValue : accountTransactionELI) {
			acctgTransId = genericValue.getString("acctgTransId");
		}

		accountTransactionELI = null;
		try {
			accountTransactionELI = delegator
					.findList("AccountTransaction", EntityCondition
							.makeCondition("acctgTransId", acctgTransId), null,
							null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the AccountTransactionReccords
		for (GenericValue genericValue : accountTransactionELI) {
			genericValue.setString("reference", reference);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static Boolean isEnoughBalance(Long memberAccountId,
			BigDecimal bdAmount) {

		BigDecimal bdAVailableBalance = getAvailableBalanceVer2(
				String.valueOf(memberAccountId), new Timestamp(Calendar
						.getInstance().getTimeInMillis()));
		BigDecimal bdMinimumBalance = getMinimumBalance(memberAccountId);
		BigDecimal bdTotalCharges = getChargesTotal(memberAccountId, bdAmount,
				"CASHWITHDRAWAL");

		BigDecimal bdTotalDeducted = bdAmount.add(bdTotalCharges);
		BigDecimal bdTotalRemaining = bdAVailableBalance
				.subtract(bdTotalDeducted);

		if (bdTotalRemaining.compareTo(bdMinimumBalance) == 1)
			return true;

		return false;
	}

	private static Boolean isEnoughBalance(Long memberAccountId,
			BigDecimal bdAmount, String transactionType) {

		BigDecimal bdAVailableBalance = getAvailableBalanceVer3(
				String.valueOf(memberAccountId), new Timestamp(Calendar
						.getInstance().getTimeInMillis()));
		BigDecimal bdMinimumBalance = getMinimumBalance(memberAccountId);
		BigDecimal bdTotalCharges = getChargesTotal(memberAccountId, bdAmount,
				transactionType);

		BigDecimal bdTotalDeducted = bdAmount.add(bdTotalCharges);
		BigDecimal bdTotalRemaining = bdAVailableBalance
				.subtract(bdTotalDeducted);

		if (bdTotalRemaining.compareTo(bdMinimumBalance) == 1)
			return true;

		return false;
	}

	private static ChargeDutyItem getChargeDuty(String transactionId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> accountTransactionELI = null;
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("transactionAmount");
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("accountTransactionParentId",
							transactionId), null, listOrder, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// add the charge - no 1, commission no 0
		ChargeDutyItem chargeDutyItem = new ChargeDutyItem();
		if (accountTransactionELI.size() == 3) {
			chargeDutyItem.setChargeAmount(accountTransactionELI.get(1)
					.getBigDecimal("transactionAmount"));
			chargeDutyItem.setDutyAmount(accountTransactionELI.get(0)
					.getBigDecimal("transactionAmount"));
		}
		return chargeDutyItem;
	}

	public static String cashWithdrawal(GenericValue accountTransaction,
			Map<String, String> userLogin, String withdrawalType) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = withdrawalType;
		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));

		String acctgTransId = postCashWithdrawalTransaction(accountTransaction,
				userLogin);
		;
		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);

		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());
		Long sequence = 0L;
		sequence = sequence + 1;
		log.info("#########1 Employee Branch ID " + employeeBranchId
				+ " Member Branch ID " + memberBranchId);
		addChargesToTransaction(accountTransaction, userLogin, transactionType,
				employeeBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId);
		// increaseDecrease
		createTransaction(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	
	public static String cashWithdrawalATM(GenericValue accountTransaction,
			Map<String, String> userLogin, String withdrawalType, String SystemTrace) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = withdrawalType;
		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));
		
	//	LoanUtilities.getMemberB
		String memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());

		String acctgTransId = postCashWithdrawalTransactionATM(accountTransaction,
				userLogin, memberBranchId);
		;
		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);

		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		//tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());
		Long sequence = 0L;
		sequence = sequence + 1;
		log.info("#########1 Employee Branch ID " + employeeBranchId
				+ " Member Branch ID " + memberBranchId);
		addChargesToTransactionATM(accountTransaction, userLogin, transactionType,
				employeeBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId, SystemTrace);
		//SystemTrace
		// increaseDecrease
		createTransactionATM(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId, SystemTrace);

		return accountTransactionParent.getString("accountTransactionParentId");
	}
	
	
	public static String cashWithdrawalATMReversal(GenericValue accountTransaction,
			Map<String, String> userLogin, String withdrawalType, String SystemTrace) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = withdrawalType;
		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));
	//	LoanUtilities.getMemberB
		String memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());

		String acctgTransId = postCashWithdrawalTransactionATM(accountTransaction,
				userLogin, memberBranchId);
		;
		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);

		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		//tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());
		Long sequence = 0L;
		sequence = sequence + 1;
		
		log.info("#########1 Employee Branch ID " + employeeBranchId
				+ " Member Branch ID " + memberBranchId);
		
		addChargesToTransactionATMReversal(accountTransaction, userLogin, transactionType,
				employeeBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId);
		// increaseDecrease
		//cashWithdrawalATMReversal
		createTransactionATMReversal(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	
	// partnerTransactionId
	public static String cashWithdrawal(GenericValue accountTransaction,
			Map<String, String> userLogin, String withdrawalType,
			String partnerTransactionId) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = withdrawalType;
		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));

		String memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(memberAccountId).getLong("partyId")
				.toString());

		String acctgTransId = postCashWithdrawalTransactionMsacco(
				accountTransaction, userLogin, memberBranchId);
		;
		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);

		String glLedgerAccountId = null;
		String commissionAccountId = null;
		// String tellerAccountId = null;
		String exciseDutyAccountId = null;

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		// tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// String employeeBranchId =
		// getEmployeeBranch(userLogin.get("partyId"));
		Long sequence = 0L;
		sequence = sequence + 1;
		log.info("#########1 Employee Branch ID " + "" + " Member Branch ID "
				+ memberBranchId);
		addChargesToTransaction(accountTransaction, userLogin, transactionType,
				memberBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId);
		// increaseDecrease
		log.info("PPPPPPPPPPPPPPPP 3 " + partnerTransactionId);
		createTransactionMsacco(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId,
				partnerTransactionId);

		return acctgTransId;
		// accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String cashWithdrawal(GenericValue accountTransaction,
			Map<String, String> userLogin) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = "CASHWITHDRAWAL";
		String memberAccountId = accountTransaction
				.getString("memberAccountId");

		memberAccountId = memberAccountId.replaceAll(",", "");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));
		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;

		GenericValue accountProduct = getAccountProductEntity(Long
				.valueOf(memberAccountId));

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(LoanUtilities
				.getMemberAccount(Long.valueOf(memberAccountId))
				.getLong("partyId").toString());

		log.info("#########2 Employee Branch ID " + employeeBranchId
				+ " Member Branch ID " + memberBranchId);
		Long sequence = 0L;
		sequence = sequence + 1;

		String acctgTransId = postCashWithdrawalTransaction(accountTransaction,
				userLogin);
		addChargesToTransaction(accountTransaction, userLogin, transactionType,
				employeeBranchId, memberBranchId, acctgTransId,
				glLedgerAccountId, sequence, commissionAccountId,
				exciseDutyAccountId);
		// increaseDecrease
		createTransaction(accountTransaction, transactionType, userLogin,
				memberAccountId, transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);

		// return "success";
		return accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String cashWithdrawalVer2(GenericValue accountTransaction,
			Map<String, String> userLogin) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		/****
		 * Debit Member Deposits (Liability A/C) (Amount + Commission + Excise
		 * Duty) - from ledger account on AccountProduct Credit Commission
		 * Account (Revenue A/C) - from commission account on AccountProduct
		 * Credit Excise Duty Account - from Excise account on AccountProduct
		 * Credit teller account from treasury account for the logged in user
		 * 
		 * */

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = "CASHWITHDRAWAL";
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));
		String accountTransactionParentId = accountTransactionParent
				.getString("accountTransactionParentId");

		BigDecimal bdCommissionAmount = getTransactionCommissionAmount(transactionAmount);
		BigDecimal bdExciseDutyAmount = getTransactionExcideDutyAmount(bdCommissionAmount);

		// GL
		/****
		 * Dr total - Amount + Commission + Excise Duty to member deposits Cr
		 * teller a/c with Amount Cr Commission a/c with commission Cr excise
		 * duty with excise duty amount
		 * 
		 ***/
		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyIdForMember = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities
				.getMemberBranchId(partyIdForMember);
		// LoanUtilities.getMemberBranchId(partyId);
		BigDecimal bdTotalAmount = BigDecimal.ZERO;
		bdTotalAmount = transactionAmount.add(bdCommissionAmount).add(
				bdExciseDutyAmount);

		List<GenericValue> listPostEntity = new ArrayList<GenericValue>();

		Long sequence = 0l;

		// Post memberDeposit
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
				glAccountTypeId, employeeBranchId, bdTotalAmount,
				memberAccountId, acctgTransId, "D", sequence.toString(),
				memberBranchId));

		// Post for Teller
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(tellerAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));
		// Post for Commission
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(commissionAccountId,
				glAccountTypeId, employeeBranchId, bdCommissionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));
		// Post for Excise Duty
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(exciseDutyAccountId,
				glAccountTypeId, employeeBranchId, bdExciseDutyAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));

		// Return acctgTransId
		// tt
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listPostEntity);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		// return acctgTrans

		// Subsidiary
		/***
		 * Record Amount + Record Commission - Record Excise Duty -
		 * **/
		List<GenericValue> listAccountTransaction = new ArrayList<GenericValue>();
		// get the parent id
		// get acctgTransId
		// Create the subsidiary with these two - this means we do gl posting
		// first then

		// Cash withdrawal
		listAccountTransaction.add(createTransactionVer2("CASHWITHDRAWAL",
				userLogin, memberAccountId.toString(), transactionAmount, null,
				accountTransactionParentId, acctgTransId));
		// Commission
		listAccountTransaction.add(createTransactionVer2(
				"WITHDRAWALCOMMISSION", userLogin, memberAccountId.toString(),
				bdCommissionAmount, null, accountTransactionParentId,
				acctgTransId));
		// Excercise Duty
		listAccountTransaction.add(createTransactionVer2("EXCISEDUTY",
				userLogin, memberAccountId.toString(), bdExciseDutyAmount,
				null, accountTransactionParentId, acctgTransId));

		try {
			delegator.storeAll(listAccountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease
		// createTransaction(accountTransaction, transactionType, userLogin,
		// memberAccountId.toString(), transactionAmount, null,
		// accountTransactionParent
		// .getString("accountTransactionParentId"));

		// return "success";
		return accountTransactionParent.getString("accountTransactionParentId");
	}

	/*****
	 * @author Japheth Odonya @when Jul 6, 2015 10:21:00 PM
	 *         cashWithdrawalInterbranch
	 * ***/
	public static String cashWithdrawalInterbranch(
			GenericValue accountTransaction, Map<String, String> userLogin) {

		log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		/****
		 * Debit Member Deposits (Liability A/C) (Amount + Commission + Excise
		 * Duty) - from ledger account on AccountProduct Credit Commission
		 * Account (Revenue A/C) - from commission account on AccountProduct
		 * Credit Excise Duty Account - from Excise account on AccountProduct
		 * Credit teller account from treasury account for the logged in user
		 * 
		 * */

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				accountTransaction, userLogin);
		String transactionType = "CASHWITHDRAWAL";
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		accountTransaction.set("accountTransactionParentId",
				accountTransactionParent
						.getString("accountTransactionParentId"));
		String accountTransactionParentId = accountTransactionParent
				.getString("accountTransactionParentId");

		BigDecimal bdCommissionAmount = getTransactionCommissionAmount(transactionAmount);
		BigDecimal bdExciseDutyAmount = getTransactionExcideDutyAmount(bdCommissionAmount);

		// GL
		/****
		 * Dr total - Amount + Commission + Excise Duty to member deposits Cr
		 * teller a/c with Amount Cr Commission a/c with commission Cr excise
		 * duty with excise duty amount
		 * 
		 ***/
		String glLedgerAccountId = null;
		String commissionAccountId = null;
		String tellerAccountId = null;
		String exciseDutyAccountId = null;
		String settlementAccountId = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		commissionAccountId = accountProduct.getString("commissionAccountId");
		exciseDutyAccountId = accountProduct.getString("exciseDutyAccountId");
		tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);
		settlementAccountId = getCashAccount(null,
				"OVERTHECOUNTERSETTLEMENTACCOUNT");

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyIdForMember = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities
				.getMemberBranchId(partyIdForMember);
		// LoanUtilities.getMemberBranchId(partyId);
		BigDecimal bdTotalAmount = BigDecimal.ZERO;
		bdTotalAmount = transactionAmount.add(bdCommissionAmount).add(
				bdExciseDutyAmount);

		List<GenericValue> listPostEntity = new ArrayList<GenericValue>();

		Long sequence = 0l;

		// Post memberDeposit
		sequence = sequence + 1;

		if (employeeBranchId.equals(memberBranchId)) {
			// Employee is being served at his/her branch
			listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
					glAccountTypeId, employeeBranchId, bdTotalAmount,
					memberAccountId, acctgTransId, "D", sequence.toString(),
					memberBranchId));
		} else {
			// Employee is being served at a different branch
			listPostEntity.add(createAccountPostingEntryVer2(
					settlementAccountId, glAccountTypeId, employeeBranchId,
					bdTotalAmount, memberAccountId, acctgTransId, "D",
					sequence.toString(), memberBranchId));
		}

		// Post for Teller
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(tellerAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));
		// Post for Commission
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(commissionAccountId,
				glAccountTypeId, employeeBranchId, bdCommissionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));
		// Post for Excise Duty
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(exciseDutyAccountId,
				glAccountTypeId, employeeBranchId, bdExciseDutyAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));

		if (!employeeBranchId.equals(memberBranchId)) {
			// Now post the same transaction to the member's branch savings
			// withdrawable
			// and settlement
			// Dr Svings and Cr settlement

			sequence = sequence + 1;
			// Dr Ledger
			listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
					glAccountTypeId, memberBranchId, bdTotalAmount,
					memberAccountId, acctgTransId, "D", sequence.toString(),
					memberBranchId));

			sequence = sequence + 1;
			// Cr Over the counter settlement
			listPostEntity.add(createAccountPostingEntryVer2(
					settlementAccountId, glAccountTypeId, memberBranchId,
					bdTotalAmount, memberAccountId, acctgTransId, "C",
					sequence.toString(), memberBranchId));

			// Now post to HQ

			String hqBranchId = HQBRANCH;
			String employeeBranchSettlementAccountId = null;
			String memberBranchSettlementAccountId = null;
			GenericValue employeeBranch = LoanUtilities.getEntityValue(
					"PartyGroup", "partyId", employeeBranchId);
			GenericValue memberBranch = LoanUtilities.getEntityValue(
					"PartyGroup", "partyId", memberBranchId);

			employeeBranchSettlementAccountId = employeeBranch
					.getString("glAccountId");
			memberBranchSettlementAccountId = memberBranch
					.getString("glAccountId");

			// Dr Settlement Employee Branch
			sequence = sequence + 1;
			listPostEntity.add(createAccountPostingEntryVer2(
					employeeBranchSettlementAccountId, glAccountTypeId,
					hqBranchId, bdTotalAmount, memberAccountId, acctgTransId,
					"C", sequence.toString(), memberBranchId));

			// Cr Settlement Member Branch

			listPostEntity.add(createAccountPostingEntryVer2(
					memberBranchSettlementAccountId, glAccountTypeId,
					hqBranchId, bdTotalAmount, memberAccountId, acctgTransId,
					"D", sequence.toString(), memberBranchId));

		}

		// Return acctgTransId
		// tt
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listPostEntity);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		// return acctgTrans

		// Subsidiary
		/***
		 * Record Amount + Record Commission - Record Excise Duty -
		 * **/
		List<GenericValue> listAccountTransaction = new ArrayList<GenericValue>();
		// get the parent id
		// get acctgTransId
		// Create the subsidiary with these two - this means we do gl posting
		// first then

		// Cash withdrawal
		listAccountTransaction.add(createTransactionVer2("CASHWITHDRAWAL",
				userLogin, memberAccountId.toString(), transactionAmount, null,
				accountTransactionParentId, acctgTransId));
		// Commission
		listAccountTransaction.add(createTransactionVer2(
				"WITHDRAWALCOMMISSION", userLogin, memberAccountId.toString(),
				bdCommissionAmount, null, accountTransactionParentId,
				acctgTransId));
		// Excercise Duty
		listAccountTransaction.add(createTransactionVer2("EXCISEDUTY",
				userLogin, memberAccountId.toString(), bdExciseDutyAmount,
				null, accountTransactionParentId, acctgTransId));

		try {
			delegator.storeAll(listAccountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String cashDepositVer2(GenericValue accountTransaction,
			Map<String, String> userLogin) {

		// Post the Cash Deposit to the Teller for the logged in user
		/***
		 * Dr to the Teller Account Cr to the Member Deposits
		 * 
		 * */
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		String glLedgerAccountId = null;
		String tellerAccountId = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String memberPartyId = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(memberPartyId);

		// LoanUtilities.getMemberBranchId(partyId);

		List<GenericValue> listPostEntity = new ArrayList<GenericValue>();

		Long sequence = 0l;

		// Post memberDeposit
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "C", sequence.toString(),
				memberBranchId));

		// Post for Teller
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(tellerAccountId,
				glAccountTypeId, employeeBranchId, transactionAmount,
				memberAccountId, acctgTransId, "D", sequence.toString(),
				memberBranchId));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listPostEntity);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Update account transactions to reflect the ID in postings
		accountTransaction.set("acctgTransId", acctgTransId);
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return accountTransaction.getString("accountTransactionParentId");
	}

	/****
	 * @author Japheth Odonya @when Jun 28, 2015 12:54:58 PM
	 * 
	 *         Direct Debit posting
	 * 
	 * */
	public static String directDebitPosting(GenericValue accountTransaction,
			Map<String, String> userLogin) {

		// Post the Direct Debit to the Bank Selected
		/***
		 * Dr to the Bank Account and Cr to the Member Deposits
		 * 
		 * */
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		String glLedgerAccountId = null;
		// String tellerAccountId = null;

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glLedgerAccountId = accountProduct.getString("glAccountId");
		// tellerAccountId = TreasuryUtility.getTellerAccountId(userLogin);
		String finAccountId = accountTransaction.getString("finAccountId");
		String bankglAccountId = LoanUtilities.getBankglAccountId(finAccountId);

		log.info(" ############## Posting to Bank GL Account "
				+ bankglAccountId);

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyId = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);
		String branchId = LoanUtilities.getMemberBranchId(partyId);

		List<GenericValue> listPostEntity = new ArrayList<GenericValue>();

		Long sequence = 0l;

		// Post memberDeposit
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(glLedgerAccountId,
				glAccountTypeId, branchId, transactionAmount, memberAccountId,
				acctgTransId, "C", sequence.toString(), partyId));

		// Post for Bank - the Debited Bank Account
		sequence = sequence + 1;
		listPostEntity.add(createAccountPostingEntryVer2(bankglAccountId,
				glAccountTypeId, branchId, transactionAmount, memberAccountId,
				acctgTransId, "D", sequence.toString(), partyId));

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listPostEntity);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Update account transactions to reflect the ID in postings
		accountTransaction.set("acctgTransId", acctgTransId);
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return accountTransaction.getString("accountTransactionParentId");
	}

	/***
	 * 
	 * Get the excise duty
	 * 
	 * */
	public static BigDecimal getTransactionExcideDutyAmount(
			BigDecimal bdCommissionAmount) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> exciseDutyELI = null;

		try {
			exciseDutyELI = delegator.findList("ExciseDuty", null, null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue exciseDuty = null;

		// get the excise duty
		for (GenericValue genericValue : exciseDutyELI) {
			exciseDuty = genericValue;
		}

		if (exciseDuty != null) {
			BigDecimal bdDutyPercent = exciseDuty
					.getBigDecimal("dutyPercentage");
			BigDecimal bdDuty = bdCommissionAmount.multiply(bdDutyPercent)
					.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
			return bdDuty;
		}

		return null;
	}

	/****
	 * Get the commission amount given the transaction amount using the scale
	 * defined in the Setup Configuration
	 * 
	 **/
	public static BigDecimal getTransactionCommissionAmount(
			BigDecimal transactionAmount) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> commissionChargeELI = null;
		List<String> commissionsOrder = new ArrayList<String>();
		commissionsOrder.add("commissionChargeId");
		try {
			commissionChargeELI = delegator.findList("CommissionCharge", null,
					null, commissionsOrder, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		BigDecimal bdCommissionAmount = null;

		// BigDecimal.ZERO;

		for (GenericValue genericValue : commissionChargeELI) {
			BigDecimal bdLowerValue = genericValue.getBigDecimal("fromAmount");
			BigDecimal bdUpperValue = genericValue.getBigDecimal("toAmount");

			if ((bdLowerValue != null) && (bdUpperValue != null)) {

				// if amount between the two values then assign
				if ((transactionAmount.compareTo(bdLowerValue) >= 0)
						&& (transactionAmount.compareTo(bdUpperValue) <= 0)) {
					bdCommissionAmount = genericValue
							.getBigDecimal("chargeAmount");
				}

			} else {

				if (bdCommissionAmount == null)
					bdCommissionAmount = genericValue
							.getBigDecimal("chargeAmount");
			}
		}

		return bdCommissionAmount;
	}

	private static String postCashWithdrawalTransaction(
			GenericValue accountTransaction, Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);
		// createMemberDepositEntry(accountTransaction, acctgTransId);
		// createMemberCashEntry(accountTransaction, acctgTransId);

		createMemberDepositEntryAccount(accountTransaction, acctgTransId);
		createMemberCashEntryTeller(accountTransaction, acctgTransId, userLogin);
		return acctgTransId;
	}
	
	private static String postCashWithdrawalTransactionATM(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String memberBranchId) {
		// TODO Auto-generated method stub
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);
		// createMemberDepositEntry(accountTransaction, acctgTransId);
		// createMemberCashEntry(accountTransaction, acctgTransId);

		createMemberDepositEntryAccountMsacco(accountTransaction, acctgTransId,
				memberBranchId);

		// Credit Member Branch Settlement Account
		creditMemberBranchATMSettlementAccount(accountTransaction,
				acctgTransId, userLogin, memberBranchId);
		// createMemberCashEntryTeller(accountTransaction, acctgTransId,
		// userLogin);
		return acctgTransId;
	}
	
	
	private static String postCashWithdrawalTransactionATMReversal(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String memberBranchId) {
		// TODO Auto-generated method stub
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);
		// createMemberDepositEntry(accountTransaction, acctgTransId);
		// createMemberCashEntry(accountTransaction, acctgTransId);

		createMemberDepositEntryAccountMsaccoReversal(accountTransaction, acctgTransId,
				memberBranchId);

		// Credit Member Branch Settlement Account
		creditMemberBranchATMSettlementAccountReversal(accountTransaction,
				acctgTransId, userLogin, memberBranchId);
		// createMemberCashEntryTeller(accountTransaction, acctgTransId,
		// userLogin);
		return acctgTransId;
	}

	private static String postCashWithdrawalTransactionMsacco(
			GenericValue accountTransaction, Map<String, String> userLogin,
			String memberBranchId) {
		// TODO Auto-generated method stub
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);
		// createMemberDepositEntry(accountTransaction, acctgTransId);
		// createMemberCashEntry(accountTransaction, acctgTransId);

		createMemberDepositEntryAccountMsacco(accountTransaction, acctgTransId,
				memberBranchId);

		// Credit Member Branch Settlement Account
		creditMemberBranchMsaccoSettlementAccount(accountTransaction,
				acctgTransId, userLogin, memberBranchId);
		// createMemberCashEntryTeller(accountTransaction, acctgTransId,
		// userLogin);
		return acctgTransId;
	}

	private static void creditMemberBranchMsaccoSettlementAccount(
			GenericValue accountTransaction, String acctgTransId,
			Map<String, String> userLogin, String memberBranchId) {
		String glAccountId = "";
		// glAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue branch = LoanUtilities.getEntityValue("PartyGroup",
				"partyId", memberBranchId);
		glAccountId = branch.getString("msaccoSettlementAccountId");
		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", memberBranchId,
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "C",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}
	
	
	private static void creditMemberBranchATMSettlementAccount(
			GenericValue accountTransaction, String acctgTransId,
			Map<String, String> userLogin, String memberBranchId) {
		String glAccountId = "";
		// glAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue branch = LoanUtilities.getEntityValue("PartyGroup",
				"partyId", memberBranchId);
		glAccountId = branch.getString("atmSettlementAccountId");
		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", memberBranchId,
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "C",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}
	
	private static void creditMemberBranchATMSettlementAccountReversal(
			GenericValue accountTransaction, String acctgTransId,
			Map<String, String> userLogin, String memberBranchId) {
		String glAccountId = "";
		// glAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue branch = LoanUtilities.getEntityValue("PartyGroup",
				"partyId", memberBranchId);
		glAccountId = branch.getString("atmSettlementAccountId");
		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", memberBranchId,
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "D",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	private static void createMemberCashEntry(GenericValue accountTransaction,
			String acctgTransId) {

		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						accountHolderTransactionSetup
								.getString("cashAccountId"),
						"organizationPartyId", "Company", "amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "C",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}
	}

	private static void createMemberDepositEntry(
			GenericValue accountTransaction, String acctgTransId) {

		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "1", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						accountHolderTransactionSetup
								.getString("memberDepositAccId"),
						"organizationPartyId", "Company", "amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "D",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	public static GenericValue createAccountPostingEntryVer2(
			String glAccountId, String glAccountTypeId,
			String employeeBranchId, BigDecimal amount, Long memberAccountId,
			String acctgTransId, String postingType, String sequence,
			String memberBranchId) {

		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		// GenericValue memberAccount =
		// AccHolderTransactionServices.getMemberAccount(memberAccountId);

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");
		if (glAccountTypeId == null)
			glAccountTypeId = "MEMBER_DEPOSIT";

		if (employeeBranchId == null)
			employeeBranchId = "Company";

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", sequence, "partyId",
						memberBranchId, "glAccountTypeId", glAccountTypeId,
						"glAccountId", glAccountId, "organizationPartyId",
						employeeBranchId, "amount", amount, "currencyUomId",
						"KES", "origAmount", amount, "origCurrencyUomId",
						"KES", "debitCreditFlag", postingType,
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		// try {
		// delegator.createOrStore(acctgTransEntry);
		// } catch (GenericEntityException e) {
		// e.printStackTrace();
		// log.error("Could not create acctgTransEntry");
		// }

		return acctgTransEntry;

	}

	private static void createMemberDepositEntryAccountMsacco(
			GenericValue accountTransaction, String acctgTransId,
			String memberBranchId) {

		// Get the account to deposit from the account product setup
		String glAccountId = "";

		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		// GenericValue memberAccount =
		// AccHolderTransactionServices.getMemberAccount(memberAccountId);
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glAccountId = accountProduct.getString("glAccountId");

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "1", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", memberBranchId,
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "D",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}
	
	
	private static void createMemberDepositEntryAccountMsaccoReversal(
			GenericValue accountTransaction, String acctgTransId,
			String memberBranchId) {

		// Get the account to deposit from the account product setup
		String glAccountId = "";

		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		// GenericValue memberAccount =
		// AccHolderTransactionServices.getMemberAccount(memberAccountId);
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glAccountId = accountProduct.getString("glAccountId");

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "1", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", memberBranchId,
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "C",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	private static void createMemberDepositEntryAccount(
			GenericValue accountTransaction, String acctgTransId) {

		// Get the account to deposit from the account product setup
		String glAccountId = "";

		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		// GenericValue memberAccount =
		// AccHolderTransactionServices.getMemberAccount(memberAccountId);
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		glAccountId = accountProduct.getString("glAccountId");

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "1", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", "Company",
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "D",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	private static void createMemberCashEntryTeller(
			GenericValue accountTransaction, String acctgTransId,
			Map<String, String> userLogin) {

		String glAccountId = "";
		glAccountId = TreasuryUtility.getTellerAccountId(userLogin);

		// GenericValue accountHolderTransactionSetup =
		// getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId,

				"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", "Company",
						"amount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"currencyUomId", "KES", "origAmount",
						accountTransaction.getBigDecimal("transactionAmount"),
						"origCurrencyUomId", "KES", "debitCreditFlag", "C",
						"reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}
	}

	private static void createMemberDepositEntry(BigDecimal amount,
			String acctgTransId, String postingType, String employeeBranchId) {
		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", "1", "partyId",
						employeeBranchId, "glAccountTypeId", "MEMBER_DEPOSIT",
						"glAccountId", accountHolderTransactionSetup
								.getString("memberDepositAccId"),
						"organizationPartyId", employeeBranchId, "amount",
						amount, "currencyUomId", "KES", "origAmount", amount,
						"origCurrencyUomId", "KES", "debitCreditFlag",
						postingType, "reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	private static void createPayrollPostingEntry(BigDecimal amount,
			String acctgTransId, String postingType, String glAccountId,
			String sequence) {
		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", sequence, "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", "Company",
						"amount", amount, "currencyUomId", "KES", "origAmount",
						amount, "origCurrencyUomId", "KES", "debitCreditFlag",
						postingType, "reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}

	}

	private static void createMemberCashEntry(BigDecimal amount,
			String acctgTransId, String postingType, String employeeBranchId) {

		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetup("MEMBERTRANSACTIONACCOUNT");

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", "2", "partyId",
						employeeBranchId, "glAccountTypeId", "MEMBER_DEPOSIT",
						"glAccountId", accountHolderTransactionSetup
								.getString("cashAccountId"),
						"organizationPartyId", employeeBranchId, "amount",
						amount, "currencyUomId", "KES", "origAmount", amount,
						"origCurrencyUomId", "KES", "debitCreditFlag",
						postingType, "reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}
	}

	public static GenericValue getAccountHolderTransactionSetup(
			String accountHolderTransactionSetupId) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							accountHolderTransactionSetupId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		return accountHolderTransactionSetup;
	}

	// Create a record in AcctgTrans
	public static String creatAccountTransRecord(
			GenericValue accountTransactiont, Map<String, String> userLogin) {

		GenericValue acctgTrans = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String acctgTransId = delegator.getNextSeqId("AcctgTrans", 1);

		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");

		acctgTrans = delegator.makeValidValue("AcctgTrans", UtilMisc.toMap(
				"acctgTransId", acctgTransId,

				"acctgTransTypeId", "MEMBER_DEPOSIT", "transactionDate",
				new Timestamp(Calendar.getInstance().getTimeInMillis()),
				"isPosted", "Y", "postedDate", new Timestamp(Calendar
						.getInstance().getTimeInMillis()),

				"glFiscalTypeId", "ACTUAL", "partyId", "Company",
				"createdDate", new Timestamp(Calendar.getInstance()
						.getTimeInMillis()), "createdByUserLogin", createdBy,
				"lastModifiedDate", new Timestamp(Calendar.getInstance()
						.getTimeInMillis()), "lastModifiedByUserLogin",
				updatedBy));
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTrans");
		}
		return acctgTransId;
	}

	public static String creatAccountTransRecordVer2(
			GenericValue accountTransaction, Map<String, String> userLogin) {

		GenericValue acctgTrans = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String acctgTransId = delegator.getNextSeqId("AcctgTrans", 1);

		String memberBranchId = "";

		if (userLogin != null) {
			memberBranchId = LoanUtilities.getMemberBranchId(userLogin
					.get("partyId"));
		}

		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
			userLogin.put("partyId", "Company");
			memberBranchId = "Company";
		}
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");

		acctgTrans = delegator.makeValidValue("AcctgTrans", UtilMisc.toMap(
				"acctgTransId", acctgTransId,

				"acctgTransTypeId", "MEMBER_DEPOSIT", "transactionDate",
				new Timestamp(Calendar.getInstance().getTimeInMillis()),
				"isPosted", "Y", "postedDate", new Timestamp(Calendar
						.getInstance().getTimeInMillis()),

				"glFiscalTypeId", "ACTUAL", "partyId", memberBranchId,
				"createdDate", new Timestamp(Calendar.getInstance()
						.getTimeInMillis()), "createdByUserLogin", createdBy,
				"lastModifiedDate", new Timestamp(Calendar.getInstance()
						.getTimeInMillis()), "lastModifiedByUserLogin",
				updatedBy));
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTrans");
		}
		return acctgTransId;
	}

	private static GenericValue createAccountTransactionParent(
			GenericValue accounTransaction, Map<String, String> userLogin) {
		GenericValue transactionParent;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String accountTransactionParentId = delegator
				.getNextSeqId("AccountTransactionParent");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		// String branchId = getEmployeeBranch((String)
		// userLogin.get("partyId"));
		// String partyId = loanApplication.getString("partyId");

		transactionParent = delegator
				.makeValidValue("AccountTransactionParent", UtilMisc.toMap(
						"accountTransactionParentId",
						accountTransactionParentId, "isActive", "Y",
						"createdBy", createdBy, "updatedBy", updatedBy,
						"memberAccountId",
						accounTransaction.getLong("memberAccountId"),
						"approved", "NO", "rejected", "NO", "posted", "posted"));
		try {
			delegator.createOrStore(transactionParent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction Parent");
		}

		return transactionParent;
	}

	public static BigDecimal getChargesTotal(Long memberAccountId,
			BigDecimal baseAmount, String transactionType) {
		BigDecimal bdTotalChargeAmount = BigDecimal.ZERO;
		// Get the Product by first accessing the MemberAccount
		String accountProductId = getAccountProduct(memberAccountId);

		// Get the Charges for the Product
		List<GenericValue> accountProductChargeELI = null;
		accountProductChargeELI = getAccountProductCharges(accountProductId,
				transactionType);
		log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "
				+ accountProductChargeELI.size());
		// Create a transaction in Account Transaction for each of the Charges
		for (GenericValue accountProductCharge : accountProductChargeELI) {
			// addCharge(accountProductCharge, accountTransaction, userLogin);
			bdTotalChargeAmount = bdTotalChargeAmount.add(getCharge(
					accountProductCharge, baseAmount));
		}
		// Create an Account Transaction for each of the Charges

		return bdTotalChargeAmount;
	}

	private static String getAccountProduct(Long memberAccountId) {

		// String memberAccountId = String.valueOf(memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// memberAccountId = memberAccountId.replaceAll(",", "");
		GenericValue memberAccount = null;
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get AccountProduct
		String accountProductId = memberAccount.getString("accountProductId");
		return accountProductId;
	}

	private static Long getAccountProductFromLoanProduct(Long loanProductId) {

		// String memberAccountId = String.valueOf(memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// memberAccountId = memberAccountId.replaceAll(",", "");
		GenericValue loanProduct = null;
		try {
			loanProduct = delegator.findOne("LoanProduct",
					UtilMisc.toMap("loanProductId", loanProductId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get AccountProduct
		Long accountProductId = loanProduct.getLong("accountProductId");
		return accountProductId;
	}

	private static GenericValue getAccountProductEntity(Long memberAccountId) {

		// String memberAccountId = String.valueOf(memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// memberAccountId = memberAccountId.replaceAll(",", "");
		GenericValue memberAccount = null;
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get AccountProduct
		Long accountProductId = memberAccount.getLong("accountProductId");

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

	private static List<GenericValue> getAccountProductCharges(
			String accountProductId, String transactionType) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						Long.valueOf(accountProductId)), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, transactionType)),
						EntityOperator.AND);
		List<GenericValue> accountProductChargeELI = null;
		try {
			accountProductChargeELI = delegator.findList(
					"AccountProductCharge", accountChargeConditions, null,
					null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if (accountProductChargeELI == null) {
			// result.put("", "No Member Accounts");
			log.info(" ######### The Account Has no Charges #########");
		}
		return accountProductChargeELI;
	}

	private static BigDecimal getCharge(GenericValue accountProductCharge,
			BigDecimal baseAmount) {
		// Add Account Transaction
		BigDecimal bdChargeAmount;
		bdChargeAmount = getChargeAmount(accountProductCharge, baseAmount);
		return bdChargeAmount;
	}

	private static BigDecimal getChargeAmount(
			GenericValue accountProductCharge, BigDecimal baseAmount) {
		String strFixed = accountProductCharge.getString("isFixed");
		BigDecimal bdChargeAmount;
		String isPercentageOfOtherCharge;
		BigDecimal bdTransactionAmount = baseAmount;
		// Delegator delegator = accountProductCharge.getDelegator();
		if (strFixed.equals("Y")) {
			bdChargeAmount = accountProductCharge.getBigDecimal("fixedAmount");
		} else {
			// Its not fixed
			// It is a rate/percentage
			// Can either be the rate of parent charge or the rate of the
			// transaction
			isPercentageOfOtherCharge = accountProductCharge
					.getString("isPercentageOfOtherCharge");
			if (isPercentageOfOtherCharge.equals("Y")) {
				// Get the value of the charge
				GenericValue parentAccountCharge = getAccoutCharge(
						accountProductCharge.getString("parentChargeId"),
						accountProductCharge);
				BigDecimal bdParentAmount;
				if (parentAccountCharge.getString("isFixed").equals("Y")) {
					bdParentAmount = parentAccountCharge
							.getBigDecimal("fixedAmount");
				} else {
					bdParentAmount = parentAccountCharge
							.getBigDecimal("rateAmount")
							.setScale(6, RoundingMode.HALF_UP)
							.multiply(
									bdTransactionAmount.setScale(6,
											RoundingMode.HALF_UP))
							.divide(new BigDecimal(100), 6,
									RoundingMode.HALF_UP);
				}

				bdChargeAmount = bdParentAmount
						.setScale(6, RoundingMode.HALF_UP)
						.multiply(
								accountProductCharge
										.getBigDecimal("rateAmount").setScale(
												6, RoundingMode.HALF_UP))
						.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);

			} else {
				bdChargeAmount = accountProductCharge
						.getBigDecimal("rateAmount")
						.setScale(6, RoundingMode.HALF_UP)
						.multiply(
								bdTransactionAmount.setScale(6,
										RoundingMode.HALF_UP))
						.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP);
			}
		}
		return bdChargeAmount;
	}

	/**
	 * Get Minimum Balance
	 * */
	public static BigDecimal getMinimumBalance(Long memberAccountId) {
		BigDecimal bdMinimumBalance = BigDecimal.ZERO;
		String accountProductId = getAccountProduct(memberAccountId);
		accountProductId = accountProductId.replaceAll(",", "");
		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator.findOne(
					"AccountProduct",
					UtilMisc.toMap("accountProductId",
							Long.valueOf(accountProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (accountProduct != null) {
			if (accountProduct.getBigDecimal("minBalanceAmt") != null) {
				bdMinimumBalance = accountProduct
						.getBigDecimal("minBalanceAmt");
			}
		}

		return bdMinimumBalance;
	}

	public static String getAccountProductName(Long memberAccountId) {
		String accountProductId = getAccountProduct(memberAccountId);
		accountProductId = accountProductId.replaceAll(",", "");
		GenericValue accountProduct = null;
		String accountProductName = "";
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProduct = delegator.findOne(
					"AccountProduct",
					UtilMisc.toMap("accountProductId",
							Long.valueOf(accountProductId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (accountProduct != null) {
			if (accountProduct.getString("name") != null) {
				accountProductName = accountProduct.getString("name");
			}
		}

		return accountProductName;
	}

	public static String removeCommas(Long partyId) {
		String id = partyId.toString().replace(",", "");
		return id;
	}

	public static GenericValue getMemberAccount(Long memberAccountId) {
		GenericValue memberAccount = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", memberAccountId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Could not get member account ");
		}

		return memberAccount;
	}

	public static String getMemberNames(Long partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Get Member
		GenericValue member = null;
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String memberNames = "";

		memberNames = member.getString("firstName") + " "
				+ member.getString("middleName") + " "
				+ member.getString("lastName");
		return memberNames;
	}

	public static String cashDeposit(BigDecimal transactionAmount,
			Long memberAccountId, Map<String, String> userLogin,
			String withdrawalType) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease
		String acctgTransId = postCashDeposit(memberAccountId, userLogin,
				transactionAmount);
		GenericValue accountTransaction = null;
		createTransaction(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String cashDepositVersion4(BigDecimal transactionAmount,
			Long memberAccountId, Map<String, String> userLogin,
			String withdrawalType, String acctgTransId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		createTransactionVersion4(accountTransaction, transactionType,
				userLogin, memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);
		// postCashDeposit(memberAccountId, userLogin, transactionAmount);
		// postCashWithdrawalTransaction(accountTransaction, userLogin);
		return accountTransactionParent.getString("accountTransactionParentId");
	}

	/***
	 * Cash Deposit From Station processing
	 * 
	 * Does not post to the GL
	 * **/
	public static String cashDepositFromStationProcessing(
			BigDecimal transactionAmount, Long memberAccountId,
			Map<String, String> userLogin, String withdrawalType,
			String acctgTransId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		accountTransaction = createTransactionVer2(transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// createTransaction(accountTransaction, transactionType, userLogin,
		// memberAccountId.toString(), transactionAmount, null,
		// accountTransactionParent.getString("accountTransactionParentId"));
		// postCashDeposit(memberAccountId, userLogin, transactionAmount);
		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String memberAccountJournalVoucher(
			BigDecimal transactionAmount, Long memberAccountId,
			Map<String, String> userLogin, String transactionType,
			Long memberAccountVoucherId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		// String acctgTransId = postCashDeposit(memberAccountId, userLogin,
		// transactionAmount);

		// GenericValue, String, Map<String,String>, String, BigDecimal, String,
		// String, String
		// GenericValue, String, Map<String,String>, String, BigDecimal, null,
		// String

		createTransactionVersion2(accountTransaction, transactionType,
				userLogin, memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"),
				memberAccountVoucherId);

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	public static String memberAccountJournalVoucher(
			BigDecimal transactionAmount, Long memberAccountId,
			Map<String, String> userLogin, String transactionType,
			Long memberAccountVoucherId, String acctgTransId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		// String acctgTransId = postCashDeposit(memberAccountId, userLogin,
		// transactionAmount);

		// GenericValue, String, Map<String,String>, String, BigDecimal, String,
		// String, String
		// GenericValue, String, Map<String,String>, String, BigDecimal, null,
		// String

		createTransaction(null, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId,
				null, null);
		// createTransactionVersion2(accountTransaction, transactionType,
		// userLogin, memberAccountId.toString(), transactionAmount, null,
		// accountTransactionParent
		// .getString("accountTransactionParentId"),
		// memberAccountVoucherId);

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	private static String postCashDeposit(Long memberAccountId,
			Map<String, String> userLogin, BigDecimal amount) {
		// ..
		GenericValue accountTransaction = null;
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyUd"));
		createMemberDepositEntry(amount, acctgTransId, "C", employeeBranchId);
		createMemberCashEntry(amount, acctgTransId, "D", employeeBranchId);
		return acctgTransId;
	}

	/****
	 * @author Japheth Odonya @when Jun 9, 2015 11:49:12 PM
	 * 
	 *         Posting HQ Salaries
	 * 
	 * */
	public static void postPayrollSalariesHQ(Map<String, String> userLogin,

	BigDecimal bdNSSF, BigDecimal bdNHIF, BigDecimal bdPENSION,
			BigDecimal bdPAYE, BigDecimal bdNETPAY, BigDecimal bdSalaries,

			String NSSFAccountId, String NHIFAccountId,
			String PENSIONAccountId, String PAYEAccountId,
			String NETPAYAccountId, String SalariesAccountId) {
		// ..

		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		GenericValue accountTransaction = null;
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);

		// createMemberDepositEntry(amount, acctgTransId, "C");
		// createMemberCashEntry(amount, acctgTransId, "D");

		String sequence = "00001";
		// Debit NSSF
		createPayrollPostingEntry(bdNSSF, acctgTransId, "C", NSSFAccountId,
				sequence);
		// Debit NHIF
		sequence = "00002";
		createPayrollPostingEntry(bdNHIF, acctgTransId, "C", NHIFAccountId,
				sequence);
		// Debit PENSION
		sequence = "00003";
		createPayrollPostingEntry(bdPENSION, acctgTransId, "C",
				PENSIONAccountId, sequence);
		// Debit PAYE
		sequence = "00004";
		createPayrollPostingEntry(bdPAYE, acctgTransId, "C", PAYEAccountId,
				sequence);

		// Debit NEYPAY
		sequence = "00005";
		createPayrollPostingEntry(bdNETPAY, acctgTransId, "C", NETPAYAccountId,
				sequence);

		// Credit Salaries
		sequence = "00006";
		createPayrollPostingEntry(bdSalaries, acctgTransId, "D",
				SalariesAccountId, sequence);

	}

	/****
	 * @author Japheth Odonya @when Jun 11, 2015 11:21:17 PM
	 * 
	 *         Posting the APPLIED CARD
	 * */

	// bdTotalCharge
	// bdChargeAmount
	// bdExciseDuty

	// glAccountId
	// chargeAccountId
	// exciseDutyAccountId

	public static String postCardApplicationFee(Map<String, String> userLogin,

	BigDecimal bdTotalCharge, BigDecimal bdChargeAmount,
			BigDecimal bdExciseDuty,

			String glAccountId, String chargeAccountId,
			String exciseDutyAccountId) {
		// ..

		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		GenericValue accountTransaction = null;
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);

		// createMemberDepositEntry(amount, acctgTransId, "C");
		// createMemberCashEntry(amount, acctgTransId, "D");

		// bdTotalCharge
		// bdChargeAmount
		// bdExciseDuty

		// glAccountId
		// chargeAccountId
		// exciseDutyAccountId
		String sequence = "00001";
		// Debit glAccountId / member deposits/ savings
		createPayrollPostingEntry(bdTotalCharge, acctgTransId, "D",
				glAccountId, sequence);
		// Debit NHIF
		sequence = "00002";
		createPayrollPostingEntry(bdChargeAmount, acctgTransId, "C",
				chargeAccountId, sequence);
		// Debit PENSION
		sequence = "00003";
		createPayrollPostingEntry(bdExciseDuty, acctgTransId, "C",
				exciseDutyAccountId, sequence);

		return acctgTransId;
	}

	/***
	 * @deprecated Will use it when we need to post the member account reversals
	 * */
	private static String postMemberAccountTransaction(Long memberAccountId,
			Map<String, String> userLogin, BigDecimal amount) {
		// ..
		GenericValue accountTransaction = null;
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);

		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));

		createMemberDepositEntry(amount, acctgTransId, "C", employeeBranchId);
		createMemberCashEntry(amount, acctgTransId, "D", employeeBranchId);

		return acctgTransId;
	}

	private static GenericValue createAccountTransactionParent(
			Long memberAccountId, Map<String, String> userLogin) {
		GenericValue transactionParent;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String accountTransactionParentId = delegator
				.getNextSeqId("AccountTransactionParent");
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		// String branchId = getEmployeeBranch((String)
		// userLogin.get("partyId"));
		// String partyId = loanApplication.getString("partyId");

		transactionParent = delegator.makeValidValue(
				"AccountTransactionParent", UtilMisc.toMap(
						"accountTransactionParentId",
						accountTransactionParentId, "isActive", "Y",
						"createdBy", createdBy, "updatedBy", updatedBy,
						"memberAccountId", memberAccountId, "approved", "NO",
						"rejected", "NO", "posted", "posted"));
		try {
			delegator.createOrStore(transactionParent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction Parent");
		}

		return transactionParent;
	}

	public static String getcreateAccountTransactionParentId(
			Long memberAccountId, Map<String, String> userLogin) {
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);
		String accountTransactionParentId = accountTransactionParent
				.getString("accountTransactionParentId");
		return accountTransactionParentId;
	}

	/****
	 * Reverse Transaction
	 * */
	public static String reverseTransaction(String acctgTransId ,
			Map<String, String> userLogin) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String createdBy = userLogin.get("userLoginId");
		// Get all the account transactions under parent and set their
		// increase/decrease to R
		log.info(" TRansaction ID AAAAAAAAAAAAAA "+acctgTransId);
		// GenericValue userLogin = (GenericValue) request
		// .getAttribute("userLogin");

		// Get all the Cheque Deposit Transactions that are Unposted and Cleared
		// then Post each one of them
		List<GenericValue> accountTransactionELI = null;

		// String chequeDepostTransaction = "CHEQUEDEPOSIT";
		// Timestamp currentDate = new Timestamp(Calendar.getInstance()
		// .getTimeInMillis());

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"acctgTransId", EntityOperator.EQUALS, acctgTransId)),
						EntityOperator.AND);

		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Reverse MPA Records
		String newacctgTransId = creatAccountTransRecord(null, userLogin);

		String accountTransactionId = null;
		for (GenericValue accountTransaction : accountTransactionELI) {
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
			accountTransactionId = delegator.getNextSeqId("AccountTransaction");

			accountTransaction.setString("accountTransactionId",
					accountTransactionId);

			if (accountTransaction.getString("increaseDecrease").equals("I")) {
				accountTransaction.setString("increaseDecrease", "D");
			} else {
				accountTransaction.setString("increaseDecrease", "I");
			}

			accountTransaction.set("transactionType",
					getTransactionTypeReversalName(accountTransaction
							.getString("transactionType")));
			accountTransaction.set("createdBy", createdBy);

			String originalAccountTransactionId = accountTransaction
					.getString("accountTransactionId");

			accountTransaction.setString("originalAcctgTransId", acctgTransId);
			accountTransaction.setString("originalAccountTransactionId",
					originalAccountTransactionId);
			accountTransaction.setString("acctgTransId", newacctgTransId);
			// Get new Accounting Transaction ID
			try {
				delegator.createOrStore(accountTransaction);
			} catch (GenericEntityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}

		}

		// Reverse GL Postings
		// Now post the reversal in the GL
		List<GenericValue> acctgTransEntryELI = null;
		try {
			acctgTransEntryELI = delegator
					.findList("AcctgTransEntry", EntityCondition.makeCondition(
							"acctgTransId", acctgTransId), null, null, null,
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the acctgTransEntry
		for (GenericValue genericValue : acctgTransEntryELI) {

			if (genericValue.getString("debitCreditFlag").equals("C")) {
				genericValue.setString("debitCreditFlag", "D");
			} else {
				genericValue.setString("debitCreditFlag", "C");
			}
			genericValue.setString("acctgTransId", newacctgTransId);
			//genericValue.setString("createdBy", createdBy);

			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return "success";
	}

	private static Object getTransactionTypeReversalName(String transactionType) {
		if (transactionType.equals("LOANREPAYMENT")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("MSACCOWITHDRAWAL")) {
			return transactionType + "REVERSED";
		}

		else if (transactionType.equals("DEPOSITFROMSALARY")) {
			return transactionType + "REVERSED";
		}

		else if (transactionType.equals("TRANSFERFROM")) {
			return transactionType + "REVERSED";
		}

		else if (transactionType.equals("SALARYPROCESSING")) {
			return transactionType + "REVERSED";
		}

		else if (transactionType.equals("CHEQUEDEPOSIT")) {
			return transactionType + "REVERSED";
		}

		else if (transactionType.equals("TOOTHERACCOUNTS")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("LOANDISBURSEMENT")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("EXCISEDUTY")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("LOANCASHPAY")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("CASHDEPOSIT")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("CASHWITHDRAWAL")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("MEMBERACCOUNTJVDEC")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("DEPOSITFROMEXCESS")) {
			return transactionType + "REVERSED";
		} else if (transactionType.equals("TRANSFERTO")) {
			return transactionType + "REVERSED";
		}

		else {
			return transactionType + " Reversed";
		}
		// return null;
	}

	public static List<DeductionItem> getDeductions(String payrollNo) {
		List<DeductionItem> listDeducionItems = new ArrayList<DeductionItem>();
		DeductionItem deductionItem = null;
		String code;

		// Get partyId
		Long partyId = LoanUtilities.getMemberId(payrollNo);

		// Get Loan Application IDs
		List<Long> listLoanApplicationIds = LoanUtilities
				.getLoanApplicationIds(partyId);

		GenericValue loanApplication = null;
		for (Long loanApplicationId : listLoanApplicationIds) {
			// Create Deduction Item List
			loanApplication = LoanUtilities
					.getLoanApplicationEntity(loanApplicationId);

			// Add Principal
			deductionItem = new DeductionItem();
			code = LoanUtilities.getLoanProductCode(loanApplication
					.getLong("loanProductId"));

			deductionItem.setCode(code + "A");

			BigDecimal bdAmount = LoanRepayments.getTotalPrincipalDue(
					partyId.toString(), loanApplicationId.toString());
			deductionItem.setBdAmount(bdAmount);

			BigDecimal bdBalance = loanApplication
					.getBigDecimal("loanAmt")
					.subtract(
							LoanServices
									.getLoansRepaidByLoanApplicationId(loanApplicationId));
			bdBalance = bdBalance.subtract(bdAmount);

			deductionItem.setBdBalance(bdBalance);

			deductionItem.setDeductionDate(new Timestamp(Calendar.getInstance()
					.getTimeInMillis()));

			listDeducionItems.add(deductionItem);

			// Add Interest
			deductionItem.setCode(code + "B");

			bdAmount = LoanRepayments.getTotalInterestDue(partyId.toString(),
					loanApplicationId.toString());
			deductionItem.setBdAmount(bdAmount);

			deductionItem.setBdBalance(BigDecimal.ZERO);

			deductionItem.setDeductionDate(new Timestamp(Calendar.getInstance()
					.getTimeInMillis()));

			listDeducionItems.add(deductionItem);

			// Add Insurance
			deductionItem.setCode(code + "C");

			bdAmount = LoanRepayments.getTotalInsuranceDue(partyId.toString(),
					loanApplicationId.toString());
			deductionItem.setBdAmount(bdAmount);

			deductionItem.setBdBalance(BigDecimal.ZERO);

			deductionItem.setDeductionDate(new Timestamp(Calendar.getInstance()
					.getTimeInMillis()));

			listDeducionItems.add(deductionItem);

		}

		return listDeducionItems;
	}

	/****
	 * No Posting to the GL at this stage
	 * 
	 * */
	public static String memberTransactionDeposittt(
			BigDecimal transactionAmount, Long memberAccountId,
			Map<String, String> userLogin, String withdrawalType,
			String accountTransactionParentId, String productChargeId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		if (accountTransactionParentId == null) {
			GenericValue accountTransactionParent = createAccountTransactionParent(
					memberAccountId, userLogin);
			accountTransactionParentId = accountTransactionParent
					.getString("accountTransactionParentId");
		}
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		// createTransaction(accountTransaction, transactionType, userLogin,
		// memberAccountId.toString(), transactionAmount, productChargeId,
		// accountTransactionParentId);
		// postCashDeposit(memberAccountId, userLogin, transactionAmount);
		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParentId;
	}

	// acctgTransId
	/****
	 * Overloaded the memberTransactionDeposit to add acctgTransId to the
	 * transaction
	 * */
	public static String memberTransactionDeposit(BigDecimal transactionAmount,
			Long memberAccountId, Map<String, String> userLogin,
			String withdrawalType, String accountTransactionParentId,
			String productChargeId, String acctgTransId, Long accountProductId,
			Long loanApplicationId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		if (accountTransactionParentId == null) {
			GenericValue accountTransactionParent = createAccountTransactionParent(
					memberAccountId, userLogin);
			accountTransactionParentId = accountTransactionParent
					.getString("accountTransactionParentId");
		}
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		createTransaction(accountTransaction, transactionType, userLogin,
				memberAccountId.toString(), transactionAmount, productChargeId,
				accountTransactionParentId, acctgTransId, accountProductId,
				loanApplicationId);
		// postCashDeposit(memberAccountId, userLogin, transactionAmount);
		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParentId;
	}

	public static Long getMemberSavingsAccountId(String payrollNumber) {
		// TODO Auto-generated method stub
		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenCodeId(SAVINGS_ACCOUNT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");

		GenericValue member = RemittanceServices
				.getMemberByPayrollNo(payrollNumber);
		Long partyId = member.getLong("partyId");

		GenericValue memberAccount = null;

		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductId),

				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,
						partyId)

				), EntityOperator.AND);

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		Long memberAccountId = null;

		if (memberAccount != null)
			memberAccountId = memberAccount.getLong("memberAccountId");

		return memberAccountId;
	}

	private static void postSalaryProcessing(Long memberAccountId,
			Map<String, String> userLogin, BigDecimal amount) {
		// ..
		GenericValue accountTransaction = null;
		String acctgTransId = creatAccountTransRecord(accountTransaction,
				userLogin);
		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		createMemberDepositEntry(amount, acctgTransId, "C", employeeBranchId);
		createMemberCashEntry(amount, acctgTransId, "D", employeeBranchId);

	}

	/***
	 * Post Entry
	 * */
	public static void createAccountPostingEntry(BigDecimal amount,
			String acctgTransId, String postingType, String glAccountId) {

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", "2", "partyId", "Company",
						"glAccountTypeId", "MEMBER_DEPOSIT", "glAccountId",
						glAccountId, "organizationPartyId", "Company",
						"amount", amount, "currencyUomId", "KES", "origAmount",
						amount, "origCurrencyUomId", "KES", "debitCreditFlag",
						postingType, "reconcileStatusId", "AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}
	}

	public static void createAccountPostingEntry(BigDecimal amount,
			String acctgTransId, String postingType, String glAccountId,
			String entrySequence, String branchId) {

		GenericValue acctgTransEntry = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId,

						"acctgTransEntrySeqId", entrySequence, "partyId",
						branchId, "glAccountTypeId", "MEMBER_DEPOSIT",
						"glAccountId", glAccountId, "organizationPartyId",
						branchId, "amount", amount, "currencyUomId", "KES",
						"origAmount", amount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create acctgTransEntry");
		}
	}

	/***
	 * CommissionCharge
	 * */

	public static Boolean commissionChargeIsSetup() {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> commissionChargeELI = null;
		try {
			commissionChargeELI = delegator.findList("CommissionCharge", null,
					null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((commissionChargeELI != null) && (commissionChargeELI.size() > 0))
			return true;

		return false;
	}

	/****
	 * @author Japheth Odonya @when Jun 28, 2015 1:10:58 PM
	 * 
	 *         Bank Account is setup
	 * **/
	public static Boolean checkBankGLAccount(GenericValue accountTransaction,
			Map<String, String> userLogin) {

		// Get the finAccountId
		String finAccountId = accountTransaction.getString("finAccountId");
		log.info("BBBBBBBBBBBBBBBB The Finacial ID  is BBBBBBBB "
				+ finAccountId);
		String bankglAccountId = LoanUtilities.getBankglAccountId(finAccountId);
		log.info("BBBBBBBBBBBBBBBB The Bank GL Account ID  is BBBBBBBB "
				+ bankglAccountId);
		if (bankglAccountId == null)
			return false;

		String branchId = getBranch(userLogin.get("partyId"));

		log.info("BBBBBBBBBBBBBBBB The Branch is BBBBBBBB " + branchId);

		return LoanUtilities.organizationAccountMapped(bankglAccountId,
				branchId);

	}

	/***
	 * Check that the ledger account has been specified
	 * */
	public static Boolean legerAccountSetUp(GenericValue accountTransaction) {

		// Get the member account ID
		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		if (accountProduct == null)
			return false;

		String glAccountId = accountProduct.getString("glAccountId");

		if ((glAccountId != null) && (!glAccountId.equals("")))
			return true;

		return false;
	}

	public static Boolean commissionAccountSetUp(GenericValue accountTransaction) {

		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		if (accountProduct == null)
			return false;

		String commissionAccountId = accountProduct
				.getString("commissionAccountId");

		if ((commissionAccountId != null) && (!commissionAccountId.equals("")))
			return true;

		return false;
	}

	public static Boolean exciseDutyAccountSetUp(GenericValue accountTransaction) {

		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		if (accountProduct == null)
			return false;

		String exciseDutyAccountId = accountProduct
				.getString("exciseDutyAccountId");

		if ((exciseDutyAccountId != null) && (!exciseDutyAccountId.equals("")))
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when Apr 23, 2015 12:31:37 PM Check that Excise
	 *         Duty has been setup - at least we know what the rate is
	 * */
	public static Boolean exciseDutyRateSetUp() {
		// ExciseDuty
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> branchesELI = null;
		try {
			branchesELI = delegator.findList("ExciseDuty", null, null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((branchesELI != null) && (branchesELI.size() > 0))
			return true;

		return false;
	}

	public static Boolean legerAccountMappedToMemberBranch(
			GenericValue accountTransaction) {

		Long memberAccountId = accountTransaction.getLong("memberAccountId");

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		if (accountProduct == null)
			return false;

		return false;
	}

	public static BigDecimal getAvailableBalanceAfterCurrentTransactionVer3(
			String memberAccountId, BigDecimal transactionAmount) {

		BigDecimal bdBalanceAmount = null;

		Timestamp balanceDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		bdBalanceAmount = getAvailableBalanceVer3(memberAccountId, balanceDate);

		BigDecimal bdCommissionAmount = getTransactionCommissionAmount(transactionAmount);
		BigDecimal bdExciseDutyAmount = getTransactionExcideDutyAmount(bdCommissionAmount);

		BigDecimal bdTotalAmount = transactionAmount.add(bdCommissionAmount)
				.add(bdExciseDutyAmount);

		bdBalanceAmount = bdBalanceAmount.subtract(bdTotalAmount);

		return bdBalanceAmount;
	}

	public static BigDecimal getAvailableBalanceVer3(String memberAccountId) {

		BigDecimal bdBalanceAmount = null;

		Timestamp balanceDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		bdBalanceAmount = getAvailableBalanceVer3(memberAccountId, balanceDate);

		return bdBalanceAmount;
	}

	/***
	 * Return verdict - either balance is enough or its not
	 * */
	public static String isTellerBalanceEnough(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();

		String treasuryId = (String) request.getParameter("treasuryId");

		Long memberAccountId = Long.valueOf(request
				.getParameter("memberAccountId"));

		BigDecimal transactionAmount = new BigDecimal(
				request.getParameter("transactionAmount"));

		Boolean isSufficent = TreasuryUtility.tellerBalanceSufficient(
				treasuryId, memberAccountId, transactionAmount);

		result.put("TELLERBALANCEENOUGH", isSufficent);

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

	/****
	 * Check teller
	 * 
	 * */
	// HttpServletRequest request, HttpServletResponse response
	public static synchronized String checkTellerLimitOnDeposit(
			HttpServletRequest request, HttpServletResponse response) {

		HttpSession session;
		session = request.getSession();
		Map<String, String> userLogin = (Map<String, String>) session
				.getAttribute("userLogin");

		// request
		// .getAttribute("userLogin");
		// if (session.getAttribute(userLogin.get("userLoginId")) == null)
		// {
		// log.info(" LLLLLLLLLLL Yet to start Processing LLLLLLLLLLLLL, will set startedProcessing and start ...the user is "+userLogin.get("userLoginId"));
		// session.setAttribute(userLogin.get("userLoginId"), true);
		// } else {
		// log.info(" SSSSSSSSSSS Started Process , wont do processing the user is "+userLogin.get("userLoginId"));
		//
		// }
		Map<String, Object> result = FastMap.newInstance();

		// LoanUtilities.get

		String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// (String) request.getParameter("treasuryId");

		// Long memberAccountId = Long.valueOf(request
		// .getParameter("memberAccountId"));

		BigDecimal transactionAmount = new BigDecimal(
				request.getParameter("transactionAmount"));

		log.info(" TTTTTTT Transaction Amount ---- " + transactionAmount);
		log.info(" TTTTTTT Treasury ID ---- " + treasuryId);

		Boolean tellerOverLimit = TreasuryUtility.isTellerOverLimit(userLogin,
				treasuryId, transactionAmount);

		result.put("TELLEROVERLIMIT", tellerOverLimit);

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

	/***
	 * Get GL Account ID
	 * 
	 * */
	public static String getGlAccount(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		String memberAccountId = (String) request
				.getParameter("memberAccountId");
		log.info(" ######### The Member Account is #########" + memberAccountId);
		memberAccountId = memberAccountId.replaceAll(",", "");

		String glAccountId = null;

		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenMemberAccountId(Long
						.valueOf(memberAccountId));

		glAccountId = accountProduct.getString("glAccountId");
		result.put("glAccountId", glAccountId);
		log.info("TTTTTTTTTTTTT The GL Account is --- " + glAccountId);

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
				e1.printStackTrace();
			}
		}
		return json;
	}

	/****
	 * @author Japheth Odonya @when Jun 8, 2015 12:37:38 PM
	 * 
	 *         getGlLoanAccount
	 * 
	 *         PRINCIPAL INTERESTCHARGE INTERESTPAID INSURANCECHARGE
	 *         INSURANCEPAYMENT
	 * 
	 * */
	public static String getGlLoanAccount(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		String sourceType = (String) request.getParameter("sourceType");
		log.info(" ######### The sourceType is #########" + sourceType);
		// memberAccountId = memberAccountId.replaceAll(",", "");

		String glAccountId = null;
		String accountType = "";
		if (sourceType.equals("PRINCIPAL")) {
			accountType = "PRINCIPALPAYMENT";
			// glAccountId = getGLAccount

		} else if (sourceType.equals("INTERESTCHARGE")) {
			accountType = "INTERESTACCRUAL";
		} else if (sourceType.equals("INTERESTPAID")) {
			accountType = "INTERESTPAYMENT";
		} else if (sourceType.equals("INSURANCECHARGE")) {
			accountType = "INSURANCEACCRUAL";
		} else if (sourceType.equals("INSURANCEPAYMENT")) {
			accountType = "INSURANCEPAYMENT";
		}

		GenericValue accountHolderTransactionSetup = getAccountHolderTransactionSetup(accountType);
		// glAccountId = accountHolderTransactionSetup.getString(name)

		glAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		// GenericValue accountProduct =
		// LoanUtilities.getAccountProductGivenMemberAccountId(Long.valueOf(memberAccountId));

		// glAccountId = accountProduct.getString("glAccountId");
		result.put("glAccountId", glAccountId);
		log.info("TTTTTTTTTTTTT The GL Account is --- " + glAccountId);

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
				e1.printStackTrace();
			}
		}
		return json;
	}

	/****
	 * @author Japheth Odonya @when Jun 8, 2015 3:43:24 PM
	 * 
	 *         General Member Voucher
	 * */
	public static String generalMemberVoucher(BigDecimal transactionAmount,
			Long memberAccountId, Map<String, String> userLogin,
			String transactionType, Long generalMemberVoucherId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + memberAccountId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParent(
				memberAccountId, userLogin);

		// Set the the Treasury ID
		// String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease

		GenericValue accountTransaction = null;
		// String acctgTransId = postCashDeposit(memberAccountId, userLogin,
		// transactionAmount);

		// GenericValue, String, Map<String,String>, String, BigDecimal, String,
		// String, String
		// GenericValue, String, Map<String,String>, String, BigDecimal, null,
		// String

		createTransactionGeneralVoucherVersion(accountTransaction,
				transactionType, userLogin, memberAccountId.toString(),
				transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"),
				generalMemberVoucherId);

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	private static void createTransactionGeneralVoucherVersion(
			GenericValue loanApplication, String transactionType,
			Map<String, String> userLogin, String memberAccountId,
			BigDecimal transactionAmount, String productChargeId,
			String accountTransactionParentId, Long generalMemberVoucherId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		String partyId = getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}
		if (memberAccountId != null) {
			memberAccountId = memberAccountId.replaceAll(",", "");
			memberAccountIdLong = Long.valueOf(memberAccountId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		String treasuryId = null;

		if (loanApplication != null)
			treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountIdLong,
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "generalMemberVoucherId",
						generalMemberVoucherId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/****
	 * @author Japheth Odonya @when Jun 24, 2015 12:56:43 AM
	 * 
	 *         Check Added Status
	 * 
	 * */
	public static String checkTransactionTypeAdded(Long memberAccountId,
			String transactionType) {
		String addedStatus = "added";

		GenericValue accountProduct = LoanUtilities
				.getAccountProductGivenMemberAccountId(memberAccountId);

		Long accountProductId = accountProduct.getLong("accountProductId");
		// CHEQUEWITHDRAWAL BANKERSWITHDRAWAL
		// Check that there are record for CHEQUEWITHDRAWAL and
		// BANKERSWITHDRAWAL

		if (!LoanUtilities.existsCharges("CHEQUEWITHDRAWAL", accountProductId)) {
			return "Please set up charges for Cheque Withdrawal first !";
		}

		if (!LoanUtilities.existsCharges("BANKERSWITHDRAWAL", accountProductId)) {
			return "Please set up charges for Bankers Cheque Withdrawal first !";
		}

		return addedStatus;
	}

	/***
	 * @author Japheth Odonya @when Jun 30, 2015 3:58:41 PM Create a transfer
	 *         from Source Member Account to Destination Member Account
	 * **/
	public static void accountTransferTransaction(String sourceMemberAccountId,
			String destinationMemberAccountId,
			BigDecimal bdShareCapitalDeficit, Map<String, String> userLogin) {

		String sourceglLedgerAccountId = null;
		String destglLedgerAccountId = null;
		// Long memberAccountId = accountTransaction.getLong("memberAccountId");
		GenericValue sourceAccountProduct = getAccountProductEntity(Long
				.valueOf(sourceMemberAccountId));
		sourceglLedgerAccountId = sourceAccountProduct.getString("glAccountId");

		GenericValue destAccountProduct = getAccountProductEntity(Long
				.valueOf(destinationMemberAccountId));
		destglLedgerAccountId = destAccountProduct.getString("glAccountId");

		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(null, userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyId = LoanUtilities.getMemberPartyIdFromMemberAccountId(Long
				.valueOf(sourceMemberAccountId));

		// LoanUtilities.getE
		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(partyId);
		String entrySequenceId = "1";
		// Post Entries - DR source and CR destination
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		postTransactionEntry(delegator, bdShareCapitalDeficit,
				employeeBranchId, memberBranchId, sourceglLedgerAccountId, "D",
				acctgTransId, glAccountTypeId, entrySequenceId);

		entrySequenceId = "2";
		postTransactionEntry(delegator, bdShareCapitalDeficit,
				employeeBranchId, memberBranchId, destglLedgerAccountId, "C",
				acctgTransId, glAccountTypeId, entrySequenceId);

		// Create Transactions for the same (Member Statement)

		// Source
		// cashDepositVersion4(bdShareCapitalDeficit,
		// Long.valueOf(sourceMemberAccountId), userLogin, "TRANSFERFROM",
		// acctgTransId);
		memberTransactionDeposit(bdShareCapitalDeficit,
				Long.valueOf(sourceMemberAccountId), userLogin, "TRANSFERFROM",
				null, null, acctgTransId,
				destAccountProduct.getLong("accountProductId"), null);
		// Destination
		memberTransactionDeposit(bdShareCapitalDeficit,
				Long.valueOf(destinationMemberAccountId), userLogin,
				"TRANSFERTO", null, null, acctgTransId,
				sourceAccountProduct.getLong("accountProductId"), null);

	}

	public static BigDecimal getTotalDeposits(String code, Long memberId,
			Timestamp startDate, Timestamp endDate) {
		// AccHolderTransactionServices.getTotalBalance(memberAccountId,
		// balanceDate)
		Long accountProductId = LoanUtilities
				.getAccountProductIdGivenCodeId(code);
		Long memberAccountId = LoanUtilities
				.getMemberAccountIdFromMemberAccount(memberId, accountProductId);
		return AccHolderTransactionServices.calculateTotalIncrease(
				memberAccountId.toString(), startDate, endDate, "I");
	}

	public static BigDecimal getTotalPrincipalPaid(String loanApplicationId,
			Long memberId, Timestamp startDate, Timestamp endDate) {

		return LoanRepayments.getTotalPrincipalPaid(loanApplicationId,
				startDate, endDate);
		// return LoanRepayments.getTotalPrincipalPaid(loanApplicationId,
		// memberId, startDate, endDate);

	}

	public static BigDecimal getTotalInterestPaid(String loanApplicationId,
			Long memberId, Timestamp startDate, Timestamp endDate) {
		// TODO Auto-generated method stub
		return LoanRepayments.getTotalInterestPaid(loanApplicationId,
				startDate, endDate);
		// getTotalInterestPaid(loanApplicationId, memberId, startDate,
		// endDate);
	}

	public static BigDecimal getTotalInsurancePaid(String loanApplicationId,
			Long memberId, Timestamp startDate, Timestamp endDate) {
		// TODO Auto-generated method stub
		return LoanRepayments.getTotalInsurancePaid(loanApplicationId,
				startDate, endDate);
		// AccHolderTransactionServices.getTotalInsurancePaid(loanApplicationId,
		// memberId, startDate, endDate);
	}

	public static String cashDepositLoan(BigDecimal transactionAmount,
			Long loanApplicationId, Map<String, String> userLogin,
			String withdrawalType, String acctgTransId) {

		log.info(" Transaction Amount ---- " + transactionAmount);
		log.info(" Transaction MA ---- " + loanApplicationId);

		// log.info(" UserLogin ---- " + userLogin.get("userLoginId"));
		log.info(" Transaction Amount ---- " + transactionAmount);
		if (userLogin == null) {
			userLogin = new HashMap<String, String>();
			userLogin.put("userLoginId", "admin");
		}

		// Save Parent
		GenericValue accountTransactionParent = createAccountTransactionParentForLoans(
				null, userLogin);
		String transactionType = withdrawalType;

		// Set the the Treasury ID
		String treasuryId = TreasuryUtility.getTellerId(userLogin);
		// accountTransaction.set("treasuryId", treasuryId);
		// addChargesToTransaction(accountTransaction, userLogin,
		// transactionType);
		// increaseDecrease
		// String acctgTransId = postCashDeposit(memberAccountId, userLogin,
		// transactionAmount);
		GenericValue accountTransaction = null;
		createTransactionLoan(accountTransaction, transactionType, userLogin,
				loanApplicationId.toString(), transactionAmount, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId,
				treasuryId);

		// postCashWithdrawalTransaction(accountTransaction, userLogin);

		return accountTransactionParent.getString("accountTransactionParentId");
	}

	private static GenericValue createAccountTransactionParentForLoans(
			GenericValue accounTransaction, Map<String, String> userLogin) {
		GenericValue transactionParent;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String accountTransactionParentId = delegator
				.getNextSeqId("AccountTransactionParent");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		// String branchId = getEmployeeBranch((String)
		// userLogin.get("partyId"));
		// String partyId = loanApplication.getString("partyId");

		transactionParent = delegator.makeValidValue(
				"AccountTransactionParent", UtilMisc.toMap(
						"accountTransactionParentId",
						accountTransactionParentId, "isActive", "Y",
						"createdBy", createdBy, "updatedBy", updatedBy,
						"memberAccountId", null, "approved", "NO", "rejected",
						"NO", "posted", "posted"));
		try {
			delegator.createOrStore(transactionParent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction Parent");
		}

		return transactionParent;
	}

	/****
	 * Create Transaction Loan
	 * 
	 * */
	private static void createTransactionLoan(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String loanApplicationId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId,
			String acctgTransId, String treasuryId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);// loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));

		loanApplication = LoanUtilities.getEntityValue("LoanApplication",
				"loanApplicationId", Long.valueOf(loanApplicationId));
		String partyId = loanApplication.getLong("partyId").toString();

		// getMemberPartyId(memberAccountId);
		// loanApplication.getString("partyId");

		String increaseDecrease;

		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		// Check for withdrawal and deposit - overrides the earlier settings for
		// product charges
		if (productChargeId == null) {
			if (((transactionType != null) && (transactionType
					.equals("CASHWITHDRAWAL")))
					|| ((transactionType != null) && (transactionType
							.equals("ATMWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("VISAWITHDRAW")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCOWITHDRAWAL")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCE")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCLEARANCECHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVDEC")))

					|| ((transactionType != null) && (transactionType
							.equals("CARDAPPLICATIONCHARGES")))

					|| ((transactionType != null) && (transactionType
							.equals("EXCISEDUTY")))

					|| ((transactionType != null) && (transactionType
							.equals("POSCASHPURCHASE")))) {
				increaseDecrease = "D";
			}

			if (((transactionType != null) && (transactionType
					.equals("CASHDEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("MSACCODEPOSIT")))

					|| ((transactionType != null) && (transactionType
							.equals("SALARYPROCESSING")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCASHPAY")))

					|| ((transactionType != null) && (transactionType
							.equals("LOANCHEQUEPAY")))

					|| ((transactionType != null) && (transactionType
							.equals("MEMBERACCOUNTJVINC")))) {
				increaseDecrease = "I";
			}
		}

		// acctgTransId

		Long memberAccountIdLong = null;
		Long productChargeIdLong = null;
		Long partyIdLong = null;

		if (productChargeId != null) {
			productChargeId = productChargeId.replaceAll(",", "");
			productChargeIdLong = Long.valueOf(productChargeId);
		}

		if (partyId != null) {
			partyId = partyId.replaceAll(",", "");
			partyIdLong = Long.valueOf(partyId);
		}

		// "partyId", Long.valueOf(partyId),

		// String treasuryId = null;

		// if (loanApplication != null)
		// treasuryId = loanApplication.getString("treasuryId");

		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId",
						partyIdLong, "increaseDecrease", increaseDecrease,
						"loanApplicationId", Long.valueOf(loanApplicationId),
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "treasuryId",
						treasuryId, "accountTransactionParentId",
						accountTransactionParentId, "acctgTransId",
						acctgTransId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}

	/****
	 * @author Japheth Odonya @when Jul 22, 2015 6:29:05 PM
	 * 
	 * */
	public static String postUnclearedCheque(GenericValue accountTransaction,
			Map<String, String> userLogin) {
		// Post the transaction
		log.info(" Party ID " + accountTransaction.getLong("partyId"));
		log.info(" Amount "
				+ accountTransaction.getBigDecimal("transactionAmount"));

		BigDecimal bdChequeAmount = accountTransaction
				.getBigDecimal("transactionAmount");

		Long memberAccountId = accountTransaction.getLong("memberAccountId");
		ChargeDutyItem chargeDutyItem = getChequeDepositChargeAmount(accountTransaction
				.getLong("memberAccountId"));

		BigDecimal bdChequeDepositChargeAmount = chargeDutyItem
				.getChargeAmount();
		String chequeDepositChargeAccountId = chargeDutyItem
				.getChargeAccountId();

		BigDecimal bdExciseDutyAmount = chargeDutyItem.getDutyAmount();
		String exciseDutyAccountId = chargeDutyItem.getDutyAccountId();

		GenericValue accountProduct = getAccountProductEntity(memberAccountId);
		String unclearedChequeAccountId = accountProduct
				.getString("unclearedChequesAccountId");
		BigDecimal bdUnclearedAmount = bdChequeAmount
				.subtract(bdChequeDepositChargeAmount.add(bdExciseDutyAmount));
		// Post Cheque

		/****
		 * CR Cheque Deposit Charge for this Product CR Excise Duty CR Uncleared
		 * Cheque
		 * 
		 * Dr Bank A/C (Bank Cheque Account for this product)
		 * 
		 * */
		// Get tha acctgTransId
		String acctgTransId = creatAccountTransRecordVer2(accountTransaction,
				userLogin);
		String glAccountTypeId = "MEMBER_DEPOSIT";
		String partyId = LoanUtilities
				.getMemberPartyIdFromMemberAccountId(memberAccountId);

		// Update Cheque Deposit Transaction
		accountTransaction.setString("treasuryId",
				TreasuryUtility.getTreasuryId(userLogin));
		accountTransaction.setString("acctgTransId", acctgTransId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// LoanUtilities.getE
		String employeeBranchId = getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(partyId);

		String entrySequenceId = "1";
		// CR Cheque Deposit Charge for this Product

		postTransactionEntry(delegator, bdChequeDepositChargeAmount,
				employeeBranchId, memberBranchId, chequeDepositChargeAccountId,
				"C", acctgTransId, glAccountTypeId, entrySequenceId);

		entrySequenceId = "2";
		// CR Excise Duty
		postTransactionEntry(delegator, bdExciseDutyAmount, employeeBranchId,
				memberBranchId, exciseDutyAccountId, "C", acctgTransId,
				glAccountTypeId, entrySequenceId);

		entrySequenceId = "3";
		// CR Uncleared Cheque Amount
		postTransactionEntry(delegator, bdUnclearedAmount, employeeBranchId,
				memberBranchId, unclearedChequeAccountId, "C", acctgTransId,
				glAccountTypeId, entrySequenceId);

		entrySequenceId = "4";
		// Dr Bank A/C (Bank Cheque Account for this product)
		postTransactionEntry(delegator, bdChequeAmount, employeeBranchId,
				memberBranchId, unclearedChequeAccountId, "D", acctgTransId,
				glAccountTypeId, entrySequenceId);

		// Create Transactions for the same (Member Statement)

		// Source
		// cashDepositVersion4(bdShareCapitalDeficit,
		// Long.valueOf(sourceMemberAccountId), userLogin, "TRANSFERFROM",
		// acctgTransId);
		// memberTransactionDeposit(bdShareCapitalDeficit,
		// Long.valueOf(sourceMemberAccountId), userLogin, "TRANSFERFROM",
		// null, null, acctgTransId,
		// destAccountProduct.getLong("accountProductId"), null);

		// Add Excise Duty and Charge Amount to MPA (Cheque Deposit Charges)
		// Adding Cheque Charge to MPA

		createTransaction(accountTransaction, "Cheque Deposit Charge",
				userLogin, memberAccountId.toString(),
				bdChequeDepositChargeAmount, chargeDutyItem
						.getProductChargeId().toString(),
				accountTransaction.getString("accountTransactionParentId"),
				acctgTransId);

		// Adding Excise Duty to MPA
		createTransaction(accountTransaction, "Cheque Deposit Excise",
				userLogin, memberAccountId.toString(), bdExciseDutyAmount,
				chargeDutyItem.getExciseDutyChargeId().toString(),
				accountTransaction.getString("accountTransactionParentId"),
				acctgTransId);

		return "success";
	}

	private static ChargeDutyItem getChequeDepositChargeAmount(
			Long memberAccountId) {
		// TODO Auto-generated method stub
		ChargeDutyItem chargeDutyItem = new ChargeDutyItem();
		GenericValue accountProduct = getAccountProductEntity(memberAccountId);

		// private String chargeAccountId;
		// private String dutyAccountId;
		// private BigDecimal chargeAmount;
		// private BigDecimal dutyAmount;

		// if (accountProduct != null)
		// return accountProduct.getString("chequeBankAccountId");
		// CHEQUEDEPOSIT
		// accountProductId

		// Get from AccountProductCharge where accountProductId is
		// accountProduct.getLong("accountProductId")
		List<GenericValue> accountProductChargeELI = null;
		EntityConditionList<EntityExpr> accountProductChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProduct.getLong("accountProductId")),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CHEQUEDEPOSIT")),
						EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			accountProductChargeELI = delegator.findList(
					"AccountProductCharge", accountProductChargeConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : accountProductChargeELI) {
			if (genericValue.getString("isFixed").equals("Y")) {
				chargeDutyItem.setChargeAmount(genericValue
						.getBigDecimal("fixedAmount"));
				chargeDutyItem
						.setChargeAccountId(getChargeAccountId(genericValue
								.getLong("productChargeId")));
				chargeDutyItem.setProductChargeId(genericValue
						.getLong("productChargeId"));

			} else {
				chargeDutyItem.setDutyAmount(genericValue
						.getBigDecimal("rateAmount"));
				chargeDutyItem.setDutyAccountId(getChargeAccountId(genericValue
						.getLong("productChargeId")));
				chargeDutyItem.setExciseDutyChargeId(genericValue
						.getLong("productChargeId"));
			}
		}

		chargeDutyItem.setDutyAmount(chargeDutyItem.getChargeAmount().multiply(
				chargeDutyItem.getDutyAmount().divide(
						new BigDecimal(ONEHUNDRED), 4, RoundingMode.HALF_UP)));

		return chargeDutyItem;
	}

	private static String getChargeAccountId(Long productChargeId) {

		GenericValue productCharge = LoanUtilities.getEntityValue(
				"ProductCharge", "productChargeId", productChargeId);

		if (productCharge != null)
			return productCharge.getString("chargeAccountId");
		return null;
	}
	
	/****
	 * Reverse ATM Transaction
	 * */
	public static String reverseATMTransaction(String SystemTrace) {
		
		
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//ATMWITHDRAWAL
		EntityConditionList<EntityExpr> withdrawalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"systemtrace", EntityOperator.EQUALS,
						SystemTrace), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "ATMWITHDRAWAL")),
						EntityOperator.AND);
		List<GenericValue> withdrawalTransactionELI = null; 
		try {
			// cashDepositELI = delegator.findList("AccountTransaction",
			// EntityCondition.makeCondition("memberAccountId",
			// memberAccountId), null, null, null, false);

			withdrawalTransactionELI = delegator.findList("AccountTransaction",
					withdrawalConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		if((withdrawalTransactionELI == null) || (withdrawalTransactionELI.size() < 1)){
			return "notransaction";
		}
		
		
		//Check if Reversal Exists
		//ATMWITHDRAWALREVERSAL
		EntityConditionList<EntityExpr> withdrawalTransactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"systemtrace", EntityOperator.EQUALS,
						SystemTrace), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "ATMWITHDRAWALREVERSAL")),
						EntityOperator.AND);
		List<GenericValue> accountWithdrawalTransactionELI = null; 
		try {
			// cashDepositELI = delegator.findList("AccountTransaction",
			// EntityCondition.makeCondition("memberAccountId",
			// memberAccountId), null, null, null, false);

			accountWithdrawalTransactionELI = delegator.findList("AccountTransaction",
					withdrawalTransactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((accountWithdrawalTransactionELI != null) && (accountWithdrawalTransactionELI.size() > 0)){
			return "reversed";
		}
		
		String newacctgTransId = creatAccountTransRecord(null, null);
		// Get the acctgTransEntry list with thetransactionId
		// Update the source record too (Subsidiary)

		// Get new tras
		// ***
		// Change the transactionType MSACCOWITHDRAWALREV
		// TODO Auto-generated method stub

		String thetransactionId = "";
		
		List<GenericValue> accountTransactionELI = null;
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("systemtrace",
							SystemTrace), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue genericValue : accountTransactionELI) {

			if (genericValue.getString("systemtrace") != null) {
				thetransactionId = genericValue.getString("acctgTransId");
			}
		}

		accountTransactionELI = null;
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					EntityCondition.makeCondition("acctgTransId",
							thetransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the AccountTransactionReccords
		String accountTransactionId = "";
		for (GenericValue accountTransaction : accountTransactionELI) {

			accountTransactionId = delegator.getNextSeqId("AccountTransaction");
			accountTransaction.setString("transactionType",
					"ATMWITHDRAWALREVERSAL");
			accountTransaction.setString("increaseDecrease", "I");
			accountTransaction.setString("acctgTransId", newacctgTransId);
			accountTransaction.setString("systemtrace",
					SystemTrace);

			accountTransaction.setString("accountTransactionId",
					accountTransactionId);

			try {
				delegator.create(accountTransaction);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// Get the thetransactionId

		List<GenericValue> acctgTransEntryELI = null;
		try {
			acctgTransEntryELI = delegator.findList("AcctgTransEntry",
					EntityCondition.makeCondition("acctgTransId",
							thetransactionId), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get the acctgTransEntry
		for (GenericValue genericValue : acctgTransEntryELI) {

			if (genericValue.getString("debitCreditFlag").equals("C")) {
				genericValue.setString("debitCreditFlag", "D");
			} else {
				genericValue.setString("debitCreditFlag", "C");
			}
			genericValue.setString("acctgTransId", newacctgTransId);

			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// update the entries reveresed (D to C and C to D)
		// update the transactionId to the new id
		// create the record for each
		return "success";

	}

}
