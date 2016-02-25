/**
 * 
 */
package org.ofbiz.accounting.teller;

import java.math.BigDecimal;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

/**
 * @author samoei
 *
 */
public class TellerUtilServices {
	public static final String module = TellerUtilServices.class.getName();

	public static boolean needsApproval(GenericValue cashWithdrawal, Delegator delegator) {
		
		BigDecimal transactionAmount = cashWithdrawal.getBigDecimal("transactionAmount");
		BigDecimal limitAmount = getTellerLimitAmount(delegator);
		
		if (transactionAmount.compareTo(limitAmount) == 1) {
			System.out.println("THE AMOUNT IS BIGGER THAN THE LIMIT");
			return true;
		}else if (transactionAmount.compareTo(limitAmount) == 0) {
			System.out.println("THE AMOUNT IS EQUAL THAN THE LIMIT");
			return true;
		}else{
			System.out.println("NO NEED TO WORRY THE AMOUNT IS LESS THAN THE LIMIT");
			return false;
		}
		
	}
	
	private static BigDecimal getTellerLimitAmount(Delegator delegator) {
		BigDecimal limitAmount = null;
		GenericValue limitRecord = null;

		try {
			limitRecord = delegator.findOne("TellerLimitAmount", UtilMisc.toMap("tellerLimitAmountId", "TELER_LIMIT"), false);
			limitAmount = limitRecord.getBigDecimal("amount");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return limitAmount;
	}

	public static boolean createCashWithdrawalApprovalDoc(GenericValue cashWithdrawal, Delegator delegator, GenericValue userLogin) {
		boolean transactionCreated = false;
		GenericValue tellerLimitApprovals = null;
		String tellerLimitApprovalsId = null;
		
		System.out.println("SHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHHSHHHHSSHH ");
		
		
		tellerLimitApprovals = delegator.makeValue("TellerLimitApprovals");
		tellerLimitApprovalsId = delegator.getNextSeqId("TellerLimitApprovals");
		tellerLimitApprovals.put("tellerLimitApprovalsId", tellerLimitApprovalsId);
		tellerLimitApprovals.put("tellerId", userLogin.getString("userLoginId"));
		tellerLimitApprovals.put("memberAccountId", cashWithdrawal.getLong("memberAccountId"));
		tellerLimitApprovals.put("statusName", "DRAFTED");
		tellerLimitApprovals.put("amount", cashWithdrawal.getBigDecimal("transactionAmount"));
		
		try {
			tellerLimitApprovals.create();
			transactionCreated = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return transactionCreated;
	}
	
	
}
