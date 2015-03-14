package org.ofbiz.salaryprocessing;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.ofbiz.loans.LoanServices;
import org.ofbiz.webapp.event.EventHandlerException;

public class SalaryProcessingServices {
	public static Logger log = Logger.getLogger(SalaryProcessingServices.class);

	public static String processSalaryReceivedNoDeduct(
			HttpServletRequest request, HttpServletResponse response) {

		// salaryMonthYearId
		String salaryMonthYearId = (String) request
				.getParameter("salaryMonthYearId");

		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		GenericValue salaryMonthYear = null;
		salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = Long.valueOf(salaryMonthYearId);
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);

		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode);

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "MISSING";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", "999");

		Long productChargeId = null;
		GenericValue productCharge = null;

		BigDecimal bdSalaryChargeAmt = BigDecimal.ZERO;
		BigDecimal bdSalaryExciseAmt = BigDecimal.ZERO;
		Long salaryProductChargeId = null;
		String salaryProductChargeName = null;
		Long salaryExciseDutyId = null;
		String salaryExciseDutyName = null;

		for (GenericValue genericValue : listAccountProductCharge) {
			productChargeId = genericValue.getLong("productChargeId");
			log.info(" CCCCCCCCCCCCCCC Charge ID "
					+ genericValue.getLong("productChargeId"));

			productCharge = LoanUtilities.getProductCharge(productChargeId);

			if (productCharge != null)
				log.info(" CCCCCCCCCCCCCCC Charge Name "
						+ productCharge.getString("name"));

			// if has no parent the its salary charge amount
			if (genericValue.getLong("parentChargeId") == null) {
				// Its salary amount
				bdSalaryChargeAmt = genericValue.getBigDecimal("fixedAmount");

				salaryProductChargeId = genericValue.getLong("productChargeId");
				salaryProductChargeName = productCharge.getString("name");
			}

			// if has parent then its excise duty amount
			if (genericValue.getLong("parentChargeId") != null) {
				// Its salary amount
				bdSalaryExciseAmt = genericValue.getBigDecimal("rateAmount")
						.multiply(bdSalaryChargeAmt)
						.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);

				salaryExciseDutyId = genericValue.getLong("productChargeId");
				salaryExciseDutyName = productCharge.getString("name");
			}

			log.info(" CCCCCCCCCCCCCCCSSSS Salary Charge " + bdSalaryChargeAmt);
			log.info(" CCCCCCCCCCCCCCCSSSS Excise Duty " + bdSalaryExciseAmt);
		}

		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");
		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty
		doProcessing(userLogin, month, year, employerCode, bdSalaryChargeAmt,
				bdSalaryExciseAmt, salaryProductChargeId,
				salaryProductChargeName, salaryExciseDutyId,
				salaryExciseDutyName);

		log.info("HHHHHHHHHHHH Salary Processing ... No Deductions !!!");

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
		return "SUCCESS";
	}

	private static void doProcessing(Map<String, String> userLogin,
			String month, String year, String employerCode,
			BigDecimal bdSalaryChargeAmt, BigDecimal bdSalaryExciseAmt,
			Long salaryProductChargeId, String salaryProductChargeName,
			Long salaryExciseDutyId, String salaryExciseDutyName) {
		// Get the payroll numbers from MemberSalary given month, year and
		// employerCode
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode);

		BigDecimal bdTotalSalaryPosted = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryExciseDuty = BigDecimal.ZERO;
		BigDecimal bdNetSalaryAmt = BigDecimal.ZERO;
		Long memberAccountId = null;
		String payrollNumber = null;

		String accountTransactionParentId = null;

		List<GenericValue> listSalaryToUpdate = new ArrayList<GenericValue>();
		for (GenericValue genericValue : listMemberSalary) {
			bdNetSalaryAmt = genericValue.getBigDecimal("netSalary");
			bdTotalSalaryPosted = bdTotalSalaryPosted.add(bdNetSalaryAmt);

			accountTransactionParentId = AccHolderTransactionServices
					.getcreateAccountTransactionParentId(memberAccountId,
							userLogin);
			payrollNumber = genericValue.getString("payrollNumber");
			// memberAccountId = LoanUtilities.getS
			// Add Net Salary to the Savings Account
			memberAccountId = AccHolderTransactionServices
					.getMemberSavingsAccountId(payrollNumber);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdNetSalaryAmt, memberAccountId, userLogin,
					"SALARYPROCESSING", accountTransactionParentId, null);

			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdTotalSalaryCharge, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString());
			// Add Excise Duty
			bdTotalSalaryExciseDuty = bdTotalSalaryExciseDuty
					.add(bdSalaryExciseAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdTotalSalaryExciseDuty, memberAccountId, userLogin,
					salaryExciseDutyName, accountTransactionParentId,
					salaryExciseDutyId.toString());

			genericValue.set("processed", "Y");
			listSalaryToUpdate.add(genericValue);
		}

		// Post Total Net Salary in GL bdTotalSalaryPosted

		// Create One AcctgTrans
		GenericValue accountTransaction = null;
		String acctgTransId = AccHolderTransactionServices
				.creatAccountTransRecord(accountTransaction, userLogin);
		// SALARYPROCESSING
		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("SALARYPROCESSING");
		String debitAccountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		String creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		// salaryProductChargeId = salaryProductChargeId.replaceAll(",", "");
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");

		Long entrySequence = 1L;
		// ------------------------
		// Debit Leaf Base with the total
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryPosted, acctgTransId, "D", debitAccountId, entrySequence.toString());

		BigDecimal bdTotalCharges = bdTotalSalaryCharge
				.add(bdTotalSalaryExciseDuty);
		BigDecimal bdTotalMemberDepositAmt = bdTotalSalaryPosted
				.subtract(bdTotalCharges);

		// Credit Member Deposits with (total net - (total charge + total excise
		// duty))
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalMemberDepositAmt, acctgTransId, "C", creditAccountId, entrySequence.toString());
		// Credit Salary Charge with total salary charge
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryCharge, acctgTransId, "C",
				salaryChargeCreditAccountId, entrySequence.toString());
		// Credit Excise Duty with total excise duty
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryExciseDuty, acctgTransId, "C",
				salaryExciseCreditAccountId, entrySequence.toString());

		// Update the MemberSalary to processed

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listSalaryToUpdate);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Prior Transaction when a cheque was paid
		// Debit Cash at Bank
		// Credit Leaf Base

	}

	private static List<GenericValue> getMemberSalaryList(String month,
			String year, String employerCode) {
		List<GenericValue> memberSalaryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> memberSalaryConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month),

				EntityCondition.makeCondition("year", EntityOperator.EQUALS,
						year),

				EntityCondition.makeCondition("employerCode",
						EntityOperator.EQUALS, employerCode),
				// processed
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, null)),
						EntityOperator.AND);

		try {
			memberSalaryELI = delegator.findList("MemberSalary",
					memberSalaryConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberSalaryELI;
	}

	private static Boolean getMissingPayrollNumbers(String month, String year,
			String employerCode) {
		Boolean missing = false;

		// Get All the Payroll Numbers in the Member Salary given month, year
		// and employerCode
		List<GenericValue> memberSalaryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> memberSalaryConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month), EntityCondition
						.makeCondition("year", EntityOperator.EQUALS, year),

				EntityCondition.makeCondition("employerCode",
						EntityOperator.EQUALS, employerCode)),
						EntityOperator.AND);

		try {
			memberSalaryELI = delegator.findList("MemberSalary",
					memberSalaryConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue missingSalaryPayrollNumber = null;
		String payrollNumber = "";
		for (GenericValue genericValue : memberSalaryELI) {
			// check if this Payroll Number exists in Member
			// If does not exist set missing to true
			payrollNumber = genericValue.getString("payrollNumber");

			Boolean payrollExists = LoanUtilities
					.payrollNumberExists(payrollNumber);

			if (!payrollExists) {
				missing = true;

				// Save the Payroll Number Missing
				// Long missingMemberLogId =
				// delegator.getNextSeqIdLong("MissingMemberLog", 1);
				missingSalaryPayrollNumber = delegator.makeValue(
						"MissingSalaryPayrollNumber", UtilMisc.toMap(
								"isActive", "Y", "createdBy", "admin",
								"employerCode", employerCode, "payrollNumber",
								payrollNumber, "year", year, "month", month));
				try {
					delegator.createOrStore(missingSalaryPayrollNumber);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}

		}

		return missing;
	}

	public static String processSalaryReceivedDeduct(
			HttpServletRequest request, HttpServletResponse response) {

		// salaryMonthYearId
		String salaryMonthYearId = (String) request
				.getParameter("salaryMonthYearId");

		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		GenericValue salaryMonthYear = null;
		salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = Long.valueOf(salaryMonthYearId);
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);

		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode);

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "MISSING";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", "999");

		Long productChargeId = null;
		GenericValue productCharge = null;

		BigDecimal bdSalaryChargeAmt = BigDecimal.ZERO;
		BigDecimal bdSalaryExciseAmt = BigDecimal.ZERO;
		Long salaryProductChargeId = null;
		String salaryProductChargeName = null;
		Long salaryExciseDutyId = null;
		String salaryExciseDutyName = null;

		for (GenericValue genericValue : listAccountProductCharge) {
			productChargeId = genericValue.getLong("productChargeId");
			log.info(" CCCCCCCCCCCCCCC Charge ID "
					+ genericValue.getLong("productChargeId"));

			productCharge = LoanUtilities.getProductCharge(productChargeId);

			if (productCharge != null)
				log.info(" CCCCCCCCCCCCCCC Charge Name "
						+ productCharge.getString("name"));

			// if has no parent the its salary charge amount
			if (genericValue.getLong("parentChargeId") == null) {
				// Its salary amount
				bdSalaryChargeAmt = genericValue.getBigDecimal("fixedAmount");

				salaryProductChargeId = genericValue.getLong("productChargeId");
				salaryProductChargeName = productCharge.getString("name");
			}

			// if has parent then its excise duty amount
			if (genericValue.getLong("parentChargeId") != null) {
				// Its salary amount
				bdSalaryExciseAmt = genericValue.getBigDecimal("rateAmount")
						.multiply(bdSalaryChargeAmt)
						.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);

				salaryExciseDutyId = genericValue.getLong("productChargeId");
				salaryExciseDutyName = productCharge.getString("name");
			}

			log.info(" CCCCCCCCCCCCCCCSSSS Salary Charge " + bdSalaryChargeAmt);
			log.info(" CCCCCCCCCCCCCCCSSSS Excise Duty " + bdSalaryExciseAmt);
		}

		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");
		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty
		doProcessingWithDeductions(userLogin, month, year, employerCode,
				bdSalaryChargeAmt, bdSalaryExciseAmt, salaryProductChargeId,
				salaryProductChargeName, salaryExciseDutyId,
				salaryExciseDutyName);

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

	private static void doProcessingWithDeductions(
			Map<String, String> userLogin, String month, String year,
			String employerCode, BigDecimal bdSalaryChargeAmt,
			BigDecimal bdSalaryExciseAmt, Long salaryProductChargeId,
			String salaryProductChargeName, Long salaryExciseDutyId,
			String salaryExciseDutyName) {
		// Get the payroll numbers from MemberSalary given month, year and
		// employerCode
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode);

		BigDecimal bdTotalSalaryPosted = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryExciseDuty = BigDecimal.ZERO;
		BigDecimal bdNetSalaryAmt = BigDecimal.ZERO;
		Long memberAccountId = null;
		String payrollNumber = null;

		String accountTransactionParentId = null;

		List<GenericValue> listSalaryToUpdate = new ArrayList<GenericValue>();
		List<GenericValue> listLoanRepayments = new ArrayList<GenericValue>();
		
		for (GenericValue genericValue : listMemberSalary) {

			//###### Add the Net Salary to Member Account
			bdNetSalaryAmt = genericValue.getBigDecimal("netSalary");
			bdTotalSalaryPosted = bdTotalSalaryPosted.add(bdNetSalaryAmt);

			accountTransactionParentId = AccHolderTransactionServices
					.getcreateAccountTransactionParentId(memberAccountId,
							userLogin);
			payrollNumber = genericValue.getString("payrollNumber");
			// memberAccountId = LoanUtilities.getS
			// Add Net Salary to the Savings Account
			memberAccountId = AccHolderTransactionServices
					.getMemberSavingsAccountId(payrollNumber);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdNetSalaryAmt, memberAccountId, userLogin,
					"SALARYPROCESSING", accountTransactionParentId, null);
			
			//###### Deduct the Salary Processing Fee
			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdTotalSalaryCharge, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString());
			
			//####### Deduct the Excise Duty
			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdTotalSalaryCharge, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString());

			//####### Deduct the total Loan Deductions
			GenericValue member =	RemittanceServices.getMemberByPayrollNo(payrollNumber);
			List<Long> listLoanApplicationIds = LoanServices.getDisbursedLoansIds(member.getLong("partyId"));
			BigDecimal bdMemberTotalLoanExpectedAmt = BigDecimal.ZERO;
			BigDecimal bdLoanExpectedAmt = BigDecimal.ZERO;
			//String productName = "";
			GenericValue loanRepayment;
			Long loanRepaymentId = null;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			for (Long loanApplicationId : listLoanApplicationIds) {
				bdLoanExpectedAmt = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
				bdLoanExpectedAmt = bdLoanExpectedAmt.add(LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString()));
				bdLoanExpectedAmt = bdLoanExpectedAmt.add(LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString()));
				
				bdMemberTotalLoanExpectedAmt = bdMemberTotalLoanExpectedAmt.add(bdLoanExpectedAmt);
				AccHolderTransactionServices.memberTransactionDeposit(bdLoanExpectedAmt, memberAccountId, userLogin, "LOANREPAYMENT", accountTransactionParentId, null);
				
				BigDecimal totalLoanDue = LoanRepayments.getTotalLoanByLoanDue(loanApplicationId.toString());
				BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
				BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
				BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
				
				BigDecimal interestAmount = totalInterestDue;
				BigDecimal insuranceAmount = totalInsuranceDue;
				BigDecimal principalAmount = totalPrincipalDue;
				
				loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment");
				loanRepayment =  delegator.makeValue(
						"LoanRepayment", UtilMisc.toMap(
								"loanRepaymentId", loanRepaymentId,
								"isActive", "Y",
								"createdBy", "admin",
								"transactionType", "LOANREPAYMENT",
								"loanApplicationId", loanApplicationId,
								"partyId", member.getLong("partyId").toString(),
								
								"transactionAmount",
								bdLoanExpectedAmt,
								
								"totalLoanDue", totalLoanDue,
								
								"totalInterestDue", totalInterestDue,
								
								"totalInsuranceDue", totalInsuranceDue,
								
								"totalPrincipalDue", totalPrincipalDue,
								
								
								
								
								"interestAmount", interestAmount,
								"insuranceAmount", insuranceAmount,
								"principalAmount", principalAmount
								
								));
				
				try {
					delegator.createOrStore(loanRepayment);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//LoanRepayments.repayLoanWithoutDebitingCash(loanRepayment, userLogin);
				listLoanRepayments.add(loanRepayment);
			}
			
			// Deduct the total Account Contributions
			List<GenericValue> listMemberAccounts = LoanUtilities.getMemberContributingAccounts(member.getLong("partyId"));
			BigDecimal bdTotalContributingAmount = BigDecimal.ZERO;
			BigDecimal bdContributingAmt;
			for (GenericValue memberAccount : listMemberAccounts) {
				bdContributingAmt = LoanUtilities.getContributionAmount(memberAccount, member.getLong("partyId"));
				AccHolderTransactionServices.memberTransactionDeposit(bdContributingAmt, memberAccount.getLong("memberAccountId") , userLogin, "DEPOSITFROMSALARY", accountTransactionParentId, null);
				bdTotalContributingAmount = bdTotalContributingAmount.add(bdContributingAmt);
			}
			// Repay Loan with total loan repaid amount above for each loan
			// Make contribution to each account with the contribution amount

			// GL POsting
			// -- sum value for net salary - (salary processing fee + excise
			// duty + total loans + total account contributions) 




			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdTotalSalaryCharge, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString());

			genericValue.set("processed", "Y");
			listSalaryToUpdate.add(genericValue);
		}

		// Post Total Net Salary in GL bdTotalSalaryPosted

		// Create One AcctgTrans
		GenericValue accountTransaction = null;
		String acctgTransId = AccHolderTransactionServices
				.creatAccountTransRecord(accountTransaction, userLogin);
		// SALARYPROCESSING
		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("SALARYPROCESSING");
		String debitAccountId = accountHolderTransactionSetup
				.getString("cashAccountId");
		String creditAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		// salaryProductChargeId = salaryProductChargeId.replaceAll(",", "");
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");

		Long entrySequence = 1L;
		// ------------------------
		// Debit Leaf Base with the total
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryPosted, acctgTransId, "D", debitAccountId, entrySequence.toString());

		BigDecimal bdTotalCharges = bdTotalSalaryCharge
				.add(bdTotalSalaryExciseDuty);
		BigDecimal bdTotalMemberDepositAmt = bdTotalSalaryPosted
				.subtract(bdTotalCharges);

		// Credit Member Deposits with (total net - (total charge + total excise
		// duty))
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalMemberDepositAmt, acctgTransId, "C", creditAccountId, entrySequence.toString());
		// Credit Salary Charge with total salary charge
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryCharge, acctgTransId, "C",
				salaryChargeCreditAccountId, entrySequence.toString());
		// Credit Excise Duty with total excise duty
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryExciseDuty, acctgTransId, "C",
				salaryExciseCreditAccountId, entrySequence.toString());
		
		//Post the Loan Repayments
		for (GenericValue genericValue : listLoanRepayments) {
			//entrySequence = entrySequence + 1;
			LoanRepayments.repayLoanWithoutDebitingCash(genericValue, userLogin, entrySequence);
		}
		
		//listLoanRepayments.add(loanRepayment);

		// Update the MemberSalary to processed

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.storeAll(listSalaryToUpdate);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Prior Transaction when a cheque was paid
		// Debit Cash at Bank
		// Credit Leaf Base

	}

	public static BigDecimal getTotalNetSalaryAmount(Long salaryMonthYearId) {
		BigDecimal bdTotalAmount = BigDecimal.ZERO;

		// Need month, year and employerCode
		Long month = null;
		Long year = null;
		String employerCode = "";

		// Find the SalaryMonthYear
		GenericValue salaryMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			salaryMonthYear = delegator.findOne("SalaryMonthYear",
					UtilMisc.toMap("salaryMonthYearId", salaryMonthYearId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		month = salaryMonthYear.getLong("month");
		year = salaryMonthYear.getLong("year");

		GenericValue station = LoanUtilities.getStation(salaryMonthYear
				.getString("stationId"));
		employerCode = station.getString("employerCode");

		// Get total amount
		List<GenericValue> StationSalarySumsELI = new ArrayList<GenericValue>();
		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> StationSalarySumsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month.toString()),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year.toString()),

						EntityCondition.makeCondition("employerCode",
								EntityOperator.EQUALS, employerCode)),
						EntityOperator.AND);
		try {
			StationSalarySumsELI = delegator.findList("StationSalarySums",
					StationSalarySumsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : StationSalarySumsELI) {
			bdTotalAmount = bdTotalAmount.add(genericValue
					.getBigDecimal("netSalary"));
		}

		return bdTotalAmount;
	}

	private static void removeMissingPayrollNumbersLog(String month,
			String year, String employerCode) {
		List<GenericValue> missingSalaryPayrollNumberELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> missingSalaryPayrollNumberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month.trim()),

				EntityCondition.makeCondition("year", EntityOperator.EQUALS,
						year.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			missingSalaryPayrollNumberELI = delegator.findList(
					"MissingSalaryPayrollNumber",
					missingSalaryPayrollNumberConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		try {
			delegator.removeAll(missingSalaryPayrollNumberELI);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}

	public static Long countMissingPayrollNumbers(String month, String year,
			String employerCode) {
		List<GenericValue> missingSalaryPayrollNumberELI = new ArrayList<GenericValue>();

		EntityConditionList<EntityExpr> missingSalaryPayrollNumberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employerCode", EntityOperator.EQUALS,
						employerCode.trim()),

				EntityCondition.makeCondition("month", EntityOperator.EQUALS,
						month.trim()),

				EntityCondition.makeCondition("year", EntityOperator.EQUALS,
						year.trim())

				), EntityOperator.AND);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			missingSalaryPayrollNumberELI = delegator.findList(
					"MissingSalaryPayrollNumber",
					missingSalaryPayrollNumberConditions, null, null, null,
					false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return Long.valueOf(missingSalaryPayrollNumberELI.size());

	}

}
