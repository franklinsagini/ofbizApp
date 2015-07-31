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
 * @author Japheth Odonya @when Sep 17, 2014 1:30:38 PM Treasury Management
 *         Utility
 * */
public class TreasuryUtility {

	public static final Logger log = Logger.getLogger(TreasuryUtility.class);

	public static BigDecimal getTellerBalance(Map<String, String> userLogin) {

		BigDecimal bdTellerBalance = BigDecimal.ZERO;

		// Teller Balance = Amount Allocated Today from Vault + Cash Deposits -
		// Cash Withdrawals
		BigDecimal bdTotalAllocated = getTotalAllocated(userLogin);
		BigDecimal bdTotalCashDeposits = getTotalCashDeposit(userLogin);
		BigDecimal bdTotalCashWithdrawals = getTotalCashWithdrawal(userLogin);

		bdTellerBalance = bdTotalAllocated.add(bdTotalCashDeposits).subtract(
				bdTotalCashWithdrawals);

		return bdTellerBalance;
	}

	// getTellerBalance
	public static BigDecimal getTellerBalanceWithOpeningBalance(
			Map<String, String> userLogin) {

		BigDecimal bdTellerBalance = BigDecimal.ZERO;

		// Teller Balance = Amount Allocated Today from Vault + Cash Deposits -
		// Cash Withdrawals
		BigDecimal bdTotalAllocated = getTotalAllocated(userLogin);
		BigDecimal bdTotalCashDeposits = getTotalCashDepositToday(userLogin);
		BigDecimal bdTotalCashWithdrawals = getTotalCashWithdrawalToday(userLogin);
		
		BigDecimal bdTotalLoanCashDeposit = getTotalLoanCashDepositToday(userLogin);

		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Date date = new Date
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Date date = new Date(calendar.getTimeInMillis());

		BigDecimal bdOpeningBalance = getOpeningBalance(treasuryId, date);
		BigDecimal bdDeallocatedAmount = getTotalDeAllocatedToday(userLogin,
				new Timestamp(date.getTime()));
		
		bdTellerBalance = bdTotalAllocated.add(bdOpeningBalance)
				.add(bdTotalCashDeposits).subtract(bdTotalCashWithdrawals);
		
		bdTellerBalance = bdTellerBalance.add(bdTotalLoanCashDeposit);
		
		bdTellerBalance = bdTellerBalance.subtract(bdDeallocatedAmount);
		log.info("DDDDDDDD DDDDDDDDDDD ########## ");
		log.info("DDDDDDDD AMount Deallocated is ########## "
				+ bdDeallocatedAmount);

		return bdTellerBalance;
	}

	/***
	 * Get teller balance before specified date
	 * */
	public static BigDecimal getTellerBalanceBeforeDate(
			Map<String, String> userLogin, Timestamp date) {

		BigDecimal bdTellerBalance = BigDecimal.ZERO;

		// Teller Balance = Amount Allocated Today from Vault + Cash Deposits -
		// Cash Withdrawals
		BigDecimal bdTotalAllocated = getTotalAllocated(userLogin, date);
		BigDecimal bdTotalDeAllocated = getTotalDeAllocated(userLogin, date);

		BigDecimal bdTotalCashDeposits = getTotalCashDeposit(userLogin, date);
		BigDecimal bdTotalCashDepositsReversal = getTotalCashDepositReversal(userLogin, date);
		
		BigDecimal bdTotalCashWithdrawals = getTotalCashWithdrawal(userLogin,
				date);
		BigDecimal bdTotalCashWithdrawalReversal = getTotalCashWithdrawalReversed(userLogin,
				date);
		
		//Add Loan Cash Pay
		
		//Add Cash Withdrawal Reversal
		
		BigDecimal bdTotalLoanCashPay = getTotalLoanCashPay(userLogin, date);
		BigDecimal bdTotalLoanCashPayReversal = getTotalLoanCashPayReversal(userLogin, date);
		


		bdTellerBalance = bdTotalAllocated.subtract(bdTotalDeAllocated)
				.add(bdTotalCashDeposits).subtract(bdTotalCashWithdrawals);
		
		bdTellerBalance = bdTellerBalance.add(bdTotalLoanCashPay);
		bdTellerBalance = bdTellerBalance.add(bdTotalCashWithdrawalReversal);
		
		bdTellerBalance = bdTellerBalance.subtract(bdTotalCashDepositsReversal);
		bdTellerBalance = bdTellerBalance.subtract(bdTotalLoanCashPayReversal);
		
		

		return bdTellerBalance;
	}

	private static BigDecimal getTotalCashWithdrawal(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashWithdrawalELI = null;
		// CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)),
						EntityOperator.AND);

		log.info(" ############ Cash withdrawal createdBy : " + createdBy);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			cashWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ############ withdrawals size : " + cashWithdrawalELI.size());

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	private static BigDecimal getTotalCashDeposit(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	
	/***
	 * Get total cash deposit reversal
	 * */
	
	private static BigDecimal getTotalCashDepositReversal(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");
		
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSITREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit Reversed createdBy : " + createdBy);
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
	
	
	/***
	 * Get Loan Cash Pay
	 * */
	private static BigDecimal getTotalLoanCashPay(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAY"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	 * Get total cash pay reversal
	 * 
	 * 
	 * */
	private static BigDecimal getTotalLoanCashPayReversal(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");
		
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAYREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)

				), EntityOperator.AND);

		log.info(" ############ Loan Cash Pay Reversed createdBy : " + createdBy);
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
	 * Get Cash Withdrawal Reversed
	 * */
	private static BigDecimal getTotalCashWithdrawalReversed(
			Map<String, String> userLogin, Timestamp date) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWALREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstampDateCreated)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	public static BigDecimal getOpeningBalance(Map<String, String> userLogin) {

		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		return getOpeningBalance(treasuryId,
				new Date(calendar.getTimeInMillis()));
	}

	public static BigDecimal getOpeningBalance(String treasuryId, Date date) {
		BigDecimal bdBalance = BigDecimal.ZERO;
		Timestamp transactionDate = new Timestamp(date.getTime());
		/****
		 * Transfers In - Transfers Out + Transactions In - Transactions Out
		 * before that date
		 * */
		log.info(" TTTTTTTTT Treasury ID " + treasuryId
				+ "DDDDDDDDDDD Date is " + transactionDate);

		GenericValue treasury = getTreasury(treasuryId);

		String partyId = treasury.getString("employeeResponsible");

		String userLoginId = getUserLoginId(partyId);
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", userLoginId);
		userLogin.put("partyId", partyId);

		String treasuryTypeName = TreasuryReconciliation
				.getTreasuryTypeName(treasuryId);
		if (treasuryTypeName.equals("BANK")) {

			log.info("IIIIIIIII Its a BANK !!!!!!!!!!!!!!!!!!!!! ");
			String glAccountId = TreasuryReconciliation
					.getTreasuryAccountId(treasuryId);
			bdBalance = TreasuryAccounting.getAccountBalance(glAccountId,
					transactionDate);

			return bdBalance;
		}
		
		
		if (treasuryTypeName.equals("VAULT")) {

			log.info("IIIIIIIII Its a VAULT !!!!!!!!!!!!!!!!!!!!! ");
			String glAccountId = TreasuryReconciliation
					.getTreasuryAccountId(treasuryId);
			bdBalance = TreasuryAccounting.getAccountBalance(glAccountId,
					transactionDate);

			return bdBalance;
		}

		bdBalance = getTellerBalanceBeforeDate(userLogin, transactionDate);

		return bdBalance;
	}

	/***
	 * Get transactions in on the date specified (DEPOSITS)
	 * */
	public static BigDecimal getTransactionsIn(String treasuryId, Date date) {
		BigDecimal bdTotalsIn = BigDecimal.ZERO;

		Timestamp transactionDate = new Timestamp(date.getTime());
		GenericValue treasury = getTreasury(treasuryId);

		String partyId = treasury.getString("employeeResponsible");

		String userLoginId = getUserLoginId(partyId);
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", userLoginId);
		userLogin.put("partyId", partyId);

		bdTotalsIn = getTotalCashDeposit(userLogin, transactionDate, true);
		
		
		//Add CASHWITHDRAWALREVERSED
		BigDecimal bdTotalWithdrawalReversed = getTotalWithdrawalsReversed(userLogin, transactionDate, true, treasuryId);
		//Add LOANCASHPAY
		BigDecimal bdTotalLoanCashRepay = getTotalLoanCashRepay(userLogin, transactionDate, true, treasuryId);
		bdTotalsIn = bdTotalsIn.add(bdTotalWithdrawalReversed);
		bdTotalsIn = bdTotalsIn.add(bdTotalLoanCashRepay);
		
		
		//bdTotalsIn = bdTotalsIn.add(getTotalLoanCashDeposit(userLogin, transactionDate, true));
		return bdTotalsIn;
	}
	
	
	private static BigDecimal getTotalLoanCashDeposit(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAY"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	private static BigDecimal getTotalCashDeposit(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	/***
	 * Get transactions out on the date specified (WITHDRAWALS)
	 * */
	public static BigDecimal getTransactionsOut(String treasuryId, Date date) {
		BigDecimal bdTotalsOut = BigDecimal.ZERO;

		Timestamp transactionDate = new Timestamp(date.getTime());
		GenericValue treasury = getTreasury(treasuryId);

		String partyId = treasury.getString("employeeResponsible");

		String userLoginId = getUserLoginId(partyId);
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", userLoginId);
		userLogin.put("partyId", partyId);

		
		bdTotalsOut = getTotalCashWithdrawal(userLogin, transactionDate, true);
		
		//Add CASHDEPOSITREVERSED
		
		BigDecimal bdTotalCashDepositReversed = getTotalCashDepositReversed(userLogin, transactionDate, true, treasuryId);
		//Add LOANCASHPAYREVERSED
		BigDecimal bdTotalLoanCashRepayReversed = getTotalLoanCashRepayReversed(userLogin, transactionDate, true, treasuryId);
		bdTotalsOut = bdTotalsOut.add(bdTotalCashDepositReversed);
		bdTotalsOut = bdTotalsOut.add(bdTotalLoanCashRepayReversed);

		return bdTotalsOut;
	}

	private static BigDecimal getTotalCashWithdrawal(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Withdrawal createdBy : " + createdBy);
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

	/***
	 * @author Japheth Odonya @when Sep 16, 2014 9:05:13 PM
	 * 
	 *         Get total amount allocated to this teller in the start of day
	 *         transfer
	 * */
	public static BigDecimal getTotalAllocated(Map<String, String> userLogin) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		// GenericValue treasuryTransfer = null;
		// treasuryTransfer = getTreasuryTransfer(treasuryId);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferList(treasuryId);

		// if (treasuryTransfer != null){
		// bdAmountAllocated =
		// treasuryTransfer.getBigDecimal("transactionAmount");
		// }

		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null)
					&& (genericValue.getBigDecimal("transactionAmount") != null)) {
				bdAmountAllocated = bdAmountAllocated.add(genericValue
						.getBigDecimal("transactionAmount"));
			}
		}

		return bdAmountAllocated;
	}

	public static BigDecimal getTotalAllocated(Map<String, String> userLogin,
			Timestamp date) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		// GenericValue treasuryTransfer = null;
		// treasuryTransfer = getTreasuryTransfer(treasuryId);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferInList(
				treasuryId, date);

		// if (treasuryTransfer != null){
		// bdAmountAllocated =
		// treasuryTransfer.getBigDecimal("transactionAmount");
		// }

		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null)
					&& (genericValue.getBigDecimal("transactionAmount") != null)) {
				bdAmountAllocated = bdAmountAllocated.add(genericValue
						.getBigDecimal("transactionAmount"));
			}
		}

		return bdAmountAllocated;
	}

	public static BigDecimal getTotalDeAllocated(Map<String, String> userLogin,
			Timestamp date) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		// GenericValue treasuryTransfer = null;
		// treasuryTransfer = getTreasuryTransfer(treasuryId);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferOutList(
				treasuryId, date);

		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null)
					&& (genericValue.getBigDecimal("transactionAmount") != null)) {
				bdAmountAllocated = bdAmountAllocated.add(genericValue
						.getBigDecimal("transactionAmount"));
			}
		}

		return bdAmountAllocated;
	}

	public static BigDecimal getTotalDeAllocatedToday(
			Map<String, String> userLogin, Timestamp date) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		// GenericValue treasuryTransfer = null;
		// treasuryTransfer = getTreasuryTransfer(treasuryId);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferOutListToday(
				treasuryId, date);

		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null)
					&& (genericValue.getBigDecimal("transactionAmount") != null)) {
				bdAmountAllocated = bdAmountAllocated.add(genericValue
						.getBigDecimal("transactionAmount"));
			}
		}

		return bdAmountAllocated;
	}

	public static BigDecimal getTotalDeAllocatedToday(
			Map<String, String> userLogin) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");

		// Get amount allocated to Transaction
		BigDecimal bdAmountAllocated = BigDecimal.ZERO;
		// GenericValue treasuryTransfer = null;
		// treasuryTransfer = getTreasuryTransfer(treasuryId);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		List<GenericValue> listTreasuryTransfer = getTreasuryTransferOutListToday(
				treasuryId, new Timestamp(calendar.getTimeInMillis()));

		for (GenericValue genericValue : listTreasuryTransfer) {
			if ((genericValue != null)
					&& (genericValue.getBigDecimal("transactionAmount") != null)) {
				bdAmountAllocated = bdAmountAllocated.add(genericValue
						.getBigDecimal("transactionAmount"));
			}
		}

		return bdAmountAllocated;
	}

	public static String getTreasuryId(Map<String, String> userLogin) {
		// Get Treasury ID
		String partyId = userLogin.get("partyId");
		String treasuryId = getTeller(partyId).getString("treasuryId");
		return treasuryId;
	}

	/***
	 * Check that teller balance is sufficent
	 * **/
	public static Boolean tellerBalanceSufficient(String treasuryId,
			Long memberAccountId, BigDecimal transactionAmount) {

		GenericValue treasury = getTreasury(treasuryId);

		String partyId = treasury.getString("employeeResponsible");

		String userLoginId = getUserLoginId(partyId);
		Map<String, String> userLogin = new HashMap<String, String>();
		userLogin.put("userLoginId", userLoginId);
		userLogin.put("partyId", partyId);

		BigDecimal bdTellerBalanceAmt = getTellerBalanceWithOpeningBalance(userLogin);

		// BigDecimal bdCommissionAmount =
		// AccHolderTransactionServices.getTransactionCommissionAmount(transactionAmount);
		// BigDecimal bdExciseDutyAmount =
		// AccHolderTransactionServices.getTransactionExcideDutyAmount(bdCommissionAmount);

		// BigDecimal bdTotalAmount =
		// transactionAmount.add(bdCommissionAmount).add(bdExciseDutyAmount);

		if (bdTellerBalanceAmt.compareTo(transactionAmount) != -1)
			return true;

		return false;
	}

	public static String getUserLoginId(String partyId) {

		List<GenericValue> userLoginELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			userLoginELI = delegator.findList("UserLogin",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
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
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("treasuryId", treasuryId),
					null, null, null, false);
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

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + treasuryId);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						treasuryId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
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

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + treasuryId);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						treasuryId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
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
			// treasuryTransfer = genericValue;
			treasuryTransferList.add(genericValue);
		}
		return treasuryTransferList;
	}

	/****
	 * Get all transfers In before this date
	 * */
	private static List<GenericValue> getTreasuryTransferInList(
			String treasuryId, Timestamp date) {
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		// Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + treasuryId);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						treasuryId), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.LESS_THAN,
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
			// treasuryTransfer = genericValue;
			treasuryTransferList.add(genericValue);
		}
		return treasuryTransferList;
	}

	/****
	 * Get all transfer out list
	 * 
	 * */
	private static List<GenericValue> getTreasuryTransferOutList(
			String treasuryId, Timestamp date) {
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		// Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + treasuryId);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"sourceTreasury", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, new Timestamp(
										calendar.getTimeInMillis()))),
						EntityOperator.AND);

		try {
			treasuryTransferELI = delegator.findList("TreasuryTransfer",
					transferConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		List<GenericValue> treasuryTransferList = new ArrayList<GenericValue>();
		for (GenericValue genericValue : treasuryTransferELI) {
			// treasuryTransfer = genericValue;
			treasuryTransferList.add(genericValue);
		}
		return treasuryTransferList;
	}

	private static List<GenericValue> getTreasuryTransferOutListToday(
			String treasuryId, Timestamp date) {
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		// Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + treasuryId);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"sourceTreasury", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("createdStamp",
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
			// treasuryTransfer = genericValue;
			treasuryTransferList.add(genericValue);
		}
		return treasuryTransferList;
	}

	/***
	 * Cash Amount Withdrawn today
	 * */
	public static BigDecimal getTotalCashWithdrawal(
			Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashWithdrawalELI = null;
		// CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL")),
						EntityOperator.AND);

		log.info(" ############ Cash withdrawal createdBy : " + createdBy);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			cashWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ############ withdrawals size : " + cashWithdrawalELI.size());

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	public static BigDecimal getTotalCashDeposit(String treasuryId) {
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

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT")),
						EntityOperator.AND);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	public static BigDecimal getTotalCashDepositToday(
			Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT")

						, EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated)), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	
	public static BigDecimal getTotalLoanCashDepositToday(
			Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAY")

						, EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated)), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	public static BigDecimal getTotalCashWithdrawalToday(
			Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashWithdrawalELI = null;
		// CASHWITHDRAWAL
		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWAL"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated)), EntityOperator.AND);

		log.info(" ############ Cash withdrawal createdBy : " + createdBy);

		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			cashWithdrawalELI = delegator.findList("AccountTransaction",
					transactionConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		log.info(" ############ withdrawals size : " + cashWithdrawalELI.size());

		BigDecimal bdBalance = BigDecimal.ZERO;
		for (GenericValue genericValue : cashWithdrawalELI) {
			bdBalance = bdBalance.add(genericValue
					.getBigDecimal("transactionAmount"));
		}
		return bdBalance;
	}

	/****
	 * @author Japheth Odonya @when Sep 16, 2014 8:46:39 PM
	 * 
	 *         Get Teller Name
	 * */
	public static String getTellerName(Map<String, String> userLogin) {
		String tellerName = "";

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");

		GenericValue teller = getTeller(partyId);

		if (teller != null)
			tellerName = teller.getString("name");

		return tellerName;
	}

	public static Boolean hasTellerAssigned(Map<String, String> userLogin) {

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");

		GenericValue teller = getTeller(partyId);

		if (!tellerOpened(userLogin)) {
			return false;
		}

		BigDecimal bdTellerBalance = getTellerBalance(userLogin);

		if (teller != null) // check that the teller assigned is of type teller
		{
			//if (bdTellerBalance.compareTo(BigDecimal.ZERO) == 1) {
				return true;
			//}
		}

		return false;
	}

	public static Boolean tellerOpened(Map<String, String> userLogin) {

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");

		GenericValue teller = getTeller(partyId);

		// Check if there is a transfer for today
		String destinationTreasury = null;

		destinationTreasury = teller.getString("treasuryId");

		Boolean todayTransferExists = false;

		todayTransferExists = todayTransfer(destinationTreasury);

		// BigDecimal bdTellerBalance = getTellerBalance(userLogin);
		//
		// if (teller != null) // check that the teller assigned is of type
		// teller
		// {
		// if (bdTellerBalance.compareTo(BigDecimal.ZERO) == 1) {
		// return true;
		// }
		// }

		return todayTransferExists;
	}

	private static Boolean todayTransfer(String destinationTreasury) {

		/****
		 * 
		 * **/
		List<GenericValue> treasuryTransferELI = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		// Set calendar to truncate current time timestamp to Year, Month and
		// Day only (exclude hour, minute and seconds)
		// Date currentDate = new java.util.Date();
		// Timestamp date = new Timestamp()
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		// Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// UtilDateTime.
		log.info("################## The time is "
				+ new Timestamp(calendar.getTimeInMillis()));
		log.info("################## The treasury is " + destinationTreasury);

		EntityConditionList<EntityExpr> transferConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"destinationTreasury", EntityOperator.EQUALS,
						destinationTreasury), EntityCondition.makeCondition(
						"createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
						new Timestamp(calendar.getTimeInMillis()))),
						EntityOperator.AND);

		try {
			treasuryTransferELI = delegator.findList("TreasuryTransfer",
					transferConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		if ((treasuryTransferELI != null) && (treasuryTransferELI.size() > 0))
			return true;

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

	public static String getTelleAssignee(Map<String, String> userLogin) {
		String tellerAssignee = "";
		String partyId = userLogin.get("partyId");
		log.info("########## The Party is ::: " + partyId);

		// tellerAssignee = partyId;
		// Get the Person with Party ID
		tellerAssignee = getPersonNames(partyId);

		return tellerAssignee;
	}

	public static String getTellerAccountId(Map<String, String> userLogin) {
		String glAccountId = "";

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");
		glAccountId = getTeller(partyId).getString("glAccountId");

		return glAccountId;
	}

	public static String getTellerTreasuryId(Map<String, String> userLogin) {
		String treasuryId = "";

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");
		GenericValue teller = getTeller(partyId);
		
		if (teller != null)
			treasuryId = teller.getString("treasuryId");

		return treasuryId;
	}

	private static String getPersonNames(String partyId) {
		// TODO Auto-generated method stub
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		GenericValue person = null;
		try {
			person = delegator.findOne("Person",
					UtilMisc.toMap("partyId", partyId), false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		// minVal = (a < b) ? a : b;
		String names = "";
		names = (person.getString("firstName") != null) ? names + " "
				+ person.getString("firstName") : names + "";
		names = (person.getString("middleName") != null) ? names + " "
				+ person.getString("middleName") : names + "";
		names = (person.getString("lastName") != null) ? names + " "
				+ person.getString("lastName") : names + "";

		return names;
	}

	public static String getTellerId(Map<String, String> userLogin) {
		String treasuryId = "";

		// Get the teller assigned to this partyId (name for Treasury where
		// employeeResponsible is the guy logged in)
		String partyId = userLogin.get("partyId");
		treasuryId = getTeller(partyId).getString("treasuryId");
		return treasuryId;
	}

	public static Timestamp getEndOfDay(Date transferDate) {

		LocalDate localTransferDate = new LocalDate(transferDate.getTime());
		localTransferDate = localTransferDate.plusDays(1);

		return new Timestamp(localTransferDate.toDate().getTime());
	}

	public static String getFinAccountName(String finAccountId) {
		String finAccountName = "";

		List<GenericValue> finAccountELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			finAccountELI = delegator
					.findList("FinAccount", EntityCondition.makeCondition(
							"finAccountId", finAccountId), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		if (finAccountELI.size() < 1) {
			return null;
		}

		for (GenericValue genericValue : finAccountELI) {
			finAccountName = genericValue.getString("finAccountName") + " - "
					+ genericValue.getString("finAccountCode");
		}

		return finAccountName;
	}

	public static Boolean isTellerOverLimit(Map<String, String> userLogin,
			String treasuryId, BigDecimal transactionAmount) {
		// TODO Auto-generated method stub

		// Date date = new Date(Calendar.getInstance().getTimeInMillis());
		BigDecimal bdTreasuryBalance = getTellerBalanceWithOpeningBalance(userLogin);

		// TreasuryReconciliation.getTotalBalance(treasuryId, date);
		// getTellerBalance(userLogin);
		BigDecimal bdTreasuryLimitAmount = getTellerLimit(treasuryId);

		log.info("TTTTTTTTTTT Teller Limit amount is " + bdTreasuryLimitAmount);
		log.info("TTTTTTTTTTT Treasury Balance amount is " + bdTreasuryBalance);
		log.info("TTTTTTTTTTT Transaction amount is " + transactionAmount);

		BigDecimal bdBalanceAfterTransaction = bdTreasuryBalance
				.add(transactionAmount);
		log.info("TTTTTTTTTTT Balance after transaction amount is "
				+ bdBalanceAfterTransaction);

		if (bdBalanceAfterTransaction.compareTo(bdTreasuryLimitAmount) == 1)
			return true;

		return false;
	}

	/***
	 * @author Japheth Odonya @when Jun 2, 2015 3:28:27 PM Get the limit for
	 *         teller or any other treasy type
	 * */
	public static BigDecimal getTellerLimit(String treasuryId) {
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("treasuryId", treasuryId),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String treasuryTypeId = "";
		for (GenericValue genericValue : treasuryELI) {
			treasuryTypeId = genericValue.getString("treasuryTypeId");
		}

		// get the limit for the treasury type
		List<GenericValue> treasuryTypeELI = null; // =
		try {
			treasuryTypeELI = delegator.findList("TreasuryType",
					EntityCondition.makeCondition("treasuryTypeId",
							treasuryTypeId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		BigDecimal bdTreasuryLimt = BigDecimal.ZERO;
		GenericValue treasuryType = null;
		for (GenericValue genericValue : treasuryTypeELI) {
			treasuryType = genericValue;
		}

		bdTreasuryLimt = treasuryType.getBigDecimal("limitAmount");
		return bdTreasuryLimt;
	}

	public static BigDecimal getTotalLoanCashPay(String treasuryId) {
		Map<String, String> userLogin = new HashMap<String, String>();

		GenericValue treasury = getTreasury(treasuryId);
		String partyId = treasury.getString("employeeResponsible");
		String userLoginId = getUserLoginId(partyId);

		userLogin.put("partyId", partyId);
		userLogin.put("userLoginId", userLoginId);

		return getTotalLoanCashPay(userLogin, treasuryId);
	}
	
	public static BigDecimal getTotalCashWithdrawalReversed(String treasuryId) {
		Map<String, String> userLogin = new HashMap<String, String>();

		GenericValue treasury = getTreasury(treasuryId);
		String partyId = treasury.getString("employeeResponsible");
		String userLoginId = getUserLoginId(partyId);

		userLogin.put("partyId", partyId);
		userLogin.put("userLoginId", userLoginId);

		return getTotalCashWithdrawalReversed(userLogin, treasuryId);
	}

	public static BigDecimal getTotalCashDepositReversed(String treasuryId) {
		Map<String, String> userLogin = new HashMap<String, String>();

		GenericValue treasury = getTreasury(treasuryId);
		String partyId = treasury.getString("employeeResponsible");
		String userLoginId = getUserLoginId(partyId);

		userLogin.put("partyId", partyId);
		userLogin.put("userLoginId", userLoginId);

		return getTotalCashDepositReversed(userLogin, treasuryId);
	}
	
	
	
	/***
	 * CASHWITHDRAWALREVERSED
	 * */
	
	public static BigDecimal getTotalCashWithdrawalReversed(Map<String, String> userLogin, String treasuryId) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWALREVERSED")),
						EntityOperator.AND);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ Cash CASHWITHDRAWALREVERSED createdBy : " + createdBy);
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

	/***
	 * CASHDEPOSITREVERSED
	 * */
	public static BigDecimal getTotalCashDepositReversed(Map<String, String> userLogin, String treasuryId) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSITREVERSED")),
						EntityOperator.AND);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ CASHDEPOSITREVERSED : " + createdBy);
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

	
	//getTotalLoanCashPay
	public static BigDecimal getTotalLoanCashPay(Map<String, String> userLogin, String treasuryId) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAY")),
						EntityOperator.AND);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	
	//CASHLOANPAYREVERSED
	public static BigDecimal getTotalCashLoanPayReversed(Map<String, String> userLogin) {
		// TODO Auto-generated method stub
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		// Timestamp tstampDateCreated = new
		// Timestamp(calendar.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"createdBy", EntityOperator.EQUALS, createdBy),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSIT")),
						EntityOperator.AND);

		// ,
		// EntityCondition
		// .makeCondition("createdStamp",
		// EntityOperator.GREATER_THAN_EQUAL_TO, tstampDateCreated)
		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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

	
	/***
	 * Withdrawals Reversed
	 * 
	 * */
	private static BigDecimal getTotalWithdrawalsReversed(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict, String treasuryId) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHWITHDRAWALREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	
	/***
	 * Loan Cash Repay
	 * */
	private static BigDecimal getTotalLoanCashRepay(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict, String treasuryId) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAY"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	
	/***
	 * Cash Deposit Reversed
	 * **/
	private static BigDecimal getTotalCashDepositReversed(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict, String treasuryId) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "CASHDEPOSITREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
	 * Loan Cash Pay Reversed
	 * 
	 * */
	private static BigDecimal getTotalLoanCashRepayReversed(
			Map<String, String> userLogin, Timestamp transactionDate,
			boolean strict, String treasuryId) {
		String createdBy = userLogin.get("userLoginId");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(transactionDate.getTime());
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(transactionDate.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);

		Timestamp tstampDateCreated = new Timestamp(calendar.getTimeInMillis());

		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		List<GenericValue> cashDepositELI = null;

		EntityConditionList<EntityExpr> transactionConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"treasuryId", EntityOperator.EQUALS, treasuryId),
						EntityCondition.makeCondition("transactionType",
								EntityOperator.EQUALS, "LOANCASHPAYREVERSED"),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.GREATER_THAN_EQUAL_TO,
								tstampDateCreated),

						EntityCondition.makeCondition("createdStamp",
								EntityOperator.LESS_THAN, tstEndDay)

				), EntityOperator.AND);

		log.info(" ############ Cash Deposit createdBy : " + createdBy);
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
