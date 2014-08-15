package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

/**
 * @author charles
 * **/
public class PayrollProcess {
	static BigDecimal bdNSSFStatutory=BigDecimal.ZERO;
	static BigDecimal bdNSSFVoluntary=BigDecimal.ZERO;
	static BigDecimal bdPensionAmt=BigDecimal.ZERO;
	static BigDecimal bdMAX_PENSION_CONTRIBUTION=BigDecimal.ZERO;
	static BigDecimal bdBasicPay = BigDecimal.ZERO; 
	
	private static Logger log = Logger.getLogger(PayrollProcess.class);
	
	public static String runPayroll(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		String payrollPeriodId = (String)request.getParameter("payrollPeriodId");
		//Get employees
		List<GenericValue> employeesELI = null;
		//String partyId = party.getString("partyId");
		log.info("######### payrollPeriodId is :::: "+payrollPeriodId);
		
		
		//Delegator delegator = DelegatorFactoryImpl.getDelegator(null); //= party.getDelegator();
		
//		EntityConditionList<EntityExpr> employeeConditions = EntityCondition
//		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
//				"partyId", EntityOperator.EQUALS,
//				partyId), EntityCondition.makeCondition(
//				"payrollPeriodId", EntityOperator.EQUALS,
//				periodId)), EntityOperator.AND);

//			try {
//				chequeDepositELI = delegator.findList("AccountTransaction",
//						transactionConditions, null, null, null, false);
//			
//			} catch (GenericEntityException e2) {
//				e2.printStackTrace();
//			}

		try {
			employeesELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("payrollPeriodId", payrollPeriodId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		log.info("######### Loop through Staff");
		
		for (GenericValue genericValue : employeesELI) {
			processPayroll(genericValue, genericValue.getString("staffPayrollId"), delegator);
			log.info("######### Staff ID "+genericValue.getString("staffPayrollId"));
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
	
	private static void processPayroll(GenericValue employee, String staffPayrollId,  Delegator delegator){
		//Compute Gross Pay
		BigDecimal bdGrossPay;
		BigDecimal bdTaxablePay;
		bdBasicPay = getBasicPay(employee, delegator);
		log.info("######### Basic Pay Amount "+bdBasicPay);
		bdGrossPay = sumPayments(employee, delegator);
		log.info("######### Gross Pay Amount "+bdGrossPay);
		
		bdTaxablePay = calcTaxablePay(employee, delegator, bdGrossPay, staffPayrollId);
		log.info("######### Taxable Pay Amount "+bdTaxablePay);
		
		
		
		
		
		
		
		//Save Gross Pay
		String staffPayrollElementsSequenceId = delegator.getNextSeqId("StaffPayrollElements");
		
		GenericValue staffPayrollElement = delegator.makeValidValue("StaffPayrollElements", UtilMisc.toMap("staffPayrollElementsSequenceId", staffPayrollElementsSequenceId, "payrollElementId", "GROSSPAY", "amount", bdGrossPay, "staffPayrollId", staffPayrollId));
		try {
			staffPayrollElement = delegator.createSetNextSeqId(staffPayrollElement);
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
	
	private static BigDecimal calcTaxablePay(GenericValue employee, Delegator delegator, BigDecimal bdGrossPay, String staffPayrollId) {
		BigDecimal bdTaxableIncome=BigDecimal.ZERO;
		BigDecimal bdExcessPensionBenefit=BigDecimal.ZERO;
		
		bdExcessPensionBenefit = calcExcessPensionBen(employee, delegator, staffPayrollId);
		
		
		
		return bdTaxableIncome;
	}

	private static BigDecimal calcExcessPensionBen(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		BigDecimal bdEPB=BigDecimal.ZERO;
		
		bdNSSFStatutory=getNSSFStatutory(employee, delegator);		                log.info("######### NSSF Statutory "+bdNSSFStatutory);
		bdNSSFVoluntary=getNSSFVolAmount(employee, delegator, staffPayrollId);		log.info("######### NSSF Voluntary "+bdNSSFVoluntary);
		bdPensionAmt=getPensionAmount(employee, delegator, staffPayrollId);		    log.info("######### Pension "+bdPensionAmt);
		bdMAX_PENSION_CONTRIBUTION=getMaxPensionContribution(employee, delegator);  log.info("######### Max Pension "+bdMAX_PENSION_CONTRIBUTION);
		
		bdEPB=(bdNSSFStatutory.multiply(new BigDecimal(2))).add(bdNSSFVoluntary).add(bdPensionAmt.multiply(new BigDecimal(3)))
		.subtract(bdMAX_PENSION_CONTRIBUTION);
		
		if(bdEPB.intValue()<=0)
		{
			bdEPB=BigDecimal.ZERO;
		}

		log.info("######### EPB "+bdEPB);
		return bdEPB;
	}

	private static BigDecimal getMaxPensionContribution(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdMAxPensionAmount = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants",
					null, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollConstant : payrollConstantELI) {
			//Get the amount
			
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
					EntityCondition.makeCondition("payrollConstantsId", payrollConstant.getString("payrollConstantsId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			//Get the amount
			bdMaxPension = staffPayrollConstants.getBigDecimal("pension_maxContibution");
		}
		return bdMaxPension;
	}

	private static BigDecimal getPensionAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> nssfVolELI = null;
		BigDecimal bdPensionAmount = BigDecimal.ZERO;
		try {
			nssfVolELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollConstant : nssfVolELI) {
			//Get the amount
			
			bdPensionAmount = getPensionPercentage(employee, delegator).multiply(bdBasicPay);
		}
		
		return bdPensionAmount;
	}

	private static BigDecimal getBasicPay(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdBasicPy = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId", "BASICPAY"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollElement : payrollElementELI) {
			//Get the amount
			
			bdBasicPy = getElementAmount(payrollElement, delegator);
		}
		
		return bdBasicPy;
	}

	private static BigDecimal getPensionPercentage(
			GenericValue staffPayroll, Delegator delegator) {
		BigDecimal bdPensionPercentage = BigDecimal.ZERO;
		List<GenericValue> pensionPercentgelELI = null;
		try {
			pensionPercentgelELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId", staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue staffPension : pensionPercentgelELI) {
			//Get the amount
			
			bdPensionPercentage =  new BigDecimal((getpensionPercentage(staffPension, delegator).intValue())/100);
		}
		
		return bdPensionPercentage;
	}

	private static BigDecimal getpensionPercentage(GenericValue staffPension,
			Delegator delegator) {
		BigDecimal bdPesionPercentage = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId", staffPension.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue pensionPertge : staffPayrollELI) {
			//Get the amount
			bdPesionPercentage = pensionPertge.getBigDecimal("pensionpercentage");
		}
		return bdPesionPercentage;
	}

	private static BigDecimal getNSSFVolAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> nssfVolELI = null;
		BigDecimal bdNSSFVolAmount = BigDecimal.ZERO;
		try {
			nssfVolELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollConstant : nssfVolELI) {
			//Get the amount
			
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
					EntityCondition.makeCondition("staffPayrollId", staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue nssfVolAmnt : staffPayrollELI) {
			//Get the amount
			bdNSSFVolAmount = bdNSSFVolAmount.add(nssfVolAmnt.getBigDecimal("nssfVolAmount"));
		}
		return bdNSSFVolAmount;
	}

	private static BigDecimal getNSSFStatutory(GenericValue employee, Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdNSSF = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants",
					null, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollConstant : payrollConstantELI) {
			//Get the amount
			
			bdNSSF = getNSSFAmount(payrollConstant, delegator);
		}
		
		return bdNSSF;
	}

	private static BigDecimal sumPayments(GenericValue employee, Delegator delegator){
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdGross = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("elementType", "Payment"), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue payrollElement : payrollElementELI) {
			//Get the amount
			
			bdGross = bdGross.add(getElementAmount(payrollElement, delegator));
		}
		
		return bdGross;
	}
	
	private static BigDecimal getElementAmount(GenericValue payrollElement, Delegator delegator){
		//Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollElementsELI = delegator.findList("StaffPayrollElements",
					EntityCondition.makeCondition("payrollElementId", payrollElement.getString("payrollElementId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			//Get the amount
			bdAmount = bdAmount.add(staffPayrollElement.getBigDecimal("amount"));
		}
		return bdAmount;
	}
	
	private static BigDecimal getNSSFAmount(GenericValue payrollConstant, Delegator delegator){
		//Do the real getting
		BigDecimal bdNSSFAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId", payrollConstant.getString("payrollConstantsId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			//Get the amount
			bdNSSFAmount = staffPayrollConstants.getBigDecimal("staticnssfAmount");
		}
		return bdNSSFAmount;
	}

}
