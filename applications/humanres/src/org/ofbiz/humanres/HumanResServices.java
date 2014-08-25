package org.ofbiz.humanres;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
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
public static String getLeaveBalance(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Date appointmentdate = null;
		try {
			appointmentdate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("appointmentdate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String leaveTypeId = new String((request.getParameter("leaveTypeId")).toString());
		String partyId = new String(request.getParameter("partyId")).toString();
		
		//   get current leave balance  //
		
		List<GenericValue> getApprovedLeaveSumELI = null;		
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
					EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, leaveTypeId),
					EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS, "LEAVE_APPROVED")),
						EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}
		log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+getApprovedLeaveSumELI);
	double approvedLeaveSum =0;
	double  usedLeaveDays = 0;
	double lostLeaveDays = 0;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			 approvedLeaveSum += genericValue.getLong("leaveDuration");
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
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		
		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(stappointmentdate, stCurrentDate, monthDay);

		int months = difference.getMonths();
		String approvedLeaveSumed = Double.toString(approvedLeaveSum);
		double accruedLeaveDay = months * accrualRate;
		double leaveBalances =  accruedLeaveDay - approvedLeaveSum; 
		String accruedLeaveDays = Double.toString(accruedLeaveDay);
		String leaveBalance = Double.toString(leaveBalances);

		//return leaveBalance;
		result.put("approvedLeaveSumed",approvedLeaveSumed );
		result.put("accruedLeaveDays", accruedLeaveDays);
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


	public static String getLeaveDuration(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		Date fromDate = null;
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
		log.info("LLLLLLLLL FROM : "+fromDate);
		log.info("LLLLLLLLL TO : "+thruDate);

		int leaveDuration = AccHolderTransactionServices.calculateWorkingDaysBetweenDates(fromDate, thruDate);
		
		result.put("leaveDuration", leaveDuration);

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
	
	public static String  getLeaveEnd(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Date fromDate = null;
		try {
			fromDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		
		int leaveDuration = new Integer(request.getParameter("leaveDuration")).intValue();

		LocalDateTime dateFromDate = new LocalDateTime(fromDate.getTime());

		Date endDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveDuration);
		
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18ThruDate = sdfDisplayDate.format(endDate);
	    String thruDate = sdfDate.format(endDate);
		
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
public static void main(String[] args){
	
	
}
}

