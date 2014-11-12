package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.event.EventHandlerException;
import org.ofbiz.workflow.WorkflowServices;


public class LeaveServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);

/*  =============    close financial year leaves ==============   */

public static String closeFinacialYear(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> personsELI = null; 
		try {
			personsELI = delegator.findAll("LeaveBalancesView", true);
				
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String partyId ="", appointmentdate = ""; 
		for (GenericValue genericValue : personsELI) {
			partyId = genericValue.getString("partyId");
			appointmentdate = genericValue.getString("appointmentdate");

			if (appointmentdate !=null && appointmentdate !="") {
						log.info("-----++++++-----partyId-----------------" +appointmentdate);
			log.info("---------------------			appointmentdate--------------------------" +partyId);
			deleteExistingResetAnnualLost(delegator);
			deleteExistingLeaveOpeningBalances(delegator);
			calculateCarryOverLost(partyId, appointmentdate);// call method to calculate
			Leave.closeFinacialYearForCompassionate(request, response);
			}
			
			
		}
		//log.info("------------------------------------------------" +partyId);
		
		return partyId;
	}


	public static void calculateCarryOverLost(String partyId, String appointmentdate) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String partyIds = partyId;
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;		
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "ANNUAL_LEAVE"),
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		//log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	double  usedLeaveDays = 0, lostLeaveDays = 0 , carryOverLeaveDays = 0;
	double MAXCARRYOVER = 15;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			usedLeaveDays += genericValue.getDouble("leaveDuration");
		}
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	double accrualRate = 0; 
	GenericValue accrualRates = null;
		try {
			 accrualRates = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "ANNUAL_LEAVE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (accrualRates != null) {
			accrualRate = accrualRates.getDouble("accrualRate");
		} else {
			System.out.println("######## Accrual Rate not found #### ");
		}
		//========= ==============================//
		LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();
		int thisYear = today.getYear();
		String currentYear = Integer.toString(thisYear);
			//log.info(" FFFFFFFFFFF First Day "+firstDayOfYear.toDate());
		LocalDateTime accrueStart;
		LocalDateTime stappointmentdate = new LocalDateTime(appointmentdate);
		if(stappointmentdate.isBefore(firstDayOfYear)){
				accrueStart = firstDayOfYear;
		}
		else
		{
			accrueStart = stappointmentdate;
		}
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		PeriodType monthDay = PeriodType.months();
		Period difference = new Period(accrueStart, stCurrentDate, monthDay);
		int months = difference.getMonths();
		double accruedLeaveDay = months * accrualRate;
		double leaveBalances =  accruedLeaveDay - usedLeaveDays;
		if (leaveBalances > MAXCARRYOVER) {
			lostLeaveDays = leaveBalances - MAXCARRYOVER;
			carryOverLeaveDays = MAXCARRYOVER;		 	
		 } 
		 if (leaveBalances <= MAXCARRYOVER) {
		 	lostLeaveDays = 0;
			carryOverLeaveDays = leaveBalances;
		 }
		// Delete record if it was created before end of this year
		deleteExistingCarryOverLost(delegator, partyId, currentYear);
		//Create record afresh
		String carryOverLostId=null;
		if (UtilValidate.isEmpty(carryOverLostId)) {
			try {
				carryOverLostId = delegator.getNextSeqId("EmplCarryOverLost");
			} catch (IllegalArgumentException e) {
				return;
			}
		}
		GenericValue leavelog = delegator.makeValue("EmplCarryOverLost",
				"partyId", partyId,
				"financialYear", currentYear,
	            "accruedDays", accruedLeaveDay , 
	            "usedLeaveDays", usedLeaveDays,
	            "lostLeaveDays", lostLeaveDays, 
	            "carryOverLeaveDays", carryOverLeaveDays);
	try {
		delegator.create(leavelog);
	} catch (GenericEntityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		
	}
	public static void deleteExistingCarryOverLost(Delegator delegator, String partyId, String currentYear) {
		
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),null,null),EntityOperator.AND);

		/*try {
			List<GenericValue> ExistingCarryOverLost  = delegator.findList("EmplCarryOverLost",
					leaveConditions, null, null, null, false);
			
			for (GenericValue genericValue : ExistingCarryOverLost) {
				ExistingCarryOverLost.remove(genericValue);
			}
			
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}*/
		
    try {
             GenericValue ExistingCarryOverLost = delegator.findOne("EmplCarryOverLost", 
             	UtilMisc.toMap( "partyId", partyId), false);
             if (ExistingCarryOverLost != null && !ExistingCarryOverLost.isEmpty()) {
            	 ExistingCarryOverLost.remove();
             }
       } catch (GenericEntityException e) {
            //return ServiceUtil.returnError("Failed. " +e.getMessage());
    	   e.printStackTrace();
       }  
}
	
	public static String resetUnusedCarryOver(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> resetELI = null; 
		String partyIds = "";
		try {
			resetELI = delegator.findAll("EmplCarryOverLost", true);
				
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : resetELI) {
			String partyId = genericValue.getString("partyId");
			String financialYear = genericValue.getString("financialYear");
			Double resetLeaveDays = genericValue.getDouble("carryOverLeaveDays");
			//log.info("------------------------------------------------" +partyId);
			//log.info("------------------------------------------------" +financialYear);
				resetCarryOverLeaveDays(delegator, partyId, financialYear, resetLeaveDays);// call method to reset
		}
		return partyIds;
	}
	

public static void resetCarryOverLeaveDays(Delegator delegator, String partyId, String financialYear ,double resetLeaveDays) {

	double carryOverLeaveDays = 0;
	log.info("++++++++++++++++++++++++++++++" +partyId);
      try {
             GenericValue resetGV = delegator.findOne("EmplCarryOverLost", 
             	UtilMisc.toMap("partyId", partyId), false);
             if (resetGV != null && !resetGV.isEmpty()) {
            	 resetGV.put("carryOverLeaveDays", carryOverLeaveDays);            	 
            	 resetGV.put("resetLeaveDays", resetLeaveDays);
            	 resetGV.store();
             }
       } catch (GenericEntityException e) {
            e.printStackTrace();;
       }  
}
/* =========================  Generate leave balances ========================== */



	public static String generateLeaveBalances(HttpServletRequest request,
			HttpServletResponse response) {
		//Map<String, Object> result = FastMap.newInstance();

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> personsELI = null; // =
		deleteExistingLeaveBalance(delegator);
		Timestamp now = UtilDateTime.nowTimestamp();
		String thisYear=getCurrentYear(now);
		try {
			personsELI = delegator.findAll("LeaveBalancesView", true);
			log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+personsELI);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		//log.info("+++++++++++++++++++++++++++++++++" +personsELI);
		
		String partyId ="", appointmentdate = ""; 
		for (GenericValue genericValue : personsELI) {
			partyId = genericValue.getString("partyId");
			appointmentdate = genericValue.getString("appointmentdate");
			//log.info("===================="+partyId);
			//log.info("++++++++++++++++++++"+appointmentdate);
			calculateLeaveBalanceSave(partyId, appointmentdate, thisYear);
		}
		//log.info("------------------------------------------------" +partyId);
		return partyId;
	}


	
	public static void calculateLeaveBalanceSave(String partyId, String appointmentdate, String year) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		//String partyIds = partyId;
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;		
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS, year),
					EntityCondition.makeCondition("isDeductedFromAnnual",EntityOperator.EQUALS, "Y"),// changed leavetypeId to isDeductedFromAnnual
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		//log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	BigDecimal approvedLeaveSum =BigDecimal.ZERO;
	//double  usedLeaveDays = 0;
	BigDecimal lostLeaveDays = BigDecimal.ZERO;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			//approvedLeaveSum += genericValue.getLong("leaveDuration");
			approvedLeaveSum = approvedLeaveSum.add(genericValue.getBigDecimal("leaveDuration"));
		}
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	BigDecimal accrualRate = BigDecimal.ZERO; 
	BigDecimal carryOverLeaveDays= BigDecimal.ZERO; 
	GenericValue employeeLeaveType = null;
	GenericValue carryGV = null;
		try {
			employeeLeaveType = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "ANNUAL_LEAVE"), false);
			
			 carryGV = delegator.findOne("EmplCarryOverLost", 
		             	UtilMisc.toMap("partyId", partyId), false);
		           	log.info("++++++++++++++carryGV++++++++++++++++" +carryGV);
			
			
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			
		}
 
	       
		
		if (accrualRate != null) {

			accrualRate = employeeLeaveType.getBigDecimal("accrualRate");
			carryOverLeaveDays = carryGV.getBigDecimal("carryOverLeaveDays");
			
		} else {
			System.out.println("######## Accrual Rate not found #### ");
		}
		
		//========= ==============================//
		LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();
		
		log.info(" FFFFFFFFFFF First Day "+firstDayOfYear.toDate());
		LocalDateTime accrueStart;
		LocalDateTime stappointmentdate = new LocalDateTime(appointmentdate);
		if(stappointmentdate.isBefore(firstDayOfYear)){
			
			accrueStart = firstDayOfYear;
		}
		else
		{
			accrueStart = stappointmentdate;
		}
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		

		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(accrueStart, stCurrentDate, monthDay);

		int months = difference.getMonths();
		BigDecimal accruedLeaveDay = accrualRate.multiply(new BigDecimal(months));
		BigDecimal leaveBalances =  (accruedLeaveDay.subtract(approvedLeaveSum)).add(carryOverLeaveDays); 
		/*BigDecimal leaveBalances =  accruedLeaveDay.subtract(approvedLeaveSum); */


		GenericValue leavelog = delegator.makeValue("LeaveBalances",
				"partyId", partyId, 
	            "accruedDays", accruedLeaveDay.doubleValue() , 
	            "usedLeaveDays", approvedLeaveSum.doubleValue(),
	            "lostLeaveDays", lostLeaveDays.doubleValue(), 
	            "LeaveDaysCarriedOver", carryOverLeaveDays.doubleValue(), 
	            "availableLeaveDays", leaveBalances.doubleValue());
	try {
		delegator.create(leavelog);
	} catch (GenericEntityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		
	}



	private static void deleteExistingLeaveBalance(Delegator delegator) {
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("LeaveBalances");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED  ALL RECORDS!" );
		
	}
	
	// DELETE ALL RESET ANNUAL LEAVE DAYS
	
	private static void deleteExistingResetAnnualLost(Delegator delegator){
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("AnnualLeaveBalancesReset");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED  ALL RECORDS IN RESET LEAVE DAYS!");
		
	}
	
	// DELETE ALL LEAVE OPENING BALANCES
	private static void deleteExistingLeaveOpeningBalances(Delegator delegator){
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("EmplLeaveOpeningBalance");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED ALL LEAVE OPENING BALANCES!");
		
	}

public static Map getCarryoverUsed(Delegator delegator, Double leaveDuration, String partyId) {
	Map<String, Object> UsedDays = FastMap.newInstance();
	//double carryOverLeaveDays = 0;
	double leaveDurationRemainder = 0, carryOverRemain = 0, carryOverUsed = 0 ;

	log.info("++++++++++partyId++++++++++++++++++++" +partyId);
	log.info("++++++++++leaveDuration++++++++++++++++++++" +leaveDuration);
	GenericValue carryGV = null; //GenericValue result = null;

      try {
            carryGV = delegator.findOne("EmplCarryOverLost", 
             	UtilMisc.toMap("partyId", partyId), false);
           	log.info("++++++++++++++carryGV++++++++++++++++" +carryGV);
             }
       catch (GenericEntityException e) {
            e.printStackTrace();;
       }  
       double carryOverLeaveDays = carryGV.getDouble("carryOverLeaveDays");
       log.info("++++++++++++++++carryOverLeaveDays++++++++++++++" +carryOverLeaveDays);
       if (carryOverLeaveDays > leaveDuration) {
       	carryOverUsed = leaveDuration;
       	carryOverRemain = carryOverLeaveDays - leaveDuration;
       	leaveDurationRemainder = 0;
       }
       if (  leaveDuration >= carryOverLeaveDays) {
       	carryOverUsed = carryOverLeaveDays;
       	leaveDurationRemainder = leaveDuration - carryOverLeaveDays ;
       	carryOverRemain = 0 ;
       }
       UsedDays.put("carryOverUsed", carryOverUsed);
       UsedDays.put("leaveDurationRemainder", leaveDurationRemainder);
       log.info("=======result========" +UsedDays);
      try {
             GenericValue updateCarryOverGV = delegator.findOne("EmplCarryOverLost", 
             	UtilMisc.toMap("partyId", partyId), false);
             if (updateCarryOverGV != null && !updateCarryOverGV.isEmpty()) {
            	 updateCarryOverGV.put("carryOverLeaveDays", carryOverRemain);
            	 updateCarryOverGV.store();
             }
       } catch (GenericEntityException e) {
            e.printStackTrace();;
       }
	return UsedDays; 
	

      // return Map

}







	public static String forwardApplication(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
       GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String user = userLogin.getString("partyId");
		String approvalStatuslog = null;
		// =============== primary Keys     ============//
		
		String partyId = (String) request.getParameter("partyId");
		String leaveTypeId = (String) request.getParameter("leaveTypeId");
		String leaveId = (String) request.getParameter("leaveId");
		
		log.info(" LLLLLLLLLLLLLLL the leave ID is LLLLLLLLLLLLLLLLLLl "+leaveId);
		Timestamp fromDate = null;
		try {
			fromDate = new Timestamp(
					((Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")))).getTime());
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// =============== primary Keys     ============//
		
		List<GenericValue> leaveApplicationELI = null;
		GenericValue leave = null;
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>From Date : " + fromDate);

		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("leaveId",EntityOperator.EQUALS, leaveId)
						),
						EntityOperator.AND);
		
		/**
		 * 
		 * 
		 * EntityCondition.makeCondition("fromDate",
								EntityOperator.EQUALS, new java.sql.Date(fromDate.getTime()))
		 * */
		
		log.info(" Date : "+fromDate);
		log.info(" Leave Type : "+leaveTypeId);
		log.info(" Party : "+partyId);
		log.info(" leaveId : "+leaveId);
		log.info(" userLogin : "+user);

		try {
			leaveApplicationELI = delegator.findList("EmplLeave", leaveConditions, null, null, null, false);
			log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>We are here >>>>>>>>>>>>>>>>>>>>>");
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get Leave Application";
		}
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> leaveApplicationELI: " + leaveApplicationELI);
		//String documentApprovalId = null, workflowDocumentTypeId = null, organizationUnitId = null;
		for (GenericValue genericValue : leaveApplicationELI) {
			// Get Unit and Document
				leave = genericValue;
			}
			String organizationUnitId = leave.getString("organizationUnitId");
			String workflowDocumentTypeId = leave.getString("workflowDocumentTypeId");
			String documentApprovalId = leave.getString("documentApprovalId");
			double leaveDuration = leave.getDouble("leaveDuration");
			Map carryOverLeaveDaysUsed = null;
			GenericValue documentApproval = null; GenericValue leavelog = null;
			documentApproval =  WorkflowServices.doFoward(delegator, organizationUnitId,	workflowDocumentTypeId, documentApprovalId);
		log.info("=====================" +documentApproval);

		if (documentApproval == null) {
			// Leave Approved
			result.put("fowardMessage", "");
		} else {
			leave.set("documentApprovalId", documentApproval.getString("documentApprovalId"));

			
				leave.set("approvalStatus", documentApproval.getString("stageAction"));
				leave.set("applicationStatus",documentApproval.getString("stageAction"));
				leave.set("approvalStatus" , documentApproval.getString("stageAction"));
				approvalStatuslog = documentApproval.getString("stageAction");
					// Employee to go for leave.
				carryOverLeaveDaysUsed = getCarryoverUsed(delegator, leaveDuration, partyId);
				//log.info("gggggggggggg            ggggggggggggggggg" +carryOverLeaveDaysUsed);
				if (carryOverLeaveDaysUsed != null) {
					
					leave.set("carryOverUsed", carryOverLeaveDaysUsed.get("carryOverUsed"));
					leave.set("leaveDuration", carryOverLeaveDaysUsed.get("leaveDurationRemainder"));
					log.info("PPPPPPPPPPPPPPPPPPPPPPPP        carryOverLeaveDaysUsed" +carryOverLeaveDaysUsed);
					log.info("gggggggggggg            leaveDurationRemainder" +carryOverLeaveDaysUsed.get("leaveDurationRemainder"));
				}


			leavelog = delegator.makeValue("LeaveStatusLog", "leaveStsLogId", delegator.getNextSeqId("LeaveStatusLog"), 
            "approvedBy", userLogin.getString("partyId"), 
            "nextApprover", documentApproval.getString("responsibleEmployee"),
            "partyId", partyId, 
            "leaveId", leaveId, "approvalStatus" ,approvalStatuslog);
        

			try {
				leavelog.create();
			} catch (GenericEntityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			// Set Responsible
			// responsibleEmployee

			leave.set("responsibleEmployee",	documentApproval.getString("responsibleEmployee"));
			try {
				delegator.createOrStore(leave);
				//delegator.create("LeaveStatusLog", leavelog);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			result.put("fowardMessage",	documentApproval.getString("stageAction"));
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(result.get("fowardMessage").toString());
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";// result.get("fowardMessage").toString();

	}
	

	public static String getWorkflowDocumentType(String documentName) {
		Map<String, Object> result = FastMap.newInstance();
		log.info("What we got the Document Name ############ " + documentName);

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> workflowDocumentTypeELI = null; // =

		try {
			workflowDocumentTypeELI = delegator.findList(
					"WorkflowDocumentType",
					EntityCondition.makeCondition("name", documentName), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String workflowDocumentTypeId = "";
		for (GenericValue genericValue : workflowDocumentTypeELI) {
			workflowDocumentTypeId = genericValue
					.getString("workflowDocumentTypeId");
		}

		result.put("workflowDocumentTypeId", workflowDocumentTypeId);
		return workflowDocumentTypeId;
	}


		public static String getEmplUnit(GenericValue person) {
		String organizationUnitId = "";
		String partyId = person.getString("partyId");

		Delegator delegator = person.getDelegator();

		List<GenericValue> getEmplUnitELI = null;
		try {
			getEmplUnitELI = delegator.findList("UnitEmployeeMap",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueUnit : getEmplUnitELI) {
			organizationUnitId = genericValueUnit.getString("organizationUnitId");
		}
		// ///////////////////////////////////////////////////

		return organizationUnitId;

	}

	public static String getLeaveAppointmentDate(GenericValue person) {
		String appointmentdate = "";

		String partyId = person.getString("partyId");

		Delegator delegator = person.getDelegator();

		List<GenericValue> getLeaveappointmentdateELI = null;

		try {
			getLeaveappointmentdateELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : getLeaveappointmentdateELI) {
			appointmentdate = genericValue.getString("appointmentdate");
		}

		return appointmentdate;

	}
	
	/*public static String getLeaveappointmentdate(GenericValue person) {
		String appointmentdate = "";

		String partyId = person.getString("partyId");

		Delegator delegator = person.getDelegator();

		List<GenericValue> getappointmentdateELI = null;

		try {
			getappointmentdateELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : getappointmentdateELI) {
			appointmentdate = genericValue.getString("appointmentdate");
		}

		return appointmentdate;

	}*/

	public static String getpartyIdFrom(GenericValue party) {
		String partyIdFromV = "";

		String partyId = party.getString("partyId");

		Delegator delegator = party.getDelegator();

		List<GenericValue> employmentsELI = null;

		try {
			employmentsELI = delegator.findList("Employment",
					EntityCondition.makeCondition("partyIdTo", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : employmentsELI) {
			partyIdFromV = genericValue.getString("partyIdFrom");
		}

		return partyIdFromV;
	}
	
	public static String getCurrentYear(Date fromDate) {
		LocalDateTime today = new LocalDateTime(fromDate);
		int thisYear = today.getYear();
		String currentYear = Integer.toString(thisYear);
		log.info("==================CURRENT YEAR ############ " + currentYear);
		return currentYear;
	}


	public static String getSupervisorLevel(GenericValue party) {
		String superVisorLevelValue = "";

		// GenericValue superVisorLevel = null;
		String partyId = party.getString("partyId");

		Delegator delegator = party.getDelegator();


		List<GenericValue> levelsELI = null; // =

		try {
			levelsELI = delegator.findList("SupervisorLevel",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : levelsELI) {
			superVisorLevelValue = genericValue.getString("supervisorLevel");
		}

		// superVisorLevelValue = superVisorLevel.getString("supervisorLevel");

		return superVisorLevelValue;
	}

	public static Map<String, Object> addStaffOpeningBalance(DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = ctx.getDelegator();
		Timestamp now = UtilDateTime.nowTimestamp();
		List<GenericValue> toBeStored = FastList.newInstance();
		Locale locale = (Locale) context.get("locale");
		// in most cases userLogin will be null, but get anyway so we can keep
		// track of that info if it is available
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = (String) context.get("partyId");

		// if specified partyId starts with a number, return an error
		/*if (UtilValidate.isNotEmpty(leaveBalanceId) && leaveBalanceId.matches("\\d+")) {
			return ServiceUtil.returnError(UtilProperties.getMessage(
					null, "party.id_is_digit", locale));
		}

		// partyId might be empty, so check it and get next seq party id if
		// empty
		if (UtilValidate.isEmpty(leaveBalanceId)) {
			try {
				leaveBalanceId = delegator.getNextSeqId("EmplLeaveOpeningBalance");
			} catch (IllegalArgumentException e) {
				return ServiceUtil.returnError(UtilProperties.getMessage(
						null, "party.id_generation_failure", locale));
			}
		}*/

		// check to see if party object exists, if so make sure it is PERSON
		// type party
		GenericValue EmplLeaveOpeningBalance = null;

		try {
			EmplLeaveOpeningBalance = delegator.findOne("EmplLeaveOpeningBalance",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e) {
			Debug.logWarning(e.getMessage(), null);
		}

	
		EmplLeaveOpeningBalance = delegator.makeValue("EmplLeaveOpeningBalance",
				UtilMisc.toMap("partyId", partyId));
		EmplLeaveOpeningBalance.setNonPKFields(context);
		toBeStored.add(EmplLeaveOpeningBalance);

		try {
			delegator.storeAll(toBeStored);
		} catch (GenericEntityException e) {
			Debug.logWarning(e.getMessage(), null);
			return ServiceUtil.returnError(UtilProperties.getMessage(
					null, "person.create.db_error",
					new Object[] { e.getMessage() }, locale));
		}

		result.put("partyId", partyId);
		result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
		return result;
	}
	
}
