package org.ofbiz.memberaccountmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.accountholdertransactions.LoanRepayments;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class MemberAccountManagementServices {
	
	private static Logger log = Logger
			.getLogger(MemberAccountManagementServices.class);

	
	public static String getMemberAccounts(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		partyId = partyId.replaceAll(",", "");
		List<GenericValue> memberAccountELI = null;
		EntityConditionList<EntityExpr> memberAccountConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId))
						),
						EntityOperator.AND);
		try {
			memberAccountELI = delegator.findList("MemberAccount", memberAccountConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (memberAccountELI == null){
			result.put("", "No Accounts");
		}
		
		GenericValue accountProduct;
		for (GenericValue genericValue : memberAccountELI) {
			accountProduct = LoanUtilities.getAccountProduct(genericValue.getLong("accountProductId"));
			result.put(genericValue.get("memberAccountId").toString(), genericValue.get("accountNo")+" - "+accountProduct.getString("name")+" - "+genericValue.getString("accountName"));
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
	
	/****
	 * getMemberLoans
	 * 
	 * */
	public static String getMemberLoans(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		partyId = partyId.replaceAll(",", "");
		
		List<GenericValue> loanApplicationELI = null;
		Long loanDisbursedStatusId = getLoanStatusId("DISBURSED");
		EntityConditionList<EntityExpr> loanApplicationConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"loanStatusId", EntityOperator.EQUALS,
								loanDisbursedStatusId),
								EntityCondition
										.makeCondition("partyId",
												EntityOperator.EQUALS,
												Long.valueOf(partyId))), EntityOperator.AND);
		try {
			loanApplicationELI = delegator.findList("LoanApplication",
					loanApplicationConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (loanApplicationELI == null){
			result.put("", "No Loans");
		}
		
		GenericValue loanProduct;
		for (GenericValue genericValue : loanApplicationELI) {
			//accountProduct = LoanUtilities.getAccountProduct(genericValue.getLong("accountProductId"));
			Long loanApplicationId = genericValue.getLong("loanApplicationId");
			//loanProduct = LoanUtilities.getLoanApplicationEntity(loanApplicationId);
			loanProduct = LoanUtilities.getLoanProduct(genericValue.getLong("loanProductId"));
			BigDecimal bdLoanAmt = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
			result.put(genericValue.get("loanApplicationId").toString(), loanProduct.get("name")+" - "+loanProduct.getString("code")+" - Loan Amt : "+genericValue.getBigDecimal("loanAmt")+" Balance : "+bdLoanAmt);
		}
		
		//Add Defaulted loans DEFAULTED
		List<GenericValue> loanApplicationClearedELI = null;
		Long loanClearedStatusId = getLoanStatusId("CLEARED");
		EntityConditionList<EntityExpr> loanApplicationClearedConditions = EntityCondition
				.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition(
								"loanStatusId", EntityOperator.EQUALS,
								loanClearedStatusId),
								EntityCondition
										.makeCondition("partyId",
												EntityOperator.EQUALS,
												Long.valueOf(partyId))), EntityOperator.AND);
		try {
			loanApplicationClearedELI = delegator.findList("LoanApplication",
					loanApplicationClearedConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : loanApplicationClearedELI) {
			//accountProduct = LoanUtilities.getAccountProduct(genericValue.getLong("accountProductId"));
			Long loanApplicationId = genericValue.getLong("loanApplicationId");
			//loanProduct = LoanUtilities.getLoanApplicationEntity(loanApplicationId);
			loanProduct = LoanUtilities.getLoanProduct(genericValue.getLong("loanProductId"));
			BigDecimal bdLoanAmt = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
			result.put(genericValue.get("loanApplicationId").toString(), loanProduct.get("name")+" - "+loanProduct.getString("code")+" - Loan Amt : "+genericValue.getBigDecimal("loanAmt")+" Balance : "+bdLoanAmt);
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
	
	/***
	 * Get Loan Status
	 * */

	public static Long getLoanStatusId(String name) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long loanStatusId = 0L;
		for (GenericValue genericValue : loanStatusELI) {
			loanStatusId = genericValue.getLong("loanStatusId");
		}

		String statusIdString = String.valueOf(loanStatusId);
		statusIdString = statusIdString.replaceAll(",", "");
		loanStatusId = Long.valueOf(statusIdString);
		return loanStatusId;
	}
	
	
	//Member Account Voucher 
	/***
	 * Add a decrease for sourceMemberAccountId
	 * 
	 * Add an increase for destMemberAccountId
	 * 
	 * Amount is amount
	 * 
	 * */
	public static String createMemberAccountVoucherTransaction(Map<String, String> userLogin, Long memberAccountVoucherId){
		//Find MemberAccountVoucher
		GenericValue memberAccountVoucher = getMemberAccountVoucher(memberAccountVoucherId);
		//Decrease the source
		log.info("//////////////////////// Posting the voucher");
		BigDecimal amount = memberAccountVoucher.getBigDecimal("amount");
		Long memberAccountId = memberAccountVoucher.getLong("sourceMemberAccountId");
		String transactionType = "MEMBERACCOUNTJVDEC";
		
		//AccHolderTransactionServices.cashDeposit(amount, memberAccountId, userLogin, transactionType);
		
		AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, memberAccountVoucherId);
		
		memberAccountId = memberAccountVoucher.getLong("destMemberAccountId");
		transactionType = "MEMBERACCOUNTJVINC";

		AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, memberAccountVoucherId);

		log.info("//////////////////////// Posted the voucher");
		
		
		return "success";
	}
	
	
	private static GenericValue getMemberAccountVoucher(Long memberAccountVoucherId) {
		GenericValue memberAccountVoucher = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberAccountVoucher = delegator.findOne("MemberAccountVoucher",
					UtilMisc.toMap("memberAccountVoucherId", memberAccountVoucherId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberAccountVoucher;
	}
	
	
	private static GenericValue getGeneralMemberVoucher(Long generalMemberVoucherId) {
		GenericValue generalMemberVoucher = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			generalMemberVoucher = delegator.findOne("GeneralMemberVoucher",
					UtilMisc.toMap("generalMemberVoucherId", generalMemberVoucherId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return generalMemberVoucher;
	}
	
	
	private static GenericValue getMemberLoansVoucher(Long memberLoansVoucherId) {
		GenericValue memberLoansVoucher = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberLoansVoucher = delegator.findOne("MemberLoansVoucher",
					UtilMisc.toMap("memberLoansVoucherId", memberLoansVoucherId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return memberLoansVoucher;
	}
	
	/***
	 * 
	 **/
	public static String createMemberLoansVoucherTransaction(Map<String, String> userLogin, Long memberLoansVoucherId){
		
		//Find MemberLoansVoucher
		GenericValue memberLoansVoucher = getMemberLoansVoucher(memberLoansVoucherId);
		BigDecimal principalAmount = memberLoansVoucher.getBigDecimal("principalAmount");
		BigDecimal interestAmount = memberLoansVoucher.getBigDecimal("interestAmount");
		BigDecimal insuranceAmount = memberLoansVoucher.getBigDecimal("insuranceAmount");
		BigDecimal amount = memberLoansVoucher.getBigDecimal("amount");
		
		log.info(" principalAmount "+memberLoansVoucher.getBigDecimal("principalAmount"));
		log.info(" interestAmount "+memberLoansVoucher.getBigDecimal("interestAmount"));
		log.info(" insuranceAmount "+memberLoansVoucher.getBigDecimal("insuranceAmount"));
		log.info(" amount "+memberLoansVoucher.getBigDecimal("amount"));
		Long sourceLoanApplicationId = memberLoansVoucher.getLong("sourceLoanApplicationId");
		log.info(" sourceLoanApplicationId "+sourceLoanApplicationId);

		Long destLoanApplicationId = memberLoansVoucher.getLong("destLoanApplicationId");
		log.info(" destLoanApplicationId "+destLoanApplicationId);
		
		//Add a negative transaction to source loan application
		reduceLoanRepaymentInSource(sourceLoanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		
		//Add a positive transaction to destination loan application
		addLoanRepaymentInDestination(destLoanApplicationId, principalAmount, interestAmount, insuranceAmount, amount,  userLogin);
		//LoanRepayments.repayLoanWithoutDebitingCash(loanRepayment, userLogin, entrySequence)

		return "success";
	}
	
	
	private static void addLoanRepaymentInDestination(
			Long destLoanApplicationId, BigDecimal principalAmount,
			BigDecimal interestAmount, BigDecimal insuranceAmount,
			BigDecimal amount, Map<String, String> userLogin) {
		GenericValue loanApplication = LoanUtilities.getLoanApplicationEntity(destLoanApplicationId);
		GenericValue loanRepayment = null; 
		//String partyId = (String) userLogin.get("partyId");
		String userLoginId = (String) userLogin.get("userLoginId");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		Long loanApplicationId = destLoanApplicationId;
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		//LoanRepayments.getT
		BigDecimal totalLoanDue = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
		BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
		BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
		
		BigDecimal loanInterest = interestAmount;
		BigDecimal loanInsurance = insuranceAmount;
		BigDecimal loanPrincipal = principalAmount;
		BigDecimal transactionAmount = amount;
				
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLoginId, "partyId", loanApplication.getLong("partyId"), "loanApplicationId",
				loanApplication.getLong("loanApplicationId"),

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

	}

	private static void reduceLoanRepaymentInSource(
			Long sourceLoanApplicationId, BigDecimal principalAmount,
			BigDecimal interestAmount, BigDecimal insuranceAmount,
			BigDecimal amount, Map<String, String> userLogin) {
		GenericValue loanApplication = LoanUtilities.getLoanApplicationEntity(sourceLoanApplicationId);
		GenericValue loanRepayment = null; 
		//String partyId = (String) userLogin.get("partyId");
		String userLoginId = (String) userLogin.get("userLoginId");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanRepaymentId = delegator.getNextSeqIdLong("LoanRepayment", 1);
		Long loanApplicationId = sourceLoanApplicationId;
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		//LoanRepayments.getT
		BigDecimal totalLoanDue = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		BigDecimal totalInterestDue = LoanRepayments.getTotalInterestByLoanDue(loanApplicationId.toString());
		BigDecimal totalInsuranceDue = LoanRepayments.getTotalInsurancByLoanDue(loanApplicationId.toString());
		BigDecimal totalPrincipalDue = LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationId.toString());
		
		if (interestAmount == null)
			interestAmount = BigDecimal.ZERO;
		
		if (insuranceAmount == null)
			insuranceAmount = BigDecimal.ZERO;
		
		if (insuranceAmount == null)
			insuranceAmount = BigDecimal.ZERO;

		if (principalAmount == null)
			principalAmount = BigDecimal.ZERO;
		
		BigDecimal loanInterest = interestAmount.multiply(new BigDecimal(-1));
		BigDecimal loanInsurance = insuranceAmount.multiply(new BigDecimal(-1));
		BigDecimal loanPrincipal = principalAmount.multiply(new BigDecimal(-1));
		BigDecimal transactionAmount = amount.multiply(new BigDecimal(-1));
				
		loanRepayment = delegator.makeValue("LoanRepayment", UtilMisc.toMap(
				"loanRepaymentId", loanRepaymentId, "isActive", "Y",
				"createdBy", userLoginId, "partyId", loanApplication.getLong("partyId"), "loanApplicationId",
				loanApplication.getLong("loanApplicationId"),

				"loanNo", loanApplication.getString("loanNo"),
				"loanAmt", bdLoanAmt,

				"totalLoanDue", totalLoanDue, "totalInterestDue",
				totalInterestDue, "totalInsuranceDue", totalInsuranceDue,
				"totalPrincipalDue", totalPrincipalDue, "interestAmount",
				loanInterest, "insuranceAmount", loanInsurance,
				"principalAmount", loanPrincipal, "transactionAmount",
				transactionAmount));
		try {
			delegator.createOrStore(loanRepayment);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		
	}

	public static String getTotalRepaid(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");
		log.info(" ######### The Loan Application ID is #########" + loanApplicationId);
		loanApplicationId = loanApplicationId.replaceAll(",", "");

		BigDecimal bdTotalRepaid = LoansProcessingServices.getLoansRepaidByLoanApplicationId(Long.valueOf(loanApplicationId));
		

		
		result.put("amountInSourceRepaid", bdTotalRepaid);
		result.put("amountInDestinationRepaid", bdTotalRepaid);
		
		log.info(" LOOOOOOOOOOOOOOOOOOOOOOks like work is going on !!!! ");

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
	
	
	public static String createGeneralMemberVoucherTransaction(Map<String, String> userLogin, Long generalMemberVoucherId){
		//Find GeneralMemberVoucher
		GenericValue generalMemberVoucher = getGeneralMemberVoucher(generalMemberVoucherId);
		//Decrease the source
		log.info("//////////////////////// Posting the voucher");
		BigDecimal amount = generalMemberVoucher.getBigDecimal("amount");
		Long memberAccountId = generalMemberVoucher.getLong("sourceMemberAccountId");
		String transactionType = "MEMBERACCOUNTJVDEC";
		
		AccHolderTransactionServices.cashDeposit(amount, memberAccountId, userLogin, transactionType);
		
		AccHolderTransactionServices.generalMemberVoucher(amount, memberAccountId, userLogin, transactionType, generalMemberVoucherId);
		memberAccountId = generalMemberVoucher.getLong("destMemberAccountId");
		transactionType = "MEMBERACCOUNTJVINC";

		AccHolderTransactionServices.generalMemberVoucher(amount, memberAccountId, userLogin, transactionType, generalMemberVoucherId);

		log.info("//////////////////////// Posted the voucher");
		
		
		return "success";
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 2:09:59 PM
	 * Add the lines
	 * **/
	public static String createGeneralglLines(GenericValue header, Map<String, String> userLogin){
		//Add Source MPA
		
		addSourceMPA(header, userLogin);
		
		//Add Destination MPA
		addDestinationMPA(header, userLogin);
		
		//Add gl Line
		addglLine(header, userLogin);
		
		return "success";
		
	}

	private static void addglLine(GenericValue header,
			Map<String, String> userLogin) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long generalglLineId = null;
		
		Long generalglLinesCount = header.getLong("generalglLinesCount");
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		
		Long count = 0L;
		
		List<GenericValue> listGeneralglLines = new ArrayList<GenericValue>();
		GenericValue generalglLine = null;
		while (count < generalglLinesCount) {
			
			//Add a new line
			generalglLineId =	delegator.getNextSeqIdLong("GeneralglLine", 1);
			
			generalglLine = delegator.makeValue("GeneralglLine", UtilMisc.toMap(
					"generalglLineId", generalglLineId,
					"generalglHeaderId", generalglHeaderId,
					"isActive", "Y",
					"createdBy", userLogin.get("userLoginId")));
			
			listGeneralglLines.add(generalglLine);
			
			count++;
			
		}
				
		try {
			delegator.storeAll(listGeneralglLines);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
	}

	private static void addDestinationMPA(GenericValue header,
			Map<String, String> userLogin) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long destinationmpaLineId = null;
		
		Long destinationmpaLinesCount = header.getLong("destinationmpaLinesCount");
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		
		Long count = 0L;
		
		List<GenericValue> listDestinationLines = new ArrayList<GenericValue>();
		GenericValue destinationmpaLine = null;
		while (count < destinationmpaLinesCount) {
			
			//Add a new line
			destinationmpaLineId =	delegator.getNextSeqIdLong("DestinationmpaLine", 1);
			
			destinationmpaLine = delegator.makeValue("DestinationmpaLine", UtilMisc.toMap(
					"destinationmpaLineId", destinationmpaLineId,
					"generalglHeaderId", generalglHeaderId,
					"isActive", "Y",
					"createdBy", userLogin.get("userLoginId")));
			
			listDestinationLines.add(destinationmpaLine);
			
			count++;
			
		}
				
		try {
			delegator.storeAll(listDestinationLines);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
	}

	private static void addSourceMPA(GenericValue header,
			Map<String, String> userLogin) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long sourcempaLineId = null;
		
		Long sourcempaLinesCount = header.getLong("sourcempaLinesCount");
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		
		Long count = 0L;
		
		List<GenericValue> listSourceLines = new ArrayList<GenericValue>();
		
		while (count < sourcempaLinesCount) {
			
			//Add a new line
			sourcempaLineId =	delegator.getNextSeqIdLong("SourcempaLine", 1);
			GenericValue sourcempaLine = null;
			sourcempaLine = delegator.makeValue("SourcempaLine", UtilMisc.toMap(
					"sourcempaLineId", sourcempaLineId,
					"generalglHeaderId", generalglHeaderId,
					"isActive", "Y",
					"createdBy", userLogin.get("userLoginId")));
			
			listSourceLines.add(sourcempaLine);
			
			count++;
			
		}
				
		try {
			delegator.storeAll(listSourceLines);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		
	}
	
	/***
	 * 
	 * @author Japheth Odonya  @when Jul 5, 2015 8:03:22 PM
	 * 
	 * Adding Lines
	 * **/
	//addLines
	public static String addLines(Long generalglHeaderId, Map<String, String> userLogin){
		
		GenericValue header = LoanUtilities.getEntityValue("GeneralglHeader", "generalglHeaderId", generalglHeaderId);
		
		if (alreadyProcessed(generalglHeaderId))
			return "Already processed !";
		
		if (header == null)
			return "No header found !";
		
		if (header.getLong("sourcempaLinesCount") == null)
			return "Make sure source mpa lines have a value , ZERO if none";

		if (header.getLong("destinationmpaLinesCount") == null)
			return "Make sure destination mpa lines have a value , ZERO if none";

		if (header.getLong("generalglLinesCount") == null)
			return "Make sure gl lines have a value , ZERO if none";

		createGeneralglLines(header, userLogin);
		return "success";
	}
	
	
	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 10:58:28 PM
	 * 
	 * Processing MPA Journal
	 * */
	public static String processMPAJournal(Long generalglHeaderId, Map<String, String> userLogin){
		
		GenericValue header = LoanUtilities.getEntityValue("GeneralglHeader", "generalglHeaderId", generalglHeaderId);
		
		if (header == null)
			return "No header found !";
		
		if (alreadyProcessed(generalglHeaderId))
			return "Already processed !";
		
		//Source lines must have amounts
		if (sourceLinesMissAmounts(generalglHeaderId))
			return "All source lines must have amounts or be deleted";
		
		//Destination lines must have amounts
		if (destinationLinesMissAmounts(generalglHeaderId))
			return "All Destination lines must have amounts or be deleted";
		
		//gllines must have amounts
		if (glLinesMissAmounts(generalglHeaderId))
			return "All GL lines must have amounts or be deleted";

		BigDecimal bdControlAmount = header.getBigDecimal("controlAmount");
		
		BigDecimal bdSourceTotal = BigDecimal.ZERO;
		BigDecimal bdDestinationTotal = BigDecimal.ZERO;
		BigDecimal bdTotalDebit = BigDecimal.ZERO;
		BigDecimal bdTotalCredit = BigDecimal.ZERO;
		
		bdSourceTotal = getSourceLinesTotal(generalglHeaderId);
		bdDestinationTotal = getDestinationTotal(generalglHeaderId);
		
		bdTotalDebit = getTotalDebit(generalglHeaderId);
		bdTotalCredit = getTotalCredit(generalglHeaderId);
		
		//Check that controlAmount equals to total source if total source is not zero
		if ((bdSourceTotal.compareTo(BigDecimal.ZERO) != 0) && (bdSourceTotal.compareTo(bdControlAmount) != 0))
			return "If source mpa line is provided it must be equal to the Control / Header amount";
		
		//Check that controlAmount equals to total destination if total destination is not zero
		if ((bdDestinationTotal.compareTo(BigDecimal.ZERO) != 0) && (bdDestinationTotal.compareTo(bdControlAmount) != 0))
			return "If destination mpa line is provided it must be equal to the Control / Header amount";

		
		//Check that controlAmount equals total credit
		if (bdControlAmount.compareTo(bdTotalCredit) != 0)
			return "Total Credit must be equal to total Control Amount";
		
		//Check that controlAmount equals total debit
		if (bdControlAmount.compareTo(bdTotalDebit) != 0)
			return "Total Debit must be equal to total Control Amount";
		
		//Check that credit not zero
		if (bdTotalCredit.compareTo(BigDecimal.ZERO) == 0)
			return "Total Credit must be more than ZERO";
		
		//check that debit not zero
		if (bdTotalDebit.compareTo(BigDecimal.ZERO) == 0)
			return "Total Debit must be more than ZERO";
		
		//Check that total credit equals total debit
		if (bdTotalCredit.compareTo(bdTotalCredit) != 0)
			return "Total Debit must be equal to total Credit !";
		
		//post the gl lines and return the acctgTransId
		String acctgTransId = postJournalglLines(header, userLogin);
		
		//post the source lines 
		postSourceglLines(header, userLogin, acctgTransId);
		//post the destination lines
		postDestinationglLines(header, userLogin, acctgTransId);
		
		//Set header as processed processed
		
		header.set("processed", "Y");
		header.set("updatedBy", userLogin.get("userLoginId"));
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			delegator.createOrStore(header);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "success";
	}

	/***
	 * @author Japheth Odonya  @when Jul 6, 2015 12:08:05 AM
	 * Post Destination GL Lines
	 * */
	private static void postDestinationglLines(GenericValue header,
			Map<String, String> userLogin, String acctgTransId) {
		/****
		 * Destination options could be
		 * 
		 * <option key="ACCOUNT" description="Member ACCOUNT"/>
				<option key="PRINCIPAL" description="Loan PRINCIPAL"/>
				<option key="INTERESTCHARGE" description="Loan Interest Charged"/>
				<option key="INTERESTPAID" description="Loan Interest Paid"/>
				<option key="INSURANCECHARGE" description="Loan Insurance Charge"/>
				<option key="INSURANCEPAYMENT" description="Loan Insurance Payment"/>
		 * */
		//Get list destination gl lines and post
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		
		//Get list of source mpa lines and post
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("DestinationmpaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		//BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			//total = total.add(genericValue.getBigDecimal("amount"));
			addDestinationMPALine(genericValue, userLogin, acctgTransId);
		}
	}

	private static void addDestinationMPALine(GenericValue genericValue,
			Map<String, String> userLogin, String acctgTransId) {
		/****
		 * 
		 * /****
		 * 
		 * <option key="ACCOUNT" description="Member ACCOUNT"/>
				<option key="PRINCIPAL" description="Loan PRINCIPAL"/>
				<option key="INTERESTCHARGE" description="Loan Interest Charged"/>
				<option key="INTERESTPAID" description="Loan Interest Paid"/>
				<option key="INSURANCECHARGE" description="Loan Insurance Charge"/>
				<option key="INSURANCEPAYMENT" description="Loan Insurance Payment"/>
		 * */
		String destinationType = genericValue.getString("destinationType");
		Long loanApplicationId = null;
		BigDecimal principalAmount = null;
		BigDecimal interestAmount = null; 
		BigDecimal insuranceAmount = null;
		BigDecimal amount = genericValue.getBigDecimal("amount");
		Long partyId = genericValue.getLong("destPartyId");
		if (destinationType.equals("ACCOUNT")){
			//Get memberAccountId and remove the amount from the account
			//Get memberAccountId and remove the amount from the account
			//Decrease the source
			log.info("//////////////////////// Posting the voucher");
			Long memberAccountId = genericValue.getLong("destMemberAccountId");
			String transactionType = "MEMBERACCOUNTJVINC";
			//AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, genericValue.getLong("generalglHeaderId"));
			AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, genericValue.getLong("generalglHeaderId"), acctgTransId);

		} else if (destinationType.equals("PRINCIPAL")){
			//Get Loan ID and post the principal
			loanApplicationId = genericValue.getLong("destLoanApplicationId");
			principalAmount = amount;
			addLoanRepaymentInDestination(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		}
		else if (destinationType.equals("INTERESTCHARGE")){
			//Get Loan ID and add interest charge
			loanApplicationId = genericValue.getLong("destLoanApplicationId");
			addInterestCharged(partyId, loanApplicationId, amount, "DESTINATION", acctgTransId);
		}
		else if (destinationType.equals("INTERESTPAID")){
			//Get Loan ID and add interest paid
			loanApplicationId = genericValue.getLong("destLoanApplicationId");
			interestAmount = amount;
			addLoanRepaymentInDestination(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		}
		else if (destinationType.equals("INSURANCECHARGE")){
			//Get Loan ID and add insurance charge
			loanApplicationId = genericValue.getLong("destLoanApplicationId");
			addInsuranceCharged(partyId, loanApplicationId, amount, "DESTINATION", acctgTransId);
		}
		else if (destinationType.equals("INSURANCEPAYMENT")){
			//Get Loan ID and add insurance payment
			loanApplicationId = genericValue.getLong("destLoanApplicationId");
			insuranceAmount = amount;
			addLoanRepaymentInDestination(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		}
	}

	/***
	 * @author Japheth Odonya  @when Jul 6, 2015 10:02:57 AM
	 * 
	 * Add Insurance Charged
	 * */
	private static void addInsuranceCharged(Long partyId, Long loanApplicationId, BigDecimal bdInsuranceAccrued, String sourceorDestination, String acctgTransId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		bdInsuranceAccrued = bdInsuranceAccrued.setScale(4, RoundingMode.HALF_UP);
		
		//, String chargeorPay
		if (sourceorDestination.equals("SOURCE"))
			bdInsuranceAccrued = bdInsuranceAccrued.multiply(new BigDecimal(-1));
		
		GenericValue loanExpectation = null;
		
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		GenericValue member = LoanUtilities.getEntityValue("Member", "partyId", partyId);
		String employeeNo = member.getString("memberNumber");
				
		String employeeNames = member.getString("firstName") + " "
						+ member.getString("middleName") + " "
						+ member.getString("lastName");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();
		
		String monthPadded = String.valueOf(month);//paddString(2, String.valueOf(month));
		String monthYear = monthPadded+String.valueOf(year);
		// TODO Auto-generated method stub
		loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		bdInsuranceAccrued = bdInsuranceAccrued.setScale(4, RoundingMode.HALF_UP);
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId, "loanNo",
						loanApplication.getString("loanNo"), "loanApplicationId", loanApplicationId,
						"employeeNo", employeeNo, "repaymentName",
						"INSURANCE", "employeeNames", employeeNames,
						"dateAccrued", new Timestamp(Calendar.getInstance()
								.getTimeInMillis()), "isPaid", "N",
						"isPosted", "N", "amountDue", bdInsuranceAccrued,
						"amountAccrued", bdInsuranceAccrued,
						
						"month", monthYear,
						"acctgTransId", acctgTransId,
						"partyId",
						partyId, "loanAmt", bdLoanAmt));
		
		try {
			delegator.createOrStore(loanExpectation);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * @author Japheth Odonya  @when Jul 6, 2015 10:02:14 AM
	 * Add Interest Charged
	 * */
	private static void addInterestCharged(Long partyId, Long loanApplicationId, BigDecimal bdInterestAccrued, String sourceorDestination, String acctgTransId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanExpectationId = delegator.getNextSeqIdLong("LoanExpectation",
				1L);
		bdInterestAccrued = bdInterestAccrued.setScale(4, RoundingMode.HALF_UP);
		
		if (sourceorDestination.equals("SOURCE"))
			bdInterestAccrued = bdInterestAccrued.multiply(new BigDecimal(-1));
		
		GenericValue loanExpectation = null;
		
		GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
		GenericValue member = LoanUtilities.getEntityValue("Member", "partyId", partyId);
		String employeeNo = member.getString("memberNumber");
				
		String employeeNames = member.getString("firstName") + " "
						+ member.getString("middleName") + " "
						+ member.getString("lastName");
		BigDecimal bdLoanAmt = loanApplication.getBigDecimal("loanAmt");
		
		LocalDate localDate = new LocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthOfYear();
		
		String monthPadded = String.valueOf(month);//paddString(2, String.valueOf(month));
		String monthYear = monthPadded+String.valueOf(year);
		
		loanExpectation = delegator.makeValue("LoanExpectation", UtilMisc
				.toMap("loanExpectationId", loanExpectationId, "loanNo",
						loanApplication.getString("loanNo"), "loanApplicationId", loanApplicationId,
						"employeeNo", employeeNo, "repaymentName",
						"INTEREST", "employeeNames", employeeNames,
						"dateAccrued", new Timestamp(Calendar.getInstance()
								.getTimeInMillis()), "isPaid", "N",
						"isPosted", "N", "amountDue", bdInterestAccrued,
						"amountAccrued", bdInterestAccrued,
						"month", monthYear,
						"acctgTransId", acctgTransId,
						"partyId",
						partyId, "loanAmt", bdLoanAmt));
		
		try {
			delegator.createOrStore(loanExpectation);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * @author Japheth Odonya  @when Jul 6, 2015 12:08:27 AM
	 * POst Source GL Lines
	 * */
	private static void postSourceglLines(GenericValue header,
			Map<String, String> userLogin, String acctgTransId) {
		/***
		 * Source Type could be
		 * 
		 * <option key="ACCOUNT" description="Member ACCOUNT"/>
				<option key="PRINCIPAL" description="Loan PRINCIPAL"/>
				<option key="INTERESTCHARGE" description="Loan Interest Charged"/>
				<option key="INTERESTPAID" description="Loan Interest Paid"/>
				<option key="INSURANCECHARGE" description="Loan Insurance Charge"/>
				<option key="INSURANCEPAYMENT" description="Loan Insurance Payment"/>
		 * */
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		
		//Get list of source mpa lines and post
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("SourcempaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		//BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			//total = total.add(genericValue.getBigDecimal("amount"));
			addSourceMPALine(genericValue, userLogin, acctgTransId);
		}
		
	}

	private static void addSourceMPALine(GenericValue genericValue,
			Map<String, String> userLogin, String acctgTransId) {
		/****
		 * 
		 * <option key="ACCOUNT" description="Member ACCOUNT"/>
				<option key="PRINCIPAL" description="Loan PRINCIPAL"/>
				<option key="INTERESTCHARGE" description="Loan Interest Charged"/>
				<option key="INTERESTPAID" description="Loan Interest Paid"/>
				<option key="INSURANCECHARGE" description="Loan Insurance Charge"/>
				<option key="INSURANCEPAYMENT" description="Loan Insurance Payment"/>
		 * */
		String sourceType = genericValue.getString("sourceType");
		Long loanApplicationId = null;
		BigDecimal principalAmount = null;
		BigDecimal interestAmount = null; 
		BigDecimal insuranceAmount = null;
		BigDecimal amount = genericValue.getBigDecimal("amount");
		Long partyId = genericValue.getLong("sourcePartyId");
		if (sourceType.equals("ACCOUNT")){
			//Get memberAccountId and remove the amount from the account
			//Decrease the source
			log.info("//////////////////////// Posting the voucher");
			Long memberAccountId = genericValue.getLong("sourceMemberAccountId");
			String transactionType = "MEMBERACCOUNTJVDEC";
			//AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, genericValue.getLong("generalglHeaderId"));
			AccHolderTransactionServices.memberAccountJournalVoucher(amount, memberAccountId, userLogin, transactionType, genericValue.getLong("generalglHeaderId"), acctgTransId);
		} else if (sourceType.equals("PRINCIPAL")){
			//Get Loan ID and post the principal
			loanApplicationId = genericValue.getLong("sourceLoanApplicationId");
			principalAmount = genericValue.getBigDecimal("amount");
			reduceLoanRepaymentInSource(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
			
		}
		else if (sourceType.equals("INTERESTCHARGE")){
			//Get Loan ID and add interest charge		
			loanApplicationId = genericValue.getLong("sourceLoanApplicationId");
			addInterestCharged(partyId, loanApplicationId, amount, "SOURCE", acctgTransId);
		}
		else if (sourceType.equals("INTERESTPAID")){
			//Get Loan ID and add interest paid
			loanApplicationId = genericValue.getLong("sourceLoanApplicationId");
			interestAmount = genericValue.getBigDecimal("amount");
			reduceLoanRepaymentInSource(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		}
		else if (sourceType.equals("INSURANCECHARGE")){
			//Get Loan ID and add insurance charge
			loanApplicationId = genericValue.getLong("sourceLoanApplicationId");
			addInsuranceCharged(partyId, loanApplicationId, amount, "SOURCE", acctgTransId);
		}
		else if (sourceType.equals("INSURANCEPAYMENT")){
			//Get Loan ID and add insurance payment
			loanApplicationId = genericValue.getLong("sourceLoanApplicationId");
			insuranceAmount = genericValue.getBigDecimal("amount");
			reduceLoanRepaymentInSource(loanApplicationId, principalAmount, interestAmount, insuranceAmount, amount, userLogin);
		}
		
	}

	/***
	 * @author Japheth Odonya  @when Jul 6, 2015 12:09:00 AM
	 * Post Journal GL Lines
	 * **/
	private static String postJournalglLines(GenericValue header,
			Map<String, String> userLogin) {
		String acctgTransId = null;
		//Get the lines
		//post each line
		Long generalglHeaderId = header.getLong("generalglHeaderId");
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId)

				

				), EntityOperator.AND);
		
		//EntityCondition.makeCondition(
		//"debitCredit", EntityOperator.EQUALS,
		//"DEBIT"),

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("GeneralglLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		String acctgTransType = "MEMBER_DEPOSIT";
		acctgTransId = AccHolderTransactionServices.createAccountingTransaction(null, acctgTransType, userLogin);
		String postingType = "";
		String employeeBranchId = AccHolderTransactionServices.getEmployeeBranch(userLogin.get("partyId"));
		String memberBranchId = AccHolderTransactionServices.getEmployeeBranch(userLogin.get("partyId"));
		String glAccountId = null;
		//BigDecimal total = BigDecimal.ZERO;
		Long entrySequenceId = 0L;
		for (GenericValue genericValue : linesELI) {
			//total = total.add(genericValue.getBigDecimal("amount"));
			//POST the gl line
			if (genericValue.getString("debitCredit").equals("DEBIT"))
			{
				postingType = "D";
			} else if (genericValue.getString("debitCredit").equals("CREDIT")){
				postingType = "C";
			}
			BigDecimal bdAmount = BigDecimal.ZERO;
			
			if (genericValue.getBigDecimal("amount") != null)
				bdAmount = genericValue.getBigDecimal("amount");
			
			glAccountId = genericValue.getString("glAccountId");
			entrySequenceId = entrySequenceId + 1;
			AccHolderTransactionServices.postTransactionEntry(delegator, bdAmount, employeeBranchId, memberBranchId, glAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId.toString());
		}
		
		return acctgTransId;
	}

	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 11:45:29 PM
	 * Total Credit
	 * */
	private static BigDecimal getTotalCredit(Long generalglHeaderId) {
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId),
						
						EntityCondition.makeCondition(
								"debitCredit", EntityOperator.EQUALS,
								"CREDIT")

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("GeneralglLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			
			if (genericValue.getBigDecimal("amount") != null)
				total = total.add(genericValue.getBigDecimal("amount"));
		}
		return total;
	}

	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 11:45:46 PM
	 * Total Debit
	 * */
	private static BigDecimal getTotalDebit(Long generalglHeaderId) {
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId),
						
						EntityCondition.makeCondition(
								"debitCredit", EntityOperator.EQUALS,
								"DEBIT")
						

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("GeneralglLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			if (genericValue.getBigDecimal("amount") != null)
				total = total.add(genericValue.getBigDecimal("amount"));
		}
		return total;
	}

	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 11:46:06 PM
	 * Destination total
	 * */
	private static BigDecimal getDestinationTotal(Long generalglHeaderId) {
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId)


				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("DestinationmpaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			if (genericValue.getBigDecimal("amount") != null)
			total = total.add(genericValue.getBigDecimal("amount"));
		}
		return total;
	}

	/***
	 * @author Japheth Odonya  @when Jul 5, 2015 11:46:31 PM
	 * 
	 * Source Total
	 * */
	private static BigDecimal getSourceLinesTotal(Long generalglHeaderId) {
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("SourcempaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal total = BigDecimal.ZERO;
		for (GenericValue genericValue : linesELI) {
			if (genericValue.getBigDecimal("amount") != null)
				total = total.add(genericValue.getBigDecimal("amount"));
		}
		return total;
	}

	private static boolean glLinesMissAmounts(Long generalglHeaderId) {
		//Check if there is a glLine with amount not set
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId),

				EntityCondition.makeCondition("amount", EntityOperator.EQUALS,
						null)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("GeneralglLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((linesELI != null) && (linesELI.size() > 0))
			return true;
		return false;
	}

	private static boolean destinationLinesMissAmounts(Long generalglHeaderId) {
		//Check if there is a destinationLIne with amount not set
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId),

				EntityCondition.makeCondition("amount", EntityOperator.EQUALS,
						null)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("DestinationmpaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((linesELI != null) && (linesELI.size() > 0))
			return true;
		
		return false;
	}

	private static boolean sourceLinesMissAmounts(Long generalglHeaderId) {
		//Check if there is a sourceLine with amount not set
		EntityConditionList<EntityExpr> linesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"generalglHeaderId", EntityOperator.EQUALS,
						generalglHeaderId),

				EntityCondition.makeCondition("amount", EntityOperator.EQUALS,
						null)

				), EntityOperator.AND);

		List<GenericValue> linesELI = new ArrayList<GenericValue>();
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			linesELI = delegator.findList("SourcempaLine",
					linesConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((linesELI != null) && (linesELI.size() > 0))
			return true;
		
		return false;
	}

	private static boolean alreadyProcessed(Long generalglHeaderId) {
		// TODO Auto-generated method stub
		//processed
		GenericValue header = LoanUtilities.getEntityValue("GeneralglHeader", "generalglHeaderId", generalglHeaderId);
		
		if ((header.getString("processed") != null) && (header.getString("processed").equals("Y")))
			return true;
		
		return false;
	}
	
	public static String checkNotProcessed(Long generalglHeaderId, Map<String, String> userLogin){
		GenericValue header = LoanUtilities.getEntityValue("GeneralglHeader", "generalglHeaderId", generalglHeaderId);
		
		if ((header.getString("processed") != null) && (header.getString("processed").equals("Y")))
			return "The Journal has already been processed, cannot therefore delete lines!";
		
		return "success";
	}

}
