package org.ofbiz.humanres;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.sql.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
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
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.calendar.RecurrenceRule;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class HumanResServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);

	// ==============================================================
	public static String getLeaveBalance(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Timestamp now = UtilDateTime.nowTimestamp();
		String financialYear = LeaveServices.getCurrentYear(now);
		Date appointmentdate = null;
		try {
			appointmentdate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("appointmentdate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String leaveTypeId = new String(
				(request.getParameter("leaveTypeId")).toString());
		String partyId = new String(request.getParameter("partyId")).toString();
		// get current leave balance //

		List<GenericValue> getApprovedLeaveSumELI = null;
		GenericValue carryOverLeaveGV = null;
		try {
			carryOverLeaveGV = delegator.findOne("EmplCarryOverLost",
					UtilMisc.toMap("partyId", partyId), false);
			log.info("++++++++++++++carryOverLeaveGV++++++++++++++++"
					+ carryOverLeaveGV);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			;
		}
		/*
		 * double carryOverLeaveDays =
		 * carryOverLeaveGV.getDouble("carryOverLeaveDays");
		 */
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("financialYear",
								EntityOperator.EQUALS, financialYear),
						EntityCondition.makeCondition("leaveTypeId",
								EntityOperator.EQUALS, "ANNUAL_LEAVE"),
						EntityCondition.makeCondition("applicationStatus",
								EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);

		try {
			getApprovedLeaveSumELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			// e2.printStackTrace();
			return "Cannot Get approved leaves";
		}
		log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
				+ getApprovedLeaveSumELI);
		double approvedLeaveSum = 0;
		double usedLeaveDays = 0;
		double lostLeaveDays = 0;
		for (GenericValue genericValue : getApprovedLeaveSumELI) {
			approvedLeaveSum += genericValue.getDouble("leaveDuration");
		}
		log.info("============================================================"
				+ approvedLeaveSum);

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

		// ========= ==============================//

		LocalDateTime stappointmentdate = new LocalDateTime(appointmentdate);
		/*
		 * LocalDateTime stCurrentDate = new
		 * LocalDateTime(Calendar.getInstance().getTimeInMillis());
		 */

		LocalDateTime today = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		LocalDateTime firstDayOfYear = today.dayOfYear().withMinimumValue();

		log.info(" FFFFFFFFFFF First Day " + firstDayOfYear.toDate());
		LocalDateTime accrueStart;
		if (stappointmentdate.isBefore(firstDayOfYear)) {

			accrueStart = firstDayOfYear;
		} else {
			accrueStart = stappointmentdate;
		}
		LocalDateTime stCurrentDate = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());

		PeriodType monthDay = PeriodType.months();

		Period difference = new Period(accrueStart, stCurrentDate, monthDay);

		int months = difference.getMonths();
		/* String approvedLeaveSumed = Double.toString(approvedLeaveSum); */
		double accruedLeaveDay = months * accrualRate;
		/*
		 * double leaveBalances = accruedLeaveDay + carryOverLeaveDays -
		 * approvedLeaveSum;
		 */
		String accruedLeaveDays = Double.toString(accruedLeaveDay);
		/* String leaveBalance = Double.toString(leaveBalances); */

		// ==============CONSIDER LEAVE BALANCES=========================
		GenericValue getAnualLeaveBalanceELI = null;
		BigDecimal annualBal = BigDecimal.ZERO;
		BigDecimal annualUsed = BigDecimal.ZERO;
		BigDecimal annualCarryOver = BigDecimal.ZERO;
		BigDecimal annualAccrued = BigDecimal.ZERO;
		try {

			getAnualLeaveBalanceELI = delegator.findOne("LeaveBalances",
					UtilMisc.toMap("partyId", partyId), false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();

		}
		if (getAnualLeaveBalanceELI != null) {
			annualBal = getAnualLeaveBalanceELI
					.getBigDecimal("availableLeaveDays");
			annualUsed = getAnualLeaveBalanceELI.getBigDecimal("usedLeaveDays");
			annualCarryOver = getAnualLeaveBalanceELI
					.getBigDecimal("LeaveDaysCarriedOver");
			annualAccrued = getAnualLeaveBalanceELI
					.getBigDecimal("accruedDays");

		} else {
			annualBal = new BigDecimal(accruedLeaveDay);
			annualAccrued = new BigDecimal(accruedLeaveDay);
		}

		// return leaveBalance;
		result.put("approvedLeaveSumed", annualUsed);
		result.put("accruedLeaveDays", annualAccrued);
		result.put("leaveBalance", annualBal);
		result.put("carryOverLeaveDays", annualCarryOver);

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

	/*
	 * ============================COMPASSIONATE LEAVE
	 * BALANCES========================================
	 */
	/*
	 * public static String getCompassionateLeaveBalance(HttpServletRequest
	 * request,HttpServletResponse response) { Map<String, Object> result =
	 * FastMap.newInstance(); Delegator delegator = (Delegator)
	 * request.getAttribute("delegator"); Timestamp now =
	 * UtilDateTime.nowTimestamp(); String
	 * financialYear=LeaveServices.getCurrentYear(now);
	 * 
	 * 
	 * String partyId = new String(request.getParameter("partyId")).toString();
	 * // get current leave balance //
	 * 
	 * List<GenericValue> getApprovedLeaveSumELI = null;
	 * EntityConditionList<EntityExpr> leaveConditions = EntityCondition
	 * .makeCondition(UtilMisc.toList( EntityCondition.makeCondition( "partyId",
	 * EntityOperator.EQUALS, partyId),
	 * EntityCondition.makeCondition("financialYear",EntityOperator.EQUALS,
	 * financialYear),
	 * EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS,
	 * "COMPASSIONATE_LEAVE"),
	 * EntityCondition.makeCondition("applicationStatus", EntityOperator.EQUALS,
	 * "Approved")), EntityOperator.AND);
	 * 
	 * try { getApprovedLeaveSumELI = delegator.findList("EmplLeave",
	 * leaveConditions, null, null, null, false); } catch
	 * (GenericEntityException e2) { e2.printStackTrace();
	 * 
	 * } //log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+
	 * getApprovedLeaveSumELI); double approvedLeaveSum=0; for (GenericValue
	 * genericValue : getApprovedLeaveSumELI) { //approvedLeaveSum +=
	 * genericValue.getLong("leaveDuration"); approvedLeaveSum +=
	 * genericValue.getDouble("leaveDuration"); }
	 * //log.info("============================================================"
	 * +approvedLeaveSum);
	 * 
	 * // ============ get accrual rate ================ // Long days=null;
	 * GenericValue employeeLeaveType = null; try { employeeLeaveType =
	 * delegator.findOne("EmplLeaveType", UtilMisc.toMap("leaveTypeId",
	 * "COMPASSIONATE_LEAVE"), false); } catch (GenericEntityException e) {
	 * e.printStackTrace();
	 * 
	 * } if (employeeLeaveType != null) {
	 * 
	 * days = employeeLeaveType.getLong("days");
	 * 
	 * } else { System.out.println("######## Days not found #### "); }
	 * 
	 * GenericValue getCompassionateLeaveBalanceELI = null ;
	 * 
	 * 
	 * double carryOverLeaveDays = 0, leaveBalances = 0;
	 * 
	 * try {
	 * 
	 * getCompassionateLeaveBalanceELI =
	 * delegator.findOne("CompassionateLeaveBalances",
	 * UtilMisc.toMap("partyId",partyId), false);
	 * 
	 * } catch (GenericEntityException e2) { e2.printStackTrace();
	 * 
	 * }
	 * 
	 * if (getCompassionateLeaveBalanceELI!=null) {
	 * leaveBalances=getCompassionateLeaveBalanceELI
	 * .getDouble("availableLeaveDays");
	 * carryOverLeaveDays=getCompassionateLeaveBalanceELI
	 * .getDouble("carriedOverDays");
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * //String leaveBalance = Double.toString(leaveBalances);
	 * 
	 * //return leaveBalance; result.put("approvedLeaveSumed",approvedLeaveSum
	 * ); result.put("accruedLeaveDays", days); result.put("leaveBalance" ,
	 * leaveBalances); result.put("carryOverLeaveDays" , carryOverLeaveDays);
	 * 
	 * Gson gson = new Gson(); String json = gson.toJson(result);
	 * 
	 * // set the X-JSON content type
	 * response.setContentType("application/x-json"); // jsonStr.length is not
	 * reliable for unicode characters try {
	 * response.setContentLength(json.getBytes("UTF8").length); } catch
	 * (UnsupportedEncodingException e) { try { throw new
	 * EventHandlerException("Problems with Json encoding", e); } catch
	 * (EventHandlerException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); } }
	 * 
	 * // return the JSON String Writer out; try { out = response.getWriter();
	 * out.write(json); out.flush(); } catch (IOException e) { try { throw new
	 * EventHandlerException( "Unable to get response writer", e); } catch
	 * (EventHandlerException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); } }
	 * 
	 * return json;
	 * 
	 * }
	 */

	// ==============================================================

	public static String getLeaveDuration(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String leaveTypeId = (String) request.getParameter("leaveTypeId")
				.toString();
		int leaveDuration = 0;
		Date fromDate = null;
		GenericValue getLeaveDayTypeELI = null;
		String daytype = "";
		String hasbalance = "";
		int res = 2;
		try {
			fromDate = (Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
					.getParameter("fromDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Date thruDate = null;
		try {
			thruDate = (Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
					.getParameter("thruDate")));
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		Logger log = Logger.getLogger(HumanResServices.class);
		log.info("=================================LLLLLLLLL leaveTypeId : "
				+ leaveTypeId);
		log.info("======================================LLLLLLLLL FROM : "
				+ fromDate);
		log.info("======================================LLLLLLLLL TO : "
				+ thruDate);

		try {

			getLeaveDayTypeELI = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", leaveTypeId), false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();

		}
		if (getLeaveDayTypeELI != null) {
			daytype = getLeaveDayTypeELI.getString("daytype");
			hasbalance = getLeaveDayTypeELI.getString("hasbalance");

		} else {
			String errorMsg = "================================NOTHING FOUND HERE===============";
		}

		if (daytype.equalsIgnoreCase("Work_days")) {
			leaveDuration = calculateWorkingNonHolidayDaysBetweenDates(
					fromDate, thruDate);

		} else if (daytype.equalsIgnoreCase("Calendar_days")) {
			leaveDuration = calculateCalenderDaysBetweenDates(fromDate,
					thruDate);

		}

		String indicator = null;

		if (hasbalance.equalsIgnoreCase("Yes")) {

			indicator = "Y";
		} else if (hasbalance.equalsIgnoreCase("No")) {
			indicator = "N";

		}

		Date resumeDate = calculateEndWorkingDay(thruDate, res);
		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

		String i18resumptionDate = sdfDisplayDate.format(resumeDate);
		String resumptionDate = sdfDate.format(resumeDate);

		result.put("resumptionDate_i18n", i18resumptionDate);
		result.put("resumptionDate", resumptionDate);

		result.put("leaveDuration", leaveDuration);
		result.put("hasBalance", indicator);

		log.info("======================================leaveDuration :=== "
				+ leaveDuration);
		log.info("======================================hasBalance :==== "
				+ indicator);
		log.info("======================================resumeDate :==== "
				+ resumptionDate);

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

	public static String getLeaveEnd(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String leaveTypeId = (String) request.getParameter("leaveTypeId")
				.toString();
		Date fromDate = null;
		String daytype = "";
		GenericValue getLeaveDayTypeELI = null;
		Date endDate = null;
		Date resumeDate = null;

		try {
			fromDate = (Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
					.getParameter("fromDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		BigDecimal leaveDurationBid = new BigDecimal(request.getParameter("leaveDuration"));
        
		if(leaveDurationBid.compareTo(BigDecimal.ONE) < 0 ){
			LocalDate localDateEndDate = new LocalDate(fromDate.getTime());
			endDate = fromDate;
			resumeDate = endDate;
			
			log.info("=================================LLLLLLLLL leaveTypeId : "
					+ leaveTypeId);
			log.info("======================================LLLLLLLLL FROM : "
					+ fromDate);
			log.info("==================================Duration BIGD====TO : "
					+ leaveDurationBid);
			log.info("==================================Resumption====TO : "
					+ resumeDate);

			
			
		}
		else if (leaveDurationBid.compareTo(BigDecimal.ONE) > 0  || leaveDurationBid.equals(BigDecimal.ONE)){
		int leaveDuration = leaveDurationBid.intValue();
		
		Logger log = Logger.getLogger(HumanResServices.class);
		log.info("=================================LLLLLLLLL leaveTypeId : "
				+ leaveTypeId);
		log.info("======================================LLLLLLLLL FROM : "
				+ fromDate);
		log.info("======================================LLLLLLLLL TO : "
				+ leaveDuration);
		log.info("==================================Duration BIGD====TO : "
				+ leaveDurationBid);
		log.info("==================================Resumption====TO : "
				+ resumeDate);

		try {

			getLeaveDayTypeELI = delegator.findOne("EmplLeaveType",
					UtilMisc.toMap("leaveTypeId", leaveTypeId), false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();

		}
		if (getLeaveDayTypeELI != null) {
			daytype = getLeaveDayTypeELI.getString("daytype");

			log.info("======================================DAYTYPE : "
					+ daytype);

			if (daytype.equalsIgnoreCase("Calendar_days")) {
				endDate = calculateEndCalenderDay(fromDate, leaveDuration);
				int leaveTillResumption = leaveDuration + 1;
				resumeDate = calculateEndCalenderDay(fromDate,
						leaveTillResumption);

			} else if (daytype.equalsIgnoreCase("Work_days")) {
				endDate = calculateEndWorkingNonHolidayDay(fromDate,
						leaveDuration);
				int leaveTillResumption = leaveDuration + 1;
				resumeDate = calculateEndWorkingNonHolidayDay(fromDate,
						leaveTillResumption);

			}

		}
        
	}
		LocalDateTime dateFromDate = new LocalDateTime(fromDate.getTime());

		/*
		 * Date endDate =
		 * AccHolderTransactionServices.calculateEndWorkingDay(fromDate,
		 * leaveDuration);
		 * 
		 * int leaveTillResumption = leaveDuration+1; Date resumeDate =
		 * AccHolderTransactionServices.calculateEndWorkingDay(fromDate,
		 * leaveTillResumption);
		 */

		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("MM/dd/yyyy");
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

	public static String getBranches(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String bankDetailsId = (String) request.getParameter("bankDetailsId");
		// GenericValue saccoProduct = null;
		// EntityListIterator branchesELI;// =
		// delegator.findListIteratorByCondition("BankBranch", new
		// EntityExpr("bankDetailsId", EntityOperator.EQUALS, bankDetailsId),
		// null, UtilMisc.toList("bankBranchId", "branchName"), "branchName",
		// null);
		// branchesELI =
		// delegator.findListIteratorByCondition(dynamicViewEntity,
		// whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy,
		// findOptions)
		// branchesELI = delegator.findListIteratorByCondition("BankBranch", new
		// EntityExpr("productId", EntityOperator.NOT_EQUAL, null),
		// UtilMisc.toList("productId"), null);
		List<GenericValue> branchesELI = null;

		// branchesELI = delegator.findList("BankBranch", new EntityExpr(),
		// UtilMisc.toList("bankBranchId", "branchName"), null, null, null);
		try {
			// branchesELI = delegator.findList("BankBranch",
			// EntityCondition.makeConditionWhere("(bankDetailsId = "+bankDetailsId+")"),
			// null, null, null, false);
			branchesELI = delegator.findList("BankBranch", EntityCondition
					.makeCondition("bankDetailsId", bankDetailsId), null, null,
					null, false);

		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// SaccoProduct

		// Add Branches to a list

		if (branchesELI == null) {
			result.put("", "No Braches");
		}

		for (GenericValue genericValue : branchesELI) {
			result.put(genericValue.get("bankBranchId").toString(),
					genericValue.get("branchName"));
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

	public static String getConfirmationDate(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String employmentStatusEnumId = (String) request
				.getParameter("employmentStatusEnumId");
		Map<String, Object> result = FastMap.newInstance();
		Date appointmentdate = null;
		int periodBeforeConfirn = 0;

		try {
			appointmentdate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("appointmentdate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		GenericValue period = null;
		try {
			period = delegator.findOne("Enumeration",
					UtilMisc.toMap("enumId", employmentStatusEnumId), false);
			log.info("++++++++++++++period++++++++++++++++" + period);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (period != null) {
			periodBeforeConfirn = (period.getLong("periodBeforeConfirmation"))
					.intValue();
		} else {

		}

		LocalDate dateAppointmentDate = new LocalDate(appointmentdate);

		LocalDate confirmDate = dateAppointmentDate
				.plusMonths(periodBeforeConfirn);

		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

		String i18confirmationdate = sdfDisplayDate
				.format(confirmDate.toDate());
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

	public static String getRetirementDate(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Date birthDate = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			birthDate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("birthDate")));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		List<GenericValue> prefix = null;
		GenericValue pre = null;
		int retireAge = 0;

		try {

			prefix = delegator.findList("RetirementAge", null, null, null,
					null, false);

		} catch (GenericEntityException e) {
			return null;
		}

		if (prefix.size() > 0) {
			pre = prefix.get(0);
			retireAge = Integer.parseInt(pre.getString("retirementAge"));
		}

		LocalDate datebirthDate = new LocalDate(birthDate);

		LocalDate bodDate = datebirthDate.plusYears(retireAge);

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

	public static String getEndOfContractDate(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Date appointmentdate = null;
		int period = new Integer(request.getParameter("contractPeriod"))
				.intValue();

		try {
			appointmentdate = (Date) (new SimpleDateFormat("yyyy-MM-dd")
					.parse(request.getParameter("appointmentdate")));
			log.info("++++++++++++++>>>>>>>>>Appointment Date"
					+ appointmentdate + ">>>>>>>Period" + period);
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		LocalDate datebirthDate = new LocalDate(appointmentdate);

		LocalDate contEndDate = datebirthDate.plusMonths(period);
		LocalDate contractEndDate = contEndDate.minusDays(1);

		SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

		String i18contractEnd = sdfDisplayDate.format(contractEndDate.toDate());
		String contractEnd = sdfDate.format(contractEndDate.toDate());

		result.put("contractEnd_i18n", i18contractEnd);
		result.put("contractEnd", contractEnd);

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

	/*
	 * public static String NextPayrollNumber() {
	 * 
	 * Map<String, Object> result = FastMap.newInstance(); Delegator delegator =
	 * DelegatorFactoryImpl.getDelegator(null); String nextEmployeeNumber =
	 * null; String PayrollPrefix="HCS";
	 * 
	 * 
	 * List<GenericValue> employeeELI = null;
	 * 
	 * 
	 * GenericValue lastEmployee = null; List<GenericValue> prefix = null;
	 * GenericValue pre = null;
	 * 
	 * try {
	 * 
	 * prefix = delegator.findList("payRollPrefix",null, null, null, null,
	 * false);
	 * 
	 * } catch (GenericEntityException e) { return null; }
	 * 
	 * if (prefix.size() > 0){ pre = prefix.get(0); PayrollPrefix =
	 * pre.getString("payRollPrefix"); }
	 * 
	 * try { List<String> orderByList = new ArrayList<String>();
	 * orderByList.add("-createdStamp");
	 * 
	 * employeeELI = delegator.findList("RoleTypeAndPersonEmployeeAndBranch",
	 * EntityCondition.makeCondition("roleTypeId", "EMPLOYEE"), null,
	 * orderByList, null, false);
	 * 
	 * if (employeeELI.size() > 0){ lastEmployee = employeeELI.get(0); String
	 * emplNo=lastEmployee.getString("employeeNumber");
	 * 
	 * 
	 * String trancatemplNo= StringUtils.substring(emplNo, 3); int
	 * newEmplNo=Integer.parseInt(trancatemplNo)+1; String
	 * h=String.valueOf(newEmplNo); nextEmployeeNumber=PayrollPrefix.concat(h);
	 * 
	 * log.info("++++++++++++++newPayrollNo++++++++++++++++"
	 * +nextEmployeeNumber); } } catch (GenericEntityException e2) {
	 * e2.printStackTrace(); }
	 * 
	 * 
	 * 
	 * result.put("employeeNumber_i18n", nextEmployeeNumber);
	 * result.put("employeeNumber", nextEmployeeNumber);
	 * 
	 * return nextEmployeeNumber;
	 * 
	 * 
	 * }
	 */

	public static String NextStaffPayrollNumber(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Map<String, Object> result = FastMap.newInstance();
		String employmentTerms = new String(
				(request.getParameter("employmentTerms")).toString());

		String nextEmployeeNumber = null;
		String PayrollPrefix = "HCS";
		String contractPayrollPrefix = "CONT";
		String internPayrollPrefix = "INT";

		List<GenericValue> employeeELI = null;

		GenericValue lastEmployee = null;
		List<GenericValue> prefix = null;
		GenericValue pre = null;

		try {

			prefix = delegator.findList("payRollPrefix", null, null, null,
					null, false);

		} catch (GenericEntityException e) {
			return null;
		}

		if (prefix.size() > 0) {
			pre = prefix.get(0);
			PayrollPrefix = pre.getString("payRollPrefix");
		}

		EntityConditionList<EntityExpr> payRollConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"roleTypeId", EntityOperator.EQUALS, "EMPLOYEE"),
						EntityCondition.makeCondition("employmentTerms",
								EntityOperator.EQUALS, employmentTerms)),
						EntityOperator.AND);

		try {
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("-createdStamp");

			employeeELI = delegator.findList(
					"RoleTypeAndPersonEmployeeAndBranch", payRollConditions,
					null, orderByList, null, false);

			if (employeeELI.size() > 0) {
				lastEmployee = employeeELI.get(0);
				String emplNo = lastEmployee.getString("employeeNumber");
				String emplTerms = lastEmployee.getString("employmentTerms");

				if (employmentTerms.equalsIgnoreCase("permanent")) {

					String trancatemplNo = StringUtils.substring(emplNo, 3);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = PayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("contract")) {

					String trancatemplNo = StringUtils.substring(emplNo, 4);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = contractPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("intern")) {

					String trancatemplNo = StringUtils.substring(emplNo, 3);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = internPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				}
			} else if (employeeELI.size() == 0) {

				if (employmentTerms.equalsIgnoreCase("permanent")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = PayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("contract")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = contractPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("intern")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = internPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				}

			}

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		result.put("nextEmployeeNumber_i18n", nextEmployeeNumber);
		result.put("nextEmployeeNumber", nextEmployeeNumber);

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

	public static String NextPayrollNumber(String employmentTerms) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String nextEmployeeNumber = null;
		String PayrollPrefix = "HCS";
		String contractPayrollPrefix = "CONT";
		String internPayrollPrefix = "INT";

		List<GenericValue> employeeELI = null;

		GenericValue lastEmployee = null;
		List<GenericValue> prefix = null;
		GenericValue pre = null;

		try {

			prefix = delegator.findList("payRollPrefix", null, null, null,
					null, false);

		} catch (GenericEntityException e) {
			return null;
		}

		if (prefix.size() > 0) {
			pre = prefix.get(0);
			PayrollPrefix = pre.getString("payRollPrefix");
		}

		EntityConditionList<EntityExpr> payRollConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
				/*
				 * EntityCondition.makeCondition("roleTypeId",
				 * EntityOperator.EQUALS, "EMPLOYEE"),
				 */
				EntityCondition.makeCondition("employmentTerms",
						EntityOperator.EQUALS, employmentTerms)),
						EntityOperator.AND);

		try {
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("-createdStamp");

			employeeELI = delegator.findList(
					"RoleTypeAndPersonEmployeeAndBranch", payRollConditions,
					null, orderByList, null, false);

			if (employeeELI.size() > 0) {
				lastEmployee = employeeELI.get(0);
				String emplNo = lastEmployee.getString("employeeNumber");
				// String emplTerms=lastEmployee.getString("employmentTerms");

				if (employmentTerms.equalsIgnoreCase("permanent")) {

					String trancatemplNo = StringUtils.substring(emplNo, 3);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = PayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("contract")) {

					String trancatemplNo = StringUtils.substring(emplNo, 4);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = contractPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("intern")) {

					String trancatemplNo = StringUtils.substring(emplNo, 3);
					int newEmplNo = Integer.parseInt(trancatemplNo) + 1;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = internPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				}
			} else if (employeeELI.size() == 0) {

				if (employmentTerms.equalsIgnoreCase("permanent")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = PayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("contract")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = contractPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				} else if (employmentTerms.equalsIgnoreCase("intern")) {

					int newEmplNo = 1001;
					String h = String.valueOf(newEmplNo);
					nextEmployeeNumber = internPayrollPrefix.concat(h);

					log.info("++++++++++++++newPayrollNo++++++++++++++++"
							+ nextEmployeeNumber);

				}

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		result.put("employeeNumber_i18n", nextEmployeeNumber);
		result.put("employeeNumber", nextEmployeeNumber);

		return nextEmployeeNumber;

	}

	public static String showHideFields(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String leaveTypeId = new String(
				(request.getParameter("leaveTypeId")).toString());
		String deductedFromAnnual = null;
		String show = "";

		List<GenericValue> leaveELI = null;
		GenericValue leaveType = null;
		try {
			leaveELI = delegator.findList("EmplLeaveType",
					EntityCondition.makeCondition("leaveTypeId", leaveTypeId),
					null, null, null, false);

			if (leaveELI.size() > 0) {
				leaveType = leaveELI.get(0);
				deductedFromAnnual = leaveType
						.getString("isDeductedFromAnnual");

			}
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if (deductedFromAnnual.equalsIgnoreCase("y")) {
			show = ("SHOW");
		} else {
			show = "HIDE";

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

	public static String payrollUpperCase(String payRoll) {
		/*
		 * Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		 * deleteExistingPayrollPrefix(delegator);
		 */
		String upperCasePayroll = payRoll.toUpperCase();
		

		log.info("++++++++++++++upperCasePayroll++++++++++++++++"
				+ upperCasePayroll);
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
		log.info("DELETED  ALL RECORDS!");
		
	
	}

	public static String validatePayrollPrefix(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Map<String, Object> result = FastMap.newInstance();
		String payrollPrefix = (String) request.getParameter("lowerCase");
		String howLong = "OK";

		log.info("++++++++++++++payrollPrefix++++++++++++++++" + payrollPrefix);

		int strLength = payrollPrefix.length();

		if (strLength != 3) {
			howLong = "NOTOK";
		} else {
			howLong = "OK";
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

	public static String validateAnnualResetDays(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		/*
		 * String lostDays= (String)request.getParameter("annualLostLeaveDays");
		 * String daysToReset=
		 * (String)request.getParameter("annualLeaveDaysLost");
		 */
		int lostDays = new Integer(request.getParameter("lostLeaveDays"))
				.intValue();
		int daysToReset = new Integer(
				request.getParameter("annualLeaveDaysLost")).intValue();

		String state = "GGGGGG";

		log.info("++++++++++++++lostDays++++++++++++++++" + lostDays);
		log.info("++++++++++++++daysToReset++++++++++++++++" + daysToReset);

		if (daysToReset > lostDays) {
			state = "MORE";
		} else if (daysToReset < 0) {
			state = "LITTLE";
		} else {
			state = "INVALID";
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
	
	public static String getDateToday(Date confirmDate) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		LocalDateTime today = new LocalDateTime(Calendar.getInstance()
				.getTimeInMillis());
		LocalDateTime confirm = new LocalDateTime(confirmDate);
		String state = "";

		if (today.isAfter(confirm)) {
			state = "OVERDUE";
		} else if (today.isBefore(confirm)) {
			state = "NOT YET";

		} else if (today.equals(confirm)) {
			state = "DUE";

		}

		log.info("+++++++++++++++++++++++Today: " + today);
		log.info("+++++++++++++++++++++++confirm: " + confirm);
		log.info("+++++++++++++++++++++++state: " + state);
		return state;
	}

	public static Map<String, Object> dismissStaffOnProbation(
			DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String partyId = (String) context.get("partyId");

		log.info("+++++++++++++++++++++++PARTYID: " + partyId);

		try {
			delegator.removeByAnd("Person", UtilMisc.toMap("partyId", partyId));
			delegator.removeByAnd("PartyRole",
					UtilMisc.toMap("partyId", partyId));
			delegator.removeByAnd("userLogin",
					UtilMisc.toMap("partyId", partyId));
			delegator.removeByAnd("Party", UtilMisc.toMap("partyId", partyId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
			// if this fails no problem
		}

		result.put("partyId", partyId);
		result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
		return result;

	}

	public static Date calculateEndWorkingDay(Date startDate, int noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 1;
		while (count < noOfDays) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}

		return localDateEndDate.toDate();
	}

	public static Date calculateEndWorkingNonHolidayDay(Date startDate,
			int noOfDays) {

		LocalDate localDateEndDate = new LocalDate(startDate.getTime());

		// If this is happening on sunday or saturday push it to start on monday
		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(1);
		}
		// Calculate End Date
		int count = 1;
		// noOfDays = noOfDays - 1;
		while (count < noOfDays) {
			if (localDateEndDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
				localDateEndDate = localDateEndDate.plusDays(3);
			} else {
				localDateEndDate = localDateEndDate.plusDays(1);
			}
			count++;
		}

		int noOfHolidays = getNumberOfHolidays(startDate, noOfDays);
		log.info("=============== NUMBER OF HOLIDAYS" + noOfHolidays);
		localDateEndDate = localDateEndDate.plusDays(noOfHolidays);

		if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		} else if (localDateEndDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			localDateEndDate = localDateEndDate.plusDays(2);
		}

		return localDateEndDate.toDate();
	}

	/*****
	 * Calculate Holidays from start date given number of days
	 * */
	private static int getNumberOfHolidays(Date startDate, int noOfDays) {
		int holidayCount = 0;
		LocalDate localDateEndDate = new LocalDate(startDate.getTime());
		LocalDate localDateHoliday;
		List<Date> listHolidays = getHolidayList();
		int count;
		for (Date date : listHolidays) {
			count = 0;
			localDateEndDate = new LocalDate(startDate.getTime());
			localDateHoliday = new LocalDate(date.getTime());
			while (count < noOfDays) {

				if ((localDateEndDate.getDayOfMonth() == localDateHoliday
						.getDayOfMonth())
						&& (localDateEndDate.getMonthOfYear() == localDateHoliday
								.getMonthOfYear())
						&& (localDateEndDate.getDayOfWeek() != DateTimeConstants.SATURDAY)) {
					holidayCount++;
				}

				localDateEndDate = localDateEndDate.plusDays(1);
				count++;
			}
		}

		return holidayCount;
	}

	public static int getNumberOfHolidays(Date startDate, Date endDate) {
		int holidayCount = 0;
		LocalDate localDateStartDate = new LocalDate(startDate.getTime());
		LocalDate localDateEndDate = new LocalDate(endDate.getTime());
		LocalDate localDateHoliday;
		List<Date> listHolidays = getHolidayList();
		for (Date date : listHolidays) {
			// localDateEndDate = new LocalDate(startDate.getTime());
			localDateHoliday = new LocalDate(date.getTime());
			while (localDateStartDate.toDate()
					.before(localDateEndDate.toDate())) {

				if ((localDateStartDate.getDayOfMonth() == localDateHoliday
						.getDayOfMonth())
						&& (localDateStartDate.getMonthOfYear() == localDateHoliday
								.getMonthOfYear())
						&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)) {
					holidayCount++;
				}

				localDateStartDate = localDateStartDate.plusDays(1);
			}
		}

		return holidayCount;
	}

	private static List<Date> getHolidayList() {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<Date> listHolidayList = new ArrayList<Date>();
		List<GenericValue> holidaysELI = null;
		try {
			holidaysELI = delegator.findAll("PublicHolidays", true);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			listHolidayList.add(genericValue.getDate("holidayDate"));
		}

		return listHolidayList;
	}

	public static int calculateWorkingDaysBetweenDates(Date startDate,
			Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);
		Date holidayDate;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDate holiday = null;
		List<GenericValue> holidaysELI = null;
		int holiDate = 0, holiMonth = 0, statDate = 0, statMonth = 0;
		try {
			holidaysELI = delegator.findAll("PublicHolidays", true);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : holidaysELI) {
			holidayDate = genericValue.getDate("holidayDate");

			holiday = new LocalDate(holidayDate);

			holiDate = holiday.getDayOfMonth();
			holiMonth = holiday.getMonthOfYear();
			statDate = localDateStartDate.getDayOfMonth();
			statMonth = localDateStartDate.getMonthOfYear();

			log.info("++++++++++++++holidays++++++++++++++++" + holiday);
			log.info("++++++++++++++holiDate++++++++++++++++" + holiDate);
			log.info("++++++++++++++holiMonth++++++++++++++++" + holiMonth);
			log.info("++++++++++++++leavestart++++++++++++++++"	+ localDateStartDate);
			log.info("++++++++++++++statDate++++++++++++++++" + statDate);
			log.info("++++++++++++++endMonth++++++++++++++++" + statMonth);
		}

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)
					&& (localDateStartDate.getDayOfMonth() != holiday
							.getDayOfMonth())
					&& (localDateStartDate.getMonthOfYear() != holiday
							.getMonthOfYear())
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return daysCount;
	}

	public static int calculateWorkingNonHolidayDaysBetweenDates(
			Date startDate, Date endDate) {
		int daysCount = 1;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);
		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)

					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		int noOfHolidays = getNumberOfHolidays(startDate, endDate);

		daysCount = daysCount - noOfHolidays;

		return daysCount;
	}

	public static int calculateWorkingDaysBetweenDates2(Date startDate,
			Date endDate) {
		int daysCount = 1;
		int holi = 0;
		LocalDate localDateStartDate = new LocalDate(startDate);
		LocalDate localDateEndDate = new LocalDate(endDate);
		Date holidayDate;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDate holiday = null;
		List<GenericValue> holidaysELI = null;
		int holiDate = 0, holiMonth = 0, statDate = 0, statMonth = 0;

		EntityConditionList<EntityExpr> dateConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"holidayDate", EntityOperator.GREATER_THAN_EQUAL_TO,
						startDate), EntityCondition.makeCondition(
						"holidayDate", EntityOperator.LESS_THAN_EQUAL_TO,
						endDate)), EntityOperator.AND);

		try {
			/* holidaysELI = delegator.findAll("PublicHolidays", true);sas */
			holidaysELI = delegator.findList("PublicHolidays", dateConditions,
					null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValue : holidaysELI) {
			holi++;

			holiDate = holiday.getDayOfMonth();
			holiMonth = holiday.getMonthOfYear();
			statDate = localDateStartDate.getDayOfMonth();
			statMonth = localDateStartDate.getMonthOfYear();

			log.info("++++++++++++++holidays++++++++++++++++" + holi);
			log.info("++++++++++++++holiDate++++++++++++++++" + holiDate);
			log.info("++++++++++++++holiMonth++++++++++++++++" + holiMonth);
			log.info("++++++++++++++leavestart++++++++++++++++"
					+ localDateStartDate);
			log.info("++++++++++++++statDate++++++++++++++++" + statDate);
			log.info("++++++++++++++endMonth++++++++++++++++" + statMonth);
		}

		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			}

			localDateStartDate = localDateStartDate.plusDays(1);
		}

		return (daysCount - holi);
	}

	public static Boolean isHoliday(Date date) {
		// LocalDate localDateStartDate = new LocalDate(startDate);
		// LocalDate localDateEndDate = new LocalDate(endDate);
		LocalDate localDate = new LocalDate(date);
		// =================holiday
		// consideration===========================================
		Date holidays = null;
		Boolean isHoliday = false;
		int holiDate = 0, holiMonth = 0, statDate = 0, statMonth = 0;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> holidaysELI = null;
		try {
			holidaysELI = delegator.findAll("PublicHolidays", true);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		log.info("++++++++++++++date++++++++++++++++" + localDate);

		LocalDate holidayDate = null;
		for (GenericValue genericValue : holidaysELI) {
			holidayDate = new LocalDate(genericValue.getDate("holidayDate"));

			if ((localDate.getDayOfMonth() == holidayDate.getDayOfMonth())
					&& (localDate.getMonthOfYear() == holidayDate
							.getMonthOfYear())) {
				return true;
			}
		}

		return isHoliday;

	}

	public static String getBranchDepartments(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String branchId = (String) request.getParameter("branchId");
		List<GenericValue> departmentsELI = null;

		try {

			departmentsELI = delegator.findList("department",
					EntityCondition.makeCondition("branchId", branchId), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (departmentsELI == null) {
			result.put("", "No departments");
		}

		for (GenericValue genericValue : departmentsELI) {
			result.put(genericValue.get("departmentId").toString(),
					genericValue.get("departmentName"));
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

	public static String isEntryInteger(String text) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String state = null;
		try {
			Integer.parseInt(text);
			state = "VALID";
			deleteExistingRetirementAge(delegator);
		} catch (NumberFormatException e) {
			state = "INVALID";
		}
		return state;
	}

	private static void deleteExistingRetirementAge(Delegator delegator) {
		// TODO Auto-generated method stub
		log.info("######## Tyring to Delete ######## !!!");

		try {
			delegator.removeAll("RetirementAge");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("DELETED  ALL RECORDS!");

	}

	public static String CheckExistenceOfQualitativeIndicators() {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue lastEmployee = null;
		List<GenericValue> indicators = null;
		GenericValue pre = null;
		String state = null;

		try {

			/*
			 * indicators = delegator.findList("PerfGoals",null, null, null,
			 * null, false);
			 */
			indicators = delegator.findList("PerfGoals", EntityCondition
					.makeCondition("perfGoalsDefId", "QTT_GOALS"), null, null,
					null, false);

		} catch (GenericEntityException e) {
			return null;
		}

		if (indicators.size() > 0) {
			/* pre = indicators.get(0); */
			state = "VALID";
		} else {
			state = "INVALID";
		}
		return state;
	}

	// ================================ CHECK EXISTANCE OF A STAFF IN A GROUP
	// ====================================================

	public static String findExistanceOfStaffInReviewGroups(String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String state = null;
		List<GenericValue> FileLI = null;
		try {
			FileLI = delegator.findList("StaffInPerfReviewGroup",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (FileLI.size() > 0) {
			state = "INVALID";
		} else {
			state = "VALID";
		}

		return state;

	}

	// ================================ CHECK EXISTANCE OF A YEAR IN REVIEW
	// GROUPS ====================================================

	public static String findExistanceOfYear(String party) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String state = null;
		List<GenericValue> FileLI = null;
		try {
			FileLI = delegator.findList("PerfReviewPeriod",
					EntityCondition.makeCondition("year", party), null, null,
					null, false);

		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (FileLI.size() > 0) {
			state = "INVALID";
		} else {
			state = "VALID";
		}

		return state;

	}

	// ================================ CHECK EXISTANCE OF AN OPEN REVIEW PERIOD
	// ====================================================

	public static String findExistanceOfOpenPeriod() {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		String state = null;
		List<GenericValue> FileLI = null;
		try {
			FileLI = delegator.findList("PerfReviewPeriod",
					EntityCondition.makeCondition("status", "OPEN"), null,
					null, null, false);

		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (FileLI.size() > 0) {
			state = "INVALID";
		} else {
			state = "VALID";
		}

		return state;

	}

	public static String getTotalPartyPerformance(String party, String year) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String state = null;
		ResultSet rs1;
		String quarter = year + "-Quarter-1";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"scoreOne").stripTrailingZeros());
			count++;

		}

		String f = String.valueOf(totalpercentage);

		return f + "%";
	}

	// ========================== ALL STAFF QUARTERLY TOTALS
	// =================================

	public static BigDecimal getFirstQuarterTotalPartyPerformance(String party,	String year) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-1";
		int count = 0;
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {

			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}
		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterTotalPartyPerformance(
			String party, String year) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterTotalPartyPerformance(String party,
			String year) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterTotalPartyPerformance(
			String party, String year) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getFourQuartersTotalPartyPerformance(String party,
			String year) {
		BigDecimal q1 = getFirstQuarterTotalPartyPerformance(party, year);
		BigDecimal q2 = getSecondQuarterTotalPartyPerformance(party, year);
		BigDecimal q3 = getThirdQuarterTotalPartyPerformance(party, year);
		BigDecimal q4 = getFourthQuarterTotalPartyPerformance(party, year);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}
	
	
	public static BigDecimal getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoals(String party, String year, String Quantitative_goalType, String Qualitative_goalType) {
		BigDecimal q1 = getFourQuartersTotalPartyPerformanceSingle(party, year, Quantitative_goalType);
		BigDecimal q2 = getFourQuartersTotalPartyPerformanceSingle(party, year, Qualitative_goalType);
		BigDecimal total = q1.add(q2);

		return total;
	}
	
	public static String getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoalsToString(String party, String year, String Quantitative_goalType, String Qualitative_goalType) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformanceOfQualAndQuantiGoals(party, year, Quantitative_goalType, Qualitative_goalType) ;
		qString = String.valueOf(score);

		return qString + " %";

	}
	
	

	public static String Q1String(String party, String year) {
		String qString = null;
		BigDecimal score = getFirstQuarterTotalPartyPerformance(party, year);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q2String(String party, String year) {
		String qString = null;
		BigDecimal score = getSecondQuarterTotalPartyPerformance(party, year);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q3String(String party, String year) {
		String qString = null;
		BigDecimal score = getThirdQuarterTotalPartyPerformance(party, year);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q4String(String party, String year) {
		String qString = null;
		BigDecimal score = getFourthQuarterTotalPartyPerformance(party, year);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String TotalScoreString(String party, String year) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformance(party, year);
		qString = String.valueOf(score);

		return qString + " %";

	}

	/* ANNUAL LEAVE BALANCE CALCULATION */

	public static String scheduleAnnualLeaveBalanceCalculation(
			HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory())
				.createLocalDispatcher("interestcalculations", delegator);

		Map<String, String> context = UtilMisc.toMap("message",
				"Annual Balance Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			int frequency = RecurrenceRule.DAILY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule("calculateStaffAnnualLeaveDays", context,
					startTime, frequency, interval, count);
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

	public static Map<String, Object> calculateAnnualLeavebalance(
			DispatchContext context, Map<String, String> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		System.out
				.println("############## Attempting to Calculate Annual leave balances ... "
						+ Calendar.getInstance().getTime());

		HttpServletRequest request = null;
		HttpServletResponse response = null;
		Leave.generateAnnulLeaveBalancesWithOpeningBalances(request, response);

		return result;
	}
	
	
	
	/* SEND SCHEDULED EMAIL NOTIFICATION */

	public static String scheduleSendingEmailNotificationNONStaff(
			HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory())
				.createLocalDispatcher("interestcalculations", delegator);

		Map<String, String> context = UtilMisc.toMap("message",
				"Email Sending Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			int frequency = RecurrenceRule.SECONDLY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule("sendScheduledEmailNotificationNonStaff", context,
					startTime, frequency, interval, count);
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
	
	
	
	

	/* SEND SCHEDULED EMAIL NOTIFICATION */

	public static String scheduleSendingEmailNotification(
			HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory())
				.createLocalDispatcher("interestcalculations", delegator);

		Map<String, String> context = UtilMisc.toMap("message",
				"Email Sending Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			int frequency = RecurrenceRule.SECONDLY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule("sendScheduledEmailNotificationToStaff", context,
					startTime, frequency, interval, count);
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

	/* DISABLE/ENABLE LOGINS */
	public static Map<String, Object> enableDisableLogins(DispatchContext ctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = ctx.getDelegator();
		List<GenericValue> OnLeaveELI = null;
		List<GenericValue> OutOfLeaveELI = null;
		String state = "NOTSEND";
		Map<String, Object> results = ServiceUtil.returnSuccess();
		// Timestamp now = UtilDateTime.nowTimestamp();

		String now = Calendar.getInstance().getTime().toString();
		Date noww = null;
		try {
			noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK)
					.parse(now);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		java.sql.Date today = new java.sql.Date(noww.getTime());

		EntityConditionList<EntityExpr> onLeaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"fromDate", EntityOperator.LESS_THAN_EQUAL_TO, today),
						EntityCondition.makeCondition("thruDate",
								EntityOperator.GREATER_THAN, today),
						EntityCondition.makeCondition("applicationStatus",
								EntityOperator.EQUALS, "Approved")),
						EntityOperator.AND);

		try {
			OnLeaveELI = delegator.findList("EmplLeave", onLeaveConditions,
					null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : OnLeaveELI) {
			String party = genericValue.getString("partyId");
			String userName;
			GenericValue OnleaveParty = null;
			List<GenericValue> OnleavePartyELI = null;
			try {
				// OnleaveParty =
				// delegator.findOne("PartyAndUserLoginAndPerson",
				// UtilMisc.toMap("partyId", party), false);
				OnleavePartyELI = delegator.findList(
						"PartyAndUserLoginAndPerson",
						EntityCondition.makeCondition("partyId", party), null,
						null, null, false);
				log.info("++++++++++++++OnleaveParty++++++++++++++++"
						+ OnleaveParty);
			} catch (GenericEntityException e) {
				e.printStackTrace();
				;
			}

			for (GenericValue genericValue2 : OnleavePartyELI) {
				userName = genericValue2.getString("userLoginId");

				if (userName != null) {

					GenericValue OnleavePartyLogin = null;
					try {
						OnleavePartyLogin = delegator.findOne("UserLogin",
								UtilMisc.toMap("userLoginId", userName), false);
						log.info("++++++++++++++OnleavePartyLogin++++++++++++++++"
								+ OnleavePartyLogin);
					} catch (GenericEntityException e) {
						e.printStackTrace();
						;
					}
					if (OnleavePartyLogin != null) {
						OnleavePartyLogin.set("enabled", "N");

						try {
							delegator.createOrStore(OnleavePartyLogin);
						} catch (GenericEntityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {

					}

				} else {
					System.out.println("######## User not found #### ");
				}
			}

		}

		try {
			OutOfLeaveELI = delegator.findList("PartyAndUserLoginAndPerson",
					EntityCondition.makeCondition("enabled", "N"), null, null,
					null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : OutOfLeaveELI) {
			String party = genericValue.getString("partyId");

			EntityConditionList<EntityExpr> OffLeaveConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("partyId", EntityOperator.EQUALS,
									party), EntityCondition.makeCondition(
							"fromDate", EntityOperator.LESS_THAN_EQUAL_TO,
							today), EntityCondition.makeCondition("thruDate",
							EntityOperator.GREATER_THAN, today),
							EntityCondition.makeCondition("applicationStatus",
									EntityOperator.EQUALS, "Approved")),
							EntityOperator.AND);

			List<GenericValue> DisabledUsersInLeave = null;
			try {
				DisabledUsersInLeave = delegator.findList("EmplLeave",
						OffLeaveConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			if (DisabledUsersInLeave.size() == 0) {

				// ==================================================================================
				String userName2;
				GenericValue OffleaveParty = null;
				List<GenericValue> OffleavePartyELI = null;
				try {
					// OffleaveParty =
					// delegator.findOne("PartyAndUserLoginAndPerson",
					// UtilMisc.toMap("partyId", party), false);
					OffleavePartyELI = delegator.findList(
							"PartyAndUserLoginAndPerson",
							EntityCondition.makeCondition("partyId", party),
							null, null, null, false);
					log.info("++++++++++++++OnleaveParty++++++++++++++++"
							+ OffleavePartyELI);
				} catch (GenericEntityException e) {
					e.printStackTrace();
					;
				}

				for (GenericValue genericValue3 : OffleavePartyELI) {
					userName2 = genericValue3.getString("userLoginId");

					if (userName2 != null) {

						GenericValue OnleavePartyLogin = null;
						try {
							OnleavePartyLogin = delegator.findOne("UserLogin",
									UtilMisc.toMap("userLoginId", userName2),
									false);
							log.info("++++++++++++++OnleavePartyLogin++++++++++++++++"
									+ OnleavePartyLogin);
						} catch (GenericEntityException e) {
							e.printStackTrace();
							;
						}
						if (OnleavePartyLogin != null) {
							OnleavePartyLogin.set("enabled", "Y");

							try {
								delegator.createOrStore(OnleavePartyLogin);
							} catch (GenericEntityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {

						}

					} else {
						System.out.println("######## User not found #### ");
					}
				}

			}

		}

		return results;
	}

	// ======================= DISABLE AND ENABLE USERLOGINS
	// ===================================

	public static String scheduleDisableEnableUserLogins(
			HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		LocalDispatcher dispatcher = (new GenericDispatcherFactory())
				.createLocalDispatcher("interestcalculations", delegator);

		Map<String, String> context = UtilMisc.toMap("message",
				"Userlogins enable/Disable Testing !!");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			long startTime = (new Date()).getTime();
			int frequency = RecurrenceRule.SECONDLY;
			int interval = 5;
			int count = -1;
			dispatcher.schedule(
					"scheduleDisableEnableUserLoginAsTheyAreOnLeave", context,
					startTime, frequency, interval, count);
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

	

	

	/* SINGLE STAFF REPORT QUARTERLY SCORES CALCULATION */

	public static BigDecimal getFirstQuarterPartyPerformancePerIndicator(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		GenericValue pre;
		String quarter = year + "-Quarter-1";
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreFour").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q1party++++++++++++++++" + party);
		log.info("++++++++++++++q1totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q1maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterPartyPerformancePerIndicator(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);
		

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreFour").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q2maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterPartyPerformancePerIndicator(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreFour").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q3party++++++++++++++++" + party);
		log.info("++++++++++++++q3totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q3maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterPartyPerformancePerIndicator(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreFour").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q4party++++++++++++++++" + party);
		log.info("++++++++++++++q4totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q4maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourQuartersTotalPartyPerformancePerIndicator(
			String party, String year, String indicator) {
		BigDecimal q1 = getFirstQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		BigDecimal q2 = getSecondQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		BigDecimal q3 = getThirdQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		BigDecimal q4 = getFourthQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String PartyPerformancePerIndicatorQ1String(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFirstQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ2String(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getSecondQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ3String(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getThirdQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ4String(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourthQuarterPartyPerformancePerIndicator(party,
				year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorTotalScoreString(
			String party, String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformancePerIndicator(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	// ========================== SINGLE STAFF QUARTERLY TOTALS
	// =================================

	public static BigDecimal getFirstQuarterTotalPartyPerformanceSingle(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-1";
		int count = 0;
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterTotalPartyPerformanceSingle(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		

		}

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterTotalPartyPerformanceSingle(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		

		}
		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterTotalPartyPerformanceSingle(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		

		}

		return QuarterScore;
	}

	public static String getMaxTotalPartyPerformanceSingle(String party,
			String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-1";

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"scoreOne").stripTrailingZeros());

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);

		return f + " %";
	}

	public static BigDecimal getFourQuartersTotalPartyPerformanceSingle(
			String party, String year, String goalType) {
		BigDecimal q1 = getFirstQuarterTotalPartyPerformanceSingle(party, year,
				goalType);
		BigDecimal q2 = getSecondQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		BigDecimal q3 = getThirdQuarterTotalPartyPerformanceSingle(party, year,
				goalType);
		BigDecimal q4 = getFourthQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String Q1StringSingle(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFirstQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q2StringSingle(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getSecondQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q3StringSingle(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getThirdQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q4StringSingle(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourthQuarterTotalPartyPerformanceSingle(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String TotalScoreStringSingle(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformanceSingle(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}
	
	
	
	
	
	
	
	
	// ========================== SINGLE STAFF QUARTERLY TOTALS PER QUANTITATIVE GOAL =================================

		public static BigDecimal getFirstQuarterTotalPartyPerformancePerQuantitativeGoal(
				String party, String year, String goalType) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			BigDecimal maxtotalpercentage = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> holidaysELI = null;
			String quarter = year + "-Quarter-1";
			int count = 0;
			BigDecimal QuarterScore = BigDecimal.ZERO;

			EntityConditionList<EntityExpr> totalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition("quarter",
									EntityOperator.EQUALS, quarter),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, goalType),
							EntityCondition.makeCondition("year",
									EntityOperator.EQUALS, year)),
							EntityOperator.AND);

			try {
				holidaysELI = delegator.findList("PerfPartyReview",
						totalConditions, null, null, null, false);
				/*
				 * holidaysELI = delegator.findList("PerfPartyReview",
				 * EntityCondition.makeCondition("partyId", year), null, null, null,
				 * false);
				 */

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : holidaysELI) {
				
				totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
				maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
				
				
					BigDecimal five = new BigDecimal(5);
					BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
					BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
					BigDecimal four = new BigDecimal(4);
					QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
					
					
					log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

			}

			

			return QuarterScore;
		}

		public static BigDecimal getSecondQuarterTotalPartyPerformancePerQuantitativeGoal(
				String party, String year, String goalType) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			BigDecimal maxtotalpercentage = BigDecimal.ZERO;
			BigDecimal QuarterScore = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> holidaysELI = null;
			String quarter = year + "-Quarter-2";
			int count = 0;

			EntityConditionList<EntityExpr> totalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition("quarter",
									EntityOperator.EQUALS, quarter),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, goalType),
							EntityCondition.makeCondition("year",
									EntityOperator.EQUALS, year)),
							EntityOperator.AND);

			try {
				holidaysELI = delegator.findList("PerfPartyReview",
						totalConditions, null, null, null, false);
				/*
				 * holidaysELI = delegator.findList("PerfPartyReview",
				 * EntityCondition.makeCondition("partyId", year), null, null, null,
				 * false);
				 */

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : holidaysELI) {
				totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
				maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
				
				
					BigDecimal five = new BigDecimal(5);
					BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
					BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
					BigDecimal four = new BigDecimal(4);
					QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
					
					
					log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

			

			}

			return QuarterScore;
		}

		public static BigDecimal getThirdQuarterTotalPartyPerformancePerQuantitativeGoal(
				String party, String year, String goalType) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			BigDecimal maxtotalpercentage = BigDecimal.ZERO;
			BigDecimal QuarterScore = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> holidaysELI = null;
			String quarter = year + "-Quarter-3";
			int count = 0;

			EntityConditionList<EntityExpr> totalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition("quarter",
									EntityOperator.EQUALS, quarter),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, goalType),
							EntityCondition.makeCondition("year",
									EntityOperator.EQUALS, year)),
							EntityOperator.AND);

			try {
				holidaysELI = delegator.findList("PerfPartyReview",
						totalConditions, null, null, null, false);
				/*
				 * holidaysELI = delegator.findList("PerfPartyReview",
				 * EntityCondition.makeCondition("partyId", year), null, null, null,
				 * false);
				 */

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : holidaysELI) {
				totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
				maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
				
				
					BigDecimal five = new BigDecimal(5);
					BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
					BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
					BigDecimal four = new BigDecimal(4);
					QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
					
					
					log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

			

			}
			return QuarterScore;
		}

		public static BigDecimal getFourthQuarterTotalPartyPerformancePerQuantitativeGoal(
				String party, String year, String goalType) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			BigDecimal maxtotalpercentage = BigDecimal.ZERO;
			BigDecimal QuarterScore = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> holidaysELI = null;
			String quarter = year + "-Quarter-4";
			int count = 0;

			EntityConditionList<EntityExpr> totalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition("quarter",
									EntityOperator.EQUALS, quarter),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, goalType),
							EntityCondition.makeCondition("year",
									EntityOperator.EQUALS, year)),
							EntityOperator.AND);

			try {
				holidaysELI = delegator.findList("PerfPartyReview",
						totalConditions, null, null, null, false);
				/*
				 * holidaysELI = delegator.findList("PerfPartyReview",
				 * EntityCondition.makeCondition("partyId", year), null, null, null,
				 * false);
				 */

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : holidaysELI) {
				totalpercentage = genericValue.getBigDecimal("scoreFour").stripTrailingZeros();
				maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
				
				
					BigDecimal five = new BigDecimal(5);
					BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
					BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
					BigDecimal four = new BigDecimal(4);
					QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
					
					
					log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

			

			}

			return QuarterScore;
		}

		public static String getMaxTotalPartyPerformancePerQuantitativeGoal(String party,
				String year, String goalType) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> holidaysELI = null;
			String quarter = year + "-Quarter-1";

			EntityConditionList<EntityExpr> totalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, goalType),
							EntityCondition.makeCondition("quarter",
									EntityOperator.EQUALS, quarter),
							EntityCondition.makeCondition("year",
									EntityOperator.EQUALS, year)),
							EntityOperator.AND);

			try {
				holidaysELI = delegator.findList("PerfPartyReview",
						totalConditions, null, null, null, false);
				/*
				 * holidaysELI = delegator.findList("PerfPartyReview",
				 * EntityCondition.makeCondition("partyId", year), null, null, null,
				 * false);
				 */

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : holidaysELI) {
				totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
						"scoreOne").stripTrailingZeros());

			}

			String f = String.valueOf(totalpercentage);
			log.info("++++++++++++++q2party++++++++++++++++" + party);
			log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);

			return f + " %";
		}

		public static BigDecimal getFourQuartersTotalPartyPerformancePerQuantitativeGoal(
				String party, String year, String goalType) {
			BigDecimal q1 = getFirstQuarterTotalPartyPerformancePerQuantitativeGoal(party, year,
					goalType);
			BigDecimal q2 = getSecondQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			BigDecimal q3 = getThirdQuarterTotalPartyPerformancePerQuantitativeGoal(party, year,
					goalType);
			BigDecimal q4 = getFourthQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			BigDecimal q12 = q1.add(q2);
			BigDecimal q123 = q12.add(q3);
			BigDecimal q1234 = q123.add(q4);

			return q1234;
		}

		public static String Q1StringPerQuantitativeGoal(String party, String year,
				String goalType) {
			String qString = null;
			BigDecimal score = getFirstQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			qString = String.valueOf(score);

			return qString + " %";

		}

		public static String Q2StringPerQuantitativeGoal(String party, String year,
				String goalType) {
			String qString = null;
			BigDecimal score = getSecondQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			qString = String.valueOf(score);

			return qString + " %";

		}

		public static String Q3StringPerQuantitativeGoal(String party, String year,
				String goalType) {
			String qString = null;
			BigDecimal score = getThirdQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			qString = String.valueOf(score);

			return qString + " %";

		}

		public static String Q4StringPerQuantitativeGoal(String party, String year,
				String goalType) {
			String qString = null;
			BigDecimal score = getFourthQuarterTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			qString = String.valueOf(score);

			return qString + " %";

		}

		public static String TotalScoreStringPerQuantitativeGoal(String party, String year,
				String goalType) {
			String qString = null;
			BigDecimal score = getFourQuartersTotalPartyPerformancePerQuantitativeGoal(party,
					year, goalType);
			qString = String.valueOf(score);

			return qString + " %";

		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static String StaffBonusOnSalary(String party, String year) {
		String bonus = null;
		BigDecimal fifty = new BigDecimal(50);
		BigDecimal seventy = new BigDecimal(70);
		BigDecimal seventyone = new BigDecimal(71);
		BigDecimal eighty = new BigDecimal(80);
		BigDecimal eightyone = new BigDecimal(81);
		BigDecimal ninty = new BigDecimal(90);

		BigDecimal score = getFourQuartersTotalPartyPerformance(party, year);
		if ((score.compareTo(fifty) == 0) || (score.compareTo(fifty) == -1)) {
			bonus = "No Bonus";
		} else if ((score.compareTo(fifty) == 1)
				&& (score.compareTo(seventyone) == -1)) {
			bonus = "25 %";

		} else if ((score.compareTo(seventy) == 1)
				&& (score.compareTo(eightyone) == -1)) {
			bonus = "50 %";

		} else if ((score.compareTo(eighty) == 1)
				&& (score.compareTo(eightyone) == -1)) {
			bonus = "75 %";

		} else if (score.compareTo(ninty) == 1) {
			bonus = "100 %";

		}

		return bonus;

	}

	public static String StaffSalaryIncrement(String party, String year) {
		String increment = null;
		BigDecimal fifty = new BigDecimal(50);
		BigDecimal seventy = new BigDecimal(70);
		BigDecimal seventyone = new BigDecimal(71);
		BigDecimal eighty = new BigDecimal(80);
		BigDecimal eightyone = new BigDecimal(81);
		BigDecimal ninty = new BigDecimal(90);

		BigDecimal score = getFourQuartersTotalPartyPerformance(party, year);
		if ((score.compareTo(fifty) == 0) || (score.compareTo(fifty) == -1)) {
			increment = "Nil Increment";
		} else if ((score.compareTo(fifty) == 1)
				&& (score.compareTo(seventyone) == -1)) {
			increment = "5 %";

		} else if ((score.compareTo(seventy) == 1)
				&& (score.compareTo(eightyone) == -1)) {
			increment = "8 %";

		} else if ((score.compareTo(eighty) == 1)
				&& (score.compareTo(eightyone) == -1)) {
			increment = "10 %";

		} else if (score.compareTo(ninty) == 1) {
			increment = "12 %";

		}

		return increment;

	}

	/* SELF APPRAISAL REPORT */

	public static BigDecimal getFirstQuarterPartyPerformancePerIndicatorSelf(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		GenericValue pre;
		String quarter = year + "-Quarter-1";
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q1party++++++++++++++++" + party);
		log.info("++++++++++++++q1totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q1maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterPartyPerformancePerIndicatorSelf(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q2maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterPartyPerformancePerIndicatorSelf(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q3party++++++++++++++++" + party);
		log.info("++++++++++++++q3totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q3maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterPartyPerformancePerIndicatorSelf(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q4party++++++++++++++++" + party);
		log.info("++++++++++++++q4totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q4maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourQuartersTotalPartyPerformancePerIndicatorSelf(
			String party, String year, String indicator) {
		BigDecimal q1 = getFirstQuarterPartyPerformancePerIndicatorSelf(party,
				year, indicator);
		BigDecimal q2 = getSecondQuarterPartyPerformancePerIndicatorSelf(party,
				year, indicator);
		BigDecimal q3 = getThirdQuarterPartyPerformancePerIndicatorSelf(party,
				year, indicator);
		BigDecimal q4 = getFourthQuarterPartyPerformancePerIndicatorSelf(party,
				year, indicator);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String PartyPerformancePerIndicatorQ1StringSelf(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFirstQuarterPartyPerformancePerIndicatorSelf(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ2StringSelf(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getSecondQuarterPartyPerformancePerIndicatorSelf(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ3StringSelf(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getThirdQuarterPartyPerformancePerIndicatorSelf(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ4StringSelf(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourthQuarterPartyPerformancePerIndicatorSelf(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorTotalScoreStringSelf(
			String party, String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformancePerIndicatorSelf(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	// ========================== SINGLE STAFF QUARTERLY TOTALS
	// =================================

	public static BigDecimal getFirstQuarterTotalPartyPerformanceSingleSelf(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-1";
		int count = 0;
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterTotalPartyPerformanceSingleSelf(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterTotalPartyPerformanceSingleSelf(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterTotalPartyPerformanceSingleSelf(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreTwo").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}
		return QuarterScore;
	}

	public static String getMaxTotalPartyPerformanceSingleSelf(String party,
			String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"scoreOne").stripTrailingZeros());

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);

		return f + " %";
	}

	public static BigDecimal getFourQuartersTotalPartyPerformanceSingleSelf(
			String party, String year, String goalType) {
		BigDecimal q1 = getFirstQuarterTotalPartyPerformanceSingleSelf(party,
				year, goalType);
		BigDecimal q2 = getSecondQuarterTotalPartyPerformanceSingleSelf(party,
				year, goalType);
		BigDecimal q3 = getThirdQuarterTotalPartyPerformanceSingleSelf(party,
				year, goalType);
		BigDecimal q4 = getFourthQuarterTotalPartyPerformanceSingleSelf(party,
				year, goalType);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String Q1StringSingleSelf(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFirstQuarterTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q2StringSingleSelf(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getSecondQuarterTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q3StringSingleSelf(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getThirdQuarterTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q4StringSingleSelf(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourthQuarterTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String TotalScoreStringSingleSelf(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	/* HOD/SUPERVISOR APPRAISAL REPORT */

	/* SINGLE STAFF REPORT QUARTERLY SCORES CALCULATION */

	public static BigDecimal getFirstQuarterPartyPerformancePerIndicatorHOD(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		GenericValue pre;
		String quarter = year + "-Quarter-1";
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreThree").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q1party++++++++++++++++" + party);
		log.info("++++++++++++++q1totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q1maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterPartyPerformancePerIndicatorHOD(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreThree").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q2maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterPartyPerformancePerIndicatorHOD(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreThree").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q3party++++++++++++++++" + party);
		log.info("++++++++++++++q3totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q3maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterPartyPerformancePerIndicatorHOD(
			String party, String year, String indicator) {
		BigDecimal Qpercentage = BigDecimal.ZERO;
		BigDecimal maxpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		GenericValue pre;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition(
								"PerfActionPlanIndicatorId",
								EntityOperator.EQUALS, indicator),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (holidaysELI.size() > 0) {
			pre = holidaysELI.get(0);
			Qpercentage = pre.getBigDecimal("scoreThree").stripTrailingZeros();
			maxpercentage = pre.getBigDecimal("scoreOne").stripTrailingZeros();
		}

		else {
		}

		String f = String.valueOf(Qpercentage);
		log.info("++++++++++++++q4party++++++++++++++++" + party);
		log.info("++++++++++++++q4totalpercentage++++++++++++++++" + f);
		log.info("++++++++++++++q4maxtotalpercentage++++++++++++++++"
				+ maxpercentage);

		BigDecimal five = new BigDecimal(5);
		BigDecimal staffScoreDivideCountBy5 = Qpercentage.divide(five, 20,
				RoundingMode.HALF_UP);
		BigDecimal staffScoreBy5MultiplyMaxScore = staffScoreDivideCountBy5
				.multiply(maxpercentage);
		BigDecimal four = new BigDecimal(4);
		QuarterScore = staffScoreBy5MultiplyMaxScore.divide(four, 2,
				RoundingMode.HALF_UP);

		return QuarterScore;
	}

	public static BigDecimal getFourQuartersTotalPartyPerformancePerIndicatorHOD(
			String party, String year, String indicator) {
		BigDecimal q1 = getFirstQuarterPartyPerformancePerIndicatorHOD(party,
				year, indicator);
		BigDecimal q2 = getSecondQuarterPartyPerformancePerIndicatorHOD(party,
				year, indicator);
		BigDecimal q3 = getThirdQuarterPartyPerformancePerIndicatorHOD(party,
				year, indicator);
		BigDecimal q4 = getFourthQuarterPartyPerformancePerIndicatorHOD(party,
				year, indicator);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String PartyPerformancePerIndicatorQ1StringHOD(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFirstQuarterPartyPerformancePerIndicatorHOD(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ2StringHOD(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getSecondQuarterPartyPerformancePerIndicatorHOD(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ3StringHOD(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getThirdQuarterPartyPerformancePerIndicatorHOD(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorQ4StringHOD(String party,
			String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourthQuarterPartyPerformancePerIndicatorHOD(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String PartyPerformancePerIndicatorTotalScoreStringHOD(
			String party, String year, String indicator) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformancePerIndicatorHOD(
				party, year, indicator);
		qString = String.valueOf(score);

		return qString + " %";

	}

	// ========================== SINGLE STAFF QUARTERLY TOTALS
	// =================================

	public static BigDecimal getFirstQuarterTotalPartyPerformanceSingleHOD(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-1";
		int count = 0;
		BigDecimal QuarterScore = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreThree").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getSecondQuarterTotalPartyPerformanceSingleHOD(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-2";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreThree").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getThirdQuarterTotalPartyPerformanceSingleHOD(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-3";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreThree").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static BigDecimal getFourthQuarterTotalPartyPerformanceSingleHOD(
			String party, String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		BigDecimal maxtotalpercentage = BigDecimal.ZERO;
		BigDecimal QuarterScore = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String quarter = year + "-Quarter-4";
		int count = 0;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("quarter",
								EntityOperator.EQUALS, quarter),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = genericValue.getBigDecimal("scoreThree").stripTrailingZeros();
			maxtotalpercentage = genericValue.getBigDecimal("scoreOne").stripTrailingZeros();
			
			
				BigDecimal five = new BigDecimal(5);
				BigDecimal staffScoreDivideCountBy5 = totalpercentage.divide(five, 20, RoundingMode.HALF_UP);
				BigDecimal staffScoreDivideCountBy5MultiplyMaxScore = staffScoreDivideCountBy5.multiply(maxtotalpercentage);
				BigDecimal four = new BigDecimal(4);
				QuarterScore = QuarterScore.add(staffScoreDivideCountBy5MultiplyMaxScore.divide(four, 2, RoundingMode.HALF_UP));
				
				
				log.info("++++++++++++++ QuarterScore >>>>>>>>>>>>>++++++++++++++++" + QuarterScore);

		}

		return QuarterScore;
	}

	public static String getMaxTotalPartyPerformanceSingleHOD(String party,
			String year, String goalType) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;

		EntityConditionList<EntityExpr> totalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, party),
						EntityCondition.makeCondition("perfGoalsDefId",
								EntityOperator.EQUALS, goalType),
						EntityCondition.makeCondition("year",
								EntityOperator.EQUALS, year)),
						EntityOperator.AND);

		try {
			holidaysELI = delegator.findList("PerfPartyReview",
					totalConditions, null, null, null, false);
			/*
			 * holidaysELI = delegator.findList("PerfPartyReview",
			 * EntityCondition.makeCondition("partyId", year), null, null, null,
			 * false);
			 */

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"scoreOne").stripTrailingZeros());

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++q2party++++++++++++++++" + party);
		log.info("++++++++++++++q2totalpercentage++++++++++++++++" + f);

		return f + " %";
	}

	public static BigDecimal getFourQuartersTotalPartyPerformanceSingleHOD(
			String party, String year, String goalType) {
		BigDecimal q1 = getFirstQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		BigDecimal q2 = getSecondQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		BigDecimal q3 = getThirdQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		BigDecimal q4 = getFourthQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		BigDecimal q12 = q1.add(q2);
		BigDecimal q123 = q12.add(q3);
		BigDecimal q1234 = q123.add(q4);

		return q1234;
	}

	public static String Q1StringSingleHOD(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFirstQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q2StringSingleHOD(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getSecondQuarterTotalPartyPerformanceSingleHOD(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q3StringSingleHOD(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getThirdQuarterTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String Q4StringSingleHOD(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourthQuarterTotalPartyPerformanceSingleHOD(
				party, year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String TotalScoreStringSingleHOD(String party, String year,
			String goalType) {
		String qString = null;
		BigDecimal score = getFourQuartersTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		qString = String.valueOf(score);

		return qString + " %";

	}

	public static String TotalScoreStringSelf(String party, String year,
			String goalType1, String goalType) {
		String qString = null;
		BigDecimal score1 = getFourQuartersTotalPartyPerformanceSingleSelf(
				party, year, goalType1);
		BigDecimal score = getFourQuartersTotalPartyPerformanceSingleSelf(
				party, year, goalType);
		BigDecimal score3 = score1.add(score);
		qString = String.valueOf(score3);

		return qString + " %";

	}

	public static String TotalScoreStringHOD(String party, String year,
			String goalType1, String goalType) {
		String qString = null;
		BigDecimal score1 = getFourQuartersTotalPartyPerformanceSingleHOD(
				party, year, goalType1);
		BigDecimal score = getFourQuartersTotalPartyPerformanceSingleHOD(party,
				year, goalType);
		BigDecimal score3 = score1.add(score);
		qString = String.valueOf(score3);

		return qString + " %";

	}

	/*
	 * =================== PERFORMANCE REVIEW PERCENTAGE TOTALS VALIDATION
	 * ========================
	 */

	/* QUALITATIVE + QUANTITATIVE (VALIDATION AT PERSPECTIVE CREATION) */

	public static String getTotal() {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		String state = null;
		ResultSet rs1;

		try {
			holidaysELI = delegator.findList("PerfGoalsDef", null, null, null,
					null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue
					.getBigDecimal("percentage"));

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++totalpercentage++++++++++++++++" + f);
		int j = Integer.parseInt(f);
		if (j != 100) {
			state = "INVALID";
		} else if (j == 100) {
			state = "VALID";

		}

		return state;
	}

	/*
	 * ALL QUANTITATIVE PERSPECTIVES SHOULD ADD UP TO QUANTITATIVE PERCENTAGE
	 * TOTAL (VALIDATION AT OBJECTIVE CREATION)
	 */

	public static String getQuantitativeTotal() {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		GenericValue quantitative = null;
		String state = null;
		String goalDef = "QNT_GOALS";
		BigDecimal QuantitativeGoalTotal = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"perfGoalsDefId", EntityOperator.EQUALS, goalDef)),
						EntityOperator.AND);

		try {
			quantitative = delegator.findOne("PerfGoalsDef",
					UtilMisc.toMap("perfGoalsDefId", goalDef), false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			holidaysELI = delegator.findList("PerfGoals",
					quantitativetotalConditions, null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (quantitative != null) {
			QuantitativeGoalTotal = quantitative.getBigDecimal("percentage")
					.stripTrailingZeros();
		} else {

		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"percentage").stripTrailingZeros());

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++totalpercentage++++++++++++++++" + f);
		int j = Integer.parseInt(f);
		if (QuantitativeGoalTotal.compareTo(totalpercentage) == 0) {
			state = "VALID";
		} else {
			state = "INVALID";

		}

		return state;
	}

	/*
	 * ALL QUALITATIVE PERSPECTIVES SHOULD ADD UP TO QUALITATIVE PERCENTAGE
	 * TOTAL (VALIDATION AT OBJECTIVE CREATION)
	 */

	public static String getQualitativeTotal() {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> holidaysELI = null;
		GenericValue quantitative = null;
		String state = null;
		String goalDef = "QTT_GOALS";
		BigDecimal QuantitativeGoalTotal = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> qualitativetotalConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"perfGoalsDefId", EntityOperator.EQUALS, goalDef)),
						EntityOperator.AND);

		try {
			quantitative = delegator.findOne("PerfGoalsDef",
					UtilMisc.toMap("perfGoalsDefId", goalDef), false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			holidaysELI = delegator.findList("PerfGoals",
					qualitativetotalConditions, null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (quantitative != null) {
			QuantitativeGoalTotal = quantitative.getBigDecimal("percentage")
					.stripTrailingZeros();
		} else {

		}

		for (GenericValue genericValue : holidaysELI) {
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal(
					"percentage").stripTrailingZeros());

		}

		String f = String.valueOf(totalpercentage);
		log.info("++++++++++++++totalpercentage++++++++++++++++" + f);
		int j = Integer.parseInt(f);
		if (QuantitativeGoalTotal.compareTo(totalpercentage) == 0) {
			state = "VALID";
		} else {
			state = "INVALID";

		}

		return state;
	}

	/*
	 * ALL OBJECTIVES TO ADD UP TO RESPECTIVE PERSPECTIVE PERCENTAGE TOTAL
	 * (VALIDATION AT ACTION PLANS CREATION)
	 */

	public static String getObjectiveAndRespectivePerspectiveTotal(
			String objective) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> allObjectiveInThisPerspectiveELI = null;
		GenericValue UsedObjective = null;
		String state = null;
		BigDecimal perspectiveTotal = BigDecimal.ZERO;
		String groupId;
		String perspectiveId;
		GenericValue Perspective = null;

		try {
			UsedObjective = delegator.findOne(
					"PerfReviewsGroupObjectiveDefinition",
					UtilMisc.toMap("PerfReviewsGroupObjectiveDefinitionId",
							objective), false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (UsedObjective != null) {
			groupId = UsedObjective.getString("perfReviewDefId");
			perspectiveId = UsedObjective.getString("perfGoalsId");

			try {
				Perspective = delegator.findOne("PerfGoals",
						UtilMisc.toMap("perfGoalsId", perspectiveId), false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			perspectiveTotal = Perspective.getBigDecimal("percentage")
					.stripTrailingZeros();

			EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("perfReviewDefId",
									EntityOperator.EQUALS, groupId),
							EntityCondition.makeCondition("perfGoalsId",
									EntityOperator.EQUALS, perspectiveId)),
							EntityOperator.AND);

			try {
				allObjectiveInThisPerspectiveELI = delegator.findList(
						"PerfReviewsGroupObjectiveDefinition",
						quantitativetotalConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : allObjectiveInThisPerspectiveELI) {
				totalpercentage = totalpercentage.add(genericValue
						.getBigDecimal("percentage").stripTrailingZeros());
			}

			if (totalpercentage.compareTo(perspectiveTotal) == 0) {
				state = "VALID";
			} else {
				state = "INVALID";

			}

		}

		return state;
	}

	/*
	 * ALL OBJECTIVES TO ADD UP TO RESPECTIVE PERSPECTIVE PERCENTAGE TOTAL
	 * (VALIDATION AT INDICATORS CREATION)
	 */

	public static String getActionPlanAndRespectiveObjectiveTotal(
			String actionPlan) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> allActionPlanInThisObjectiveELI = null;
		GenericValue UsedActionPlan = null;
		String state = null;
		BigDecimal objectiveTotal = BigDecimal.ZERO;
		String groupId;
		String ObjectiveId;
		GenericValue Objective = null;

		try {
			UsedActionPlan = delegator.findOne(
					"PerfObjectiveActionPlanDefinition",
					UtilMisc.toMap("PerfObjectiveActionPlanId", actionPlan),
					false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (UsedActionPlan != null) {
			groupId = UsedActionPlan.getString("perfReviewDefId");
			ObjectiveId = UsedActionPlan
					.getString("PerfReviewsGroupObjectiveDefinitionId");

			try {
				Objective = delegator.findOne(
						"PerfReviewsGroupObjectiveDefinition", UtilMisc.toMap(
								"PerfReviewsGroupObjectiveDefinitionId",
								ObjectiveId), false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			objectiveTotal = Objective.getBigDecimal("percentage")
					.stripTrailingZeros();

			EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
							.makeCondition("perfReviewDefId",
									EntityOperator.EQUALS, groupId),
							EntityCondition.makeCondition(
									"PerfReviewsGroupObjectiveDefinitionId",
									EntityOperator.EQUALS, ObjectiveId)),
							EntityOperator.AND);

			try {
				allActionPlanInThisObjectiveELI = delegator.findList(
						"PerfObjectiveActionPlanDefinition",
						quantitativetotalConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : allActionPlanInThisObjectiveELI) {
				totalpercentage = totalpercentage.add(genericValue
						.getBigDecimal("percentage").stripTrailingZeros());
			}

			if (totalpercentage.compareTo(objectiveTotal) == 0) {
				state = "VALID";
			} else {
				state = "INVALID";

			}

		}

		return state;
	}

	/*
	 * ALL INDICATORSS SHOULD ADD UP TO ACTION PLANS PERCENTAGE TOTAL
	 * (VALIDATION AT REVIEW GROUP CREATION)
	 */

	public static String getIndicatorTotalsCompareToActionPlans() {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> indicatorsELI = null;
		List<GenericValue> indicatorsTotalELI = null;
		GenericValue actionPlan = null;
		String state = null;
		BigDecimal ActionPlanTotal = BigDecimal.ZERO;
		String objectiveId;
		String groupId;
		String PerspectiveId = null;


		try {
			indicatorsELI = delegator.findList("PerfReviewsGroupObjectiveDefinition", null,	null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : indicatorsELI) {
			groupId = genericValue.getString("perfReviewDefId");
			PerspectiveId = genericValue.getString("perfGoalsId");

			EntityConditionList<EntityExpr> indicatorTotalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition
					.makeCondition("perfGoalsId",EntityOperator.EQUALS, PerspectiveId),
							EntityCondition.makeCondition("perfReviewDefId",EntityOperator.EQUALS, groupId)),
							EntityOperator.AND);

			try {
				indicatorsTotalELI = delegator.findList("PerfReviewsGroupObjectiveDefinition",
						indicatorTotalConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue2 : indicatorsTotalELI) {
				totalpercentage = totalpercentage.add(genericValue2.getBigDecimal("percentage").stripTrailingZeros());
				
			}

			try {
				actionPlan = delegator.findOne(	"PerfGoals", UtilMisc.toMap("perfGoalsId", PerspectiveId),	false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			if (actionPlan != null) {
				ActionPlanTotal = actionPlan.getBigDecimal("percentage").stripTrailingZeros();
			} else {

			}

			if (ActionPlanTotal.compareTo(totalpercentage) == 0) {
				state = "VALID";
			} else {
				state = "INVALID";

			}

		}

		return state;
	}
	

	public static String CompareStartEndDate(Date from, Date end) {
		LocalDate StartDate = new LocalDate(from);
		LocalDate EndDate = new LocalDate(end);
		
		String state = null;
		if (StartDate.isAfter(EndDate)) {
			state = "INVALID";
		} else {
			state = "VALID";
		}
		
		return state;

	}

	
	public static BigDecimal returnBigDecimal() {
		BigDecimal st = BigDecimal.ZERO;
		return st;
	}
	

	public static void ScheduleSocietyMails(ArrayList<String> email, String subject, String mailBody) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue scheduledMail = null;
		
		 for (int i = 0; i < email.size(); i++) {
			    String mail_value = email.get(i);
			    System.out.println("Element: " + mail_value);
			

		scheduledMail = delegator.makeValue("SocietyScheduledMail", "scheduleId",	delegator.getNextSeqId("SocietyScheduledMail"),
											"email",	mail_value, "subject", subject, "body",mailBody, "sendStatus", "NOTSEND");
		
		
		log.info("=============>>>>>>>>>>> THIS IS THE MAIL >>>>>>>>" + scheduledMail);

		try {
			scheduledMail.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
			
		 }
	
	}
	
	// ================ MAKE SURE ALL PERFORMANCE SCORES HAVE BEEN FORWARDED TO HR BEFORE CLOSING A REVIEW PERIOD
	
	public static String haveAllScoresBeenForwarded(String period) {
		String results = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		List<GenericValue> leaveELI = null;
		
		EntityConditionList<EntityExpr> closeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("quarter",EntityOperator.EQUALS, period)
						// EntityCondition.makeCondition("stage",EntityOperator.NOT_EQUAL, "FORWARDED")
						),
						EntityOperator.AND);
		
		try {
			leaveELI = delegator.findList("PerfPartyReview", closeConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
/*
		if (leaveELI.size() > 0) {
			results = "INVALID";
		}*/
		//else {
			results = "VALID";
		//}

		return results;
	}
	
	
	
	public static String getObjectiveAndRespectivePerspectiveTotalAndCompare() {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> allObjectiveInThisPerspectiveELI = null;
		List<GenericValue> UsedObjective = null;
		String state = null;
		BigDecimal perspectiveTotal = BigDecimal.ZERO;
		String groupId;
		String perspectiveId;
		GenericValue Perspective = null;

		try {
			UsedObjective = delegator.findList("PerfReviewsGroupObjectiveDefinition", null,	null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		for (GenericValue genericValue : UsedObjective) {
			totalpercentage = BigDecimal.ZERO;
			groupId = genericValue.getString("perfReviewDefId");
			perspectiveId = genericValue.getString("perfGoalsId");
			
			log.info("=============>>>>>>>>>>> GroupId  >>>>>>>>" + groupId);
			log.info("=============>>>>>>>>>>> perspectiveId  >>>>>>>>" + perspectiveId);
			
			EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("perfReviewDefId",EntityOperator.EQUALS, groupId),
							EntityCondition.makeCondition("perfGoalsId", EntityOperator.EQUALS, perspectiveId)),EntityOperator.AND);
			
			
			try {
				allObjectiveInThisPerspectiveELI = delegator.findList("PerfReviewsGroupObjectiveDefinition", quantitativetotalConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue2 : allObjectiveInThisPerspectiveELI) {
				totalpercentage = totalpercentage.add(genericValue2.getBigDecimal("percentage").stripTrailingZeros());
			}
			
			
			try {
				Perspective = delegator.findOne("PerfGoals", UtilMisc.toMap("perfGoalsId", perspectiveId), false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			if (Perspective != null) {
				String s = Perspective.getString("percentage");
				int t = Integer.parseInt(s);
				perspectiveTotal = new BigDecimal(t);
			}

			
			log.info("=============>>>>>>>>>>> Objective Total >>>>>>>>" + totalpercentage);
			log.info("=============>>>>>>>>>>> Perspective Total >>>>>>>>" + perspectiveTotal);

			if (totalpercentage.compareTo(perspectiveTotal) == 0) {
				state = "VALID";
			} else {
				state = "INVALID";

			}
		}

		

		return state;
	}

	
	      /* ============================== % TOTAL VALIDATION AT OBJECTIVE CREATION ======================================*/
	    		   
	
	public static String getObjectiveAndRespectivePerspectiveTotalAndCompareAtObjectiveCreation(String groupid, String goalId, BigDecimal percentage) {
		BigDecimal totalpercentage = BigDecimal.ZERO;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> UsedObjective = null;
		String state = null;
		BigDecimal perspectiveTotal = BigDecimal.ZERO;
		GenericValue Perspective = null;
		
		EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("perfReviewDefId",EntityOperator.EQUALS, groupid),
				EntityCondition.makeCondition("perfGoalsId", EntityOperator.EQUALS, goalId)),EntityOperator.AND);

		try {
			UsedObjective = delegator.findList("PerfReviewsGroupObjectiveDefinition", quantitativetotalConditions,	null, null, null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		for (GenericValue genericValue : UsedObjective) {
			
			totalpercentage = totalpercentage.add(genericValue.getBigDecimal("percentage").stripTrailingZeros());
			
			log.info("=============>>>>>>>>>>> Single  >>>>>>>>" + totalpercentage);
			
		}
		
		log.info("=============>>>>>>>>>>> TotalInDb  >>>>>>>>" + totalpercentage);
			
			try {
				Perspective = delegator.findOne("PerfGoals", UtilMisc.toMap("perfGoalsId", goalId), false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (Perspective != null) {
				String s = Perspective.getString("percentage");
				int t = Integer.parseInt(s);
				perspectiveTotal = new BigDecimal(t);
				//perspectiveTotal = Perspective.getBigDecimal("percentage").stripTrailingZeros();
			}

			

			BigDecimal g =percentage.stripTrailingZeros();
			BigDecimal totalInDb = totalpercentage.add(g);
			log.info("=============>>>>>>>>>>> Objective Total >>>>>>>>" + totalInDb);
			log.info("=============>>>>>>>>>>> Perspective Total >>>>>>>>" + perspectiveTotal);
			
			

			if ((totalInDb).compareTo(perspectiveTotal) == -1 || (totalInDb).compareTo(perspectiveTotal) == 0) {
				state = "VALID";
			} else {
				state = "INVALID";

			}
		

		

		return state;
	}

	
	
	 /* ============================== % TOTAL VALIDATION AT PERSPECTIVE CREATION ======================================*/
	   
	
		public static String getPerspectiveAndRespectiveGoalTotalAndCompareAtPerspectiveCreation(String goalDefId, BigDecimal percentage) {
			BigDecimal totalpercentage = BigDecimal.ZERO;
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> UsedObjective = null;
			String state = null;
			BigDecimal perspectiveTotal = BigDecimal.ZERO;
			GenericValue Goal = null;
			
			EntityConditionList<EntityExpr> quantitativetotalConditions = EntityCondition
			.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("perfGoalsDefId", EntityOperator.EQUALS, goalDefId)),EntityOperator.AND);

			try {
				UsedObjective = delegator.findList("PerfGoals", quantitativetotalConditions,	null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			
			for (GenericValue genericValue : UsedObjective) {
				
				totalpercentage = totalpercentage.add(genericValue.getBigDecimal("percentage").stripTrailingZeros());
				
				log.info("=============>>>>>>>>>>> Single  >>>>>>>>" + totalpercentage);
				
			}
			
			log.info("=============>>>>>>>>>>> TotalInDb  >>>>>>>>" + totalpercentage);
				
				try {
					Goal = delegator.findOne("PerfGoalsDef", UtilMisc.toMap("perfGoalsDefId", goalDefId), false);

				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				if (Goal != null) {
					String s = Goal.getString("percentage");
					int t = Integer.parseInt(s);
					perspectiveTotal = new BigDecimal(t);
				}

				

				BigDecimal g =percentage.stripTrailingZeros();
				BigDecimal totalInDb = totalpercentage.add(g);
				log.info("=============>>>>>>>>>>> Objective Total >>>>>>>>" + totalInDb);
				log.info("=============>>>>>>>>>>> Perspective Total >>>>>>>>" + perspectiveTotal);
				
				

				if ((totalInDb).compareTo(perspectiveTotal) == -1 || (totalInDb).compareTo(perspectiveTotal) == 0) {
					state = "VALID";
				} else {
					state = "INVALID";

				}
			

			

			return state;
		}
		
		
	/*=================================== VALIDATE SCORES BEFORE FORWARDING ======================================*/
		
		public static String forwardPerformanceScoresValidation(String party, String whichScore) {
			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> StaffScoresELI = null;
			List<GenericValue> StaffScoresELI2 = null;
			String state = null;
			BigDecimal one = BigDecimal.ONE;
			BigDecimal blank = null;

			EntityConditionList<EntityExpr> anyZeroConditions = EntityCondition.makeCondition(UtilMisc.toList(
					        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, party),
							EntityCondition.makeCondition(whichScore, EntityOperator.LESS_THAN, one)),
							EntityOperator.AND);
			
			EntityConditionList<EntityExpr> anyblankConditions = EntityCondition.makeCondition(UtilMisc.toList(
			        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, party),
					EntityCondition.makeCondition(whichScore, EntityOperator.EQUALS, blank)),
					EntityOperator.AND);

			try {
				StaffScoresELI2 = delegator.findList("PerfPartyReview",
						anyblankConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			if (StaffScoresELI2.size() > 0) {
				state = "INVALID";
				return state;
			} 
			
			try {
				StaffScoresELI = delegator.findList("PerfPartyReview",
						anyZeroConditions, null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			

			if (StaffScoresELI.size() > 0) {
				state = "INVALID";
				return state;
			} else {
				state = "VALID";
			}


			return state;
		}
		
		
		
		
/*=================================== VALIDATE SCORES REVIEWS ======================================*/
		
		public static String PerformanceScoresReviewValidation(String whichScore) {
			String state = null;
			
			if (whichScore.equalsIgnoreCase("")) {
				state = "INVALID";
			} else {
				state = "VALID";
			}
			
			return state;
		}


	// Method for the Employee call Back to
		
	public static String getLeaveEndExend(String leaveId){
            Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
			Date fromDate = null;
			Date endDate = null;
			Date resumeDate = null;
		
			GenericValue getLeaveResumptionExtendELI = null;
			try {

				getLeaveResumptionExtendELI = delegator.findOne("EmplLeave",
						UtilMisc.toMap("leaveId", leaveId), false);
			} catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
			Date resumingLeaveDate = null;
			Double resumingLeaveDuration = 0.0;
			if(getLeaveResumptionExtendELI.size() > 0){
				 resumingLeaveDate = getLeaveResumptionExtendELI.getDate("resumeForExtendDate");
				 log.info("#####resumeForExtendDateJKJKJK#######"+resumingLeaveDate);
				 resumingLeaveDuration = getLeaveResumptionExtendELI.getDouble("daysDifference");
				 log.info("#####dresumingLeaveDurationJJJJJJ#######"+resumingLeaveDuration);
			}
			
			// calculate leave end date
			endDate = calculateEndWorkingNonHolidayDay(resumingLeaveDate,
					resumingLeaveDuration.intValue());
			 log.info("#####END DATEJJJJJJJJJJJJJ#######"+endDate);
		//	 Date endDateSql = java.sql.Date.valueOf(endDate.toString());
			 
			 Date convertedEndDate = sqlDateConvert(endDate);
			 log.info("#####convertedEndDateYYYYYYYYYY#######"+convertedEndDate);
			 
			
		    Date reComeBack = calculateEndWorkingNonHolidayDayResumption(endDate);
		    log.info("##reComeBackJJJJJJJJJJJJJ#######"+reComeBack);
		    
		    
			
			/// GET LEAVE DETAILS TO FILL THE nEXT lEAVE APP
			GenericValue getLeaveDetails = null;
			try {

				getLeaveDetails = delegator.findOne("EmplLeave",
						UtilMisc.toMap("leaveId", leaveId), false);
			} catch (GenericEntityException e2) {
				e2.printStackTrace();

			}
			
			
		//	if(getLeaveDetails.size() > 0){
				
			//	getLeaveDetails.set("calledBack", "extended");
				
				GenericValue makeValuesToEmplLeave = delegator.makeValue("EmplLeave");
				String newLeaveId  =  delegator.getNextSeqId("EmplLeave");
				makeValuesToEmplLeave.put("leaveId", newLeaveId);
				makeValuesToEmplLeave.put("partyId", getLeaveDetails.getString("partyId"));
				makeValuesToEmplLeave.put("leaveTypeId", getLeaveDetails.getString("leaveTypeId"));
				makeValuesToEmplLeave.put("emplLeaveReasonTypeId", getLeaveDetails.getString("emplLeaveReasonTypeId"));
				makeValuesToEmplLeave.put("fromDate", resumingLeaveDate);
				makeValuesToEmplLeave.put("leaveDuration", resumingLeaveDuration);
				makeValuesToEmplLeave.put("thruDate", convertedEndDate);
				makeValuesToEmplLeave.put("isDeductedFromAnnual", "Y");
				makeValuesToEmplLeave.put("financialYear",  getLeaveDetails.getString("financialYear"));
				makeValuesToEmplLeave.put("hasbalance", getLeaveDetails.getString("hasbalance"));
				makeValuesToEmplLeave.put("resumptionDate", reComeBack);
				makeValuesToEmplLeave.put("isActive", getLeaveDetails.getString("isActive"));
				makeValuesToEmplLeave.put("createdBy",getLeaveDetails.getString("createdBy"));
				makeValuesToEmplLeave.put("leaveStatus", getLeaveDetails.getString("leaveStatus"));
				makeValuesToEmplLeave.put("createdDate", new java.sql.Timestamp(0));
				makeValuesToEmplLeave.put("applicationStatus", getLeaveDetails.getString("applicationStatus"));
				makeValuesToEmplLeave.put("approvalStatus", getLeaveDetails.getString("approvalStatus"));
				makeValuesToEmplLeave.put("responsibleEmployee", getLeaveDetails.getString("responsibleEmployee"));
				makeValuesToEmplLeave.put("handedOverTo", getLeaveDetails.getString("handedOverTo"));
				makeValuesToEmplLeave.put("workflowDocumentTypeId", getLeaveDetails.getString("workflowDocumentTypeId"));
				try{
					makeValuesToEmplLeave.create();
				}catch(GenericEntityException ex){
					ex.printStackTrace();
				}
				
	//}
			return leaveId;
		}// close the method 

	///calculate resumption Non Holiday
	
	public static Date calculateEndWorkingNonHolidayDayResumption(Date resumeDate) {
		//int noOfDays =  duration + 1;
		LocalDate localDateEndDate = new LocalDate(resumeDate.getTime());
        LocalDate resumeDay = localDateEndDate.plusDays(1);
		// If this is happening on sunday or saturday push it to start on monday
		if (resumeDay.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			resumeDay = resumeDay.plusDays(2);
		}

		if (resumeDay.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			resumeDay = resumeDay.plusDays(1);
		}

		int noOfHolidays = getNumberOfHolidays(resumeDate, 1);
		log.info("=============== NUMBER OF HOLIDAYS" + noOfHolidays);
		resumeDay = resumeDay.plusDays(noOfHolidays);
        
		if (resumeDay.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			resumeDay = resumeDay.plusDays(2);
		} else if (resumeDay.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			resumeDay = resumeDay.plusDays(2);
		}
		
		while(isHoliday(resumeDay.toDate()) == true){
			if(isHoliday(resumeDay.toDate()) == true){
				resumeDay = resumeDay.plusDays(1);
			}
		}  
		
		if (resumeDay.getDayOfWeek() == DateTimeConstants.SATURDAY) {
			resumeDay = resumeDay.plusDays(2);
		} else if (resumeDay.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			resumeDay = resumeDay.plusDays(2);
		}
		log.info("#####################calculateEndWorkingNonHolidayDayResumption##"+resumeDay.toDate());
		
		Date date = java.sql.Date.valueOf(resumeDay.toString());
	
		 return date;
	}
	
	/// calculate  resume date Another
	
	public static Date calculateEndWorkingNonHolidayDayResume(Date fromDate,int leaveDuration){
		
		Date endDate = null;
		Date resumeDate = null;
		
		endDate = calculateEndWorkingNonHolidayDay(fromDate,
				leaveDuration);
		int leaveTillResumption = leaveDuration + 1;
		resumeDate = calculateEndWorkingNonHolidayDay(fromDate,
				leaveTillResumption);
		log.info("#################Util Endateon##"+resumeDate);
		
		Date date = java.sql.Date.valueOf(resumeDate.toString());
		log.info("#################sql Endateon##"+date);
		return date;
		
	}
	
	  public static Date sqlDateConvert(Date Date){
		  LocalDate localDateEndDate = new LocalDate(Date.getTime());
		  SimpleDateFormat theFormat = new SimpleDateFormat("yyyy-mm-dd");
		  
		  Date date = java.sql.Date.valueOf(localDateEndDate.toString());
		 	 
		  return date;
		 	 
		 	/* java.sql.Date convertedDate = null;
		 		 try {
		 			Date parsedDate =  theFormat.parse(Date.toString());
		 		    convertedDate= new java.sql.Date(parsedDate.getTime());
		 		} catch (ParseException e) {
		 			// TODO Auto-generated catch block
		 		e.printStackTrace();
		 		}
		 	
		 		 return convertedDate;*/
		  }//close method
	
	
}
