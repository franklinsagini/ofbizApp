package org.ofbiz.loans;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.webapp.event.EventHandlerException;

/***
 * Computing Armotization
 * 
 * @author Japheth Odonya @when Aug 7, 2014 12:21:09 AM
 * 
 **/
public class AmortizationServices {
	private static int ONEHUNDRED = 100;
	private static int ONE = 1;

	/****
	 * For the Loan Application Specified calculate the Armotization Schedule
	 **/
	public static String generateschedule(HttpServletRequest request,
			HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		GenericValue loanApplication = null, loanAmortization;

		// Get the Loan Application ID
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		/**
		 * Given: Loan Amount, Interest Rate and Payment Period Calculate the
		 * Monthly Payment (Straight Line)
		 * 
		 * **/
		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal("interestRatePM").divide(
				new BigDecimal(ONEHUNDRED));
		int iRepaymentPeriod = loanApplication.getInteger("repaymentPeriod");
		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount = calculatePaymentAmount(
				dbLoanAmt,
				bdInterestRatePM,
				iRepaymentPeriod);

		//This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;
		String loanAmortizationId;
		
		int iAmortizationCount = 0;
		
		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();
		
		while (iAmortizationCount < iRepaymentPeriod){
			iAmortizationCount++;
			loanAmortizationId = delegator.getNextSeqId("LoanAmortization", 1);
			
			loanAmortization = delegator.makeValueSingle("LoanAmortization", UtilMisc.toMap("loanAmortizationId", loanAmortizationId));
			loanAmortization.set("loanAmortizationId", loanAmortizationId);
			loanAmortization.set("paymentNo", iAmortizationCount);
			loanAmortization.set("loanApplicationId", loanApplicationId);
			
			loanAmortization.set("paymentAmount", paymentAmount);
			
			bdRepaymentInterestAmt = bdPreviousBalance.multiply(bdInterestRatePM);
			
			loanAmortization.set("interestAmount", bdRepaymentInterestAmt);
			
			dbRepaymentPrincipalAmt = paymentAmount.subtract(bdRepaymentInterestAmt);
			loanAmortization.set("principalAmount", dbRepaymentPrincipalAmt);
			
			bdPreviousBalance = bdPreviousBalance.subtract(dbRepaymentPrincipalAmt);
			loanAmortization.set("balanceAmount", bdPreviousBalance);
			
			listTobeStored.add(loanAmortization);
		}
		//Save the list
		try {
			delegator.storeAll(listTobeStored);
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// Get the

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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return "";
	}

	/***
	 * Calculate the Per Month Payment, Assuming Straight Line
	 * 
	 * @author Japheth Odonya @when Aug 7, 2014 12:21:55 AM
	 * 
	 * */
	private static BigDecimal calculatePaymentAmount(BigDecimal loanAmt,
			BigDecimal interestRatePM, int repaymentPeriod) {

		BigDecimal bdPaymentAmount;
		// paymentAmount = (interestRatePM.multiply(loanAmt)).multiply((new
		// BigDecimal(1).add(interestRatePM.divide(new
		// BigDecimal(100)))).pow(repaymentPeriod));

		BigDecimal bdInterestByPrincipal = interestByPrincipal(interestRatePM,
				loanAmt);

		BigDecimal bdOnePlusInterestPowerPeriod = onePlusInterestPowerPeriod(
				interestRatePM, repaymentPeriod);

		BigDecimal bdOnePlusInterestPowerPeriodMinusOne = onePlusInterestPowerPeriodMinusOne(
				interestRatePM, repaymentPeriod);

		// paymentAmount = interestByPrincipal timesOnePlusInterestPowerPeriod
		// divideOnePlusInterestMinusOne
		bdPaymentAmount = bdInterestByPrincipal.multiply(
				bdOnePlusInterestPowerPeriod).divide(
				bdOnePlusInterestPowerPeriodMinusOne);

		return bdPaymentAmount;
	}

	/***
	 * Product of Interest Rate and Principal
	 * */
	private static BigDecimal interestByPrincipal(BigDecimal interestRatePM,
			BigDecimal loanAmt) {
		return interestRatePM.multiply(loanAmt);
	}

	/***
	 * One Plus Interest Power Period
	 * */
	private static BigDecimal onePlusInterestPowerPeriod(
			BigDecimal interestRatePM, int repaymentPeriod) {
		return (interestRatePM.add(new BigDecimal(ONE))).pow(repaymentPeriod);
	}

	/***
	 * One Plus Interest Power Period Minus ONE
	 * **/
	private static BigDecimal onePlusInterestPowerPeriodMinusOne(
			BigDecimal interestRatePM, int repaymentPeriod) {
		return ((new BigDecimal(ONE).add(interestRatePM)).pow(repaymentPeriod))
				.subtract(new BigDecimal(ONE));
	}

}
