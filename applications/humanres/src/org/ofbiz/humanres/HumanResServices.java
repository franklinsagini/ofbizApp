package org.ofbiz.humanres;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
//import org.ofbiz.webapp.event.EventHandlerException;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;
//import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
//import org.ofbiz.webapp.event.EventHandlerException;


public class HumanResServices {

	public static String getLeaveDuration(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		//request.getPa
		//yyyy-MM-dd
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
		//Days days = Days.days(leaveDuration);
		
		//int stleaveDuration = leaveDuration;
		LocalDateTime dateThruDate = dateFromDate.plusDays(leaveDuration);
		
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18ThruDate = sdfDisplayDate.format(dateThruDate.toDate());
	    String thruDate = sdfDate.format(dateThruDate.toDate());
		
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

}
