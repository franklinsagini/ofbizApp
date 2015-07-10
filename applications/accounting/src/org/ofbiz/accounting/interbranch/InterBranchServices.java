package org.ofbiz.accounting.interbranch;

import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class InterBranchServices {

	public static final String module = InterBranchServices.class.getName();
	public static final String resource = "OrderUiLabels";
	public static final String resource_error = "OrderErrorUiLabels";
	public static final String resourceProduct = "ProductUiLabels";

	public static Map<String, Object> generateEndDayTrans(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> successResult = ServiceUtil.returnSuccess();

		String branchId = (String) context.get("branchId");
		String glAccountId = "42000003";
		String settlementAccount = "943006";
		Delegator delegator = ctx.getDelegator();
		
		List<GenericValue> items = null;
		String acctgTransId = "";
		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, branchId),
				EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, branchId),
				EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId)
				));
		
		try {
			items = delegator.findList("AcctgTransEntry", cond, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (!(items == null)) {
			for (GenericValue item : items) {
				//generate transactions
				GenericValue acctgTrans = delegator.makeValue("AcctgTrans");
				acctgTransId = delegator.getNextSeqId("AcctgTrans");
				acctgTrans.put("acctgTransId", acctgTransId);
				acctgTrans.put("isPosted", "Y");
				acctgTrans.put("transactionDate", UtilDateTime.nowTimestamp());
				acctgTrans.put("acctgTransTypeId", "_NA_");
				acctgTrans.put("description", "SETTLEMENT TRANSACTION");
				try {
					acctgTrans.create();
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			if (item.getString("debitCreditFlag") == "D") {
				//owed by other
				GenericValue acctgTransEntry = delegator.makeValue("AcctgTransEntry");
				acctgTransEntry.put("acctgTransEntrySeqId", delegator.getNextSeqId("AcctgTransEntry"));
				acctgTransEntry.put("glAccountId", settlementAccount);
				acctgTransEntry.put("debitCreditFlag", "D");
				acctgTransEntry.put("organizationPartyId", branchId);
				acctgTransEntry.put("partyId", branchId);
				acctgTransEntry.put("amount",item.getString("amount"));
				try {
					acctgTransEntry.create();
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (item.getString("debitCreditFlag") == "C") {
				//owed by other
				GenericValue acctgTransEntry = delegator.makeValue("AcctgTransEntry");
				acctgTransEntry.put("acctgTransEntrySeqId", delegator.getNextSeqId("AcctgTransEntry"));
				acctgTransEntry.put("glAccountId", settlementAccount);
				acctgTransEntry.put("debitCreditFlag", "C");
				acctgTransEntry.put("organizationPartyId", branchId);
				acctgTransEntry.put("partyId", branchId);
				acctgTransEntry.put("amount",item.getString("amount"));
				try {
					acctgTransEntry.create();
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			}
		}else{
			//no transactions
		}
		
		return successResult;
	}

}
