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
import org.ofbiz.webapp.event.EventHandlerException;

public class SalaryProcessingServices {
	public static Logger log = Logger.getLogger(SalaryProcessingServices.class);

	public static String processSalaryReceivedNoDeduct(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
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
		
		for (GenericValue genericValue : listAccountProductCharge) {
			productChargeId = genericValue.getLong("productChargeId");
			log.info(" CCCCCCCCCCCCCCC Charge ID "
					+ genericValue.getLong("productChargeId"));

			productCharge = LoanUtilities.getProductCharge(productChargeId);

			if (productCharge != null)
				log.info(" CCCCCCCCCCCCCCC Charge Name "
						+ productCharge.getString("name"));
			
			//if has no parent the its salary charge amount
			if (genericValue.getLong("parentChargeId") == null){
				//Its salary amount
				bdSalaryChargeAmt = genericValue.getBigDecimal("fixedAmount");
			}
			
			//if has parent then its excise duty amount
			if (genericValue.getLong("parentChargeId") != null){
				//Its salary amount
				bdSalaryExciseAmt = genericValue.getBigDecimal("rateAmount").multiply(bdSalaryChargeAmt).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
			}
			
			
			log.info(" CCCCCCCCCCCCCCCSSSS Salary Charge "
					+ bdSalaryChargeAmt);
			log.info(" CCCCCCCCCCCCCCCSSSS Excise Duty "
					+ bdSalaryExciseAmt);
		}
		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty
		doProcessing(month, year, employerCode, bdSalaryChargeAmt, bdSalaryExciseAmt);

		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");

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

	private static void doProcessing(String month, String year,
			String employerCode, BigDecimal bdSalaryChargeAmt,
			BigDecimal bdSalaryExciseAmt) {
		//Get the payroll numbers from MemberSalary given month, year and employerCode
		List<GenericValue> listMemberSalary = getMemberSalaryList(month, year, employerCode);
		
		BigDecimal bdTotalSalaryPosted = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryCharge = BigDecimal.ZERO;
		BigDecimal bdTotalSalaryExciseDuty = BigDecimal.ZERO;
		for (GenericValue genericValue : listMemberSalary) {
			bdTotalSalaryPosted = bdTotalSalaryPosted.add(genericValue.getBigDecimal("netSalary"));
			
			//Add Net Salary to the Savings Account
			
			//Deduct the Salary Charge
			
			
			//Deduct the Salary Duty
			
			
			//Add Salary Charge
			bdTotalSalaryCharge = bdTotalSalaryCharge.add(bdSalaryChargeAmt);
			//Add Excise Duty
			bdTotalSalaryExciseDuty = bdTotalSalaryExciseDuty.add(bdSalaryExciseAmt);
		}
		
		//Post Total Net Salary in GL bdTotalSalaryPosted
		//Credit Leaf Base with the total
		
		
		//Debit Member Deposits with (total net - (total charge + total excise duty)) 
		//Debit Salary Charge with total salary charge
		// Debit Excise Duty with total excise duty
		
		
		
		//Prior Transaction when a cheque was paid
		//Credit Cash at Bank
		//Debit Leaf Base
		
	}

	private static List<GenericValue> getMemberSalaryList(String month, String year,
			String employerCode) {
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

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

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

		log.info(" DDDDDDDDEDUCT LLLLLLLLLLLLLLLL Month " + month + " Year "
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
		Map<String, String> userLogin = (Map<String, String>) request
				.getAttribute("userLogin");

		log.info("HHHHHHHHHHHH Salary Processing ... With Deductions !!!");

		// Continue Processing for Salary Without Deductions
		// Get the Salary Processing Charge
		// Get the Salary Processing Charge Excise Duty
		List<GenericValue> listAccountProductCharge = LoanUtilities
				.getAccountProductChargeList("SALARYPROCESSING", "999");

		Long productChargeId = null;
		GenericValue productCharge = null;
		
		BigDecimal bdSalaryChargeAmt = BigDecimal.ZERO;
		BigDecimal bdSalaryExciseAmt = BigDecimal.ZERO;
		
		for (GenericValue genericValue : listAccountProductCharge) {
			productChargeId = genericValue.getLong("productChargeId");
			log.info(" CCCCCCCCCCCCCCC Charge ID "
					+ genericValue.getLong("productChargeId"));

			productCharge = LoanUtilities.getProductCharge(productChargeId);

			if (productCharge != null)
				log.info(" CCCCCCCCCCCCCCC Charge Name "
						+ productCharge.getString("name"));
			
			//if has no parent the its salary charge amount
			if (genericValue.getLong("parentChargeId") == null){
				//Its salary amount
				bdSalaryChargeAmt = genericValue.getBigDecimal("fixedAmount");
			}
			
			//if has parent then its excise duty amount
			if (genericValue.getLong("parentChargeId") != null){
				//Its salary amount
				bdSalaryExciseAmt = genericValue.getBigDecimal("rateAmount").multiply(bdSalaryExciseAmt).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
			}
			
			
			log.info(" CCCCCCCCCCCCCCCSSSS Salary Charge "
					+ bdSalaryChargeAmt);
			log.info(" CCCCCCCCCCCCCCCSSSS Excise Duty "
					+ bdSalaryExciseAmt);
		}
		
		//Get Salary Charge
		
		//Get Salary Excise Duty

		// For each Member
		// Post Net Salary
		// Post Salary Processing Charge
		// Post Salary Processing Excise Duty

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
