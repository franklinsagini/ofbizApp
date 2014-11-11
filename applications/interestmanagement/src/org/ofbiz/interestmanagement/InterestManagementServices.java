package org.ofbiz.interestmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
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
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.calendar.RecurrenceRule;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class InterestManagementServices {
	public static Logger log = Logger.getLogger(InterestManagementServices.class);
	
	public static String getFixedDepositContractDuration(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		Date startDate = null;
		try {
			startDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("startDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Date endDate = null;
		try {
			endDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("endDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		Logger log = Logger.getLogger(InterestManagementServices.class);
		log.info("LLLLLLLLL FROM : "+startDate);
		log.info("LLLLLLLLL TO : "+endDate);
		
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);

		int periodInMonths = Months.monthsBetween(localDateStartDate, localDateEndDate).getMonths();
		
		result.put("periodInMonths", periodInMonths);

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
	
	public static String  getFixedDepositContractEndDate(HttpServletRequest request,
			HttpServletResponse response) {
		
		Map<String, Object> result = FastMap.newInstance();
		Date startDate = null;
		
		try {
			startDate = (Date)(new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("startDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		
		int periodInMonths = new Integer(request.getParameter("periodInMonths")).intValue();

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());
		localDateEndDate = localDateEndDate.plusMonths(periodInMonths);
		//Date endDate = AccHolderTransactionServices.calculateEndWorkingDay(fromDate, leaveDuration);
		
		
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		
		String i18EndDate = sdfDisplayDate.format(localDateEndDate.toDate());
	    String endDate = sdfDate.format(localDateEndDate.toDate());
		
	    result.put("endDate_i18n", i18EndDate);
	    result.put("endDate", endDate);
	    Gson gson = new Gson();
		String json = gson.toJson(result);

		response.setContentType("application/x-json");
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
	
	public static Map<String, Object> calculateFixedDepositInterest(DispatchContext context, Map<String, String> map){
		Map<String, Object> result = new HashMap<String, Object>();
		System.out.println("############## Attempting to calculate Fixed Deposit Interest ... "+Calendar.getInstance().getTime());
		
		//Get all fixed deposit active contracts
		//FixedDepositContract
		processFixedDepositContracts();
		
		return result;
	}
	
	private static void processFixedDepositContracts() {
		//Get all fixed deposit contracts not closed
		EntityConditionList<EntityExpr> contractsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"closed", EntityOperator.EQUALS,
						"NO")),
						EntityOperator.AND);
		List<GenericValue> fixedDepositContractELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			fixedDepositContractELI = delegator.findList("FixedDepositContract",
					contractsConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (fixedDepositContractELI == null) {
			log.info(" ######### There are no fixed deposit contracts #########");
		}

		for (GenericValue fixedDepositContract : fixedDepositContractELI) {
			//for each fixed deposit contract, check if there is a run for this month and add an earning if there is none
			computeInterest(fixedDepositContract);
		}
		
	}

	private static void computeInterest(GenericValue fixedDepositContract) {
		BigDecimal bdAmount = fixedDepositContract.getBigDecimal("amount");
		BigDecimal bdInterestRatePA =  fixedDepositContract.getBigDecimal("interestRatePA");
		Long periodInMonths =  fixedDepositContract.getLong("periodInMonths");
		Timestamp startDate = fixedDepositContract.getTimestamp("startDate");
		Timestamp endDate =  fixedDepositContract.getTimestamp("endDate");
		
		Long fixedDepositContractId = fixedDepositContract.getLong("fixedDepositContractId");
		Long memberAccountId = fixedDepositContract.getLong("memberAccountId");
		// Check if there is a run for this day and add interest if there is none
		
		Boolean earningExist = false;
		
		earningExist = checkEarningExist(fixedDepositContractId, memberAccountId);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long fixedDepositContractEarningId = delegator.getNextSeqIdLong("FixedDepositContractEarning", 1);
		GenericValue fixedDepositContractEarning = null;
		BigDecimal bdAmountEarned = BigDecimal.ZERO;
		
		if (!earningExist){
			log.info("AAAAAAAAAA Adding !!");
			bdAmountEarned = calculateFixedDepositEarning(bdAmount,bdInterestRatePA, periodInMonths);
			//Add earning
			fixedDepositContractEarning = delegator.makeValue("FixedDepositContractEarning", UtilMisc
					.toMap("fixedDepositContractEarningId", fixedDepositContractEarningId,
							"fixedDepositContractId", fixedDepositContractId,
							"isActive","Y",
							"createdBy","admin",
							"dateEarned", new Timestamp(Calendar.getInstance().getTimeInMillis()), 
							
							"amount", bdAmountEarned.setScale(6, RoundingMode.HALF_UP)
					));
			try {
				delegator.createOrStore(fixedDepositContractEarning);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			
		}
		
	}

	/***
	 * Compute daily Fixed Deposit Earning
	 * */
	private static BigDecimal calculateFixedDepositEarning(BigDecimal bdAmount,
			BigDecimal bdInterestRatePA, Long periodInMonths) {
		//INTEREST = P * R
		//amount = P = bdAmount
		//T is 1/365
		//Interest
		BigDecimal bdDailyInterestRate = bdInterestRatePA.divide(new BigDecimal(100), 6, RoundingMode.HALF_UP).divide(new BigDecimal(365), 6, RoundingMode.HALF_UP);
		
		BigDecimal bdEarnedAmount =  bdAmount.multiply(bdDailyInterestRate);
		return bdEarnedAmount;
	}

	private static Boolean checkEarningExist(Long fixedDepositContractId,
			Long memberAccountId) {
		
		LocalDateTime startOfDayDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		startOfDayDateTime = startOfDayDateTime.withTime(0, 0, 0, 0);
		LocalDateTime endOfDayDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis());
		endOfDayDateTime = endOfDayDateTime.plusDays(1).withTime(0, 0, 0, 0);
		EntityConditionList<EntityExpr> earningConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"fixedDepositContractId", EntityOperator.EQUALS,
						fixedDepositContractId), EntityCondition.makeCondition(
								"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
								new Timestamp(startOfDayDateTime.toDate().getTime())),  EntityCondition.makeCondition(
									"createdStamp", EntityOperator.LESS_THAN_EQUAL_TO,
									new Timestamp(endOfDayDateTime.toDate().getTime()))),
						EntityOperator.AND);
		List<GenericValue> fixedDepositContractEarningELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {

			fixedDepositContractEarningELI = delegator.findList("FixedDepositContractEarning",
					earningConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		
		if ((fixedDepositContractEarningELI == null) || (fixedDepositContractEarningELI.size() <= 0)){
			log.info("NNNNNNNNNNNNNNNN Today Earning not added, am going to add it !!!!!!!!!!!!! ");
			return false;
		} else{
			log.info("GGGGGGGGGGGGGGG Today earning already added Nothing to do ");
		}

		return true;
	}

	public static String scheduleInterestServices(HttpServletRequest request,
			HttpServletResponse response){
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory()).createLocalDispatcher("interestcalculations", delegator);
		
		Map<String, String> context = UtilMisc.toMap("message", "Interest Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			//result = dispatcher.runSync("calculateInterestEarned", context);
			//dispatcher.schedule("calculateInterestEarned", startTime, context);
			int frequency = RecurrenceRule.SECONDLY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule("calculateInterestEarned", context, startTime, frequency, interval, count);
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

}
