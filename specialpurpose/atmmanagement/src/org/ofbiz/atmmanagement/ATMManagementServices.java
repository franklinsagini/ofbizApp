package org.ofbiz.atmmanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

/***
 * @author Japheth Odonya  @when Nov 5, 2014 8:40:14 PM
 * 
 * */
public class ATMManagementServices {
	
	public static Logger log = Logger.getLogger(ATMManagementServices.class);
	
	public static String getMemberAccounts(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		partyId = partyId.replaceAll(",", "");
		List<GenericValue> memberAccountELI = null;
		try {
			memberAccountELI = delegator.findList("MemberAccount", EntityCondition.makeCondition("partyId", Long.valueOf(partyId)), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (memberAccountELI == null){
			result.put("", "No Accounts");
		}
		
		for (GenericValue genericValue : memberAccountELI) {
			result.put(genericValue.get("memberAccountId").toString(), genericValue.get("accountNo")+" - "+genericValue.getString("accountName"));
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
	
	public static String applyforATM(HttpServletRequest request,
			HttpServletResponse response) {
		//Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String cardApplicationId = (String) request
				.getParameter("cardApplicationId");
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session
				.getAttribute("userLogin");
		String userLoginId = userLogin.getString("userLoginId");

		GenericValue cardApplication = null;

		cardApplicationId = cardApplicationId.replaceAll(",", "");
		try {
			cardApplication = delegator.findOne(
					"CardApplication",
					UtilMisc.toMap("cardApplicationId",
							Long.valueOf(cardApplicationId)), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		//
		Long cardStatusId = getCardStatus("APPLIED");
		

		cardApplication.set("cardStatusId", cardStatusId);
		try {
			delegator.createOrStore(cardApplication);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		// Create Card Log
		GenericValue cardLog;
		Long cardLogId = delegator.getNextSeqIdLong("CardLog", 1);
		//loanApplicationId = loanApplicationId.replaceAll(",", "");
		cardLog = delegator.makeValue("CardLog", UtilMisc.toMap(
				"cardLogId", cardLogId, "cardApplicationId",
				Long.valueOf(cardApplicationId), "cardStatusId", cardStatusId, "createdBy",
				userLoginId, "comment", "Applied for ATM Card"));

		try {
			delegator.createOrStore(cardLog);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		return "success";
	}

	private static Long getCardStatus(String name) {
		List<GenericValue> cardStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardStatusELI = delegator.findList("CardStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long cardStatusId = 0L;
		for (GenericValue genericValue : cardStatusELI) {
			cardStatusId = genericValue.getLong("loanStatusId");
		}
		return cardStatusId;
	}
	
	/****
	 * @author Japheth Odonya  @when Nov 5, 2014 8:39:57 PM
	 * Check that Member Has Enough Money on account
	 * 		Account Balance >= (Min Balance + Charge Amount)
	 * **/
	public static String memberHasEnoughBalance(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		Long cardApplicationId = Long.valueOf(request.getParameter("cardApplicationId"));
		
		
		
		String memberAccountId = getMemberAccountId(cardApplicationId);

		memberAccountId = memberAccountId.replaceAll(",", "");
		List<GenericValue> memberAccountELI = null;
		try {
			memberAccountELI = delegator.findList("MemberAccount", EntityCondition.makeCondition("memberAccountId", Long.valueOf(memberAccountId)), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (memberAccountELI == null){
			result.put("", "No Accounts");
		}
		GenericValue memberAccount = null;
		for (GenericValue genericValue : memberAccountELI) {
			memberAccount = genericValue;
			//result.put(genericValue.get("memberAccountId").toString(), genericValue.get("accountNo")+" - "+genericValue.getString("accountName"));
		}
		
		//Get Minimum Balance
		BigDecimal bdAccountMinimumBalanceAmount = getAccountMinimumBalance(memberAccount.getLong("accountProductId"));
		
		//Get Card Application Charge
		BigDecimal bdCardChargeAmount = getCardCharge("ATM Application Fee");
		
		//Get Member Balance for the Account
		//( Opening Balance + Total Deposits and other increases (I) - Total Withdrawals and Charges (D))
		BigDecimal bdMemberBalanceAmount = getMemberBalanceAmount(memberAccount);
		//Check if Member Balance >= Minimum Balance + Card Application Charge
		
		String balanceStatus = "NOTENOUGH";
		
		if (bdMemberBalanceAmount.compareTo(bdAccountMinimumBalanceAmount.add(bdCardChargeAmount)) > -1){
			balanceStatus = "ENOUGH";
		} 
		
		result.put("balanceStatus", balanceStatus);
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}

	private static String getMemberAccountId(Long cardApplicationId) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue cardApplication = null;

		try {
			cardApplication = delegator.findOne("CardApplication",
					UtilMisc.toMap("cardApplicationId", cardApplicationId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String memberAccountId = cardApplication.getString("memberAccountId");
		return memberAccountId;
	}

	/***
	 * Get Member Balance 
	 * 		Opening Balance + Deposits (Increments) - Withdrawals and Charges (D)
	 * */
	private static BigDecimal getMemberBalanceAmount(GenericValue memberAccount) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		BigDecimal bdAccountBalanceAmount = AccHolderTransactionServices.getTotalSavings(memberAccount.getLong("memberAccountId").toString(), delegator);
		
		log.info(" BBBBBBBBBBBBBB Balance is "+bdAccountBalanceAmount);
		return bdAccountBalanceAmount;
	}

	/***
	 * @author Japheth Odonya  @when Nov 5, 2014 8:39:48 PM
	 * Get the Charge Amount from the ProductCharge where all the charges are defined
	 * ** We know that all card charges are fixed figures so we will just go ahead and pick a value from the 
	 * 	  fixedAmount column of the product charge
	 * **
	 * */
	private static BigDecimal getCardCharge(String chargeName) {
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> productChargeELI = null;
		try {
			productChargeELI = delegator.findList("ProductCharge", EntityCondition.makeCondition("name", chargeName), null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		BigDecimal bdProductChargeAmount =  BigDecimal.ZERO;
		for (GenericValue genericValue : productChargeELI) {
			//result.put(genericValue.get("memberAccountId").toString(), genericValue.get("accountNo")+" - "+genericValue.getString("accountName"));
			bdProductChargeAmount = genericValue.getBigDecimal("fixedAmount");
		}
		
		log.info(" CCCCCCCCCCCCCCCCCCCCC Card Amount is "+bdProductChargeAmount);
		return bdProductChargeAmount;
	}

	
	/**
	 * @author Japheth Odonya  @when Nov 5, 2014 8:39:37 PM
	 * Determines Account Minimum Balance from Configuration
	 * */
	private static BigDecimal getAccountMinimumBalance(Long accountProductId) {
		
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue accountProduct = null;

		// SaccoProduct
		try {
			accountProduct = delegator.findOne("AccountProduct",
					UtilMisc.toMap("accountProductId", accountProductId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		BigDecimal bdMinimumBalance = BigDecimal.ZERO;

		bdMinimumBalance = accountProduct.getBigDecimal("minBalanceAmt");
		
		log.info(" MMMMMMMMMMMMMMMMMM Minimum Balance is "+bdMinimumBalance);
		return bdMinimumBalance;
	}

}
