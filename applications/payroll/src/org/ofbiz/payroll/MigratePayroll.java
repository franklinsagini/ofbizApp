package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
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

/**
 * @author charles
 **/
public class MigratePayroll {
	private static Logger log = Logger.getLogger(MigratePayroll.class);

	static Timestamp curTimeStamp = null;
	static Timestamp newPeriodEndDate = null;

	public static String migratePayroll(HttpServletRequest request, HttpServletResponse response) {
		String newPayrollPeriodId = "";
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		// String oldPayrollPeriodId = (String)
		// request.getParameter("payrollPeriodId");
		String oldPayrollPeriodId = getActivePayrollPeriod(delegator);

		if (!(oldPayrollPeriodId == null)) {
			// Get employees
			List<GenericValue> employeesELI = null;
			// String partyId = party.getString("partyId");
			log.info("######### Old payrollPeriodId is :::: " + oldPayrollPeriodId);

			newPayrollPeriodId = getNewPayrollPeriodID(oldPayrollPeriodId, delegator);
			log.info("######### New payrollPeriodId is :::: " + newPayrollPeriodId);

			curTimeStamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
			newPeriodEndDate = getPeriodEndDate(newPayrollPeriodId, delegator);

			try {
				employeesELI = delegator.findList("StaffPayroll",
						EntityCondition.makeCondition("payrollPeriodId", oldPayrollPeriodId), null, null, null, false);

			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue genericValue : employeesELI) {

				if (genericValue.getString("closed").equals("Y")) {
					rollOverPayroll(genericValue, genericValue.getString("staffPayrollId"), newPayrollPeriodId,
							delegator);
					log.info("######### Staff ID " + genericValue.getString("staffPayrollId"));

					/*
					 * genericValue.setString("closed", "Y"); try {
					 * delegator.createOrStore(genericValue); } catch
					 * (GenericEntityException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); }
					 */
				} else {
					log.info(">>>>>>>>>>>>>>>>>>>>>>>>> Cannot Migrate An Open Period <<<<<<<<<<<<<<<<<<<<<<<<<");
				}

			}
		} else {
			log.info("No Active Period ID Found");
		}

		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException("Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return "";
	}

	private static String getActivePayrollPeriod(Delegator delegator) {
		String PayrollPeriodId = null;
		List<GenericValue> ppELI = null;

		try {
			ppELI = delegator.findList("PayrollPeriod", EntityCondition.makeCondition("status", "Open"), null, null,
					null, false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : ppELI) {
			PayrollPeriodId = genericValue.getString("payrollPeriodId");
		}

		return PayrollPeriodId;
	}

	private static Timestamp getPeriodEndDate(String newPayrollPeriodId, Delegator delegator) {
		List<GenericValue> payrollPeriodELI = null;
		Timestamp endDate = null;
		try {
			payrollPeriodELI = delegator.findList("PayrollPeriod",
					EntityCondition.makeCondition("payrollPeriodId", newPayrollPeriodId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollPeriod : payrollPeriodELI) {

			endDate = payrollPeriod.getTimestamp("endDate");
		}

		return endDate;
	}

	private static String getNewPayrollPeriodID(String oldPayrollPeriodId, Delegator delegator) {
		List<GenericValue> payrollPeriodELI = null;
		String newPPID = "";
		try {
			payrollPeriodELI = delegator.findList("PayrollPeriod",
					EntityCondition.makeCondition("payrollPeriodId", oldPayrollPeriodId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollPeriod : payrollPeriodELI) {

			log.info("#########>>>>>>>>>1 2 pakb>>>>>>>>>>>>" + payrollPeriod.getLong(("sequence_no")));

			newPPID = getNewPeriod(payrollPeriod, payrollPeriod.getLong("sequence_no"), delegator);

			payrollPeriod.setString("currentperiod", "N");
			payrollPeriod.setString("status", "Closed");
			try {
				delegator.createOrStore(payrollPeriod);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return newPPID;
	}

	private static String getNewPeriod(GenericValue payrollPeriod, Long seq_No, Delegator delegator) {
		String pId = "";
		Long newSeq = 0l;
		String yearId = "";

		List<GenericValue> periodELI = new LinkedList<GenericValue>();

		if (isLastSequence(payrollPeriod.getString("payrollYearId"), seq_No, delegator) == true) {
			yearId = getNewYear(payrollPeriod.getString("payrollYearId"), delegator);
			newSeq = new Long(1);
			log.info("######### YEAR ID>>>>>>>1>>>>>>" + yearId);

		} else {
			yearId = payrollPeriod.getString("payrollYearId");
			newSeq = seq_No + 1;
			log.info("######### YEAR ID>>>>>>>>2>>>>>" + yearId);
		}

		EntityConditionList<EntityExpr> payrollPeriodConditions = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("payrollYearId", EntityOperator.EQUALS, yearId),
						EntityCondition.makeCondition("sequence_no", EntityOperator.EQUALS, newSeq)),
				EntityOperator.AND);

		try {
			periodELI = delegator.findList("PayrollPeriod", payrollPeriodConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue period : periodELI) {
			// Get the amount
			log.info("######### 2>>>>>>>>>>>>>>>>>>" + period.getString("payrollPeriodId"));
			pId = period.getString("payrollPeriodId");

			period.setString("currentperiod", "Y");
			period.setString("status", "Open");
			period.setString("payrollcheck", "O");
			try {
				delegator.createOrStore(period);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pId;
	}

	private static boolean isLastSequence(String payrollYearId, Long seqNo, Delegator delegator) {

		List<GenericValue> periodELI = new LinkedList<GenericValue>();

		EntityConditionList<EntityExpr> payrollPeriodConditions = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("payrollYearId", EntityOperator.EQUALS, payrollYearId),
						EntityCondition.makeCondition("sequence_no", EntityOperator.EQUALS, seqNo + 1)),
				EntityOperator.AND);

		try {
			periodELI = delegator.findList("PayrollPeriod", payrollPeriodConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		log.info("######### 3.5>>>>>>>>>>>>>>>>>>");

		if (periodELI.isEmpty()) {
			log.info("######### 3>>>>>>>>>>>>>>>>>>");
			return true;
		} else {
			log.info("######### 4>>>>>>>>>>>>>>>>>>");
			return false;
		}
	}

	private static String getNewYear(String oldYearId, Delegator delegator) {
		String newYear = "";
		List<GenericValue> PayrollYearELI = null;
		try {

			PayrollYearELI = delegator.findList("PayrollYear",
					EntityCondition.makeCondition("payrollYearId", oldYearId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollYear : PayrollYearELI) {

			String seq = payrollYear.getString("yearSeq");

			int setId = Integer.parseInt(seq);
			newYear = getnewYearId(setId, delegator);

			payrollYear.setString("currentyear", "N");
			payrollYear.setString("status", "Closed");
			try {
				delegator.createOrStore(payrollYear);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newYear;
	}

	private static String getnewYearId(int setId, Delegator delegator) {
		String newYearId = "";
		List<GenericValue> newYearELI = null;
		try {

			newYearELI = delegator.findList("PayrollYear",
					EntityCondition.makeCondition("yearSeq", String.valueOf(setId + 1)), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue newYear : newYearELI) {

			newYearId = newYear.getString("payrollYearId");

			newYear.setString("currentyear", "Y");
			newYear.setString("status", "Open");
			try {
				delegator.createOrStore(newYear);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newYearId;
	}

	/*
	 * private static Long getnewPeriodSeq(GenericValue newYear, Delegator
	 * delegator) { Long newSeq=0l; List<GenericValue> newYearPeriodELI = null;
	 * 
	 * EntityConditionList<EntityExpr> payrollPeriodConditions = EntityCondition
	 * .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
	 * "payrollYearId", EntityOperator.EQUALS,
	 * newYear.getString("payrollYearId")), EntityCondition.makeCondition(
	 * "sequence_no", EntityOperator.EQUALS, 1)), EntityOperator.AND);
	 * 
	 * try {
	 * 
	 * newYearPeriodELI = delegator.findList("PayrollPeriod",
	 * payrollPeriodConditions, null, null, null, false); } catch
	 * (GenericEntityException e) { e.printStackTrace(); }
	 * 
	 * for (GenericValue newYearPeriod : newYearPeriodELI) {
	 * 
	 * 
	 * } return newSeq; }
	 */

	private static void rollOverPayroll(GenericValue employee, String staffPayrollId, String newPayrollPeriodId,
			Delegator delegator) {
		// log.info("######### Roll over starts here>>>>>>>>>>>>>>>>>>>>>");

		List<GenericValue> staffPayrollDetailsELI = null;
		try {

			staffPayrollDetailsELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId", staffPayrollId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollDetails : staffPayrollDetailsELI) {

			GenericValue newStaffPayroll = createStaffToSave(delegator, staffPayrollDetails.getString("partyId"),
					staffPayrollDetails.getBigDecimal("pensionpercentage"),
					staffPayrollDetails.getBigDecimal("nssfVolAmount"), newPayrollPeriodId);
			try {
				delegator.store(newStaffPayroll);
				log.info("New Staff Payroll ID = >>>>>>>>>>>>" + newStaffPayroll.getString("staffPayrollId"));

				rollOverParameters(employee, staffPayrollId, newStaffPayroll.getString("staffPayrollId"),
						newPayrollPeriodId, delegator);

			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static void rollOverParameters(GenericValue employee, String OldStaffPayrollId, String NewStaffPayrollId,
			String newPayrollPeriodId, Delegator delegator) {
		List<GenericValue> staffPayrollElementDetailsELI = null;
		List<GenericValue> staffLeaveElementDetailsELI = null;
		Timestamp recurrencyDate = null;

	
		// -------------------------- find the staff on Leave -----------------------------------
		
		String empPartyId = null;
		String empPayrollPeriod = null;
		Timestamp startDate = null;
		Timestamp endDate = null;
		BigDecimal basicSalary = BigDecimal.ZERO;
		Double payrollCalculationRate = 0.0;
		BigDecimal leaveAllowance = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> payrollLeaveCond = EntityCondition.makeCondition(UtilMisc
				.toList(EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS, OldStaffPayrollId)));
		try {
			staffLeaveElementDetailsELI = delegator.findList("StaffPayroll", payrollLeaveCond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String successMessage = null;
		BigDecimal BalanceSave = BigDecimal.ZERO;
		for (GenericValue genericValue : staffLeaveElementDetailsELI) {
			empPartyId = genericValue.getString("partyId");
			empPayrollPeriod = genericValue.getString("payrollPeriodId");

			startDate = getStartDateGivenPayrollPeriod(empPayrollPeriod);
			endDate = getEndDateGivenPayrollPeriod(empPayrollPeriod);

			if (checkIfStaffWasOnLeave(startDate, endDate, empPartyId) == true){
           
				basicSalary = getBasicSalary(OldStaffPayrollId);
			    payrollCalculationRate = CalcRate(basicSalary);
			   // BigDecimal calcRateBigG = BigDecimal.valueOf(payrollCalculationRate);
			    leaveAllowance = getleaveAllowance(payrollCalculationRate, basicSalary);
			    
			    successMessage = createElementToSaveTw(delegator, "10052",  leaveAllowance,
			    		BalanceSave, recurrencyDate,  NewStaffPayrollId);
			    
			    log.info("##### leaveAllowance  ######-----"+ leaveAllowance);
			}
		}
		
		// -----------------------------End Of Adding Leave Allowance--------------------------
		EntityConditionList<EntityExpr> elementConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(
								EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS,
										OldStaffPayrollId),
						EntityCondition.makeCondition("elementType", EntityOperator.NOT_EQUAL, "System Element")),
				EntityOperator.AND);

		try {
			staffPayrollElementDetailsELI = delegator.findList("PayrollElementAndStaffPayrollElement",
					elementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}


		List<GenericValue> listSystemElements = new ArrayList<GenericValue>();
		// GenericValue systemElement;

		for (GenericValue staffPayrollElementDetails : staffPayrollElementDetailsELI) {

			/*
			 * if(staffPayrollElementDetails.getTimestamp("recurrencyExpiry").
			 * equals(null)) { recurrencyDate=null; } else
			 * if(staffPayrollElementDetails.getTimestamp("recurrencyExpiry").
			 * after(newPeriodEndDate)) {
			 * recurrencyDate=staffPayrollElementDetails.getTimestamp(
			 * "recurrencyExpiry"); } else { recurrencyDate=null; }
			 */
			
			if(!staffPayrollElementDetails.getString("payrollElementId").equals("10052")){
			

			listSystemElements
					.add(createElementToSave(delegator, staffPayrollElementDetails.getString("payrollElementId"),
							staffPayrollElementDetails.getBigDecimal("amount"),
							staffPayrollElementDetails.getBigDecimal("balance"), recurrencyDate, NewStaffPayrollId));
			}
		
		}
		try {
			delegator.storeAll(listSystemElements);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static GenericValue createStaffToSave(Delegator delegator, String partyId, BigDecimal pensionPercentage,
			BigDecimal nssfVolAmt, String newPayrollPeriodId) {
		String staffPayrollSequenceId = delegator.getNextSeqId("StaffPayroll");

		GenericValue staffPayroll = delegator.makeValidValue("StaffPayroll",
				UtilMisc.toMap("staffPayrollId", staffPayrollSequenceId, "partyId", partyId, "pensionpercentage",
						pensionPercentage, "nssfVolAmount", nssfVolAmt, "payrollPeriodId", newPayrollPeriodId, "closed",
						"N"));
		try {
			staffPayroll = delegator.createSetNextSeqId(staffPayroll);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return staffPayroll;
	}

	private static GenericValue createElementToSave(Delegator delegator, String payrollElementId, BigDecimal Amount,
			BigDecimal Balance, Timestamp recurrencyDate, String newStaffPayrollId) {

		String staffPayrollElementsSequenceId = delegator.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElement = delegator.makeValidValue("StaffPayrollElements",
				UtilMisc.toMap("staffPayrollElementsId", staffPayrollElementsSequenceId, "payrollElementId",
						payrollElementId, "amount", Amount, "staffPayrollId", newStaffPayrollId, "valueChanged", "N",
						"balance", Balance, "recurrencyExpiry", recurrencyDate));
		try {
			staffPayrollElement = delegator.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return staffPayrollElement;
	}

	public static java.sql.Timestamp getStartDateGivenPayrollPeriod(String payrollPeriodId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue getDateELI = null;
		java.sql.Timestamp startDate = null;
		try {
			getDateELI = delegator.findOne("PayrollPeriod", UtilMisc.toMap("payrollPeriodId", payrollPeriodId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (getDateELI != null) {
			startDate = getDateELI.getTimestamp("startDate");
		}
		return startDate;
	}

	public static java.sql.Timestamp getEndDateGivenPayrollPeriod(String payrollPeriodId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue getDateELI = null;
		java.sql.Timestamp endDate = null;
		try {
			getDateELI = delegator.findOne("PayrollPeriod", UtilMisc.toMap("payrollPeriodId", payrollPeriodId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (getDateELI != null) {
			endDate = getDateELI.getTimestamp("endDate");
		}
		return endDate;
	}

	public static boolean checkIfStaffWasOnLeave(java.sql.Timestamp startDate, java.sql.Timestamp endDate,
			String partyId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		java.sql.Date  dateStart  = new java.sql.Date(startDate.getTime());
		java.sql.Date  dateEnd   = new java.sql.Date(endDate.getTime());
		// LocalDate startdateConvertToLocalTime = new LocalDate(firstdate);
		// Timestamp startDateTimestamp = new
		// Timestamp(startdateConvertToLocalTime.toDateTimeAtStartOfDay().getMillis());

		// LocalDate enddateConvertToLocalTime = new LocalDate(endDate);
		// Timestamp endDateTimestamp = new
		// Timestamp(enddateConvertToLocalTime.toDateTimeAtStartOfDay().getMillis());
		List<GenericValue> listEmployeeOnLeaveELI = null;
		Double daysQual = 14.0;
		EntityConditionList<EntityExpr> leaveConditions = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("approvalStatus", EntityOperator.EQUALS, "Approved"),
						EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN, dateEnd),
						EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, dateStart),
						EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("leaveTypeId", EntityOperator.EQUALS, "ANNUAL_LEAVE")),
				EntityOperator.AND);

		try {
			listEmployeeOnLeaveELI = delegator.findList("EmplLeave", leaveConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (listEmployeeOnLeaveELI.size() > 0) {
			return true;
		}
		
		log.info("-#########  leaveConditions  SIIIIZE#####------"+listEmployeeOnLeaveELI.size()); 
		
		return false;

	}

	public static BigDecimal getBasicSalary(String payrollId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		EntityConditionList<EntityExpr> payrollElementCondition = EntityCondition.makeCondition(
				UtilMisc.toList(EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS, payrollId),
						EntityCondition.makeCondition("payrollElementId", EntityOperator.EQUALS, "BASICPAY")),
				EntityOperator.AND);
		List<GenericValue> getBasicPM = null;
		try {
			getBasicPM = delegator.findList("StaffPayrollElements", payrollElementCondition, null, null,
					null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		log.info("-------StaffPayRollElements-------" + payrollId);
		BigDecimal salaryPayPerMonth = BigDecimal.ZERO;
		for (GenericValue genericValue : getBasicPM) {
			salaryPayPerMonth = genericValue.getBigDecimal("amount");
		}
		log.info("-------Employeee Salary ----------" + salaryPayPerMonth);
		return salaryPayPerMonth;

	}

	public static Double CalcRate(BigDecimal basicSalary) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue leaveIdELI = null;
		try {
			leaveIdELI = delegator.findOne("PayrollElement", UtilMisc.toMap("payrollElementId", "10052"), false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
		}
		Double calcRate = 0.0;
		if (leaveIdELI.size() > 0) {
			calcRate = leaveIdELI.getDouble("calcRate");
		}
		log.info("_-------CALC RATE-- "+calcRate);
		return calcRate;
	}

	public static BigDecimal getleaveAllowance(Double calRate, BigDecimal salaryBasic) {
		BigDecimal calRateBigG = new BigDecimal(calRate);
		BigDecimal hundred = new BigDecimal(100);
		BigDecimal axcrHund = calRateBigG.divide(hundred, 2 ,RoundingMode.HALF_UP);
 		BigDecimal total = BigDecimal.ZERO;
 		
		try {
			total = salaryBasic.multiply(axcrHund);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total;
	}
	
	private static String createElementToSaveTw(Delegator delegator, String payrollElementId, BigDecimal Amount,
			BigDecimal Balance, Timestamp recurrencyDate, String newStaffPayrollId) {
		
		String succ = "success";

		String staffPayrollElementsSequenceId = delegator.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElement = delegator.makeValidValue("StaffPayrollElements",
				UtilMisc.toMap("staffPayrollElementsId", staffPayrollElementsSequenceId, "payrollElementId",
						payrollElementId, "amount", Amount, "staffPayrollId", newStaffPayrollId, "valueChanged", "N",
						"balance", Balance, "recurrencyExpiry", recurrencyDate));
		try {
			staffPayrollElement = delegator.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return  succ;
	}


}
