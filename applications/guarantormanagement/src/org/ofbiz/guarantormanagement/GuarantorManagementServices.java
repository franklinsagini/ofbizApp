package org.ofbiz.guarantormanagement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;

/***
 * @author Japheth Odonya  @when Jun 30, 2015 12:07:02 AM
 * 
 * Guarantor Management Functions
 *
 * */
public class GuarantorManagementServices {
	
	public static Logger log = Logger.getLogger(GuarantorManagementServices.class);
	
	public static String removeGuarantor(Long loanGuarantorId, Map<String, String> userLogin){
		
		//Check that if you remove this guarantor, the total loan balance is still less than the 
		//total deposits for the current members
		GenericValue loanGuarantor = LoanUtilities.getLoanGuarantorEntity(loanGuarantorId);
		
		Long loanApplicationId = loanGuarantor.getLong("loanApplicationId");
		BigDecimal bdLoanBalance = LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId);
		
		BigDecimal bdTotalGuarantorDepositsWithoutThisGuarantor = LoanUtilities.getTotalGuarantorDepositsWithoutThisGuarantor(loanGuarantorId, loanApplicationId);
		
		if (bdLoanBalance.compareTo(bdTotalGuarantorDepositsWithoutThisGuarantor) == 1)
			return "You cannot remove this Guarantor, the remaining guarantors deposits will be less than the loan balance, please add another Guarantor to this loan first!"+" Loan Balance is "+bdLoanBalance+" While total Deposits remaining will be "+bdTotalGuarantorDepositsWithoutThisGuarantor;
		
		//If successfull, create a removed / replaced guarantor record
		GenericValue loanGuarantorRemoved = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long loanGuarantorRemovedId = delegator.getNextSeqIdLong("LoanGuarantorRemoved");
		loanGuarantorRemoved = delegator.makeValue("LoanGuarantorRemoved", UtilMisc
				.toMap("loanGuarantorRemovedId", loanGuarantorRemovedId, "isActive",
						"Y", "createdBy", userLogin.get("userLoginId"),
						"loanApplicationId", loanApplicationId,

						"removedGuarantorId", loanGuarantorId
						));

		log.info(" Added a removed Guarantor Log");

		try {
			TransactionUtil.begin();
			delegator.create(loanGuarantorRemoved);
			TransactionUtil.commit();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		//Remove the guarantor
		Map<String, Long> guarantorCondition = new HashMap<String, Long>();
		guarantorCondition.put("loanGuarantorId", loanGuarantorId);
		try {
			delegator.removeByAnd("LoanGuarantor", guarantorCondition);
			//delegator.removeByAnd("LoanGuarantor", EntityCondition.makeCondition("loanGuarantorId", loanGuarantorId));
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		LoanServices.generateGuarantorPercentages(loanApplicationId);
		
		return "success";
	}
	
	/***
	 * @author Japheth Odonya  @when Jun 30, 2015 1:27:16 AM
	 * 
	 * Get Loan Application ID given loanGuarantorID
	 * */
	public static Long getLoanApplicationIdGivenGuarantorId(Long loanGuarantorId){
		GenericValue loanGuarantor = LoanUtilities.getLoanGuarantorEntity(loanGuarantorId);
		Long loanApplicationId = loanGuarantor.getLong("loanApplicationId");
		return loanApplicationId;
	}

}
