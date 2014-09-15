package org.ofbiz.treasurymanagement;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.loans.LoanAccounting;

/***
 * @author Japheth Odonya  @when Sep 14, 2014 11:46:13 PM
 * 
 * Treasury Accounting
 * */
public class TreasuryAccounting {
	public static Logger log = Logger.getLogger(TreasuryAccounting.class);
	
	public static String postTreasuryTransfer(GenericValue treasuryTransfer, Map<String, String> userLogin){
		
		String sourceTreasury = treasuryTransfer.getString("sourceTreasury");
				
		String destinationTreasury = treasuryTransfer.getString("destinationTreasury");
		
		BigDecimal transactionAmount = treasuryTransfer.getBigDecimal("transactionAmount");
		
		log.info("######### Posting Treasury transfer from source to destination (sourceTreasury : "+sourceTreasury+"  destinationTreasury : "+destinationTreasury);
		log.info(" ###### The amount is : "+transactionAmount);
		
		String employeePartyId = userLogin.get("partyId");
		
		//Get Source Account
		String sourceAccountId = getTansferAccount(sourceTreasury);
		
		//Get Destination Account
		String destinationAccountId = getTansferAccount(destinationTreasury);
		
		String acctgTransType = "TREASURY_TRANSFER";
		
//		String acctgTransId = createAccountingTransaction(treasuryTransfer,
//				acctgTransType, userLogin);
		// TODO -  Associate Employees with a branch
		String partyId = "Company";
		String acctgTransId = 	AccHolderTransactionServices.createAccountingTransaction(treasuryTransfer, acctgTransType, userLogin);
		Delegator delegator = treasuryTransfer.getDelegator();
		
		//Credit Source
		String postingType = "C";
		String entrySequenceId = "00001";
		LoanAccounting.postTransactionEntry(delegator, transactionAmount, partyId, sourceAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		
		//Debit Destination
		postingType = "D";
		entrySequenceId = "00002";
		LoanAccounting.postTransactionEntry(delegator, transactionAmount, partyId, destinationAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		return "posted";
	}


	/***
	 * @author Japheth Odonya  @when Sep 15, 2014 12:25:12 AM
	 * 
	 * Given the treasuryId, return the account
	 * */
	private static String getTansferAccount(String treasuryId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//Get Treasury
		GenericValue treasury = null;

		try {
			treasury = delegator.findOne("Treasury",
					UtilMisc.toMap("treasuryId", treasuryId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Treasury ID";
		}
		
		//Get TreasuryType from Treasury
		String treasuryTypeId = treasury.getString("treasuryTypeId");
		
		GenericValue treasuryType = null;
		try {
			treasuryType = delegator.findOne("TreasuryType",
					UtilMisc.toMap("treasuryTypeId", treasuryTypeId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "Cannot Get Treasury Type ID";
		}
		
		//Get glAccountId from Treasury Type
		String accountId = treasuryType.getString("glAccountId");
		return accountId;
	}

}
