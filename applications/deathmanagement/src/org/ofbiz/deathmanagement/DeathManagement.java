package org.ofbiz.deathmanagement;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.loansprocessing.LoansProcessingServices;


public class DeathManagement {
	
	public static BigDecimal getFuneralExpenseAmount(){
		BigDecimal bdAmount = BigDecimal.ZERO;
		
		
		List<GenericValue> FuneralExpenseAmountELI = null;

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<String> listOrder = new ArrayList<String>();
		listOrder.add("funeralExpenseAmountId");
		try {
			FuneralExpenseAmountELI = delegator.findList("FuneralExpenseAmount", null, null,
					listOrder, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		for (GenericValue expenseAmount : FuneralExpenseAmountELI) {
			bdAmount = expenseAmount.getBigDecimal("amount");
			
		}
		
		
		return bdAmount;
	}
	
	//org.ofbiz.deathmanagement.DeathManagement.updateDeadMembersLoansToDeceased(deathNotificationId)
	public static String updateDeadMembersLoansToDeceased(Long deathNotificationId, Map<String, String> userLogin){
		
		System.out.println(" Death Notification ID is "+deathNotificationId);
		
		GenericValue deathNotification = LoanUtilities.getEntityValue("DeathNotification", "deathNotificationId", deathNotificationId);
		
		Long partyId = deathNotification.getLong("partyId");
		
		//Get disbursed loans for the partyId
		List<Long> disbursedLoansList =  LoansProcessingServices.getDisbursedLoanApplicationList(partyId);
		
		//Get DECEASED status id
		Long deceasedLoanStatusId = LoanUtilities.getLoanStatusId("DECEASED");
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		for (Long loanApplicationId : disbursedLoansList) {
			//Update the loan to DECEASED
			GenericValue loanApplication = LoanUtilities.getEntityValue("LoanApplication", "loanApplicationId", loanApplicationId);
			loanApplication.set("loanStatusId", deceasedLoanStatusId);
			try {
				delegator.createOrStore(loanApplication);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Add a loan status log for DECEASED
			addLog(loanApplicationId, deceasedLoanStatusId, (String) userLogin.get("userLoginId"));
			
		}
		
		return "";
	}

	private static void addLog(Long loanApplicationId, Long deceasedLoanStatusId, String userLoginId) {
		GenericValue loanStatusLog;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		Long loanStatusLogId = delegator.getNextSeqIdLong("LoanStatusLog", 1);
		loanStatusLog = delegator.makeValue("LoanStatusLog", UtilMisc.toMap(
				"loanStatusLogId", loanStatusLogId, "loanApplicationId",
				Long.valueOf(loanApplicationId), "loanStatusId", deceasedLoanStatusId,
				"createdBy", userLoginId, "comment", "Deceased Member"
						
				));
		try {
			delegator.createOrStore(loanStatusLog);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
