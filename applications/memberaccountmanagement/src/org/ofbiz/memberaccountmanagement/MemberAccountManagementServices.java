package org.ofbiz.memberaccountmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
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

}
