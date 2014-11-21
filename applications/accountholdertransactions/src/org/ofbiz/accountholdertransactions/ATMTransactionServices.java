package org.ofbiz.accountholdertransactions;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.model.ATMStatus;
import org.ofbiz.atmmanagement.ATMManagementServices;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.service.DispatchContext;

public class ATMTransactionServices {
	
	public static Logger log = Logger.getLogger(ATMTransactionServices.class);
	
	public static Map<String, Object> getMemberBalance(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		
		String cardNumber=(String) context.get("cardNumber");
		
		log.info(" ##### Card Number "+cardNumber);
		
		
		ATMStatus cardState  = getCardState(cardNumber);
		
		String status = cardState.getStatus(); 
		result.put("status", status);
		if (!cardState.getStatus().equals("ACTIVE")){
			return result;
		}
		String memberAccountId = String.valueOf(cardState.getMemberAccountId());
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		 BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		 bdOpeningBalance = AccHolderTransactionServices.calculateOpeningBalance(memberAccountId,
		 delegator);
		// // Get Total Deposits
		 Timestamp balanceDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
		 
		 BigDecimal balance = AccHolderTransactionServices.getAvailableBalanceVer2(memberAccountId, balanceDate).add(bdOpeningBalance);
				 
		 log.info(" #####BBBBBBB  Balance "+balance);
		 result.put("balance", balance) ;
		return result;
	}
	
	private static ATMStatus getCardState(String cardNumber) {
		ATMStatus state = null;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> cardApplicationELI = null;
		try {
			cardApplicationELI = delegator.findList("CardApplication", EntityCondition
					.makeCondition("cardNumber", cardNumber), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		Long cardStatusId = null;
		
		for (GenericValue genericValue : cardApplicationELI) {
			state = new ATMStatus();
			state.setMemberAccountId(genericValue.getLong("memberAccountId"));
			cardStatusId = genericValue.getLong("cardStatusId");
		}
		Long activeCardStatusId = ATMManagementServices.getCardStatus("ACTIVE");
		if (state == null) //No Card Found
		{
			state = new ATMStatus();
			state.setStatus("NOCARDFOUND");
		} else{
			
			if (cardStatusId.equals(activeCardStatusId)){
				state.setStatus("ACTIVE");
			} else{
				state.setStatus("CARDNOTACTIVE");
			}
			
		}
		return state;
	}

	public static Map<String, Object> withdrawMemberCash(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		
		String cardNumber=(String) context.get("cardNumber");
		BigDecimal amount = (BigDecimal)  context.get("amount");
		
		log.info(" ##### Card Number "+cardNumber);
		log.info(" ##### Amount "+amount);
		
		
		ATMStatus cardState  = getCardState(cardNumber);
		
		String status = cardState.getStatus(); 
		result.put("status", status);
		if (!cardState.getStatus().equals("ACTIVE")){
			return result;
		}
		String memberAccountId = String.valueOf(cardState.getMemberAccountId());
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		 BigDecimal bdOpeningBalance = BigDecimal.ZERO;
		// // Get Opening Balance
		 bdOpeningBalance = AccHolderTransactionServices.calculateOpeningBalance(memberAccountId,
		 delegator);
		// // Get Total Deposits
		 Timestamp balanceDate = new Timestamp(Calendar.getInstance().getTimeInMillis());
		 
		 BigDecimal balance = AccHolderTransactionServices.getAvailableBalanceVer2(memberAccountId, balanceDate).add(bdOpeningBalance);
			
		 
		 BigDecimal bdChargesTotal = AccHolderTransactionServices.getChargesTotal(Long.valueOf(memberAccountId), amount, "CASHWITHDRAWAL");
		 
		BigDecimal bdMinimumBalance = AccHolderTransactionServices.getMinimumBalance(Long.valueOf(memberAccountId));
		
		BigDecimal bdTotalTransactionAmount = amount.add(bdChargesTotal).add(bdMinimumBalance);
		
		if (balance.compareTo(bdTotalTransactionAmount) != -1){
			//withdraw cash
			AccHolderTransactionServices.cashWithdrawal(amount, memberAccountId);
			status = "SUCCESS";
		} else{
			//transaction cannot succeed
			status = "NOTENOUGHBALANCE";
		}
		
		 log.info(" #####BBBBBBB  Balance "+balance.subtract(bdTotalTransactionAmount));
		 //result.put("balance", balance) ;
		return result;
	}

	
	public static Map<String, Object> getMemberStatement(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		return result;
	}

}
