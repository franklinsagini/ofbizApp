package org.ofbiz.accountholdertransactions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import com.google.gson.Gson;

public class MemberStatementService {

	public final static String module = MemberStatementService.class.getName();

	public static Map<String, Object> memberTransactions(DispatchContext dctx,
			Map<String, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String user = (String) context.get("user");
		Locale locale = (Locale) context.get("locale");
		String partyId = "";
		GenericValue userLogin = null;
		try {
			userLogin = delegator.findOne(
					"UserLogin",
					UtilMisc.toMap("userLoginId",
							user),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		partyId = userLogin.getString("partyId");

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId))),
						EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		List<String> orderByList = new ArrayList<String>();
		orderByList.add("createdStamp DESC");
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Transaction transaction;

		/***
		 * public String transactionType; public BigDecimal transactionAmount;
		 * public Timestamp createdStamp;
		 * 
		 * public String accountNo; public String accountName;
		 * 
		 * **/
		GenericValue memberAccount = null;

		Map<String, Object> result = ServiceUtil.returnSuccess();
		List<Transaction> listTransactions = new ArrayList<Transaction>();
		Timestamp dateCreated;
		Gson gson = new Gson();

		for (GenericValue genericValue : accountTransactionELI) {
			transaction = new Transaction();

			transaction.setTransactionType(genericValue
					.getString("transactionType"));
			transaction.setTransactionAmount(genericValue
					.getBigDecimal("transactionAmount"));
			
			//genericValue.getT
			dateCreated = new Timestamp(genericValue.getTimestamp("createdStamp").getTime());
			DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
			transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));
			try {
				memberAccount = delegator.findOne(
						"MemberAccount",
						UtilMisc.toMap("memberAccountId",
								genericValue.getLong("memberAccountId")),
						false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			transaction.setAccountName(memberAccount.getString("accountName"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			listTransactions.add(transaction);
		}

		String json = gson.toJson(listTransactions);
		result.put("transactions", json);
		// result.put("listTransactions", listTransactions);
		return result;

	}
	
	/***
	 * Member Transactions Given Member Number
	 * */
	public static Map<String, Object> memberTransactionsGivenMemberNumber(DispatchContext dctx,
			Map<String, ?> context) {
		Delegator delegator = dctx.getDelegator();
		String memberNumber = (String) context.get("memberNumber");
		//GenericValue member = LoanUtilities.getEntityValue("Member", primaryKeyName, primaryKeyValue)
		GenericValue member = LoanUtilities.getMemberGivenMemberNumber(memberNumber.trim());
		String partyId = member.getString("partyId");


		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId))),
						EntityOperator.AND);
		List<GenericValue> accountTransactionELI = null;
		List<String> orderByList = new ArrayList<String>();
		orderByList.add("createdStamp DESC");
		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, orderByList, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Transaction transaction;

		/***
		 * public String transactionType; public BigDecimal transactionAmount;
		 * public Timestamp createdStamp;
		 * 
		 * public String accountNo; public String accountName;
		 * 
		 * **/
		GenericValue memberAccount = null;

		Map<String, Object> result = ServiceUtil.returnSuccess();
		List<Transaction> listTransactions = new ArrayList<Transaction>();
		Timestamp dateCreated;
		Gson gson = new Gson();

		for (GenericValue genericValue : accountTransactionELI) {
			transaction = new Transaction();

			transaction.setTransactionType(genericValue
					.getString("transactionType"));
			transaction.setTransactionAmount(genericValue
					.getBigDecimal("transactionAmount"));
			
			//genericValue.getT
			dateCreated = new Timestamp(genericValue.getTimestamp("createdStamp").getTime());
			DateTime dateTimeCreated = new DateTime(dateCreated.getTime());
			transaction.setCreatedStamp(dateTimeCreated.toString("dd/MM/yyyy"));
			try {
				memberAccount = delegator.findOne(
						"MemberAccount",
						UtilMisc.toMap("memberAccountId",
								genericValue.getLong("memberAccountId")),
						false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			transaction.setAccountName(memberAccount.getString("accountName"));
			transaction.setAccountNo(memberAccount.getString("accountNo"));
			listTransactions.add(transaction);
		}

		String json = gson.toJson(listTransactions);
		result.put("transactions", json);
		// result.put("listTransactions", listTransactions);
		return result;

	}

}
