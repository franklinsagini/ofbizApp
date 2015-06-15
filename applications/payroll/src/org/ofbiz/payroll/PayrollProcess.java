package org.ofbiz.payroll;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.payroll.TaxTracker;
import org.ofbiz.webapp.event.EventHandlerException;


/**
 * @author charles
 * **/
public class PayrollProcess {

	static BigDecimal bdMAX_PENSION_CONTRIBUTION = BigDecimal.ZERO;
	static BigDecimal bdINSURANCE_RELIEF_MAX = BigDecimal.ZERO;
	static BigDecimal bdBasicPay = BigDecimal.ZERO;

	/***
	 * Values to Write
	 *
	 * */

	public static BigDecimal bdEXCESSPENSIONBENEFIT;
	public static BigDecimal bdINSURANCERELIEF;
	public static BigDecimal bdLOWINTERESTBENEFIT;
	public static BigDecimal bdMPR;
	public static BigDecimal bdNETPAY;
	public static BigDecimal bdNHIF;
	public static BigDecimal bdNHIFEmployer = BigDecimal.ZERO;
	public static BigDecimal bdPAYE;
	public static BigDecimal bdTAXABLEINCOME;
	public static BigDecimal bdTOTDEDUCTIONS;
	public static BigDecimal bdNSSFStatutory = BigDecimal.ZERO;
	public static BigDecimal bdNSSFEmployer = BigDecimal.ZERO;
	public static BigDecimal bdNSSFVoluntary = BigDecimal.ZERO;
	public static BigDecimal bdPensionAmt = BigDecimal.ZERO;
	public static BigDecimal bdPensionEmployer = BigDecimal.ZERO;
	
	
	public static BigDecimal bdNSSFSUMMARY = BigDecimal.ZERO;
	public static BigDecimal bdNHIFSUMMARY = BigDecimal.ZERO;
	public static BigDecimal bdPAYESUMMARY = BigDecimal.ZERO;
	public static BigDecimal bdNETPAYSUMMARY = BigDecimal.ZERO;
	public static BigDecimal bdPENSIONSUMMARY = BigDecimal.ZERO;
	
	

	private static Logger log = Logger.getLogger(PayrollProcess.class);

	public static String runPayroll(HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String payrollPeriodId = (String) request
				.getParameter("payrollPeriodId");
		// Get employees
		List<GenericValue> employeesELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### payrollPeriodId is :::: " + payrollPeriodId);

		// Delegator delegator = DelegatorFactoryImpl.getDelegator(null); //=
		// party.getDelegator();

		// EntityConditionList<EntityExpr> employeeConditions = EntityCondition
		// .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		// "partyId", EntityOperator.EQUALS,
		// partyId), EntityCondition.makeCondition(
		// "payrollPeriodId", EntityOperator.EQUALS,
		// periodId)), EntityOperator.AND);

		// try {
		// chequeDepositELI = delegator.findList("AccountTransaction",
		// transactionConditions, null, null, null, false);
		//
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }

		try {
			employeesELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("payrollPeriodId", payrollPeriodId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		//removeCalculatedValues(payrollPeriodId, delegator);

		log.info("######### Loop through Staff");

	for (GenericValue genericValue : employeesELI) {
			deleteStaffSystemElements(genericValue
					.getString("staffPayrollId"), delegator); 
			
			processPayroll(genericValue, genericValue
					.getString("staffPayrollId"), delegator);
			log.info("######### Staff ID "
					+ genericValue.getString("staffPayrollId"));
			manageBalances(genericValue, genericValue.getString("staffPayrollId"), delegator);
		}

	List<GenericValue> listPayrollPeriodSummary = new ArrayList<GenericValue>();
	
	log.info("######### bdNSSFSUMMARY ######### " + bdNSSFSUMMARY);
	log.info("######### bdNHIFSUMMARY ######### " + bdNHIFSUMMARY);
	log.info("######### bdPENSIONSUMMARY ######### " + bdPENSIONSUMMARY);
	log.info("######### bdPAYESUMMARY ######### " + bdPAYESUMMARY);
	log.info("######### bdNETPAYSUMMARY ######### " + bdNETPAYSUMMARY);
	
	deletePayrollPeriodSummary(delegator, payrollPeriodId);
	
	bdNSSFSUMMARY = bdNSSFSUMMARY.setScale(4, RoundingMode.HALF_UP);
	listPayrollPeriodSummary.add(createSummaryToSave(delegator, payrollPeriodId, "NSSF", bdNSSFSUMMARY));	
	bdNHIFSUMMARY = bdNHIFSUMMARY.setScale(4, RoundingMode.HALF_UP);	
	listPayrollPeriodSummary.add(createSummaryToSave(delegator, payrollPeriodId, "NHIF", bdNHIFSUMMARY));	
	bdPENSIONSUMMARY = bdPENSIONSUMMARY.setScale(4, RoundingMode.HALF_UP);	
	listPayrollPeriodSummary.add(createSummaryToSave(delegator, payrollPeriodId, "PENSION", bdPENSIONSUMMARY));	
	bdPAYESUMMARY = bdPAYESUMMARY.setScale(4, RoundingMode.HALF_UP);
	listPayrollPeriodSummary.add(createSummaryToSave(delegator, payrollPeriodId, "PAYE", bdPAYESUMMARY));	
	bdNETPAYSUMMARY = bdNETPAYSUMMARY.setScale(4, RoundingMode.HALF_UP);
	listPayrollPeriodSummary.add(createSummaryToSave(delegator, payrollPeriodId, "NETPAY", bdNETPAYSUMMARY));
	
	try {
		delegator.storeAll(listPayrollPeriodSummary);
	} catch (GenericEntityException e) {
		// TODO Auto-generated catch block
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
	private static GenericValue createSummaryToSave(Delegator delegator, String payrollPeriodId, String Name, BigDecimal Amount ){
		String payrollPeriodSummarySequenceId = delegator
		.getNextSeqId("PayrollPeriodSummary");

		GenericValue payrollPeriodSummary = delegator.makeValidValue(
				"PayrollPeriodSummary", UtilMisc.toMap(
						"payrollPeriodSummaryId",
						payrollPeriodSummarySequenceId, "payrollPeriodId",
						payrollPeriodId, "elementName", Name, "amount",
						Amount, "closed", "N"));
		try {
			payrollPeriodSummary = delegator
					.createSetNextSeqId(payrollPeriodSummary);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return payrollPeriodSummary;
	}
	
	private static void deletePayrollPeriodSummary(Delegator delegator, String payrollPeriodId) {
		// TODO Auto-generated method stub
		List<GenericValue> PayrollPeriodSummaryELI = null;
		
		try {
			PayrollPeriodSummaryELI = delegator.findList("PayrollPeriodSummary",
					EntityCondition.makeCondition("payrollPeriodId", payrollPeriodId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		try {
			delegator.removeAll(PayrollPeriodSummaryELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	private static void manageBalances(GenericValue employee,
			String staffPayrollId, Delegator delegator) {
		List<GenericValue> staffPayrollElementELI = null;	
		
		EntityConditionList<EntityExpr> elementConditions = EntityCondition
		.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("staffPayrollId", EntityOperator.EQUALS, staffPayrollId),
				EntityCondition.makeCondition("elementType", EntityOperator.EQUALS, "Deduction"),
				EntityCondition.makeCondition("hasBalance", EntityOperator.EQUALS, "Y")), EntityOperator.AND);
		
		try {
			staffPayrollElementELI = delegator.findList("PayrollElementAndStaffPayrollElement",
					elementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementELI) {
			calcBalances(staffPayrollElement, delegator);
		}		
	}

	private static void calcBalances(GenericValue staffPayrollElement,
			Delegator delegator) {
		BigDecimal amount=BigDecimal.ZERO;
		BigDecimal balance=BigDecimal.ZERO;
		BigDecimal newAmount=BigDecimal.ZERO;
		BigDecimal newBalance=BigDecimal.ZERO;
		
		if(!(staffPayrollElement.getBigDecimal("amount")==null))
		{
			amount=staffPayrollElement.getBigDecimal("amount");
		}
		if(!(staffPayrollElement.getBigDecimal("balance")==null))
		{
			balance=staffPayrollElement.getBigDecimal("balance");
		}
		
		
		if(!(staffPayrollElement.getString("valueChanged").equals("Y")))
		{
			if(staffPayrollElement.getString("cummulative").equals("Y"))
			{				
				newBalance=balance.add(amount);
				setNewBalance(staffPayrollElement, amount, newBalance, delegator);
				
			}else
			{
				if(amount.compareTo(balance)<=0)
				{
					newBalance=balance.subtract(amount);
					newAmount=amount;					
				}
				else
				{
					newBalance=BigDecimal.ZERO;
					newAmount=balance;
				}
				setNewBalance(staffPayrollElement, newAmount, newBalance, delegator);
			}
		}
		else
		{
			log.info("Balance Already Modified ");
		}
	}

	private static void setNewBalance(GenericValue staffPayrolElement,
			BigDecimal newAmount, BigDecimal newBalance, Delegator delegator) {
		
	//	String staffPayrollElementsSequenceId = delegator.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElementNew = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsId",staffPayrolElement.getString("staffPayrollElementsId"), 
						"staffPayrollId", staffPayrolElement.getString("staffPayrollId"),  
						"payrollElementId",	staffPayrolElement.getString("payrollElementId"), 
						"amount", newAmount, 
						"balance", newBalance,
						"valueChanged", "Y"));
		try {
		//	staffPayrollElementNew = delegator.createSetNextSeqId(staffPayrollElementNew);
			delegator.store(staffPayrollElementNew);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}	
	}

	private static void deleteStaffSystemElements(String staffPayrollId,
			Delegator delegator) {
		// TODO Auto-generated method stub
		//Get the PayrollElementID that are system elements
		List<GenericValue> listPayrollElementId = getSystemElements(delegator);
		
		//Delete from staffpayrollelements a record that is this PayrollElementID and this staffPayrollId
		deleteStaffPayrollElement(listPayrollElementId, staffPayrollId, delegator);
		
		
	}

	private static void deleteStaffPayrollElement(
			List<GenericValue> listPayrollElementId, String staffPayrollId, Delegator delegator) {
		
		for (GenericValue genericValue : listPayrollElementId) {
			deleteRecord(delegator, genericValue.getString("payrollElementId"), staffPayrollId );
		}
		
	}

	private static void deleteRecord(Delegator delegator, String payrollElementId,
			String staffPayrollId) {
		// TODO Auto-generated method stub
		List<GenericValue> StaffPayrollElementELI = null;
		
		EntityConditionList<EntityExpr> staffPayrollElementConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
				"staffPayrollId", EntityOperator.EQUALS, staffPayrollId),
				EntityCondition.makeCondition("payrollElementId",
						EntityOperator.EQUALS, payrollElementId)),
				EntityOperator.AND);
		
		try {
			StaffPayrollElementELI = delegator.findList("StaffPayrollElements",
					staffPayrollElementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		try {
			delegator.removeAll(StaffPayrollElementELI);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static List<GenericValue> getSystemElements(Delegator delegator) {
		List<GenericValue> listSystemElements = null;
		
		EntityConditionList<EntityExpr> PayrollElementConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
				"elementType", EntityOperator.EQUALS, "System Element"),
				EntityCondition.makeCondition("isInterest",
						EntityOperator.EQUALS, "Y")),
				EntityOperator.OR);
		try {
			listSystemElements = delegator.findList("PayrollElement",
					PayrollElementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return listSystemElements;
	}


	private static void processPayroll(GenericValue employee,
			String staffPayrollId, Delegator delegator) {
		// Compute Gross Pay
		BigDecimal bdGrossPay = BigDecimal.ZERO;
		BigDecimal bdTaxablePay = BigDecimal.ZERO;
		BigDecimal bdGrossTax = BigDecimal.ZERO;
		BigDecimal bdPAYEAmount = BigDecimal.ZERO;
		BigDecimal bdNHIFAmount = BigDecimal.ZERO;
		BigDecimal bdTotRelief = BigDecimal.ZERO;
		BigDecimal bdTotDeductions = BigDecimal.ZERO;
		BigDecimal bdNetPay = BigDecimal.ZERO;

		bdBasicPay = getBasicPay(employee, staffPayrollId, delegator);
		log.info("######### Basic Pay Amount " + bdBasicPay);
		bdGrossPay = sumPayments(employee, staffPayrollId, delegator);
		log.info("######### Gross Pay Amount " + bdGrossPay);

		bdTaxablePay = calcTaxablePay(employee, delegator, bdGrossPay,
				staffPayrollId);
		log.info("######### Taxable Pay Amount " + bdTaxablePay);
		bdTAXABLEINCOME = bdTaxablePay;
		
		if(bdTaxablePay.compareTo(BigDecimal.ZERO)>0)
		{
			bdGrossTax = computeGrossTax(bdTaxablePay, delegator);
			bdNHIFAmount = computeNHIF(employee, delegator, bdBasicPay);
			bdNHIF = bdNHIFAmount;
			bdTotRelief = getTotalRelief(employee, staffPayrollId, delegator);

			bdPAYEAmount = bdGrossTax.subtract(bdTotRelief);
			bdPAYE = bdPAYEAmount;			
			
		}
		else
		{
			bdGrossTax = BigDecimal.ZERO;
			bdNHIFAmount = computeNHIF(employee, delegator, bdBasicPay);
			bdNHIF = bdNHIFAmount;
			//bdTotRelief = getTotalRelief(employee, staffPayrollId, delegator);
			bdINSURANCERELIEF=BigDecimal.ZERO;
			bdMPR=BigDecimal.ZERO;
//			bdTotRelief=BigDecimal.ZERO;

			bdPAYEAmount = BigDecimal.ZERO;
			bdPAYE = bdPAYEAmount;
		}

		bdNHIFEmployer=bdNHIF;		

		calculateInterestAmounts(employee, staffPayrollId, delegator);
		
		if(bdGrossPay.equals(BigDecimal.ZERO))
		{
			bdNSSFStatutory=BigDecimal.ZERO;
			bdNSSFVoluntary=BigDecimal.ZERO;
			bdNSSFEmployer=BigDecimal.ZERO;			
		}

		bdTotDeductions = sumDeductions(employee, staffPayrollId, delegator).add(bdPensionAmt)
				.add(bdPAYEAmount).add(bdNSSFStatutory).add(bdNSSFVoluntary)
				.add(bdNHIFAmount);
		log.info("######### Tot Deductions " + bdTotDeductions);
		bdTOTDEDUCTIONS = bdTotDeductions;

		bdNetPay = bdGrossPay.subtract(bdTotDeductions);
		log.info("######### Net Pay " + bdNetPay);
		bdNETPAY = bdNetPay;

		// bdtotDeductions=getTotalDeductions(employee, delegator, );


		// Save Gross Pay
		String staffPayrollElementsSequenceId;
		GenericValue staffPayrollElement;

		staffPayrollElementsSequenceId = delegator
				.getNextSeqId("StaffPayrollElements");

		staffPayrollElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsSequenceId",
						staffPayrollElementsSequenceId, "payrollElementId",
						"GROSSPAY", "amount", bdGrossPay, "staffPayrollId",
						staffPayrollId, "valueChanged", "N", "balance", BigDecimal.valueOf(0.0)));
		try {
			staffPayrollElement = delegator
					.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			delegator.createOrStore(staffPayrollElement);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> listSystemElements = new ArrayList<GenericValue>();
		GenericValue systemElement;
		
		
		
		/***
		 * "EXCESSPENSIONBENEFIT" "INSURANCERELIEF" "LOWINTERESTBENEFIT" "MPR"
		 * "NETPAY" "NHIF" "NSSF" "NSSFVOL" "PAYE" "PENSION" "TAXABLEINCOME"
		 * "TOTDEDUCTIONS"
		 * */
		// EXCESSPENSIONBENEFIT
		
		bdEXCESSPENSIONBENEFIT = bdEXCESSPENSIONBENEFIT.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "EXCESSPENSIONBENEFIT", bdEXCESSPENSIONBENEFIT, staffPayrollId));
		bdINSURANCERELIEF = bdINSURANCERELIEF.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "INSURANCERELIEF", bdINSURANCERELIEF, staffPayrollId));
		bdLOWINTERESTBENEFIT = bdLOWINTERESTBENEFIT.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "LOWINTERESTBENEFIT", bdLOWINTERESTBENEFIT, staffPayrollId));
		bdMPR = bdMPR.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "MPR", bdMPR, staffPayrollId));
		bdNETPAY = bdNETPAY.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NETPAY", bdNETPAY, staffPayrollId));
		bdNHIF = bdNHIF.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NHIF", bdNHIF, staffPayrollId));
		bdNHIFEmployer = bdNHIFEmployer.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NHIFEMPLOYER", bdNHIFEmployer, staffPayrollId));
		bdPAYE = bdPAYE.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "PAYE", bdPAYE, staffPayrollId));
		bdTAXABLEINCOME = bdTAXABLEINCOME.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "TAXABLEINCOME", bdTAXABLEINCOME, staffPayrollId));
		bdTOTDEDUCTIONS = bdTOTDEDUCTIONS.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "TOTDEDUCTIONS", bdTOTDEDUCTIONS, staffPayrollId));
		bdNSSFStatutory = bdNSSFStatutory.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NSSF", bdNSSFStatutory, staffPayrollId));
		bdNSSFEmployer = bdNSSFEmployer.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NSSFEMPLOYER", bdNSSFEmployer, staffPayrollId));
		bdNSSFVoluntary = bdNSSFVoluntary.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "NSSFVOL", bdNSSFVoluntary, staffPayrollId));
		bdPensionAmt = bdPensionAmt.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "PENSION", bdPensionAmt, staffPayrollId));
		bdPensionEmployer = bdPensionEmployer.setScale(4, RoundingMode.HALF_UP);
		listSystemElements.add(createElementToSave(delegator, "PENSIONEMPLOYER", bdPensionEmployer, staffPayrollId));
		
		bdNSSFSUMMARY = bdNSSFSUMMARY.add(bdNSSFStatutory).add(bdNSSFVoluntary).add(bdNSSFEmployer);
		bdNHIFSUMMARY = bdNHIFSUMMARY.add(bdNHIF).add(bdNHIFEmployer);
		bdPENSIONSUMMARY = bdPENSIONSUMMARY.add(bdPensionAmt).add(bdPensionEmployer);
		bdPAYESUMMARY = bdPAYESUMMARY.add(bdPAYE);
		bdNETPAYSUMMARY = bdNETPAYSUMMARY.add(bdNETPAY);

		try {
			delegator.storeAll(listSystemElements);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static GenericValue createElementToSave(Delegator delegator, String payrollElementId, BigDecimal elementAmount, String staffPayrollId ){
		String staffPayrollElementsSequenceId = delegator
		.getNextSeqId("StaffPayrollElements");

		GenericValue staffPayrollElement = delegator.makeValidValue(
				"StaffPayrollElements", UtilMisc.toMap(
						"staffPayrollElementsId",
						staffPayrollElementsSequenceId, "payrollElementId",
						payrollElementId, "amount", elementAmount, "staffPayrollId",
						staffPayrollId, "valueChanged", "N", "balance", BigDecimal.valueOf(0.0)));
		try {
			staffPayrollElement = delegator
					.createSetNextSeqId(staffPayrollElement);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return staffPayrollElement;
	}

	private static void calculateInterestAmounts(GenericValue employee,
			String staffPayrollId, Delegator delegator) {

		List<GenericValue> payrollElementsWithInterestELI = null;
		// BigDecimal bdLoanAmount = BigDecimal.ZERO;

		try {
			payrollElementsWithInterestELI = delegator.findList(
					"PayrollElement", EntityCondition.makeCondition(
							"hasInterest", "Y"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// for (GenericValue payrollElement : payrollElementELI) {
		// // Get the amount
		//
		// getLoanElementAmount(payrollElement, delegator);
		// }

		// We have PayrollElements with interest
		for (GenericValue payrolElement : payrollElementsWithInterestELI) {
			if ((payrolElement.getString("isfixed") != null)
					&& (payrolElement.getString("isfixed").equals("Y"))) {
				// Get the amount and add it to the staffPayrolElements
				processFixedInterest(delegator, payrolElement, staffPayrollId);
			} else if ((payrolElement.getString("isfixed") != null)
					&& (payrolElement.getString("isfixed").equals("N"))) {
				// Get the rate, Get the frequency, Get the Balance before
				// deduction, multiply rate by balance then save the new value
				processVariableInterest(delegator, payrolElement,
						staffPayrollId);
			}
		}
	}

	private static BigDecimal getTotalRelief(GenericValue employee,String staffPayrollId,
			Delegator delegator) {

		BigDecimal bdtotalRelief = BigDecimal.ZERO;
		BigDecimal bdInsuranceRelief = BigDecimal.ZERO;
		BigDecimal bdMPRAmount = BigDecimal.ZERO;

		bdInsuranceRelief = getInsuranceRelief(employee, staffPayrollId, delegator).setScale(6, RoundingMode.HALF_UP);
		bdINSURANCERELIEF = bdInsuranceRelief;
		bdMPRAmount = getMPR(employee, delegator);
		bdMPR = bdMPRAmount;
		log.info("######### Insurance Relief " + bdInsuranceRelief);
		log.info("######### MPR " + bdMPR);
		bdtotalRelief = bdInsuranceRelief.add(bdMPR);

		log.info("######### TotRelief " + bdtotalRelief);

		return bdtotalRelief;
	}

	private static BigDecimal getMPR(GenericValue employee, Delegator delegator) {
		BigDecimal bdMPR = BigDecimal.ZERO;

		List<GenericValue> payrollConstantELI = null;

		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdMPR = getMPRAmount(payrollConstant, delegator);
		}
		return bdMPR;
	}

	private static BigDecimal getInsuranceRelief(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdInsRelief = BigDecimal.ZERO;

		BigDecimal bdInsAmount = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> elementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"insurancecontribution", EntityOperator.EQUALS, "Y"),
						EntityCondition.makeCondition("hasRelief",
								EntityOperator.EQUALS, "Y")),
						EntityOperator.AND);

		try {
			payrollElementELI = delegator.findList("PayrollElement",
					elementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdInsAmount = bdInsAmount.add(getElementAmount(payrollElement,staffPayrollId,
					delegator));
		}

		bdInsRelief = bdInsAmount.multiply(getInsuranceReliefPercentage(
				employee, delegator));

		log.info("######### Insurance Max " + bdINSURANCE_RELIEF_MAX);
		if (bdInsRelief.compareTo(bdINSURANCE_RELIEF_MAX) >= 0) {
			bdInsRelief = bdINSURANCE_RELIEF_MAX;
		}

		return bdInsRelief;
	}

	private static BigDecimal getInsuranceReliefPercentage(
			GenericValue employee, Delegator delegator) {
		BigDecimal bdInsPercentage = BigDecimal.ZERO;
		List<GenericValue> insurancePercentgelELI = null;
		try {
			insurancePercentgelELI = delegator.findList("PayrollConstants",
					null, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPension : insurancePercentgelELI) {
			// Get the amount

			bdInsPercentage = getInsPercentage(staffPension, delegator)
					.multiply(new BigDecimal(0.01));
			log.info("######### Insurance but now % " + bdInsPercentage);
		}

		return bdInsPercentage;
	}

	private static BigDecimal getInsPercentage(GenericValue staffInsurance,
			Delegator delegator) {
		BigDecimal bdInsPercentage = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							staffInsurance.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue insPercentge : staffPayrollELI) {
			// Get the amount
			log.info("######### Insurance insuranceReliefRate % "
					+ insPercentge.getBigDecimal("insuranceReliefRate"));
			bdInsPercentage = insPercentge.getBigDecimal("insuranceReliefRate");
			bdINSURANCE_RELIEF_MAX = insPercentge
					.getBigDecimal("insuranceReliefmax");
		}
		log.info("######### Insurance % " + bdInsPercentage);
		return bdInsPercentage;
	}

	private static BigDecimal calcTaxablePay(GenericValue employee,
			Delegator delegator, BigDecimal bdGrossPay, String staffPayrollId) {
		BigDecimal bdTaxableIncome = BigDecimal.ZERO;
		BigDecimal bdExcessPensionBenefit = BigDecimal.ZERO;
		BigDecimal bdLowInterestBenefit = BigDecimal.ZERO;
		BigDecimal bdDisabilityAllowance = BigDecimal.ZERO;
		BigDecimal bdNonTaxableAmounts = BigDecimal.ZERO;
		BigDecimal bdReliefOnTaxableIncome = BigDecimal.ZERO;

		bdExcessPensionBenefit = calcExcessPensionBen(employee, delegator,
				staffPayrollId);
		bdLowInterestBenefit = calcLowInterestBen(employee, staffPayrollId, delegator);
		bdLOWINTERESTBENEFIT = bdLowInterestBenefit;
		bdDisabilityAllowance = getDisabilityAllowance(employee, delegator);
		bdNonTaxableAmounts = getNonTaxableAmounts(employee, staffPayrollId, delegator);
		bdReliefOnTaxableIncome = getReliefOnTaxableIncome(employee, staffPayrollId, delegator);
		

		bdTaxableIncome = bdGrossPay.add(bdExcessPensionBenefit).add(
				bdLowInterestBenefit).subtract(bdNSSFStatutory).subtract(
				bdNSSFVoluntary).subtract(bdPensionAmt).subtract(
				bdDisabilityAllowance).subtract(bdNonTaxableAmounts).subtract(bdReliefOnTaxableIncome);

		if (bdTaxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
			bdTaxableIncome = BigDecimal.ZERO;
		}

		return bdTaxableIncome;
	}

	private static BigDecimal getReliefOnTaxableIncome(GenericValue employee,
			String staffPayrollId, Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdReliefOnTaxableIncome = BigDecimal.ZERO;

/*		EntityConditionList<EntityExpr> reliefOnTaxableIncomeConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"reliefotp", EntityOperator.EQUALS, "Y"),
						EntityCondition.makeCondition("taxable",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);*/

		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("reliefotp","Y"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdReliefOnTaxableIncome = bdReliefOnTaxableIncome.add(getElementAmount(
					payrollElement, staffPayrollId, delegator));
		}

		return bdReliefOnTaxableIncome;
	}
	private static BigDecimal getNonTaxableAmounts(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdNonTaxableAmounts = BigDecimal.ZERO;

		EntityConditionList<EntityExpr> nonTaxableConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"elementType", EntityOperator.EQUALS, "Payment"),
						EntityCondition.makeCondition("taxable",
								EntityOperator.EQUALS, "N")),
						EntityOperator.AND);

		try {
			payrollElementELI = delegator.findList("PayrollElement",
					nonTaxableConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdNonTaxableAmounts = bdNonTaxableAmounts.add(getElementAmount(
					payrollElement, staffPayrollId, delegator));
		}

		return bdNonTaxableAmounts;
	}

	private static BigDecimal getDisabilityAllowance(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdDisabilityAllowance = BigDecimal.ZERO;

		if (checkEmployeeDisability(employee, delegator)) {
			try {
				payrollConstantELI = delegator.findList("PayrollConstants",
						null, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			for (GenericValue payrollConstant : payrollConstantELI) {
				// Get the amount

				bdDisabilityAllowance = getDisabilityAllowanceAmount(
						payrollConstant, delegator);
			}
		} else {
			bdDisabilityAllowance = BigDecimal.ZERO;
		}
		return bdDisabilityAllowance;
	}

	private static boolean checkEmployeeDisability(GenericValue employee,
			Delegator delegator) {
		// List<GenericValue> employeeELI = null;
		GenericValue disabledPerson = null;
		// BigDecimal bdGross = BigDecimal.ZERO;
		try {
			disabledPerson = delegator.findOne("Person", UtilMisc.toMap(
					"partyId", employee.getString("partyId")), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		// for (GenericValue payrollElement : employeeELI) {
		// //Get the amount
		//
		// bdGross = bdGross.add(getElementAmount(payrollElement, delegator));
		// }

		if ((disabledPerson == null)
				|| (disabledPerson.getString("isDisabled") == null)
				|| !(disabledPerson.getString("isDisabled").equals("Y"))) {
			return false;
		} else {
			return true;
		}
	}

	private static BigDecimal getDisabilityAllowanceAmount(
			GenericValue payrollConstant, Delegator delegator) {
		// Do the real getting
		BigDecimal bdDisabltyAllowanceAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdDisabltyAllowanceAmount = staffPayrollConstants
					.getBigDecimal("disability_relief");
		}
		return bdDisabltyAllowanceAmount;
	}


	private static BigDecimal getMPRAmount(GenericValue payrollConstant,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdMPRAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdMPRAmount = staffPayrollConstants
					.getBigDecimal("monthlyPersonalRelief");
		}
		return bdMPRAmount;
	}

	private static BigDecimal calcLowInterestBen(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		BigDecimal bdlowIntBen = BigDecimal.ZERO;
		BigDecimal bdsalaryAdvanceAmount = BigDecimal.ZERO;
		BigDecimal bdsalaryAdvanceBalance = BigDecimal.ZERO;
		BigDecimal bdFinalSalaryAdvance = BigDecimal.ZERO;
		String balanceModified = "";

		List<GenericValue> payrollElementELI = null;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId",
							"10040"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdsalaryAdvanceAmount = getElementAmount(payrollElement, staffPayrollId, delegator);
			bdsalaryAdvanceBalance = getSalaryAdvanceBalance(payrollElement, staffPayrollId,
					delegator);
			balanceModified = getBalanceModified(payrollElement, staffPayrollId, delegator);
		}
		if (!balanceModified.equals("Y")) {
			bdFinalSalaryAdvance = bdsalaryAdvanceBalance;
		} else {
			bdFinalSalaryAdvance = bdsalaryAdvanceAmount
					.add(bdsalaryAdvanceBalance);
		}

		bdlowIntBen = bdFinalSalaryAdvance.multiply(getPrescribedInterestRate(
				employee, delegator));

		return bdlowIntBen;
	}

	private static BigDecimal getPrescribedInterestRate(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdPrescribedIntRate = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdPrescribedIntRate = getPrescribedInterestRateAmount(
					payrollConstant, delegator).divide(new BigDecimal(1200),
					RoundingMode.HALF_UP);
		}

		return bdPrescribedIntRate;
	}

	private static BigDecimal getPrescribedInterestRateAmount(
			GenericValue payrollConstant, Delegator delegator) {
		// Do the real getting
		BigDecimal bdlowInterstRate = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			long lir = staffPayrollConstants.getLong("lowInterstRate");
			bdlowInterstRate = new BigDecimal(new Long(lir).intValue());
		}
		return bdlowInterstRate;
	}

	private static String getBalanceModified(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		String ismodified = "";
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			ismodified = staffPayrollElement.getString("valueChanged");
		}
		return ismodified;
	}

	private static BigDecimal getSalaryAdvanceBalance(
			GenericValue payrollElement, String staffPayrollId, Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			bdAmount = bdAmount.add(staffPayrollElement
					.getBigDecimal("balance"));
		}
		return bdAmount;
	}

	private static BigDecimal calcExcessPensionBen(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		BigDecimal bdEPB = BigDecimal.ZERO;

		bdNSSFStatutory = getNSSFStatutory(employee, delegator);
		log.info("######### NSSF Statutory " + bdNSSFStatutory);
		bdNSSFEmployer=bdNSSFStatutory;
		bdNSSFVoluntary = getNSSFVolAmount(employee, delegator, staffPayrollId);
		log.info("######### NSSF Voluntary " + bdNSSFVoluntary);
		bdPensionAmt = getPensionAmount(employee, delegator, staffPayrollId);
		log.info("######### Pension " + bdPensionAmt);
		log.info("######### Pension " + bdPensionEmployer);
		bdMAX_PENSION_CONTRIBUTION = getMaxPensionContribution(employee,
				delegator);
		log.info("######### Max Pension " + bdMAX_PENSION_CONTRIBUTION);

/*		bdEPB = (bdNSSFStatutory.multiply(new BigDecimal(2))).add(
				bdNSSFVoluntary).add(bdPensionAmt.multiply(new BigDecimal(3)))
				.subtract(bdMAX_PENSION_CONTRIBUTION);
		if (bdEPB.intValue() <= 0) {
			bdEPB = BigDecimal.ZERO;
		}*/
		
		if(bdPensionAmt.compareTo(bdMAX_PENSION_CONTRIBUTION)>0)
		{
			bdEPB=bdPensionAmt.subtract(bdMAX_PENSION_CONTRIBUTION);
		}

		log.info("######### EPB " + bdEPB);
		bdEXCESSPENSIONBENEFIT = bdEPB;
		return bdEPB;
	}

	private static BigDecimal getMaxPensionContribution(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdMAxPensionAmount = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdMAxPensionAmount = getMaxPensionAmount(payrollConstant, delegator);
		}

		return bdMAxPensionAmount;
	}

	private static BigDecimal getMaxPensionAmount(GenericValue payrollConstant,
			Delegator delegator) {
		BigDecimal bdMaxPension = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdMaxPension = staffPayrollConstants
					.getBigDecimal("pension_maxContibution");
		}
		return bdMaxPension;
	}

	private static BigDecimal getPensionAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> pensionELI = null;
		BigDecimal bdPensionAmount = BigDecimal.ZERO;
		try {
			pensionELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : pensionELI) {
			// Get the amount

			bdPensionAmount = getPensionPercentage(employee, delegator)
					.multiply(bdBasicPay);
			
			 bdPensionEmployer=getPensionEmployerRate(delegator)
				.multiply(bdBasicPay);
		}

		return bdPensionAmount;
	}
	
	private static BigDecimal getPensionEmployerRate(Delegator delegator) {
		BigDecimal bdPensionEmployerRate = BigDecimal.ZERO;
		List<GenericValue> payrollElementELI = null;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId",
							"PENSIONEMPLOYER"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElem : payrollElementELI) {
			// Get the amount
			double x = payrollElem.getDouble(("calcRate"));
			
			bdPensionEmployerRate = new BigDecimal(payrollElem.getDouble("calcRate")).divide(new BigDecimal(100));
			log.info("######### Pension Employer % " + bdPensionEmployerRate);
		}

		return bdPensionEmployerRate;
	}

	private static BigDecimal getBasicPay(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdBasicPy = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("payrollElementId",
							"BASICPAY"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdBasicPy = getElementAmount(payrollElement, staffPayrollId, delegator);
		}

		return bdBasicPy;
	}

	private static BigDecimal getPensionPercentage(GenericValue staffPayroll,
			Delegator delegator) {
		BigDecimal bdPensionPercentage = BigDecimal.ZERO;
		List<GenericValue> pensionPercentgelELI = null;
		try {
			pensionPercentgelELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPension : pensionPercentgelELI) {
			// Get the amount

			bdPensionPercentage = getpensionPercentage(staffPension, delegator)
					.divide(new BigDecimal(100), RoundingMode.HALF_UP);
			log.info("######### Pension % " + bdPensionPercentage);
		}

		return bdPensionPercentage;
	}

	private static BigDecimal getpensionPercentage(GenericValue staffPension,
			Delegator delegator) {
		BigDecimal bdPesionPercentage = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPension.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue pensionPertge : staffPayrollELI) {
			// Get the amount
			bdPesionPercentage = pensionPertge
					.getBigDecimal("pensionpercentage");
		}
		return bdPesionPercentage;
	}

	private static BigDecimal getNSSFVolAmount(GenericValue employee,
			Delegator delegator, String staffPayrollId) {
		List<GenericValue> nssfVolELI = null;
		BigDecimal bdNSSFVolAmount = BigDecimal.ZERO;
		try {
			nssfVolELI = delegator.findList("StaffPayroll", EntityCondition
					.makeCondition("staffPayrollId", staffPayrollId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : nssfVolELI) {
			// Get the amount

			bdNSSFVolAmount = getNSSFVolAmount(payrollConstant, delegator);
		}

		return bdNSSFVolAmount;
	}

	private static BigDecimal getNSSFVolAmount(GenericValue staffPayroll,
			Delegator delegator) {
		BigDecimal bdNSSFVolAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollELI = new LinkedList<GenericValue>();
		try {
			staffPayrollELI = delegator.findList("StaffPayroll",
					EntityCondition.makeCondition("staffPayrollId",
							staffPayroll.getString("staffPayrollId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue nssfVolAmnt : staffPayrollELI) {
			// Get the amount
			bdNSSFVolAmount = bdNSSFVolAmount.add(nssfVolAmnt
					.getBigDecimal("nssfVolAmount"));
		}
		return bdNSSFVolAmount;
	}

	private static BigDecimal getNSSFStatutory(GenericValue employee,
			Delegator delegator) {
		List<GenericValue> payrollConstantELI = null;
		BigDecimal bdNSSF = BigDecimal.ZERO;
		try {
			payrollConstantELI = delegator.findList("PayrollConstants", null,
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollConstant : payrollConstantELI) {
			// Get the amount

			bdNSSF = getNSSFAmount(payrollConstant, delegator);
		}

		return bdNSSF;
	}

	private static BigDecimal sumPayments(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdGross = BigDecimal.ZERO;
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					EntityCondition.makeCondition("elementType", "Payment"),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdGross = bdGross.add(getElementAmount(payrollElement, staffPayrollId, delegator));
		}

		return bdGross;
	}

	/**
	 * @author charles
	 * **/
	private static BigDecimal sumDeductions(GenericValue employee, String staffPayrollId,
			Delegator delegator) {
		List<GenericValue> payrollElementELI = null;
		BigDecimal bdOtherDeduction = BigDecimal.ZERO;
		
		EntityConditionList<EntityExpr> PayrollElementConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition
				.makeCondition("elementType", EntityOperator.EQUALS,
						"Deduction"), EntityCondition.makeCondition(
				"employercontribution", EntityOperator.NOT_EQUAL,
				"Y")), EntityOperator.AND);
		
		try {
			payrollElementELI = delegator.findList("PayrollElement",
					PayrollElementConditions,null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue payrollElement : payrollElementELI) {
			// Get the amount

			bdOtherDeduction = bdOtherDeduction.add(getElementAmount(
					payrollElement, staffPayrollId, delegator));
		}

		return bdOtherDeduction;
	}

	private static BigDecimal getElementAmount(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		
		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElement.getString("payrollElementId"))), EntityOperator.AND);
		
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", staffPayrolElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			bdAmount = bdAmount
					.add(staffPayrollElement.getBigDecimal("amount"));
		}
		return bdAmount;
	}

	private static BigDecimal getNSSFAmount(GenericValue payrollConstant,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdNSSFAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollConstantsELI = new LinkedList<GenericValue>();
		
		try {
			staffPayrollConstantsELI = delegator.findList("PayrollConstants",
					EntityCondition.makeCondition("payrollConstantsId",
							payrollConstant.getString("payrollConstantsId")),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollConstants : staffPayrollConstantsELI) {
			// Get the amount
			bdNSSFAmount = staffPayrollConstants
					.getBigDecimal("staticnssfAmount");
		}
		return bdNSSFAmount;
	}

	private static BigDecimal computeGrossTax(BigDecimal bdTaxablePay,
			Delegator delegator) {
		BigDecimal grossTax = BigDecimal.ZERO;
		BigDecimal bdLowerBracket, bdUpperBracket = BigDecimal.ZERO;
		BigDecimal bdPercentage = BigDecimal.ZERO;

		// Get the TaxTable
		List<GenericValue> payeTableELI = new LinkedList<GenericValue>();
		try {
			payeTableELI = delegator.findList("PAYETable", null, null, UtilMisc
					.toList("lowerbracket"), null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdGrowth;
		BigDecimal bdNewTaxable = bdTaxablePay;
		List<TaxTracker> listTaxTracker = new ArrayList<TaxTracker>();
		TaxTracker taxTracker;
		Long count = 0l;
		for (GenericValue genericValue : payeTableELI) {
			count++;
			bdLowerBracket = genericValue.getBigDecimal("lowerbracket");
			bdUpperBracket = genericValue.getBigDecimal("upperbracket");
			bdPercentage = genericValue.getBigDecimal("percentage");
			log.info("###############################");
			log.info("Lower Value:" + bdLowerBracket);
			log.info("Upper Value:" + bdUpperBracket);
			log.info("Percentage Value:" + bdPercentage);
			taxTracker = new TaxTracker();
			bdGrowth = bdUpperBracket.subtract(bdLowerBracket);
			if (!(bdNewTaxable.compareTo(bdGrowth) == -1)) {
				taxTracker.setTaxBase(bdGrowth);
				bdNewTaxable = bdNewTaxable.subtract(bdGrowth);
			} else {
				taxTracker.setTaxBase(bdNewTaxable);
				bdNewTaxable = BigDecimal.ZERO;
			}
			bdPercentage = bdPercentage.multiply(new BigDecimal(0.01));
			taxTracker.setTaxPercent(bdPercentage);
			taxTracker.setCount(count);
			log.info("########## Percentage Divided by 100 : " + bdPercentage);
			listTaxTracker.add(taxTracker);

		}

		// Cummulate the PAYE
		for (TaxTracker taxTracker2 : listTaxTracker) {
			grossTax = grossTax.add(taxTracker2.getTaxBase().multiply(
					taxTracker2.getTaxPercent()));
		}


		log.info("##########Gross Tax :" + grossTax);
		return grossTax;
	}

	private static BigDecimal computeNHIF(GenericValue employee,
			Delegator delegator, BigDecimal bdBasicPay) {
		BigDecimal bdLowerBracket, bdUpperBracket = BigDecimal.ZERO;
		BigDecimal contribution = BigDecimal.ZERO;
		
		if(bdBasicPay.equals(BigDecimal.ZERO))
		{
			contribution=BigDecimal.ZERO;
		}
		else
		{
			// Get the TaxTable
			List<GenericValue> nhifTableELI = new LinkedList<GenericValue>();
			try {
				nhifTableELI = delegator.findList("NHIFTable", null, null, UtilMisc
						.toList("lowerbracket"), null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}

			

			for (GenericValue genericValue : nhifTableELI) {
				bdLowerBracket = genericValue.getBigDecimal("lowerbracket");
				bdUpperBracket = genericValue.getBigDecimal("upperbracket");


				if ((!(bdBasicPay.compareTo(bdLowerBracket) == -1))
						&& (!(bdBasicPay.compareTo(bdUpperBracket) == 1))) {

					contribution = genericValue.getBigDecimal("contribution");
				}
			}
		}
		
		
		log.info("######### NHIF " + contribution);
		return contribution;
	}

/*	private static BigDecimal getLoanElementAmount(GenericValue payrollElement, String staffPayrollId,
			Delegator delegator) {
		// Do the real getting
		BigDecimal bdAmount = BigDecimal.ZERO;
		List<GenericValue> staffPayrollElementsELI = new LinkedList<GenericValue>();
		try {
			staffPayrollElementsELI = delegator.findList(
					"StaffPayrollElements", EntityCondition.makeCondition(
							"payrollElementId", payrollElement
									.getString("payrollElementId")), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue staffPayrollElement : staffPayrollElementsELI) {
			// Get the amount
			if (staffPayrollElement.getString("isfixed").equals("Y")) {

			} else {

			}
		}
		return bdAmount;
	}*/

	/***
	 * @author charles Get the fixed Interest
	 * */
	private static void processFixedInterest(Delegator delegator,
			GenericValue payrolElement, String staffPayrollId) {
		// Save the amount to staffPayrollElements
		/***
		 * payrollElementId staffPayrollId amount balance
		 *
		 *
		 * */
		// GenericValue interestPayrolElement =
		// delegator.makeValidValue("PayrollElement", )
		
		GenericValue staffPayrollElement = getStaffPayrolElement(payrolElement
				.getString("payrollElementId"), staffPayrollId, delegator);
		
		if(!(staffPayrollElement==null))
		{
			String staffPayrollElementsId = delegator.getNextSeqId("StaffPayrollElements");
			BigDecimal bdInterestAmt = payrolElement.getBigDecimal("interestamount");
			GenericValue interestPayrolElement = delegator.makeValidValue(
			"StaffPayrollElements", UtilMisc.toMap(
					"staffPayrollElementsId", staffPayrollElementsId,
					"payrollElementId", payrolElement.getString("childId"),
					"staffPayrollId", staffPayrollId, "amount",
					bdInterestAmt, "balance", BigDecimal.ZERO));
			log.info("Fixed  Interest Amount :: " + bdInterestAmt);

			try {
				delegator.createOrStore(interestPayrolElement);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		else
		{
			log.info("################ Interest Payroll Element Does Not Exist");
		}
	}

	/***
	 * @author charles Get/Compute the variable interest
	 * */
	private static void processVariableInterest(Delegator delegator,
			GenericValue payrolElement, String staffPayrollId) {
		// Get the rate, Get the frequency, Get the Balance before deduction,
		// multiply rate by balance then save the new value
		// BigDecimal bdBalanceAmt =
		GenericValue staffPayrollElement = getStaffPayrolElement(payrolElement
				.getString("payrollElementId"), staffPayrollId, delegator);

		/****
		 * staffPayrollElementsId staffPayrollId payrollElementId amount
		 *
		 *
		 * */
		if(!(staffPayrollElement==null))
		{
			BigDecimal bdInterestAmt = computeInterestAmount(staffPayrollElement,
					payrolElement, delegator);
			log.info("################ Variable Interest Amount :: "
					+ bdInterestAmt);

			String staffPayrollElementsId = delegator
					.getNextSeqId("StaffPayrollElements");
			GenericValue interestPayrolElement = delegator.makeValidValue(
					"StaffPayrollElements", UtilMisc.toMap(
							"staffPayrollElementsId", staffPayrollElementsId,
							"payrollElementId", payrolElement.getString("childId"),
							"staffPayrollId", staffPayrollId, "amount",
							bdInterestAmt, "balance", BigDecimal.ZERO));
			try {
				delegator.createOrStore(interestPayrolElement);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
		}
		else
		{
			log.info("################ Interest Payroll Element Does Not Exist");
		}
		
		
	}

	private static BigDecimal computeInterestAmount(
			GenericValue staffPayrollElement, GenericValue payrolElement,
			Delegator delegator) {

		BigDecimal bdFinalBalance = BigDecimal.ZERO, bdFinalInterestAmt = BigDecimal.ZERO;

		if ((staffPayrollElement != null) && (staffPayrollElement.getString("valueChanged") != null)
				&& (staffPayrollElement.getString("valueChanged").equals("Y"))) {
			bdFinalBalance = staffPayrollElement.getBigDecimal("amount").add(
					staffPayrollElement.getBigDecimal("balance"));
		} else if ((staffPayrollElement != null) && (staffPayrollElement.getString("valueChanged") != null)
				&& (staffPayrollElement.getString("valueChanged").equals("N"))) {
			bdFinalBalance = staffPayrollElement.getBigDecimal("balance");
		}
		log.info("################ Interest Balance :: " + bdFinalBalance);

		// PM Interest
		// BigDecimal bdInterestRate =
		// (payrolElement.getBigDecimal("interestrate") == null) ?
		// BigDecimal.ZERO : payrolElement.getBigDecimal("interestrate");
		BigDecimal bdInterestRate;
		if (payrolElement.getBigDecimal("interestrate") == null) {
			bdInterestRate = BigDecimal.ZERO;
		} else {
			bdInterestRate = payrolElement.getBigDecimal("interestrate");
		}

		if ((payrolElement.getString("interestfrequency") != null)
				&& (payrolElement.getString("interestfrequency").equals("pa"))) {
			bdInterestRate = bdInterestRate.divide(new BigDecimal(1200), 6,
					RoundingMode.HALF_UP).setScale(6, RoundingMode.HALF_UP);
		} else if ((payrolElement.getString("interestfrequency") != null)
				&& (payrolElement.getString("interestfrequency").equals("pm"))) {
			bdInterestRate = (BigDecimal) bdInterestRate.divide(
					new BigDecimal(100), 6, RoundingMode.HALF_UP).setScale(6,
					RoundingMode.HALF_UP);
		}
		log.info("################ Interest Rate :: " + bdInterestRate);

		bdFinalInterestAmt = bdFinalBalance.multiply(bdInterestRate).setScale(
				6, RoundingMode.HALF_UP);
		//
		return bdFinalInterestAmt;
	}

	/***
	 * @author charles 
	 * Get the StaffPayrolElement given the payrollElementId and
	 *         staffPayrollId
	 * */
	private static GenericValue getStaffPayrolElement(String payrollElementId,
			String staffPayrollId, Delegator delegator) {

		List<GenericValue> staffPayrolElementsELI = new LinkedList<GenericValue>();

		EntityConditionList<EntityExpr> staffPayrolElementConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("staffPayrollId", EntityOperator.EQUALS,
								staffPayrollId), EntityCondition.makeCondition(
						"payrollElementId", EntityOperator.EQUALS,
						payrollElementId)), EntityOperator.AND);

		try {
			staffPayrolElementsELI = delegator.findList("StaffPayrollElements",
					staffPayrolElementConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue staffPayrolElement = null;
		for (GenericValue genericValue : staffPayrolElementsELI) {
			staffPayrolElement = genericValue;
		}

		return staffPayrolElement;
	}

}
