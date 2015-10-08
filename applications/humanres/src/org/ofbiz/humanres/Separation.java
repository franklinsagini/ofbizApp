package org.ofbiz.humanres;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
	

	public static BigDecimal calculateTheCalendarDaysBetweenDates(Date created,
		String effectiveDate) {
		Double daysCount = 1.0;
		
		LocalDate localDateStartDate = new LocalDate(created);
		LocalDate localDateEndDate = new LocalDate(effectiveDate);
		while (localDateStartDate.toDate().before(localDateEndDate.toDate())) {
			/*if ((localDateStartDate.getDayOfWeek() != DateTimeConstants.SATURDAY)
					&& (localDateStartDate.getDayOfWeek() != DateTimeConstants.SUNDAY)) {
				daysCount++;
			   }*/
			daysCount++;
			
			localDateStartDate = localDateStartDate.plusDays(1);

		}
        log.info("###########################"+daysCount);
        
		BigDecimal daysCounting = new BigDecimal(daysCount);

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
	

	public static BigDecimal employeeLoans(String partyId){
		
		//EMPLOYEE LOANS TO BE INCLUDED IN THIS METHOD
		
		BigDecimal employeeLoans= new BigDecimal(9000);
		return employeeLoans;
		
	}
	
	public static BigDecimal employeeBasicSalaryKsh(String party){
		BigDecimal empSalary = new BigDecimal(30000);
		 
		 return empSalary;
		
	}
	
/*	public static Double AmountDueToLeave(Double daysTo, Double leaveBal,Double basicSalary){
		Double leaveAllowance=0.0;
		Double excessLeave=0.0;
		Double lessLeave=0.0;
		if(daysTo >= 60){
			leaveAllowance=(leaveBal * basicSalary * 12.0)/365.0;
		}
		else if (daysTo < 60.0) {
			Double addToLeaveToDaysTo;
			addToLeaveToDaysTo=	daysTo+leaveBal;
			 if(addToLeaveToDaysTo==60.0){
				 leaveAllowance=0.00;
			 }else if(addToLeaveToDaysTo>60.0){
				  excessLeave=addToLeaveToDaysTo-60.0;
				 leaveAllowance=(excessLeave * basicSalary * 12.0)/365.0;
			 }else if (addToLeaveToDaysTo < 60) {
				 lessLeave = 60.0 - addToLeaveToDaysTo;
				 leaveAllowance=0.0;
			}
		}else if (daysTo < 60.0 && leaveBal >60.0 ){ 
			leaveAllowance=0.00;
		}else if (daysTo > 60.0 && leaveBal > 60.0) {
			leaveAllowance=(leaveBal * basicSalary * 12.0)/365.0;
		}
		
		return leaveAllowance;
	}*/
	
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
			 noticePeriod= new BigDecimal(noticePeriodDays);
			 log.info("***########**NoticePeriod#####"+noticePeriod);
		}
		
		BigDecimal month = new BigDecimal(12);
		BigDecimal daysOfyear = new BigDecimal(365);
		
		// if days he applied are more or equal to notice period required then give leave allowance
		
		if(daysTo.compareTo(noticePeriod)>=0){ 
			leaveAllowance = (leaveBal.multiply(basicSalary).multiply(month)).divide(daysOfyear,4,RoundingMode.HALF_UP);
		
		}else if (daysTo.compareTo(noticePeriod) < 0) {
			BigDecimal addToLeaveToDaysTo = daysTo.add(leaveBal);
			log.info("#################FIRSTAddToLeaveToDays###############"+ addToLeaveToDaysTo);
			if(addToLeaveToDaysTo.compareTo(noticePeriod)==0){
				
				leaveAllowance = BigDecimal.ZERO;
				
			}else if (addToLeaveToDaysTo.compareTo(noticePeriod) > 0) {
				log.info("#################SECONSDAddToLeaveToDays###############"+ addToLeaveToDaysTo);
				BigDecimal excessLeavedays = addToLeaveToDaysTo.subtract(noticePeriod);
				leaveAllowance = (excessLeavedays.multiply(basicSalary).multiply(new BigDecimal(12))).divide(new BigDecimal(365),4,RoundingMode.HALF_UP);
				log.info("#################ExcessoLeaveToDays###############"+ excessLeavedays);
			}else if (addToLeaveToDaysTo.compareTo(noticePeriod) < 0) {
				
				BigDecimal penaltLessLeaveDays = addToLeaveToDaysTo.subtract(noticePeriod);
				leaveAllowance= (penaltLessLeaveDays.multiply(basicSalary).multiply(month)).divide(daysOfyear,4,RoundingMode.HALF_UP); 
				
				
			}
		}
		
		log.info("#########**LEAVE DAYS ALLOWANCE***######     "+ leaveAllowance);
		
		return leaveAllowance;
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
		log.info("############# YEARS BETWEEN METHOD###############");       
		
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
		       
		        log.info("############################"+years);
		        return years;
		    }
		    
//get notice period of the searation type
	public static String getNoticePeriod(String separationTypeId){
		
		log.info("##############NOTICE PERIOD##############");
		
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
		
		log.info("##############GOLDEN HAND SHAKE##############");
		
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

public static BigDecimal grossTotal(BigDecimal basicSalary, BigDecimal leaveAllowance, BigDecimal servicePay, BigDecimal transportAllowance, BigDecimal goldenHandShake ){
	log.info("########GROSS METHOD#######");
	
	BigDecimal total= BigDecimal.ZERO;
	try{
	total=basicSalary.add(leaveAllowance).add(servicePay).add(transportAllowance).add(goldenHandShake);
	}catch(Exception e){
		e.printStackTrace();
	}
	log.info("########GROSS RESULT#######  "+total);
	return total;
	
 }


//PAYE

 public static BigDecimal amountPAYE(BigDecimal grossIncome){
	 BigDecimal Percentage = new BigDecimal(0.13);
	 BigDecimal PayeeAmt=BigDecimal.ZERO;
	 try{
         
		 PayeeAmt =  Percentage.multiply(grossIncome);
	
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 log.info("########PAYE#######  "+PayeeAmt);
	 return PayeeAmt;
	
 }
 //Amount After Paye
 public static BigDecimal totalNetAmount(BigDecimal grossIncome,BigDecimal payeAmt){
	 BigDecimal netAmount= BigDecimal.ZERO;
	 try{
	 
		 netAmount = grossIncome.subtract(payeAmt);
		 
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 log.info("########NET AFTER PAYE#######  "+netAmount);
	 return netAmount;
	 
 }
 
 public static BigDecimal finalTotal(BigDecimal netAfterPaye,BigDecimal amountLostByEmployee, BigDecimal loans){
	
	 BigDecimal finalAmount= BigDecimal.ZERO;
	 try{
		 
		 finalAmount = netAfterPaye.subtract(amountLostByEmployee).subtract(loans);
		 
	 }catch(Exception e){
			e.printStackTrace();
	 }
	 log.info("########FINAL AMOUNT#######  "+finalAmount);
	 
	 return finalAmount;
	 
	 
 }
 

}// end of class definition
