/*package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.payroll.TaxTracker;
import org.ofbiz.webapp.event.EventHandlerException;

<<<<<<< HEAD


  @author charles

=======
/**
 * @author charles
 * **/
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744
public class PayrollProcess {

	static BigDecimal bdMAX_PENSION_CONTRIBUTION = BigDecimal.ZERO;
	static BigDecimal bdINSURANCE_RELIEF_MAX = BigDecimal.ZERO;
	static BigDecimal bdBasicPay = BigDecimal.ZERO;

	/***
	 * Values to Write
	 *
	 * */

	public static BigDecimal bdEXCESSPENSIONBENEFIT;
	public static BigDecimal bdINSURANCERELIEF;
	public static BigDecimal bdLOWINTERESTBENEFIT;
	public static BigDecimal bdMPR;
	public static BigDecimal bdNETPAY;
	public static BigDecimal bdNHIF;
	public static BigDecimal bdPAYE;
	public static BigDecimal bdTAXABLEINCOME;
	public static BigDecimal bdTOTDEDUCTIONS;
	public static BigDecimal bdNSSFStatutory = BigDecimal.ZERO;
	public static BigDecimal bdNSSFVoluntary = BigDecimal.ZERO;
	public static BigDecimal bdPensionAmt = BigDecimal.ZERO;

	private static Logger log = Logger.getLogger(PayrollProcess.class);

	public static String runPayroll(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String payrollPeriodId = (String) request
				.getParameter("payrollPeriodId");
		// Get employees
		List<GenericValue> employeesELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### payrollPeriodId is :::: " + payrollPeriodId);

		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null); //=
		// party.getDelegator();

		// EntityConditionList<EntityExpr> employeeConditions = EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		// "partyId", EntityOperator.EQUALS,
		// partyId), EntityCondition.makeCondition(
		// "payrollPeriodId", EntityOperator.EQUALS,
		// periodId)), EntityOperator.AND);

		// try {
		// chequeDepositELI = delegator.findList("AccountTransaction",
		// transactionConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }

		try {
			employeesELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("payrollPeriodId", payrollPeriodId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		log.info("######### Loop through Staff");

		for (GenericValue genericValue : employeesELI) {
			processPayroll(genericValue, genericValue
					.getString("staffPayrollId"), delegator);
			log.info("######### Staff ID "
					+ genericValue.getString("staffPayrollId"));
		}

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

	private static void processPayroll(GenericValue employee,
			String staffPayrollId, Delegator delegator) {
		// Compute Gross Pay
		BigDecimal bdGrossPay = BigDecimal.ZERO;
		BigDecimal bdTaxablePay = BigDecimal.ZERO;
		BigDecimal bdGrossTax = BigDecimal.ZERO;
		BigDecimal bdPAYEAmount = BigDecimal.ZERO;
		BigDecimal bdNHIFAmount = BigDecimal.ZERO;
		BigDecimal bdTotRelief = BigDecimal.ZERO;
		BigDecimal bdTotDeductions = BigDecimal.ZERO;
		BigDecimal bdNetPay = BigDecimal.ZERO;

		bdBasicPay = getBasicPay(employee, staffPayrollId, delegator);
		log.info("######### Basic Pay Amount " + bdBasicPay);
		bdGrossPay = sumPayments(employee, staffPayrollId, delegator);
		log.info("######### Gross Pay Amount " + bdGrossPay);

		bdTaxablePay = calcTaxablePay(employee, delegator, bdGrossPay,
				staffPayrollId);
		log.info("######### Taxable Pay Amount " + bdTaxablePay);
		bdTAXABLEINCOME = bdTaxablePay;

		bdGrossTax = computeGrossTax(bdTaxablePay, delegator);
		bdNHIFAmount = computeNHIF(employee, delegator, bdBasicPay);
		bdNHIF = bdNHIFAmount;
		bdTotRelief = getTotalRelief(employee, staffPayrollId, delegator);

		bdPAYEAmount = bdGrossTax.subtract(bdTotRelief);
		bdPAYE = bdPAYEAmount;

		calculateInterestAmounts(employee, staffPayrollId, delegator);

		bdTotDeductions = sumDeductions(employee, staffPayrollId, delegator).add(bdPensionAmt)
				.add(bdPAYEAmount).add(bdNSSFStatutory).add(bdNSSFVoluntary)
				.add(bdNHIFAmount);
		log.info("######### Tot Deductions " + bdTotDeductions);
		bdTOTDEDUCTIONS = bdTotDeductions;

		bdNetPay = bdGrossPay.subtract(bdTotDeductions);
		log.info("######### Net Pay " + bdNetPay);
		bdNETPAY = bdNetPay;

		// bdtotDeductions=getTotalDeductions(employee, delegator, );

<<<<<<< HEAD



=======
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744
		// Save Gross Pay
		String staffPayrollElementsSequenceId;
		GenericValue staffPayrollElement;

		staffPayrollElementsSequenceId = delegator
				.getNextSeqId("StaffPayrollElements");

		staffPayrollElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsSequenceId",
						staffPayrollElementsSequenceId, "payrollElementId",
						"GROSSPAY", "amount", bdGrossPay, "staffPayrollId",
						staffPayrollId, "valueChanged", "N", "balance", BigDecimal.valueOf(0.0)));
		try {
			staffPayrollElement = delegator
					.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			delegator.createOrStore(staffPayrollElement);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> listSystemElements = new ArrayList<GenericValue>();
		GenericValue systemElement;
		/***
		 * "EXCESSPENSIONBENEFIT" "INSURANCERELIEF" "LOWINTERESTBENEFIT" "MPR"
		 * "NETPAY" "NHIF" "NSSF" "NSSFVOL" "PAYE" "PENSION" "TAXABLEINCOME"
		 * "TOTDEDUCTIONS"
		 * */
		/**
		 * 	public static BigDecimal bdEXCESSPENSIONBENEFIT;
			public static BigDecimal bdINSURANCERELIEF;
			public static BigDecimal bdLOWINTERESTBENEFIT;
			public static BigDecimal bdMPR;
			public static BigDecimal bdNETPAY;
			public static BigDecimal bdNHIF;
			public static BigDecimal bdPAYE;
			public static BigDecimal bdTAXABLEINCOME;
			public static BigDecimal bdTOTDEDUCTIONS;
			public static BigDecimal bdNSSFStatutory = BigDecimal.ZERO;
			public static BigDecimal bdNSSFVoluntary = BigDecimal.ZERO;
			public static BigDecimal bdPensionAmt = BigDecimal.ZERO;
		 **/
		// EXCESSPENSIONBENEFIT
		listSystemElements.add(createElementToSave(delegator, "EXCESSPENSIONBENEFIT", bdEXCESSPENSIONBENEFIT, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "INSURANCERELIEF", bdINSURANCERELIEF, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "LOWINTERESTBENEFIT", bdLOWINTERESTBENEFIT, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "MPR", bdMPR, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "NETPAY", bdNETPAY, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "NHIF", bdNHIF, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "PAYE", bdPAYE, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "TAXABLEINCOME", bdTAXABLEINCOME, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "TOTDEDUCTIONS", bdTOTDEDUCTIONS, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "NSSF", bdNSSFStatutory, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "NSSFVOL", bdNSSFVoluntary, staffPayrollId));
		listSystemElements.add(createElementToSave(delegator, "PENSION", bdPensionAmt, staffPayrollId));

		try {
			delegator.storeAll(listSystemElements);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static GenericValue createElementToSave(Delegator delegator, String payrollElementId, BigDecimal elementAmount, String staffPayrollId ){
		String staffPayrollElementsSequenceId = delegator
		.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsId",
						staffPayrollElementsSequenceId, "payrollElementId",
						payrollElementId, "amount", elementAmount, "staffPayrollId",
						staffPayrollId, "valueChanged", "N", "balance", BigDecimal.valueOf(0.0)));
		try {
			staffPayrollElement = delegator
					.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return staffPayrollElement;
	}

	private static BigDecimal calculateInterestAmounts(GenericValue employee,
			String staffPayrollId, Delegator delegator) {

		List<GenericValue> payrollElementsWithInterestELI = null;
		// BigDecimal bdLoanAmount = BigDecimal.ZERO;

		try {
			payrollElementsWithInterestELI = delegator.findList(
					"PayrollElement", EntityCondition.makeCondition(
							"hasInterest", "Y"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// for (GenericValue payrollElement : payrollElementELI) {
		// // Get the amount
		//
		// getLoanElementAmount(payrollElement, delegator);
		// }

		// We have PayrollElements with interest
		for (GenericValue payrolElement : payrollElementsWithInterestELI) {
			if ((payrolElement.getString("isfixed") != null)
					&& (payrolElement.getString("isfixed").equals("Y"))) {
				// Get the amount and add it to the staffPayrolElements
				processFixedInterest(delegator, payrolElement, staffPayrollId);
			} else if ((payrolElement.getString("isfixed") != null)
					&& (payrolElement.getString("isfixed").equals("N"))) {
				// Get the rate, Get the frequency, Get the Balance before
				// deduction, multiply rate by balance then save the new value
				processVariableInterest(delegator, payrolElement,
						staffPayrollId);
			}
		}

		return null;
	}

	private static BigDecimal getTotalRelief(GenericValue employee,String staffPayrollId,
			Delegator delegator) {
<<<<<<< HEAD
		BigDecimal bdtotalRelief=BigDecimal.ZERO;
		BigDecimal bdInsuranceRelief=BigDecimal.ZERO;
		BigDecimal bdMPR=BigDecimal.ZERO;

		bdInsuranceRelief=getInsuranceRelief(employee, delegator);
		bdMPR=getMPR(employee, delegator);
		log.info("######### Insurance Relief " + bdInsuranceRelief);
		log.info("######### MPR " + bdMPR);
		bdtotalRelief=bdInsuranceRelief.add(bdMPR);

=======
		BigDecimal bdtotalRelief = BigDecimal.ZERO;
		BigDecimal bdInsuranceRelief = BigDecimal.ZERO;
		BigDecimal bdMPRAmount = BigDecimal.ZERO;

		bdInsuranceRelief = getInsuranceRelief(employee, staffPayrollId, delegator);
		bdINSURANCERELIEF = bdInsuranceRelief;
		bdMPRAmount = getMPR(employee, delegator);
		bdMPR = bdMPRAmount;
		log.info("######### Insurance Relief " + bdInsuranceRelief);
		log.info("######### MPR " + bdMPR);
		bdtotalRelief = bdInsuranceRelief.add(bdMPR);

		log.info("######### TotRelief " + bdtotalRelief);
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744

		return bdtotalRelief;
	}

	private static BigDecimal getMPR(GenericValue employee, Delegator delegator) {
		BigDecimal bdMPR = BigDecimal.ZERO;

		List<GenericValue> payrollConstantELI = null;

		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdMPR = getMPRAmount(payrollConstant, delegator);
		}
		return bdMPR;
	}

	private static BigDecimal getInsuranceRelief(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdInsRelief = BigDecimal.ZERO;
<<<<<<< HEAD

		EntityConditionList<EntityExpr> elementConditions = EntityCondition
		 .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		 "insurancecontribution", EntityOperator.EQUALS,
		 "Y"), EntityCondition.makeCondition(
		 "hasRelief", EntityOperator.EQUALS,
		 "Y")), EntityOperator.AND);
=======
		BigDecimal bdInsAmount = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> elementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"insurancecontribution", EntityOperator.EQUALS, "Y"),
						EntityCondition.makeCondition("hasRelief",
								EntityOperator.EQUALS, "Y")),
						EntityOperator.AND);
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744

		try {
			payrollElementELI = delegator.findList("PayrollElement",
					elementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdInsAmount = bdInsAmount.add(getElementAmount(payrollElement,staffPayrollId,
					delegator));
		}

		bdInsRelief = bdInsAmount.multiply(getInsuranceReliefPercentage(
				employee, delegator));

		log.info("######### Insurance Max " + bdINSURANCE_RELIEF_MAX);
		if (bdInsRelief.compareTo(bdINSURANCE_RELIEF_MAX) >= 0) {
			bdInsRelief = bdINSURANCE_RELIEF_MAX;
		}
<<<<<<< HEAD
//		bdInsRelief
=======
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744

		return bdInsRelief;
	}

	private static BigDecimal getInsuranceReliefPercentage(
			GenericValue employee, Delegator delegator) {
		BigDecimal bdInsPercentage = BigDecimal.ZERO;
		List<GenericValue> insurancePercentgelELI = null;
		try {
			insurancePercentgelELI = delegator.findList("PayrollConstants",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPension : insurancePercentgelELI) {
			// Get the amount

			bdInsPercentage = getInsPercentage(staffPension, delegator)
					.multiply(new BigDecimal(0.01));
			log.info("######### Insurance but now % " + bdInsPercentage);
		}

		return bdInsPercentage;
	}

	private static BigDecimal getInsPercentage(GenericValue staffInsurance,
			Delegator delegator) {
		BigDecimal bdInsPercentage = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							staffInsurance.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue insPercentge : staffPayrollELI) {
			// Get the amount
			log.info("######### Insurance insuranceReliefRate % "
					+ insPercentge.getBigDecimal("insuranceReliefRate"));
			bdInsPercentage = insPercentge.getBigDecimal("insuranceReliefRate");
			bdINSURANCE_RELIEF_MAX = insPercentge
					.getBigDecimal("insuranceReliefmax");
		}
		log.info("######### Insurance % " + bdInsPercentage);
		return bdInsPercentage;
	}

	private static BigDecimal calcTaxablePay(GenericValue employee,
			Delegator delegator, BigDecimal bdGrossPay, String staffPayrollId) {
		BigDecimal bdTaxableIncome = BigDecimal.ZERO;
		BigDecimal bdExcessPensionBenefit = BigDecimal.ZERO;
		BigDecimal bdLowInterestBenefit = BigDecimal.ZERO;
		BigDecimal bdDisabilityAllowance = BigDecimal.ZERO;
		BigDecimal bdNonTaxableAmounts = BigDecimal.ZERO;

		bdExcessPensionBenefit = calcExcessPensionBen(employee, delegator,
				staffPayrollId);
		bdLowInterestBenefit = calcLowInterestBen(employee, staffPayrollId, delegator);
		bdLOWINTERESTBENEFIT = bdLowInterestBenefit;
		bdDisabilityAllowance = getDisabilityAllowance(employee, delegator);
		bdNonTaxableAmounts = getNonTaxableAmounts(employee, staffPayrollId, delegator);

		bdTaxableIncome = bdGrossPay.add(bdExcessPensionBenefit).add(
				bdLowInterestBenefit).subtract(bdNSSFStatutory).subtract(
				bdNSSFVoluntary).subtract(bdPensionAmt).subtract(
				bdDisabilityAllowance).subtract(bdNonTaxableAmounts);

		if (bdTaxableIncome.intValue() <= 0) {
			bdTaxableIncome = BigDecimal.ZERO;
		}

		return bdTaxableIncome;
	}

	private static BigDecimal getNonTaxableAmounts(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdNonTaxableAmounts = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> nonTaxableConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"elementType", EntityOperator.EQUALS, "Payment"),
						EntityCondition.makeCondition("taxable",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);

		try {
			payrollElementELI = delegator.findList("PayrollElement",
					nonTaxableConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdNonTaxableAmounts = bdNonTaxableAmounts.add(getElementAmount(
					payrollElement, staffPayrollId, delegator));
		}

		return bdNonTaxableAmounts;
	}

	private static BigDecimal getDisabilityAllowance(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdDisabilityAllowance = BigDecimal.ZERO;

		if (checkEmployeeDisability(employee, delegator)) {
			try {
				payrollConstantELI = delegator.findList("PayrollConstants",
						null, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue payrollConstant : payrollConstantELI) {
				// Get the amount

				bdDisabilityAllowance = getDisabilityAllowanceAmount(
						payrollConstant, delegator);
			}
		} else {
			bdDisabilityAllowance = BigDecimal.ZERO;
		}
		return bdDisabilityAllowance;
	}

	private static boolean checkEmployeeDisability(GenericValue employee,
			Delegator delegator) {
		// List<GenericValue> employeeELI = null;
		GenericValue disabledPerson = null;
		// BigDecimal bdGross = BigDecimal.ZERO;
		try {
			disabledPerson = delegator.findOne("Person", UtilMisc.toMap(
					"partyId", employee.getString("partyId")), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// for (GenericValue payrollElement : employeeELI) {
		// //Get the amount
		//
		// bdGross = bdGross.add(getElementAmount(payrollElement, delegator));
		// }

		if ((disabledPerson == null)
				|| (disabledPerson.getString("isDisabled") == null)
				|| !(disabledPerson.getString("isDisabled").equals("Y"))) {
			return false;
		} else {
			return true;
		}
	}

	private static BigDecimal getDisabilityAllowanceAmount(
			GenericValue payrollConstant, Delegator delegator) {
		// Do the real getting
		BigDecimal bdDisabltyAllowanceAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdDisabltyAllowanceAmount = staffPayrollConstants
					.getBigDecimal("disability_relief");
		}
		return bdDisabltyAllowanceAmount;
	}

<<<<<<< HEAD
	private static BigDecimal getMPRAmount(
			GenericValue payrollConstant, Delegator delegator) {
=======
	private static BigDecimal getMPRAmount(GenericValue payrollConstant,
			Delegator delegator) {
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744
		// Do the real getting
		BigDecimal bdMPRAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdMPRAmount = staffPayrollConstants
					.getBigDecimal("monthlyPersonalRelief");
		}
		return bdMPRAmount;
	}

	private static BigDecimal calcLowInterestBen(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		BigDecimal bdlowIntBen = BigDecimal.ZERO;
		BigDecimal bdsalaryAdvanceAmount = BigDecimal.ZERO;
		BigDecimal bdsalaryAdvanceBalance = BigDecimal.ZERO;
		BigDecimal bdFinalSalaryAdvance = BigDecimal.ZERO;
		String balanceModified = "";

		List<GenericValue> payrollElementELI = null;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId",
							"SALARYADVANCE"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdsalaryAdvanceAmount = getElementAmount(payrollElement, staffPayrollId, delegator);
			bdsalaryAdvanceBalance = getSalaryAdvanceBalance(payrollElement, staffPayrollId,
					delegator);
			balanceModified = getBalanceModified(payrollElement, staffPayrollId, delegator);
		}
		if (!balanceModified.equals("Y")) {
			bdFinalSalaryAdvance = bdsalaryAdvanceBalance;
		} else {
			bdFinalSalaryAdvance = bdsalaryAdvanceAmount
					.add(bdsalaryAdvanceBalance);
		}

		bdlowIntBen = bdFinalSalaryAdvance.multiply(getPrescribedInterestRate(
				employee, delegator));

		return bdlowIntBen;
	}

	private static BigDecimal getPrescribedInterestRate(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdPrescribedIntRate = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdPrescribedIntRate = getPrescribedInterestRateAmount(
					payrollConstant, delegator).divide(new BigDecimal(1200),
					RoundingMode.HALF_UP);
		}

		return bdPrescribedIntRate;
	}

	private static BigDecimal getPrescribedInterestRateAmount(
			GenericValue payrollConstant, Delegator delegator) {
		// Do the real getting
		BigDecimal bdlowInterstRate = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			long lir = staffPayrollConstants.getLong("lowInterstRate");
			bdlowInterstRate = new BigDecimal(new Long(lir).intValue());
		}
		return bdlowInterstRate;
	}

	private static String getBalanceModified(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		String ismodified = "";
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			ismodified = staffPayrollElement.getString("valueChanged");
		}
		return ismodified;
	}

	private static BigDecimal getSalaryAdvanceBalance(
			GenericValue payrollElement, String staffPayrollId, Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			bdAmount = bdAmount.add(staffPayrollElement
					.getBigDecimal("balance"));
		}
		return bdAmount;
	}

	private static BigDecimal calcExcessPensionBen(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		BigDecimal bdEPB = BigDecimal.ZERO;

		bdNSSFStatutory = getNSSFStatutory(employee, delegator);
		log.info("######### NSSF Statutory " + bdNSSFStatutory);
		bdNSSFVoluntary = getNSSFVolAmount(employee, delegator, staffPayrollId);
		log.info("######### NSSF Voluntary " + bdNSSFVoluntary);
		bdPensionAmt = getPensionAmount(employee, delegator, staffPayrollId);
		log.info("######### Pension " + bdPensionAmt);
		bdMAX_PENSION_CONTRIBUTION = getMaxPensionContribution(employee,
				delegator);
		log.info("######### Max Pension " + bdMAX_PENSION_CONTRIBUTION);

		bdEPB = (bdNSSFStatutory.multiply(new BigDecimal(2))).add(
				bdNSSFVoluntary).add(bdPensionAmt.multiply(new BigDecimal(3)))
				.subtract(bdMAX_PENSION_CONTRIBUTION);

		if (bdEPB.intValue() <= 0) {
			bdEPB = BigDecimal.ZERO;
		}

		log.info("######### EPB " + bdEPB);
		bdEXCESSPENSIONBENEFIT = bdEPB;
		return bdEPB;
	}

	private static BigDecimal getMaxPensionContribution(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdMAxPensionAmount = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdMAxPensionAmount = getMaxPensionAmount(payrollConstant, delegator);
		}

		return bdMAxPensionAmount;
	}

	private static BigDecimal getMaxPensionAmount(GenericValue payrollConstant,
			Delegator delegator) {
		BigDecimal bdMaxPension = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdMaxPension = staffPayrollConstants
					.getBigDecimal("pension_maxContibution");
		}
		return bdMaxPension;
	}

	private static BigDecimal getPensionAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> nssfVolELI = null;
		BigDecimal bdPensionAmount = BigDecimal.ZERO;
		try {
			nssfVolELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : nssfVolELI) {
			// Get the amount

			bdPensionAmount = getPensionPercentage(employee, delegator)
					.multiply(bdBasicPay);
		}

		return bdPensionAmount;
	}

	private static BigDecimal getBasicPay(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdBasicPy = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId",
							"BASICPAY"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdBasicPy = getElementAmount(payrollElement, staffPayrollId, delegator);
		}

		return bdBasicPy;
	}

	private static BigDecimal getPensionPercentage(GenericValue staffPayroll,
			Delegator delegator) {
		BigDecimal bdPensionPercentage = BigDecimal.ZERO;
		List<GenericValue> pensionPercentgelELI = null;
		try {
			pensionPercentgelELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPension : pensionPercentgelELI) {
			// Get the amount

			bdPensionPercentage = getpensionPercentage(staffPension, delegator)
					.divide(new BigDecimal(100), RoundingMode.HALF_UP);
			log.info("######### Pension % " + bdPensionPercentage);
		}

		return bdPensionPercentage;
	}

	private static BigDecimal getpensionPercentage(GenericValue staffPension,
			Delegator delegator) {
		BigDecimal bdPesionPercentage = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPension.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue pensionPertge : staffPayrollELI) {
			// Get the amount
			bdPesionPercentage = pensionPertge
					.getBigDecimal("pensionpercentage");
		}
		return bdPesionPercentage;
	}

	private static BigDecimal getNSSFVolAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> nssfVolELI = null;
		BigDecimal bdNSSFVolAmount = BigDecimal.ZERO;
		try {
			nssfVolELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : nssfVolELI) {
			// Get the amount

			bdNSSFVolAmount = getNSSFVolAmount(payrollConstant, delegator);
		}

		return bdNSSFVolAmount;
	}

	private static BigDecimal getNSSFVolAmount(GenericValue staffPayroll,
			Delegator delegator) {
		BigDecimal bdNSSFVolAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue nssfVolAmnt : staffPayrollELI) {
			// Get the amount
			bdNSSFVolAmount = bdNSSFVolAmount.add(nssfVolAmnt
					.getBigDecimal("nssfVolAmount"));
		}
		return bdNSSFVolAmount;
	}

	private static BigDecimal getNSSFStatutory(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdNSSF = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdNSSF = getNSSFAmount(payrollConstant, delegator);
		}

		return bdNSSF;
	}

	private static BigDecimal sumPayments(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdGross = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("elementType", "Payment"),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdGross = bdGross.add(getElementAmount(payrollElement, staffPayrollId, delegator));
		}

		return bdGross;
	}

	/**
	 * @author charles
	 * **/
	private static BigDecimal sumDeductions(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdOtherDeduction = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("elementType", "Deduction"),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdOtherDeduction = bdOtherDeduction.add(getElementAmount(
					payrollElement, staffPayrollId, delegator));
		}

		return bdOtherDeduction;
	}

	private static BigDecimal getElementAmount(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			bdAmount = bdAmount
					.add(staffPayrollElement.getBigDecimal("amount"));
		}
		return bdAmount;
	}

	private static BigDecimal getNSSFAmount(GenericValue payrollConstant,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdNSSFAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdNSSFAmount = staffPayrollConstants
					.getBigDecimal("staticnssfAmount");
		}
		return bdNSSFAmount;
	}

	private static BigDecimal computeGrossTax(BigDecimal bdTaxablePay,
			Delegator delegator) {
		BigDecimal grossTax = BigDecimal.ZERO;
		BigDecimal bdLowerBracket, bdUpperBracket = BigDecimal.ZERO;
		BigDecimal bdPercentage = BigDecimal.ZERO;

		// Get the TaxTable
		List<GenericValue> payeTableELI = new LinkedList<GenericValue>();
		try {
			payeTableELI = delegator.findList("PAYETable", null, null, UtilMisc
					.toList("lowerbracket"), null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdGrowth;
		BigDecimal bdNewTaxable = bdTaxablePay;
		List<TaxTracker> listTaxTracker = new ArrayList<TaxTracker>();
		TaxTracker taxTracker;
		Long count = 0l;
		for (GenericValue genericValue : payeTableELI) {
			count++;
			bdLowerBracket = genericValue.getBigDecimal("lowerbracket");
			bdUpperBracket = genericValue.getBigDecimal("upperbracket");
			bdPercentage = genericValue.getBigDecimal("percentage");
			log.info("###############################");
			log.info("Lower Value:" + bdLowerBracket);
			log.info("Upper Value:" + bdUpperBracket);
			log.info("Percentage Value:" + bdPercentage);
			taxTracker = new TaxTracker();
			bdGrowth = bdUpperBracket.subtract(bdLowerBracket);
			if (!(bdNewTaxable.compareTo(bdGrowth) == -1)) {
				taxTracker.setTaxBase(bdGrowth);
				bdNewTaxable = bdNewTaxable.subtract(bdGrowth);
			} else {
				taxTracker.setTaxBase(bdNewTaxable);
				bdNewTaxable = BigDecimal.ZERO;
			}
			bdPercentage = bdPercentage.multiply(new BigDecimal(0.01));
			taxTracker.setTaxPercent(bdPercentage);
			taxTracker.setCount(count);
			log.info("########## Percentage Divided by 100 : " + bdPercentage);
			listTaxTracker.add(taxTracker);

		}

		// Cummulate the PAYE
		for (TaxTracker taxTracker2 : listTaxTracker) {
			grossTax = grossTax.add(taxTracker2.getTaxBase().multiply(
					taxTracker2.getTaxPercent()));
		}

<<<<<<< HEAD
		log.info("##########Gross Tax :" +grossTax);
		return grossTax;
	}

	private static BigDecimal computeNHIF(GenericValue employee, Delegator delegator,
			BigDecimal bdBasicPay) {
=======
		log.info("##########Gross Tax :" + grossTax);
		return grossTax;
	}

	private static BigDecimal computeNHIF(GenericValue employee,
			Delegator delegator, BigDecimal bdBasicPay) {
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744
		BigDecimal bdLowerBracket, bdUpperBracket = BigDecimal.ZERO;

		// Get the TaxTable
		List<GenericValue> nhifTableELI = new LinkedList<GenericValue>();
		try {
			nhifTableELI = delegator.findList("NHIFTable", null, null, UtilMisc
					.toList("lowerbracket"), null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal contribution = BigDecimal.ZERO;

		for (GenericValue genericValue : nhifTableELI) {
			bdLowerBracket = genericValue.getBigDecimal("lowerbracket");
			bdUpperBracket = genericValue.getBigDecimal("upperbracket");

<<<<<<< HEAD
			if ((!(bdBasicPay.compareTo(bdLowerBracket) == -1)) && (!(bdBasicPay.compareTo(bdUpperBracket) == 1 ))){
=======
			if ((!(bdBasicPay.compareTo(bdLowerBracket) == -1))
					&& (!(bdBasicPay.compareTo(bdUpperBracket) == 1))) {
>>>>>>> 1815ed96357c3af7b69e0ef97d456f95af1c4744

				contribution = genericValue.getBigDecimal("contribution");
			}
		}
		log.info("######### NHIF " + contribution);
		return contribution;
	}

	private static BigDecimal getLoanElementAmount(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", EntityCondition.makeCondition(
							"payrollElementId", payrollElement
									.getString("payrollElementId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			if (staffPayrollElement.getString("isfixed").equals("Y")) {

			} else {

			}
		}
		return bdAmount;
	}

	/***
	 * @author charles Get the fixed Interest
	 * */
	private static void processFixedInterest(Delegator delegator,
			GenericValue payrolElement, String staffPayrollId) {
		// Save the amount to staffPayrollElements
		/***
		 * payrollElementId staffPayrollId amount balance
		 *
		 *
		 * */
		// GenericValue interestPayrolElement =
		// delegator.makeValidValue("PayrollElement", )
		String staffPayrollElementsId = delegator
				.getNextSeqId("StaffPayrollElements");
		BigDecimal bdInterestAmt = payrolElement
				.getBigDecimal("interestamount");
		GenericValue interestPayrolElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsId", staffPayrollElementsId,
						"payrollElementId", payrolElement.getString("childId"),
						"staffPayrollId", staffPayrollId, "amount",
						bdInterestAmt));
		log.info("Fixed  Interest Amount :: " + bdInterestAmt);

		try {
			delegator.createOrStore(interestPayrolElement);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	/***
	 * @author charles Get/Compute the variable interest
	 * */
	private static void processVariableInterest(Delegator delegator,
			GenericValue payrolElement, String staffPayrollId) {
		// Get the rate, Get the frequency, Get the Balance before deduction,
		// multiply rate by balance then save the new value
		// BigDecimal bdBalanceAmt =
		GenericValue staffPayrollElement = getStaffPayrolElement(payrolElement
				.getString("payrollElementId"), staffPayrollId, delegator);

		/****
		 * staffPayrollElementsId staffPayrollId payrollElementId amount
		 *
		 *
		 * */
		BigDecimal bdInterestAmt = computeInterestAmount(staffPayrollElement,
				payrolElement, delegator);
		log.info("################ Variable Interest Amount :: "
				+ bdInterestAmt);

		String staffPayrollElementsId = delegator
				.getNextSeqId("StaffPayrollElements");
		GenericValue interestPayrolElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsId", staffPayrollElementsId,
						"payrollElementId", payrolElement.getString("childId"),
						"staffPayrollId", staffPayrollId, "amount",
						bdInterestAmt));
		try {
			delegator.createOrStore(interestPayrolElement);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}

	private static BigDecimal computeInterestAmount(
			GenericValue staffPayrollElement, GenericValue payrolElement,
			Delegator delegator) {

		BigDecimal bdFinalBalance = BigDecimal.ZERO, bdFinalInterestAmt = BigDecimal.ZERO;

		if ((staffPayrollElement != null) && (staffPayrollElement.getString("valueChanged") != null)
				&& (staffPayrollElement.getString("valueChanged").equals("Y"))) {
			bdFinalBalance = staffPayrollElement.getBigDecimal("amount").add(
					staffPayrollElement.getBigDecimal("balance"));
		} else if ((staffPayrollElement != null) && (staffPayrollElement.getString("valueChanged") != null)
				&& (staffPayrollElement.getString("valueChanged").equals("N"))) {
			bdFinalBalance = staffPayrollElement.getBigDecimal("balance");
		}
		log.info("################ Interest Balance :: " + bdFinalBalance);

		// PM Interest
		// BigDecimal bdInterestRate =
		// (payrolElement.getBigDecimal("interestrate") == null) ?
		// BigDecimal.ZERO : payrolElement.getBigDecimal("interestrate");
		BigDecimal bdInterestRate;
		if (payrolElement.getBigDecimal("interestrate") == null) {
			bdInterestRate = BigDecimal.ZERO;
		} else {
			bdInterestRate = payrolElement.getBigDecimal("interestrate");
		}

		if ((payrolElement.getString("interestfrequency") != null)
				&& (payrolElement.getString("interestfrequency").equals("pa"))) {
			bdInterestRate = bdInterestRate.divide(new BigDecimal(1200), 6,
					RoundingMode.HALF_UP).setScale(6, RoundingMode.HALF_UP);
		} else if ((payrolElement.getString("interestfrequency") != null)
				&& (payrolElement.getString("interestfrequency").equals("pm"))) {
			bdInterestRate = (BigDecimal) bdInterestRate.divide(
					new BigDecimal(100), 6, RoundingMode.HALF_UP).setScale(6,
					RoundingMode.HALF_UP);
		}
		log.info("################ Interest Rate :: " + bdInterestRate);

		bdFinalInterestAmt = bdFinalBalance.multiply(bdInterestRate).setScale(
				6, RoundingMode.HALF_UP);
		//
		return bdFinalInterestAmt;
	}

	/***
	 * @author charles Get the StaffPayrolElement given the payrollElementId and
	 *         staffPayrollId
	 * */
	private static GenericValue getStaffPayrolElement(String payrollElementId,
			String staffPayrollId, Delegator delegator) {

		List<GenericValue> staffPayrolElementsELI = new LinkedList<GenericValue>();

		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElementId)), EntityOperator.AND);

		try {
			staffPayrolElementsELI = delegator.findList("StaffPayrollElements",
					staffPayrolElementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue staffPayrolElement = null;
		for (GenericValue genericValue : staffPayrolElementsELI) {
			staffPayrolElement = genericValue;
		}

		return staffPayrolElement;
	}

}
 */
