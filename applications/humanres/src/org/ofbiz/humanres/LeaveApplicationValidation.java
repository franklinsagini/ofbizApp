package org.ofbiz.humanres;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
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

/****
 * @author Japheth Odonya @when Oct 6, 2014 7:09:13 PM
 * 
 *         Member Validations org.ofbiz.party.party.MemberValidation
 *         uniqueFieldsValidation idNumber pinNumber payrollNumber mobileNumber
 *         employeeNumber
 * */
public class LeaveApplicationValidation {
	

	public static String leaveValidation(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String leaveTypeId = new String((request.getParameter("leaveTypeId")).toString());
		String partyId = new String(request.getParameter("partyId")).toString();
		int leaveDuration = new Integer(request.getParameter("leaveDuration")).intValue();
		Date fromDate = null;

		try {
			fromDate = (Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
					.getParameter("fromDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		
		/*if (leaveTypeId=="ANNUAL_LEAVE") {*/
			result.put("GenderState", getGenderState(leaveTypeId, partyId));
			result.put("NoticePeriodState",	getNoticePeriodState(leaveTypeId, fromDate));
			result.put("durationState", getLeaveDurationState(leaveTypeId, leaveDuration));
			/*result.put("onceAyearState", getLeaveOnceAyearState(partyId, fromDate));*/
	  /*   } 
		
		else if(leaveTypeId!="ANNUAL_LEAVE") {
			result.put("GenderState", getGenderState(leaveTypeId, partyId));
			result.put("NoticePeriodState",	getNoticePeriodState(leaveTypeId, fromDate));
			result.put("durationState", getLeaveDurationState(leaveTypeId, leaveDuration));
			result.put("onceAyearState", "VALID");
		}
		*/

		

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

	public static String getGenderState(String leaveTypeId, String partyId) {
		String gender = null;
		String usergender = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> leaveELI = null;
		GenericValue leaveType = null;

		List<GenericValue> genderELI = null;
		GenericValue userGender = null;
		try {
			leaveELI = delegator.findList("EmplLeaveType",
					EntityCondition.makeCondition("leaveTypeId", leaveTypeId),
					null, null, null, false);

			genderELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);

			if ((leaveELI.size() > 0) && (genderELI.size() > 0)) {
				leaveType = leaveELI.get(0);
				userGender = genderELI.get(0);
				gender = leaveType.getString("gender");
				usergender = userGender.getString("gender");

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String state = "";
		if ((gender.equalsIgnoreCase(usergender))
				|| (gender.equalsIgnoreCase("all"))) {

			state = "VALID";

		} else {

			state = "INVALID";
		}
		return state;

	}

	public static String getNoticePeriodState(String leaveTypeId,
			Date fromDatefd) {

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long noticeperiod = null;
		Timestamp now = UtilDateTime.nowTimestamp();
		/*
		 * String now2=now.toString(); Date now3=null; try { now3 = (Date)(new
		 * SimpleDateFormat("yyyy-MM-dd").parse(now2)); } catch (ParseException
		 * e2) { e2.printStackTrace(); }
		 */
		int userGivenNotice = AccHolderTransactionServices
				.calculateWorkingDaysBetweenDates(now, fromDatefd);

		List<GenericValue> leaveELI = null;
		GenericValue leaveType = null;
		try {
			leaveELI = delegator.findList("EmplLeaveType",
					EntityCondition.makeCondition("leaveTypeId", leaveTypeId),
					null, null, null, false);

			if ((leaveELI.size() > 0)) {
				leaveType = leaveELI.get(0);
				noticeperiod = leaveType.getLong("noticeperiod");

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String state = "";
		if (userGivenNotice >= noticeperiod) {

			state = "VALID";

		} else {
			state = "INVALID";
		}
		return state;

	}

	public static String getLeaveDurationState(String leaveTypeId,
			int userDuration) {
		Long minDuration = null;
		Long maxDuration = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> leaveELI = null;
		GenericValue leaveType = null;
		try {
			leaveELI = delegator.findList("EmplLeaveType",
					EntityCondition.makeCondition("leaveTypeId", leaveTypeId),
					null, null, null, false);

			if (leaveELI.size() > 0) {
				leaveType = leaveELI.get(0);
				minDuration = leaveType.getLong("minDays");
				maxDuration = leaveType.getLong("maxDays");

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		String state = "";
		if ((userDuration <= maxDuration) && (userDuration >= minDuration)) {

			state = "VALID";

		} else {

			state = "INVALID";
		}
		return state;

	}
	
	
		public static String getLeaveOnceAyearState(String partyId, Date from) {

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			LocalDateTime today = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
			int thisYear = today.getYear();
			String currentYear = Integer.toString(thisYear);
			String userYear=null;
			Date fromDate=null;
			int approvedLeaveYear=0;
			
			 List<GenericValue> getLeaveELI=null;
			 GenericValue leave = null;
			
			EntityConditionList<EntityExpr> getLeave = EntityCondition
					.makeCondition(UtilMisc.toList(
//					    EntityCondition.makeCondition("approvalStatus", EntityOperator.EQUALS, "Approved"),
						EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, "ANNUAL_LEAVE"),null),EntityOperator.AND);

			try {
				List<String> orderByList = new ArrayList<String>();
				orderByList.add("-leaveId");
				
				
				 getLeaveELI = delegator.findList("EmplLeave",
						getLeave, null, null, null, false);
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
				
			}
			
				if ((getLeaveELI.size() > 0)) {
					leave = getLeaveELI.get(0);
					fromDate = leave.getDate("fromDate");
					String LeaveYear=leave.getString("financialYear");
					approvedLeaveYear=Integer.valueOf(LeaveYear);

				}
     
			LocalDateTime fromb = new LocalDateTime(from);
			int userfrom = fromb.getYear();
			userYear = Integer.toString(userfrom);
			String state = "";
			if (userfrom==approvedLeaveYear) {

				state = "INVALID";

			} else if(fromb.isBefore(today)) {
				state = "PAST";
			
		  } else {
			state = "VALID";
		  }
			return state;

		}
}
