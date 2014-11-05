package org.ofbiz.humanres;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;


public class Leave {
	public static Logger log = Logger.getLogger(Leave.class);

/*  =============    close financial year leaves ==============   */

public static String closeFinacialYear(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		Timestamp now = UtilDateTime.nowTimestamp();
		String financialYear=LeaveServices.getCurrentYear(now);
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
			calculateLostNonForwarded(partyId, financialYear);// call method to calculate	
			}
			
			
		}
		//log.info("------------------------------------------------" +partyId);
		return partyId;
	}


	public static void calculateLostNonForwarded(String partyId, String year) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String partyIds = partyId;
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;		
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
					EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS, year),
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "COMPASSIONATE_LEAVE"),
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "Approved")),EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		//log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	double  usedLeaveDays = 0, lostLeaveDays = 0 , carryOverLeaveDays = 0;
	double MAXCARRYOVER = 0;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			usedLeaveDays += genericValue.getDouble("leaveDuration");
		}
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	double days = 0; 
	GenericValue Day = null;
		try {
			Day = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "COMPASSIONATE_LEAVE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (Day != null) {
			days = Day.getDouble("days");
		} else {
			System.out.println("######## Days not found for this leave #### ");
		}
		//========= ==============================//
		LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();
		int thisYear = today.getYear();
		String currentYear = Integer.toString(thisYear);
		
		
		double leaveBalances =  days - usedLeaveDays;
		lostLeaveDays = leaveBalances - MAXCARRYOVER;
		
		// Delete record if it was created before end of this year
		deleteExistingCompassionateLost(delegator, partyId, currentYear, "COMPASSIONATE_LEAVE");
		
		GenericValue leavelog = delegator.makeValue("EmplCompassionateLost",
				"partyId", partyId,
				"financialYear", currentYear,
	            "leaveTypeId", "COMPASSIONATE_LEAVE" , 
	            "usedLeaveDays", usedLeaveDays,
	            "allocatedLeaveDays", days,
	            "balance", leaveBalances,
	            "lostLeaveDays", lostLeaveDays, 
	            "carryOverLeaveDays", carryOverLeaveDays);
		
		
		
		
		
	try {
		delegator.create(leavelog);
	} catch (GenericEntityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		
		
		
	
	}
			
	
	public static void deleteExistingCompassionateLost(Delegator delegator, String partyId, String currentYear, String leaveTypeId) {
		
		EntityConditionList<EntityExpr> leavetype = EntityCondition
				.makeCondition(UtilMisc.toList(	EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, leaveTypeId),null),EntityOperator.AND);
       try {
            
              List<GenericValue> ExistingCarryOverLost =delegator.findList("EmplCompassionateLost", leavetype, null, null, null, false);
              
              for (GenericValue genericValue : ExistingCarryOverLost) {
            	  
            	  ExistingCarryOverLost.remove(genericValue);
              }
       } catch (GenericEntityException e) {
            //return ServiceUtil.returnError("Failed. " +e.getMessage());
    	   e.printStackTrace();
       }  
}
	
/* =========================  Generate leave balances ========================== */



	public static String generateCompassionateLeaveBalances(HttpServletRequest request,	HttpServletResponse response) {
		//Map<String, Object> result = FastMap.newInstance();

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> personsELI = null; // =
		deleteExistingCompassionateLeaveBalance(delegator);
		Timestamp now = UtilDateTime.nowTimestamp();
		String financialYear=LeaveServices.getCurrentYear(now);
		try {
			personsELI = delegator.findAll("LeaveBalancesView", true);
			log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+personsELI);
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		//log.info("+++++++++++++++++++++++++++++++++" +personsELI);
		
		String partyId =""; 
		for (GenericValue genericValue : personsELI) {
			partyId = genericValue.getString("partyId");
			calculateCompassionateLeaveBalanceSave(partyId,financialYear);
		}
		//log.info("------------------------------------------------" +partyId);
		return partyId;
	}


	
	public static void calculateCompassionateLeaveBalanceSave(String partyId, String year) {
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
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "COMPASSIONATE_LEAVE"),
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
	BigDecimal days = BigDecimal.ZERO; 
	GenericValue employeeLeaveType = null;
		try {
			employeeLeaveType = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "COMPASSIONATE_LEAVE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			
		}
		if (days != null) {

			days = employeeLeaveType.getBigDecimal("days");
			
		} else {
			System.out.println("######## Days not found #### ");
		}
		
		
		BigDecimal leaveBalances =  days.subtract(approvedLeaveSum); 


		GenericValue leavelog = delegator.makeValue("CompassionateLeaveBalances", 
				"partyId", partyId, 
	            "days", days.doubleValue() , 
	            "usedLeaveDays", approvedLeaveSum.doubleValue(),
	            "lostLeaveDays", lostLeaveDays.doubleValue(), 
	            "availableLeaveDays", leaveBalances.doubleValue());
	try {
		delegator.create(leavelog);
	} catch (GenericEntityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
	}



	private static void deleteExistingCompassionateLeaveBalance(Delegator delegator) {
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("CompassionateLeaveBalances");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED  ALL RECORDS!" );
		
	}



}
