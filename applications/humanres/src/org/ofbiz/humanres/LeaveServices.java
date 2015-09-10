package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Array;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
//import org.ofbiz.registry.FileServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.calendar.RecurrenceRule;
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
			
			calculateCarryOverLost(partyId, appointmentdate);// call method to calculate
			deleteExistingResetAnnualLost(delegator);
			deleteExistingLeaveOpeningBalances(delegator);
			deleteExistingLeaveCalendar(delegator);
			 
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
		
		//===============================Consider Opening Balances and Reset lost days=====================================
		GenericValue getAnualOpeningSumELI=null;
		GenericValue getAnualResetDaysELI=null;
		BigDecimal annualResetLeaveDays = BigDecimal.ZERO;
		
		BigDecimal LeaveBalanceCarriedForward = BigDecimal.ZERO; 
		BigDecimal LeaveDaysUsed= BigDecimal.ZERO; 
		try {
			 
			 getAnualOpeningSumELI = delegator.findOne("EmplLeaveOpeningBalance", 
					            UtilMisc.toMap("partyId",partyId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		try {
			 
			 getAnualResetDaysELI = delegator.findOne("AnnualLeaveBalancesReset", 
					            UtilMisc.toMap("partyId",partyId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		//===================Consider Annual Leave Balances====================================
		GenericValue getAnualLeaveBalanceELI=null;
		BigDecimal annualBal=BigDecimal.ZERO;
		try {
			 
			 getAnualLeaveBalanceELI = delegator.findOne("LeaveBalances", 
					            UtilMisc.toMap("partyId",partyId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		if (getAnualLeaveBalanceELI!=null) {
			annualBal=getAnualLeaveBalanceELI.getBigDecimal("availableLeaveDays");
			
			
		} else {
			String errorMsg = "================================NOTHING FOUND HERE===============";
		}
		
		if (getAnualResetDaysELI!=null) {
			annualResetLeaveDays=getAnualResetDaysELI.getBigDecimal("annualLeaveDaysLost");
			
			
		} else {
			String errorMsg = "================================NOTHING FOUND HERE===============";
		}
		
		
		if (getAnualOpeningSumELI!=null) {
			LeaveBalanceCarriedForward = getAnualOpeningSumELI.getBigDecimal("annualLeaveBalanceCarriedForward");
			LeaveDaysUsed = getAnualOpeningSumELI.getBigDecimal("annualLeaveDaysUsed");
			
			
		} else {
			
			String errorMsg = "================================NOTHING FOUND HERE===============";
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
		double accruedLeaveDay = (months * accrualRate)+LeaveBalanceCarriedForward.doubleValue()+annualResetLeaveDays.doubleValue();
		log.info("++++accruedLeaveDay===="+accruedLeaveDay+"====LeaveBalanceCarriedForward:"+LeaveBalanceCarriedForward+"annualResetLeaveDays:"+annualResetLeaveDays);
		double totalUsed=usedLeaveDays+LeaveDaysUsed.doubleValue();
		double leaveBalances =  accruedLeaveDay - totalUsed;
		/*if (leaveBalances > MAXCARRYOVER) {
			lostLeaveDays = leaveBalances - MAXCARRYOVER;
			carryOverLeaveDays = MAXCARRYOVER;		 	
		 } 
		 if (leaveBalances <= MAXCARRYOVER) {
		 	lostLeaveDays = 0;
			carryOverLeaveDays = leaveBalances;
		 }
		 */
		 if (annualBal.doubleValue() > MAXCARRYOVER) {
				lostLeaveDays = annualBal.doubleValue() - MAXCARRYOVER;
				carryOverLeaveDays = MAXCARRYOVER;		 	
			 } 
			 if (annualBal.doubleValue() <= MAXCARRYOVER) {
			 	lostLeaveDays = 0;
				carryOverLeaveDays = annualBal.doubleValue();
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
	            "usedLeaveDays", totalUsed,
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
	
	private static void deleteExistingLeaveCalendar(Delegator delegator){
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("EmplLeaveCalender");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED ALL LEAVE CALENDAR!");
		
	}


public static Map getCarryoverUsed(Delegator delegator, Double leaveDuration, String partyId) {
	Map<String, Object> UsedDays = FastMap.newInstance();
	//double carryOverLeaveDays = 0;
	double leaveDurationRemainder = 0, carryOverRemain = 0, carryOverUsed = 0 ;

	log.info("++++++++++partyId++++++++++++++++++++" +partyId);
	log.info("++++++++++leaveDuration++++++++++++++++++++" +leaveDuration);
	GenericValue carryGV = null; //GenericValue result = null;
	
	double carryOverLeaveDays = 0;

      try {
            carryGV = delegator.findOne("EmplCarryOverLost", 
             	UtilMisc.toMap("partyId", partyId), false);
           	log.info("++++++++++++++carryGV++++++++++++++++" +carryGV);
             }
       catch (GenericEntityException e) {
            e.printStackTrace();;
       }  
      
      if (carryGV != null) {
    	  carryOverLeaveDays = carryGV.getDouble("carryOverLeaveDays");
	} else {

	}
      
       
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
			String handover = leave.getString("handedOverTo");
			Map carryOverLeaveDaysUsed = null;
			GenericValue documentApproval = null; GenericValue leavelog = null; GenericValue emailRecord_handover = null; GenericValue emailRecord_approver = null; GenericValue emailRecord_applicant = null;
			documentApproval =  WorkflowServices.doFoward(delegator, organizationUnitId,	workflowDocumentTypeId, documentApprovalId);
		log.info("=====================" +documentApproval);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WORKFLOW-DOC: " + workflowDocumentTypeId);

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
				
				
				GenericValue staff = null;

				try {
					staff = delegator.findOne("Person",
							UtilMisc.toMap("partyId", partyId), false);
				} catch (GenericEntityException e) {
					Debug.logWarning(e.getMessage(), null);
				}
				
				String payroll = staff.getString("employeeNumber");
				String fname = staff.getString("firstName");
				String sname = staff.getString("lastName");
				



			leavelog = delegator.makeValue("LeaveStatusLog", "leaveStsLogId", delegator.getNextSeqId("LeaveStatusLog"), 
            "approvedBy", userLogin.getString("partyId"), 
            "nextApprover", documentApproval.getString("responsibleEmployee"),
            "partyId", partyId, 
            "leaveId", leaveId, "approvalStatus" ,approvalStatuslog);
			
			emailRecord_approver = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", documentApproval.getString("responsibleEmployee"),
		            "subject", "Leave Approval", 
		            "body", "A leave application has been forwarded to you and it requires your approval. Please log onto the System for details",
		            "sendStatus", "NOTSEND");
			
			emailRecord_applicant = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", partyId,
		            "subject", "Leave Status", 
		            "body", "Your Leave Application has a status:-["+approvalStatuslog+"]",
		            "sendStatus", "NOTSEND");
			
			emailRecord_handover = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", handover,
		            "subject", "Responsibility Handover", 
		            "body", "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has reached the status of:-["+approvalStatuslog+"] and they are handing over their resiponsibilities to you. Communicate to them for more information",
		            "sendStatus", "NOTSEND");

			try {
				leavelog.create();
				emailRecord_approver.create();
				emailRecord_applicant.create();
				emailRecord_handover.create();
				
			} catch (GenericEntityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			// Set Responsible
			// responsibleEmployee

			leave.set("responsibleEmployee",	documentApproval.getString("responsibleEmployee"));
			leave.set("rejectReason" , "-");
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
	
	
	
	
	/* ====================================== GET FIRST APPROVER ON LEAVE WORKFLOW ===========================================*/
	                 
	public static String getFirstWorkflowApprover(String leaveid) {
		

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);


		GenericValue leavesELI = null; 
		GenericValue workflowELI = null; 
		GenericValue workflowResponsibleStaff = null; 
		List <GenericValue> getWorkflowELI = null;
		List <GenericValue> getWorkflowResponsibleStaffELI = null;
		String party = null;
		String partyWorkFlow = null;
		String WorkFlowPartyResponsible = null;

		try {
			
			leavesELI = delegator.findOne("EmplLeave",
							UtilMisc.toMap("leaveId", leaveid), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (leavesELI != null) {
			party = leavesELI.getString("partyId");
		} else {

		}
		
		try {
			getWorkflowELI = delegator.findList("LeaveWorkFlowStaffMapping",	EntityCondition.makeCondition("partyId", party), null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowELI.size() > 0) {
			workflowELI = getWorkflowELI.get(0);
			partyWorkFlow = workflowELI.getString("leaveWorkFlowId");
		}
		
		EntityConditionList<EntityExpr> workflowConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("leaveWorkFlowId", EntityOperator.EQUALS, partyWorkFlow),
					EntityCondition.makeCondition("isFirst", EntityOperator.EQUALS, "YES")),
						EntityOperator.AND);
		
		try {
			getWorkflowResponsibleStaffELI = delegator.findList("LeaveWorkFlowResponsibleStaff", workflowConditions, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowResponsibleStaffELI.size() > 0) {
			workflowResponsibleStaff = getWorkflowResponsibleStaffELI.get(0);
			WorkFlowPartyResponsible = workflowResponsibleStaff.getString("responsibleEmployee");
		}

		

		return WorkFlowPartyResponsible;
	}
	
	
	
	
	public static String ForwardMyLeaveApplication(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
	       GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
			String user = userLogin.getString("partyId");
			String leaveid = (String) request.getParameter("leaveId");
			List<GenericValue> toBeStored = FastList.newInstance();
			GenericValue leavesELI = null; 
			GenericValue partyELI = null; 
			GenericValue leaveLogs = null; 
			GenericValue ForwaedTo = null; 
			GenericValue LeaveTypeELI = null; 
			GenericValue emailRecord_firstApprover = null;
			String firstApprover = 	getFirstWorkflowApprover(leaveid);	
			
			try {
				
				leavesELI = delegator.findOne("EmplLeave",
								UtilMisc.toMap("leaveId", leaveid), false);
						
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			if (leavesELI != null) {
				leavesELI.set("responsibleEmployee", firstApprover);
				
				String party = leavesELI.getString("partyId");
				
				try {
					
					partyELI = delegator.findOne("Person",	UtilMisc.toMap("partyId", party), false);
							
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				
				String fname = null;
				String sname = null;
				String payrolll = null;
				
					fname = partyELI.getString("firstName");
					sname = partyELI.getString("lastName");
					payrolll = partyELI.getString("employeeNumber");
					String fullName = " "+fname+" "+sname;
					
					
					try {
						
						ForwaedTo = delegator.findOne("Person",	UtilMisc.toMap("partyId", firstApprover), false);
								
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}
					
                       try {
						
						LeaveTypeELI = delegator.findOne("EmplLeaveType",	UtilMisc.toMap("leaveTypeId", leavesELI.getString("leaveTypeId")), false);
								
					} catch (GenericEntityException e) {
						e.printStackTrace();
					}
					
					String fname2 = null;
					String sname2 = null;
					
						fname2 = ForwaedTo.getString("firstName");
						sname2 = ForwaedTo.getString("lastName");
						String fullName2 = " "+fname2+" "+sname2;
				
				emailRecord_firstApprover = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
						"partyId", firstApprover,
			            "subject", "LEAVE APPROVAL", 
			            "body", "Leave Application has been Send to you for approval by :"+fullName+" Payroll :"+payrolll,
			            "sendStatus", "NOTSEND");
				
				leaveLogs = delegator.makeValue("staffLeaveLogs", "logId", delegator.getNextSeqId("staffLeaveLogs"), 
						"partyId", party,
			            "leaveType", LeaveTypeELI.getString("description"), 
			            "action", "Forward to "+fullName2,
			            "actionBy", user);
				
				
				
				toBeStored.add(leavesELI);
				toBeStored.add(leaveLogs);
				toBeStored.add(emailRecord_firstApprover);
			} else {

			}
			
			
			try {
				delegator.storeAll(toBeStored);
				SendScheduledMail(request, response);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			
			
		return leaveid;
	}
	
	
	

	
	
	
	/* ================================= APPROVE LEAVE ===========================================================*/
	
	public static String approveLeave(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
       GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String user = userLogin.getString("partyId");
		String leaveid = (String) request.getParameter("leaveId");
		
		
	
		List<GenericValue> toBeStored = FastList.newInstance();
		
		GenericValue leavesELI = null; 
		GenericValue workflowELI = null; 
		GenericValue forwardedToName = null;
		GenericValue workflowResponsibleStaff = null; 
		GenericValue workflowNextResponsibleStaff = null; 
		List <GenericValue> getWorkflowELI = null; 
		List <GenericValue> getWorkflowNextResponsibleStaffELI = null;
		List <GenericValue> getWorkflowResponsibleStaffELI = null;
		String party = null;
		String partyWorkFlow = null;
		String isThemLast = null;
		String nextResponsibleStaff = null;
		String email_To_StaffHandedOver = null;
		String leaveStart = null;
		String leaveStop = null;
		String leaveDuration = null;
		GenericValue leaveLogs = null; 
		GenericValue ForwaedTo = null; 
		GenericValue LeaveTypeELI = null; 
		List<GenericValue> leaveStaffELI = null;
	

		try {
			
			leavesELI = delegator.findOne("EmplLeave",
							UtilMisc.toMap("leaveId", leaveid), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (leavesELI != null) {
			party = leavesELI.getString("partyId");
			email_To_StaffHandedOver = leavesELI.getString("handedOverTo");
			leaveDuration = leavesELI.getString("leaveDuration");
			leaveStart = leavesELI.getString("fromDate");
			leaveStop = leavesELI.getString("thruDate");
		} else {

		}
		
		try {
			getWorkflowELI = delegator.findList("LeaveWorkFlowStaffMapping",	EntityCondition.makeCondition("partyId", party), null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowELI.size() > 0) {
			workflowELI = getWorkflowELI.get(0);
			partyWorkFlow = workflowELI.getString("leaveWorkFlowId");
		}
		
		EntityConditionList<EntityExpr> workflowConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("leaveWorkFlowId", EntityOperator.EQUALS, partyWorkFlow),
					EntityCondition.makeCondition("responsibleEmployee", EntityOperator.EQUALS, user)),
						EntityOperator.AND);
		
		try {
			getWorkflowResponsibleStaffELI = delegator.findList("LeaveWorkFlowResponsibleStaff", workflowConditions, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowResponsibleStaffELI.size() > 0) {
			workflowResponsibleStaff = getWorkflowResponsibleStaffELI.get(0);
			isThemLast = workflowResponsibleStaff.getString("isLast");
		}
		
		if (isThemLast.equalsIgnoreCase("YES")) {
			
			leavesELI.set("approvalStatus", "Approved");
			leavesELI.set("applicationStatus", "Approved");
			
			
			GenericValue emailRecord_handover = null;
			GenericValue emailRecord_applicant = null; 
			GenericValue staff = null;
			GenericValue leaveStaffMail = null;

			try {
				staff = delegator.findOne("Person",
						UtilMisc.toMap("partyId", party), false);
			} catch (GenericEntityException e) {
				Debug.logWarning(e.getMessage(), null);
			}
			
			String payroll = staff.getString("employeeNumber");
			String fname = staff.getString("firstName");
			String sname = staff.getString("lastName");
			
			try {
				
				ForwaedTo = delegator.findOne("Person",	UtilMisc.toMap("partyId", user), false);
						
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
               try {
				
				LeaveTypeELI = delegator.findOne("EmplLeaveType",	UtilMisc.toMap("leaveTypeId", leavesELI.getString("leaveTypeId")), false);
						
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			String fname2 = null;
			String sname2 = null;
			
				fname2 = ForwaedTo.getString("firstName");
				sname2 = ForwaedTo.getString("lastName");
				String fullName2 = " "+fname2+" "+sname2;
			
				String leaveStaffId= null;
				
		try{
		    	
		    leaveStaffELI= delegator.findList("LeaveStaff", EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL,null), null, null, null, false);
		    
		    }catch(GenericEntityException e){
		    	e.printStackTrace();
		    }
			System.out.println("##########: SIZE: "+leaveStaffELI.size());
		    for (GenericValue genericValue : leaveStaffELI) {
		    	
		    	leaveStaffId = genericValue.getString("partyId");
		    	System.out.println("TRYING TO SEND EMAIL FOR PARTY ID: "+leaveStaffId);
//		    	 leaveStaffMail = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
//				 			"partyId", leaveStaffId,
//				 		    "subject", "NOTIFICATION : LEAVE APPROVAL ", 
//				 		    "body", "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has been Approved.Duration of leave is :"+leaveDuration+" days, It starts on :"+leaveStart+" and end on :"+leaveStop,
//				 		    "sendStatus", "NOTSEND");
		    	String body = "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has been Approved.Duration of leave is :"+leaveDuration+" days, It starts on :"+leaveStart+" and end on :"+leaveStop;
		 		GenericValue leaveStaffMailGV = delegator.makeValue("StaffScheduledMail");
		 		String msgId = delegator.getNextSeqId("StaffScheduledMail");
		 		leaveStaffMailGV.put("msgId", msgId);
		 		leaveStaffMailGV.put("partyId", leaveStaffId);
		 		leaveStaffMailGV.put("subject", "LEAVE NOTIFICATION");
		 		leaveStaffMailGV.put("body", body);
		 		leaveStaffMailGV.put("sendStatus", "NOTSEND");
				try {
					leaveStaffMailGV.create();
					System.out.println("EMAIL SENT FOR PARTY ID: "+leaveStaffId);
				} catch (GenericEntityException e) {
					System.out.println("EMAIL NOT SENT FOR PARTY ID: "+leaveStaffId);
					e.printStackTrace();
				}	 
		    	 
		    	 
		    }
		       
		    
					    	
			emailRecord_handover = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", email_To_StaffHandedOver,
		            "subject", "RESPONSIBILITIES HANDOVER", 
		            "body", "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has been Approved and they are handing over their resiponsibilities to you. Communicate to them for more information",
		            "sendStatus", "NOTSEND");
			
			emailRecord_applicant = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", party,
		            "subject", "LEAVE FORWARDED", 
		            "body", "Your leave application has been been APPROVED. Duration of leave is :"+leaveDuration+" days, It starts on :"+leaveStart+" and end on :"+leaveStop,
		            "sendStatus", "NOTSEND");
			
			leaveLogs = delegator.makeValue("staffLeaveLogs", "logId", delegator.getNextSeqId("staffLeaveLogs"), 
					"partyId", party,
		            "leaveType", LeaveTypeELI.getString("description"), 
		            "action", "Approved By "+fullName2,
		            "actionBy", user);
			
			toBeStored.add(leavesELI);
			toBeStored.add(leaveLogs);
			toBeStored.add(emailRecord_handover);
			toBeStored.add(emailRecord_applicant);
			
			System.out.println("### THE ABOVE PART WAS EXECUTED #####");
			
		} else if(isThemLast.equalsIgnoreCase("NO")){
			
			EntityConditionList<EntityExpr> workflowNextStaffConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("leaveWorkFlowId", EntityOperator.EQUALS, partyWorkFlow),
					EntityCondition.makeCondition("comeAfter", EntityOperator.EQUALS, user)),
						EntityOperator.AND);
		
		try {
			getWorkflowNextResponsibleStaffELI = delegator.findList("LeaveWorkFlowResponsibleStaff", workflowNextStaffConditions, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowNextResponsibleStaffELI.size() > 0) {
			workflowNextResponsibleStaff = getWorkflowNextResponsibleStaffELI.get(0);
			nextResponsibleStaff = workflowNextResponsibleStaff.getString("responsibleEmployee");
		}
		
        try {
			
			forwardedToName = delegator.findOne("Person",
							UtilMisc.toMap("partyId", nextResponsibleStaff), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String fname = null;
		String sname = null;
		String payrolll = null;
		
			fname = forwardedToName.getString("firstName");
			sname = forwardedToName.getString("lastName");
			payrolll = forwardedToName.getString("employeeNumber");
			String fullName = " "+fname+" "+sname;
			
		leavesELI.set("responsibleEmployee", nextResponsibleStaff);
		leavesELI.set("applicationStatus", "Application Forwarded to"+fullName);
		leavesELI.set("approvalStatus", "Application Forwarded to"+fullName);
		
		GenericValue Applicantstaff = null;

		try {
			Applicantstaff = delegator.findOne("Person",
					UtilMisc.toMap("partyId", party), false);
		} catch (GenericEntityException e) {
			Debug.logWarning(e.getMessage(), null);
		}
		
		String staffpayroll = Applicantstaff.getString("employeeNumber");
		String staffirstname = Applicantstaff.getString("firstName");
		String staflastsname = Applicantstaff.getString("lastName");
		String staffullName = " "+staffirstname+" "+staflastsname;
		
		GenericValue ForwaedTo2 = null;
		GenericValue LeaveTypeELI2 = null;
		
		try {
			
			ForwaedTo2 = delegator.findOne("Person",	UtilMisc.toMap("partyId", nextResponsibleStaff), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
           try {
			
			LeaveTypeELI2 = delegator.findOne("EmplLeaveType",	UtilMisc.toMap("leaveTypeId", leavesELI.getString("leaveTypeId")), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String fname22 = null;
		String sname22 = null;
		
			fname22 = ForwaedTo2.getString("firstName");
			sname22 = ForwaedTo2.getString("lastName");
			String fullName22 = " "+fname22+" "+sname22;
			

		GenericValue emailRecord_approver = null; GenericValue emailRecord_applicant = null; 
		emailRecord_approver = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
				"partyId", nextResponsibleStaff,
	            "subject", "LEAVE APPROVAL", 
	            "body", "Leave application of ["+staffullName+"] Payroll:-["+staffpayroll+"]  has been forwarded to you and it requires your approval. Please log onto the System for details",
	            "sendStatus", "NOTSEND");
		
		emailRecord_applicant = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
				"partyId", party,
	            "subject", "LEAVE FORWARDED", 
	            "body", "Your leave application has been been forwarded to :"+fullName+" Payroll :"+payrolll+" For approval",
	            "sendStatus", "NOTSEND");
		
		leaveLogs = delegator.makeValue("staffLeaveLogs", "logId", delegator.getNextSeqId("staffLeaveLogs"), 
				"partyId", party,
	            "leaveType", LeaveTypeELI2.getString("description"), 
	            "action", "Forwarded to "+fullName22,
	            "actionBy", user);
		
		toBeStored.add(leavesELI);
		toBeStored.add(leaveLogs);
		toBeStored.add(emailRecord_approver);
		toBeStored.add(emailRecord_applicant);

		}

		try {
			delegator.storeAll(toBeStored);
			SendScheduledMail(request, response);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return leaveid;
	}
	
	public static String validateWorkflowName(String workflow) {
		String state = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
	     List<GenericValue> getWorkflowELI = null;
		
		try {
			getWorkflowELI = delegator.findList("LeaveWorkFlow",null, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		for (GenericValue genericValue : getWorkflowELI) {
			String dbWorkflowName = genericValue.getString("name");
			
			log.info("-----++++++-----DB-----------------" +dbWorkflowName);
			log.info("-----++++++-----USER-----------------" +workflow);
			
			if (dbWorkflowName == workflow) {
				state = "INVALID";
			} else {
				state = "VALID";
			}
			
		}
		
		
		return state;
		
	}
	
	public static String validateWorkflowStaffMapping(String party) {
		String state = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
	     List<GenericValue> getWorkflowELI = null;
		
		try {
			getWorkflowELI = delegator.findList("LeaveWorkFlowStaffMapping",null, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		for (GenericValue genericValue : getWorkflowELI) {
			String dbWorkflowName = genericValue.getString("partyId");
			
			log.info("-----++++++-----DB-----------------" +dbWorkflowName);
			log.info("-----++++++-----USER-----------------" +party);
			
			if (dbWorkflowName.equals(party)) {
				state = "INVALID";
			} else {
				state = "VALID";
			}
			
		}
		
		
		return state;
		
	}
	
	/* SEND SCHEDULED EMAIL NOTIFICATION */

	public static String SendScheduledMail(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory()).createLocalDispatcher("interestcalculations", delegator);

		Map<String, String> context = UtilMisc.toMap("message",	"Email Sending Testing !!");
		try {
			dispatcher.runAsync("sendScheduledEmailNotificationToStaff", context);
		} catch (GenericServiceException e) {
			e.printStackTrace();
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
	
	/////
	/*public static String runFileRequestService(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
        LocalDispatcher dispatcher = (new GenericDispatcherFactory()).createLocalDispatcher("interestcalculations", delegator);
         Map<String, Object> vlFulFill = FastMap.newInstance();
         GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
         vlFulFill.put("userLogin", userLogin);
        try {
            dispatcher.runAsync("requestfiles", vlFulFill);
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
                log.info("#######################>>>>>>>>>>#$$$$$$$$$$$$$$$$$$$$$$$$$Error");
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
    
   
    
    public static String RequestAndSendNotification(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
            String leaveid = (String) request.getParameter("partyId");
            
            runFileRequestService(request, response);
            SendScheduledMail(request, response);
            
            
        return leaveid;
    }
	
	*/
	
	
	
	/* ================================= BY PASS  LEAVE ===========================================================*/
	
	public static String byPassLeaveApprover(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
       GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String user = userLogin.getString("partyId");
		String leaveid = (String) request.getParameter("leaveId");
		
		
	
		List<GenericValue> toBeStored = FastList.newInstance();
		
		GenericValue leavesELI = null; 
		GenericValue workflowELI = null; 
		GenericValue forwardedToName = null;
		GenericValue workflowResponsibleStaff = null; 
		GenericValue workflowNextResponsibleStaff = null; 
		List <GenericValue> getWorkflowELI = null; 
		List <GenericValue> getWorkflowNextResponsibleStaffELI = null;
		List <GenericValue> getWorkflowResponsibleStaffELI = null;
		String party = null;
		String partyWorkFlow = null;
		String isThemLast = null;
		String nextResponsibleStaff = null;
		String email_To_StaffHandedOver = null;
		String leaveStart = null;
		String leaveStop = null;
		String leaveDuration = null;
		GenericValue leaveLogs = null; 
		GenericValue ForwaedTo = null; 
		GenericValue LeaveTypeELI = null; 
        String goToNextResponsibleStaff=null;
        List<GenericValue> leaveStaffELI = null;
        
		try {
			
			leavesELI = delegator.findOne("EmplLeave",
							UtilMisc.toMap("leaveId", leaveid), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (leavesELI != null) {
			party = leavesELI.getString("partyId");
			email_To_StaffHandedOver = leavesELI.getString("handedOverTo");
			leaveDuration = leavesELI.getString("leaveDuration");
			leaveStart = leavesELI.getString("fromDate");
			leaveStop = leavesELI.getString("thruDate");
			goToNextResponsibleStaff=leavesELI.getString("responsibleEmployee");
		} else {

		}
		log.info("##################################"+ goToNextResponsibleStaff);
		
		try {
			getWorkflowELI = delegator.findList("LeaveWorkFlowStaffMapping",	EntityCondition.makeCondition("partyId", party), null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowELI.size() > 0) {
			workflowELI = getWorkflowELI.get(0);
			partyWorkFlow = workflowELI.getString("leaveWorkFlowId");
		}
		
		EntityConditionList<EntityExpr> workflowConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("leaveWorkFlowId", EntityOperator.EQUALS, partyWorkFlow),
					EntityCondition.makeCondition("responsibleEmployee", EntityOperator.EQUALS, goToNextResponsibleStaff)),
						EntityOperator.AND);
		
		try {
			getWorkflowResponsibleStaffELI = delegator.findList("LeaveWorkFlowResponsibleStaff", workflowConditions, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowResponsibleStaffELI.size() > 0) {
			workflowResponsibleStaff = getWorkflowResponsibleStaffELI.get(0);
			isThemLast = workflowResponsibleStaff.getString("isLast");
		}
		
		if (isThemLast.equalsIgnoreCase("YES")) {
			
			leavesELI.set("approvalStatus", "Approved");
			leavesELI.set("applicationStatus", "Approved");
			
			
			GenericValue emailRecord_handover = null;
			GenericValue emailRecord_applicant = null; 
			GenericValue staff = null;

			try {
				staff = delegator.findOne("Person",
						UtilMisc.toMap("partyId", party), false);
			} catch (GenericEntityException e) {
				Debug.logWarning(e.getMessage(), null);
			}
			
			String payroll = staff.getString("employeeNumber");
			String fname = staff.getString("firstName");
			String sname = staff.getString("lastName");
			
			try {
				
				ForwaedTo = delegator.findOne("Person",	UtilMisc.toMap("partyId", goToNextResponsibleStaff), false);
						
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
               try {
				
				LeaveTypeELI = delegator.findOne("EmplLeaveType",	UtilMisc.toMap("leaveTypeId", leavesELI.getString("leaveTypeId")), false);
						
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			String fname2 = null;
			String sname2 = null;
			
				fname2 = ForwaedTo.getString("firstName");
				sname2 = ForwaedTo.getString("lastName");
				String fullName2 = " "+fname2+" "+sname2;
				String leaveStaffId= null;
				//
				try{
			    	
				    leaveStaffELI= delegator.findList("LeaveStaff", EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL,null), null, null, null, false);
				    
				    }catch(GenericEntityException e){
				    	e.printStackTrace();
				    }
					System.out.println("##########: SIZE: "+leaveStaffELI.size());
				    for (GenericValue genericValue : leaveStaffELI) {
				    	
				    	leaveStaffId = genericValue.getString("partyId");
				    	System.out.println("TRYING TO SEND EMAIL FOR PARTY ID: "+leaveStaffId);

				    	String body = "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has been Approved.Duration of leave is :"+leaveDuration+" days, It starts on :"+leaveStart+" and end on :"+leaveStop;
				 		GenericValue leaveStaffMailGV = delegator.makeValue("StaffScheduledMail");
				 		String msgId = delegator.getNextSeqId("StaffScheduledMail");
				 		leaveStaffMailGV.put("msgId", msgId);
				 		leaveStaffMailGV.put("partyId", leaveStaffId);
				 		leaveStaffMailGV.put("subject", "LEAVE NOTIFICATION");
				 		leaveStaffMailGV.put("body", body);
				 		leaveStaffMailGV.put("sendStatus", "NOTSEND");
						try {
							leaveStaffMailGV.create();
							System.out.println("EMAIL SENT FOR PARTY ID: "+leaveStaffId);
						} catch (GenericEntityException e) {
							System.out.println("EMAIL NOT SENT FOR PARTY ID: "+leaveStaffId);
							e.printStackTrace();
						}	 
				    	 
				    	 
				    }
				       
				//
	
			emailRecord_handover = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", email_To_StaffHandedOver,
		            "subject", "RESPONSIBILITIES HANDOVER", 
		            "body", "Leave application of ["+fname+" "+sname+"] Payroll:-["+payroll+"] has been Approved and they are handing over their resiponsibilities to you. Communicate to them for more information",
		            "sendStatus", "NOTSEND");
			
			emailRecord_applicant = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
					"partyId", party,
		            "subject", "LEAVE FORWARDED", 
		            "body", "Your leave application has been been APPROVED. Duration of leave is :"+leaveDuration+" days, It starts on :"+leaveStart+" and end on :"+leaveStop,
		            "sendStatus", "NOTSEND");
			
			leaveLogs = delegator.makeValue("staffLeaveLogs", "logId", delegator.getNextSeqId("staffLeaveLogs"), 
					"partyId", party,
		            "leaveType", LeaveTypeELI.getString("description"), 
		            "action", "Approved By "+fullName2,
		            "actionBy", user);
			
			toBeStored.add(leavesELI);
			toBeStored.add(leaveLogs);
			toBeStored.add(emailRecord_handover);
			toBeStored.add(emailRecord_applicant);
		} else if(isThemLast.equalsIgnoreCase("NO")){
			
			EntityConditionList<EntityExpr> workflowNextStaffConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("leaveWorkFlowId", EntityOperator.EQUALS, partyWorkFlow),
					EntityCondition.makeCondition("comeAfter", EntityOperator.EQUALS, goToNextResponsibleStaff)),
						EntityOperator.AND);
		
		try {
			getWorkflowNextResponsibleStaffELI = delegator.findList("LeaveWorkFlowResponsibleStaff", workflowNextStaffConditions, null,null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		if (getWorkflowNextResponsibleStaffELI.size() > 0) {
			workflowNextResponsibleStaff = getWorkflowNextResponsibleStaffELI.get(0);
			nextResponsibleStaff = workflowNextResponsibleStaff.getString("responsibleEmployee");
		}
		
        try {
			
			forwardedToName = delegator.findOne("Person",
							UtilMisc.toMap("partyId", nextResponsibleStaff), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String fname = null;
		String sname = null;
		String payrolll = null;
		
			fname = forwardedToName.getString("firstName");
			sname = forwardedToName.getString("lastName");
			payrolll = forwardedToName.getString("employeeNumber");
			String fullName = " "+fname+" "+sname;
			
		leavesELI.set("responsibleEmployee", nextResponsibleStaff);
		leavesELI.set("applicationStatus", "Application Forwarded to"+fullName);
		leavesELI.set("approvalStatus", "Application Forwarded to"+fullName);
		
		GenericValue Applicantstaff = null;

		try {
			Applicantstaff = delegator.findOne("Person",
					UtilMisc.toMap("partyId", party), false);
		} catch (GenericEntityException e) {
			Debug.logWarning(e.getMessage(), null);
		}
		
		String staffpayroll = Applicantstaff.getString("employeeNumber");
		String staffirstname = Applicantstaff.getString("firstName");
		String staflastsname = Applicantstaff.getString("lastName");
		String staffullName = " "+staffirstname+" "+staflastsname;
		
		GenericValue ForwaedTo2 = null;
		GenericValue LeaveTypeELI2 = null;
		
		try {
			
			ForwaedTo2 = delegator.findOne("Person",	UtilMisc.toMap("partyId", nextResponsibleStaff), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
           try {
			
			LeaveTypeELI2 = delegator.findOne("EmplLeaveType",	UtilMisc.toMap("leaveTypeId", leavesELI.getString("leaveTypeId")), false);
					
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String fname22 = null;
		String sname22 = null;
		
			fname22 = ForwaedTo2.getString("firstName");
			sname22 = ForwaedTo2.getString("lastName");
			String fullName22 = " "+fname22+" "+sname22;
			

		GenericValue emailRecord_approver = null; GenericValue emailRecord_applicant = null; 
		emailRecord_approver = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
				"partyId", nextResponsibleStaff,
	            "subject", "LEAVE APPROVAL", 
	            "body", "Leave application of ["+staffullName+"] Payroll:-["+staffpayroll+"]  has been forwarded to you and it requires your approval. Please log onto the System for details",
	            "sendStatus", "NOTSEND");
		
		emailRecord_applicant = delegator.makeValue("StaffScheduledMail", "msgId", delegator.getNextSeqId("StaffScheduledMail"), 
				"partyId", party,
	            "subject", "LEAVE FORWARDED", 
	            "body", "Your leave application has been been forwarded to :"+fullName+" Payroll :"+payrolll+" For approval",
	            "sendStatus", "NOTSEND");
		
		leaveLogs = delegator.makeValue("staffLeaveLogs", "logId", delegator.getNextSeqId("staffLeaveLogs"), 
				"partyId", party,
	            "leaveType", LeaveTypeELI2.getString("description"), 
	            "action", "Forwarded to "+fullName22,
	            "actionBy", user);
		
		toBeStored.add(leavesELI);
		toBeStored.add(leaveLogs);
		toBeStored.add(emailRecord_approver);
		toBeStored.add(emailRecord_applicant);

		}

		try {
			delegator.storeAll(toBeStored);
			SendScheduledMail(request, response);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return leaveid;
	}
	
	
}
