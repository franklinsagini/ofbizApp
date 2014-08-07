package org.ofbiz.loans;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
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

		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		deleteExistingSchedule(delegator, loanApplicationId);

		/**
		 * Given: Loan Amount, Interest Rate and Payment Period Calculate the
		 * Monthly Payment (Straight Line)
		 * 
		 * **/
		BigDecimal dbLoanAmt = loanApplication.getBigDecimal("loanAmt");
		BigDecimal bdInterestRatePM = loanApplication.getBigDecimal(
				"interestRatePM").divide(new BigDecimal(ONEHUNDRED));
		int iRepaymentPeriod = loanApplication.getLong("repaymentPeriod")
				.intValue();
		BigDecimal dbRepaymentPrincipalAmt, bdRepaymentInterestAmt;
		BigDecimal paymentAmount = calculatePaymentAmount(dbLoanAmt,
				bdInterestRatePM, iRepaymentPeriod);

		// This value will be changing as we go along
		BigDecimal bdPreviousBalance = dbLoanAmt;
		String loanAmortizationId;

		int iAmortizationCount = 0;

		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();

		while (iAmortizationCount < iRepaymentPeriod) {
			iAmortizationCount++;
			loanAmortizationId = delegator.getNextSeqId("LoanAmortization", 1);

			bdRepaymentInterestAmt = bdPreviousBalance
					.multiply(bdInterestRatePM);
			dbRepaymentPrincipalAmt = paymentAmount
					.subtract(bdRepaymentInterestAmt);
			bdPreviousBalance = bdPreviousBalance
					.subtract(dbRepaymentPrincipalAmt);
			loanAmortization = delegator.makeValue("LoanAmortization", UtilMisc
					.toMap("loanAmortizationId", loanAmortizationId,
							"paymentNo", new Long(iAmortizationCount)
									.longValue(), "loanApplicationId",
							loanApplicationId, "paymentAmount", paymentAmount
									.setScale(6, RoundingMode.HALF_UP),
							"interestAmount", bdRepaymentInterestAmt.setScale(
									6, RoundingMode.HALF_UP),
							"principalAmount", dbRepaymentPrincipalAmt
									.setScale(6, RoundingMode.HALF_UP),
							"balanceAmount", bdPreviousBalance.setScale(6,
									RoundingMode.HALF_UP)));
			listTobeStored.add(loanAmortization);
		}
		// Save the list
		try {
			delegator.storeAll(listTobeStored);
		} catch (GenericEntityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
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
				bdOnePlusInterestPowerPeriodMinusOne, RoundingMode.HALF_UP);

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

	/***
	 * Delete the existing Armotization schedule for the regenetaion to happen
	 * */
	private static void deleteExistingSchedule(Delegator delegator,
			String loanApplicationId) {
		// Get the loan armotization entities for the loan application and
		// delete them
		List<GenericValue> loanAmortizationELI = null; // =

		try {
			loanAmortizationELI = delegator.findList("LoanAmortization",
					EntityCondition.makeCondition("loanApplicationId",
							loanApplicationId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		List<GenericValue> toDeleteList = new LinkedList<GenericValue>();

		for (GenericValue genericValue : loanAmortizationELI) {
			toDeleteList.add(genericValue);
		}

		try {
			delegator.removeAll(toDeleteList);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
