/**
 * 
 */
package org.ofbiz.accounting.teller;

import java.math.BigDecimal;
import java.util.List;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

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
	
	public static boolean isApprover(GenericValue userLogin, Delegator delegator, GenericValue approval) {
		
		String partyId = userLogin.getString("partyId");
		List<GenericValue>approvers;
		GenericValue approver;
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>TRANSACTION AMOUNT: "+approval.getBigDecimal("amount"));
		boolean isApprover = false;
		
		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId)
				));
		try {
			approvers = delegator.findList("TellerLimitApprovers", cond, null, null, null, false);
			if (approvers.size() > 0) {
				approver = approvers.get(0);
				
				if (approval.getBigDecimal("amount").compareTo(approver.getBigDecimal("amount")) == 1) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>APPROVER AMOUNT: "+approver.getBigDecimal("amount"));
					System.out.println("STOP THIS TRANSACTION APPROVER CAN NOT APPROVE THIS AMOUNT");
					isApprover = false;
				}else if (approval.getBigDecimal("amount").compareTo(approver.getBigDecimal("amount")) == 0) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>APPROVER AMOUNT: "+approver.getBigDecimal("amount"));
					System.out.println("CONTINUE ALL IS GOOD APPROVER AMOUNT IS EQUAL TO TRANSACTION AMOUNT");
					isApprover = true;
				}else{
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>APPROVER AMOUNT: "+approver.getBigDecimal("amount"));
					System.out.println("CONTINUE ALL IS GOOD APPROVER AMOUNT IS LESS THAN TRANSACTION AMOUNT");
					isApprover = true;
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
			
		
		return isApprover;
		
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
