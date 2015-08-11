package org.ofbiz.loans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.accountholdertransactions.RemittanceServices;
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
import org.ofbiz.loanclearing.LoanClearingServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/**
 * @author Japheth Odonya @when Aug 22, 2014 2:21:39 PM
 * 
 *         Loan Accounting - Posting Loan Related Stuff Disbursement and
 *         Repayment will be posted here
 * 
 * **/
public class LoanAccounting {

	public static Logger log = Logger.getLogger(LoanAccounting.class);

	public static synchronized String postDisbursement(GenericValue loanApplication,
			Map<String, String> userLogin) {
		Map<String, Object> result = FastMap.newInstance();
		Long loanApplicationId = loanApplication
				.getLong("loanApplicationId");// (String)context.get("loanApplicationId");
		log.info("What we got is ############ " + loanApplicationId);
		Delegator delegator;
		delegator = loanApplication.getDelegator();
		// GenericValue accountTransaction = null;
		//loanApplicationId = loanApplicationId.replaceFirst(",", "");
		//Long theLoanApplicationId = Long.valueOf(loanApplicationId);
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

		String accountTransactionParentId = getAccountTransactionParentId(loanApplication, userLogin);
		// Creates Charge entry for each charge on loan application
		// Also creates a

		acctgTransType = "SERVICE_CHARGES";
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createChargesEntries(loanApplication, userLogin, glAcctTypeIdcharges, accountTransactionParentId, acctgTransId);
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
		createLoanDisbursementAccountingTransaction(loanApplication, userLogin, accountTransactionParentId, acctgTransId);
		
		//If the Loan Was cleared - get cleared amount and post to mpa 
		if (loanWasCleared(loanApplicationId)){
			
			//Get total amount cleared
			BigDecimal bdTotalCleared = getTotalClearedAmount(loanApplicationId);
			//Debit Member Account with cleared amount
			//AccHolderTransactionServices.cashWithdrawal(amount, memberAccountId, withdrawalType);
			//Get Savings Member Account ID given loanApplicationId
			Long memberAccountId = LoanUtilities.getSavingsMemberAccountId(loanApplicationId);
			
			//Get total charges based on rates
			BigDecimal bdTotalLoanCost = BigDecimal.ZERO;
			bdTotalLoanCost = bdTotalLoanCost.add(bdTotalCleared);
			
			List<Long> listLoanApplicationIDs = getLoanApplicationIDsCleared(loanApplicationId);
			
			BigDecimal bdTotalCharge = BigDecimal.ZERO;
			for (Long clearedLoanApplicationId : listLoanApplicationIDs) {
				
				BigDecimal bdTotalLoanBalanceAmount = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(clearedLoanApplicationId);
				BigDecimal bdInterestAmount = LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString());
				BigDecimal bdTotalInsuranceAmount = LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString());
				log.info(" LLLLLLLL Loan Amount to offset AAAAAAAAAA"+bdTotalLoanBalanceAmount);
				
				//LoansProcessingServices.get
				bdTotalLoanCost = bdTotalLoanCost.add(LoanRepayments.getTotalInterestByLoanDue(clearedLoanApplicationId.toString()));
				bdTotalLoanCost = bdTotalLoanCost.add(LoanRepayments.getTotalInsurancByLoanDue(clearedLoanApplicationId.toString()));
			
				log.info("1CCCCCCCCCCCC Total Loan Balance amount computed "+bdTotalLoanBalanceAmount);
				//BigDecimal bdTotal = bdTotalLoanBalanceAmount.add(bdInterestAmount).add(bdTotalInsuranceAmount);
				BigDecimal bdTotal = bdTotalLoanBalanceAmount;
				bdTotal = bdTotal.add(bdInterestAmount);
				bdTotal = bdTotal.add(bdTotalInsuranceAmount);
				
				log.info("2CCCCCCCCCCCC Total Loan Balance after adding interest and insurance "+bdTotal);
				bdTotalCharge = bdTotalCharge.add(org.ofbiz.loans.LoanServices.getLoanClearingCharge(clearedLoanApplicationId, bdTotal));
				log.info("3CCCCCCCCCCCC Charge Computed in iteration "+bdTotalCharge);
				
				saveLoanRepaymentClearance(clearedLoanApplicationId, bdTotalLoanBalanceAmount, bdInterestAmount, bdTotalInsuranceAmount, acctgTransId);
			}
			
			//AccHolderTransactionServices.memberTransactionDeposit(bdTotalLoanCost, memberAccountId, userLogin, "LOANCLEARANCE", accountTransactionParentId, null);
			AccHolderTransactionServices.memberTransactionDeposit(bdTotalLoanCost, memberAccountId, userLogin, "LOANCLEARANCE", accountTransactionParentId, null, acctgTransId, null, loanApplicationId);
			
			//Debit Member Account with the rate charged for clearances
			//AccHolderTransactionServices.memberTransactionDeposit(bdTotalCharge, memberAccountId, userLogin, "LOANCLEARANCECHARGES", accountTransactionParentId, null);
			log.info("4CCCCCCCCCCCC The Charge passed to Account Transaction for capture "+bdTotalCharge);
			AccHolderTransactionServices.memberTransactionDeposit(bdTotalCharge, memberAccountId, userLogin, "LOANCLEARANCECHARGES", accountTransactionParentId, null, acctgTransId, null, loanApplicationId);
			
			//Post the clearance charges
			
		}
		try {
			
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.put("disbursementResult", "posted");
		return "success";
	}
	
	
	public static BigDecimal getTotalClearedAmount(Long theLoanApplicationId) {
		// TODO Auto-generated method stub
		List<GenericValue> loanClearELI = null; // =
		EntityConditionList<EntityExpr> loanClearConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.EQUALS, theLoanApplicationId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", loanClearConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// get loanClearId
		Long loanClearId = null;
		for (GenericValue genericValue : loanClearELI) {
			loanClearId = genericValue.getLong("loanClearId");
		}

		BigDecimal bdTotal = LoanClearingServices
				.getTotalAmountToClear(loanClearId);

		return bdTotal;
	}

	private static boolean loanWasCleared(Long theLoanApplicationId) {
		List<GenericValue> loanClearELI = null; // =
		EntityConditionList<EntityExpr> loanClearConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.EQUALS, theLoanApplicationId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", loanClearConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if ((loanClearELI != null) && (loanClearELI.size() > 0)) {
			return true;
		}

		return false;
	}

	
	private static List<Long> getLoanApplicationIDsCleared(Long theLoanApplicationId) {
		// TODO Auto-generated method stub
		List<GenericValue> loanClearELI = null; // =
		EntityConditionList<EntityExpr> loanClearConditions = EntityCondition
				.makeCondition(UtilMisc.toList(

				EntityCondition.makeCondition("loanApplicationId",
						EntityOperator.EQUALS, theLoanApplicationId)

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanClearELI = delegator.findList("LoanClear", loanClearConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// get loanClearId
		Long loanClearId = null;
		for (GenericValue genericValue : loanClearELI) {
			loanClearId = genericValue.getLong("loanClearId");
		}

		List<Long> listLoanApplicationIds = new ArrayList<Long>();
		listLoanApplicationIds = LoanClearingServices.getLoanApplicationIDsCleared(loanClearId);

		return listLoanApplicationIds;
	}
	private static String getAccountTransactionParentId(
			GenericValue loanApplication, Map<String, String> userLogin) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue accountTransactionParent;
		String accountTransactionParentId = delegator
				.getNextSeqId("AccountTransactionParent");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = (String) userLogin.get("partyId");

		Long partyId = loanApplication.getLong("partyId");

		String memberAccountId = getMemberAccountId(loanApplication);
		memberAccountId = memberAccountId.replaceAll(",", "");
		accountTransactionParent = delegator.makeValidValue(
				"AccountTransactionParent", UtilMisc.toMap(
						"accountTransactionParentId",
						accountTransactionParentId, "isActive", "Y",
						"createdBy", createdBy, "updatedBy", updatedBy,
						"branchId", branchId, "partyId", partyId,
						"memberAccountId", Long.valueOf(memberAccountId)));
		try {
			delegator.createOrStore(accountTransactionParent);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}

		return accountTransactionParentId;
	}

	/****
	 * @author Japheth Odonya @when Aug 22, 2014 2:59:33 PM Create Disbursement
	 *         Transactions in the source document
	 * */
	private static void createLoanDisbursementAccountingTransaction(
			GenericValue loanApplication, Map<String, String> userLogin,
			String accountTransactionParentId, String acctgTransId) {
		// Create an Account Holder Transaction for this disbursement

		BigDecimal transactionAmount = loanApplication.getBigDecimal("loanAmt");

		Long savingsAccountProductId = getAccountProductGivenCodeId("999");

		String memberAccountId = getMemberAccountId(loanApplication,
				savingsAccountProductId);
		String transactionType = "LOANDISBURSEMENT";

		// Retention
		GenericValue loanProduct = LoanUtilities.getLoanProduct(loanApplication
				.getLong("loanProductId"));
		// Check if the Loan Retains BOSA
		if ((loanProduct.getString("retainBOSADeposit") != null)
				&& (loanProduct.getString("retainBOSADeposit").equals("Yes"))) {
			// This loan retains for BOSA deposit e.g JEKI LOAN, lets get the
			// percentage retained and add it to the
			// member deposits
			// BigDecimal percentageOfMemberNetSalaryAmt =

			BigDecimal percentageDepositRetained = loanProduct
					.getBigDecimal("percentageDepositRetained");
			if (percentageDepositRetained != null) {
				// Process the Retention
				BigDecimal bdBosaRetainedAmount = (percentageDepositRetained
						.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
						.multiply(transactionAmount).setScale(4,
								RoundingMode.HALF_UP);

				Long memberDepositProductId = getAccountProductGivenCodeId("901");
				String memberDepositAccountId = getMemberAccountId(
						loanApplication, memberDepositProductId);

				String retentionTransactionType = "RETAINEDASDEPOSIT";

				createTransaction(loanApplication, retentionTransactionType,
						userLogin, memberDepositAccountId,
						bdBosaRetainedAmount, null, accountTransactionParentId, acctgTransId);

				transactionAmount = transactionAmount
						.subtract(bdBosaRetainedAmount);

			} else {
				log.error(" percentageDepositRetained ########### The Retention Value is not specified #####333");
			}

			// tt
		}

		createTransaction(loanApplication, transactionType, userLogin,
				memberAccountId, transactionAmount, null,
				accountTransactionParentId, acctgTransId);
	}

	// Get Account Product Id given Code, for instance, Savings Account is code
	// 999

	private static Long getAccountProductGivenCodeId(String code) {
		Long accountProductId = null;

		List<GenericValue> accountProductELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> accountProductConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"code", EntityOperator.EQUALS, code)),
						EntityOperator.AND);
		try {
			accountProductELI = delegator.findList("AccountProduct",
					accountProductConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue accountProduct = null;
		for (GenericValue genericValue : accountProductELI) {
			accountProduct = genericValue;
		}

		if (accountProduct != null) {
			accountProductId = accountProduct.getLong("accountProductId");
		}

		return accountProductId;
	}

	private static void createTransaction(GenericValue loanApplication,
			String transactionType, Map<String, String> userLogin,
			String memberAccountId, BigDecimal transactionAmount,
			String productChargeId, String accountTransactionParentId, String acctgTransId) {
		Delegator delegator = loanApplication.getDelegator();
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = (String) userLogin.get("partyId");
		Long partyId = loanApplication.getLong("partyId");
		String increaseDecrease;
		if (productChargeId == null) {
			increaseDecrease = "I";
		} else {
			increaseDecrease = "D";
			productChargeId = productChargeId.replaceAll(",", "");
		}
		Long productChargeIdLong;
		if (productChargeId != null) {
			productChargeIdLong = Long.valueOf(productChargeId);
		} else {
			productChargeIdLong = null;
		}
		
		Long loanApplicationId = null;
		if ((loanApplication != null) && (loanApplication.getLong("loanApplicationId") != null)){
			loanApplicationId = loanApplication.getLong("loanApplicationId");
		}

		memberAccountId = memberAccountId.replaceAll(",", "");
		accountTransaction = delegator.makeValidValue("AccountTransaction",
				UtilMisc.toMap("accountTransactionId", accountTransactionId,
						"accountTransactionParentId",
						accountTransactionParentId, "isActive", "Y",
						"createdBy", createdBy, "updatedBy", updatedBy,
						"branchId", branchId, "partyId", partyId,
						"increaseDecrease", increaseDecrease, "slipNumber",
						AccHolderTransactionServices.getNextSlipNumber(),
						"memberAccountId", Long.valueOf(memberAccountId),
						"productChargeId", productChargeIdLong,
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "acctgTransId", acctgTransId, "loanApplicationId", loanApplicationId));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
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
		
		GenericValue accountProduct = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");
		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = loanApplication.getDelegator();
		memberId = memberId.replaceAll(",", "");
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)), EntityCondition.makeCondition(
						"accountProductId", EntityOperator.EQUALS, accountProductId)),
						EntityOperator.AND);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue memberAccount = null;
		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		String memberAccountId = "";

		if (memberAccount != null) {
			memberAccountId = memberAccount.getString("memberAccountId");
		}

		return memberAccountId;
	}

	private static String getMemberAccountId(GenericValue loanApplication,
			Long accountProductId) {
		String memberId = loanApplication.getString("partyId");

		List<GenericValue> memberAccountELI = new ArrayList<GenericValue>();
		Delegator delegator = loanApplication.getDelegator();
		memberId = memberId.replaceAll(",", "");
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS,
						Long.valueOf(memberId)),

				EntityCondition.makeCondition("accountProductId",
						EntityOperator.EQUALS, accountProductId)

				), EntityOperator.AND);
		try {
			memberAccountELI = delegator.findList("MemberAccount",
					memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue memberAccount = null;
		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
		}

		String memberAccountId = "";

		if (memberAccount != null) {
			memberAccountId = memberAccount.getString("memberAccountId");
		}

		return memberAccountId;
	}

	/***
	 * @author Japheth Odonya @when Aug 22, 2014 2:58:17 PM Creates Charge entry
	 *         for each charge on loan application
	 * **/
	private static void createChargesEntries(GenericValue loanApplication,
			Map<String, String> userLogin, String acctgTransType,
			String accountTransactionParentId, String acctgTransId) {
		// Give Loan Application get the charges and post to each
		Delegator delegator = loanApplication.getDelegator();
		List<GenericValue> loanApplicationChargeELI = null;
		String loanApplicationId = loanApplication
				.getString("loanApplicationId");

		// Pick only upfront payable charges like negotiation or appraisal fee
		// TODO - Add filter for upfront
		// chargedUpfront
		loanApplicationId = loanApplicationId.replaceAll(",", "");
		EntityConditionList<EntityExpr> chargesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"loanApplicationId", EntityOperator.EQUALS,
						Long.valueOf(loanApplicationId)), EntityCondition
						.makeCondition("chargedUpfront", EntityOperator.EQUALS,
								"Y")), EntityOperator.AND);
		try {
			loanApplicationChargeELI = delegator.findList(
					"LoanApplicationCharge", chargesConditions, null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal transactionAmount;
		String memberAccountId = getMemberAccountId(loanApplication);
		// for each charge make a post
		for (GenericValue loanApplicationCharge : loanApplicationChargeELI) {
			// Make entries for this charge
			processCharge(loanApplication, userLogin, acctgTransType,
					loanApplicationCharge, acctgTransId);

			// transactionAmount =
			// loanApplicationCharge.getBigDecimal("fixedAmount");
			BigDecimal dbONEHUNDRED = new BigDecimal(100);
			transactionAmount = loanApplicationCharge
					.getBigDecimal("rateAmount")
					.multiply(loanApplication.getBigDecimal("loanAmt"))
					.divide(dbONEHUNDRED, 4, RoundingMode.HALF_UP);

			String transactionType = getChargeName(loanApplicationCharge);
			String productChargeId = loanApplicationCharge
					.getString("productChargeId");
			createTransaction(loanApplication, transactionType, userLogin,
					memberAccountId, transactionAmount, productChargeId,
					accountTransactionParentId, acctgTransId);
		}

	}

	/**
	 * Make a double entry post for each charge
	 * */
	private static void processCharge(GenericValue loanApplication,
			Map<String, String> userLogin, String acctgTransType,
			GenericValue loanApplicationCharge, String acctgTransId) {
		//String acctgTransId = createAccountingTransaction(loanApplication,
		//		acctgTransType, userLogin);

		log.info(" ### Transaction ID Charge ##### " + acctgTransId);

		// System.exit(0);
		// Get Service Charge Account (To Credit)
		String chargeAccountId = getChargeAccount(loanApplicationCharge);
		// Get Member Deposits Account (to Credit)
		String memberDepositsAccountId = getMemberDepositsAccountToCharge(loanApplication);
		BigDecimal dbChargeAmount = loanApplicationCharge
				.getBigDecimal("fixedAmount");

		Delegator delegator = loanApplication.getDelegator();
		String partyId = (String) userLogin.get("partyId");
		String postingType = "C";
		String entrySequenceId = "00003";
		// Post to charge service (Cr)
		postTransactionEntry(delegator, dbChargeAmount, partyId,
				chargeAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId, userLogin);
		
		// Debit the Member Deposit
		postingType = "D";
		entrySequenceId = "00004";
		//postTra
		postTransactionEntry(delegator, dbChargeAmount, partyId,
				memberDepositsAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId, userLogin);
		
	}

	private static String getMemberDepositsAccountToCharge(
			GenericValue loanApplication) {
//		GenericValue accountHolderTransactionSetup = null;
//		Delegator delegator = loanApplication.getDelegator();
//		try {
//			accountHolderTransactionSetup = delegator.findOne(
//					"AccountHolderTransactionSetup", UtilMisc.toMap(
//							"accountHolderTransactionSetupId",
//							"MEMBERTRANSACTIONCHARGE"), false);
//		} catch (GenericEntityException e) {
//			e.printStackTrace();
//			log.error("######## Cannot Get Member Deposit Account in Member Transaction Charge, make sure there is a record in Account Holder Transaction Setup with ID MEMBERTRANSACTIONCHARGE and accounts configured ");
//		}

		String memberDepositAccountId = "";
//		if (accountHolderTransactionSetup != null) {
//			memberDepositAccountId = accountHolderTransactionSetup
//					.getString("memberDepositAccId");
//		} else {
//			log.error("######## Cannot Get Member Deposit Account in Member Transaction Charge, make sure there is a record in Account Holder Transaction Setup with ID MEMBERTRANSACTIONCHARGE and accounts configured ");
//		}
		
		//Pick this from members savings account
		GenericValue accountProduct = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		memberDepositAccountId = accountProduct.getString("glAccountId");
		
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
			productCharge = delegator.findOne(
					"ProductCharge",
					UtilMisc.toMap("productChargeId",
							Long.valueOf(productChargeId)), false);
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
				acctgTransType, entrySequenceId, userLogin);
	}

	public static void postTransactionEntry(Delegator delegator,
			BigDecimal bdLoanAmount, String partyId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId, Map<String, String> userLogin) {
		GenericValue acctgTransEntry;
		
		String employeeBranchId = null;
				
		if ((userLogin != null) && (!userLogin.get("userLoginId").equals("admin"))){
			employeeBranchId = AccHolderTransactionServices.getEmployeeBranch(userLogin.get("partyId"));
		} else{
			employeeBranchId = "Company";
		}
		
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", partyId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", employeeBranchId, "amount",
						bdLoanAmount, "currencyUomId", "KES", "origAmount",
						bdLoanAmount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post an entry");
		}
	}
	
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
						"AES_NOT_RECONCILED", "acctgTransId", acctgTransId));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post an entry");
		}
	}
	
	
	public static void postTransactionEntryParty(Delegator delegator,
			BigDecimal bdLoanAmount, String partyId, String organizationPartyId,
			String loanReceivableAccount, String postingType,
			String acctgTransId, String acctgTransType, String entrySequenceId) {
		GenericValue acctgTransEntry;
		acctgTransEntry = delegator.makeValidValue("AcctgTransEntry", UtilMisc
				.toMap("acctgTransId", acctgTransId, "acctgTransEntrySeqId",
						entrySequenceId, "partyId", partyId, "glAccountTypeId",
						acctgTransType, "glAccountId", loanReceivableAccount,

						"organizationPartyId", organizationPartyId, "amount",
						bdLoanAmount, "currencyUomId", "KES", "origAmount",
						bdLoanAmount, "origCurrencyUomId", "KES",
						"debitCreditFlag", postingType, "reconcileStatusId",
						"AES_NOT_RECONCILED"));

		try {
			delegator.createOrStore(acctgTransEntry);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could post an entry");
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
				acctgTransType, entrySequenceId,userLogin);
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
	public static String createAccountingTransaction(
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
				"acctgTransId", acctgTransId, "acctgTransTypeId",
				acctgTransType, "transactionDate", currentDateTime, "isPosted",
				"Y", "postedDate", currentDateTime, "glFiscalTypeId", "ACTUAL",
				"partyId", partyId, "createdByUserLogin", createdBy,
				"createdDate", currentDateTime, "lastModifiedDate",
				currentDateTime, "lastModifiedByUserLogin", createdBy));
		// try {
		// acctgTrans = delegator.createSetNextSeqId(acctgTrans);
		// } catch (GenericEntityException e1) {
		// e1.printStackTrace();
		// }
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return acctgTransId;
	}
	
	public static void saveLoanRepayment(Long loanApplicationId, BigDecimal bdTotalLoanBalanceAmount, BigDecimal bdInterestAmount, BigDecimal bdTotalInsuranceAmount) {
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;
		
		GenericValue loanApplication = LoanUtilities.getLoanApplicationEntity(loanApplicationId);
		
		// Loan Principal
		loanPrincipal = LoanRepayments.getTotalPrincipaByLoanDue(String.valueOf(loanApplicationId));
		// Get This Loan's Interest
				
		loanInterest = bdInterestAmount;
		// Get This Loan's Insurance
		loanInsurance = bdTotalInsuranceAmount;
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = loanPrincipal.add(loanInterest).add(
				loanInsurance);

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		BigDecimal totalInterestDue = bdInterestAmount;
		
		BigDecimal totalInsuranceDue = bdTotalInsuranceAmount;
		BigDecimal totalPrincipalDue = bdTotalLoanBalanceAmount;
		
		BigDecimal totalLoanDue = totalInterestDue.add(totalInsuranceDue).add(
				totalPrincipalDue);

		
		Long partyId = loanApplication.getLong("partyId");
		GenericValue loanRepayment = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", "admin", "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}
	
	public static void saveLoanRepaymentClearance(Long loanApplicationId, BigDecimal bdTotalLoanBalanceAmount, BigDecimal bdInterestAmount, BigDecimal bdTotalInsuranceAmount, String acctgTransId) {
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;
		
		GenericValue loanApplication = LoanUtilities.getLoanApplicationEntity(loanApplicationId);
		
		// Loan Principal
		loanPrincipal = bdTotalLoanBalanceAmount;//LoanRepayments.getTotalPrincipaByLoanDue(String.valueOf(loanApplicationId));
		// Get This Loan's Interest
				
		loanInterest = bdInterestAmount;
		// Get This Loan's Insurance
		loanInsurance = bdTotalInsuranceAmount;
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = loanPrincipal.add(loanInterest).add(
				loanInsurance);

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		BigDecimal totalInterestDue = bdInterestAmount;
		
		BigDecimal totalInsuranceDue = bdTotalInsuranceAmount;
		BigDecimal totalPrincipalDue = bdTotalLoanBalanceAmount;
		
		BigDecimal totalLoanDue = totalInterestDue.add(totalInsuranceDue).add(
				totalPrincipalDue);

		
		Long partyId = loanApplication.getLong("partyId");
		GenericValue loanRepayment = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", "admin", "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount, "acctgTransId", acctgTransId));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}


}
