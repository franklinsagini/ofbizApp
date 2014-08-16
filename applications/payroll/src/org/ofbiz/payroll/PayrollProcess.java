package org.ofbiz.payroll;

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
import org.ofbiz.webapp.event.EventHandlerException;


/**
 * @author charles
 * **/
public class PayrollProcess {
	static BigDecimal bdNSSFStatutory = BigDecimal.ZERO;
	static BigDecimal bdNSSFVoluntary = BigDecimal.ZERO;
	static BigDecimal bdPensionAmt = BigDecimal.ZERO;
	static BigDecimal bdMAX_PENSION_CONTRIBUTION = BigDecimal.ZERO;
	static BigDecimal bdBasicPay = BigDecimal.ZERO;

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
		BigDecimal bdGrossPay=BigDecimal.ZERO;
		BigDecimal bdTaxablePay=BigDecimal.ZERO;
		BigDecimal bdGrossTax=BigDecimal.ZERO;
		BigDecimal bdNHIF=BigDecimal.ZERO;
		BigDecimal bdTotRelief=BigDecimal.ZERO;
		
		bdBasicPay = getBasicPay(employee, delegator);
		log.info("######### Basic Pay Amount " + bdBasicPay);
		bdGrossPay = sumPayments(employee, delegator);
		log.info("######### Gross Pay Amount " + bdGrossPay);

		bdTaxablePay = calcTaxablePay(employee, delegator, bdGrossPay,
				staffPayrollId);
		log.info("######### Taxable Pay Amount " + bdTaxablePay);

		bdGrossTax=computeGrossTax(bdTaxablePay, delegator);
		bdNHIF=computeNHIF(employee, delegator, bdBasicPay);
		bdTotRelief=getTotalRelief(employee, delegator);

		
		
		
		// Save Gross Pay
		String staffPayrollElementsSequenceId = delegator
				.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsSequenceId",
						staffPayrollElementsSequenceId, "payrollElementId",
						"GROSSPAY", "amount", bdGrossPay, "staffPayrollId",
						staffPayrollId));
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
	}

	private static BigDecimal getTotalRelief(GenericValue employee,
			Delegator delegator) {
		BigDecimal bdtotalRelief=BigDecimal.ZERO;
		BigDecimal bdInsuranceRelief=BigDecimal.ZERO;
		BigDecimal bdMPR=BigDecimal.ZERO;
		
		bdInsuranceRelief=getInsuranceRelief(employee, delegator);
		bdMPR=getMPR(employee, delegator);
		log.info("######### Insurance Relief " + bdInsuranceRelief);
		log.info("######### MPR " + bdMPR);
		bdtotalRelief=bdInsuranceRelief.add(bdMPR);
		
		
		return bdtotalRelief;
	}

	private static BigDecimal getMPR(GenericValue employee, Delegator delegator) {
		BigDecimal bdMPR = BigDecimal.ZERO;
		
		List<GenericValue> payrollConstantELI = null;

		try {
			payrollConstantELI = delegator.findList("PayrollConstants",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdMPR = getMPRAmount(payrollConstant, delegator);
		}
		return bdMPR;
	}

	private static BigDecimal getInsuranceRelief(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdInsRelief = BigDecimal.ZERO;
		
		EntityConditionList<EntityExpr> elementConditions = EntityCondition
		 .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		 "insurancecontribution", EntityOperator.EQUALS,
		 "Y"), EntityCondition.makeCondition(
		 "hasRelief", EntityOperator.EQUALS,
		 "Y")), EntityOperator.AND);
		
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					elementConditions,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdInsRelief = bdInsRelief.add(getElementAmount(payrollElement, delegator));
		}
//		bdInsRelief 

		return bdInsRelief;
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
		bdLowInterestBenefit = calcLowInterestBen(employee, delegator);
		bdDisabilityAllowance = getDisabilityAllowance(employee, delegator);
		bdNonTaxableAmounts = getNonTaxableAmounts(employee, delegator);

		bdTaxableIncome = bdGrossPay.add(bdExcessPensionBenefit).add(
				bdLowInterestBenefit).subtract(bdNSSFStatutory).subtract(
				bdNSSFVoluntary).subtract(bdPensionAmt).subtract(
				bdDisabilityAllowance).subtract(bdNonTaxableAmounts);

		if (bdTaxableIncome.intValue() <= 0) {
			bdTaxableIncome = BigDecimal.ZERO;
		}

		return bdTaxableIncome;
	}

	private static BigDecimal getNonTaxableAmounts(GenericValue employee,
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
					payrollElement, delegator));
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
			bdDisabltyAllowanceAmount = staffPayrollConstants.getBigDecimal("disability_relief");
		}
		return bdDisabltyAllowanceAmount;
	}
	
	private static BigDecimal getMPRAmount(
			GenericValue payrollConstant, Delegator delegator) {
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
			bdMPRAmount = staffPayrollConstants.getBigDecimal("monthlyPersonalRelief");
		}
		return bdMPRAmount;
	}

	private static BigDecimal calcLowInterestBen(GenericValue employee,
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

			bdsalaryAdvanceAmount = getElementAmount(payrollElement, delegator);
			bdsalaryAdvanceBalance = getSalaryAdvanceBalance(payrollElement,
					delegator);
			balanceModified = getBalanceModified(payrollElement, delegator);
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

	private static String getBalanceModified(GenericValue payrollElement,
			Delegator delegator) {
		// Do the real getting
		String ismodified = "";
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
			ismodified = staffPayrollElement.getString("valueChanged");
		}
		return ismodified;
	}

	private static BigDecimal getSalaryAdvanceBalance(
			GenericValue payrollElement, Delegator delegator) {
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

	private static BigDecimal getBasicPay(GenericValue employee,
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

			bdBasicPy = getElementAmount(payrollElement, delegator);
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

	private static BigDecimal sumPayments(GenericValue employee,
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

			bdGross = bdGross.add(getElementAmount(payrollElement, delegator));
		}

		return bdGross;
	}

	private static BigDecimal getElementAmount(GenericValue payrollElement,
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
			bdAmount = bdAmount.add(staffPayrollElement.getBigDecimal("amount"));
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
			log.info("########## Percentage Divided by 100 : " +bdPercentage);
			listTaxTracker.add(taxTracker);

		}

		// Cummulate the PAYE
		for (TaxTracker taxTracker2 : listTaxTracker) {
			grossTax = grossTax.add(taxTracker2.getTaxBase().multiply(
					taxTracker2.getTaxPercent()));
		}
		
		log.info("##########Gross Tax :" +grossTax);
		return grossTax;
	}
	
	private static BigDecimal computeNHIF(GenericValue employee, Delegator delegator,
			BigDecimal bdBasicPay) {
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
			
			if ((!(bdBasicPay.compareTo(bdLowerBracket) == -1)) && (!(bdBasicPay.compareTo(bdUpperBracket) == 1 ))){
				
				contribution = genericValue.getBigDecimal("contribution");
			}
		}
		log.info("######### NHIF " + contribution);
		return contribution;
	}

}
