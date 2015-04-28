package org.ofbiz.treasurymanagement;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

/***
 * @author Japheth Odonya  @when Sep 17, 2014 1:30:38 PM
 * Treasury Management Utility
 * */
public class TreasuryUtility {
	
	public static final Logger log = Logger.getLogger(TreasuryUtility.class);
	
	public static BigDecimal getTellerBalance(Map<String, String> userLogin){
		
		
		BigDecimal bdTellerBalance = BigDecimal.ZERO;
		
		//Teller Balance = Amount Allocated Today from Vault + Cash Deposits - Cash Withdrawals
		BigDecimal bdTotalAllocated = getTotalAllocated(userLogin);
		BigDecimal bdTotalCashDeposits = getTotalCashDeposit(userLogin);
		BigDecimal bdTotalCashWithdrawals = getTotalCashWithdrawal(userLogin);
		
		bdTellerBalance = bdTotalAllocated.add(bdTotalCashDeposits).subtract(bdTotalCashWithdrawals);
		
		
		return bdTellerBalance;
	}
	
	public static BigDecimal getOpeningBalance(String treasuryId, Timestamp date){
		BigDecimal bdBalance = BigDecimal.ZERO;
		
		/****
		 * Transfers In - Transfers Out + Transactions In - Transactions Out before that date
		 * */
		log.info(" TTTTTTTTT Treasury ID "+treasuryId+"DDDDDDDDDDD Date is "+date);
		return bdBalance;
	}

	/***
	 * @author Japheth Odonya  @when Sep 16, 2014 9:05:13 PM
	 * 
	 * Get total amount allocated to this teller in the start of day transfer
	 * */
	public static BigDecimal getTotalAllocated(Map<String, String> userLogin) {
		//Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");
		
		//Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		//GenericValue treasuryTransfer = null;
		//treasuryTransfer = getTreasuryTransfer(treasuryId);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferList(treasuryId);
		
		// if (treasuryTransfer != null){
		// bdAmountAllocated =
		// treasuryTransfer.getBigDecimal("transactionAmount");
		// }
		
		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null) && (genericValue.getBigDecimal("transactionAmount") != null)){
			bdAmountAllocated = bdAmountAllocated.add(genericValue.getBigDecimal("transactionAmount"));
			}
		}
		
		
		return bdAmountAllocated;
	}
	
	public static String getTreasuryId(Map<String, String> userLogin) {
		//Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");
		return treasuryId;
	}
	
	
	/***
	 * Check that teller balance is sufficent
	 * **/
	public static Boolean tellerBalanceSufficient(String treasuryId, Long memberAccountId,  BigDecimal transactionAmount){
		
		GenericValue treasury = getTreasury(treasuryId);
		
		String partyId = treasury.getString("employeeResponsible");
		
		String userLoginId = getUserLoginId(partyId);
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", userLoginId);
		userLogin.put("partyId", partyId);
		
		
		BigDecimal bdTellerBalanceAmt = getTellerBalance(userLogin);
		
		//BigDecimal bdCommissionAmount = AccHolderTransactionServices.getTransactionCommissionAmount(transactionAmount);
		//BigDecimal bdExciseDutyAmount = AccHolderTransactionServices.getTransactionExcideDutyAmount(bdCommissionAmount);
		
		//BigDecimal bdTotalAmount = transactionAmount.add(bdCommissionAmount).add(bdExciseDutyAmount);
		
		if (bdTellerBalanceAmt.compareTo(transactionAmount) != -1)
			return true;
		
		
		return false;
	}

	private static String getUserLoginId(String partyId) {
		
		List<GenericValue> userLoginELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			userLoginELI = delegator.findList("UserLogin", EntityCondition
					.makeCondition("partyId", partyId), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue userLogin = null;
		for (GenericValue genericValue : userLoginELI) {
			userLogin = genericValue;
		}
		
		if (userLogin != null)
			return userLogin.getString("userLoginId");
		
		return null;
	}

	private static GenericValue getTreasury(String treasuryId) {
		List<GenericValue> treasuryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury", EntityCondition
					.makeCondition("treasuryId", treasuryId), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue treasury = null;
		for (GenericValue genericValue : treasuryELI) {
			treasury = genericValue;
		}
		return treasury;
	}

	private static GenericValue getTreasuryTransfer(String treasuryId) {
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//Set calendar to truncate current time timestamp to Year, Month and Day only (exclude hour, minute and seconds)
		Calendar calendar =  Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		//UtilDateTime.
		log.info("################## The time is "+new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is "+treasuryId);
		
		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						treasuryId),  EntityCondition
						.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								new Timestamp(calendar.getTimeInMillis()))),
						EntityOperator.AND);

		try {
			treasuryTransferELI = delegator.findList("TreasuryTransfer",
					transferConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue treasuryTransfer = null;
		for (GenericValue genericValue : treasuryTransferELI) {
			treasuryTransfer = genericValue;
		}
		return treasuryTransfer;
	}

	
	private static List<GenericValue> getTreasuryTransferList(String treasuryId) {
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		//Set calendar to truncate current time timestamp to Year, Month and Day only (exclude hour, minute and seconds)
		Calendar calendar =  Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		//UtilDateTime.
		log.info("################## The time is "+new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is "+treasuryId);
		
		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						treasuryId),  EntityCondition
						.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								new Timestamp(calendar.getTimeInMillis()))),
						EntityOperator.AND);

		try {
			treasuryTransferELI = delegator.findList("TreasuryTransfer",
					transferConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		List<GenericValue> treasuryTransferList = new ArrayList<GenericValue>();
		for (GenericValue genericValue : treasuryTransferELI) {
			//treasuryTransfer = genericValue;
			treasuryTransferList.add(genericValue);
		}
		return treasuryTransferList;
	}
	/***
	 * Cash Amount Withdrawn today
	 * */
	public static BigDecimal getTotalCashWithdrawal(Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");
		
		Calendar calendar =  Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		//Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());
		
		List<GenericValue> cashWithdrawalELI = null;
		//CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS,
						createdBy), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL")		
						),
						EntityOperator.AND);
		
		log.info(" ############ Cash withdrawal createdBy : "+createdBy);
		
//		,
//		EntityCondition
//		.makeCondition("createdStamp",
//				EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			cashWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ############ withdrawals size : "+cashWithdrawalELI.size());


		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}
	
	
	public static BigDecimal getTotalCashDeposit(String treasuryId){
		Map<String, String> userLogin = new HashMap<String, String>();
		
		GenericValue treasury = getTreasury(treasuryId);
		String partyId = treasury.getString("employeeResponsible");
		String userLoginId = getUserLoginId(partyId);
		
		userLogin.put("partyId", partyId);
		userLogin.put("userLoginId", userLoginId);
		
		return getTotalCashDeposit(userLogin);
	}
	
	
	public static BigDecimal getTotalCashWithdrawal(String treasuryId) {
		Map<String, String> userLogin = new HashMap<String, String>();
		
		GenericValue treasury = getTreasury(treasuryId);
		String partyId = treasury.getString("employeeResponsible");
		String userLoginId = getUserLoginId(partyId);
		userLogin.put("partyId", partyId);
		userLogin.put("userLoginId", userLoginId);
		
		return getTotalCashWithdrawal(userLogin);
	}

	/***
	 * Cash Amount Deposited Today
	 * */
	public static BigDecimal getTotalCashDeposit(Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");
		
		Calendar calendar =  Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		//Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());
		
		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS,
						createdBy), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT")		
						),
						EntityOperator.AND);
		
//		,
//		EntityCondition
//		.makeCondition("createdStamp",
//				EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ Cash Deposit createdBy : "+createdBy);
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cashDepositELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashDepositELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}
	
	/****
	 * @author Japheth Odonya  @when Sep 16, 2014 8:46:39 PM
	 * 
	 * Get Teller Name
	 * */
	public static String getTellerName(Map<String, String> userLogin){
		String tellerName = "";
		
		//Get the teller assigned to this partyId (name for Treasury where employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId"); 
		
		GenericValue teller = getTeller(partyId);
		
		if (teller != null)
			tellerName = teller.getString("name");
		
		return tellerName;
	}
	
	public static Boolean hasTellerAssigned(Map<String, String> userLogin){
		
		//Get the teller assigned to this partyId (name for Treasury where employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId"); 
		
		GenericValue teller = getTeller(partyId);
		BigDecimal bdTellerBalance = getTellerBalance(userLogin);
		
		if (teller != null) // check that the teller assigned is of type teller
		{
			if (bdTellerBalance.compareTo(BigDecimal.ZERO) == 1){
				return true;
			}
		}
		
		return false;
	}
	
	
	private static GenericValue getTeller(String partyId) {
		List<GenericValue> treasuryELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury", EntityCondition
					.makeCondition("employeeResponsible", partyId), null, null,
					null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue treasury = null;
		for (GenericValue genericValue : treasuryELI) {
			treasury = genericValue;
		}
		return treasury;
	}
	

	public static String getTelleAssignee(Map<String, String> userLogin){
		String tellerAssignee = "";
		String partyId = userLogin.get("partyId");
		log.info("########## The Party is ::: "+partyId);
		
		//tellerAssignee = partyId;
		//Get the Person with Party ID
		tellerAssignee = getPersonNames(partyId);
		
		return tellerAssignee;
	}
	
	public static String getTellerAccountId(Map<String, String> userLogin){
		String glAccountId = "";
		
		//Get the teller assigned to this partyId (name for Treasury where employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId"); 
		glAccountId = getTeller(partyId).getString("glAccountId");
		
		return glAccountId;
	}

	public static String getTellerTreasuryId(Map<String, String> userLogin){
		String treasuryId = "";
		
		//Get the teller assigned to this partyId (name for Treasury where employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId"); 
		treasuryId = getTeller(partyId).getString("treasuryId");
		
		return treasuryId;
	}
	
	private static String getPersonNames(String partyId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue person = null;
		try {
			person = delegator
					.findOne("Person", UtilMisc.toMap(
							"partyId", partyId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		//minVal = (a < b) ? a : b;
		String names = "";
		names = (person.getString("firstName") != null) ? names+" "+person.getString("firstName") : names+"";
		names = (person.getString("middleName") != null) ? names+" "+person.getString("middleName") : names+"";
		names = (person.getString("lastName") != null) ? names+" "+person.getString("lastName") : names+"";
		
		return names;
	}
	
	public static String getTellerId(Map<String, String> userLogin){
		String treasuryId = "";
		
		//Get the teller assigned to this partyId (name for Treasury where employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId"); 
		treasuryId = getTeller(partyId).getString("treasuryId");
		return treasuryId;
	}
	
	public static Timestamp getEndOfDay(Date transferDate){
		
		LocalDate localTransferDate = new LocalDate(transferDate.getTime());
		localTransferDate = localTransferDate.plusDays(1);

		return new Timestamp(localTransferDate.toDate().getTime());
	}
	
	public static String getFinAccountName(String finAccountId){
		String finAccountName = "";
		
		List<GenericValue> finAccountELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			finAccountELI = delegator.findList("FinAccount",
					EntityCondition.makeCondition("finAccountId", finAccountId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (finAccountELI.size() < 1) {
			return null;
		}


		for (GenericValue genericValue : finAccountELI) {
			finAccountName =genericValue.getString("finAccountName")+" - "+genericValue.getString("finAccountCode");
		}
		
		return finAccountName;
	}
	
	
}
