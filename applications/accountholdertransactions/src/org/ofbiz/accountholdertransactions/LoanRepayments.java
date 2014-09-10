package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.webapp.event.EventHandlerException;

/***
 * @author Japheth Odonya @when Sep 10, 2014 7:14:36 PM
 * 
 *         Loan Repayments Processing
 * **/
public class LoanRepayments {
	public static Logger log = Logger.getLogger(LoanRepayments.class);

	public static String generateLoansRepaymentExpected(
			HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");

		List<GenericValue> loanAmortizationELI = null;

		Timestamp currentDate = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());

		EntityConditionList<EntityExpr> loanRepaymentConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isAccrued", EntityOperator.EQUALS, "N"),
						EntityCondition.makeCondition("expectedPaymentDate",
								EntityOperator.LESS_THAN_EQUAL_TO, currentDate)

				), EntityOperator.AND);

		try {
			loanAmortizationELI = delegator.findList("LoanAmortization",
					loanRepaymentConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ######### Looking for Amortizations that are due #########");
		if (loanAmortizationELI == null) {
			log.info(" ######### No Amortizations Due #########");
		} else {
			log.info(" ######### Total Number of Amortizations Due is   #########"
					+ loanAmortizationELI.size());
		}

		String acctgTransType = "MEMBER_DEPOSIT";
		// int count = 0;
		// for (GenericValue accountTransaction : accountTransactionELI) {
		// log.info("CCCCCC  Counting "+count);
		// try {
		// TransactionUtil.begin();
		// } catch (GenericTransactionException e) {
		// e.printStackTrace();
		// }
		// postChequeDeposit(accountTransaction, delegator, acctgTransType);
		// log.info("#####PPPPPPPPPPPPPP Posted ####  "+accountTransaction.getBigDecimal("transactionAmount"));
		// // Update Account Transaction to read Posted and when it was Posted
		// updateAccountTransaction(accountTransaction, delegator);
		// try {
		// TransactionUtil.commit();
		// } catch (GenericTransactionException e) {
		// e.printStackTrace();
		// }
		// }
		
		//for each amortization create an expection (LoanExpectation)
		for (GenericValue loanAmortization : loanAmortizationELI) {
			
			//Remember to Update Amortization as isAccrued and with dateAccrued
			createLoanExpectation(loanAmortization, delegator);
		}
		
		//Get Expectations
		List<GenericValue> loanExpectationELI = null;
		EntityConditionList<EntityExpr> loanExpectationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"isPosted", EntityOperator.EQUALS, "N")

				), EntityOperator.AND);

		try {
			loanExpectationELI = delegator.findList("LoanExpectation",
					loanExpectationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ######### Looking for Loan Expectations not yet posted #########");
		if (loanExpectationELI == null) {
			log.info(" ######### No Expectations Not Yet Posted #########");
		} else {
			log.info(" ######### Total Number of LoanExpectations Not Yet Posted   #########"
					+ loanExpectationELI.size());
		}
		
		for (GenericValue loanExpectation : loanExpectationELI) {
			//Remember to update LoanExpectation as Posted
			postLoanExpectation(loanExpectation, delegator);
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

	/***
	 * @author Japheth Odonya  @when Sep 10, 2014 8:58:22 PM
	 * 
	 * Post the items in LoanExpectation
	 * Principal
	 * Interest
	 * Insurance
	 * 
	 * Set LoanExpectationToPosted
	 * */
	private static void postLoanExpectation(GenericValue loanExpectation,
			Delegator delegator) {
		// TODO Auto-generated method stub
		
	}

	/***
	 * Create a LoanExpectation and Update LoanAmortization to isAccrued = Y and Set
	 *  dateAccrued/Charged to today/currentDate
	 * 
	 * */
	private static void createLoanExpectation(GenericValue loanAmortization,
			Delegator delegator) {
		// TODO Auto-generated method stub
		
	}

}
