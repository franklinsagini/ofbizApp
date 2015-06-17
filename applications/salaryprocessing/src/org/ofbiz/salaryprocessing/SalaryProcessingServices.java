package org.ofbiz.salaryprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
	//AccHolderTransactionServices.

	
	//	public static synchronized String processSalaryReceivedNoDeduct(
	//HttpServletRequest request, HttpServletResponse response)
	public static synchronized String processSalaryReceivedNoDeduct(
			Long salaryMonthYearId, Map<String, String> userLogin) {

		// salaryMonthYearId
		//String salaryMonthYearId = (String) request
		//		.getParameter("salaryMonthYearId");

		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		GenericValue salaryMonthYear = null;
		//salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = salaryMonthYearId;
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);
		
		List<GenericValue> listMemberSalaryItems = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		
		log.info("SSSSSSSSSSSS salaryMonthYearId SSSSSSSS ::: "+salaryMonthYearId);
		
		if ((listMemberSalaryItems == null) || (listMemberSalaryItems.size() < 1)){
			return " No data to process or station already processed !";
		}
		
		//Cheque that the amount available is equal to the total not salary
		BigDecimal bdTotalNetSalaryAmt = getTotalNetSalaryAmount(salaryMonthYearId);
		BigDecimal bdTotalChequeAmountAvailable = RemittanceServices.getTotalRemittedChequeAmountAvailable(employerCode, month, year);

		//Everything to 2 decimal places
		bdTotalNetSalaryAmt = bdTotalNetSalaryAmt.setScale(2, RoundingMode.HALF_DOWN);
		bdTotalChequeAmountAvailable = bdTotalChequeAmountAvailable.setScale(2, RoundingMode.HALF_DOWN);
		
		if (bdTotalNetSalaryAmt.compareTo(bdTotalChequeAmountAvailable) != 0){
			return "The available cheque amount must be equal to the salaries total, Total Salary Amount is "+bdTotalNetSalaryAmt+" while total cheque amounts is "+bdTotalChequeAmountAvailable;
		}
		
		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode, salaryMonthIdLong);
		
		

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "One or more payroll numbers missing in the system , please check the missing payroll numbers menu/link!";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}
		
		//Check that all members have Savings account - code 999
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		Boolean missingSavingsAccount =  false;
		String missingSavingsListing = "";
		//clearMissingMember
		RemittanceServices.clearMissingMember(month, employerCode);
		//RemittanceServices.re
		for (GenericValue memberSalary : listMemberSalary) {
			
			//Check if member has code 999 account
			//AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE
			if (!LoanUtilities.hasAccount(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, memberSalary.getString("payrollNumber"))){
				missingSavingsAccount = true;
				
				if (missingSavingsListing.equals("")){
					missingSavingsListing = memberSalary.getString("payrollNumber");
				} else{
					missingSavingsListing = missingSavingsListing + " , " + memberSalary.getString("payrollNumber");
				}
				
				//Add User to missing accounts
				
				RemittanceServices.addMissingMemberLog(userLogin, memberSalary.getString("payrollNumber"), month, employerCode, AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, null, null);
			}
		}
		
		if (missingSavingsAccount){
			return "There are member accounts missing, please check the Missing Members Members menu in Account Holders transactions . The list has these payrolls ("+missingSavingsListing+")";
		}
		

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = null;
		
		listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((listAccountProductCharge == null) || (listAccountProductCharge.size() < 2)){
			return " Please check that Salary Processing Charge and its excise duty are defined with correct amount / figure for each!";
		}

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
		
		//Check that Salary Charge and Excise duty have account set
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");
		
		if ((salaryChargeCreditAccountId == null) || (salaryChargeCreditAccountId.equals(""))){
			return "Please ensure that the Salary Processing charge has a gl account set !! Check Product Charge list in loans if charge account is specified";
		}

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");
		
		if ((salaryExciseCreditAccountId == null) || (salaryExciseCreditAccountId.equals(""))){
			return "Please ensure that the Excise charge has a GL Account set !! Check Product Charge list in loans if charge account is specified";
		}
		
		//Employee Must have a branch
		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		if ((branchId == null) || (branchId.equals("")))
			return "The employee logged into the system must have a branch, please check with HR!!";

		
		String savingsAccountGLAccountId = LoanUtilities.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((savingsAccountGLAccountId == null) || (savingsAccountGLAccountId.equals(""))){
			return "Please ensure that the Savings Account (Code 999 ) has a ledger account defined in the setup";
		}
		String branchName = LoanUtilities.getBranchName(branchId);
		
		if (!LoanUtilities.organizationAccountMapped(savingsAccountGLAccountId, branchId)){
			return "Please make sure that the Savings Account GL account is mapped to the employee's Branch ("+branchName+") ";
		}
		
		
		//Check that the accounts for Salary Processing Charge and Excise duty are mapped to employee Branch
		if (!LoanUtilities.organizationAccountMapped(salaryChargeCreditAccountId, branchId))
		{
			return "Please make sure that the Salary Charge Account is mapped to the employee branch ("+branchName+") in the chart of accounts, consult FINANCE";
		}
		
		
		if (!LoanUtilities.organizationAccountMapped(salaryExciseCreditAccountId, branchId))
		{
			return "Please make sure that the Excise Duty Account is mapped to the employee branch ("+branchName+")  in the chart of accounts, consult FINANCE";
		}


		
		//Map<String, String> userLogin = (Map<String, String>) request
		//		.getAttribute("userLogin");
		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty
		doProcessing(userLogin, month, year, employerCode, bdSalaryChargeAmt,
				bdSalaryExciseAmt, salaryProductChargeId,
				salaryProductChargeName, salaryExciseDutyId,
				salaryExciseDutyName, salaryMonthYearId);

		log.info("HHHHHHHHHHHH Salary Processing ... No Deductions !!!");

//		Writer out;
//		try {
//			out = response.getWriter();
//			out.write("");
//			out.flush();
//		} catch (IOException e) {
//			try {
//				throw new EventHandlerException(
//						"Unable to get response writer", e);
//			} catch (EventHandlerException e1) {
//				e1.printStackTrace();
//			}
//		}
		return "success";
	}

	private static void doProcessing(Map<String, String> userLogin,
			String month, String year, String employerCode,
			BigDecimal bdSalaryChargeAmt, BigDecimal bdSalaryExciseAmt,
			Long salaryProductChargeId, String salaryProductChargeName,
			Long salaryExciseDutyId, String salaryExciseDutyName, Long salaryMonthYearId) {
		// Get the payroll numbers from MemberSalary given month, year and
		// employerCode
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);

		BigDecimal bdTotalSalaryPosted = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryExciseDuty = BigDecimal.ZERO;
		BigDecimal bdNetSalaryAmt = BigDecimal.ZERO;
		Long memberAccountId = null;
		String payrollNumber = null;
		
		// Create One AcctgTrans
		GenericValue accountTransaction = null;
		String acctgTransId = AccHolderTransactionServices
				.creatAccountTransRecord(accountTransaction, userLogin);

		String accountTransactionParentId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
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
					"SALARYPROCESSING", accountTransactionParentId, null, acctgTransId);

			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdSalaryChargeAmt, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString(), acctgTransId);
			// Add Excise Duty
			bdTotalSalaryExciseDuty = bdTotalSalaryExciseDuty
					.add(bdSalaryExciseAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdSalaryExciseAmt, memberAccountId, userLogin,
					salaryExciseDutyName, accountTransactionParentId,
					salaryExciseDutyId.toString(), acctgTransId);

			genericValue.set("processed", "Y");
			//listSalaryToUpdate.add(genericValue);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Post Total Net Salary in GL bdTotalSalaryPosted


		// SALARYPROCESSING
		//STATIONACCOUNTPAYMENT
//		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
//				.getAccountHolderTransactionSetup("SALARYPROCESSING");
		
		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
		.getAccountHolderTransactionSetup("STATIONACCOUNTPAYMENT");
		
		String debitAccountIdt = accountHolderTransactionSetup
				.getString("cashAccountId");
		String stationDepositAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		
		//LoanUtilities.get
		String savingsAccountGLAccountId = LoanUtilities.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);

		// salaryProductChargeId = salaryProductChargeId.replaceAll(",", "");
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");

		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		
		Long entrySequence = 1L;
		// ------------------------
		// Debit Leaf Base with the total
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryPosted, acctgTransId, "D", stationDepositAccountId, entrySequence.toString(), branchId);

		BigDecimal bdTotalCharges = bdTotalSalaryCharge
				.add(bdTotalSalaryExciseDuty);
		BigDecimal bdTotalMemberDepositAmt = bdTotalSalaryPosted
				.subtract(bdTotalCharges);

		// Credit Member Deposits with (total net - (total charge + total excise
		// duty))
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalMemberDepositAmt, acctgTransId, "C", savingsAccountGLAccountId, entrySequence.toString(), branchId);
		// Credit Salary Charge with total salary charge
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryCharge, acctgTransId, "C",
				salaryChargeCreditAccountId, entrySequence.toString(), branchId);
		// Credit Excise Duty with total excise duty
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryExciseDuty, acctgTransId, "C",
				salaryExciseCreditAccountId, entrySequence.toString(), branchId);

		// Update the MemberSalary to processed

//		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
//		try {
//			delegator.storeAll(listSalaryToUpdate);
//		} catch (GenericEntityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Prior Transaction when a cheque was paid
		// Debit Cash at Bank
		// Credit Leaf Base

	}

	private static List<GenericValue> getMemberSalaryList(String month,
			String year, String employerCode, Long salaryMonthYearId) {
		List<GenericValue> memberSalaryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

//		EntityConditionList<EntityExpr> memberSalaryConditions = EntityCondition
//				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
//						"month", EntityOperator.EQUALS, month),
//
//				EntityCondition.makeCondition("year", EntityOperator.EQUALS,
//						year),
//
//				EntityCondition.makeCondition("employerCode",
//						EntityOperator.EQUALS, employerCode),
//				// processed
//						EntityCondition.makeCondition("processed",
//								EntityOperator.EQUALS, null)),
//						EntityOperator.AND);
		
		EntityConditionList<EntityExpr> memberSalaryConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"salaryMonthYearId", EntityOperator.EQUALS, salaryMonthYearId),

				
				// processed
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);
		
//		salaryMonthYearId

		try {
			memberSalaryELI = delegator.findList("MemberSalary",
					memberSalaryConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberSalaryELI;
	}

	private static Boolean getMissingPayrollNumbers(String month, String year,
			String employerCode, Long salaryMonthYearId) {
		Boolean missing = false;

		// Get All the Payroll Numbers in the Member Salary given month, year
		// and employerCode
		List<GenericValue> memberSalaryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> memberSalaryConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"salaryMonthYearId", EntityOperator.EQUALS, salaryMonthYearId)
						
//						EntityCondition
//						.makeCondition("year", EntityOperator.EQUALS, year),
//
//				EntityCondition.makeCondition("employerCode",
//						EntityOperator.EQUALS, employerCode)
						
						),
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

	/***
	 * Full deduction
	 * **/
	//processSalaryReceivedDeduct(HttpServletRequest request, HttpServletResponse response)
	public static synchronized String processSalaryReceivedDeduct(Long salaryMonthYearId, Map<String, String> userLogin) {

		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		GenericValue salaryMonthYear = null;
		//salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = salaryMonthYearId;
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);
		
		List<GenericValue> listMemberSalaryItems = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		
		log.info("SSSSSSSSSSSS salaryMonthYearId SSSSSSSS ::: "+salaryMonthYearId);
		
		if ((listMemberSalaryItems == null) || (listMemberSalaryItems.size() < 1)){
			return " No data to process or station already processed !";
		}
		
		//Cheque that the amount available is equal to the total not salary
		BigDecimal bdTotalNetSalaryAmt = getTotalNetSalaryAmount(salaryMonthYearId);
		BigDecimal bdTotalChequeAmountAvailable = RemittanceServices.getTotalRemittedChequeAmountAvailable(employerCode, month, year);

		//Everything to 2 decimal places
		bdTotalNetSalaryAmt = bdTotalNetSalaryAmt.setScale(2, RoundingMode.HALF_DOWN);
		bdTotalChequeAmountAvailable = bdTotalChequeAmountAvailable.setScale(2, RoundingMode.HALF_DOWN);
		
		if (bdTotalNetSalaryAmt.compareTo(bdTotalChequeAmountAvailable) != 0){
			return "The available cheque amount must be equal to the salaries total, Total Salary Amount is "+bdTotalNetSalaryAmt+" while total cheque amounts is "+bdTotalChequeAmountAvailable;
		}
		
		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode, salaryMonthIdLong);
		
		

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "One or more payroll numbers missing in the system , please check the missing payroll numbers menu/link!";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}
		
		//Check that all members have Savings account - code 999
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		Boolean missingSavingsAccount =  false;
		String missingSavingsListing = "";
		//clearMissingMember
		RemittanceServices.clearMissingMember(month, employerCode);
		//RemittanceServices.re
		for (GenericValue memberSalary : listMemberSalary) {
			
			//Check if member has code 999 account
			//AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE
			if (!LoanUtilities.hasAccount(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, memberSalary.getString("payrollNumber"))){
				missingSavingsAccount = true;
				
				if (missingSavingsListing.equals("")){
					missingSavingsListing = memberSalary.getString("payrollNumber");
				} else{
					missingSavingsListing = missingSavingsListing + " , " + memberSalary.getString("payrollNumber");
				}
				
				//Add User to missing accounts
				
				RemittanceServices.addMissingMemberLog(userLogin, memberSalary.getString("payrollNumber"), month, employerCode, AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, null, null);
			}
		}
		
		if (missingSavingsAccount){
			return "There are member accounts missing, please check the Missing Members Members menu in Account Holders transactions . The list has these payrolls ("+missingSavingsListing+")";
		}
		

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = null;
		
		listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((listAccountProductCharge == null) || (listAccountProductCharge.size() < 2)){
			return " Please check that Salary Processing Charge and its excise duty are defined with correct amount / figure for each!";
		}

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
		
		//Check that Salary Charge and Excise duty have account set
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");
		
		if ((salaryChargeCreditAccountId == null) || (salaryChargeCreditAccountId.equals(""))){
			return "Please ensure that the Salary Processing charge has a gl account set !! Check Product Charge list in loans if charge account is specified";
		}

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");
		
		if ((salaryExciseCreditAccountId == null) || (salaryExciseCreditAccountId.equals(""))){
			return "Please ensure that the Excise charge has a GL Account set !! Check Product Charge list in loans if charge account is specified";
		}
		
		//Employee Must have a branch
		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		if ((branchId == null) || (branchId.equals("")))
			return "The employee logged into the system must have a branch, please check with HR!!";

		
		String savingsAccountGLAccountId = LoanUtilities.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((savingsAccountGLAccountId == null) || (savingsAccountGLAccountId.equals(""))){
			return "Please ensure that the Savings Account (Code 999 ) has a ledger account defined in the setup";
		}
		String branchName = LoanUtilities.getBranchName(branchId);
		
		if (!LoanUtilities.organizationAccountMapped(savingsAccountGLAccountId, branchId)){
			return "Please make sure that the Savings Account GL account is mapped to the employee's Branch ("+branchName+") ";
		}
		
		
		//Check that the accounts for Salary Processing Charge and Excise duty are mapped to employee Branch
		if (!LoanUtilities.organizationAccountMapped(salaryChargeCreditAccountId, branchId))
		{
			return "Please make sure that the Salary Charge Account is mapped to the employee branch ("+branchName+") in the chart of accounts, consult FINANCE";
		}
		
		
		if (!LoanUtilities.organizationAccountMapped(salaryExciseCreditAccountId, branchId))
		{
			return "Please make sure that the Excise Duty Account is mapped to the employee branch ("+branchName+")  in the chart of accounts, consult FINANCE";
		}

		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty
		doProcessingWithDeductions(userLogin, month, year, employerCode,
				bdSalaryChargeAmt, bdSalaryExciseAmt, salaryProductChargeId,
				salaryProductChargeName, salaryExciseDutyId,
				salaryExciseDutyName, salaryMonthYearId);

		
		return "success";
	}

	private static void doProcessingWithDeductions(
			Map<String, String> userLogin, String month, String year,
			String employerCode, BigDecimal bdSalaryChargeAmt,
			BigDecimal bdSalaryExciseAmt, Long salaryProductChargeId,
			String salaryProductChargeName, Long salaryExciseDutyId,
			String salaryExciseDutyName, Long salaryMonthYearId) {
		// Get the payroll numbers from MemberSalary given month, year and
		// employerCode
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);

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
					bdSalaryChargeAmt, memberAccountId, userLogin,
					salaryProductChargeName, accountTransactionParentId,
					salaryProductChargeId.toString());
			
			//####### Deduct the Excise Duty
			// Deduct the Salary Charge
			// Add Salary Charge
			bdTotalSalaryExciseDuty = bdTotalSalaryExciseDuty.add(bdSalaryExciseAmt);
			AccHolderTransactionServices.memberTransactionDeposit(
					bdSalaryExciseAmt, memberAccountId, userLogin,
					salaryExciseDutyName, accountTransactionParentId,
					salaryExciseDutyId.toString());

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
								//"transactionType", "LOANREPAYMENT",
								"loanApplicationId", loanApplicationId,
								"partyId", member.getLong("partyId"),
								
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
				
//				try {
//					delegator.createOrStore(loanRepayment);
//				} catch (GenericEntityException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
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
			//listSalaryToUpdate.add(genericValue);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		Long entrySequence = 1L;
		// ------------------------
		// Debit Leaf Base with the total
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryPosted, acctgTransId, "D", debitAccountId, entrySequence.toString(), branchId);

		BigDecimal bdTotalCharges = bdTotalSalaryCharge
				.add(bdTotalSalaryExciseDuty);
		BigDecimal bdTotalMemberDepositAmt = bdTotalSalaryPosted
				.subtract(bdTotalCharges);

		// Credit Member Deposits with (total net - (total charge + total excise
		// duty))
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalMemberDepositAmt, acctgTransId, "C", creditAccountId, entrySequence.toString(), branchId);
		// Credit Salary Charge with total salary charge
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryCharge, acctgTransId, "C",
				salaryChargeCreditAccountId, entrySequence.toString(), branchId);
		// Credit Excise Duty with total excise duty
		entrySequence = entrySequence + 1;
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalSalaryExciseDuty, acctgTransId, "C",
				salaryExciseCreditAccountId, entrySequence.toString(), branchId);
		
		//Post the Loan Repayments
		for (GenericValue genericValue : listLoanRepayments) {
			//entrySequence = entrySequence + 1;
			LoanRepayments.repayLoanWithoutDebitingCash(genericValue, userLogin, entrySequence);
		}
		
		//listLoanRepayments.add(loanRepayment);

		// Update the MemberSalary to processed

//		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
//		try {
//			delegator.storeAll(listSalaryToUpdate);
//		} catch (GenericEntityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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
	
	
	/****
	 * Take the path to csv and salaryMonthYearId and do the processing of the csv
	 * */
	public static void processCSV(String csvPath, String salaryMonthYearIdStr){
		
		log.info(" GGGGGGGGGGGGGGGGGGG ");
		log.info(" CSV Path (absolute is ) :::  "+csvPath);
		log.info(" Salary Month Year ID is :::  ) "+salaryMonthYearIdStr);
		
		/***
		 * Create MemberSalary 
		 * 
		 * 
		 * 		<field name="memberSalaryId" type="id-vlong-int"></field>
				<field name="salaryMonthYearId" type="id-vlong-int"></field>
				<field name="isActive" type="indicator"></field>
				<field name="createdBy" type="id"></field>
				<field name="month" type="id"></field>
				<field name="year" type="id"></field>
				<field name="employerCode" type="id"></field>
				<field name="payrollNumber" type="id"></field>
				<field name="netSalary" type="fixed-point"></field>
				<field name="processed" type="indicator"></field>
		 * 
		 * */
		
		//String month = "";
		//String year = "";
		//String employerCode = "";
		Long salaryMonthYearIdLong = Long.valueOf(salaryMonthYearIdStr);
		// Need month, year and employerCode

		// Find the SalaryMonthYear
		GenericValue salaryMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			salaryMonthYear = delegator.findOne("SalaryMonthYear",
					UtilMisc.toMap("salaryMonthYearId", salaryMonthYearIdLong),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long month = salaryMonthYear.getLong("month");
		Long year = salaryMonthYear.getLong("year");

		GenericValue station = LoanUtilities.getStation(salaryMonthYear
				.getString("stationId"));
		String employerCode = station.getString("employerCode");
		
		
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		Long memberSalaryId;
		GenericValue memberSalary;
		
		List<GenericValue> listMemberSalary = new ArrayList<GenericValue>();
		
		//Add the records to Member Salaries
		int count = 0;
		BigDecimal netSalary = BigDecimal.ZERO;
		try {
			br = new BufferedReader(new FileReader(csvPath));
			
			while ((line = br.readLine()) != null){
				String[] salary = line.split(csvSplitBy);
				count++;
				
				System.out.println(" Count "+count+" Payroll No "+salary[0]+" Net Pay "+salary[1]);
				netSalary = new BigDecimal(salary[1]);
				memberSalaryId = delegator.getNextSeqIdLong("MemberSalary");
				memberSalary =  delegator.makeValue(
						"MemberSalary", UtilMisc.toMap(
								"memberSalaryId", memberSalaryId,
								"salaryMonthYearId", salaryMonthYearIdLong,
								"isActive", "Y",
								"createdBy", "admin",
								//"transactionType", "LOANREPAYMENT",
								"month", month.toString(),
								"year", year.toString(),
								
								"employerCode",
								employerCode,
								
								"payrollNumber", salary[0].trim(),
								
								"netSalary", netSalary,
								
								"processed", "N"
								));
				
				listMemberSalary.add(memberSalary);
			}
			
			try {
				delegator.storeAll(listMemberSalary);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	
	}

	private static GenericValue getSalaryMonthYear(Long salaryMonthYearIdLong) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * @author Japheth Odonya  @when May 8, 2015 3:50:07 PM
	 * 
	 * processSalaryReceivedLoanDeductionOnly
	 * 
	 * */
	//(HttpServletRequest request, HttpServletResponse response)
	public static synchronized String processSalaryReceivedLoanDeductionOnly
	(Long salaryMonthYearId, Map<String, String> userLogin) {

		// salaryMonthYearId
		//String salaryMonthYearId = (String) request
		//		.getParameter("salaryMonthYearId");

		/***
		 * Get month year and employerCode from SalaryMonthYear where
		 * salaryMonthYearId is the given value
		 * */
		GenericValue salaryMonthYear = null;
		//salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = salaryMonthYearId;
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);
		
		List<GenericValue> listMemberSalaryItems = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		
		log.info("SSSSSSSSSSSS salaryMonthYearId SSSSSSSS ::: "+salaryMonthYearId);
		
		if ((listMemberSalaryItems == null) || (listMemberSalaryItems.size() < 1)){
			return " No data to process or station already processed !";
		}
		
		//Cheque that the amount available is equal to the total not salary
		BigDecimal bdTotalNetSalaryAmt = getTotalNetSalaryAmount(salaryMonthYearId);
		BigDecimal bdTotalChequeAmountAvailable = RemittanceServices.getTotalRemittedChequeAmountAvailable(employerCode, month, year);

		//Everything to 2 decimal places
		bdTotalNetSalaryAmt = bdTotalNetSalaryAmt.setScale(2, RoundingMode.HALF_DOWN);
		bdTotalChequeAmountAvailable = bdTotalChequeAmountAvailable.setScale(2, RoundingMode.HALF_DOWN);
		
		if (bdTotalNetSalaryAmt.compareTo(bdTotalChequeAmountAvailable) != 0){
			return "The available cheque amount must be equal to the salaries total, Total Salary Amount is "+bdTotalNetSalaryAmt+" while total cheque amounts is "+bdTotalChequeAmountAvailable;
		}
		
		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode, salaryMonthIdLong);
		
		

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "One or more payroll numbers missing in the system , please check the missing payroll numbers menu/link!";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}
		
		//Check that all members have Savings account - code 999
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		Boolean missingSavingsAccount =  false;
		String missingSavingsListing = "";
		//clearMissingMember
		RemittanceServices.clearMissingMember(month, employerCode);
		//RemittanceServices.re
		for (GenericValue memberSalary : listMemberSalary) {
			
			//Check if member has code 999 account
			//AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE
			if (!LoanUtilities.hasAccount(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, memberSalary.getString("payrollNumber"))){
				missingSavingsAccount = true;
				
				if (missingSavingsListing.equals("")){
					missingSavingsListing = memberSalary.getString("payrollNumber");
				} else{
					missingSavingsListing = missingSavingsListing + " , " + memberSalary.getString("payrollNumber");
				}
				
				//Add User to missing accounts
				
				RemittanceServices.addMissingMemberLog(userLogin, memberSalary.getString("payrollNumber"), month, employerCode, AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, null, null);
			}
		}
		
		if (missingSavingsAccount){
			return "There are member accounts missing, please check the Missing Members Members menu in Account Holders transactions . The list has these payrolls ("+missingSavingsListing+")";
		}
		

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = null;
		
		listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((listAccountProductCharge == null) || (listAccountProductCharge.size() < 2)){
			return " Please check that Salary Processing Charge and its excise duty are defined with correct amount / figure for each!";
		}

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
		
		//Check that Salary Charge and Excise duty have account set
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");
		
		if ((salaryChargeCreditAccountId == null) || (salaryChargeCreditAccountId.equals(""))){
			return "Please ensure that the Salary Processing charge has a gl account set !! Check Product Charge list in loans if charge account is specified";
		}

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");
		
		if ((salaryExciseCreditAccountId == null) || (salaryExciseCreditAccountId.equals(""))){
			return "Please ensure that the Excise charge has a GL Account set !! Check Product Charge list in loans if charge account is specified";
		}
		
		//Employee Must have a branch
		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		if ((branchId == null) || (branchId.equals("")))
			return "The employee logged into the system must have a branch, please check with HR!!";

		
		String savingsAccountGLAccountId = LoanUtilities.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((savingsAccountGLAccountId == null) || (savingsAccountGLAccountId.equals(""))){
			return "Please ensure that the Savings Account (Code 999 ) has a ledger account defined in the setup";
		}
		String branchName = LoanUtilities.getBranchName(branchId);
		
		if (!LoanUtilities.organizationAccountMapped(savingsAccountGLAccountId, branchId)){
			return "Please make sure that the Savings Account GL account is mapped to the employee's Branch ("+branchName+") ";
		}
		
		
		//Check that the accounts for Salary Processing Charge and Excise duty are mapped to employee Branch
		if (!LoanUtilities.organizationAccountMapped(salaryChargeCreditAccountId, branchId))
		{
			return "Please make sure that the Salary Charge Account is mapped to the employee branch ("+branchName+") in the chart of accounts, consult FINANCE";
		}
		
		
		if (!LoanUtilities.organizationAccountMapped(salaryExciseCreditAccountId, branchId))
		{
			return "Please make sure that the Excise Duty Account is mapped to the employee branch ("+branchName+")  in the chart of accounts, consult FINANCE";
		}
		
		//TODO
		return "This will be implemented as part of Remittance (C7) processing !";
	}
	
	/**
	 * @author Japheth Odonya  @when May 8, 2015 3:52:59 PM
	 * 
	 * processSalaryReceivedAccountContributionOnly
	 * 
	 * */
	//HttpServletRequest request, HttpServletResponse response
	public static synchronized String processSalaryReceivedAccountContributionOnly(Long salaryMonthYearId, Map<String, String> userLogin ) {

		GenericValue salaryMonthYear = null;
		//salaryMonthYearId = salaryMonthYearId.replaceAll(",", "");
		Long salaryMonthIdLong = salaryMonthYearId;
		salaryMonthYear = LoanUtilities.getSalaryMonthYear(salaryMonthIdLong);

		String month = String.valueOf(salaryMonthYear.getLong("month"));
		String year = String.valueOf(salaryMonthYear.getLong("year"));
		String stationId = salaryMonthYear.getString("stationId");
		stationId = stationId.replaceAll(",", "");
		String employerCode = LoanUtilities.getStationEmployerCode(stationId);
		
		List<GenericValue> listMemberSalaryItems = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		
		log.info("SSSSSSSSSSSS salaryMonthYearId SSSSSSSS ::: "+salaryMonthYearId);
		
		if ((listMemberSalaryItems == null) || (listMemberSalaryItems.size() < 1)){
			return " No data to process or station already processed !";
		}
		
		//Cheque that the amount available is equal to the total not salary
		BigDecimal bdTotalNetSalaryAmt = getTotalNetSalaryAmount(salaryMonthYearId);
		BigDecimal bdTotalChequeAmountAvailable = RemittanceServices.getTotalRemittedChequeAmountAvailable(employerCode, month, year);

		//Everything to 2 decimal places
		bdTotalNetSalaryAmt = bdTotalNetSalaryAmt.setScale(2, RoundingMode.HALF_DOWN);
		bdTotalChequeAmountAvailable = bdTotalChequeAmountAvailable.setScale(2, RoundingMode.HALF_DOWN);
		
		if (bdTotalNetSalaryAmt.compareTo(bdTotalChequeAmountAvailable) != 0){
			return "The available cheque amount must be equal to the salaries total, Total Salary Amount is "+bdTotalNetSalaryAmt+" while total cheque amounts is "+bdTotalChequeAmountAvailable;
		}
		
		// Remove Current Logs first
		removeMissingPayrollNumbersLog(month, year, employerCode);
		log.info("NOOOOOO DEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
				+ year + " Employer Code  " + employerCode);

		// List<GenericValue> MemberSalaryELI = null;

		Boolean missingPayrollNumbers = getMissingPayrollNumbers(month, year,
				employerCode, salaryMonthIdLong);
		
		

		if (missingPayrollNumbers) {

			log.info("MMMMMMMMMMMMMMM Missing Payroll Numbe, will exit LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);
			return "One or more payroll numbers missing in the system , please check the missing payroll numbers menu/link!";
		} else {
			log.info("EEEEEEEEEEEEEE Available Payroll Numbers, will continue LLLLLLLLLLLLLLLL Month "
					+ month
					+ " Year "
					+ year
					+ " Employer Code  "
					+ employerCode);

		}
		
		//Check that all members have Savings account - code 999
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year,
				employerCode, salaryMonthYearId);
		Boolean missingSavingsAccount =  false;
		String missingSavingsListing = "";
		//clearMissingMember
		RemittanceServices.clearMissingMember(month, employerCode);
		//RemittanceServices.re
		for (GenericValue memberSalary : listMemberSalary) {
			
			//Check if member has code 999 account
			//AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE
			if (!LoanUtilities.hasAccount(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, memberSalary.getString("payrollNumber"))){
				missingSavingsAccount = true;
				
				if (missingSavingsListing.equals("")){
					missingSavingsListing = memberSalary.getString("payrollNumber");
				} else{
					missingSavingsListing = missingSavingsListing + " , " + memberSalary.getString("payrollNumber");
				}
				
				//Add User to missing accounts
				
				RemittanceServices.addMissingMemberLog(userLogin, memberSalary.getString("payrollNumber"), month, employerCode, AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, null, null);
			}
		}
		
		if (missingSavingsAccount){
			return "There are member accounts missing, please check the Missing Members Members menu in Account Holders transactions . The list has these payrolls ("+missingSavingsListing+")";
		}
		

		// Continue Processing for Salary Without Deductions

		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty

		// Get all the charges for transactiontype SALARYPROCESSING
		List<GenericValue> listAccountProductCharge = null;
		
		listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((listAccountProductCharge == null) || (listAccountProductCharge.size() < 2)){
			return " Please check that Salary Processing Charge and its excise duty are defined with correct amount / figure for each!";
		}

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
		
		//Check that Salary Charge and Excise duty have account set
		GenericValue salaryProductCharge = LoanUtilities
				.getProductCharge(salaryProductChargeId);
		String salaryChargeCreditAccountId = salaryProductCharge
				.getString("chargeAccountId");
		
		if ((salaryChargeCreditAccountId == null) || (salaryChargeCreditAccountId.equals(""))){
			return "Please ensure that the Salary Processing charge has a gl account set !! Check Product Charge list in loans if charge account is specified";
		}

		GenericValue salaryExciseDutyProductCharge = LoanUtilities
				.getProductCharge(salaryExciseDutyId);
		String salaryExciseCreditAccountId = salaryExciseDutyProductCharge
				.getString("chargeAccountId");
		
		if ((salaryExciseCreditAccountId == null) || (salaryExciseCreditAccountId.equals(""))){
			return "Please ensure that the Excise charge has a GL Account set !! Check Product Charge list in loans if charge account is specified";
		}
		
		//Employee Must have a branch
		String branchId = AccHolderTransactionServices.getEmployeeBranch((String)userLogin.get("partyId"));
		if ((branchId == null) || (branchId.equals("")))
			return "The employee logged into the system must have a branch, please check with HR!!";

		
		String savingsAccountGLAccountId = LoanUtilities.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
		
		if ((savingsAccountGLAccountId == null) || (savingsAccountGLAccountId.equals(""))){
			return "Please ensure that the Savings Account (Code 999 ) has a ledger account defined in the setup";
		}
		String branchName = LoanUtilities.getBranchName(branchId);
		
		if (!LoanUtilities.organizationAccountMapped(savingsAccountGLAccountId, branchId)){
			return "Please make sure that the Savings Account GL account is mapped to the employee's Branch ("+branchName+") ";
		}
		
		
		//Check that the accounts for Salary Processing Charge and Excise duty are mapped to employee Branch
		if (!LoanUtilities.organizationAccountMapped(salaryChargeCreditAccountId, branchId))
		{
			return "Please make sure that the Salary Charge Account is mapped to the employee branch ("+branchName+") in the chart of accounts, consult FINANCE";
		}
		
		
		if (!LoanUtilities.organizationAccountMapped(salaryExciseCreditAccountId, branchId))
		{
			return "Please make sure that the Excise Duty Account is mapped to the employee branch ("+branchName+")  in the chart of accounts, consult FINANCE";
		}
		
		//TODO
		return "This will be implemented as part of Remittance (C7) processing !";
	}

}
