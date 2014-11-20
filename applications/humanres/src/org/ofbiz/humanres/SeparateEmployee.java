package org.ofbiz.humanres;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

//org.ofbiz.humanres.SeparateEmployee
public class SeparateEmployee {

	private static Logger log = Logger.getLogger(SeparateEmployee.class);
	
	public static String separateEmployee(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId =  (String) request.getParameter("partyId");
		
		List<GenericValue> sepELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### partyId is :::: " + partyId);


		try {
			sepELI = delegator.findList("SeparationApplication", EntityCondition
					.makeCondition("partyId", partyId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : sepELI) 
		{
			genericValue.setString("separated", "Y");
			genericValue.setString("status", "Separated");
			
//			updateLoginInfo(delegator, partyId);
			updatePersonalDetails(delegator, partyId);
			updatePayroll(delegator, partyId);
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	private static void updatePersonalDetails(Delegator delegator,
			String partyId) {
		List<GenericValue> persELI = null;
		try {
			persELI = delegator.findList("Person", EntityCondition
					.makeCondition("partyId", partyId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : persELI) 
		{
			genericValue.setString("isSeparated", "Y");
			genericValue.setString("employmentStatusEnumId", "EMPS_SEPARATED");
	
			try {
				delegator.createOrStore(genericValue);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private static void updatePayroll(Delegator delegator, String partyId) {
		List<GenericValue> payrollELI = null;
		String payrollPeriodId=getActivePayrollPeriodId(delegator);
		
		if(!(payrollPeriodId==null))
		{
			EntityConditionList<EntityExpr> staffPayrollConditions = EntityCondition
			 .makeCondition(UtilMisc.toList(
					 EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId), 
					 EntityCondition.makeCondition("payrollPeriodId", EntityOperator.EQUALS, payrollPeriodId)), 
					 EntityOperator.AND);
			
			try {
				payrollELI = delegator.findList("StaffPayroll", staffPayrollConditions, null,
						null, null, false);
			
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			if(!payrollELI.isEmpty())
			{
				for (GenericValue genericValue : payrollELI) 
				{
					deleteElements(genericValue.getString("staffPayrollId"), delegator); 

				}
			}
		}
			
			
		
		
	}

	private static void deleteElements(String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> StaffPayrollElementELI = null;
		try {
			StaffPayrollElementELI = delegator.findList("StaffPayrollElements",
					EntityCondition.makeCondition("staffPayrollId",staffPayrollId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		try {
			delegator.removeAll(StaffPayrollElementELI);
			
			deleteStaffPayroll(staffPayrollId, delegator);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void deleteStaffPayroll(String staffPayrollId, Delegator delegator) {
		List<GenericValue> StaffPayrollELI = null;
		try {
			StaffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",staffPayrollId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		try {
			delegator.removeAll(StaffPayrollELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static String getActivePayrollPeriodId(Delegator delegator) {
		String ppId=null;
		List<GenericValue> ppELI = null;
		try {
			ppELI = delegator.findList("PayrollPeriod", EntityCondition
					.makeCondition("status", "Open"), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : ppELI) 
		{
			ppId=genericValue.getString("payrollPeriodId");
		}
		return ppId;
	}
}

