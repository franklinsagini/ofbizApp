package org.ofbiz.treasurymanagement;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;

public class TreasuryUtility {
	
	public static final Logger log = Logger.getLogger(TreasuryUtility.class);
	
	public static BigDecimal getTellerBalance(Map<String, String> userLogin){
		
		
		BigDecimal bdTellerBalance = BigDecimal.ZERO;
		
		//Teller Balance = Amount Allocated Today from Vault + Cash Deposits - Cash Withdrawals
		BigDecimal bdTotalAllocated = getTotalAllocated();
		BigDecimal bdTotalCashDeposits = getTotalCashDeposit();
		BigDecimal bdTotalCashWithdrawals = getTotalCashWithdrawal();
		
		
		return bdTellerBalance;
	}

	private static BigDecimal getTotalAllocated() {
		// TODO Auto-generated method stub
		return null;
	}

	private static BigDecimal getTotalCashWithdrawal() {
		// TODO Auto-generated method stub
		return null;
	}

	/***
	 * Amount Transferred Today
	 * */
	private static BigDecimal getTotalCashDeposit() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String getTellerName(Map<String, String> userLogin){
		String tellerName = "";
		
		//Get the teller assigned to this partyId
		
		
		return tellerName;
	}
	
	public static String getTelleAssignee(Map<String, String> userLogin){
		String tellerAssignee = "";
		String partyId = userLogin.get("partyId");
		log.info("########## The Party is ::: "+partyId);
		
		tellerAssignee = partyId;
		
		return tellerAssignee;
	}
	
	
}
