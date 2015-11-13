package org.ofbiz.withdrawalprocessing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/**
 * @author Japheth Odonya @when Jun 19, 2015 10:27:03 AM
 * 
 *         Member Withdrawal Processing
 * 
 *         org.ofbiz.withdrawalprocessing.WithdrawalProcessingServices.
 *         getLoanBalance
 * */
public class WithdrawalProcessingServices {

	private static Logger log = Logger
			.getLogger(WithdrawalProcessingServices.class);
	public static Long PERCENT = 100L;

	public static BigDecimal getLoanBalance(Long loanApplicationId) {

		log.info(" HHHHHHHHHHHHHHH " + loanApplicationId);
		BigDecimal bdBalance = BigDecimal.ZERO;
		bdBalance = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		return bdBalance;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 12:14:35 PM Get the loan member
	 *         name
	 * */
	public static String getMemberGuaranteed(Long loanApplicationId) {
		String names = "";

		names = LoanUtilities
				.getMemberNameGivenLoanApplicationId(loanApplicationId);

		return names;
	}

	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:51:03 PM
	 * */
	public static String getMemberNumber(Long loanApplicationId) {
		String memberNumber = "";

		memberNumber = LoanUtilities
				.getMemberNumberGivenLoanApplicationId(loanApplicationId);

		return memberNumber;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:45 PM
	 * **/
	public static String getPayrollNumber(Long loanApplicationId) {
		String payrollNumber = "";

		payrollNumber = LoanUtilities
				.getPayrollNumberGivenLoanApplicationId(loanApplicationId);

		return payrollNumber;
	}

	/****
	 * @author Japheth Odonya @when Jun 19, 2015 12:48:27 PM
	 * */
	public static String getMobileNumber(Long loanApplicationId) {
		String mobileNumber = "";

		mobileNumber = LoanUtilities
				.getMobileNumberGivenLoanApplicationId(loanApplicationId);

		return mobileNumber;
	}

	// getMemberStationName
	public static String getMemberStationName(Long loanApplicationId) {
		String stationName = "";

		stationName = LoanUtilities.getMemberStationName(loanApplicationId);

		return stationName;
	}

	// Get Guaranteed %
	public static BigDecimal getLoanGuarateedPercByGuarantor(
			Long loanApplicationId, Long guarantorId) {

		BigDecimal bdLoanBalance = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);

		Long noOfGuarators = new Long(LoansProcessingServices
				.getNumberOfGuarantors(loanApplicationId).size());

		BigDecimal perGuarantorTotal = bdLoanBalance.divide(new BigDecimal(
				noOfGuarators), 2, RoundingMode.HALF_EVEN);

		if (bdLoanBalance.compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		BigDecimal bdPercentage = perGuarantorTotal.divide(bdLoanBalance, 2,
				RoundingMode.HALF_EVEN).multiply(new BigDecimal(PERCENT));

		return bdPercentage;
	}

	// Get Guaranteed Amount
	public static BigDecimal getLoanGuarateedAmountByGuarantor(
			Long loanApplicationId, Long guarantorId) {
		
		
		//getTotalLoanBalancesByLoanApplicationId
		BigDecimal bdLoanBalance =WithdrawalProcessingServices.getLoanBalance(loanApplicationId);
				
				//LoansProcessingServices.getLoanBalance.(loanApplicationId);

		Long noOfGuarators = new Long(LoansProcessingServices
				.getNumberOfGuarantors(loanApplicationId).size());

		BigDecimal perGuarantorTotal = bdLoanBalance.divide(new BigDecimal(
				noOfGuarators), 2, RoundingMode.HALF_EVEN);

		return perGuarantorTotal;
	}

	/***
	 * @author Japheth Odonya @when Jun 19, 2015 1:47:21 PM
	 * 
	 *         Get Loan Amount
	 * */
	public static BigDecimal getLoanAmount(Long loanApplicationId) {
		BigDecimal loanAmt = BigDecimal.ZERO;

		loanAmt = LoanUtilities
				.getLoanAmountGivenLoanApplicationId(loanApplicationId);

		return loanAmt;
	}

	/****
	 * @author Japheth Odonya @when Jun 19, 2015 2:38:24 PM
	 * 
	 *         Get Share Capital Amount
	 * */
	public static BigDecimal getShareCapitalBalance(String partyId) {

		Long partyIdLong = Long.valueOf(partyId);
		BigDecimal bdShareCapital = BigDecimal.ZERO;

		bdShareCapital = LoanUtilities.getShareCapitalAmount(partyIdLong);

		return bdShareCapital;
	}

	public static BigDecimal getMemberDepositBalance(String partyId) {
		BigDecimal bdMemberDeposit = BigDecimal.ZERO;
		Long partyIdLong = Long.valueOf(partyId);
		bdMemberDeposit = LoanUtilities.getMemberDepositAmount(partyIdLong);
		return bdMemberDeposit;
	}

	public static String getPartIdGivenMemberWithdrawalId(
			Long memberWithdrawalId) {
		String partyId = null;

		GenericValue memberWithdrawal = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberWithdrawal = delegator.findOne("MemberWithdrawal",
					UtilMisc.toMap("memberWithdrawalId", memberWithdrawalId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		partyId = memberWithdrawal.getLong("partyId").toString();

		return partyId;
	}

	/***
	 * Get Total Loan Balance
	 * */
	public static BigDecimal getLoanTotalLoanBalance(String partyId) {
		BigDecimal bdTotalLoanBalance = BigDecimal.ZERO;
		Long partyIdLong = Long.valueOf(partyId);
		bdTotalLoanBalance = LoansProcessingServices
				.getTotalDisbursedLoanBalances(partyIdLong);
		// LoansProcessingServices.getTotalLoanBalances(memberId,
		// loanProductId);
		bdTotalLoanBalance = bdTotalLoanBalance.setScale(2, RoundingMode.FLOOR);
		return bdTotalLoanBalance;
	}

	/***
	 * Get total Accrued Interest
	 * */
	public static BigDecimal getTotalAccruedInterest(String partyId) {
		BigDecimal bdTotalAccruedInterest = BigDecimal.ZERO;
		// Long partyIdLong = Long.valueOf(partyId);
		bdTotalAccruedInterest = LoanRepayments.getTotalInterestDue(partyId);
		
		bdTotalAccruedInterest = bdTotalAccruedInterest.setScale(2, RoundingMode.FLOOR);
		return bdTotalAccruedInterest;
	}

	/***
	 * Get total Accrued Insurance
	 * **/
	public static BigDecimal getTotalAccruedInsurance(String partyId) {
		BigDecimal bdTotalAccruedInsurance = BigDecimal.ZERO;
		bdTotalAccruedInsurance = LoanRepayments.getTotalInsuranceDue(partyId);

		bdTotalAccruedInsurance = bdTotalAccruedInsurance.setScale(2, RoundingMode.FLOOR);
		return bdTotalAccruedInsurance;
	}

	/****
	 * @author Japheth Odonya @when Jun 23, 2015 7:36:39 PM
	 * 
	 *         Total Guaranteed Amount by Member
	 * */
	public static BigDecimal getTotalGuaranteedAmount(String partyId) {
		Long partyIdLong = Long.valueOf(partyId);
		BigDecimal bdTotalGuaranteedAmount = BigDecimal.ZERO;

		// Get the list of guarateed loans
		// LoanGuarantorDisbursedLoan
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> loanGuarantorDisbursedLoanELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> loanGuarantorDisbursedLoanConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"guarantorId", EntityOperator.EQUALS, partyIdLong),

				EntityCondition.makeCondition("loanStatusId",
						EntityOperator.EQUALS, 6L)

				), EntityOperator.AND);

		try {
			loanGuarantorDisbursedLoanELI = delegator.findList(
					"LoanGuarantorDisbursedLoan",
					loanGuarantorDisbursedLoanConditions, null, null, null,
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : loanGuarantorDisbursedLoanELI) {
			bdTotalGuaranteedAmount = bdTotalGuaranteedAmount
					.add(getLoanGuarateedAmountByGuarantor(
							genericValue.getLong("loanApplicationId"),
							genericValue.getLong("guarantorId")));
		}

		return bdTotalGuaranteedAmount;
	}

	/***
	 * @author Japheth Odonya @when Jun 23, 2015 7:49:56 PM
	 * 
	 *         Share Capital Limit
	 * */
	public static BigDecimal getShareCapitalMinimum(String partyId) {
		// Get Member

		GenericValue member = LoanUtilities.getMember(partyId);

		Long memberClassId = member.getLong("memberClassId");

		if (memberClassId == null) {
			memberClassId = LoanUtilities.getMemberClassId("Class A");
		}

		BigDecimal bdShareCapitalLimit = getMinimumShareCapital(memberClassId);

		// BigDecimal bdShareCapitalLimit = new BigDecimal(20000);
		return bdShareCapitalLimit;
	}

	private static BigDecimal getMinimumShareCapital(Long memberClassId) {
		GenericValue shareMinimum = LoanUtilities
				.getShareMinimumEntity(memberClassId);

		if (shareMinimum == null)
			return null;

		return shareMinimum.getBigDecimal("minShareCapital");
	}

	/***
	 * @author Japheth Odonya @when Jun 30, 2015 1:20:26 PM
	 * 
	 *         Update Share Capital for a member
	 * 
	 * */
	public static String updateMemberShareCapitalWithMemberDeposits(
			Long partyId, Map<String, String> userLogin) {

		BigDecimal bdShareCapitalBalance = BigDecimal.ZERO;
		bdShareCapitalBalance = LoanUtilities
				.getShareCapitalAccountBalance(partyId);
		bdShareCapitalBalance = bdShareCapitalBalance.setScale(2,
				RoundingMode.HALF_EVEN);

		BigDecimal bdShareCapitalLimit = LoanUtilities
				.getShareCapitalLimit(partyId);
		bdShareCapitalLimit = bdShareCapitalLimit.setScale(2,
				RoundingMode.HALF_EVEN);

		BigDecimal bdMemberDepositAmount = LoanUtilities
				.getMemberDepositAmount(partyId);
		bdMemberDepositAmount = bdMemberDepositAmount.setScale(2,
				RoundingMode.HALF_EVEN);

		if (bdShareCapitalBalance.compareTo(bdShareCapitalLimit) == 0)
			return "The minimum share limit already met, no need for offsetting !!";

		BigDecimal bdShareCapitalDeficit = bdShareCapitalLimit
				.subtract(bdShareCapitalBalance);

		if (bdMemberDepositAmount.compareTo(bdShareCapitalDeficit) == -1)
			return "The Member deposit amount is not enough to pay for the share capital deficit! Please pay over the counter";

		// (String sourceMemberAccountId, String destinationMemberAccountId,
		// BigDecimal bdAmount, Map<String, String> userLogin)
		// Do an account transfer - from member deposit to share capital be the
		// deficit amount
		String sourceMemberAccountId = LoanUtilities
				.getMemberAccountIdGivenMemberAndAccountCode(partyId,
						AccHolderTransactionServices.MEMBER_DEPOSIT_CODE);
		String destinationMemberAccountId = LoanUtilities
				.getMemberAccountIdGivenMemberAndAccountCode(partyId,
						AccHolderTransactionServices.SHARE_CAPITAL_CODE);
		AccHolderTransactionServices.accountTransferTransaction(
				sourceMemberAccountId, destinationMemberAccountId,
				bdShareCapitalDeficit, userLogin);
		// AccHolderTransactionServices.postTransactionEntry(delegator,
		// bdLoanAmount, branchId, partyId, loanReceivableAccount, postingType,
		// acctgTransId, acctgTransType, entrySequenceId);

		return "success";
	}

	/******
	 * @author Japheth Odonya @when Jun 30, 2015 1:23:04 PM
	 * 
	 *         Offset Disbursed Loans With Member Deposits
	 * **/
	public static String offsetDisbursedLoansWithMemberDeposits(Long partyId,
			Map<String, String> userLogin, Long memberWithdrawalId) {
		
		List<String> defaultedLoansList = LoansProcessingServices.getDefaultedLoanApplicationList(partyId);
		if ((defaultedLoansList != null) && (defaultedLoansList.size() > 0)){
			return "The member has "+defaultedLoansList.size()+" defaulted loan(s), cannot therefore withdraw from the society !";
		}
		
		String memberAccountIdString = LoanUtilities.getMemberAccountIdGivenMemberAndAccountCode(partyId, AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);

		if (memberAccountIdString == null){
			return "Member must have a savings account where his/her balance will be posted";
		}
		
		//Replace guarantors for members disbursed loans
		List<String> loansGuaranteedList = LoansProcessingServices.getLoansGuaranteedList(partyId);
		if ((loansGuaranteedList != null) && (loansGuaranteedList.size() > 0))
			return " Must replace all guarantors before withdrawing a member !";
		
		Long entrySequenceId = 0L;
		String postingType = "";

		String employeeBranchId = AccHolderTransactionServices
				.getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = LoanUtilities.getMemberBranchId(partyId
				.toString());

		String acctgTransType = "MEMBER_DEPOSIT";
		String acctgTransId = AccHolderTransactionServices
				.createAccountingTransaction(null, acctgTransType, userLogin);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		BigDecimal bdTotalFromExcessPaymentAndCharged = BigDecimal.ZERO;
		//Check that a member does not have any loan with negative balance 
		String glAccountId = "";
		String setupType = "";
		
		List<String> listDisbursedLoans = LoansProcessingServices
				.getLoanApplicationList(partyId);
		for (String disbursedLoanId : listDisbursedLoans) {
			//Ensure that each of these loans does not have -ve balance
			BigDecimal disbursedLoanTotalInterestAmount = LoanRepayments.getTotalInterestByLoanDue(disbursedLoanId);
			disbursedLoanTotalInterestAmount = disbursedLoanTotalInterestAmount.setScale(2, RoundingMode.FLOOR);
			if (disbursedLoanTotalInterestAmount.compareTo(BigDecimal.ZERO) == -1){
				//return " One or more loans have an overpayment on Interest, please make sure the loans overpaid or with negative balances and recovered by passing a JV";
				//Claim interest amount
				//claimMemberInterest(acctgTransId, acctgTransType, disbursedLoanTotalInterestAmount.abs(), Long.valueOf(disbursedLoanId), userLogin);
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(disbursedLoanId), null, disbursedLoanTotalInterestAmount.abs(), null, disbursedLoanTotalInterestAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");

				//Debit Interest Receivable
				postingType = "D";

				setupType = "INTERESTPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, disbursedLoanTotalInterestAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				//save a negative and add a repayment on loan
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(disbursedLoanTotalInterestAmount.abs());
			}
			
			BigDecimal disbursedLoanTotalInsuranceAmount = LoanRepayments.getTotalInsurancByLoanDue(disbursedLoanId);
			disbursedLoanTotalInsuranceAmount = disbursedLoanTotalInsuranceAmount.setScale(2, RoundingMode.FLOOR);
			if (disbursedLoanTotalInsuranceAmount.compareTo(BigDecimal.ZERO) == -1){
				//return " One or more loans have an overpayment on Insurance, please make sure the loans overpaid or with negative balances and recovered by passing a JV";
				//Claim insurance amount
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(disbursedLoanId), null, null, disbursedLoanTotalInsuranceAmount.abs(), disbursedLoanTotalInsuranceAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");
			
				//Debit insurance receivable
				postingType = "D";

				setupType = "INSURANCEPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, disbursedLoanTotalInsuranceAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				//save a negative and add a repayment on loan
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(disbursedLoanTotalInsuranceAmount.abs());
			}
			
			
			BigDecimal disbursedLoanBalanceAmount = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(Long.valueOf(disbursedLoanId));
			disbursedLoanBalanceAmount = disbursedLoanBalanceAmount.setScale(2, RoundingMode.FLOOR);

			if (disbursedLoanBalanceAmount.compareTo(BigDecimal.ZERO) == -1){
				//return " One or more loans have an overpayment on principal, please make sure the loans overpaid or with negative balances and recovered by passing a JV";
				//Claim excess payment
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(disbursedLoanId), disbursedLoanBalanceAmount.abs(), null, null, disbursedLoanBalanceAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");
				//Save negative
			
				//Debit Loan to members
				postingType = "D";

				setupType = "PRINCIPALPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, disbursedLoanBalanceAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(disbursedLoanBalanceAmount.abs());
			}


		}
		
		List<String> listClearedLoans = LoansProcessingServices.getLoanApplicationListClearedLoans(partyId);
		for (String clearedLoanId : listClearedLoans) {
			BigDecimal clearedLoanTotalInterestAmount = LoanRepayments.getTotalInterestByLoanDue(clearedLoanId);
			clearedLoanTotalInterestAmount = clearedLoanTotalInterestAmount.setScale(2, RoundingMode.FLOOR);
			if (clearedLoanTotalInterestAmount.compareTo(BigDecimal.ZERO) == -1)
			{
				//Claim interest amount
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(clearedLoanId), null, clearedLoanTotalInterestAmount.abs(), null, clearedLoanTotalInterestAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");

				//Debit interest receivable
				postingType = "D";

				setupType = "INTERESTPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, clearedLoanTotalInterestAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(clearedLoanTotalInterestAmount.abs());
				
			}
				//return " One or more loans have an overpayment on Interest, please make sure the loans overpaid or with negative balances and recovered by passing a JV";
			
			BigDecimal clearedLoanTotalInsuranceAmount = LoanRepayments.getTotalInsurancByLoanDue(clearedLoanId);
			clearedLoanTotalInsuranceAmount = clearedLoanTotalInsuranceAmount.setScale(2, RoundingMode.FLOOR);
			if (clearedLoanTotalInsuranceAmount.compareTo(BigDecimal.ZERO) == -1)
			{
				//Claim insurance Amount
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(clearedLoanId), null, null, clearedLoanTotalInsuranceAmount.abs(), clearedLoanTotalInsuranceAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");
				
				//Debit insurance receivable
				postingType = "D";

				setupType = "INSURANCEPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, clearedLoanTotalInsuranceAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(clearedLoanTotalInsuranceAmount.abs());
			
			}
				//return " One or more loans have an overpayment on Insurance, please make sure the loans overpaid or with negative balances and recovered by passing a JV";

			BigDecimal clearedLoanBalanceAmount = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(Long.valueOf(clearedLoanId));
			clearedLoanBalanceAmount = clearedLoanBalanceAmount.setScale(2, RoundingMode.FLOOR);
			if (clearedLoanBalanceAmount.compareTo(BigDecimal.ZERO) == -1)
			{
				//Claim Loan Balance
				LoanUtilities.reduceLoanRepaymentInSourceWithType(Long.valueOf(clearedLoanId), clearedLoanBalanceAmount.abs(), null, null, clearedLoanBalanceAmount.abs(), userLogin, acctgTransId, "OVERRECOVERY");
			
				//Debit Loan to Members
				postingType = "D";

				setupType = "PRINCIPALPAYMENT";
				GenericValue accountHolderTransactionSetup = LoanRepayments
						.getAccountHolderTransactionSetupRecord(setupType, delegator);
				glAccountId = accountHolderTransactionSetup
						.getString("memberDepositAccId");
				
				entrySequenceId = entrySequenceId + 1;
				AccHolderTransactionServices.postTransactionEntry(delegator, clearedLoanBalanceAmount.abs(), employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());

				
				bdTotalFromExcessPaymentAndCharged = bdTotalFromExcessPaymentAndCharged.add(clearedLoanBalanceAmount.abs());

			}
				//return " One or more loans have an overpayment on principal, please make sure the loans overpaid or with negative balances and recovered by passing a JV";
			
		}
		
		//Save the excess to savings
		//bdTotalFromExcessPaymentAndCharged
		log.info("//////////////////////// Posting the voucher");
		Long memberAccountId = Long.valueOf(memberAccountIdString);
		String transactionType = "OVERRECOVERY";
		//AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, genericValue.getLong("generalglHeaderId"));
		AccHolderTransactionServices.memberAccountJournalVoucher(bdTotalFromExcessPaymentAndCharged, memberAccountId, userLogin, transactionType, null, acctgTransId);

		//post the credit over recovery
		if (bdTotalFromExcessPaymentAndCharged.compareTo(BigDecimal.ZERO) > 0) {
			entrySequenceId = entrySequenceId + 1;
			postingType = "C";
			glAccountId = LoanUtilities
					.getSavingsAccountglAccountId();
			AccHolderTransactionServices.postTransactionEntry(delegator,
					bdTotalFromExcessPaymentAndCharged, employeeBranchId, memberBranchId,
					glAccountId, postingType, acctgTransId,
					acctgTransType, entrySequenceId.toString());
			}

		
		BigDecimal bdMemberDepositsBalanceNow = getMemberDepositBalance(partyId.toString());
		if (((listDisbursedLoans == null) || (listDisbursedLoans.size() < 1)) && (bdMemberDepositsBalanceNow.compareTo(BigDecimal.ZERO) < 1))
			return " The member has already offset or cleared all his/her loans and deposits are zero";

		BigDecimal bdShareCapitalBalance = BigDecimal.ZERO;
		bdShareCapitalBalance = LoanUtilities
				.getShareCapitalAccountBalance(partyId);
		bdShareCapitalBalance = bdShareCapitalBalance.setScale(2,
				RoundingMode.FLOOR);

		BigDecimal bdShareCapitalLimit = LoanUtilities
				.getShareCapitalLimit(partyId);
		bdShareCapitalLimit = bdShareCapitalLimit.setScale(2,
				RoundingMode.FLOOR);

		BigDecimal bdMemberDepositAmount = LoanUtilities
				.getMemberDepositAmount(partyId);
		bdMemberDepositAmount = bdMemberDepositAmount.setScale(2,
				RoundingMode.FLOOR);

		if (bdShareCapitalBalance.compareTo(bdShareCapitalLimit) == -1)
			return "Please update share capital to share limit amount first before trying to offset loans. Share Limit amount is "
					+ bdShareCapitalLimit
					+ " while your share balance is "
					+ bdShareCapitalBalance;
		

		
		// Get Member Withdrawal Commission
		GenericValue memberWithdrawalCommission = LoanUtilities
				.getProductChargeEntity("Member Withdrawal Commission");
		if (memberWithdrawalCommission == null)
			return "Please make sure that you have defined member withdrawal commission in the Product Charge using the name 'Member Withdrawal Commission'";

		if (memberWithdrawalCommission.getString("chargeAccountId") == null)
			return "Withdrawal commission charge must have a gl account specified for correct posting of the GL";

		if (memberWithdrawalCommission.getBigDecimal("fixedAmount") == null)
			return "Please specify the Member Withdrawal Commission Charge Amount in the Product Charges";

		GenericValue memberWithdrawalExciseDuty = LoanUtilities
				.getProductChargeEntity("Member Withdrawal Excise Duty");
		if (memberWithdrawalExciseDuty == null)
			return "Please make sure that you have defined member withdrawal Excise Duty in the Product Charge using the name 'Member Withdrawal Excise Duty'";

		if (memberWithdrawalExciseDuty.getString("chargeAccountId") == null)
			return "Withdrawal Excise Duty must have a gl account specified for correct posting of the GL";

		if (memberWithdrawalExciseDuty.getBigDecimal("rateAmount") == null)
			return "Please specify the Member Withdrawal Excise Charge Amount in the Product Charges";

		BigDecimal bdMemberWithdrawalCommissionAmount = memberWithdrawalCommission
				.getBigDecimal("fixedAmount");

		BigDecimal bdMemberWithdrawalExciseDuty = memberWithdrawalExciseDuty
				.getBigDecimal("rateAmount")
				.multiply(bdMemberWithdrawalCommissionAmount)
				.divide(new BigDecimal(100), 4, RoundingMode.FLOOR);
		BigDecimal bdTotalCharge = bdMemberWithdrawalCommissionAmount
				.add(bdMemberWithdrawalExciseDuty);

		// Check if member deposits is enough to offset loans (more than loans
		// and withdrawal commission)
		BigDecimal bdLoanBalancesTotal = LoansProcessingServices
				.getTotalDisbursedLoanBalances(partyId);
		BigDecimal bdTotalLoanBalance = bdLoanBalancesTotal;

		BigDecimal bdMemberDepositBalance = getMemberDepositBalance(partyId
				.toString());

		BigDecimal bdTotalOffset = bdLoanBalancesTotal.add(bdTotalCharge);

		BigDecimal bdTotalInterestDue = getTotalAccruedInterest(partyId.toString());
				
				//LoanRepayments
				//.getTotalInterestDue(partyId.toString());
		
		BigDecimal bdTotalInsuranceDue = getTotalAccruedInsurance(partyId.toString());
			//	LoanRepayments
			//	.getTotalInsuranceDue(partyId.toString());

		bdTotalOffset = bdTotalOffset.add(bdTotalInterestDue).add(
				bdTotalInsuranceDue);
		if (bdMemberDepositBalance.compareTo(bdTotalOffset) == -1) {
			return " Member Deposits not enough to offset the loans balances (Principal Balance + Interest + Insurance) and pay the charges Member Deposit Amount is "
					+ bdMemberDepositAmount
					+ " Loan Balance Amount plus commission is "
					+ bdTotalOffset;
		}


		/***
		 * Do GL Posting from here
		 * */
		// Dr Member Deposits (Savings Non Withdrawal) with total
		// (Loans + Insurance + Interest + Commission Charge + Excise Duty +
		// Balance to go to savings)
		// bdMemberDepositBalance
		String memberDepositGLAccountId = LoanUtilities
				.getMemberDepositsGLAccountId();

		entrySequenceId = entrySequenceId + 1;
		postingType = "D";
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdMemberDepositBalance, employeeBranchId, memberBranchId,
				memberDepositGLAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId.toString());

		// Credit Loan Receivable bdTotalLoanBalance
		// PRINCIPALPAYMENT
		String loanReceivableAccountId = LoanUtilities
				.getTransactionGLAccountGivenTransactionName("PRINCIPALPAYMENT");

		if (bdTotalLoanBalance.compareTo(BigDecimal.ZERO) > 0) {
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdTotalLoanBalance, employeeBranchId, memberBranchId,
				loanReceivableAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId.toString());
		}

		// Credit Interest Receivable bdTotalInterestDue
		// INTERESTPAYMENT
		if (bdTotalInterestDue.compareTo(BigDecimal.ZERO) > 0) {
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";
		String interestAccountId = LoanUtilities
				.getTransactionGLAccountGivenTransactionName("INTERESTPAYMENT");
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdTotalInterestDue, employeeBranchId, memberBranchId,
				interestAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId.toString());
		}

		// Credit Insurance Receivable bdTotalInsuranceDue
		// INSURANCEPAYMENT
		
		if (bdTotalInsuranceDue.compareTo(BigDecimal.ZERO) > 0) {
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";

		String insuranceAccountId = LoanUtilities
				.getTransactionGLAccountGivenTransactionName("INSURANCEPAYMENT");
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdTotalInsuranceDue, employeeBranchId, memberBranchId,
				insuranceAccountId, postingType, acctgTransId, acctgTransType,
				entrySequenceId.toString());
		}

		// Credit Withdrawal Commission bdMemberWithdrawalCommissionAmount
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";
		String withdrawalCommissionAccountId = memberWithdrawalCommission
				.getString("chargeAccountId");
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdMemberWithdrawalCommissionAmount, employeeBranchId,
				memberBranchId, withdrawalCommissionAccountId, postingType,
				acctgTransId, acctgTransType, entrySequenceId.toString());

		// Credit Excise Duty bdMemberWithdrawalExciseDuty
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";
		String withdrawalExciseDutyAccountId = memberWithdrawalExciseDuty
				.getString("chargeAccountId");
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdMemberWithdrawalExciseDuty, employeeBranchId, memberBranchId,
				withdrawalExciseDutyAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId.toString());

		BigDecimal bdBalanceToSavings = bdMemberDepositBalance
				.subtract(bdTotalOffset);
		// Credit Deposits Withdrawable (Savings) with Balance (Member Deposits
		// Account - Total Offset)
		// bdBalanceToSavings
		if (bdBalanceToSavings.compareTo(BigDecimal.ZERO) > 0) {
		entrySequenceId = entrySequenceId + 1;
		postingType = "C";
		String savingsAccountGLAccountId = LoanUtilities
				.getSavingsAccountglAccountId();
		AccHolderTransactionServices.postTransactionEntry(delegator,
				bdBalanceToSavings, employeeBranchId, memberBranchId,
				savingsAccountGLAccountId, postingType, acctgTransId,
				acctgTransType, entrySequenceId.toString());
		}

		/***
		 * Do MPA Posting from here
		 * */

		// Offset the loans
		// LoanRepayments.repayLoan(loanRepayment, userLogin);
		// LoanRepayments.repayLoanWithoutDebitingCash(loanRepayment, userLogin,
		// entrySequence);
		List<String> loanApplicationIdsList = LoansProcessingServices
				.getLoanApplicationList(partyId);

		for (String loanApplicationId : loanApplicationIdsList) {
			localLoanRepay(loanApplicationId, userLogin, acctgTransId);
		}

		// Debit Member Deposit Account with the total balance
		// bdMemberDepositBalance
		memberAccountId = Long.valueOf(LoanUtilities
				.getMemberAccountIdGivenMemberAndAccountCode(partyId,
						AccHolderTransactionServices.MEMBER_DEPOSIT_CODE));
		AccHolderTransactionServices.cashDepositVersion4(
				bdMemberDepositBalance, memberAccountId,
				userLogin, "MEMBERWITHDRAWAL", acctgTransId);

		// Credit Savings Account with the total remaining
		// bdBalanceToSavings

		if (bdBalanceToSavings.compareTo(BigDecimal.ZERO) > 0) {
			String savingsMemberAccountId = LoanUtilities
					.getMemberAccountIdGivenMemberAndAccountCode(partyId,
							AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
			AccHolderTransactionServices.cashDepositVersion4(
					bdBalanceToSavings, Long.valueOf(savingsMemberAccountId),
					userLogin, "FROMMEMBERWITHDRAWAL", acctgTransId);
		}
		
		//Now approve member
		GenericValue member = LoanUtilities.getEntityValue("Member", "partyId", partyId);
		member.set("memberStatusId", LoanUtilities.getMemberStatusId("CLOSED"));
		try {
			delegator.createOrStore(member);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create member withdrawal log
		GenericValue withdrawalLog = null;
		Long withdrawalLogId = delegator.getNextSeqIdLong("WithdrawalLog");
		withdrawalLog = delegator.makeValue("WithdrawalLog", UtilMisc.toMap(
				"withdrawalLogId", withdrawalLogId, "isActive", "Y",
				"createdBy", userLogin.get("userLoginId"),
				"withdrawalstatus", "APPROVED",
				"memberWithdrawalId", memberWithdrawalId

				
				));
		
		try {
			delegator.createOrStore(withdrawalLog);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Update the Withdrawal with transaction ID
		GenericValue withdrawal = LoanUtilities.getEntityValue("MemberWithdrawal", "memberWithdrawalId", memberWithdrawalId);
		withdrawal.set("acctgTransId", acctgTransId);
		withdrawal.set("withdrawalstatus", "APPROVED");
		try {
			delegator.createOrStore(withdrawal);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//, "acctgTransId", acctgTransId
		return "success";
	}


	private static void localLoanRepay(String loanApplicationId,
			Map<String, String> userLogin, String acctgTransId) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = LoanUtilities
				.getLoanApplicationEntity(Long.valueOf(loanApplicationId));
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		GenericValue loanRepayment = null;

		BigDecimal totalLoanDue = LoansProcessingServices
				.getTotalLoanBalancesByLoanApplicationId(Long
						.valueOf(loanApplicationId));
		BigDecimal totalInterestDue = LoanRepayments
				.getTotalInterestByLoanDue(loanApplicationId);
		BigDecimal totalInsuranceDue = LoanRepayments
				.getTotalInsurancByLoanDue(loanApplicationId);
		Long partyId = loanApplication.getLong("partyId");

		BigDecimal transactionAmount = totalLoanDue.add(totalInterestDue).add(
				totalInsuranceDue);

		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLogin.get("userLoginId"), "partyId", partyId,
				"loanApplicationId", Long.valueOf(loanApplicationId),

				"loanNo", loanApplication.getString("loanNo"), "loanAmt",
				loanApplication.getBigDecimal("loanAmt"),

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalLoanDue, "interestAmount",
				totalInterestDue, "insuranceAmount", totalInsuranceDue,
				"principalAmount", totalLoanDue, "transactionAmount",
				transactionAmount, "acctgTransId", acctgTransId));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// Update Loan as Cleared
		loanApplication.set("loanStatusId",
				LoanUtilities.getLoanStatusId("CLEARED"));
		try {
			delegator.createOrStore(loanApplication);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/****
	 * @author Japheth Odonya @when Jul 1, 2015 11:41:42 PM
	 * 
	 *         Get total loan due (principal + interest + insurance)
	 * */
	public static BigDecimal getGrandTotal(String partyId) {
		BigDecimal bdGrandTotal = BigDecimal.ZERO;
		bdGrandTotal = bdGrandTotal.add(LoansProcessingServices
				.getTotalDisbursedLoanBalances(Long.valueOf(partyId)));
		bdGrandTotal = bdGrandTotal.add(LoanRepayments
				.getTotalInterestDue(partyId));
		bdGrandTotal = bdGrandTotal.add(LoanRepayments
				.getTotalInsuranceDue(partyId));
		
		bdGrandTotal = bdGrandTotal.setScale(2, RoundingMode.FLOOR);
		return bdGrandTotal;
	}
}
