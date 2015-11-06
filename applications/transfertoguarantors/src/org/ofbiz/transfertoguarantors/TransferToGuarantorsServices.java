package org.ofbiz.transfertoguarantors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/*****
 * 
 *  org.ofbiz.transfertoguarantors.TransferToGuarantorsServices.transferToGuarantors
 * */
public class TransferToGuarantorsServices {
	
	private static Logger log = Logger
			.getLogger(TransferToGuarantorsServices.class);
	
	public static String DEFAULTERLOANCODE = "D330";
	
	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:51:03 PM
	 * */
	public static String getMemberNumber(Long partyId) {
		String memberNumber = "";

		memberNumber = LoanUtilities
				.getMemberNumberGivenPartyId(partyId);

		return memberNumber;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:45 PM
	 * **/
	public static String getPayrollNumber(Long partyId) {
		String payrollNumber = "";

		payrollNumber = LoanUtilities
				.getPayrollNumberGivenPartyId(partyId);

		return payrollNumber;
	}

	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:27 PM
	 * */
	public static String getMobileNumber(Long partyId) {
		String mobileNumber = "";

		mobileNumber = LoanUtilities
				.getMobileNumberGivenPartyId(partyId);

		return mobileNumber;
	}

	// getMemberStationName
	public static String getMemberStationName(Long partyId) {
		String stationName = "";

		stationName = LoanUtilities.getMemberStationNameGivenPartyId(partyId);

		return stationName;
	}
	
	
	/*****
	 * Transfer Loan to Guarantors
	 * */
	public static String transferToGuarantors(Long loanApplicationId,
			Map<String, String> userLogin) {
		
		String postingType;
		String entrySequenceId;
		GenericValue accountHolderTransactionSetup;
		BigDecimal bdOffsetAmount = BigDecimal.ZERO;
		
		String userLoginId = userLogin.get("userLoginId");
		Long sequence = 0L;
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		//Check if the loan is already defaulted (transferred)
		if (loanApplication == null)
			return "Could not find the loan application, please try again .... ";
		
		if (loanApplication.getLong("loanStatusId") == null)
			return "The loan application does not have status , please contact ICT to fix that ";
		
		if (loanApplication.getLong("loanStatusId").equals(LoanUtilities.getLoanStatusId("DEFAULTED")))
			return "Cannot transfer a defaulted Loan, it must have already transferred";
		
		//Check if a loan is not disbursed
		if (!loanApplication.getLong("loanStatusId").equals(LoanUtilities.getLoanStatusId("DISBURSED")))
			return "We can only transfer running loans (in the DISBURSED status), please verify that this is a running loan";
		
		
		//The loan must not have negative Interest, negative insurance or negative principal balance
		BigDecimal bdInsuranceBalance = LoanRepayments.getTotalInsurancByLoanDue(loanApplication.getLong("loanApplicationId").toString());
		bdInsuranceBalance = bdInsuranceBalance.setScale(2, RoundingMode.FLOOR);
		if (bdInsuranceBalance.compareTo(BigDecimal.ZERO) == -1)
			return " The loan has -VE insurance balance which will mean the insurance was over charged or loan was over paid - please resolve that , that is part of data cleanup";
		
		BigDecimal bdInterestBalance = LoanRepayments.getTotalInterestByLoanDue(loanApplication.getLong("loanApplicationId").toString());
		bdInterestBalance = bdInterestBalance.setScale(2, RoundingMode.FLOOR);
		if (bdInterestBalance.compareTo(BigDecimal.ZERO) == -1)
			return " The loan has -VE interest balance which will mean the interest was over charged or loan was over paid - please resolve that , that is part of data cleanup";

		
		BigDecimal bdPrincipalBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		bdPrincipalBalance = bdPrincipalBalance.setScale(2, RoundingMode.FLOOR);
		if (bdPrincipalBalance.compareTo(BigDecimal.ZERO) == -1)
			return " The loan has -VE loan balance which will mean the loan was over paid - please resolve that , that is part of data cleanup";

		String acctgTransType = "LOAN_RECEIVABLE";
		
		GenericValue loanRepayment = delegator.makeValidValue("LoanRepayment", UtilMisc.toMap(
				
				"partyId", loanApplication.getLong("partyId")));

		String acctgTransId = LoanRepayments.createAccountingTransaction(loanRepayment,
				acctgTransType, userLogin, delegator);
		Long disbursedLoanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		
		if (loanApplication.getLong("loanStatusId") != disbursedLoanStatusId)
			return "Can only transfer to guarantors a running/disbursed loan ! This loan may have been disbursed already !";
		
		String statusName = "DEFAULTED";
		Long loanStatusId = LoanServices.getLoanStatusId(statusName);

		loanApplication.set("loanStatusId", loanStatusId);
		
		List<GenericValue> loanGuarantorELI = getNumberOfGuarantors(Long
				.valueOf(loanApplicationId));

		int noOfGuarantors = loanGuarantorELI.size();

		if (noOfGuarantors <= 0){
			return "The loan has no guarantors to attach to !";
		}
		
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(Long
				.valueOf(loanApplicationId));
		BigDecimal bdLoanTransferBalance = bdLoanBalance;
		BigDecimal balanceAtAttachment = bdLoanBalance;
		BigDecimal interestDueAtAttachment = BigDecimal.ZERO;
		BigDecimal insuranceDueAtAttachment = BigDecimal.ZERO;
		
		Long loanProductId = loanApplication.getLong("loanProductId");
		
		GenericValue loanProduct = LoanUtilities.getEntityValue("LoanProduct", "loanProductId", loanProductId);
		BigDecimal bdDepositsBalance = BigDecimal.ZERO;
		BigDecimal memberDepositsAtAttachment = BigDecimal.ZERO;
		
		Long accountProductId = null; // = loanProduct.getLong("accountProductId");
		
		if (loanProduct.getString("fosaOrBosa").trim().equals("FOSA")){
			accountProductId = LoanUtilities.getAccountProductIdGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		} else {
			accountProductId = LoanUtilities.getAccountProductIdGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE);
		}
		
		String loanClass = loanProduct.getString("fosaOrBosa").trim();
		
		Long partyId = loanApplication.getLong("partyId");
		//Get the multiplier account product
		if ((accountProductId != null) && (loanProduct.getString("multipleOfSavings").equals("Yes")) && (loanProduct.getBigDecimal("multipleOfSavingsAmt") != null) && (loanProduct.getBigDecimal("multipleOfSavingsAmt").compareTo(BigDecimal.ZERO) > 0))
		{
			
			bdDepositsBalance = AccHolderTransactionServices.getAccountTotalBalance(accountProductId, partyId);
			memberDepositsAtAttachment = bdDepositsBalance;
			//Get the Proportion of savings for this loan
			BigDecimal bdProportionForThisLoan = getDepositProportion(loanClass, bdDepositsBalance, loanApplicationId, partyId);
			
			log.info(" The loan proportion is "+bdProportionForThisLoan);
					
					//bdDepositsBalance.divide(loanProduct.getBigDecimal("multipleOfSavingsAmt"), 4, RoundingMode.FLOOR);
			
			//Offset member share deposits get minimum shares minus current shares (offset value and add to the member share capital)
			BigDecimal bdMinimumShareCapital = LoanUtilities.getShareCapitalLimitToAttacheLoan(partyId);
			
			BigDecimal bdShareCapitalBalance = LoanUtilities
					.getShareCapitalAccountBalance(partyId);
			bdShareCapitalBalance = bdShareCapitalBalance.setScale(2,
					RoundingMode.HALF_UP);
			
			if (bdMinimumShareCapital.compareTo(bdShareCapitalBalance) == 1){
				//Get the difference and add it to member share capital
				bdOffsetAmount = bdMinimumShareCapital.subtract(bdShareCapitalBalance);
				bdProportionForThisLoan = bdProportionForThisLoan.subtract(bdOffsetAmount);
				
				Long shareCapitalAccountProductId = LoanUtilities.getAccountProductIdGivenCodeId(AccHolderTransactionServices.SHARE_CAPITAL_CODE);
				Long memberAccountShareCapitalId = LoanUtilities.getMemberAccountIdFromMemberAccount(partyId, shareCapitalAccountProductId);
				
				String accountTransactionParentId = AccHolderTransactionServices
						.getcreateAccountTransactionParentId(memberAccountShareCapitalId,
								userLogin);
				
				//FROMLOANATTACHMENT
				AccHolderTransactionServices.memberTransactionDeposit(
						bdOffsetAmount, memberAccountShareCapitalId, userLogin,
						"FROMLOANATTACHMENT", accountTransactionParentId, null,
						acctgTransId, null, loanApplicationId);
				
				//Debit share capital account with the bdOffsetAmount
				String accountToCredit = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SHARE_CAPITAL_CODE).getString("glAccountId");
				sequence = sequence + 1;
				
				//post share capital to gl
				String posting = "C";
				LoanRepayments.postTransactionEntry(delegator, bdOffsetAmount, partyId.toString(), accountToCredit, posting, acctgTransId, acctgTransType, sequence.toString(), userLogin);
				
			}
			
			
			
			bdLoanTransferBalance = bdLoanTransferBalance.subtract(bdProportionForThisLoan);
			
			//BigDecimal totalLoanDue 
			BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
			BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
			BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
			
			insuranceDueAtAttachment = totalInsuranceDue;
			interestDueAtAttachment = totalInterestDue;
			
			BigDecimal loanInsurance = totalInsuranceDue;
			BigDecimal loanInterest = totalInterestDue;
			BigDecimal loanPrincipal = totalPrincipalDue;
			
			BigDecimal totalLoanDue = totalPrincipalDue.add(totalInterestDue).add(totalInsuranceDue) ;
			Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
			loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
					"loanRepaymentId", loanRepaymentId, "isActive", "Y",
					"createdBy", userLogin.get("userLoginId"), "partyId", partyId, "loanApplicationId",
					loanApplicationId,

					"loanNo", loanApplication.getString("loanNo"),
					"loanAmt", loanApplication.getBigDecimal("loanAmt"),

					"totalLoanDue", totalLoanDue, "totalInterestDue",
					totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
					"totalPrincipalDue", totalPrincipalDue, "interestAmount",
					loanInterest, "insuranceAmount", loanInsurance,
					"principalAmount", loanPrincipal, "transactionAmount",
					bdProportionForThisLoan, "repaymentMode", "FROMDEPOSITS", "acctgTransId", acctgTransId));
			
			//Repay Loan with the proportion Balance
			
			if (bdDepositsBalance.compareTo(BigDecimal.ZERO) > 0){
				
				//Only do this if the member has deposits
				
				String accountToDebit = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getString("glAccountId");
				sequence = Long.valueOf(LoanRepayments.repayLoanOnLoanAttachment(loanRepayment, userLogin, acctgTransId, acctgTransType, accountToDebit, sequence, bdOffsetAmount));
			
			Long memberAccountId = LoanUtilities.getMemberAccountIdFromMemberAccount(partyId, accountProductId);
			
			String accountTransactionParentId = AccHolderTransactionServices
					.getcreateAccountTransactionParentId(memberAccountId,
							userLogin);
			//Transfer proportion balance from Member Deposits Account
			//Debit Member Deposits MPA
			//Proportion + Offset Amount
			BigDecimal amountFromDeposit = bdProportionForThisLoan.add(bdOffsetAmount);
			AccHolderTransactionServices.memberTransactionDeposit(
					amountFromDeposit, memberAccountId, userLogin,
					"LOANREPAYMENT", accountTransactionParentId, null,
					acctgTransId, null, loanApplicationId);
			}
			
			
		}

		// Updates the loan to defaulted
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// Create a Log
		GenericValue loanStatusLog;
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "Loan attached to guarantors",
				
				"dateAttached", new Timestamp(Calendar.getInstance().getTimeInMillis()),
				"memberDepositsAtAttachment", memberDepositsAtAttachment,
				"balanceAtAttachment", balanceAtAttachment,
				
				"interestDueAtAttachment", interestDueAtAttachment,
				
				"insuranceDueAtAttachment", insuranceDueAtAttachment
				
						
				));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		//Clear loan by transferring to Guarantors
		
		//ATTACHED  -- clear by attaching
		//bdLoanTransferBalance
		loanStatusId = LoanServices.getLoanStatusId("ATTACHED");
		loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "Loan attached to guarantors",
				
				"dateAttached", new Timestamp(Calendar.getInstance().getTimeInMillis()),
				"memberDepositsAtAttachment", memberDepositsAtAttachment,
				"balanceAtAttachment", balanceAtAttachment,
				
				"interestDueAtAttachment", interestDueAtAttachment,
				
				"insuranceDueAtAttachment", insuranceDueAtAttachment
				
						
				));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		
		//Attached to Guarantors
		BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
		BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
		BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
		
		//bdLoanTransferBalance
		BigDecimal totalAmount = bdLoanTransferBalance;
		BigDecimal loanInsurance = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		loanInsurance = totalInsuranceDue;
		loanInterest = totalInterestDue;
		loanPrincipal = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		bdLoanTransferBalance = loanPrincipal.add(loanInsurance).add(loanInterest);		
				//totalAmount;
				
				//.subtract(totalInterestDue);
		//loanPrincipal = loanPrincipal.subtract(totalInsuranceDue);
		
		BigDecimal totalLoanDue = totalPrincipalDue.add(totalInterestDue).add(totalInsuranceDue) ;
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLogin.get("userLoginId"), "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", loanApplication.getBigDecimal("loanAmt"),

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				bdLoanTransferBalance, "repaymentMode", "ATTACHED", "acctgTransId", acctgTransId));
		
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Credit Loan to Members with total principal
		postingType = "C";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		

		accountHolderTransactionSetup = LoanRepayments.getAccountHolderTransactionSetupRecord(
				"PRINCIPALPAYMENT", delegator);
		
		String accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);

		if (loanPrincipal.compareTo(BigDecimal.ZERO) == 1) {
			LoanRepayments.postTransactionEntry(delegator, loanPrincipal, partyId.toString(), accountId, postingType, acctgTransId, acctgTransType, entrySequenceId, userLogin);
			//postTransactionEntry(delegator, loanPrincipal, partyId,
			//		accountId, postingType, acctgTransId, acctgTransType,
			//		entrySequenceId, userLogin);
		}
		
		//Debit Loan to Members for each disbursement
		

		// Create New Loans and Attach them to the Guarantors
		//Distribute the new Transfer balance to Guarantors

//		BigDecimal bdGuarantorLoanAmount = bdLoanTransferBalance.divide(new BigDecimal(
//				noOfGuarantors), 6, RoundingMode.HALF_UP);
		
		
		BigDecimal bdGuarantorLoanAmount = bdLoanTransferBalance.divide(new BigDecimal(
				noOfGuarantors), 6, RoundingMode.HALF_UP);

		for (GenericValue loanGuarantor : loanGuarantorELI) {
			sequence = createGuarantorLoans(bdGuarantorLoanAmount, loanGuarantor,
					loanApplication, userLogin, acctgTransId, sequence, acctgTransType);
		}

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static BigDecimal getDepositProportion(String loanClass,
			BigDecimal bdDepositsBalance, Long loanApplicationId, Long partyId) {
		
		//Total Balances for member running loans given class
		BigDecimal bdTotalLoanBalances = LoansProcessingServices.getTotalDisbursedLoanBalancesGivenClass(partyId, loanClass);
		
		//Total Balance for this loan
		BigDecimal bdCurrentLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		//Add Interest
		bdCurrentLoanBalance = bdCurrentLoanBalance.add(LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString()));
		
		//Add Insurance
		bdCurrentLoanBalance = bdCurrentLoanBalance.add(LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString()));
		return bdCurrentLoanBalance.divide(bdTotalLoanBalances, 4, RoundingMode.HALF_UP).multiply(bdDepositsBalance);
	}

	private static Long createGuarantorLoans(BigDecimal bdGuarantorLoanAmount,
			GenericValue loanGuarantor, GenericValue loanApplication,
			Map<String, String> userLogin, String acctgTransId, Long sequence, String acctgTransType) {
		
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		Long loanApplicationId = delegator.getNextSeqIdLong("LoanApplication",
				1);
		GenericValue newLoanApplication;
		
		String postingType;
		String entrySequenceId;
		

		GenericValue accountHolderTransactionSetup;
		
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Long defaulterLoanProductId = LoanUtilities.getLoanProductGivenCode(
		//		LoansProcessingServices.DEFAULTER_LOAN_CODE).getLong("loanProductId");
		
		Long defaulterLoanProductId = LoanUtilities.getLoanProductGivenCode(DEFAULTERLOANCODE).getLong("loanProductId");

		log.info(" $$$$$$ Loan Product "+defaulterLoanProductId);
		newLoanApplication = delegator.makeValue("LoanApplication", UtilMisc
				.toMap("loanApplicationId", loanApplicationId,
						"parentLoanApplicationId",
						loanApplication.getLong("loanApplicationId"), "loanNo",
						String.valueOf(loanApplicationId), "createdBy",
						userLogin.get("userLoginId"), "isActive", "Y",
						"isDisbursed", "Y",
						"partyId",
						loanGuarantor.getLong("guarantorId"),

						"loanProductId", defaulterLoanProductId,
						"interestRatePM",
						loanApplication.getBigDecimal("interestRatePM")

						, "repaymentPeriod",
						loanApplication.getLong("repaymentPeriod")

						, "loanAmt", bdGuarantorLoanAmount, "appliedAmt",
						bdGuarantorLoanAmount, "appraisedAmt",
						bdGuarantorLoanAmount, "approvedAmt",
						bdGuarantorLoanAmount, "loanStatusId", loanStatusId,
						"deductionType",
						loanApplication.getString("deductionType"),

						"originalLoanProductId",
						
						loanApplication.getLong("loanProductId"),
						
						"disbursementDate", new Timestamp(Calendar.getInstance().getTimeInMillis()),

						"accountProductId",
						loanApplication.getLong("accountProductId"),
						
						"acctgTransId", acctgTransId

				));
		try {
			delegator.createOrStore(newLoanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LoanServices.calculateLoanRepaymentStartDate(newLoanApplication);
		postingType = "D";
		sequence = sequence + 1;
		entrySequenceId = sequence.toString();
		

		accountHolderTransactionSetup = LoanRepayments.getAccountHolderTransactionSetupRecord(
				"PRINCIPALPAYMENT", delegator);
		
		String accountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		// createAccountingEntry(loanExpectation, acctgTransId, accountId,
		// postingType, delegator);

		if (bdGuarantorLoanAmount.compareTo(BigDecimal.ZERO) == 1) {
			LoanRepayments.postTransactionEntry(delegator, bdGuarantorLoanAmount, loanGuarantor.getLong("guarantorId").toString(), accountId, postingType, acctgTransId, acctgTransType, entrySequenceId, userLogin);
			//postTransactionEntry(delegator, loanPrincipal, partyId,
			//		accountId, postingType, acctgTransId, acctgTransType,
			//		entrySequenceId, userLogin);
		}
		//Create log - attached from defaulter
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		GenericValue loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLogin.get("userLoginId"), "comment", "Loan attached from defaulter"
				
						
				));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		return sequence;

	}

	public static List<GenericValue> getNumberOfGuarantors(
			Long loanApplicationId) {
		List<GenericValue> loanGuarantorELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanGuarantorELI = delegator.findList("LoanGuarantor",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return loanGuarantorELI;
	}

	private static BigDecimal getLoanAmount(Delegator delegator,
			Long loanApplicationId) {
		GenericValue loanApplication = null;

		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (loanApplication == null)
			return BigDecimal.ZERO;
		return loanApplication.getBigDecimal("loanAmt");
	}
	
	
	//reverseLoanAttachmentToDefaulterProcessing
	public static String reverseLoanAttachmentToDefaulterProcessing(Long loanApplicationId,
			Map<String, String> userLogin) {
		
		//The loan must be defaulted first
		String postingType;
		String entrySequenceId;
		GenericValue accountHolderTransactionSetup;
		
		String userLoginId = userLogin.get("userLoginId");
		Long sequence = 0L;
		GenericValue loanApplication = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			loanApplication = delegator.findOne(
					"LoanApplication",
					UtilMisc.toMap("loanApplicationId",
							Long.valueOf(loanApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		//Check if the loan is already defaulted (transferred)
		if (loanApplication == null)
			return "Could not find the loan application, please try again .... ";
		
		if (loanApplication.getLong("loanStatusId") == null)
			return "The loan application does not have status , please contact ICT to fix that ";
		
		//if (loanApplication.getLong("loanStatusId").equals(LoanUtilities.getMemberStatusId("DEFAULTED")))
		//	return "Cannot transfer a defaulted Loan, it must have already transferred";
		//DEFAULTED
		//DEFAULTED
		log.info(" Loan Status --- "+loanApplication.getLong("loanStatusId"));
		log.info(" Defaulted status ID --- "+LoanUtilities.getLoanStatusId("DEFAULTED"));
		//Check if a loan is not disbursed
		if (!loanApplication.getLong("loanStatusId").equals(LoanUtilities.getLoanStatusId("DEFAULTED")))
			return "We can only reverse DEFAULTED and attached loans, please verify that this is a defaulted loan";

		Long parentLoanApplicationId = loanApplication.getLong("loanApplicationId");
		//Get all disbursed loans whose parent is this loan
		List<GenericValue> attacheLoansList = getAttachedLoansList(parentLoanApplicationId);
		
		if ((attacheLoansList == null) || (attacheLoansList.size() < 1))
			return " The system cannot find loans associated with this Defaulted loan, you need to have them mapped to the parent loan - please contact ICT";
			
		
		BigDecimal bdTotalAmountAttached = getTotalAttachedAmount(parentLoanApplicationId);
		
		BigDecimal bdTotalInterestChargedOnGuarantors = getTotalTotalInterestChargesOnGuarantors(parentLoanApplicationId);
		BigDecimal bdTotalInsuranceChargedOnGuarantors = getTotalInsuranceChargedOnGuarantors(parentLoanApplicationId);
		
		
		//Reset the loan Balance for the defaulted Loan
		//Set the Loan Balance to the total loan attached
		String acctgTransId = "";
		
		setNewLoanBalance(bdTotalAmountAttached, parentLoanApplicationId, userLogin, acctgTransId);
		
		//Set the Interest due to the total interest charged
		setNewLoanInterest(bdTotalInterestChargedOnGuarantors, parentLoanApplicationId, userLogin, acctgTransId);
		//set the Insurance due to the total insurance charged
		setNewLoanInsurance(bdTotalInsuranceChargedOnGuarantors, parentLoanApplicationId, userLogin, acctgTransId);
		
		//Set loan status to Disbursed 
		loanApplication.set("loanStatusId", LoanUtilities.getLoanStatusId("DISBURSED"));
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Add Log
		addLoanReattachmentLog(LoanUtilities.getLoanStatusId("DISBURSED"), parentLoanApplicationId, userLogin, "Loan Reattached");
		
		

		
		//Return amount paid to member savings account
		for (GenericValue genericValue : attacheLoansList) {
			returnAmountPaidByGuarantor(genericValue.getLong("loanApplicationId"), acctgTransId, userLogin);
		}
		
		//Repay each guarantor loan with the the total amount 
		for (GenericValue genericValue : attacheLoansList) {
			reverseIndividualGuarantorLoan(genericValue.getLong("loanApplicationId"), acctgTransId, userLogin);
		}

		
		
		return "success";
	}

	private static void returnAmountPaidByGuarantor(Long loanApplicationId,
			String acctgTransId, Map<String, String> userLogin) {
		//Repay each interest amount  repaid with and credit savings account with the amount
		BigDecimal bdInterestPaid = LoanRepayments.getTotalInterestPaid(loanApplicationId.toString());
		//Repay each insurance amount  repaid with and credit savings account with the amount
		BigDecimal bdInsurancePaid = LoanRepayments.getTotalInsurancePaid(loanApplicationId.toString());
		//Repay each principal amount repaid with and credit savings account with the amount
		BigDecimal bdPrincipalPaid = LoanRepayments.getTotalPrincipalPaid(loanApplicationId);
		
		BigDecimal bdTotalPaid = bdInterestPaid.add(bdInsurancePaid).add(bdPrincipalPaid);
				//LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		//Add the toal to member savings account
		// Save Parent
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		Long memberAccountId  = Long.valueOf(LoanUtilities.getMemberAccountIdGivenMemberAndAccountCode(loanApplication.getLong("partyId"), AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE));
		GenericValue accountTransactionParent = AccHolderTransactionServices.createAccountTransactionParent(memberAccountId, userLogin);
				
				//createAccountTransactionParent(
				//memberAccountId.toString(), userLogin);

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
		String transactionType = "ATTACHMENTREVERSAL";
		AccHolderTransactionServices.createTransaction(null, transactionType, userLogin,
				memberAccountId.toString(), bdTotalPaid, null,
				accountTransactionParent
						.getString("accountTransactionParentId"), acctgTransId,
				null, null);
		

		
	}

	private static void reverseIndividualGuarantorLoan(Long loanApplicationId,
			String acctgTransId, Map<String, String> userLogin) {
		//Repay each interest amount  repaid with and credit savings account with the amount
		BigDecimal bdInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
		//Repay each insurance amount  repaid with and credit savings account with the amount
		BigDecimal bdInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
		//Repay each principal amount repaid with and credit savings account with the amount
		BigDecimal bdPrincipalBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		BigDecimal loanPrincipal = BigDecimal.ZERO;
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;
		
		GenericValue loanApplication = LoanUtilities.getLoanApplicationEntity(loanApplicationId);
		
		// Loan Principal
		loanPrincipal = bdPrincipalBalance;
				
				//LoanRepayments.getTotalPrincipaByLoanDue(String.valueOf(loanApplicationId));
		// Get This Loan's Interest
				
		loanInterest = bdInterestDue;
		// Get This Loan's Insurance
		loanInsurance = bdInsuranceDue;
		// Sum Principal, Interest and Insurance

		BigDecimal transactionAmount = loanPrincipal.add(loanInterest).add(
				loanInsurance);

		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");

		BigDecimal totalInterestDue = bdInterestDue;
		
		BigDecimal totalInsuranceDue = bdInsuranceDue;
		BigDecimal totalPrincipalDue = bdPrincipalBalance;
		
		BigDecimal totalLoanDue = totalInterestDue.add(totalInsuranceDue).add(
				totalPrincipalDue);

		
		Long partyId = loanApplication.getLong("partyId");
		GenericValue loanRepayment = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLogin.get("userLoginId"), "partyId", partyId, "loanApplicationId",
				loanApplicationId,

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount, "acctgTransId", acctgTransId, "repaymentType", "REATTACHMENT", "repaymentMode", "REATTACHMENT"));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}		
		//set loan to attachment reversal (ATTACHMENTREVERSAL)
		loanApplication.set("loanStatusId", LoanUtilities.getLoanStatusId("ATTACHMENTREVERSAL"));
		
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addLoanReattachmentLog(LoanUtilities.getLoanStatusId("ATTACHMENTREVERSAL"), loanApplicationId, userLogin, "Attachment reversal ");
		
	}

	private static void addLoanReattachmentLog(Long loanStatusId,
			Long parentLoanApplicationId, Map<String, String> userLogin, String comment) {
		GenericValue loanStatusLog;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog");
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc
				.toMap("loanStatusLogId", loanStatusLogId,
						
						"loanApplicationId",
						parentLoanApplicationId,
						
						"isActive", "Y",
						"createdBy", userLogin.get("userLoginId"), 
						"updatedBy", null,
						"loanStatusId", loanStatusId,
						
						"comment", comment));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void setNewLoanInsurance(
			BigDecimal bdTotalInsuranceChargedOnGuarantors,
			Long parentLoanApplicationId, Map<String, String> userLogin,
			String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		BigDecimal bdInsuranceAccrued = bdTotalInsuranceChargedOnGuarantors.setScale(4, RoundingMode.HALF_UP);
		
		
		GenericValue loanExpectation = null;
		
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", parentLoanApplicationId);
		Long partyId = loanApplication.getLong("partyId");
		GenericValue member = LoanUtilities.getEntityValue("Member", "partyId", partyId);
		String employeeNo = member.getString("memberNumber");
				
		String employeeNames = member.getString("firstName") + " "
						+ member.getString("middleName") + " "
						+ member.getString("lastName");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();
		
		String monthPadded = String.valueOf(month);//paddString(2, String.valueOf(month));
		String monthYear = monthPadded+String.valueOf(year);
		// TODO Auto-generated method stub
		loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		bdInsuranceAccrued = bdInsuranceAccrued.setScale(4, RoundingMode.HALF_UP);
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId, "loanNo",
						loanApplication.getString("loanNo"), "loanApplicationId", parentLoanApplicationId,
						"employeeNo", employeeNo, "repaymentName",
						"INSURANCE", "employeeNames", employeeNames,
						"dateAccrued", new Timestamp(Calendar.getInstance()
								.getTimeInMillis()), "isPaid", "N",
						"isPosted", "N", "amountDue", bdInsuranceAccrued,
						"amountAccrued", bdInsuranceAccrued,
						
						"month", monthYear,
						"acctgTransId", acctgTransId,
						"partyId",
						partyId, "loanAmt", bdLoanAmt, "expectationClassType", "REATTACHMENT"));
		
		try {
			delegator.createOrStore(loanExpectation);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void setNewLoanInterest(
			BigDecimal bdTotalInterestChargedOnGuarantors,
			Long parentLoanApplicationId, Map<String, String> userLogin,
			String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		BigDecimal bdInterestAccrued = bdTotalInterestChargedOnGuarantors.setScale(4, RoundingMode.HALF_UP);
		//GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", parentLoanApplicationId);
		
		GenericValue loanExpectation = null;
		
		
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", parentLoanApplicationId);
		
		Long partyId = loanApplication.getLong("partyId");
		GenericValue member = LoanUtilities.getEntityValue("Member", "partyId", partyId);
		String employeeNo = member.getString("memberNumber");
				
		String employeeNames = member.getString("firstName") + " "
						+ member.getString("middleName") + " "
						+ member.getString("lastName");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();
		
		String monthPadded = String.valueOf(month);//paddString(2, String.valueOf(month));
		String monthYear = monthPadded+String.valueOf(year);
		//acctgTransId
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId, "loanNo",
						loanApplication.getString("loanNo"), "loanApplicationId", parentLoanApplicationId,
						"employeeNo", employeeNo, "repaymentName",
						"INTEREST", "employeeNames", employeeNames,
						"dateAccrued", new Timestamp(Calendar.getInstance()
								.getTimeInMillis()), "isPaid", "N",
						"isPosted", "N", "amountDue", bdInterestAccrued,
						"amountAccrued", bdInterestAccrued,
						"month", monthYear,
						"acctgTransId", acctgTransId,
						"partyId",
						partyId, "loanAmt", bdLoanAmt, "expectationClassType", "REATTACHMENT"));
		
		try {
			delegator.createOrStore(loanExpectation);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void setNewLoanBalance(BigDecimal bdTotalAmountAttached,
			Long parentLoanApplicationId, Map<String, String> userLogin,
			String acctgTransId) {
		String userLoginId = (String) userLogin.get("userLoginId");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		Long loanApplicationId = parentLoanApplicationId;
		
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", parentLoanApplicationId);
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		//LoanRepayments.getT
		BigDecimal totalLoanDue = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
		BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
		BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
		
	
		
		BigDecimal loanInterest = BigDecimal.ZERO;
		BigDecimal loanInsurance = BigDecimal.ZERO;
		BigDecimal loanPrincipal = bdTotalAmountAttached.multiply(new BigDecimal(-1));
		BigDecimal transactionAmount = bdTotalAmountAttached.multiply(new BigDecimal(-1));
		
		GenericValue loanRepayment;
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLoginId, "partyId", loanApplication.getLong("partyId"), "loanApplicationId",
				loanApplication.getLong("loanApplicationId"),

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount, "acctgTransId", acctgTransId, "repaymentType", "REATTACHMENT", "repaymentMode", "REATTACHMENT"));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		
	}

	private static BigDecimal getTotalInsuranceChargedOnGuarantors(
			Long parentLoanApplicationId) {

		List<GenericValue> listguarantorLoans = getAttachedLoansList(parentLoanApplicationId);
		BigDecimal bdTotalInsuranceCharged = BigDecimal.ZERO;
		
		for (GenericValue genericValue : listguarantorLoans) {
			bdTotalInsuranceCharged = bdTotalInsuranceCharged.add(LoanRepayments.getTotalInsuranceByLoanExpected(genericValue.getLong("loanApplicationId").toString()));
		}
		return bdTotalInsuranceCharged;
	}

	private static BigDecimal getTotalTotalInterestChargesOnGuarantors(
			Long parentLoanApplicationId) {
		
		List<GenericValue> listguarantorLoans = getAttachedLoansList(parentLoanApplicationId);
		BigDecimal bdTotalInterestCharged = BigDecimal.ZERO;
		
		for (GenericValue genericValue : listguarantorLoans) {
			bdTotalInterestCharged = bdTotalInterestCharged.add(LoanRepayments.getTotalExpectedInterestAmount(genericValue.getLong("loanApplicationId").toString()));
		}
		
		// TODO Auto-generated method stub
		return bdTotalInterestCharged;
	}

	private static BigDecimal getTotalAttachedAmount(
			Long parentLoanApplicationId) {
		List<GenericValue> loanApplicationELI = null; 
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("parentLoanApplicationId",
							parentLoanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		BigDecimal bdTotalAmount = BigDecimal.ZERO;
		
		for (GenericValue genericValue : loanApplicationELI) {
			//Compute the total amount attached
			if (genericValue.getBigDecimal("loanAmt") != null){
				bdTotalAmount = bdTotalAmount.add(genericValue.getBigDecimal("loanAmt"));
			}
		}

		return bdTotalAmount;
	}

	private static List<GenericValue> getAttachedLoansList(Long parentLoanApplicationId) {
		
		//Get all loans whose parent is parentLoanApplicationId
		//parentLoanApplicationId
		List<GenericValue> loanApplicationELI = null; 
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					EntityCondition.makeCondition("parentLoanApplicationId",
							parentLoanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return loanApplicationELI;
	}

}
