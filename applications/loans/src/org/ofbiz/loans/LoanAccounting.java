package org.ofbiz.loans;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;

/**
 * @author Japheth Odonya @when Aug 22, 2014 2:21:39 PM
 * 
 *         Loan Accounting - Posting Loan Related Stuff Disbursement and
 *         Repayment will be posted here
 * 
 * **/
public class LoanAccounting {

	public static Logger log = Logger.getLogger(LoanAccounting.class);

	public static String postDisbursement(GenericValue loanApplication,
			Map<String, String> userLogin) {
		Map<String, Object> result = FastMap.newInstance();
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");// (String)context.get("loanApplicationId");
		log.info("What we got is ############ " + loanApplicationId);
		Delegator delegator;
		delegator = loanApplication.getDelegator();
		// GenericValue accountTransaction = null;
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Creates a record in AcctgTrans
		String acctgTransType = "LOAN_RECEIVABLE";
		String glAcctTypeIdMemberDepo = "CURRENT_LIABILITY";
		String glAcctTypeIdLoans = "CURRENT_ASSET";
		String glAcctTypeIdcharges = "OTHER_INCOME";
		String acctgTransId = createAccountingTransaction(loanApplication,
				acctgTransType, userLogin);
		
		log.info("### Transaction ID## "+acctgTransId);
		// Creates a record in AcctgTransEntry for Member Deposit Account
		createMemberDepositEntry(loanApplication, acctgTransId, userLogin,
				glAcctTypeIdMemberDepo);

		// Creates a record in AcctgTransEntry for Loans Receivable
		createLoanReceivableEntry(loanApplication, acctgTransId, userLogin,
				glAcctTypeIdLoans);

		// Creates Charge entry for each charge on loan application
		// Also creates a

		acctgTransType = "SERVICE_CHARGES";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createChargesEntries(loanApplication, userLogin, glAcctTypeIdcharges);
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		createLoanDisbursementAccountingTransaction(loanApplication, userLogin);
		try {
			
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.put("disbursementResult", "posted");
		return "";
	}


	/****
	 * @author Japheth Odonya @when Aug 22, 2014 2:59:33 PM Create Disbursement
	 *         Transactions in the source document
	 * */
	private static void createLoanDisbursementAccountingTransaction(
			GenericValue loanApplication, Map<String, String> userLogin) {
		// Create an Account Holder Transaction for this disbursement
		
		BigDecimal transactionAmount = loanApplication.getBigDecimal("loanAmt");
		String memberAccountId = getMemberAccountId(loanApplication);
		String transactionType = "LOANDISBURSEMENT";

		createTransaction(loanApplication, transactionType, userLogin, memberAccountId, transactionAmount, null);
	}

	private static void createTransaction(GenericValue loanApplication, String transactionType, Map<String, String> userLogin, String memberAccountId,
			BigDecimal transactionAmount, String productChargeId) {
		Delegator delegator = loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = (String) userLogin.get("partyId");
		String partyId =  loanApplication.getString("partyId");
		String increaseDecrease;
		if (productChargeId == null){
			increaseDecrease = "I";
		} else{
			increaseDecrease = "D";
		}
		
		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"isActive", "Y", "createdBy", createdBy, "updatedBy",
						updatedBy, "branchId", branchId,
						"partyId", partyId,
						"increaseDecrease", increaseDecrease,
						"memberAccountId", memberAccountId,
						"productChargeId", productChargeId,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType));
//		try {
//			accountTransaction = delegator
//					.createSetNextSeqId(accountTransaction);
//		} catch (GenericEntityException e1) {
//			e1.printStackTrace();
//		}
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Disbursement Transaction");
		}
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 5:54:48 PM Get a Member Account
	 *         Id given a loan application
	 * 
	 *         Pick out the Member and then look for a single Account in the
	 *         MemberAccount
	 * */
	private static String getMemberAccountId(GenericValue loanApplication) {
		String memberId = loanApplication.getString("partyId");

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = loanApplication.getDelegator();
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					EntityCondition.makeCondition("partyId", memberId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue memberAccount = null;
		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}
		
		String memberAccountId = "";
		
		if (memberAccount != null){
			memberAccountId = memberAccount.getString("memberAccountId");
		}

		return memberAccountId;
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 2:58:17 PM Creates Charge entry
	 *         for each charge on loan application
	 * **/
	private static void createChargesEntries(GenericValue loanApplication,
			Map<String, String> userLogin, String acctgTransType) {
		// Give Loan Application get the charges and post to each
		Delegator delegator = loanApplication.getDelegator();
		List<GenericValue> loanApplicationChargeELI = null;
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");
		try {
			loanApplicationChargeELI = delegator.findList(
					"LoanApplicationCharge", EntityCondition.makeCondition(
							"loanApplicationId", loanApplicationId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal transactionAmount;
		String memberAccountId = getMemberAccountId(loanApplication);
		// for each charge make a post
		for (GenericValue loanApplicationCharge : loanApplicationChargeELI) {
			// Make entries for this charge
			processCharge(loanApplication, userLogin, acctgTransType,
					loanApplicationCharge);
			
			transactionAmount = loanApplicationCharge.getBigDecimal("fixedAmount");
			String transactionType = getChargeName(loanApplicationCharge);
			String productChargeId = loanApplicationCharge.getString("productChargeId");
			createTransaction(loanApplication, transactionType, userLogin, memberAccountId, transactionAmount, productChargeId);
		}

	}

	/**
	 * Make a double entry post for each charge
	 * */
	private static void processCharge(GenericValue loanApplication,
			Map<String, String> userLogin, String acctgTransType,
			GenericValue loanApplicationCharge) {
		String acctgTransId = createAccountingTransaction(loanApplication,
				acctgTransType, userLogin);
		
		log.info(" ### Transaction ID Charge ##### "+acctgTransId);
		
		//System.exit(0);
		// Get Service Charge Account (To Credit)
		String chargeAccountId = getChargeAccount(loanApplicationCharge);
		// Get Member Deposits Account (to Credit)
		String memberDepositsAccountId = getMemberDepositsAccountToCharge(loanApplication);
		BigDecimal dbChargeAmount = loanApplicationCharge
				.getBigDecimal("fixedAmount");

		Delegator delegator = loanApplication.getDelegator();
		String partyId = (String) userLogin.get("partyId");
		String postingType = "C";
		String entrySequenceId = "00001";
		// Post to charge service (Cr)
		postTransactionEntry(delegator, dbChargeAmount, partyId,
				chargeAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId);
		// Debit the Member Deposit
		postingType = "D";
		entrySequenceId = "00002";
		postTransactionEntry(delegator, dbChargeAmount, partyId,
				memberDepositsAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
	}

	private static String getMemberDepositsAccountToCharge(
			GenericValue loanApplication) {
		GenericValue accountHolderTransactionSetup = null;
		Delegator delegator = loanApplication.getDelegator();
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							"MEMBERTRANSACTIONCHARGE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot Get Member Deposit Account in Member Transaction Charge, make sure there is a record in Account Holder Transaction Setup with ID MEMBERTRANSACTIONCHARGE and accounts configured ");
		}

		String memberDepositAccountId = "";
		if (accountHolderTransactionSetup != null) {
			memberDepositAccountId = accountHolderTransactionSetup
					.getString("memberDepositAccId");
		} else {
			log.error("######## Cannot Get Member Deposit Account in Member Transaction Charge, make sure there is a record in Account Holder Transaction Setup with ID MEMBERTRANSACTIONCHARGE and accounts configured ");
		}
		return memberDepositAccountId;
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 5:13:27 PM Get Charge Account
	 *         for the charge in the application
	 * */
	private static String getChargeAccount(GenericValue loanApplicationCharge) {
		String productChargeId = loanApplicationCharge
				.getString("productChargeId");

		Delegator delegator = loanApplicationCharge.getDelegator();
		GenericValue productCharge = null;

		try {
			productCharge = delegator.findOne("ProductCharge",
					UtilMisc.toMap("productChargeId", productChargeId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get product charge ");
		}

		String chargeAccountId = "";
		if (productCharge != null) {
			chargeAccountId = productCharge.getString("chargeAccountId");
		} else {
			log.error("######## Cannot get product charge !! ");
		}
		return chargeAccountId;
	}
	
	private static String getChargeName(GenericValue loanApplicationCharge) {
		String productChargeId = loanApplicationCharge
				.getString("productChargeId");

		Delegator delegator = loanApplicationCharge.getDelegator();
		GenericValue productCharge = null;

		try {
			productCharge = delegator.findOne("ProductCharge",
					UtilMisc.toMap("productChargeId", productChargeId), false);
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


	/**
	 * @author Japheth Odonya @when Aug 22, 2014 2:58:55 PM Creates a record in
	 *         AcctgTransEntry for Loans Receivable
	 * */
	private static void createLoanReceivableEntry(GenericValue loanApplication,
			String acctgTransId, Map<String, String> userLogin,
			String acctgTransType) {
		Delegator delegator = loanApplication.getDelegator();
		// Credit Member Deposit Account
		String loanReceivableAccount = getLoanReceivableAccount(delegator);
		String partyId = (String) userLogin.get("partyId");
		BigDecimal bdLoanAmount = loanApplication.getBigDecimal("loanAmt");
		String postingType = "D";
		String entrySequenceId = "00002";
		postTransactionEntry(delegator, bdLoanAmount, partyId,
				loanReceivableAccount, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
	}

	private static void postTransactionEntry(Delegator delegator,
			BigDecimal bdLoanAmount, String partyId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator
				.makeValidValue("AcctgTransEntry", UtilMisc.toMap(
						"acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", partyId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", "Company", "amount", bdLoanAmount,
						"currencyUomId", "KES", "origAmount", bdLoanAmount,
						"origCurrencyUomId", "KES", "debitCreditFlag",
						postingType, "reconcileStatusId", "AES_NOT_RECONCILED"));
		
		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post a Loan Receivable entry");
		}
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 2:59:02 PM Creates a record in
	 *         AcctgTransEntry for Member Deposit Account
	 * */
	private static void createMemberDepositEntry(GenericValue loanApplication,
			String acctgTransId, Map<String, String> userLogin,
			String acctgTransType) {
		Delegator delegator = loanApplication.getDelegator();
		// Credit Member Deposit Account
		String memberDepositAccount = getMemberDepositAccount(delegator);
		String partyId = (String) userLogin.get("partyId");
		String postingType = "C";
		String entrySequenceId = "00001";
		BigDecimal bdLoanAmount = loanApplication.getBigDecimal("loanAmt");
		postTransactionEntry(delegator, bdLoanAmount, partyId,
				memberDepositAccount, postingType, acctgTransId,
				acctgTransType, entrySequenceId);
	}

	/**
	 * Get Member Deposit Account
	 * 
	 * */
	private static String getMemberDepositAccount(Delegator delegator) {
		GenericValue accountHolderTransactionSetup = null;

		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							"LOANDISBURSEMENTACCOUNT"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot Get Member Deposit Account in Loan Disbursement, make sure there is a rcord in Account Holder Transaction Setup with ID LOANDISBURSEMENTACCOUNT and accounts configured ");
			return "Cannot Get Loan Disbursement account";
		}

		String memberDepositAccountId = "";
		if (accountHolderTransactionSetup != null) {
			memberDepositAccountId = accountHolderTransactionSetup
					.getString("memberDepositAccId");
		} else {
			log.error("######## Cannot Get Member Deposit Account in Loan Disbursement, make sure there is a rcord in Account Holder Transaction Setup with ID LOANDISBURSEMENTACCOUNT and accounts configured ");
		}
		return memberDepositAccountId;
	}

	private static String getLoanReceivableAccount(Delegator delegator) {
		GenericValue accountHolderTransactionSetup = null;

		// SaccoProduct
		try {
			accountHolderTransactionSetup = delegator.findOne(
					"AccountHolderTransactionSetup", UtilMisc.toMap(
							"accountHolderTransactionSetupId",
							"LOANDISBURSEMENTACCOUNT"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot Get Loan Receivable Account in Loan Disbursement, make sure there is a rcord in Account Holder Transaction Setup with ID LOANDISBURSEMENTACCOUNT and accounts configured ");
			return "Cannot Get Loan Receivable Account in Disbursement ";
		}

		String loanReceivableAccount = "";
		if (accountHolderTransactionSetup != null) {
			loanReceivableAccount = accountHolderTransactionSetup
					.getString("cashAccountId");
		} else {
			log.error("######## Cannot Get Loan Disbursement account, make sure there is a rcord in Account Holder Transaction Setup with ID LOANDISBURSEMENTACCOUNT and accounts configured ");
		}
		return loanReceivableAccount;
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 2:59:07 PM Creates a record in
	 *         AcctgTrans
	 * **/
	private static String createAccountingTransaction(
			GenericValue loanApplication, String acctgTransType,
			Map<String, String> userLogin) {

		GenericValue acctgTrans;
		String acctgTransId;
		Delegator delegator = loanApplication.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

		String partyId = (String) userLogin.get("partyId");
		String createdBy = (String) userLogin.get("userLoginId");

		Timestamp currentDateTime = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		acctgTrans = delegator.makeValidValue("AcctgTrans", UtilMisc.toMap(
				"acctgTransId", acctgTransId,
				"acctgTransTypeId", acctgTransType,
				"transactionDate",
				currentDateTime, "isPosted", "Y", "postedDate",
				currentDateTime, "glFiscalTypeId", "ACTUAL", "partyId",
				partyId, "createdByUserLogin", createdBy, "createdDate",
				currentDateTime, "lastModifiedDate", currentDateTime,
				"lastModifiedByUserLogin", createdBy));
//		try {
//			acctgTrans = delegator.createSetNextSeqId(acctgTrans);
//		} catch (GenericEntityException e1) {
//			e1.printStackTrace();
//		}
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return acctgTransId;
	}

}
