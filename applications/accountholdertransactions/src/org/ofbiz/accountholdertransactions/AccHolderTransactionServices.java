package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class AccHolderTransactionServices {
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

		BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		BigDecimal bdTotalDeposit = BigDecimal.ZERO;
		BigDecimal bdTotalWithdrawal = BigDecimal.ZERO;

		// Delegator delegator = (Delegator) request.getAttribute("delegator");
		String memberAccountId = (String) request
				.getParameter("memberAccountId");

		// Get Opening Balance
		bdOpeningBalance = calculateOpeningBalance(memberAccountId, delegator);
		// Get Total Deposits
		bdTotalDeposit = calculateTotalCashDeposits(memberAccountId, delegator);
		// Get Total Withdrawals
		bdTotalWithdrawal = calculateTotalCashWithdrawals(memberAccountId,
				delegator);

		// Available Amount = Total Opening Account + Total Deposits - Total
		// Withdrawals
		result.put("availableAmount", bdOpeningBalance.add(bdTotalDeposit).subtract(bdTotalWithdrawal));
		
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

	/***
	 * @author jodonya
	 * Calculates the Opening Balance
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
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : openingBalanceELI) {
			bdBalance.add(genericValue.getBigDecimal("savingsOpeningBalance"));
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalCashDeposits(String memberAccountId,
			Delegator delegator) {
		List<GenericValue> cashDepositELI = null;

		try {
			cashDepositELI = delegator.findList("CashDeposit",
					EntityCondition.makeCondition("memberAccountId",
							memberAccountId), null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (cashDepositELI == null) {
			// result.put("", "No Member Accounts");
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashDepositELI) {
			bdBalance.add(genericValue.getBigDecimal("cashAmount"));
		}
		return bdBalance;
	}

	private static BigDecimal calculateTotalCashWithdrawals(String memberAccountId,
			Delegator delegator) {
		List<GenericValue> cashWithdrawalELI = null;

		try {
			cashWithdrawalELI = delegator.findList("CashWithdrawal",
					EntityCondition.makeCondition("memberAccountId",
							memberAccountId), null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (cashWithdrawalELI == null) {
			// result.put("", "No Member Accounts");
			return BigDecimal.ZERO;
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance.add(genericValue.getBigDecimal("cashAmount"));
		}
		return bdBalance;
	}
}
