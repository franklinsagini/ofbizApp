package org.ofbiz.accounting.finaccount;

import java.util.Map;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class LoanDisbursement {
	public static final String module = LoanDisbursement.class.getName();
	
	public static Map<String, Object> disburseLoan(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Long loanApplicationId = (Long) context.get("loanApplicationId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		//CREATE ACCOUNTING TRANSACTION (START WITH THESE SO AS TO GET acctgTransId)		
		//Start with the header AcctgTrans
		String acctgTransType = "LOAN_RECEIVABLE";
		String glAcctTypeIdMemberDepo = "CURRENT_LIABILITY";
		String glAcctTypeIdLoans = "CURRENT_ASSET";
		String glAcctTypeIdcharges = "OTHER_INCOME";
		String acctgTransId = createAcctgTrans(acctgTransType, glAcctTypeIdMemberDepo, glAcctTypeIdLoans, glAcctTypeIdcharges);
		
		
		Map<String, Object> errorResult = ServiceUtil.returnError("THIS LOAN HAS ALREADY BEEN CLEARED. YOU CAN NOT DELETE A CLEARED LOAN");
		errorResult.put("loanClearId", loanClearId);
		errorResult.put("loanClearItemId", loanClearItemId);
		return errorResult;
		
		Map<String, Object> successResult = ServiceUtil.returnSuccess("SUCCESSFULLY DISBURSED");
		successResult.put("loanApplicationId", loanApplicationId.toString());
		return successResult;
	}
}
