package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
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
		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		BigDecimal bdTotalCashDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalCashWithdrawal = BigDecimal.ZERO;

		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		BigDecimal bdTotalChequeWithdrawal = BigDecimal.ZERO;
		// Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// Get Total Deposits
		bdTotalCashDeposit = calculateTotalCashDeposits(memberAccountId,
				delegator);
		// Get Total Withdrawals
		bdTotalCashWithdrawal = calculateTotalCashWithdrawals(memberAccountId,
				delegator);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				delegator);

		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, delegator);

		bdTotalChequeWithdrawal = calculateTotalChequeWithdrawals(
				memberAccountId, delegator);
		// Available Amount = Total Opening Account + Total Deposits - Total
		// Withdrawals
		return bdOpeningBalance.add(bdTotalCashDeposit)
				.add(bdTotalChequeDepositCleared)
				.subtract(bdTotalCashWithdrawal)
				.subtract(bdTotalChequeWithdrawal);
	}

	/***
	 * Book Balance = Total Savings + Total Cleared Chequered - Total Cheques
	 * Deposited
	 **/
	public static BigDecimal getBookBalance(String memberAccountId,
			Delegator delegator) {

		BigDecimal bdTotalChequeDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalChequeDepositCleared = BigDecimal.ZERO;
		BigDecimal bdTotalSavings = getTotalSavings(memberAccountId, delegator);

		bdTotalChequeDeposit = calculateTotalChequeDeposits(memberAccountId,
				delegator);
		bdTotalChequeDepositCleared = calculateTotalClearedChequeDeposits(
				memberAccountId, delegator);

		// return
		// bdOpeningBalance.add(bdTotalCashDeposit).add(bdTotalChequeDeposit)
		// .subtract(bdTotalCashWithdrawal).subtract(bdTotalChequeWithdrawal);
		return bdTotalSavings.add(bdTotalChequeDeposit).subtract(bdTotalChequeDepositCleared);
	}

}
