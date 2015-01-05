package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

public class ClosePayroll {
	private static Logger log = Logger.getLogger(ClosePayroll.class);
	
	public static String closePayroll(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String oldPayrollPeriodId = (String) request.getParameter("payrollPeriodId");
		
		// Get employees
		List<GenericValue> employeesELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### Old payrollPeriodId is :::: " + oldPayrollPeriodId);


		try {
			employeesELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("payrollPeriodId", oldPayrollPeriodId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : employeesELI) 
		{
			genericValue.setString("closed", "Y");
			updatePayrollPeriodSummary(delegator, oldPayrollPeriodId);
			updatePayrollPeriod(delegator, oldPayrollPeriodId);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	private static void updatePayrollPeriod(Delegator delegator,
			String oldPayrollPeriodId) {
		List<GenericValue> summaryELI = null;
		try {
			summaryELI = delegator.findList("PayrollPeriod", EntityCondition
					.makeCondition("payrollPeriodId", oldPayrollPeriodId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : summaryELI) 
		{
			genericValue.setString("payrollcheck", "C");
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private static void updatePayrollPeriodSummary(Delegator delegator, String oldPayrollPeriodId) {
		List<GenericValue> summaryELI = null;
		try {
			summaryELI = delegator.findList("PayrollPeriodSummary", EntityCondition
					.makeCondition("payrollPeriodId", oldPayrollPeriodId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : summaryELI) 
		{
			genericValue.setString("closed", "Y");
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
