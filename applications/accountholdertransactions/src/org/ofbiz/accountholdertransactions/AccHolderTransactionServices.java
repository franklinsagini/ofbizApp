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
import java.util.Calendar;
import java.util.Date;
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
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
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
		partyId = partyId.replaceAll(",", "");
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", Long.valueOf(partyId)), null,
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
	public static BigDecimal calculateOpeningBalance(String memberAccountId,
			Delegator delegator) {
		List<GenericValue> openingBalanceELI = null;
		memberAccountId = memberAccountId.replaceAll(",", "");
		try {
			openingBalanceELI = delegator.findList("MemberAccountDetails",
					EntityCondition.makeCondition("memberAccountId",
							Long.valueOf(memberAccountId)), null, null, null, false);

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
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
						"increaseDecrease", EntityOperator.EQUALS,
						increaseDecrease)), EntityOperator.AND);

		try {
			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		BigDecimal bdTransactionAmount = null;
		log.info("Got  ----------- "+cashDepositELI.size()+" Records !!!");
		for (GenericValue genericValue : cashDepositELI) {
			bdTransactionAmount = genericValue.getBigDecimal("transactionAmount");
			if (bdTransactionAmount != null){
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
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
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
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
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
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
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
		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
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
		 BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		 bdOpeningBalance = calculateOpeningBalance(memberAccountId,
		 delegator);
		// // Get Total Deposits
		return (getAvailableBalanceVer2(memberAccountId, delegator).add(bdOpeningBalance));
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
		bdOpeningBalance = calculateOpeningBalance(memberAccountId,
				 delegator);
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
		return (getBookBalanceVer2(memberAccountId, delegator).add(bdOpeningBalance));
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
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is "+bdTotalIncrease);
		
		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				delegator, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is "+bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(
				memberAccountId, delegator);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "+bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, delegator);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "+bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);
		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is "+bdTotalAvailable);
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
		String acctgTransType = "MEMBER_DEPOSIT";

		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(accountTransaction,
				acctgTransType, userLogin);
		// Do the posting
		Delegator delegator = accountTransaction.getDelegator();
		BigDecimal transactionAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		String partyId = (String) userLogin.get("partyId");

		// Debit Member Deposit

		String memberDepositAccountId = getMemberDepositAccount(accountTransaction, "MEMBERTRANSACTIONACCOUNT");
		String postingType = "D";
		String entrySequenceId = "00001";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, partyId,
				memberDepositAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		// Credit Cash Account
		String cashAccountId = getCashAccount(accountTransaction, "MEMBERTRANSACTIONACCOUNT");
		postingType = "C";
		entrySequenceId = "00002";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, partyId,
				cashAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

		return "POSTED";
	}

	private static String getCashAccount(GenericValue accountTransaction, String setUpId) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = accountTransaction.getDelegator();
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							setUpId), false);
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

	private static String getMemberDepositAccount(
			GenericValue accountTransaction, String setUpId) {

		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = accountTransaction.getDelegator();
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							setUpId), false);
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
			log.error("Could post a Transaction");
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
		Delegator delegator = accountTransaction.getDelegator();
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
			String transactionType) {

		// Get the Product by first accessing the MemberAccount
		String accountProductId = getAccountProduct(accountTransaction);

		// Get the Charges for the Product
		List<GenericValue> accountProductChargeELI = null;
		accountProductChargeELI = getAccountProductCharges(accountTransaction,
				accountProductId, transactionType);
		log.info("NNNNNNNNNNNNNN The Number of Charges is ::::: "+accountProductChargeELI.size());
		// Create a transaction in Account Transaction for each of the Charges
		for (GenericValue accountProductCharge : accountProductChargeELI) {
			addCharge(accountProductCharge, accountTransaction, userLogin);
		}
		// Create an Account Transaction for each of the Charges

		return "";
	}

	/*****
	 * @author Japheth Odonya @when Aug 25, 2014 10:26:34 PM Add Charge to
	 *         AccountTransaction Accounting (POST)
	 * **/
	private static void addCharge(GenericValue accountProductCharge,
			GenericValue accountTransaction, Map<String, String> userLogin) {
		// Add Account Transaction
		BigDecimal bdChargeAmount;
		bdChargeAmount = getChargeAmount(accountProductCharge,
				accountTransaction);
		String chargeName = getChargeName(accountProductCharge);
		// = accountProductCharge.getBigDecimal("");
		String memberAccountId = String.valueOf(accountTransaction.getLong("memberAccountId"));
		String productChargeId = String.valueOf(accountProductCharge.getLong("productChargeId"));
		
		String accountTransactionParentId = accountTransaction.getString("accountTransactionParentId");
		createTransaction(accountTransaction, chargeName, userLogin, memberAccountId, bdChargeAmount, productChargeId, accountTransactionParentId);
		
		// POST Charge
		String acctgTransType = "OTHER_INCOME";

		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(accountTransaction,
				acctgTransType, userLogin);
		
		//Debit Member Deposits
		Delegator delegator = accountTransaction.getDelegator();
		String partyId = accountTransaction.getString("partyId");
		String memberDepositAccountId = getMemberDepositAccount(accountTransaction, "MEMBERTRANSACTIONCHARGE");
		String postingType = "D";
		String entrySequenceId = "00001";
		postTransactionEntry(delegator, bdChargeAmount, partyId, memberDepositAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		
		//Credit Charge or Services
		String chargeAccountId = getCashAccount(accountTransaction, "MEMBERTRANSACTIONCHARGE");
		postingType = "C";
		entrySequenceId = "00002";
		postTransactionEntry(delegator, bdChargeAmount, partyId, chargeAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
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
			productCharge = delegator.findOne("ProductCharge",
					UtilMisc.toMap("productChargeId", Long.valueOf(productChargeId)), false);
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
		//Delegator delegator = accountProductCharge.getDelegator();
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

	private static GenericValue getAccoutCharge(String parentChargeId,
			GenericValue accountProductCharge) {
		Delegator delegator = accountProductCharge.getDelegator();
		List<GenericValue> accountProductChargeELI = null;
		parentChargeId = parentChargeId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						accountProductCharge.getLong("accountProductId")), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						accountProductCharge.getString("transactionType"))
						, EntityCondition.makeCondition(
						"productChargeId", EntityOperator.EQUALS,
						Long.valueOf(parentChargeId))
						), EntityOperator.AND);
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

		Delegator delegator = accountTransaction.getDelegator();
		accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountChargeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS,
						Long.valueOf(accountProductId)), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						transactionType)), EntityOperator.AND);
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
			memberAccount = delegator.findOne("MemberAccount",
					UtilMisc.toMap("memberAccountId", Long.valueOf(memberAccountId)), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Get AccountProduct
		String accountProductId = memberAccount.getString("accountProductId");
		return accountProductId;
	}

	/**
	 * Create a record in AccountTransaction
	 * */
	private static void createTransaction(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId) {
		Delegator delegator = loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = getEmployeeBranch((String) userLogin.get("partyId"));
		String partyId = loanApplication.getString("partyId");
		String increaseDecrease;
		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
		}

		productChargeId = productChargeId.replaceAll(",", "");
		memberAccountId = memberAccountId.replaceAll(",", "");
		
		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId, "partyId", Long.valueOf(partyId),
						"increaseDecrease", increaseDecrease,
						"memberAccountId", Long.valueOf(memberAccountId), "productChargeId",
						Long.valueOf(productChargeId), "transactionAmount",
						transactionAmount, "transactionType", transactionType, "accountTransactionParentId", accountTransactionParentId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}
	}
	
	public static String getLoanApplicationDetails(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request.getParameter("loanApplicationId");
		GenericValue loanApplication = null;
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Loan Application Details";
		}

		if (loanApplication != null) {
			//loanNo
			//loanType
			//loanAmt
			
			result.put("loanNo", loanApplication.get("loanNo"));
			result.put("loanTypeId",
					loanApplication.get("loanProductId"));
			result.put("loanAmt", loanApplication.getBigDecimal("loanAmt"));
			
			
			/***
			 * loanNo
				loanTypeId
				totalLoanDue
				totalInterestDue
				totalInsuranceDue
				totalPrincipalDue
				transactionAmount
				
				getTotalLoanDue(partyId)
				getTotalInterestDue(partyId)
				getTotalInsuranceDue(partyId)
				getTotalPrincipalDue(partyId)
			 * */
			BigDecimal totalLoanDue = LoanRepayments.getTotalLoanDue(loanApplication.getString("partyId"), loanApplication.getString("loanApplicationId"));
			result.put("totalLoanDue", totalLoanDue);
			result.put("transactionAmount", totalLoanDue);
			
			result.put("totalInterestDue", LoanRepayments.getTotalInterestDue(loanApplication.getString("partyId"), loanApplication.getString("loanApplicationId")));
			result.put("totalInsuranceDue", LoanRepayments.getTotalInsuranceDue(loanApplication.getString("partyId"), loanApplication.getString("loanApplicationId")));
			result.put("totalPrincipalDue", LoanRepayments.getTotalPrincipalDue(loanApplication.getString("partyId"), loanApplication.getString("loanApplicationId")));
			

			// result.put("selectedRepaymentPeriod",
			// saccoProduct.get("selectedRepaymentPeriod"));
		} else {
			System.out.println("######## Loan Application details not found #### ");
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
			GenericValue stationAccountTransaction, Map<String, String> userLogin) {
		
		String acctgTransType = "STATION_DEPOSIT";

		// Create the Account Trans Record
		String acctgTransId = createAccountingTransaction(stationAccountTransaction,
				acctgTransType, userLogin);
		// Do the posting
		Delegator delegator = stationAccountTransaction.getDelegator();
		BigDecimal transactionAmount = stationAccountTransaction
				.getBigDecimal("transactionAmount");
		String partyId = (String) userLogin.get("partyId");
		
		//Get Member Branch
		String branchId;
		branchId = getBranch(partyId);

		//Debit Cash/Bank Account 

		String memberDepositAccountId = getMemberDepositAccount(stationAccountTransaction, "STATIONACCOUNTPAYMENT");
		String postingType = "D";
		String entrySequenceId = "00001";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, branchId,
				memberDepositAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		// Credit Station Deposit Account
		String cashAccountId = getCashAccount(stationAccountTransaction, "STATIONACCOUNTPAYMENT");
		postingType = "C";
		entrySequenceId = "00002";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		postTransactionEntry(delegator, transactionAmount, branchId,
				cashAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}

		return "POSTED";

	}

	private static String getBranch(String partyId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// GenericValue accountTransaction = null;
		GenericValue person = null;
		try {
			person = delegator
					.findOne("Person", UtilMisc.toMap(
							"partyId", partyId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return person.getString("branchId");
	}
	
	public static String getEmployeeBranch(String partyId){
		String branchId = "";
		branchId = getBranch(partyId);
		return branchId;
	}
	
	public static String getMemberBranch(Long memberAccountId){
		String branchId = "";
		//Get MemberAccount
		GenericValue memberAccount = null;
		//memberAccountId = memberAccountId.replaceAll(",", "");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccount = delegator
					.findOne("MemberAccount", UtilMisc.toMap(
							"memberAccountId", memberAccountId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long partyId = memberAccount.getLong("partyId");
		
		//Get Member
		GenericValue member = null;
		try {
			member = delegator
					.findOne("Member", UtilMisc.toMap(
							"partyId", partyId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		branchId = member.getString("branchId");
		return branchId;
	}
	
	/***
	 * Gets the next slip number
	 * */
	public static String getNextSlipNumber(){
		String slipNumber = "";
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String helperName = delegator.getGroupHelperName("org.ofbiz");    // gets the helper (localderby, localmysql, localpostgres, etc.) for your entity group org.ofbiz
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
			statement.execute("SELECT count(SLIP_NUMBER) as slipnumbercount FROM ACCOUNT_TRANSACTION");
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
			while (results.next()){
				count = results.getLong("slipnumbercount");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int padDigits = 10;
		count = count + 1;
		
		slipNumber =	paddString(padDigits, count.toString());
		
		return slipNumber;
	}
	
	public static String paddString(int padDigits, String count) {
		String padded = String.format("%"+padDigits+"s", count).replace(' ', '0');
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
		 bdOpeningBalance = calculateOpeningBalance(memberAccountId,
		 delegator);
		// // Get Total Deposits
		 if (balanceDate == null)
			 balanceDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
		return (getAvailableBalanceVer2(memberAccountId, balanceDate).add(bdOpeningBalance));
	}
	
	public static Map<String, Object> getTotalBalanceNow(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		
		String memberAccountId=(String) context.get("memberAccountId");
		
		log.info(" ##### Account "+memberAccountId);
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		 BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		 bdOpeningBalance = calculateOpeningBalance(memberAccountId,
		 delegator);
		// // Get Total Deposits
		 Timestamp balanceDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
		 
		 BigDecimal balance = getAvailableBalanceVer2(memberAccountId, balanceDate).add(bdOpeningBalance);
				 
		 log.info(" #####BBBBBBB  Balance "+balance);
		 result.put("balance", balance) ;
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
		log.info(" IIIIIIIIIIIIIIIIIIII Total Increase is "+bdTotalIncrease);
		
		bdTotalDecrease = calculateTotalIncreaseDecrease(memberAccountId,
				balanceDate, "D");
		log.info(" DDDDDDDDDDDDDDDDDDD Total Decrease is "+bdTotalDecrease);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(
				memberAccountId, balanceDate);
		log.info(" CCCCCCCCCCCCCCCCC Total Cheque Deposit is "+bdTotalChequeDeposit);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, balanceDate);
		log.info(" CCCCCCCCCCCCCCCCCC Total Cheque Cleared is "+bdTotalChequeDepositCleared);

		bdTotalAvailable = bdTotalIncrease.subtract(bdTotalDecrease);
		bdTotalAvailable = bdTotalAvailable.subtract(bdTotalChequeDeposit);
		bdTotalAvailable = bdTotalAvailable.add(bdTotalChequeDepositCleared);
		log.info(" AAAAAAAAAAAAAAAAAAA Total Available is "+bdTotalAvailable);
		// return
		// bdTotalIncrease.add(bdTotalChequeDepositCleared).subtract(bdTotalDecrease).subtract(bdTotalChequeDeposit);
		return bdTotalAvailable;
	}
	
	private static BigDecimal calculateTotalIncreaseDecrease(
			String memberAccountId, Timestamp balanceDate, String increaseDecrease) {
		List<GenericValue> cashDepositELI = null;

		memberAccountId = memberAccountId.replaceAll(",", "");
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
						"increaseDecrease", EntityOperator.EQUALS,
						increaseDecrease), EntityCondition.makeCondition(
								"createdStamp", EntityOperator.LESS_THAN_EQUAL_TO,
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
		log.info("Got  ----------- "+cashDepositELI.size()+" Records !!!");
		for (GenericValue genericValue : cashDepositELI) {
			bdTransactionAmount = genericValue.getBigDecimal("transactionAmount");
			if (bdTransactionAmount != null){
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
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CHEQUEDEPOSIT"), EntityCondition.makeCondition(
								"createdStamp", EntityOperator.LESS_THAN_EQUAL_TO,
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
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"memberAccountId", EntityOperator.EQUALS,
						Long.valueOf(memberAccountId)), EntityCondition.makeCondition(
						"transactionType", EntityOperator.EQUALS,
						"CHEQUEDEPOSIT"), EntityCondition
						.makeCondition("clearDate",
								EntityOperator.LESS_THAN_EQUAL_TO,
								balanceDate)),
						EntityOperator.AND);
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
}
