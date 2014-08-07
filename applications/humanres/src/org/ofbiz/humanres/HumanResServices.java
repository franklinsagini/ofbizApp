package org.ofbiz.humanres;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.joda.time.JodaTimePermission;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
//import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
//import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;


public class HumanResServices {

	private static int getLeaveDuration(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Date fromDate = (Date) request.getParameter("fromDate");
		Date thruDate = (Date) request.getParameter("thruDate");
		
		
		
		GenericValue loanProduct = null;
		LocalDateTime stfromDate = new LocalDateTime(fromDate.getTime());
		LocalDateTime stthruDate = new LocalDateTime(thruDate.getTime());

		PeriodType dayDay = PeriodType.days();

		Period difference = new Period(stfromDate, stthruDate, dayDay);

		int leaveDuration = difference.getDays();
		
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
	
	private static Date getLeaveEnd(Date fromDate, int leaveDuration) {

		LocalDateTime stfromDate = new LocalDateTime(fromDate.getTime());
		Days days = Days.days(leaveDuration);
		
		//int stleaveDuration = leaveDuration;
		LocalDateTime thruDate;
		return thruDate = stfromDate.plusDays(days);
		//DateTime thruDate = new DateTime().plusDays(stleaveDuration);
		
		
		//return thruDate;
		

	}

}
