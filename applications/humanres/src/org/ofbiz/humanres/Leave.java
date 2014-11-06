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
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.base.util.Debug;
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

public static String closeFinacialYearForCompassionate(HttpServletRequest request, HttpServletResponse response) {
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

		String partyId =""; 
		for (GenericValue genericValue : personsELI) {
			partyId = genericValue.getString("partyId");
			

			if (partyId !=null && partyId !="") {
						log.info("-----++++++-----partyId-----------------" +partyId);
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
	BigDecimal  lostLeaveDays =BigDecimal.ZERO;
	BigDecimal usedLeaveDays=BigDecimal.ZERO;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			usedLeaveDays = usedLeaveDays.add(genericValue.getBigDecimal("leaveDuration"));
		}
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	Long days = null; 
	GenericValue Day = null;
		try {
			Day = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "COMPASSIONATE_LEAVE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (Day != null) {
			days = Day.getLong("days");
		} else {
			System.out.println("######## Days not found for this leave #### ");
		}
		//========= ==============================//
		LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();
		int thisYear = today.getYear();
		String currentYear = Integer.toString(thisYear);
		
		
		lostLeaveDays = (new BigDecimal(days)).subtract(usedLeaveDays);
		String lost=String.valueOf(lostLeaveDays);
		
		// Delete record if it was created before end of this year
		deleteExistingCompassionateLost(delegator, partyId, currentYear, "COMPASSIONATE_LEAVE");
		
		GenericValue leavelog = delegator.makeValue("EmplCompassionateLost",
				"partyId", partyId,
				"financialYear", currentYear,
	            "leaveTypeId", "COMPASSIONATE_LEAVE" , 
	            "usedLeaveDays", usedLeaveDays,
	            "allocatedLeaveDays", days,
	            "lostLeaveDays", lost);
		
		
		
		
		
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
	double approvedLeaveSum =0;
	//double  usedLeaveDays = 0;
	BigDecimal lostLeaveDays = BigDecimal.ZERO;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			//approvedLeaveSum += genericValue.getLong("leaveDuration");
			approvedLeaveSum += genericValue.getDouble("leaveDuration");
		}
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	Long days =null; 
	GenericValue employeeLeaveType = null;
		try {
			employeeLeaveType = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", "COMPASSIONATE_LEAVE"), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			
		}
		if (employeeLeaveType != null) {

			days = employeeLeaveType.getLong("days");
			
		} else {
			System.out.println("######## Days not found #### ");
		}
		
		double leaveBalances =  days-approvedLeaveSum; 


		GenericValue leavelog = delegator.makeValue("CompassionateLeaveBalances", 
				"partyId", partyId, 
	            "days", days.doubleValue() , 
	            "usedLeaveDays", approvedLeaveSum,
	            "lostLeaveDays", lostLeaveDays.doubleValue(), 
	            "availableLeaveDays", leaveBalances);
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

	/*===============CALCULATE LEAVE BALANCES BASED ON ENTERED OPENING BALANCES=================================*/
	
	public static String generateAnnulLeaveBalancesWithOpeningBalances(HttpServletRequest request,
			HttpServletResponse response) {
		//Map<String, Object> result = FastMap.newInstance();

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> personsELI = null; // =
		deleteExistingLeaveBalance(delegator);
		Timestamp now = UtilDateTime.nowTimestamp();
		String thisYear=LeaveServices.getCurrentYear(now);
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
			calculateAnnualLeaveBalanceWithOpeningBalancesSave(partyId, appointmentdate, thisYear);
		}
		//log.info("------------------------------------------------" +partyId);
		return partyId;
	}


	
	public static void calculateAnnualLeaveBalanceWithOpeningBalancesSave(String partyId, String appointmentdate, String year) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		//String partyIds = partyId;
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;	
		GenericValue getAnualOpeningSumELI=null;
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS, year),
					EntityCondition.makeCondition("isDeductedFromAnnual",EntityOperator.EQUALS, "Y"),
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);
		
		EntityConditionList<EntityExpr> AnnualBalanceConditions = EntityCondition.makeCondition(UtilMisc.toList(
				    EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, 10075),
				    EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "ANNUAL_LEAVE"),
					null,null),EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		
		try {
			 /*getAnualOpeningSumELI = delegator.findList("EmplLeaveOpeningBalance",
					AnnualBalanceConditions, null, null, null, false);*/
			 
			/* getAnualOpeningSumELI = delegator.findOne("EmplLeaveOpeningBalance",
						UtilMisc.toMap("leaveTypeId", "ANNUAL_LEAVE"), false);*/
			 
			 getAnualOpeningSumELI = delegator.findOne("EmplLeaveOpeningBalance", 
					            UtilMisc.toMap("partyId",partyId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		//log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	BigDecimal approvedLeaveSum =BigDecimal.ZERO;
	//double  usedLeaveDays = 0;
	BigDecimal lostLeaveDays = BigDecimal.ZERO;
	
	BigDecimal LeaveBalanceCarriedForward = BigDecimal.ZERO; 
	BigDecimal LeaveDaysUsed= BigDecimal.ZERO; 
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			//approvedLeaveSum += genericValue.getLong("leaveDuration");
			approvedLeaveSum = approvedLeaveSum.add(genericValue.getBigDecimal("leaveDuration"));
		}
		
		
		/*for (GenericValue genericValueAnnualOpening : getAnualOpeningSumELI) {
			//approvedLeaveSum += genericValue.getLong("leaveDuration");
			
			
			LeaveBalanceCarriedForward = genericValueAnnualOpening.getBigDecimal("LeaveBalanceCarriedForward");
			LeaveDaysUsed = genericValueAnnualOpening.getBigDecimal("LeaveDaysUsed");
		}*/
		//log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
		if (getAnualOpeningSumELI==null) {
			String errorMsg = "================================NOTHING FOUND HERE===============";
			
			
		} else {
			LeaveBalanceCarriedForward = getAnualOpeningSumELI.getBigDecimal("annualLeaveBalanceCarriedForward");
			LeaveDaysUsed = getAnualOpeningSumELI.getBigDecimal("annualLeaveDaysUsed");
			
		}
		
		
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
		

		BigDecimal totalCarriedOver=carryOverLeaveDays.add(LeaveBalanceCarriedForward);
		BigDecimal totalUsedDays=approvedLeaveSum.add(LeaveDaysUsed);

		int months = difference.getMonths();
		BigDecimal accruedLeaveDay = accrualRate.multiply(new BigDecimal(months));
		
		/*BigDecimal leaveBalances =  (accruedLeaveDay.subtract(approvedLeaveSum)).add(carryOverLeaveDays); */
		BigDecimal leaveBalances =  (accruedLeaveDay.subtract(totalUsedDays)).add(totalCarriedOver); 
		
		 
		
		
		
		
		
		/*BigDecimal leaveBalances =  accruedLeaveDay.subtract(approvedLeaveSum); */


		GenericValue leavelog = delegator.makeValue("LeaveBalances",
				"partyId", partyId, 
	            "accruedDays", accruedLeaveDay.doubleValue() , 
	            "usedLeaveDays", totalUsedDays.doubleValue(),
	            "lostLeaveDays", lostLeaveDays.doubleValue(), 
	            "LeaveDaysCarriedOver", totalCarriedOver.doubleValue(), 
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


}
