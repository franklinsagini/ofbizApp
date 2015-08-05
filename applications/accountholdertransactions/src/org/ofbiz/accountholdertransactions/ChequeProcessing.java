package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * @author Japheth Odonya @when Aug 24, 2014 9:51:03 PM
 * 
 *         Cheque Processing (Posting Cheques)
 *         
 *         Go through the Unposted Cheques and Post each one of them while updating the
 *         accounting transaction to read Posted and the date when the posting happened
 * */
public class ChequeProcessing {

	public static Logger log = Logger.getLogger(ChequeProcessing.class);

	/***
	 * @author Japheth Odonya @when Aug 24, 2014 9:51:57 PM
	 * 
	 *         Get all the unposted processed Cheques and Post them
	 * */
	public static String postUnpostedCheques(HttpServletRequest request,
			HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		//GenericValue userLogin = (GenericValue) request
		//		.getAttribute("userLogin");

		// Get all the Cheque Deposit Transactions that are Unposted and Cleared
		// then Post each one of them
		List<GenericValue> accountTransactionELI = null;

		String chequeDepostTransaction = "CHEQUEDEPOSIT";
		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"transactionType", EntityOperator.EQUALS,
								chequeDepostTransaction), EntityCondition
								.makeCondition("clearDate",
										EntityOperator.LESS_THAN_EQUAL_TO,
										currentDate)
								, EntityCondition
								.makeCondition("isPosted",
										EntityOperator.NOT_EQUAL, "Y")		
								),
						EntityOperator.AND);

		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ######### Will try to POST Cheques #########");
		if (accountTransactionELI == null) {
			log.info(" ######### No Deposits to Process #########");
		} else{
			log.info(" ######### The Size  #########"+accountTransactionELI.size());
		}
		String acctgTransId = "";
		String acctgTransType = "MEMBER_DEPOSIT";
		int count = 0;
		for (GenericValue accountTransaction : accountTransactionELI) {
			log.info("CCCCCC  Counting "+count);
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
			//acctgTransId = postChequeDeposit(accountTransaction, delegator, acctgTransType);
			log.info("#####PPPPPPPPPPPPPP Posted ####  "+accountTransaction.getBigDecimal("transactionAmount"));
			// Update Account Transaction to read Posted and when it was Posted
			updateAccountTransaction(accountTransaction, delegator, null);
			try {
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
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
	
	
	/****
	 * @author Japheth Odonya  @when Jun 28, 2015 11:09:05 AM
	 * 
	 * Manually Clear a cheque
	 * */
	public static String manuallyClearCheque(Map<String, String> userLogin, String accountTransactionId){
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> accountTransactionELI = null;

		String chequeDepostTransaction = "CHEQUEDEPOSIT";
	
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"transactionType", EntityOperator.EQUALS,
								chequeDepostTransaction),  EntityCondition
								.makeCondition("isPosted",
										EntityOperator.NOT_EQUAL, "Y")	,
										
										EntityCondition
										.makeCondition("accountTransactionId",
												EntityOperator.EQUALS, accountTransactionId)		
								),
						EntityOperator.AND);

		try {
			accountTransactionELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((accountTransactionELI == null) || (accountTransactionELI.size() < 1))
			return "Cheque already cleared, no cheque to clear ";
		
		log.info(" ######### Will try to POST Cheques #########");
		
		String acctgTransId = "";
		String acctgTransType = "MEMBER_DEPOSIT";
		int count = 0;
		for (GenericValue accountTransaction : accountTransactionELI) {
			log.info("CCCCCC  Counting "+count);
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
			acctgTransId = postChequeDeposit(accountTransaction, delegator, acctgTransType);
			log.info("#####PPPPPPPPPPPPPP Posted ####  "+accountTransaction.getBigDecimal("transactionAmount"));
			// Update Account Transaction to read Posted and when it was Posted
			updateAccountTransactionManualClear(accountTransaction, delegator, userLogin, acctgTransId);
			try {
				TransactionUtil.commit();
			} catch (GenericTransactionException e) {
				e.printStackTrace();
			}
		}

		return "success";
	}

	private static void updateAccountTransaction(
			GenericValue accountTransaction, Delegator delegator, String acctgTransId) {
		accountTransaction.set("isPosted", "Y");
		accountTransaction.set("datePosted", new Timestamp(Calendar
				.getInstance().getTimeInMillis()));
		//accountTransaction.setString("acctgTransId", acctgTransId);

		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	private static void updateAccountTransactionManualClear(
			GenericValue accountTransaction, Delegator delegator, Map<String, String> userLogin, String acctgTransId) {
		
		accountTransaction.set("isPosted", "Y");
		accountTransaction.set("datePosted", new Timestamp(Calendar
				.getInstance().getTimeInMillis()));
		
		accountTransaction.set("manuallyCleared", "Y");
		accountTransaction.set("originalClearDuration", accountTransaction.getLong("clearDuration"));
		accountTransaction.set("clearedBy", (String)userLogin.get("userLoginId"));
		accountTransaction.set("originalClearDate", accountTransaction.getTimestamp("clearDate"));
		accountTransaction.set("clearDate",  new Timestamp(Calendar
				.getInstance().getTimeInMillis()));
		accountTransaction.setString("acctgTransId", acctgTransId);

		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	private static String postChequeDeposit(GenericValue accountTransaction,
			Delegator delegator, String acctgTransType) {
		// Create a Transaction (acctgTrans)
		String acctgTransId = createAccountingTransaction(accountTransaction,
				acctgTransType);
		// Credit Member Deposit Account
		BigDecimal bdAmount = accountTransaction
				.getBigDecimal("transactionAmount");
		//String partyId = (String) userLogin.getString("partyId");
		String partyId = accountTransaction.getString("partyId");
		String memberDepositAccountId = getMemberDepositAccount(accountTransaction);
		String postingType = "C";
		String entrySequenceId = "00001";

		postTransactionEntry(delegator, bdAmount, partyId,
				memberDepositAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
		// Debit Bank Transfer
		String cashBankAccountId = getCashBankAccount(accountTransaction);
		postingType = "D";
		entrySequenceId = "00002";
		postTransactionEntry(delegator, bdAmount, partyId, cashBankAccountId,
				postingType, acctgTransId, acctgTransType, entrySequenceId);
		
		return acctgTransId;
	}

	private static String getCashBankAccount(GenericValue accountTransaction) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = accountTransaction.getDelegator();
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							"CHEQUEDEPOSITACCOUNT"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Could not get Cash Bank Account ");
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
			GenericValue accountTransaction) {

		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = accountTransaction.getDelegator();
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							"CHEQUEDEPOSITACCOUNT"), false);
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

	private static void postTransactionEntry(Delegator delegator,
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
	 * @author Japheth Odonya @when Aug 24, 2014 10:45:33 PM Create Account
	 *         Trans to head Postings (Double Entry)
	 * */
	private static String createAccountingTransaction(
			GenericValue accountTransaction, String acctgTransType
			) {

		GenericValue acctgTrans;
		String acctgTransId;
		Delegator delegator = accountTransaction.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

//		String partyId = (String) userLogin.get("partyId");
//		String createdBy = (String) userLogin.get("userLoginId");
		String partyId = accountTransaction.getString("partyId");
		String createdBy = accountTransaction.getString("createdBy");
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
