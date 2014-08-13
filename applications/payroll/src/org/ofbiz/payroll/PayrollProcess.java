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
		bdGrossPay = sumPayments(employee, delegator);
		log.info("######### Gross Pay Amount "+bdGrossPay);
		//Save Gross Pay
		String staffPayrollElementsSequenceId = delegator.getNextSeqId("StaffPayrollElements");
		
		GenericValue staffPayrolElement = delegator.makeValidValue("StaffPayrollElements", UtilMisc.toMap("staffPayrollElementsSequenceId", staffPayrollElementsSequenceId, "payrollElementId", "GROSSPAY", "amount", bdGrossPay, "staffPayrollId", staffPayrollId));
		try {
			staffPayrolElement = delegator.createSetNextSeqId(staffPayrolElement);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			delegator.createOrStore(staffPayrolElement);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
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
		
		for (GenericValue payrolElement : payrollElementELI) {
			//Get the amount
			
			bdGross = bdGross.add(getElementAmount(payrolElement, delegator));
		}
		
		return bdGross;
	}
	
	private static BigDecimal getElementAmount(GenericValue payrolElement, Delegator delegator){
		//Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollElementsELI = delegator.findList("StaffPayrollElements",
					EntityCondition.makeCondition("payrollElementId", payrolElement.getString("payrollElementId")), null,
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

}
