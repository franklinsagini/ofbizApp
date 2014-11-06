package org.ofbiz.treasurymanagement;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
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
 * @author Japheth Odonya  @when Sep 24, 2014 1:10:29 PM
 * 
 * */
public class TreasuryReconciliation {
	
	public static Logger log = Logger.getLogger(TreasuryReconciliation.class);
	
	public static String getTellerName(String treasuryTransferId){
		String tellerName = "";
		
		GenericValue treasuryTransfer = null;
		treasuryTransfer = getTreasuryTransfer(treasuryTransferId);
		
		String destinationTreasury = treasuryTransfer.getString("destinationTreasury");
		
		GenericValue treasury = null;
		treasury = getTreasury(destinationTreasury);
		tellerName = treasury.getString("name");
		return tellerName;
	}
	
	public static String getEmployeeName(String treasuryTransferId){
		String employeeName = "";
		
		GenericValue treasuryTransfer = null;
		treasuryTransfer = getTreasuryTransfer(treasuryTransferId);
		
		String destinationTreasury = treasuryTransfer.getString("destinationTreasury");
		
		GenericValue treasury = null;
		treasury = getTreasury(destinationTreasury);
		
		//Get Member 
		String employeeResponsible = treasury.getString("employeeResponsible");
		
		employeeName = getEmployeeNames(employeeResponsible);
		
		return employeeName;
	}
	
	public static BigDecimal getTransferAmount(String treasuryTransferId){
		BigDecimal transferAmount = BigDecimal.ZERO;
		GenericValue treasuryTransfer = null;
		treasuryTransfer = getTreasuryTransfer(treasuryTransferId);
		//transactionAmount
		transferAmount = treasuryTransfer.getBigDecimal("transactionAmount");
		return transferAmount;
	}
	
	public static BigDecimal getTotalBalance(String treasuryTransferId){
		//Total Allocated + Total Deposit - TotalWithdrawal
		
		BigDecimal bdTotalAllocated = getTransferAmount(treasuryTransferId);
		BigDecimal bdDeposit = getTotalDeposits(treasuryTransferId);
		BigDecimal bdWithdrawal = getTotalWithdrawal(treasuryTransferId);
		
		
		BigDecimal bdBalance =  bdTotalAllocated.add(bdDeposit).subtract(bdWithdrawal);
		
		return bdBalance;
	}
	
	public static BigDecimal getTotalWithdrawal(String treasuryTransferId){
		BigDecimal withdrawalAmount = BigDecimal.ZERO;
		

		
		String userLoginId = getUserLogin(treasuryTransferId);
		
		Timestamp transactionDate = getTreasuryTransfer(treasuryTransferId).getTimestamp("createdStamp");
		withdrawalAmount = getTotalCashWithdrawal(userLoginId, transactionDate);
		return withdrawalAmount;
	}
	
	public static BigDecimal getTotalDeposits(String treasuryTransferId){
		BigDecimal depositAmount = BigDecimal.ZERO;
		
		String userLoginId = getUserLogin(treasuryTransferId);
		Timestamp transactionDate = getTreasuryTransfer(treasuryTransferId).getTimestamp("createdStamp");
		
		depositAmount = getTotalCashDeposit(userLoginId, transactionDate);
		return depositAmount;
	}
	
	private static GenericValue getTreasuryTransfer(String treasuryTransferId){
		GenericValue treasuryTransfer = null;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryTransfer = delegator
					.findOne("TreasuryTransfer", UtilMisc.toMap(
							"treasuryTransferId", treasuryTransferId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		return treasuryTransfer;
	}
	
	private static GenericValue getTreasury(String treasuryId){
		GenericValue treasury = null;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasury = delegator
					.findOne("Treasury", UtilMisc.toMap(
							"treasuryId", treasuryId),
							false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		return treasury;
	}
	
	private static String getEmployeeNames(String partyId) {
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
	
	public static String getUserLogin(String treasuryTransferId){
		String userLoginId = "";
		
		GenericValue treasuryTransfer = null;
		treasuryTransfer = getTreasuryTransfer(treasuryTransferId);
		
		String destinationTreasury = treasuryTransfer.getString("destinationTreasury");
		
		GenericValue treasury = null;
		treasury = getTreasury(destinationTreasury);
		
		//Get Member 
		String employeeResponsible = treasury.getString("employeeResponsible");
		
		String partyId = employeeResponsible;
		
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
		
		userLoginId = userLogin.getString("userLoginId");
		return userLoginId;
	}
	
	/***
	 * Cash Amount Withdrawn today
	 * */
	public static BigDecimal getTotalCashWithdrawal(String userLoginId, Timestamp transactionDate) {
		// TODO Auto-generated method stub
		String createdBy = userLoginId;
		
		Calendar calendarStart =  Calendar.getInstance();
		calendarStart.setTimeInMillis(transactionDate.getTime());
		
		calendarStart.set(Calendar.MILLISECOND, 0);
		calendarStart.set(Calendar.SECOND, 0);
		calendarStart.set(Calendar.MINUTE, 0);
		calendarStart.set(Calendar.HOUR_OF_DAY, 0);
		
		Timestamp tstampDateCreatedStart = new Timestamp(calendarStart.getTimeInMillis());
		
		Calendar calendarEnd =  Calendar.getInstance();
		calendarEnd.setTimeInMillis(transactionDate.getTime());
		
		calendarEnd.set(Calendar.MILLISECOND, 0);
		calendarEnd.set(Calendar.SECOND, 0);
		calendarEnd.set(Calendar.MINUTE, 0);
		calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
		calendarEnd.set(Calendar.DATE, Calendar.DATE+1);
		
		Timestamp tstampDateCreatedEnd = new Timestamp(calendarEnd.getTimeInMillis());
		
		List<GenericValue> cashWithdrawalELI = null;
		//CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS,
						createdBy), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL"),
								EntityCondition
								.makeCondition("createdStamp",
										EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreatedStart),
										
										EntityCondition
										.makeCondition("createdStamp",
												EntityOperator.LESS_THAN_EQUAL_TO, tstampDateCreatedEnd)
						),
						EntityOperator.AND);
		
		log.info(" ############ Cash withdrawal createdBy : "+createdBy);
		

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

	/***
	 * Cash Amount Deposited Today
	 * */
	public static BigDecimal getTotalCashDeposit(String userLoginId, Timestamp transactionDate) {
		// TODO Auto-generated method stub
		String createdBy = userLoginId;
		
		Calendar calendarStart =  Calendar.getInstance();
		calendarStart.setTimeInMillis(transactionDate.getTime());
		
		calendarStart.set(Calendar.MILLISECOND, 0);
		calendarStart.set(Calendar.SECOND, 0);
		calendarStart.set(Calendar.MINUTE, 0);
		calendarStart.set(Calendar.HOUR_OF_DAY, 0);
		
		Timestamp tstampDateCreatedStart = new Timestamp(calendarStart.getTimeInMillis());
		
		Calendar calendarEnd =  Calendar.getInstance();
		calendarEnd.setTimeInMillis(transactionDate.getTime());
		
		calendarEnd.set(Calendar.MILLISECOND, 0);
		calendarEnd.set(Calendar.SECOND, 0);
		calendarEnd.set(Calendar.MINUTE, 0);
		calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
		calendarEnd.set(Calendar.DATE, Calendar.DATE+1);
		
		Timestamp tstampDateCreatedEnd = new Timestamp(calendarEnd.getTimeInMillis());
		
		List<GenericValue> cashDepositELI = null;
		//CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS,
						createdBy), EntityCondition
						.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT"),
								EntityCondition
								.makeCondition("createdStamp",
										EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreatedStart),
										
										EntityCondition
										.makeCondition("createdStamp",
												EntityOperator.LESS_THAN_EQUAL_TO, tstampDateCreatedEnd)
						),
						EntityOperator.AND);
		
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

}