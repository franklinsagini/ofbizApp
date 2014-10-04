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
import org.ofbiz.payroll.TaxTracker;
import org.ofbiz.webapp.event.EventHandlerException;


/**
 * @author charles
 * **/
public class MigratePayroll {
	private static Logger log = Logger.getLogger(PayrollProcess.class);

	public static String migratePayroll(HttpServletRequest request,
			HttpServletResponse response) {
		String newPayrollPeriodId="";
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String oldPayrollPeriodId = (String) request.getParameter("payrollPeriodId");
		
		// Get employees
		List<GenericValue> employeesELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### Old payrollPeriodId is :::: " + oldPayrollPeriodId);
		
		newPayrollPeriodId = getNewPayrollPeriodID(oldPayrollPeriodId, delegator);
		log.info("######### New payrollPeriodId is :::: " + newPayrollPeriodId);

		try {
			employeesELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("payrollPeriodId", oldPayrollPeriodId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		

	for (GenericValue genericValue : employeesELI) {
			
			rollOverPayroll(genericValue, genericValue.getString("staffPayrollId"), newPayrollPeriodId, delegator);
			log.info("######### Staff ID "+ genericValue.getString("staffPayrollId"));
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

	private static String getNewPayrollPeriodID(String oldPayrollPeriodId,
			Delegator delegator) {
		List<GenericValue> payrollPeriodELI = null;
		String newPPID = "";
		try {
			payrollPeriodELI = delegator.findList("PayrollPeriod",
					EntityCondition.makeCondition("payrollPeriodId",
							oldPayrollPeriodId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollPeriod : payrollPeriodELI) {
			// Get the amount
			log.info("#########>>>>>>>>>1 2 pakb>>>>>>>>>>>>"+payrollPeriod.getLong(("sequence_no")));

			newPPID = getNewPeriod(payrollPeriod, payrollPeriod.getLong("sequence_no"), delegator);
		}

		return newPPID;
	}

	private static String getNewPeriod(GenericValue payrollPeriod,
			Long seq_No, Delegator delegator) {
		String pId="";
		List<GenericValue> periodELI = new LinkedList<GenericValue>();

		try {
			periodELI = delegator.findList("PayrollPeriod",	EntityCondition.makeCondition("sequence_no",
					seq_No+1), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue period : periodELI) {
			// Get the amount
			log.info("######### 2>>>>>>>>>>>>>>>>>>"+period.getString("payrollPeriodId"));
			pId = period.getString("payrollPeriodId");
		}
		return pId;
	}

	private static void rollOverPayroll(GenericValue employee,
			String staffPayrollId, String newPayrollPeriodId, Delegator delegator) {
//		log.info("######### Roll over starts here>>>>>>>>>>>>>>>>>>>>>");
		
		List<GenericValue> staffPayrollDetailsELI = null;
		try {
			staffPayrollDetailsELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPayrollId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollDetails : staffPayrollDetailsELI) {

			GenericValue newStaffPayroll =  createStaffToSave(delegator, staffPayrollDetails.getString("partyId"), 
					staffPayrollDetails.getBigDecimal("pensionpercentage"), 
					staffPayrollDetails.getBigDecimal("nssfVolAmount"), newPayrollPeriodId);
			try {
				delegator.store(newStaffPayroll);
				log.info("New Staff Payroll ID = >>>>>>>>>>>>"+newStaffPayroll.getString("staffPayrollId"));
				
				rollOverParameters(employee, staffPayrollId, newStaffPayroll.getString("staffPayrollId"), delegator);
				
				
				
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	private static void rollOverParameters(GenericValue employee,
			String OldStaffPayrollId, String NewStaffPayrollId, Delegator delegator) {
		List<GenericValue> staffPayrollElementDetailsELI = null;
		//staffpayroll - type != 'Sys Elem'
		
//		cont here!!!!!!!!!!!!
		
		
		
		EntityConditionList<EntityExpr> elementConditions = EntityCondition.makeCondition(UtilMisc.toList
				(EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS, OldStaffPayrollId), 
						EntityCondition.makeCondition("payrollElementId", EntityOperator.NOT_IN, getSystemElements(delegator))), EntityOperator.AND);
		
		try {
			staffPayrollElementDetailsELI = delegator.findList("StaffPayrollElement",
					elementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		List<GenericValue> listSystemElements = new ArrayList<GenericValue>();
		GenericValue systemElement;
		
		for (GenericValue staffPayrollElementDetails : staffPayrollElementDetailsELI) {

			listSystemElements.add(createElementToSave(delegator, staffPayrollElementDetails.getString("payrollElementId"), 
					staffPayrollElementDetails.getBigDecimal("amount"), staffPayrollElementDetails.getBigDecimal("balance"), NewStaffPayrollId));
		}
		try {
			delegator.storeAll(listSystemElements);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static List<GenericValue> getSystemElements(Delegator delegator) {
		List<GenericValue> systemElementELI = null;
		try {
			systemElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("elementType",
							"System Element"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			}
		return systemElementELI;
	}

	private static GenericValue createStaffToSave(Delegator delegator, String partyId, BigDecimal pensionPercentage, BigDecimal nssfVolAmt,
			String newPayrollPeriodId ){
		String staffPayrollSequenceId = delegator
		.getNextSeqId("StaffPayroll");

		GenericValue staffPayroll = delegator.makeValidValue(
				"StaffPayroll", UtilMisc.toMap(
						"staffPayrollId",
						staffPayrollSequenceId, "partyId",
						partyId, "pensionpercentage", pensionPercentage, "nssfVolAmount",
						nssfVolAmt, "payrollPeriodId", newPayrollPeriodId, "closed", "N"));
		try {
			staffPayroll = delegator
					.createSetNextSeqId(staffPayroll);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return staffPayroll;
	}
	
	private static GenericValue createElementToSave(Delegator delegator, String payrollElementId, BigDecimal elementAmount, BigDecimal elementBalance, 
			String staffPayrollId ){
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

}
