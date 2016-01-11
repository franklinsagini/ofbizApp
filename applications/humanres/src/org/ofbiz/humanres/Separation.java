package org.ofbiz.humanres;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import javolution.util.FastMap;

/**
 * @author Ronald
 * **/

public class Separation {
	

	private static Logger log= Logger.getLogger(Separation.class);
	
	public static void SeparationModule(HttpServletRequest request, HttpServletResponse response) {
		Map<String,Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDate today= new LocalDate(Calendar.getInstance().getTimeInMillis());
        Date today_now= null;
        String separationAppId=(String)request.getParameter("separationApplicationID");
       
	} // close separationModule
	

	
	public static BigDecimal getLeaveBalances(String partyId){
		Delegator delegator=DelegatorFactoryImpl.getDelegator(null);
		BigDecimal leaveBalances =BigDecimal.ZERO;
		List<GenericValue> LeaveBalance=null;
		try{
			LeaveBalance=delegator.findList("LeaveBalances", EntityCondition
					.makeCondition(UtilMisc.toMap("partyId", partyId)), null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		
		for(GenericValue genericVal : LeaveBalance){
			 leaveBalances=genericVal.getBigDecimal("availableLeaveDays");
			
		}
		log.info("############################"+leaveBalances);
		
		return leaveBalances;
	}
	

	public static BigDecimal calculateTheCalendarDaysBetweenDates(String effectiveDate, Date created) {
		Double daysCount = 0.0;
		
		LocalDate localDateStartDate = new LocalDate(effectiveDate);
		LocalDate localDateEndDate = new LocalDate(created);
		BigDecimal daysCounting = BigDecimal.ZERO;
		
			while (localDateEndDate.toDate().before(localDateStartDate.toDate())) {
				/*if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)
						&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
					daysCount++;
				   }*/
				daysCount++;
				
				localDateEndDate = localDateEndDate.plusDays(1);

			}
		
        log.info("#########DAYS COUNT -----#########"+daysCount);
        
	     daysCounting = new BigDecimal(daysCount);

		return daysCounting;
	}
	
	public static BigDecimal EmployeeLostItemsAmount(String partyId){
		
		Delegator delegator=DelegatorFactoryImpl.getDelegator(null);
		
		List<GenericValue> employeeLostItems=null;
		try{
			employeeLostItems= delegator.findList("EmployeeLostItems", EntityCondition.makeCondition(UtilMisc.toMap("partyId", partyId)), null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		BigDecimal lostAmount=BigDecimal.ZERO;
		
		for(GenericValue employeeLostItem: employeeLostItems ){
			
		lostAmount= lostAmount.add(employeeLostItem.getBigDecimal("amountOfItem"));
			
		}	
		
		log.info("############## AMOUNT_LOST ###################   "+lostAmount);
			
		return lostAmount;
		
	}
	
	
	//EMPLOYEE LOANS TO BE INCLUDED IN THIS METHOD
	
	public static BigDecimal employeeLoans(String partyId){
		BigDecimal employeeLoans= new BigDecimal(9000);
		
		List<GenericValue> LoanDetailsELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue employeeDetail = null;
		
		// get National ID Number
		
		try{
		    employeeDetail = delegator.findOne("Person",UtilMisc.toMap("partyId",partyId), false);
		 }catch(GenericEntityException ex){
			ex.printStackTrace();
		}
		String nationalIdNumber = null;
		if(employeeDetail.size() > 0){
			nationalIdNumber = employeeDetail.getString("nationalIDNumber"); 
		}
		log.info("------------- National ID Number-----------"+nationalIdNumber);
		
		//   ---------------GET LOAN INFO------------------------------------------
		BigDecimal zeroOutStandingBalance = BigDecimal.ZERO;
		
		EntityConditionList<EntityExpr> loanConditions = EntityCondition.makeCondition(UtilMisc.toList(
		        EntityCondition.makeCondition("idNumber", EntityOperator.EQUALS, nationalIdNumber),
		        EntityCondition.makeCondition("outstandingBalance", EntityOperator.GREATER_THAN, zeroOutStandingBalance)),
				EntityOperator.AND);
		
		try{
			LoanDetailsELI = delegator.findList("LoanApplication",loanConditions, null, null, null, false) ;
		}catch(GenericEntityException ep){
			ep.printStackTrace();
		}
		    BigDecimal loanAmountTotal = BigDecimal.ZERO;
		 for(GenericValue genericValue : LoanDetailsELI){
				loanAmountTotal = loanAmountTotal.add(genericValue.getBigDecimal("outstandingBalance"));
		}
		 log.info("-------------OutStanding Loan Balance-----------"+loanAmountTotal);
	
		return loanAmountTotal;	
	}
	
	
	
	
	
	public static BigDecimal employeeBasicSalaryKsh(String party){
		
		log.info("############### method employeeBasicSalaryKsh Reached #################");

		BigDecimal empSalary = new BigDecimal(30000);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> getSeparationDetailsELI = null;
		try{
			getSeparationDetailsELI = delegator.findByAnd("SeparationApplication",UtilMisc.toMap("partyId", party),null, false);
		   }catch(GenericEntityException ex){
			ex.printStackTrace();
	    }
		
		String effectiveDate = null;
		for(GenericValue getDetail : getSeparationDetailsELI){
			effectiveDate =  getDetail.getString("effectiveDate");
		}
	
	/*	dateFromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(asFromDate);
		sqlFromDate = new java.sql.Date(dateFromDate.getTime());

		fromDateTimestamp = new Timestamp(sqlFromDate.getTime());*/
  
		LocalDate effectiveDateConvertToLocalTime = new LocalDate(effectiveDate);
		LocalDate effectiveDateMinusSomeMonths = effectiveDateConvertToLocalTime.minusMonths(2);
		LocalDate plusFourMonths = effectiveDateConvertToLocalTime.plusMonths(4);
		
		log.info("--------## effectiveDate ------------#########"+effectiveDate);

		log.info("--------## effectiveDate Minus Two Months ------------#########"+effectiveDateMinusSomeMonths);
		
		Timestamp timestamp = new Timestamp(effectiveDateMinusSomeMonths.toDateTimeAtStartOfDay().getMillis());
		
		Timestamp timestampFourMonthsAhead = new Timestamp(plusFourMonths.toDateTimeAtStartOfDay().getMillis());
		
		log.info("--------## effectiveDate Timestamp ------------#########"+timestamp);
		log.info("--------## FOUR MONTHS AHEAD Timestamp ------------#########"+timestampFourMonthsAhead);

		Date today = new java.sql.Timestamp(0);

	
		EntityConditionList<EntityExpr> payrollConditions = EntityCondition.makeCondition(UtilMisc.toList(
				        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, party),
				        EntityCondition.makeCondition("closed", EntityOperator.EQUALS, "Y"),
						EntityCondition.makeCondition("lastUpdatedStamp",EntityOperator.GREATER_THAN_EQUAL_TO, timestamp),
						EntityCondition.makeCondition("lastUpdatedStamp",EntityOperator.LESS_THAN,timestampFourMonthsAhead)),
						EntityOperator.AND);
		
		List<GenericValue> getPayrollId = null;
		try{
			getPayrollId = delegator.findList("StaffPayroll",payrollConditions,null,null,null,false);
		   }catch(GenericEntityException e){
			e.printStackTrace();
	        }
		
		   String payRollId = null;
		   for(GenericValue getThePayRollId : getPayrollId){
			     payRollId = getThePayRollId.getString("staffPayrollId");
	       	}
	         log.info("#############StaffPayRollId##########"+payRollId);
		   
		   EntityConditionList<EntityExpr> payrollElementCondition = EntityCondition.makeCondition(UtilMisc.toList(
			        EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS, payRollId),
					EntityCondition.makeCondition("payrollElementId",EntityOperator.EQUALS, "BASICPAY")),
					EntityOperator.AND);
		   
		   
	   List<GenericValue> getBasicPM = null;  
		try{
		   getBasicPM = delegator.findList("StaffPayrollElements", payrollElementCondition,null,null,null, false);
		}catch(GenericEntityException ex){
			ex.printStackTrace();
		}
        log.info("#############StaffPayRollElements ##########"+payRollId);
       
        BigDecimal salaryPayPM = BigDecimal.ZERO;
        for(GenericValue genericValue : getBasicPM){
        	salaryPayPM = genericValue.getBigDecimal("amount");
        }
        
        log.info("#############Employeee Salary ##########"+salaryPayPM);
        
        
		 return salaryPayPM;
	}
	
	
	//Leave Allowance Calculation
	
	public static BigDecimal leaveAllowanceAmount( String separationTypeId,BigDecimal daysTo, BigDecimal leaveBal, BigDecimal basicSalary){
		Delegator delegator=DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> SeparationTypeELI=null;
		BigDecimal leaveAllowance=BigDecimal.ZERO;
		
		try{
			SeparationTypeELI=delegator.findList("SeparationTypes", EntityCondition.makeCondition(UtilMisc.toMap("separationTypesId", separationTypeId))
					, null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		BigDecimal noticePeriod=BigDecimal.ZERO;
		for(GenericValue genericValue:SeparationTypeELI){
			 String noticePeriodDays=genericValue.getString("noticePeriod");
			 noticePeriod = new BigDecimal(noticePeriodDays);
			 log.info("***--**NoticePeriod--Allowance Calc Info----"+noticePeriod);
		}
		
		BigDecimal month = new BigDecimal(12);
		BigDecimal daysOfyear = new BigDecimal(365);
		
		// --------------return Leave Allowance-------------------------
		
		leaveAllowance = (leaveBal.multiply(basicSalary).multiply(month)).divide(daysOfyear,4,RoundingMode.HALF_UP);
		
	
		
		log.info("--------------**LEAVE DAYS ALLOWANCE  CALC***----------    "+ leaveAllowance);
		
		return leaveAllowance;
	}
	
	
	
	//   ------------------------LIEN OF NOTICE------------------------------------
	
		public static BigDecimal lienOfNoticeMethod(String separationTypeId,BigDecimal daysTo, BigDecimal leaveBal, BigDecimal basicSalary, String periodOdNotice){
			Delegator delegator=DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> SeparationTypeELI=null;
			BigDecimal leaveAllowance = BigDecimal.ZERO;
			BigDecimal lienOfNoticeAmount = BigDecimal.ZERO;
			BigDecimal month = new BigDecimal(12);
			BigDecimal daysOfyear = new BigDecimal(365);
			BigDecimal noticePeriod = new BigDecimal(periodOdNotice);
			BigDecimal allSixtyDays = new BigDecimal(60);   
			GenericValue procedureNotificationELE = null;
			
			 try{
				 procedureNotificationELE = delegator.findOne("SeparationTypes", UtilMisc.toMap("separationTypesId", separationTypeId), false);
			 }catch(GenericEntityException ex){
				 ex.printStackTrace();
			 }
			  String notificationProcedure = null;
			 if(procedureNotificationELE.size() > 0){
				 notificationProcedure = procedureNotificationELE.getString("notificationProcedure");
			 }
			 
			 log.info("########************NOTIFICATION PROCEDURE ******#########"+notificationProcedure);
			 
			// ---------- && (notificationProcedure == "EmplToAdmin")
			 if(notificationProcedure.equalsIgnoreCase("EmplToAdmin")){
			 
			   if(daysTo.compareTo(BigDecimal.ZERO) == 0){
				   lienOfNoticeAmount = noticePeriod.multiply(basicSalary).multiply(month).divide(daysOfyear,4,RoundingMode.HALF_UP);
			     }else if(daysTo.compareTo(noticePeriod) > 0){
				        lienOfNoticeAmount = BigDecimal.ZERO;
		     	 }else if(daysTo.compareTo(noticePeriod) < 0){
			    	BigDecimal lessDaysTo = noticePeriod.subtract(daysTo);
				    lienOfNoticeAmount = lessDaysTo.multiply(basicSalary).multiply(month).divide(daysOfyear,4,RoundingMode.HALF_UP);
			   }
			 }else{
				 lienOfNoticeAmount = BigDecimal.ZERO;
			 }
			
			log.info("--------------**LIEN OF NOTICE------***----------    "+ lienOfNoticeAmount);
			
			return lienOfNoticeAmount;
		}
	
 //-----------lieu of notice ADMIN TO EMPL
		public static BigDecimal lienOfNoticeMethodAdminToEmpl(String separationTypeId,BigDecimal daysTo, BigDecimal leaveBal, BigDecimal basicSalary, String periodOdNotice){
			Delegator delegator=DelegatorFactoryImpl.getDelegator(null);
			List<GenericValue> SeparationTypeELI=null;
			BigDecimal leaveAllowance = BigDecimal.ZERO;
			BigDecimal lienOfNoticeAmount = BigDecimal.ZERO;
			BigDecimal month = new BigDecimal(12);
			BigDecimal daysOfyear = new BigDecimal(365);
			BigDecimal noticePeriod = new BigDecimal(periodOdNotice);
			BigDecimal allSixtyDays = new BigDecimal(60);   
			GenericValue procedureNotificationELE = null;
			
			 try{
				 procedureNotificationELE = delegator.findOne("SeparationTypes", UtilMisc.toMap("separationTypesId", separationTypeId), false);
			 }catch(GenericEntityException ex){
				 ex.printStackTrace();
			 }
			  String notificationProcedure = null;
			 if(procedureNotificationELE.size() > 0){
				 notificationProcedure = procedureNotificationELE.getString("notificationProcedure");
			 }
			 
			 log.info("########************NOTIFICATION PROCEDURE ******#########"+notificationProcedure);
			 
			// ---------- && (notificationProcedure == "EmplToAdmin")
			 if(notificationProcedure.equalsIgnoreCase("AdminToEmpl")){
			 
			   if(daysTo.compareTo(BigDecimal.ZERO) == 0){
				   lienOfNoticeAmount = noticePeriod.multiply(basicSalary).multiply(month).divide(daysOfyear,4,RoundingMode.HALF_UP);
			     }else if(daysTo.compareTo(noticePeriod) > 0){
				        lienOfNoticeAmount = BigDecimal.ZERO;
		     	 }else if(daysTo.compareTo(noticePeriod) < 0){
			    	BigDecimal lessDaysTo = noticePeriod.subtract(daysTo);
				    lienOfNoticeAmount = lessDaysTo.multiply(basicSalary).multiply(month).divide(daysOfyear,4,RoundingMode.HALF_UP);
			   }
			 }else{
				 lienOfNoticeAmount = BigDecimal.ZERO;
			 }
			
			log.info("--------------**LIEN OF NOTICE------***----------    "+ lienOfNoticeAmount);
			
			return lienOfNoticeAmount;
		}
		
		
	//Method to Return the Employee Service Pay
	
	public static BigDecimal servicePayAmount(String separationTypeId, BigDecimal basicSalary, int yearsWrkd){
		
		log.info("################SERVICEPAY METHOD");
		
	    Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> TheSeparationTypeELI = null;
		BigDecimal intBigDeYearsWorked = new  BigDecimal(yearsWrkd);
		
		try{
			TheSeparationTypeELI=delegator.findList("SeparationTypes", EntityCondition.makeCondition(UtilMisc.toMap("separationTypesId", separationTypeId))
					, null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
	
		Double zeroAmount=0.0;
		BigDecimal bigZeroAmt= new BigDecimal(zeroAmount);
		
		String status = null;
		String years = null;
		BigDecimal yearss = null;
		for(GenericValue genericValue:TheSeparationTypeELI ){
			status = genericValue.getString("servicePay");
			years = genericValue.getString("yearsWorked");
			yearss= new BigDecimal(years);
		}
		
		BigDecimal servicePayAmt=BigDecimal.ZERO;
		
		if(status.equalsIgnoreCase("YES")){
			
			servicePayAmt = yearss.multiply(intBigDeYearsWorked).multiply(basicSalary);
			
		}else{
			servicePayAmt = bigZeroAmt;
		}
	
		log.info("################SERVICEPAY METHOD"+servicePayAmt);
		
		return servicePayAmt;
		
	}
		
	
	//Number of years worked
	
	public static int getNumberOfYearsBetweenWK(String strDate1, String dateFormat1, String strDate2, String dateFormat2) {
		log.info("------------- YEARS BETWEEN METHOD----------");       
		
		int years = 0;
		         
		        Date date1, date2 = null;
		        SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormat1);
		        SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
		        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
		         
		        try {
		            date1 = (Date)sdf1.parse(strDate1);
		            date2 = (Date)sdf2.parse(strDate2);
		             
		            int year1 = Integer.parseInt(sdfYear.format(date1));
		            int year2 = Integer.parseInt(sdfYear.format(date2));
		             
		            years = year2 - year1;
		        } catch (ParseException ex) {
		            System.err.println(ex.getMessage());
		        }
		       
		        log.info("#############YEARS BTW###############"+years);
		        return years;
		    }
		    
//get notice period of the searation type
	public static String getNoticePeriod(String separationTypeId){
		
		log.info("-------------NOTICE PERIOD------------");
		
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> noticePeriodELI = null;
		
		try{
			noticePeriodELI=delegator.findList("SeparationTypes", EntityCondition.makeCondition(UtilMisc.toMap("separationTypesId", separationTypeId)),
					null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		String noticeP =null;
		for(GenericValue genericValue: noticePeriodELI){
			noticeP=genericValue.getString("noticePeriod");
		}
		log.info("##############NOTICE PERIOD##############"+noticeP);
		return noticeP;
		
	}
	
	//
	
public static BigDecimal getGoldenHandShake(String separationTypeId){
		
		log.info("=============GOLDEN HAND SHAKE----------------");
		
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> noticePeriodELI = null;
		
		try{
			noticePeriodELI=delegator.findList("SeparationTypes", EntityCondition.makeCondition(UtilMisc.toMap("separationTypesId", separationTypeId)),
					null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		BigDecimal golden = BigDecimal.ZERO;
		for(GenericValue genericValue: noticePeriodELI){
			golden=genericValue.getBigDecimal("goldenHandShake");
		}
		log.info("##############GOLDEN HAND SHAKE##############"+golden);
		return golden;
		}

//

public static BigDecimal transportAllowance(String separationTypeId){
		
		log.info("##########TRANSPORT ALLOWANCE##########");
		
		Delegator delegator= DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> noticePeriodELI = null;
		
		try{
			noticePeriodELI=delegator.findList("SeparationTypes", EntityCondition.makeCondition(UtilMisc.toMap("separationTypesId", separationTypeId)),
					null, null, null, false);
		}catch(GenericEntityException e){
			e.printStackTrace();
		}
		
		BigDecimal golden = BigDecimal.ZERO;
		for(GenericValue genericValue: noticePeriodELI){
			golden=genericValue.getBigDecimal("transportAllowance");
		}
		log.info("######TRANSPORT ALLOWANCE##########"+golden);
		return golden;
		}
	
// get the appointment date
public static String getAppointmentDate(String partyId) {
	String appointmentdate = "";

	Delegator delegator =DelegatorFactoryImpl.getDelegator(null);

	List<GenericValue> getLeaveappointmentdateELI = null;

	try {
		getLeaveappointmentdateELI = delegator.findList("Person",
				EntityCondition.makeCondition("partyId", partyId), null,
				null, null, false);
	} catch (GenericEntityException e) {
		e.printStackTrace();
	}

	for (GenericValue genericValue : getLeaveappointmentdateELI) {
		appointmentdate = genericValue.getString("appointmentdate");
	}

	return appointmentdate;

}

//method for gross total

public static BigDecimal grossTotal(BigDecimal basicSalary, BigDecimal leaveAllowance, BigDecimal servicePay, BigDecimal transportAllowance, BigDecimal goldenHandShake,BigDecimal lienOfNoticeAdminToEmpl){
	log.info("########GROSS METHOD#######");
	
	// -----Subjected to PAYE
	
	BigDecimal total= BigDecimal.ZERO;
	try{
	total=basicSalary.add(leaveAllowance).add(servicePay).add(transportAllowance).add(goldenHandShake).add(lienOfNoticeAdminToEmpl);
	}catch(Exception e){
		e.printStackTrace();
	}
	log.info("########GROSS RESULT#######  "+total);
	return total;
	
 }


//PAYE

 public static BigDecimal amountPAYE(BigDecimal grossIncome,BigDecimal lienOfNotice){
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> getPAYEEtable = null;
		BigDecimal PayeeAmt = BigDecimal.ZERO;
		BigDecimal grossMinusLienOfNotice = BigDecimal.ZERO;

		log.info("--------------PAYE METHOD ON WORK------------------------");

		EntityConditionList<EntityExpr> payeeConditions = EntityCondition.makeCondition(
				UtilMisc.toList(
						EntityCondition.makeCondition("lowerbracket", EntityOperator.LESS_THAN_EQUAL_TO,
								grossIncome),
				EntityCondition.makeCondition("upperbracket", EntityOperator.LESS_THAN_EQUAL_TO, grossIncome)),
				EntityOperator.AND);
		try {
			getPAYEEtable = delegator.findList("PAYETable", payeeConditions, null, null, null, false);
		} catch (GenericEntityException ex) {
			ex.printStackTrace();
			log.info("-------------Bracket Not Found------------");
		}

		Double percent = 0.0;
		BigDecimal percentBigInt = BigDecimal.ZERO;
		BigDecimal hundredPercent = new BigDecimal(100);
		
		for (GenericValue genericValue : getPAYEEtable) {
			percent = genericValue.getDouble("percentage");
			percentBigInt = new BigDecimal(percent);
		}

		/*if (getPAYEEtable.size() < 0) {
			percentBigInt = BigDecimal.ZERO;
			log.info("-------------ZERO % USED-----------");
		}
*/
		log.info("-------------Percentage Of Tax------------" + percentBigInt);

		
		try {
			
			//grossMinusLienOfNotice = grossIncome.subtract(lienOfNotice);
			
			PayeeAmt = percentBigInt.multiply(grossIncome).divide(hundredPercent,4,RoundingMode.HALF_UP);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		BigDecimal roundedPayeeAmt = PayeeAmt.setScale(2, RoundingMode.CEILING);

		log.info("########PAYE#######  " + PayeeAmt);
		log.info("########PAYE Rounded----------**--#######  " + roundedPayeeAmt);
		return roundedPayeeAmt;

	}
 
// ------------------Subjected to PAyee
 public static BigDecimal grossSubjectToPAYEE(BigDecimal grossIncome,BigDecimal lienOfNotice){

	 BigDecimal grossMinusLienOfNotice = BigDecimal.ZERO;
	 try{
		 grossMinusLienOfNotice = grossIncome.subtract(lienOfNotice);
		
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 
	 
	 log.info("-----------Amount Subject To Tax (Hii KRA Nayo!!!!!!!!!)--------- "+grossMinusLienOfNotice);
	
	 return grossMinusLienOfNotice;
	
 }
 
 //Amount After Paye
 public static BigDecimal totalNetAmount(BigDecimal grossIncome,BigDecimal payeAmt){
	
	 BigDecimal netAmount= BigDecimal.ZERO;
	 try{
	 	 netAmount = grossIncome.subtract(payeAmt);
		 
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 log.info("---------------NET AFTER PAYE------------ "+netAmount);
	 return netAmount;
	 
 }
 
 public static BigDecimal finalTotal(BigDecimal netAfterPaye,BigDecimal amountLostByEmployee, BigDecimal lienOfNotice){
	
	 BigDecimal finalAmount= BigDecimal.ZERO;
	 try{
		 
		 finalAmount = netAfterPaye.subtract(amountLostByEmployee).subtract(lienOfNotice);
		 
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 log.info("########FINAL AMOUNT#######  "+finalAmount);
	 
	 return finalAmount;
	 
	 
 }
 

}// end of class definition

