package org.ofbiz.accounting.finaccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.math.RoundingMode;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.accounting.ledger.GeneralLedgerServices;
import org.ofbiz.base.util.UtilDateTime;
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

public class LoanDisbursement {
	public static final String module = LoanDisbursement.class.getName();
	public static Logger log = Logger.getLogger(LoanDisbursement.class);

	public static Map<String, Object> approveLoanTop(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		Long loanApplicationId = (Long) context.get("loanApplicationId");
		String loanTopUpId = (String) context.get("loanTopUpId");

		GenericValue loanTopUpRecord = getLoanTopUpRecord(delegator, loanApplicationId, loanTopUpId);
		loanTopUpRecord.set("statusName", "APPROVED");
		try {
			loanTopUpRecord.store();
		} catch (Exception e) {

		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("loanApplicationId", loanApplicationId.toString());
		return result;
	}

	public static Map<String, Object> disburseLoanTop(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		Long loanApplicationId = (Long) context.get("loanApplicationId");
		String loanTopUpId = (String) context.get("loanTopUpId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		GenericValue loanTopUpRecord = getLoanTopUpRecord(delegator, loanApplicationId, loanTopUpId);
		GenericValue loan = getLoan(delegator, loanApplicationId.toString());

		String memberPartyId = loan.getLong("partyId").toString();
		String memberBranchId = LoanUtilities.getMemberBranchId(memberPartyId);

		// Create LoanTopUpLog Record
		GenericValue loanTopUpLog = delegator.makeValue("LoanTopUpLog");
		String loanTopUpLogId = delegator.getNextSeqId("LoanTopUpLog");

		loanTopUpLog.put("loanApplicationId", loanApplicationId);
		loanTopUpLog.put("loanTopUpLogId", loanTopUpLogId);
		loanTopUpLog.put("loanTopUpId", loanTopUpId);
		loanTopUpLog.put("topUpAmount", loanTopUpRecord.getBigDecimal("amount"));
		loanTopUpLog.put("originalAppliedamount", getOriginalAppliedAmount(delegator, loanApplicationId.toString()));
		loanTopUpLog.put("originalDisbursedamount", getOriginalDisbursedAmount(delegator, loanApplicationId.toString()));
		loanTopUpLog.put("disbursedBy", userLogin.getString("userLoginId"));
		try {
			loanTopUpLog.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// Set the New Disbursed Amount
		loan.set("loanAmt", loanTopUpRecord.getBigDecimal("amount").add(getOriginalDisbursedAmount(delegator, loanApplicationId.toString())));

		// Create AcctgTrans
		String acctgTransId = GeneralLedgerServices.createGlTransactionHeader(delegator, "LOAN_RECEIVABLE", UtilDateTime.nowTimestamp(), memberPartyId, userLogin);

		// Create AcctgTransEntry(CREDIT). Remember we are only posting the top
		// up amount only
		String glAcctTypeIdMemberDepo = "CURRENT_LIABILITY";
		String glAcctTypeIdLoans = "CURRENT_ASSET";
		String glAcctTypeIdcharges = "OTHER_INCOME";
		String memberDepositAcc = getMemberDepositAccount(delegator);
		String employeeBranchId = AccHolderTransactionServices.getEmployeeBranch(userLogin.getString("partyId"));
		// Credit Member Deposit Account
		BigDecimal creditAmount = GeneralLedgerServices.createGlTransactionEntry(delegator, acctgTransId, loanTopUpRecord.getBigDecimal("amount"), memberDepositAcc, employeeBranchId, glAcctTypeIdMemberDepo, "C", userLogin.getString("partyId"), "TOP UP LOAN");

		// Create AcctgTransEntry(DEBIT)
		String loanReceivableAccount = getLoanReceivableAccount(delegator);
		BigDecimal debitAmount = GeneralLedgerServices.createGlTransactionEntry(delegator, acctgTransId, loanTopUpRecord.getBigDecimal("amount"), loanReceivableAccount, employeeBranchId, glAcctTypeIdLoans, "D", userLogin.getString("partyId"), "TOP UP LOAN");

		// LoanDisbursement.applyCharges();
		chargeAppraisalFee(delegator, loan, acctgTransId, userLogin, loanTopUpRecord.getBigDecimal("amount"));

		// MPA Transactions
		createMPATransactions(delegator, loan, loanTopUpRecord.getBigDecimal("amount"), userLogin, acctgTransId);
		createMPATransactionsAppraisal(delegator, loan, loanTopUpRecord.getBigDecimal("amount"), userLogin, acctgTransId);

		loanTopUpRecord.set("statusName", "DISBURSED");
		try {
			loanTopUpRecord.store();
			loan.store();
		} catch (Exception e) {

		}
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("loanApplicationId", loanApplicationId.toString());
		return result;
	}

	public static void createMPATransactions(Delegator delegator, GenericValue loan, BigDecimal topUpAmount, GenericValue userLogin, String acctgTransId) {
		String accountTransactionParentId = getAccountTransactionParentId(delegator, loan, userLogin);
		createLoanDisbursementAccountingTransaction(loan, topUpAmount, userLogin, accountTransactionParentId, acctgTransId, delegator);
	}
	
	public static void createMPATransactionsAppraisal(Delegator delegator, GenericValue loan, BigDecimal topUpAmount, GenericValue userLogin, String acctgTransId) {
		String accountTransactionParentId = getAccountTransactionParentId(delegator, loan, userLogin);
//		createLoanDisbursementAccountingTransaction(loan, topUpAmount, userLogin, accountTransactionParentId, acctgTransId, delegator);
		createLoanDisbursementAccountingTransactionCharge(loan, topUpAmount, userLogin, accountTransactionParentId, acctgTransId, delegator);
	}
	

	private static void createLoanDisbursementAccountingTransaction(
			GenericValue loanApplication, BigDecimal topUpAmount, GenericValue userLogin,
			String accountTransactionParentId, String acctgTransId, Delegator delegator) {
		// Create an Account Holder Transaction for this disbursement

		BigDecimal transactionAmount = topUpAmount;

		Long savingsAccountProductId = getAccountProductGivenCodeId("999", delegator);

		String memberAccountId = getMemberAccountId(loanApplication,
				savingsAccountProductId, delegator);
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

				Long memberDepositProductId = getAccountProductGivenCodeId("901", delegator);
				String memberDepositAccountId = getMemberAccountId(
						loanApplication, memberDepositProductId, delegator);

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
	
	private static void createLoanDisbursementAccountingTransactionCharge(
			GenericValue loanApplication, BigDecimal topUpAmount, GenericValue userLogin,
			String accountTransactionParentId, String acctgTransId, Delegator delegator) {
		// Create an Account Holder Transaction for this disbursement

		BigDecimal transactionAmount = getChargeAmountGivenLoanApp(delegator, loanApplication, topUpAmount);

		Long savingsAccountProductId = getAccountProductGivenCodeId("999", delegator);

		String memberAccountId = getMemberAccountId(loanApplication,
				savingsAccountProductId, delegator);
		String transactionType = "Appraisal Fee";


		
		
		GenericValue accountTransaction;
		String accountTransactionId = delegator
				.getNextSeqId("AccountTransaction");
		String createdBy = (String) userLogin.get("userLoginId");
		String updatedBy = (String) userLogin.get("userLoginId");
		String branchId = (String) userLogin.get("partyId");
		Long partyId = loanApplication.getLong("partyId");
		String increaseDecrease = "D";
		
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
						"productChargeId", Long.valueOf("10001"),
						"transactionAmount", transactionAmount,
						"transactionType", transactionType, "acctgTransId", acctgTransId, "loanApplicationId", loanApplication.getLong("loanApplicationId")));
		try {
			delegator.createOrStore(accountTransaction);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("Could not create Transaction");
		}

		
	}

	private static void createTransaction(GenericValue loanApplication,
			String transactionType, GenericValue userLogin,
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
		if ((loanApplication != null) && (loanApplication.getLong("loanApplicationId") != null)) {
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

	private static Long getAccountProductGivenCodeId(String code, Delegator delegator) {
		Long accountProductId = null;

		List<GenericValue> accountProductELI = null;
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

	private static String getAccountTransactionParentId(Delegator delegator, GenericValue loan, GenericValue userLogin) {
		String accountTransactionParentId = null;

		GenericValue accountTransactionParent = null;
		accountTransactionParent = delegator.makeValue("AccountTransactionParent");
		accountTransactionParentId = delegator.getNextSeqId("AccountTransactionParent");
		String createdBy = (String) userLogin.getString("userLoginId");
		String updatedBy = (String) userLogin.getString("userLoginId");
		String branchId = (String) userLogin.getString("partyId");
		Long partyId = loan.getLong("partyId");
		String memberAccountId = getMemberAccountId(loan, delegator);

		accountTransactionParent.put("accountTransactionParentId", accountTransactionParentId);
		accountTransactionParent.put("isActive", "Y");
		accountTransactionParent.put("createdBy", createdBy);
		accountTransactionParent.put("updatedBy", updatedBy);
		accountTransactionParent.put("branchId", branchId);
		accountTransactionParent.put("partyId", partyId);
		accountTransactionParent.put("memberAccountId", Long.valueOf(memberAccountId));

		try {
			accountTransactionParent.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return accountTransactionParentId;
	}

	public static Map<String, Object> requestTopup(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		Long loanApplicationId = (Long) context.get("loanApplicationId");
		String narration = (String) context.get("narration");
		BigDecimal amount = (BigDecimal) context.get("amount");

		// Controlss

		// 1. Check that the credit amount is not exceeded by the Debit Lines
		// Get the MultiPayment Header
		GenericValue loanApplication = null;
		BigDecimal previousAppliedAmount = BigDecimal.ZERO;
		BigDecimal previousDisbursedAmount = BigDecimal.ZERO;
		BigDecimal maxAcceptableTopupAmount = BigDecimal.ZERO;
		BigDecimal requestedTopupAmount = amount;
		try {
			loanApplication = delegator.findOne("LoanApplication", UtilMisc.toMap("loanApplicationId", loanApplicationId), false);
			if (loanApplication != null) {
				previousDisbursedAmount = loanApplication.getBigDecimal("loanAmt");
				previousAppliedAmount = loanApplication.getBigDecimal("appliedAmt");
			}
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}

		maxAcceptableTopupAmount = getMaxAcceptableTopupAmount(previousAppliedAmount, previousDisbursedAmount);
		int compare = compareLoanTopValues(requestedTopupAmount, maxAcceptableTopupAmount);

		if (compare == 1) {
			return ServiceUtil.returnError("Requested Topup Amount is exceeding the originally applied amount of " + previousAppliedAmount);
		}

		GenericValue loanTopUp = delegator.makeValue("LoanTopUp");
		String loanTopUpId = delegator.getNextSeqId("LoanTopUp");

		loanTopUp.put("loanApplicationId", loanApplicationId);
		loanTopUp.put("narration", narration);
		loanTopUp.put("amount", amount);
		loanTopUp.put("loanTopUpId", loanTopUpId);
		loanTopUp.put("statusName", "CAPTURED");
		try {
			loanTopUp.create();
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("loanApplicationId", loanApplicationId.toString());

		return result;
	}

	private static BigDecimal getMaxAcceptableTopupAmount(BigDecimal previousAppliedAmount, BigDecimal previousDisbursedAmount) {
		return previousAppliedAmount.subtract(previousDisbursedAmount);
	}

	public static BigDecimal getMaxAcceptableTopupAmount(Delegator delegator, String loanApplicationId) {
		BigDecimal orginalAppliedAmount = getOriginalAppliedAmount(delegator, loanApplicationId);
		BigDecimal orginalDisbursedAmount = getOriginalDisbursedAmount(delegator, loanApplicationId);
		return orginalAppliedAmount.subtract(orginalDisbursedAmount);
	}

	public static BigDecimal getOriginalAppliedAmount(Delegator delegator, String loanApplicationId) {
		GenericValue loan = getLoan(delegator, loanApplicationId);
		return loan.getBigDecimal("appliedAmt");
	}

	public static BigDecimal getOriginalDisbursedAmount(Delegator delegator, String loanApplicationId) {
		GenericValue loan = getLoan(delegator, loanApplicationId);
		return loan.getBigDecimal("loanAmt");
	}

	private static GenericValue getLoan(Delegator delegator, String loanApplicationId) {
		GenericValue loanApplication = null;
		try {
			loanApplication = delegator.findOne("LoanApplication", UtilMisc.toMap("loanApplicationId", Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return loanApplication;
	}

	public static GenericValue getLoanTopUpRecord(Delegator delegator, Long loanApplicationId, String loanTopUpId) {
		Map<String, String> fields = UtilMisc.<String, String> toMap("loanApplicationId", loanApplicationId, "loanTopUpId", loanTopUpId);
		GenericValue loanTopUpRecord = null;

		try {
			loanTopUpRecord = delegator.findOne("LoanTopUp", fields, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return loanTopUpRecord;
	}

	public static GenericValue getLoanTopUpRecord(Delegator delegator, String loanApplicationId) {
		List<GenericValue> loanTopUpRecords = null;
		GenericValue loanTopUpRecord = null;
		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, Long.valueOf(loanApplicationId))
				));
		try {
			loanTopUpRecords = delegator.findList("LoanTopUp", cond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		loanTopUpRecord = loanTopUpRecords.get(0);
		return loanTopUpRecord;
	}

	private static int compareLoanTopValues(BigDecimal value1, BigDecimal value2) {
		int comparison = value1.compareTo(value2);

		String str1 = "####################### Both values are equal ";
		String str2 = "####################### First Value is greater ";
		String str3 = "####################### Second value is greater";

		if (comparison == 0)
			System.out.println(str1);
		else if (comparison == 1)
			System.out.println(str2);
		else if (comparison == -1)
			System.out.println(str3);

		return comparison;
	}

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

	public static void chargeAppraisalFeeOld(Delegator delegator, GenericValue loanApplication, String acctgTransId, GenericValue userLogin, BigDecimal topUpAmt) {
		Long appraisalFeeId = Long.valueOf("10001");

		List<GenericValue> loanApplicationCharges = null;

		EntityConditionList<EntityExpr> appraisalChargeCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("loanApplicationId", EntityOperator.EQUALS, loanApplication.getLong("loanApplicationId")),
				EntityCondition.makeCondition("productChargeId", EntityOperator.EQUALS, appraisalFeeId)
				), EntityOperator.AND);

		try {
			loanApplicationCharges = delegator.findList("LoanApplicationCharge", appraisalChargeCond, null, null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String partyId = userLogin.getString("partyId");
		String employeeBranchId = AccHolderTransactionServices.getEmployeeBranch(partyId);
		if (loanApplicationCharges != null) {
			for (GenericValue loanApplicationCharge : loanApplicationCharges) {
				BigDecimal chargeAmount = loanApplicationCharge.getBigDecimal("fixedAmount");
				String creditAcc = getChargeAccount(loanApplicationCharge);
				String debitAcc = getMemberDepositsAccountToCharge(loanApplication);
				String glAccountTypeId = "OTHER_INCOME";
				createLoanChargesGlTransactions(delegator, chargeAmount, debitAcc, creditAcc, acctgTransId, glAccountTypeId, employeeBranchId, partyId);
			}
		}

	}

	public static void chargeAppraisalFee(Delegator delegator, GenericValue loanApplication, String acctgTransId, GenericValue userLogin, BigDecimal topUpAmount) {
		Long appraisalFeeId = Long.valueOf("10001");
		String partyId = userLogin.getString("partyId");
		String employeeBranchId = AccHolderTransactionServices.getEmployeeBranch(partyId);
		String creditAcc = getCreditGlAccount(delegator, loanApplication, appraisalFeeId);
		String debitAcc = getMemberDepositsAccountToCharge(loanApplication);
		String glAccountTypeId = "OTHER_INCOME";

		BigDecimal chargeAmount = getChargeAmountGivenLoanApp(delegator, loanApplication, topUpAmount);
		createLoanChargesGlTransactions(delegator, chargeAmount, debitAcc, creditAcc, acctgTransId, glAccountTypeId, employeeBranchId, partyId);

	}

	private static String getCreditGlAccount(Delegator delegator, GenericValue loanApplication, Long appraisalFeeId) {
		GenericValue loanProduct = getLoanProductGivenLoanAppId(delegator, loanApplication.getLong("loanApplicationId"));

		GenericValue loanProductCharge = getLoanProductChargeGivenLoanProductIdAndChargeId(delegator, loanProduct.getLong("loanProductId"), appraisalFeeId);
		
		return getChargeAccount(loanProductCharge);
	}

	private static BigDecimal getChargeAmountGivenLoanApp(Delegator delegator, GenericValue loanApplication, BigDecimal topUpAmount) {
		Long appraisalFeeId = Long.valueOf("10001");
		BigDecimal chargeAmount = null;

		GenericValue loanProduct = getLoanProductGivenLoanAppId(delegator, loanApplication.getLong("loanApplicationId"));

		GenericValue loanProductCharge = getLoanProductChargeGivenLoanProductIdAndChargeId(delegator, loanProduct.getLong("loanProductId"), appraisalFeeId);

		chargeAmount = calculateChargeAmountGivenRateAndLoanAmt(loanProductCharge.getBigDecimal("rateAmount"), topUpAmount);

		return chargeAmount;
	}

	private static GenericValue getLoanProductChargeGivenLoanProductIdAndChargeId(Delegator delegator, Long loanProductId, Long appraisalFeeId) {
		List<GenericValue> loanApplicationCharges = null;
		

		
		EntityConditionList<EntityExpr> appraisalChargeCond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("loanProductId", EntityOperator.EQUALS, loanProductId),
				EntityCondition.makeCondition("productChargeId", EntityOperator.EQUALS, appraisalFeeId)
				), EntityOperator.AND);

		try {
			loanApplicationCharges = delegator.findList("LoanProductCharge", appraisalChargeCond, null, null, null, false);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return loanApplicationCharges.get(0);
	}

	private static GenericValue getLoanProductGivenLoanAppId(Delegator delegator, Long loanApplicationId) {
		GenericValue loanProduct = null;
		loanProduct = getLoan(delegator, loanApplicationId.toString());
		return loanProduct;
	}

	private static BigDecimal calculateChargeAmountGivenRateAndLoanAmt(BigDecimal rate, BigDecimal topUpAmount) {
		BigDecimal chargeAmount = null;

		chargeAmount = (rate.multiply(topUpAmount)).divide(new BigDecimal(100.00));

		return chargeAmount;
	}

	private static void createLoanChargesGlTransactions(Delegator delegator, BigDecimal chargeAmount, String debitAcc, String creditAcc, String acctgTransId, String glAccountTypeId, String employeeBranchId, String partyId) {

		BigDecimal debitAmount = GeneralLedgerServices.createGlTransactionEntry(delegator, acctgTransId, chargeAmount, debitAcc, employeeBranchId, glAccountTypeId, "D", partyId, "TOP UP LOAN CHARGES");
		BigDecimal creditAmount = GeneralLedgerServices.createGlTransactionEntry(delegator, acctgTransId, chargeAmount, creditAcc, employeeBranchId, glAccountTypeId, "C", partyId, "TOP UP LOAN CHARGES");
	}

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

	private static String getMemberDepositsAccountToCharge(
			GenericValue loanApplication) {

		String memberDepositAccountId = "";

		// Pick this from members savings account
		GenericValue accountProduct = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		memberDepositAccountId = accountProduct.getString("glAccountId");

		return memberDepositAccountId;
	}

	private static String getMemberAccountId(GenericValue loanApplication, Delegator delegator) {
		String memberId = loanApplication.getString("partyId");

		GenericValue accountProduct = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		Long accountProductId = accountProduct.getLong("accountProductId");
		List<GenericValue> memberAccountELI = null;

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
			Long accountProductId, Delegator delegator) {
		String memberId = loanApplication.getString("partyId");

		List<GenericValue> memberAccountELI = null;
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

}
