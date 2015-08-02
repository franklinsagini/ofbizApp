package org.ofbiz.normalremittanceprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/*****
 * @author Japheth Odonya @when Jun 21, 2015 8:08:43 PM
 * */
public class NormalRemittanceProcessingServices {

	public static Logger log = Logger
			.getLogger(NormalRemittanceProcessingServices.class);

	public static void processCSV(String csvPath,
			String normalRemittanceMonthYearId) {

		log.info(" GGGGGGGGGGGGGGGGGGG ");
		log.info(" CSV Path (absolute is ) :::  " + csvPath);
		log.info(" normalRemittanceMonthYearId is :::  ) "
				+ normalRemittanceMonthYearId);

		/***
		 * Create MemberRemittance
		 * 
		 * 
		 * <field name="memberSalaryId" type="id-vlong-int"></field> <field
		 * name="salaryMonthYearId" type="id-vlong-int"></field> <field
		 * name="isActive" type="indicator"></field> <field name="createdBy"
		 * type="id"></field> <field name="month" type="id"></field> <field
		 * name="year" type="id"></field> <field name="employerCode"
		 * type="id"></field> <field name="payrollNumber" type="id"></field>
		 * <field name="netSalary" type="fixed-point"></field> <field
		 * name="processed" type="indicator"></field>
		 * 
		 * */

		// String month = "";
		// String year = "";
		// String employerCode = "";
		Long normalRemittanceMonthYearIdLong = Long
				.valueOf(normalRemittanceMonthYearId);
		// Need month, year and employerCode

		// Find the SalaryMonthYear
		GenericValue normalRemittanceMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			normalRemittanceMonthYear = delegator.findOne(
					"NormalRemittanceMonthYear", UtilMisc.toMap(
							"normalRemittanceMonthYearId",
							normalRemittanceMonthYearIdLong), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		Long month = normalRemittanceMonthYear.getLong("month");
		Long year = normalRemittanceMonthYear.getLong("year");

		GenericValue station = LoanUtilities
				.getStation(normalRemittanceMonthYear.getString("stationId"));
		String employerCode = station.getString("employerCode");

		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";

		Long memberRemittanceId;
		GenericValue memberRemittance;

		List<GenericValue> listMemberRemittance = new ArrayList<GenericValue>();

		// Add the records to Member Salaries
		int count = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;
		try {
			br = new BufferedReader(new FileReader(csvPath));
			// HCS002 0,James Ndungu 1,2000 2,1500 3,7000 4,1200 5,1000 6,3000
			// 7,120000 8

			/***
			 * 
			 * 0 payrollNumber 1 memberNames 2 memberDepositsAmount 3
			 * shareCapitalAmount 4 loansInterestInsuranceAmount 5
			 * fosaContributionsAmount 6 juniorAmount 7 holidayAmount 8
			 * totalAmount
			 * 
			 * */

			while ((line = br.readLine()) != null) {

				String[] remittance = line.split(csvSplitBy);
				count++;

				System.out.println(" Count " + count + " Payroll No "
						+ remittance[0] + " Name " + remittance[1]);
				// totalAmount = new BigDecimal(remittance[1]);
				memberRemittanceId = delegator
						.getNextSeqIdLong("MemberRemittance");
				memberRemittance = delegator.makeValue("MemberRemittance",
						UtilMisc.toMap(
								"memberRemittanceId",
								memberRemittanceId,
								"normalRemittanceMonthYearId",
								normalRemittanceMonthYearIdLong,
								"isActive",
								"Y",
								"createdBy",
								"admin",
								// "transactionType", "LOANREPAYMENT",
								"month", month.toString(), "year", year
										.toString(),

								"employerCode", employerCode,

								"payrollNumber", remittance[0].trim(),
								"memberNames", remittance[1].trim(),
								"memberDepositsAmount", new BigDecimal(
										remittance[2].trim()),

								"shareCapitalAmount", new BigDecimal(
										remittance[3].trim()),
								"loansInterestInsuranceAmount", new BigDecimal(
										remittance[4].trim()),
								"fosaContributionsAmount", new BigDecimal(
										remittance[5].trim()), "juniorAmount",
								new BigDecimal(remittance[6].trim()),
								"holidayAmount",
								new BigDecimal(remittance[7].trim()),

								"totalAmount",
								new BigDecimal(remittance[8].trim()),

								"processed", "N"));

				listMemberRemittance.add(memberRemittance);
			}

			try {
				delegator.storeAll(listMemberRemittance);
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
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static BigDecimal getTotalNormalRemittanceAmount(
			Long normalRemittanceMonthYearId) {
		BigDecimal bdTotalAmount = BigDecimal.ZERO;

		// Need month, year and employerCode
		Long month = null;
		Long year = null;
		String employerCode = "";

		// Find the SalaryMonthYear
		GenericValue normalRemittanceMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			normalRemittanceMonthYear = delegator.findOne(
					"NormalRemittanceMonthYear", UtilMisc.toMap(
							"normalRemittanceMonthYearId",
							normalRemittanceMonthYearId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		month = normalRemittanceMonthYear.getLong("month");
		year = normalRemittanceMonthYear.getLong("year");

		GenericValue station = LoanUtilities
				.getStation(normalRemittanceMonthYear.getString("stationId"));
		employerCode = station.getString("employerCode");

		// Get total amount
		List<GenericValue> stationRemittanceSumsELI = new ArrayList<GenericValue>();
		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> stationRemittanceSumsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month.toString()),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year.toString()),

						EntityCondition.makeCondition("employerCode",
								EntityOperator.EQUALS, employerCode),

						EntityCondition.makeCondition(
								"normalRemittanceMonthYearId",
								EntityOperator.EQUALS,
								normalRemittanceMonthYearId)),

				EntityOperator.AND);
		try {
			stationRemittanceSumsELI = delegator.findList("MemberRemittance",
					stationRemittanceSumsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : stationRemittanceSumsELI) {
			bdTotalAmount = bdTotalAmount.add(genericValue
					.getBigDecimal("totalAmount"));
		}

		return bdTotalAmount;
	}

	/***
	 * @author Japheth Odonya
	 * */
	public synchronized static  String processNormalRemittanceReceived(
			Long normalRemittanceMonthYearId, Map<String, String> userLogin) {

		// payrollNumber
		// memberNames
		// totalAmount
		


		GenericValue normalRemittanceMonthYear = LoanUtilities.getEntityValue(
				"NormalRemittanceMonthYear", "normalRemittanceMonthYearId",
				normalRemittanceMonthYearId);

		BigDecimal bdNormalRemittanceAmount = getTotalNormalRemittanceAmount(normalRemittanceMonthYearId);
		bdNormalRemittanceAmount = bdNormalRemittanceAmount.setScale(2,
				RoundingMode.HALF_UP);

		Long month = normalRemittanceMonthYear.getLong("month");
		Long year = normalRemittanceMonthYear.getLong("year");
		
		

		GenericValue station = LoanUtilities
				.getStation(normalRemittanceMonthYear.getString("stationId"));
		String employerCode = station.getString("employerCode");
		
		List<GenericValue> listMemberRemittanceList = getMemberRemittance(month.toString(), year.toString(), employerCode, normalRemittanceMonthYearId);
		
		if ((listMemberRemittanceList == null) || (listMemberRemittanceList.size() < 1))
		{
			return "No records to process, station may have been processed already !";
		}
		
		
		//Check for missing accounts
		
		Boolean failed = false;
		// Clear the missing log - delete everything from it
		//clearMissingMember(month, employerCode);
		String payrollNo = "";
		Long count = 0L;
		List<GenericValue> listMissingMemberLogELI = new ArrayList<GenericValue>();
		List<GenericValue> receivedPayrollELI = getMemberRemittance(month.toString(), year.toString(), employerCode, normalRemittanceMonthYearId);
		
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNumber");
			log.info(++count
					+ "FFFFFFFFFFFF Checking Member Deposit!!!!!!!!!!!!!! for "
					+ payrollNo);
			if ((!hasAccount(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE, payrollNo.trim())) && (genericValue.getBigDecimal("memberDepositsAmount").compareTo(BigDecimal.ZERO) > 0)) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month.toString(), employerCode,
						AccHolderTransactionServices.MEMBER_DEPOSIT_CODE, null, null);
				return payrollNo+"("+genericValue.getString("memberNames")+") Missing Member Deposit Account !";
			}

		}
		
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNumber");
			log.info(++count
					+ "FFFFFFFFFFFF Checking Share Capital!!!!!!!!!!!!!! for "
					+ payrollNo);
			if ((!hasAccount(AccHolderTransactionServices.SHARE_CAPITAL_CODE, payrollNo.trim())) && (genericValue.getBigDecimal("shareCapitalAmount").compareTo(BigDecimal.ZERO) > 0)) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month.toString(), employerCode,
						AccHolderTransactionServices.SHARE_CAPITAL_CODE, null, null);
				return payrollNo+"("+genericValue.getString("memberNames")+") Missing Share capital Account !";
			}

		}
		
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNumber");
			log.info(++count
					+ "FFFFFFFFFFFF Checking Fosa Savings!!!!!!!!!!!!!! for "
					+ payrollNo);
			if ((!hasAccount(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, payrollNo.trim())) && (genericValue.getBigDecimal("fosaContributionsAmount").compareTo(BigDecimal.ZERO) > 0)) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month.toString(), employerCode,
						AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, null, null);
				return payrollNo+"("+genericValue.getString("memberNames")+") Missing Fosa savings Account !";
			}

		}
		
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNumber");
			log.info(++count
					+ "FFFFFFFFFFFF Checking Junior Savings!!!!!!!!!!!!!! for "
					+ payrollNo);
			if ((!hasAccount(AccHolderTransactionServices.JUNIOR_ACCOUNT_CODE, payrollNo.trim())) && (genericValue.getBigDecimal("juniorAmount").compareTo(BigDecimal.ZERO) > 0)) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month.toString(), employerCode,
						AccHolderTransactionServices.JUNIOR_ACCOUNT_CODE, null, null);
				return payrollNo+"("+genericValue.getString("memberNames")+") Missing Junior Account !";
			}

		}
		
		
		for (GenericValue genericValue : receivedPayrollELI) {
			payrollNo = genericValue.getString("payrollNumber");
			log.info(++count
					+ "FFFFFFFFFFFF Checking Holiday Savings!!!!!!!!!!!!!! for "
					+ payrollNo);
			if ((!hasAccount(AccHolderTransactionServices.HOLIDAY_ACCOUNT_CODE, payrollNo.trim())) && (genericValue.getBigDecimal("holidayAmount").compareTo(BigDecimal.ZERO) > 0)) {
				failed = true;

				// Add the member to the missing log
				log.info("AAAAAAAAAAAAAAAA Adding a member!!!!!!!!!!!!!! for "
						+ payrollNo);
				RemittanceServices.addMissingMemberLog(userLogin, payrollNo, month.toString(), employerCode,
						AccHolderTransactionServices.HOLIDAY_ACCOUNT_CODE, null, null);
				return payrollNo+"("+genericValue.getString("memberNames")+") Missing Holiday Account !";
			}

		}


		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		if (failed) {
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				delegator.storeAll(listMissingMemberLogELI);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				TransactionUtil.begin();
			} catch (GenericTransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "failed";
		}
		
		//End checking
		

		BigDecimal bdTotalChequeAmount = RemittanceServices
				.getTotalRemittedChequeAmountAvailable(employerCode,
						month.toString(), year.toString());
		bdTotalChequeAmount = bdTotalChequeAmount.setScale(2,
				RoundingMode.HALF_UP);

		if (bdNormalRemittanceAmount.compareTo(bdTotalChequeAmount) != 0) {
			return " Total Remittance must be equal to the cheque received !";
		}
		
		String branchId = AccHolderTransactionServices
				.getEmployeeBranch((String) userLogin.get("partyId"));

		GenericValue accountTransaction = null;
		String acctgTransId = AccHolderTransactionServices
				.creatAccountTransRecord(accountTransaction, userLogin);
		// **Process memberDepositsAmount

		Long entrySequence = 0L;
		// CR Member Deposits
		BigDecimal bdMemberDepositsAmount = BigDecimal.ZERO;
		bdMemberDepositsAmount = getTotalAmounts(normalRemittanceMonthYearId,
				"memberDepositsAmount");
		processMemberAccountDeposit(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE, normalRemittanceMonthYearId, userLogin, month.toString(),
				year.toString(), employerCode.toString(), "memberDepositsAmount",  acctgTransId);
		
		entrySequence = entrySequence + 1;
		String memberDepositAccountId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.MEMBER_DEPOSIT_CODE).getString("glAccountId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdMemberDepositsAmount, acctgTransId, "C",
				memberDepositAccountId, entrySequence.toString(), branchId);

		// **Process shareCapitalAmount
		// CR Share Capital
		BigDecimal bdShareCapitalAmount = BigDecimal.ZERO;
		bdShareCapitalAmount = getTotalAmounts(normalRemittanceMonthYearId,
				"shareCapitalAmount");
		processMemberAccountDeposit(AccHolderTransactionServices.SHARE_CAPITAL_CODE, normalRemittanceMonthYearId, userLogin, month.toString(),
				year.toString(), employerCode.toString(), "shareCapitalAmount",  acctgTransId);


		entrySequence = entrySequence + 1;
		String shareCapitalAccountId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SHARE_CAPITAL_CODE).getString("glAccountId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdShareCapitalAmount, acctgTransId, "C",
				shareCapitalAccountId, entrySequence.toString(), branchId);

		// **Process loansInterestInsuranceAmount
		// Cr Member Loans
		// Cr Interest Receivable
		// Cr Insurance Receivable
		 
		LoanBalanceItem loanBalanceItem = doProcessingLoanOnlyDeductions(userLogin, month.toString(), year.toString(), employerCode, normalRemittanceMonthYearId, acctgTransId, entrySequence);
		BigDecimal bdTotalPrincipalAmount = loanBalanceItem.getPrincipalAmount();
		BigDecimal bdTotalInterestAmount = loanBalanceItem.getInterestAmount();
		BigDecimal bdTotalInsuranceAmount = loanBalanceItem.getInsuranceAmount();
		
		entrySequence = Long.valueOf(loanBalanceItem.getSequence());

		// **Process fosaContributionsAmount
		// CR Fosa Savings
		BigDecimal bdFosaContributionsAmount = BigDecimal.ZERO;
		bdFosaContributionsAmount = getTotalAmounts(
				normalRemittanceMonthYearId, "fosaContributionsAmount");
		processMemberAccountDeposit(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE, normalRemittanceMonthYearId, userLogin, month.toString(),
				year.toString(), employerCode.toString(), "fosaContributionsAmount",  acctgTransId);

		bdFosaContributionsAmount = bdFosaContributionsAmount.add(loanBalanceItem.getFosaSavingAmount());
		entrySequence = entrySequence + 1;
		String fosaAccountId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE).getString("glAccountId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdFosaContributionsAmount, acctgTransId, "C",
				fosaAccountId, entrySequence.toString(), branchId);

		
		// **Process juniorAmount
		// Cr Junior
		BigDecimal bdJuniorAmount = BigDecimal.ZERO;
		bdJuniorAmount = getTotalAmounts(normalRemittanceMonthYearId,
				"juniorAmount");
		processMemberAccountDeposit(AccHolderTransactionServices.JUNIOR_ACCOUNT_CODE, normalRemittanceMonthYearId, userLogin, month.toString(),
				year.toString(), employerCode.toString(), "juniorAmount",  acctgTransId);
		
		entrySequence = entrySequence + 1;
		String juniorAccountId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.JUNIOR_ACCOUNT_CODE).getString("glAccountId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdJuniorAmount, acctgTransId, "C",
				juniorAccountId, entrySequence.toString(), branchId);

		// **Process holidayAmount
		// Cr Holiday
		BigDecimal bdHolidayAmount = BigDecimal.ZERO;
		bdHolidayAmount = getTotalAmounts(normalRemittanceMonthYearId,
				"holidayAmount");
		processMemberAccountDeposit(AccHolderTransactionServices.HOLIDAY_ACCOUNT_CODE, normalRemittanceMonthYearId, userLogin, month.toString(),
				year.toString(), employerCode.toString(), "holidayAmount",  acctgTransId);
		
		entrySequence = entrySequence + 1;
		String holidayAccountId = LoanUtilities.getAccountProductGivenCodeId(AccHolderTransactionServices.JUNIOR_ACCOUNT_CODE).getString("glAccountId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdHolidayAmount, acctgTransId, "C",
				holidayAccountId, entrySequence.toString(), branchId);

		// Dr total to factory Contol/ KTDA
		BigDecimal bdTotalAmount = BigDecimal.ZERO;
		bdTotalAmount = getTotalNormalRemittanceAmount(normalRemittanceMonthYearId);

		// Debit Leaf Base with the total
		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("STATIONACCOUNTPAYMENT");

		String stationDepositAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalAmount, acctgTransId, "D",
				stationDepositAccountId, entrySequence.toString(), branchId);
		
		
		//Update all records to processed
		List<GenericValue> remittanceListForUpdate = getMemberRemittance(month.toString(), year.toString(), employerCode, normalRemittanceMonthYearId);
		//Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		for (GenericValue genericValue : remittanceListForUpdate) {
			
			//set processed yes and save
			//EntityCondition.makeCondition("processed",
			//EntityOperator.EQUALS, "N")),
			genericValue.set("processed", "Y");
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return "success";
	}

	private static void processMemberAccountDeposit(String accountProductCode,
			Long normalRemittanceMonthYearId, Map<String, String> userLogin, String month,
			String year, String employerCode, String fieldName, String acctgTransId) {
		
		//For each member remittance if amount is greater than zero deposit
		List<GenericValue> listMemberRemittance = getMemberRemittance(month,
				year, employerCode, normalRemittanceMonthYearId);
		Long memberAccountId = null;
		
		for (GenericValue genericValue : listMemberRemittance) {
			
			if (genericValue.getBigDecimal(fieldName).compareTo(BigDecimal.ZERO) > 0){
			memberAccountId = LoanUtilities.getAccountProductMemberAccountId(genericValue.getString("payrollNumber"), accountProductCode);
			AccHolderTransactionServices.memberTransactionDeposit(
					genericValue.getBigDecimal(fieldName),
					memberAccountId, userLogin,
			"DEPOSITFROMREMITTANCE", null, null,
			acctgTransId, null, null);
			}
		}
		

		
	}

	private static BigDecimal getTotalAmounts(Long normalRemittanceMonthYearId,
			String fieldName) {
		BigDecimal bdTotalAmount = BigDecimal.ZERO;

		// Need month, year and employerCode
		Long month = null;
		Long year = null;
		String employerCode = "";

		// Find the SalaryMonthYear
		GenericValue normalRemittanceMonthYear = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			normalRemittanceMonthYear = delegator.findOne(
					"NormalRemittanceMonthYear", UtilMisc.toMap(
							"normalRemittanceMonthYearId",
							normalRemittanceMonthYearId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		month = normalRemittanceMonthYear.getLong("month");
		year = normalRemittanceMonthYear.getLong("year");

		GenericValue station = LoanUtilities
				.getStation(normalRemittanceMonthYear.getString("stationId"));
		employerCode = station.getString("employerCode");

		// Get total amount
		List<GenericValue> stationRemittanceSumsELI = new ArrayList<GenericValue>();
		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> stationRemittanceSumsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"month", EntityOperator.EQUALS, month.toString()),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year.toString()),

						EntityCondition.makeCondition("employerCode",
								EntityOperator.EQUALS, employerCode),

						EntityCondition.makeCondition(
								"normalRemittanceMonthYearId",
								EntityOperator.EQUALS,
								normalRemittanceMonthYearId)),

				EntityOperator.AND);
		try {
			stationRemittanceSumsELI = delegator.findList("MemberRemittance",
					stationRemittanceSumsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : stationRemittanceSumsELI) {
			bdTotalAmount = bdTotalAmount.add(genericValue
					.getBigDecimal(fieldName));
		}

		return bdTotalAmount;
	}

	/****
	 * Loan Deductions - No Charges paid
	 * 
	 * */
	private static LoanBalanceItem doProcessingLoanOnlyDeductions(
			Map<String, String> userLogin, String month, String year,
			String employerCode, Long normalRemittanceMonthYearId,
			String acctgTransId, Long entrySequence) {
		// Get the payroll numbers from MemberSalary given month, year and
		// employerCode
		List<GenericValue> listMemberRemittance = getMemberRemittance(month,
				year, employerCode, normalRemittanceMonthYearId);

		LoanBalanceItem loanBalanceItem = new LoanBalanceItem();
		
		loanBalanceItem.setFosaSavingAmount(BigDecimal.ZERO);

		BigDecimal bdTotalPrincipalPaid = BigDecimal.ZERO;
		BigDecimal bdTotalInterestPaid = BigDecimal.ZERO;
		BigDecimal bdTotalInsurancePaid = BigDecimal.ZERO;

		Long memberAccountId = null;
		String payrollNumber = null;

		String accountTransactionParentId = null;

		List<GenericValue> listLoanRepayments = null;

		for (GenericValue genericValue : listMemberRemittance) {
			payrollNumber = genericValue.getString("payrollNumber");
			// if loansInterestInsuranceAmount > 0
			GenericValue member = RemittanceServices
					.getMemberByPayrollNo(payrollNumber);

			if (genericValue.getBigDecimal("loansInterestInsuranceAmount")
					.compareTo(BigDecimal.ZERO) == 1) {
				BigDecimal bdLoanAmount = genericValue
						.getBigDecimal("loansInterestInsuranceAmount");
				listLoanRepayments = new ArrayList<GenericValue>();

				// ###### Add the Net Salary to Member Account
				// bdNetSalaryAmt = genericValue.getBigDecimal("netSalary");
				// bdTotalSalaryPosted =
				// bdTotalSalaryPosted.add(bdNetSalaryAmt);

				accountTransactionParentId = AccHolderTransactionServices
						.getcreateAccountTransactionParentId(memberAccountId,
								userLogin);
				payrollNumber = genericValue.getString("payrollNumber");

				// ####### Deduct the total Loan Deductions
				
				List<Long> listLoanApplicationIds = LoanServices
						.getDisbursedLoansIds(member.getLong("partyId"));
				BigDecimal bdMemberTotalLoanExpectedAmt = BigDecimal.ZERO;
				BigDecimal bdLoanExpectedAmt = BigDecimal.ZERO;
				// String productName = "";
				GenericValue loanRepayment;
				Long loanRepaymentId = null;
				Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

				// BigDecimal bdSalaryBalance = bdNetSalaryAmt.subtract(
				// bdSalaryChargeAmt).subtract(bdSalaryExciseAmt);
				// bdLoanAmount
				// Do Insurance

				// Do Interest

				// Do Principal

				int totalCount = listLoanApplicationIds.size();
				int count = 0;
				for (Long loanApplicationId : listLoanApplicationIds) {

					count = count + 1;

					if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

						bdLoanExpectedAmt = LoanRepayments
								.getTotalPrincipaByLoanDue(loanApplicationId
										.toString());

						bdLoanExpectedAmt = bdLoanExpectedAmt
								.add(LoanRepayments
										.getTotalInterestByLoanDue(loanApplicationId
												.toString()));
						// if (bdSalaryBalance.compareTo(bdLoanExpectedAmt) >=
						// 0)

						bdLoanExpectedAmt = bdLoanExpectedAmt
								.add(LoanRepayments
										.getTotalInsurancByLoanDue(loanApplicationId
												.toString()));

						bdMemberTotalLoanExpectedAmt = bdMemberTotalLoanExpectedAmt
								.add(bdLoanExpectedAmt);

						// if (bdLoanExpectedAmt.compareTo(BigDecimal.ZERO) > 0)
						// {
						// AccHolderTransactionServices.memberTransactionDeposit(
						// bdLoanExpectedAmt, memberAccountId, userLogin,
						// "LOANREPAYMENT", accountTransactionParentId, null,
						// acctgTransId, null, loanApplicationId);
						// }

						BigDecimal totalLoanDue = LoanRepayments
								.getTotalLoanByLoanDue(loanApplicationId
										.toString());
						BigDecimal totalInterestDue = LoanRepayments
								.getTotalInterestByLoanDue(loanApplicationId
										.toString());
						BigDecimal totalInsuranceDue = LoanRepayments
								.getTotalInsurancByLoanDue(loanApplicationId
										.toString());
						BigDecimal totalPrincipalDue = LoanRepayments
								.getTotalPrincipaByLoanDue(loanApplicationId
										.toString());

						BigDecimal interestAmount = BigDecimal.ZERO;
						BigDecimal insuranceAmount = BigDecimal.ZERO;
						BigDecimal principalAmount = BigDecimal.ZERO;

						if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

							if (bdLoanAmount.compareTo(totalInsuranceDue) > 0) {
								insuranceAmount = totalInsuranceDue;
								bdLoanAmount = bdLoanAmount
										.subtract(insuranceAmount);
							} else {
								insuranceAmount = bdLoanAmount;
								bdLoanAmount = BigDecimal.ZERO;
							}
						}

						if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

							if (bdLoanAmount.compareTo(totalInterestDue) > 0) {
								interestAmount = totalInterestDue;
								bdLoanAmount = bdLoanAmount
										.subtract(interestAmount);
							} else {
								interestAmount = bdLoanAmount;
								bdLoanAmount = BigDecimal.ZERO;
							}
						}

						if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

							if (bdLoanAmount.compareTo(totalPrincipalDue) > 0) {
								principalAmount = totalPrincipalDue;
								bdLoanAmount = bdLoanAmount
										.subtract(principalAmount);
							} else {
								principalAmount = bdLoanAmount;
								bdLoanAmount = BigDecimal.ZERO;
							}
						}

						bdTotalPrincipalPaid = bdTotalPrincipalPaid
								.add(principalAmount);
						bdTotalInterestPaid = bdTotalInterestPaid
								.add(interestAmount);
						bdTotalInsurancePaid = bdTotalInsurancePaid
								.add(insuranceAmount);

						loanRepaymentId = delegator
								.getNextSeqIdLong("LoanRepayment");
						loanRepayment = delegator.makeValue("LoanRepayment",
								UtilMisc.toMap("loanRepaymentId",
										loanRepaymentId,
										"isActive",
										"Y",
										"createdBy",
										"admin",
										// "transactionType", "LOANREPAYMENT",
										"loanApplicationId", loanApplicationId,
										"partyId", member.getLong("partyId"),

										"transactionAmount", bdLoanExpectedAmt,

										"totalLoanDue", totalLoanDue,

										"totalInterestDue", totalInterestDue,

										"totalInsuranceDue", totalInsuranceDue,

										"totalPrincipalDue", totalPrincipalDue,

										"interestAmount", interestAmount,
										"insuranceAmount", insuranceAmount,
										"principalAmount", principalAmount,
										"repaymentMode", "REMITTANCE",

										"acctgTransId", acctgTransId

								));

						try {
							delegator.createOrStore(loanRepayment);
						} catch (GenericEntityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

				// Process By Extracting the loan balances
				// bdLoanAmount

				if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {
					// Repay Balances for each loan
					for (Long loanApplicationId : listLoanApplicationIds) {

						if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

							BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);

							

							

							BigDecimal interestAmount = BigDecimal.ZERO;
							BigDecimal insuranceAmount = BigDecimal.ZERO;
							BigDecimal principalAmount = BigDecimal.ZERO;

							

							if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0) {

								if (bdLoanAmount.compareTo(bdLoanBalance) >= 0) {
									principalAmount = bdLoanBalance;
									bdLoanAmount = bdLoanAmount
											.subtract(bdLoanBalance);
									
								//Mark loan as cleared
									
//									Long loanStatusId = LoanUtilities.getLoanStatusId("CLEARED");
//									GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
//									loanApplication.set("loanStatusId", loanStatusId);
//									try {
//										delegator.createOrStore(loanApplication);
//									} catch (GenericEntityException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
									
									LoanRepayments.clearLoan(loanApplicationId, userLogin, " Cleared From Remittance Processing");
								} else {
									principalAmount = bdLoanAmount;
									bdLoanAmount = BigDecimal.ZERO;
								}
							}

							bdTotalPrincipalPaid = bdTotalPrincipalPaid
									.add(principalAmount);
							bdTotalInterestPaid = bdTotalInterestPaid
									.add(interestAmount);
							bdTotalInsurancePaid = bdTotalInsurancePaid
									.add(insuranceAmount);

							loanRepaymentId = delegator
									.getNextSeqIdLong("LoanRepayment");
							loanRepayment = delegator.makeValue("LoanRepayment",
									UtilMisc.toMap("loanRepaymentId",
											loanRepaymentId,
											"isActive",
											"Y",
											"createdBy",
											"admin",
											// "transactionType", "LOANREPAYMENT",
											"loanApplicationId", loanApplicationId,
											"partyId", member.getLong("partyId"),

											"transactionAmount", bdLoanExpectedAmt,

											"totalLoanDue", BigDecimal.ZERO,

											"totalInterestDue", BigDecimal.ZERO,

											"totalInsuranceDue", BigDecimal.ZERO,

											"totalPrincipalDue", BigDecimal.ZERO,

											"interestAmount", interestAmount,
											"insuranceAmount", insuranceAmount,
											"principalAmount", principalAmount,
											"repaymentMode", "REMITTANCE",

											"acctgTransId", acctgTransId

									));

							try {
								delegator.createOrStore(loanRepayment);
							} catch (GenericEntityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

					}
				}
				
				if (bdLoanAmount.compareTo(BigDecimal.ZERO) > 0){
					// Deposit Excess to Savings Account
					//GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
					String thememberAccountId = LoanUtilities.getMemberAccountIdGivenMemberAndAccountCode(member.getLong("partyId"), AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);
					AccHolderTransactionServices.cashDepositFromStationProcessing(bdLoanAmount, Long.valueOf(thememberAccountId), userLogin, "DEPOSITFROMEXCESS", acctgTransId);
					
					loanBalanceItem.setFosaSavingAmount(loanBalanceItem.getFosaSavingAmount().add(bdLoanAmount));
					//bdTotalPrincipalPaid = bdTotalPrincipalPaid.add(bdLoanAmount);
				}
				

			}
			
			
		}
		
		loanBalanceItem.setPrincipalAmount(bdTotalPrincipalPaid);
		loanBalanceItem.setInsuranceAmount(bdTotalInsurancePaid);
		loanBalanceItem.setInterestAmount(bdTotalInterestPaid);


		// Post Total Net Salary in GL bdTotalSalaryPosted

		// SALARYPROCESSING
		// GenericValue accountHolderTransactionSetup =
		// AccHolderTransactionServices
		// .getAccountHolderTransactionSetup("SALARYPROCESSING");
		// String debitAccountId = accountHolderTransactionSetup
		// .getString("cashAccountId");
		// String creditAccountId = accountHolderTransactionSetup
		// .getString("memberDepositAccId");
		GenericValue accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("STATIONACCOUNTPAYMENT");

		String stationDepositAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");
		String savingsAccountGLAccountId = LoanUtilities
				.getGLAccountIDForAccountProduct(AccHolderTransactionServices.SAVINGS_ACCOUNT_CODE);



		String branchId = AccHolderTransactionServices
				.getEmployeeBranch((String) userLogin.get("partyId"));
		// ------------------------
		// Debit Leaf Base with the total
//		AccHolderTransactionServices.createAccountPostingEntry(
//				bdTotalSalaryPosted, acctgTransId, "D",
//				stationDepositAccountId, entrySequence.toString(), branchId);

		// Credit Member Deposits with (total net - (total charge + total excise
		// duty))

		// Post the Loan Repayments
		// DR the savings withdrawable savingsAccountGLAccountId
//		BigDecimal bdTotalLoanAmountPaid = bdTotalPrincipalPaid.add(
//				bdTotalInterestPaid).add(bdTotalInsurancePaid);
//		entrySequence = entrySequence + 1;
//		AccHolderTransactionServices.createAccountPostingEntry(
//				bdTotalLoanAmountPaid, acctgTransId, "D",
//				savingsAccountGLAccountId, entrySequence.toString(), branchId);

		// Principal Payment
		// CR Loan Receivable bdTotalPrincipalPaid
		entrySequence = entrySequence + 1;

		accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("PRINCIPALPAYMENT");
		String loanReceivableAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalPrincipalPaid, acctgTransId, "C",
				loanReceivableAccountId, entrySequence.toString(), branchId);

		// Interest Payment
		// CR Interest Recevable bdTotalInterestPaid
		entrySequence = entrySequence + 1;

		accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("INTERESTPAYMENT");
		String interestReceivableAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		AccHolderTransactionServices
				.createAccountPostingEntry(bdTotalInterestPaid, acctgTransId,
						"C", interestReceivableAccountId,
						entrySequence.toString(), branchId);

		// Insurance Payment
		// CR Charge/Insurance Receivable bdTotalInsurancePaid
		entrySequence = entrySequence + 1;

		accountHolderTransactionSetup = AccHolderTransactionServices
				.getAccountHolderTransactionSetup("INSURANCEPAYMENT");
		String insurancePaymentAccountId = accountHolderTransactionSetup
				.getString("memberDepositAccId");

		AccHolderTransactionServices.createAccountPostingEntry(
				bdTotalInsurancePaid, acctgTransId, "C",
				insurancePaymentAccountId, entrySequence.toString(), branchId);

		// DR
		// TODO May use it to check loan Repayment
		// for (GenericValue genericValue : listLoanRepayments) {
		// //entrySequence = entrySequence + 1;
		// LoanRepayments.repayLoanWithoutDebitingCash(genericValue, userLogin,
		// entrySequence, acctgTransId);
		// }

		// listLoanRepayments.add(loanRepayment);

		// Update the MemberSalary to processed

		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		// try {
		// delegator.storeAll(listSalaryToUpdate);
		// } catch (GenericEntityException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Prior Transaction when a cheque was paid
		// Debit Cash at Bank
		// Credit Leaf Base
		loanBalanceItem.setSequence(entrySequence.toString());
		return loanBalanceItem;

	}

	private static List<GenericValue> getMemberRemittance(String month,
			String year, String employerCode, Long normalRemittanceMonthYearId) {

		List<GenericValue> normalRemittanceMonthYearELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> normalRemittanceMonthYearConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"normalRemittanceMonthYearId", EntityOperator.EQUALS,
						normalRemittanceMonthYearId),
				// processed
						EntityCondition.makeCondition("processed",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);

		// normalRemittanceMonthYearId
		try {
			normalRemittanceMonthYearELI = delegator.findList(
					"MemberRemittance", normalRemittanceMonthYearConditions,
					null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return normalRemittanceMonthYearELI;
	}
	
	private static boolean hasAccount(String accountCode, String payrollNo) {
		// TODO Check of a member , given payroll number has an account of the
		// product given
		Long accountProductId = LoanUtilities.getAccountProductGivenCodeId(
				accountCode).getLong("accountProductId");

		Long partyId = LoanUtilities.getMemberId(payrollNo);

		Long memberAccountId = LoanUtilities
				.getMemberAccountIdFromMemberAccount(partyId, accountProductId);

		if (memberAccountId != null)
			return true;

		return false;
	}

}
