package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class AccHolderTransactionServices {

	private static Logger log = Logger
			.getLogger(AccHolderTransactionServices.class);

	public static String getBranches(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String bankDetailsId = (String) request.getParameter("bankDetailsId");

		// GenericValue saccoProduct = null;
		// EntityListIterator branchesELI;// =
		// delegator.findListIteratorByCondition("BankBranch", new
		// EntityExpr("bankDetailsId", EntityOperator.EQUALS, bankDetailsId),
		// null, UtilMisc.toList("bankBranchId", "branchName"), "branchName",
		// null);
		// branchesELI =
		// delegator.findListIteratorByCondition(dynamicViewEntity,
		// whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy,
		// findOptions)
		// branchesELI = delegator.findListIteratorByCondition("BankBranch", new
		// EntityExpr("productId", EntityOperator.NOT_EQUAL, null),
		// UtilMisc.toList("productId"), null);
		List<GenericValue> branchesELI = null;

		// branchesELI = delegator.findList("BankBranch", new EntityExpr(),
		// UtilMisc.toList("bankBranchId", "branchName"), null, null, null);
		try {
			// branchesELI = delegator.findList("BankBranch",
			// EntityCondition.makeConditionWhere("(bankDetailsId = "+bankDetailsId+")"),
			// null, null, null, false);
			branchesELI = delegator.findList("BankBranch", EntityCondition
					.makeCondition("bankDetailsId", bankDetailsId), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// SaccoProduct

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

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);

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
		result.put("availableAmount",
				getTotalSavings(memberAccountId, delegator));
		result.put("bookBalanceAmount",
				getBookBalance(memberAccountId, delegator));
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
	private static BigDecimal calculateOpeningBalance(String memberAccountId,
			Delegator delegator) {
		List<GenericValue> openingBalanceELI = null;

		try {
			openingBalanceELI = delegator.findList("MemberAccountDetails",
					EntityCondition.makeCondition("memberAccountId",
							memberAccountId), null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
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

	private static BigDecimal calculateTotalCashDeposits(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> cashDepositELI = null;

		// Conditions
		// EntityConditionList<EntityCondition> transactionConditions =
		// EntityCondition.makeCond
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition
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

		// Conditions
		// EntityConditionList<EntityCondition> transactionConditions =
		// EntityCondition.makeCond
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"increaseDecrease", EntityOperator.EQUALS,
						increaseDecrease)), EntityOperator.AND);

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

	private static BigDecimal calculateTotalCashWithdrawals(
			String memberAccountId, Delegator delegator) {
		List<GenericValue> cashWithdrawalELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CASHWITHDRAWAL")), EntityOperator.AND);

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

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CHEQUEDEPOSIT")), EntityOperator.AND);

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

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CHEQUEWITHDRAWAL")), EntityOperator.AND);

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

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						memberAccountId), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CHEQUEDEPOSIT"), EntityCondition
						.makeCondition("clearDate",
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
		// BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// BigDecimal bdTotalCashDeposit = BigDecimal.ZERO;
		// BigDecimal bdTotalCashWithdrawal = BigDecimal.ZERO;
		//
		// BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		// BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		// BigDecimal bdTotalChequeWithdrawal = BigDecimal.ZERO;
		// // Get Opening Balance
		// bdOpeningBalance = calculateOpeningBalance(memberAccountId,
		// delegator);
		// // Get Total Deposits
		// bdTotalCashDeposit = calculateTotalCashDeposits(memberAccountId,
		// delegator);
		// // Get Total Withdrawals
		// bdTotalCashWithdrawal =
		// calculateTotalCashWithdrawals(memberAccountId,
		// delegator);
		//
		// bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
		// delegator);
		//
		// bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
		// memberAccountId, delegator);
		//
		// bdTotalChequeWithdrawal = calculateTotalChequeWithdrawals(
		// memberAccountId, delegator);
		// // Available Amount = Total Opening Account + Total Deposits - Total
		// // Withdrawals
		// return bdOpeningBalance.add(bdTotalCashDeposit)
		// .add(bdTotalChequeDepositCleared)
		// .subtract(bdTotalCashWithdrawal)
		// .subtract(bdTotalChequeWithdrawal);
		return getAvailableBalanceVer2(memberAccountId, delegator);
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
		return getBookBalanceVer2(memberAccountId, delegator);
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

		bdTotalIncrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "I");
		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "D");

		BigDecimal bdTotalChequeDeposit = calculateTotalChequeDeposits(
				memberAccountId, delegator);
		BigDecimal bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, delegator);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);

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

}
