package org.ofbiz.accountholdertransactions;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
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
		GenericValue userLogin = (GenericValue)request.getAttribute("userLogin");

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
			postLoanExpectation(loanExpectation, delegator, userLogin);
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
			Delegator delegator, GenericValue userLogin) {
		
		String repaymentName = loanExpectation.getString("repaymentName");
		
		if (repaymentName.equals("INTEREST")){
			//post interest
			//LoanAccounting.postDisbursement(loanApplication, userLogin)
			postInterestAccrued(loanExpectation, delegator, userLogin);
		} else if (repaymentName.equals("INSURANCE")){
			//post insurance
			postInsuranceCharge(loanExpectation, delegator, userLogin);
		}else if (repaymentName.equals("PRINCIPAL")){
			//post principal
			postPrincipalDue(loanExpectation, delegator, userLogin);
		}
		
	}

	private static void postPrincipalDue(GenericValue loanExpectation,
			Delegator delegator, GenericValue userLogin) {
		// TODO Auto-generated method stub
		
	}

	private static void postInsuranceCharge(GenericValue loanExpectation,
			Delegator delegator, GenericValue userLogin) {
		// TODO Auto-generated method stub
		
	}

	private static void postInterestAccrued(GenericValue loanExpectation,
			Delegator delegator, GenericValue userLogin) {
		String acctgTransType = "INTEREST_RECEIVABLE";
//		String glAcctTypeIdMemberDepo = "CURRENT_LIABILITY";
//		String glAcctTypeIdLoans = "CURRENT_ASSET";
//		String glAcctTypeIdcharges = "OTHER_INCOME";
//		String acctgTransId = createAccountingTransaction(loanExpectation,
//				acctgTransType, userLogin, delegator);
		
		//log.info("### Transaction ID## "+acctgTransId);
		// Creates a record in AcctgTransEntry for Member Deposit Account
//		createMemberDepositEntry(loanExpectation, acctgTransId, userLogin,
//				glAcctTypeIdMemberDepo, delegator); 
	}

	/***
	 * Create a LoanExpectation and Update LoanAmortization to isAccrued = Y and Set
	 *  dateAccrued/Charged to today/currentDate
	 * 
	 * */
	private static void createLoanExpectation(GenericValue loanAmortization,
			Delegator delegator) {

		GenericValue loanExpectation = null;
		String loanExpectationId = delegator.getNextSeqId("LoanExpectation", 1L);
		String loanApplicationId = loanAmortization.getString("loanApplicationId");
		String employeeNo = getEmployeeNumber(loanApplicationId, delegator);
		
		List<GenericValue> listTobeStored = new LinkedList<GenericValue>();
		
		String loanNo = getLoanNo(loanApplicationId, delegator);
		
		//Adding PRINCIPAL
		BigDecimal bdPrincipalAccrued = loanAmortization.getBigDecimal("principalAmount");
		
		//INTEREST
		BigDecimal bdInterestAccrued = loanAmortization.getBigDecimal("interestAmount");
		
		//INSURANCE
		BigDecimal bdInsuranceAccrued = loanAmortization.getBigDecimal("insuranceAmount");
		
		//Adding Principal
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId,
						"loanNo", loanNo,
						"loanApplicationId",loanApplicationId,
						"employeeNo",employeeNo,
						"repaymentName","PRINCIPAL",
						"dateAccrued",new Timestamp(Calendar.getInstance().getTimeInMillis()),
						"isPaid","N",
						"isPosted","N",
						
						"amountDue",bdPrincipalAccrued,
						"amountAccrued",bdPrincipalAccrued
				));
		
		listTobeStored.add(loanExpectation);
		
		//Add Interest
		loanExpectationId = delegator.getNextSeqId("LoanExpectation", 1L);
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId,
						"loanNo", loanNo,
						"loanApplicationId",loanApplicationId,
						"employeeNo",employeeNo,
						"repaymentName","INTEREST",
						"dateAccrued",new Timestamp(Calendar.getInstance().getTimeInMillis()),
						"isPaid","N",
						"isPosted","N",
						"amountDue",bdInterestAccrued,
						"amountAccrued",bdInterestAccrued
				));
		listTobeStored.add(loanExpectation);
		
		loanExpectationId = delegator.getNextSeqId("LoanExpectation", 1L);
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId,
						"loanNo", loanNo,
						"loanApplicationId",loanApplicationId,
						"employeeNo",employeeNo,
						"repaymentName","INSURANCE",
						"dateAccrued",new Timestamp(Calendar.getInstance().getTimeInMillis()),
						"isPaid","N",
						"isPosted","N",
						"amountDue",bdInsuranceAccrued,
						"amountAccrued",bdInsuranceAccrued
				));
		listTobeStored.add(loanExpectation);
		
		//Update Amortization
		loanAmortization.set("isAccrued", "N");
		loanAmortization.set("dateAccrued", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		
		
		
		try {
			TransactionUtil.begin();
		} catch (GenericTransactionException e1) {
			e1.printStackTrace();
		}
		try {
			delegator.storeAll(listTobeStored);
			delegator.createOrStore(loanAmortization);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		try {
			TransactionUtil.commit();
		} catch (GenericTransactionException e) {
			e.printStackTrace();
		}
		
		
		
	}

	private static String getEmployeeNumber(String loanApplicationId,
			Delegator delegator) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		GenericValue member = null;
		try {
			loanApplication = delegator.findOne(
					"LoanApplication", UtilMisc.toMap(
							"loanApplicationId",
							loanApplicationId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Loan  ");
		}

		String partyId = "";
		if (loanApplication != null) {
			partyId = loanApplication.getString("partyId");
		} else {
			log.error("######## cannot get Loan Application ");
		}
		
		//Get Member given partyId
		try {
			member = delegator.findOne(
					"Member", UtilMisc.toMap(
							"partyId",
							partyId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Member  ");
		}

		String memberNumber = "";
		if (member != null) {
			memberNumber = member.getString("memberNumber");
		} else {
			log.error("######## cannot get Member ");
		}
		return memberNumber;
	}

	/***
	 * Get Loan Number given Loan Application ID
	 * */
	private static String getLoanNo(String loanApplicationId, Delegator delegator) {
		// TODO Auto-generated method stub
		GenericValue loanApplication = null;
		try {
			loanApplication = delegator.findOne(
					"LoanApplication", UtilMisc.toMap(
							"loanApplicationId",
							loanApplicationId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.error("######## Cannot get Loan  ");
		}

		String loanNo = "";
		if (loanApplication != null) {
			loanNo = loanApplication.getString("loanNo");
		} else {
			log.error("######## cannot get Loan Application ");
		}
		return loanNo;
	}
	
	private static String createAccountingTransaction(
			GenericValue loanExpectation, String acctgTransType,
			GenericValue userLogin, Delegator delegator) {

		GenericValue acctgTrans;
		String acctgTransId;
		//Delegator delegator = loanApplication.getDelegator();
		acctgTransId = delegator.getNextSeqId("AcctgTrans");

		String partyId = (String) userLogin.get("partyId");
		String createdBy = (String) userLogin.get("userLoginId");

		Timestamp currentDateTime = new Timestamp(Calendar.getInstance()
				.getTimeInMillis());
		acctgTrans = delegator.makeValidValue("AcctgTrans", UtilMisc.toMap(
				"acctgTransId", acctgTransId,
				"acctgTransTypeId", acctgTransType,
				"transactionDate",
				currentDateTime, "isPosted", "Y", "postedDate",
				currentDateTime, "glFiscalTypeId", "ACTUAL", "partyId",
				partyId, "createdByUserLogin", createdBy, "createdDate",
				currentDateTime, "lastModifiedDate", currentDateTime,
				"lastModifiedByUserLogin", createdBy));
		try {
			delegator.createOrStore(acctgTrans);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return acctgTransId;
	}
	
//	private static void createMemberDepositEntry(GenericValue loanApplication,
//			String acctgTransId, Map<String, String> userLogin,
//			String acctgTransType) {
//		Delegator delegator = loanApplication.getDelegator();
//		// Credit Member Deposit Account
//		String memberDepositAccount = getMemberDepositAccount(delegator);
//		String partyId = (String) userLogin.get("partyId");
//		String postingType = "C";
//		String entrySequenceId = "00001";
//		BigDecimal bdLoanAmount = loanApplication.getBigDecimal("loanAmt");
//		postTransactionEntry(delegator, bdLoanAmount, partyId,
//				memberDepositAccount, postingType, acctgTransId,
//				acctgTransType, entrySequenceId);
//	}


}
