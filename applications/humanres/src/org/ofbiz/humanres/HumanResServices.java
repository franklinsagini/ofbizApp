package org.ofbiz.humanres;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
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
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class HumanResServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);
	
	// ============================================================== 
public static String getLeaveBalance(HttpServletRequest request,HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Timestamp now = UtilDateTime.nowTimestamp();
		String financialYear=LeaveServices.getCurrentYear(now);
		Date appointmentdate = null;
		try {
			appointmentdate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("confirmationdate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String leaveTypeId = new String((request.getParameter("leaveTypeId")).toString());
		String partyId = new String(request.getParameter("partyId")).toString();		
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;
		GenericValue carryOverLeaveGV = null;
	      try {
	    	  carryOverLeaveGV = delegator.findOne("EmplCarryOverLost", 
	             	UtilMisc.toMap("partyId", partyId), false);
	           	log.info("++++++++++++++carryOverLeaveGV++++++++++++++++" +carryOverLeaveGV);
	             }
	       catch (GenericEntityException e) {
	            e.printStackTrace();;
	       }  
	      /* double carryOverLeaveDays = carryOverLeaveGV.getDouble("carryOverLeaveDays");*/
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS, financialYear),
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "ANNUAL_LEAVE"),
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}
		log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	double approvedLeaveSum = 0;
	double  usedLeaveDays = 0;
	double lostLeaveDays = 0;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			 approvedLeaveSum += genericValue.getDouble("leaveDuration");
		}
		log.info("============================================================" +approvedLeaveSum);
		
		// ============ get accrual rate ================ //
	double accrualRate = 0; 
	GenericValue accrualRates = null;
		try {
			 accrualRates = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", leaveTypeId), false);
		} catch (GenericEntityException e) {
			return "Cannot Get Accrual Rate";
		}
		if (accrualRates != null) {

			accrualRate = accrualRates.getDouble("accrualRate");
			
		} else {
			System.out.println("######## Accrual Rate not found #### ");
		}
		
		//========= ==============================//
	
		LocalDateTime stappointmentdate = new LocalDateTime(appointmentdate);
		/*LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance().getTimeInMillis());*/
             
		LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();
		
		log.info(" FFFFFFFFFFF First Day "+firstDayOfYear.toDate());
		LocalDateTime accrueStart;
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
		/*String approvedLeaveSumed = Double.toString(approvedLeaveSum);*/
		double accruedLeaveDay = months * accrualRate;
		/*double leaveBalances =  accruedLeaveDay + carryOverLeaveDays - approvedLeaveSum; */
		String accruedLeaveDays = Double.toString(accruedLeaveDay);
		/*String leaveBalance = Double.toString(leaveBalances);*/

		
      //==============CONSIDER LEAVE BALANCES=========================
		GenericValue getAnualLeaveBalanceELI=null;
		BigDecimal annualBal=BigDecimal.ZERO;
		BigDecimal annualUsed=BigDecimal.ZERO;
		BigDecimal annualCarryOver=BigDecimal.ZERO;
		try {
			 
			 getAnualLeaveBalanceELI = delegator.findOne("LeaveBalances", 
					            UtilMisc.toMap("partyId",partyId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		if (getAnualLeaveBalanceELI!=null) {
			annualBal=getAnualLeaveBalanceELI.getBigDecimal("availableLeaveDays");
			annualUsed=getAnualLeaveBalanceELI.getBigDecimal("usedLeaveDays");
			annualCarryOver=getAnualLeaveBalanceELI.getBigDecimal("LeaveDaysCarriedOver");
			
			
		} else {
			annualBal=new BigDecimal(accruedLeaveDay);
		}
		
		
		
		
		
		
		
		
	
		//return leaveBalance;
		result.put("approvedLeaveSumed",annualUsed );
		result.put("accruedLeaveDays", accruedLeaveDays);
		result.put("leaveBalance" , annualBal);
		result.put("carryOverLeaveDays" , annualCarryOver);

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;

	}
 /*============================COMPASSIONATE LEAVE BALANCES========================================*/
public static String getCompassionateLeaveBalance(HttpServletRequest request,HttpServletResponse response) {
	Map<String, Object> result = FastMap.newInstance();
	Delegator delegator = (Delegator) request.getAttribute("delegator");
	Timestamp now = UtilDateTime.nowTimestamp();
	String financialYear=LeaveServices.getCurrentYear(now);
	

	String partyId = new String(request.getParameter("partyId")).toString();		
	//   get current leave balance  //
	
	List<GenericValue> getApprovedLeaveSumELI = null;		
	EntityConditionList<EntityExpr> leaveConditions = EntityCondition
			.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition(
					"partyId", EntityOperator.EQUALS, partyId),
					EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS, financialYear),
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
double approvedLeaveSum=0;
	for (GenericValue genericValue : getApprovedLeaveSumELI) {
		//approvedLeaveSum += genericValue.getLong("leaveDuration");
		approvedLeaveSum += genericValue.getDouble("leaveDuration");
	}
	//log.info("============================================================" +approvedLeaveSum);
	
	// ============ get accrual rate ================ //
Long days=null; 
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
	
	
	String leaveBalance = Double.toString(leaveBalances);

	//return leaveBalance;
	result.put("approvedLeaveSumed",approvedLeaveSum );
	result.put("accruedLeaveDays", days);
	result.put("leaveBalance" , leaveBalance);

	Gson gson = new Gson();
	String json = gson.toJson(result);

	// set the X-JSON content type
	response.setContentType("application/x-json");
	// jsonStr.length is not reliable for unicode characters
	try {
		response.setContentLength(json.getBytes("UTF8").length);
	} catch (UnsupportedEncodingException e) {
		try {
			throw new EventHandlerException("Problems with Json encoding",
					e);
		} catch (EventHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// return the JSON String
	Writer out;
	try {
		out = response.getWriter();
		out.write(json);
		out.flush();
	} catch (IOException e) {
		try {
			throw new EventHandlerException(
					"Unable to get response writer", e);
		} catch (EventHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	return json;

}


// ==============================================================


	public static String getLeaveDuration(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		 Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
        String leaveTypeId = (String)request.getParameter("leaveTypeId").toString();
        int leaveDuration=0;
		Date fromDate = null;
		GenericValue getLeaveDayTypeELI=null;
		String daytype="";
		String hasbalance="";
		try {
			fromDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Date thruDate = null;
		try {
			thruDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("thruDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		Logger log = Logger.getLogger(HumanResServices.class);
		log.info("=================================LLLLLLLLL leaveTypeId : "+leaveTypeId);
		log.info("======================================LLLLLLLLL FROM : "+fromDate);
		log.info("======================================LLLLLLLLL TO : "+thruDate);
		
		try {
			 
			 getLeaveDayTypeELI = delegator.findOne("EmplLeaveType", 
					            UtilMisc.toMap("leaveTypeId",leaveTypeId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		if (getLeaveDayTypeELI!=null) {
			daytype=getLeaveDayTypeELI.getString("daytype");
			hasbalance=getLeaveDayTypeELI.getString("hasbalance");
			
			
		} else {
			String errorMsg = "================================NOTHING FOUND HERE===============";
		}
		
		if (daytype.equalsIgnoreCase("Work Days")) {
			leaveDuration = AccHolderTransactionServices.calculateWorkingDaysBetweenDates(fromDate, thruDate);
			
		} else if(daytype.equalsIgnoreCase("Calendar Days")){
			leaveDuration = calculateCalenderDaysBetweenDates(fromDate, thruDate);

		}
		
		String indicator=null;
		
		if (hasbalance.equalsIgnoreCase("Yes")) {
			
			indicator="Y";
		} else if(hasbalance.equalsIgnoreCase("No")) {
			indicator="N";

		}
		
		

		
		
		result.put("leaveDuration", leaveDuration);
		result.put("hasBalance", indicator);
		
		log.info("======================================leaveDuration :=== "+leaveDuration);
		log.info("======================================hasBalance :==== "+indicator);

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;

	}
	
	public static String  getLeaveEnd(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		 Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
	     String leaveTypeId = (String)request.getParameter("leaveTypeId").toString();
		Date fromDate = null;
		String daytype="";
		GenericValue getLeaveDayTypeELI=null;
		Date endDate=null;
		Date resumeDate = null;
		
		try {
			fromDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		int leaveDuration = new Integer(request.getParameter("leaveDuration")).intValue();
		
		Logger log = Logger.getLogger(HumanResServices.class);
		log.info("=================================LLLLLLLLL leaveTypeId : "+leaveTypeId);
		log.info("======================================LLLLLLLLL FROM : "+fromDate);
		log.info("======================================LLLLLLLLL TO : "+leaveDuration);
		
		
		
		try {
			 
			 getLeaveDayTypeELI = delegator.findOne("EmplLeaveType", 
					            UtilMisc.toMap("leaveTypeId",leaveTypeId), false);
			 
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
			
		}
		if (getLeaveDayTypeELI!=null) {
			daytype=getLeaveDayTypeELI.getString("daytype");
			
			log.info("======================================DAYTYPE : "+daytype);
			
		} else {
			String errorMsg = "================================NOTHING FOUND HERE===============";
		}
		
		if (daytype.equalsIgnoreCase("Work Days")) {
			endDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveDuration);
			int leaveTillResumption = leaveDuration+1;
			resumeDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveTillResumption);
			
		} else if(daytype.equalsIgnoreCase("Calendar Days")){
			endDate = calculateEndCalenderDay(fromDate, leaveDuration);
			int leaveTillResumption = leaveDuration+1;
			resumeDate = calculateEndCalenderDay(fromDate, leaveTillResumption);
			

		}
		
		
		
		
		
		
		

		LocalDateTime dateFromDate = new LocalDateTime(fromDate.getTime());

		/*Date endDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveDuration);
		
		int leaveTillResumption = leaveDuration+1;
		Date resumeDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveTillResumption);*/
		
		
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18ThruDate = sdfDisplayDate.format(endDate);
	    String thruDate = sdfDate.format(endDate);
	    
		String i18resumptionDate = sdfDisplayDate.format(resumeDate);
	    String resumptionDate = sdfDate.format(resumeDate);
	    
	    
	    result.put("resumptionDate_i18n", i18resumptionDate);
	    result.put("resumptionDate", resumptionDate);
	    
		
	    result.put("thruDate_i18n", i18ThruDate);
	    result.put("thruDate", thruDate);
	    
	    
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	
	
	public static String getBranches(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String bankDetailsId = (String) request.getParameter("bankDetailsId");
		//GenericValue saccoProduct = null;
		//EntityListIterator branchesELI;// = delegator.findListIteratorByCondition("BankBranch", new EntityExpr("bankDetailsId", EntityOperator.EQUALS,  bankDetailsId), null, UtilMisc.toList("bankBranchId", "branchName"), "branchName", null);
		//branchesELI = delegator.findListIteratorByCondition(dynamicViewEntity, whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy, findOptions)
		//branchesELI = delegator.findListIteratorByCondition("BankBranch", new EntityExpr("productId", EntityOperator.NOT_EQUAL, null), UtilMisc.toList("productId"), null);
		List<GenericValue> branchesELI = null;
		
		//branchesELI = delegator.findList("BankBranch", new EntityExpr(), UtilMisc.toList("bankBranchId", "branchName"), null, null, null);
		try {
			//branchesELI = delegator.findList("BankBranch", EntityCondition.makeConditionWhere("(bankDetailsId = "+bankDetailsId+")"), null, null, null, false);
			branchesELI = delegator.findList("BankBranch", EntityCondition.makeCondition("bankDetailsId", bankDetailsId), null, null, null, false);
		
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//SaccoProduct
	
		//Add Branches to a list
		
		if (branchesELI == null){
			result.put("", "No Braches");
		}
		
		for (GenericValue genericValue : branchesELI) {
			result.put(genericValue.get("bankBranchId").toString(), genericValue.get("branchName"));
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}
	
	
	
	public static String  getConfirmationDate(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String employmentStatusEnumId = (String) request.getParameter("employmentStatusEnumId");
		Map<String, Object> result = FastMap.newInstance();
		Date appointmentdate = null;
		int periodBeforeConfirn=0;
		
		try {
			appointmentdate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("appointmentdate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		
		GenericValue period = null;
	      try {
	    	  period = delegator.findOne("Enumeration", 
	             	UtilMisc.toMap("enumId", employmentStatusEnumId), false);
	           	log.info("++++++++++++++period++++++++++++++++" +period);
	             }
	       catch (GenericEntityException e) {
	            e.printStackTrace();;
	       } 
	      if (period!=null) {
			periodBeforeConfirn=(period.getLong("periodBeforeConfirmation")).intValue();
		} else {

		}
		
		
		
		
		LocalDate dateAppointmentDate = new LocalDate(appointmentdate);

		LocalDate confirmDate = dateAppointmentDate.plusMonths(periodBeforeConfirn);
		
	
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18confirmationdate = sdfDisplayDate.format(confirmDate.toDate());
	    String confirmationdate = sdfDate.format(confirmDate.toDate());
	    
	    result.put("confirmationdate_i18n", i18confirmationdate);
	    result.put("confirmationdate", confirmationdate);
	   
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	
	public static String  getRetirementDate(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Date birthDate = null;
		
		try {
			birthDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("birthDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		LocalDate datebirthDate = new LocalDate(birthDate);

		LocalDate bodDate = datebirthDate.plusYears(55);
		
	
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18retirementdate = sdfDisplayDate.format(bodDate.toDate());
	    String retirementdate = sdfDate.format(bodDate.toDate());
	    
	    result.put("retirementdate_i18n", i18retirementdate);
	    result.put("retirementdate", retirementdate);
	   
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	
	
	
	public static String  NextPayrollNumber() {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String nextEmployeeNumber = null;
		String PayrollPrefix="HCS";
		
		
		List<GenericValue> employeeELI = null;
		
		
		GenericValue lastEmployee = null;
		List<GenericValue> prefix = null;
		GenericValue pre = null;
		
		try {
			
			prefix = delegator.findList("payRollPrefix",null, null, null, null, false);
			
		} catch (GenericEntityException e) {
			return null;
		}
		
		if (prefix.size() > 0){
			pre = prefix.get(0); 
			PayrollPrefix = pre.getString("payRollPrefix");
		}
		
	try {
		List<String> orderByList = new ArrayList<String>();
		orderByList.add("-createdStamp");

		employeeELI = delegator.findList("RoleTypeAndPersonEmployeeAndBranch",
				EntityCondition.makeCondition("roleTypeId",
						"EMPLOYEE"), null, orderByList, null, false);
		
		if (employeeELI.size() > 0){
			lastEmployee = employeeELI.get(0); 
		String emplNo=lastEmployee.getString("employeeNumber");
				
			
				 String trancatemplNo= StringUtils.substring(emplNo, 3);
				 int newEmplNo=Integer.parseInt(trancatemplNo)+1;
				 String h=String.valueOf(newEmplNo);
				 nextEmployeeNumber=PayrollPrefix.concat(h);
				 
				 log.info("++++++++++++++newPayrollNo++++++++++++++++" +nextEmployeeNumber);
		}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
	
		
	    
	    result.put("employeeNumber_i18n", nextEmployeeNumber);
	    result.put("employeeNumber", nextEmployeeNumber);
	   
	    return nextEmployeeNumber;


	}
	
	
	
	
	
	
	

	
	
	
	
	public static String  showHideFields(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String leaveTypeId = new String((request.getParameter("leaveTypeId")).toString());
		String deductedFromAnnual=null;
		String show="";
		
		
		List<GenericValue> leaveELI = null;
		GenericValue leaveType = null;
		try {
			leaveELI = delegator.findList("EmplLeaveType",
					EntityCondition.makeCondition("leaveTypeId", leaveTypeId),
					null, null, null, false);

			if (leaveELI.size() > 0) {
				leaveType = leaveELI.get(0);
				deductedFromAnnual = leaveType.getString("isDeductedFromAnnual");

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (deductedFromAnnual.equalsIgnoreCase("y")) {
			show=("SHOW");
		} else {
			show="HIDE";

		}
		
		
	    result.put("show", show);
	   
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	public static String  payrollUpperCase(String payRoll) {
		/*Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		deleteExistingPayrollPrefix(delegator); */
		String upperCasePayroll=payRoll.toUpperCase();
		
		 log.info("++++++++++++++upperCasePayroll++++++++++++++++" +upperCasePayroll);
		return upperCasePayroll;
	}
	

	
	private static void deleteExistingPayrollPrefix(Delegator delegator) {
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("payRollPrefix");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED  ALL RECORDS!" );
		
	}
	
	
	public static String  validatePayrollPrefix(HttpServletRequest request,	HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Map<String, Object> result = FastMap.newInstance();
		String payrollPrefix= (String)request.getParameter("lowerCase");
		String howLong="OK";
		
		 log.info("++++++++++++++payrollPrefix++++++++++++++++" +payrollPrefix);
		 
	int strLength=payrollPrefix.length();
	
	if (strLength!=3) {
		howLong="NOTOK";
	} else {
		howLong="OK";
		deleteExistingPayrollPrefix(delegator);
	}
	    
	    result.put("howLong", howLong);
	   
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	
	public static String  validateAnnualResetDays(HttpServletRequest request,HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		/*String lostDays= (String)request.getParameter("annualLostLeaveDays");
		String daysToReset= (String)request.getParameter("annualLeaveDaysLost");*/
		int lostDays = new Integer(request.getParameter("lostLeaveDays")).intValue();
		int daysToReset = new Integer(request.getParameter("annualLeaveDaysLost")).intValue();
		
		
		String state="GGGGGG";
		
		 log.info("++++++++++++++lostDays++++++++++++++++" +lostDays);
		 log.info("++++++++++++++daysToReset++++++++++++++++" +daysToReset);
		 

	
	if (daysToReset > lostDays) {
		state="MORE";
	} else if(daysToReset < 0){
		state="LITTLE";
	}else{
		state="INVALID";
	}
	    
	    result.put("state", state);
	   
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}

		return json;


	}
	
	public static int calculateCalenderDaysBetweenDates(Date startDate,
			Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
				daysCount++;
			

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return daysCount;
	}
	
	public static Date calculateEndCalenderDay(Date startDate, int noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());

		
		// Calculate End Date
		int count = 1;
		while (count < noOfDays) {
				localDateEndDate = localDateEndDate.plusDays(1);
			
			count++;
		}

		return localDateEndDate.toDate();
	}
	
	
	
}

