package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
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
		HttpSession session;
		session = request.getSession();
		Map<String, String> userLogin = (Map<String, String>) session.getAttribute("userLogin");


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
		
		//post
		//Get the items to post
		BigDecimal bdNSSF = BigDecimal.ZERO;
		BigDecimal bdNHIF = BigDecimal.ZERO;
		BigDecimal bdPENSION = BigDecimal.ZERO;
		BigDecimal bdPAYE = BigDecimal.ZERO;
		BigDecimal bdNETPAY = BigDecimal.ZERO;
		BigDecimal bdSalaries = BigDecimal.ZERO;
		
		String NSSFAccountId = "";
		String NHIFAccountId = "";
		String PENSIONAccountId = "";
		String PAYEAccountId = "";
		String NETPAYAccountId = "";
		String SalariesAccountId = "";
		
		List<GenericValue> payrollPeriodSummaryELI = null;
		try {
			payrollPeriodSummaryELI = delegator.findList("PayrollPeriodSummary",
					EntityCondition.makeCondition("payrollPeriodId", oldPayrollPeriodId),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		GenericValue payrollPeriod = null;
		for (GenericValue genericValue : payrollPeriodSummaryELI) {
			//station = genericValue;
			
			if (genericValue.getString("elementName").equals("NSSF")){
				bdNSSF = genericValue.getBigDecimal("TotalAmount");
			}
			
			else if (genericValue.getString("elementName").equals("NHIF")){
				bdNHIF = genericValue.getBigDecimal("TotalAmount");
			}
			
			else if (genericValue.getString("elementName").equals("PENSION")){
				bdPENSION = genericValue.getBigDecimal("TotalAmount");
			}

			else if (genericValue.getString("elementName").equals("PAYE")){
				bdPAYE = genericValue.getBigDecimal("TotalAmount");
			}
			
			else if (genericValue.getString("elementName").equals("NETPAY")){
				bdNETPAY = genericValue.getBigDecimal("TotalAmount");
			}
			
		}
		
		bdSalaries = bdNSSF.add(bdNHIF).add(bdPENSION).add(bdPAYE).add(bdNETPAY);
		
		//Get Account IDs 
		
		List<GenericValue> payrollPostingAccountELI = null;
		try {
			payrollPeriodSummaryELI = delegator.findList("PayrollPostingAccount",
					null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		GenericValue payrollPostingAccount = null;
		for (GenericValue genericValue : payrollPostingAccountELI) {
			//station = genericValue;
			
			if (genericValue.getString("payrollElement").equals("NSSF")){
				NSSFAccountId = genericValue.getString("glAccountId");
			}
			
			else if (genericValue.getString("payrollElement").equals("NHIF")){
				NHIFAccountId = genericValue.getString("glAccountId");
			}
			
			else if (genericValue.getString("payrollElement").equals("PENSION")){
				PENSIONAccountId = genericValue.getString("glAccountId");
			}

			else if (genericValue.getString("payrollElement").equals("PAYE")){
				PAYEAccountId = genericValue.getString("glAccountId");
			}
			
			else if (genericValue.getString("payrollElement").equals("NETPAY")){
				NETPAYAccountId = genericValue.getString("glAccountId");
			}
			
		}
		
		AccHolderTransactionServices.postPayrollSalariesHQ(userLogin, bdNSSF, bdNHIF, bdPENSION, bdPAYE, bdNETPAY, bdSalaries, NSSFAccountId, NHIFAccountId, PENSIONAccountId, PAYEAccountId, NETPAYAccountId, SalariesAccountId);
		
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
