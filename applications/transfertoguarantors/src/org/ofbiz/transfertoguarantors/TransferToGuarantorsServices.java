package org.ofbiz.transfertoguarantors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/*****
 * 
 *  org.ofbiz.transfertoguarantors.TransferToGuarantorsServices.transferToGuarantors
 * */
public class TransferToGuarantorsServices {
	
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
		
		Long accountProductId = loanProduct.getLong("accountProductId");
		Long partyId = loanApplication.getLong("partyId");
		//Get the multiplier account product
		if ((accountProductId != null) && (loanProduct.getString("multipleOfSavings").equals("Yes")) && (loanProduct.getBigDecimal("multipleOfSavingsAmt") != null) && (loanProduct.getBigDecimal("multipleOfSavingsAmt").compareTo(BigDecimal.ZERO) > 0))
		{
			bdDepositsBalance = AccHolderTransactionServices.getAccountTotalBalance(accountProductId, partyId);
			memberDepositsAtAttachment = bdDepositsBalance;
			//Get the Proportion of savings for this loan
			BigDecimal bdProportionForThisLoan = bdDepositsBalance.divide(loanProduct.getBigDecimal("multipleOfSavingsAmt"), 4, RoundingMode.FLOOR);
			
			bdLoanTransferBalance = bdLoanTransferBalance.subtract(bdProportionForThisLoan);
			
			GenericValue loanRepayment = null;
			String acctgTransType = "LOAN_RECEIVABLE";
			
			//BigDecimal totalLoanDue 
			BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
			BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
			BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
			
			insuranceDueAtAttachment = totalInsuranceDue;
			interestDueAtAttachment = totalInterestDue;
			
			BigDecimal loanInsurance = totalInterestDue;
			BigDecimal loanInterest = totalInsuranceDue;
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
					bdProportionForThisLoan, "repaymentMode", "FROMDEPOSITS"));
			String acctgTransId = LoanRepayments.createAccountingTransaction(loanRepayment,
					acctgTransType, userLogin, delegator);
			//Repay Loan with the proportion Balance
			
			if (bdDepositsBalance.compareTo(BigDecimal.ZERO) > 0){
				
				//Only do this if the member has deposits
				
				String accountToDebit = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getString("glAccountId");
				sequence = Long.valueOf(LoanRepayments.repayLoanOnLoanAttachment(loanRepayment, userLogin, acctgTransId, acctgTransType, accountToDebit, sequence));
			
			Long memberAccountId = LoanUtilities.getMemberAccountIdFromMemberAccount(partyId, accountProductId);
			
			String accountTransactionParentId = AccHolderTransactionServices
					.getcreateAccountTransactionParentId(memberAccountId,
							userLogin);
			//Transfer proportion balance from Member Deposits Account
			//Debit Member Deposits MPA
			AccHolderTransactionServices.memberTransactionDeposit(
					bdProportionForThisLoan, memberAccountId, userLogin,
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
		loanInsurance = totalInterestDue;
		loanInterest = totalInsuranceDue;
		loanPrincipal = totalAmount.subtract(totalInterestDue);
		loanPrincipal = loanPrincipal.subtract(totalInsuranceDue);
		
		BigDecimal totalLoanDue = totalPrincipalDue.add(totalInterestDue).add(totalInsuranceDue) ;
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		
		GenericValue loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
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
				bdLoanTransferBalance, "repaymentMode", "ATTACHED"));
		
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Create New Loans and Attach them to the Guarantors
		//Distribute the new Transfer balance to Guarantors

		BigDecimal bdGuarantorLoanAmount = bdLoanTransferBalance.divide(new BigDecimal(
				noOfGuarantors), 6, RoundingMode.HALF_UP);

		for (GenericValue loanGuarantor : loanGuarantorELI) {
			createGuarantorLoans(bdGuarantorLoanAmount, loanGuarantor,
					loanApplication, userLoginId);
		}

		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "success";
	}

	private static void createGuarantorLoans(BigDecimal bdGuarantorLoanAmount,
			GenericValue loanGuarantor, GenericValue loanApplication,
			String userLoginId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		Long loanApplicationId = delegator.getNextSeqIdLong("LoanApplication",
				1);
		GenericValue newLoanApplication;

		//Long defaulterLoanProductId = LoanUtilities.getLoanProductGivenCode(
		//		LoansProcessingServices.DEFAULTER_LOAN_CODE).getLong("loanProductId");

		newLoanApplication = delegator.makeValue("LoanApplication", UtilMisc
				.toMap("loanApplicationId", loanApplicationId,
						"parentLoanApplicationId",
						loanApplication.getLong("loanApplicationId"), "loanNo",
						String.valueOf(loanApplicationId), "createdBy",
						userLoginId, "isActive", "Y", "partyId",
						loanGuarantor.getLong("guarantorId"),

						"loanProductId", loanApplication.getLong("loanProductId"),
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

						"accountProductId",
						loanApplication.getLong("accountProductId")

				));
		try {
			delegator.createOrStore(newLoanApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		//Create log - attached from defaulter
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		GenericValue loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", loanStatusId,
				"createdBy", userLoginId, "comment", "Loan attached from defaulter"
				
						
				));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

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

}
