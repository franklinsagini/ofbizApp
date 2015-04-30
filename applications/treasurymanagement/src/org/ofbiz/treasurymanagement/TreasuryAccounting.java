package org.ofbiz.treasurymanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.accountholdertransactions.AccHolderTransactionServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanAccounting;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

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
		//String partyId = "Company";
		String partyId = employeePartyId;
	
		String organizationPartyId = getEmployeeOrganizationPartyId(partyId);
		
		String acctgTransId = 	AccHolderTransactionServices.createAccountingTransaction(treasuryTransfer, acctgTransType, userLogin);
		Delegator delegator = treasuryTransfer.getDelegator();
		
		//Credit Source
		String postingType = "C";
		String entrySequenceId = "00001";
		//LoanAccounting.postTransactionEntry(delegator, transactionAmount, partyId, sourceAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		//LoanAccounting.postTransactionEntryParty(delegator, bdLoanAmount, partyId, organizationPartyId, loanReceivableAccount, postingType, acctgTransId, acctgTransType, entrySequenceId);
		LoanAccounting.postTransactionEntryParty(delegator, transactionAmount, partyId, organizationPartyId, sourceAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);

		//Debit Destination
		postingType = "D";
		entrySequenceId = "00002";
		//LoanAccounting.postTransactionEntry(delegator, transactionAmount, partyId, destinationAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		LoanAccounting.postTransactionEntryParty(delegator, transactionAmount, partyId, organizationPartyId,  destinationAccountId, postingType, acctgTransId, acctgTransType, entrySequenceId);
		return "posted";
	}


	/***
	 * Get an employee's Organization/Branch ID
	 * */
	private static String getEmployeeOrganizationPartyId(String partyId) {
		List<GenericValue> personELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			personELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		GenericValue person = null;
		for (GenericValue genericValue : personELI) {
			person = genericValue;
		}
		
		if (person == null)
			return null;
		
		String branchId = person.getString("branchId");
		
		if (branchId != null)
			branchId = branchId.trim();
		
		log.info("MMMMMMMMMMMMMMMMMMMMM The Branch ID is MMMMMMMMMMMMMMMMM <<<<<<<<<<< "+branchId);
		
		
		return branchId;
	}
	
	/****
	 * Check that the Employee Has an Organization/Branch
	 * */
	public static Boolean hasEmployeeOrganizationPartyId(Map<String, String> userLogin){
		String branchId = null;
		
		String partyId = userLogin.get("userLoginId");
		branchId = getEmployeeOrganizationPartyId(partyId);
		
		if ((branchId != null) && (!branchId.equals("")))
			return true;
		
		return false;
	}

	
	/****
	 * Check that the Employee Has an Organization/Branch
	 * */
	public static Boolean accountsMappedToEmployeeOrganizationPartyId(GenericValue treasuryTransfer, Map<String, String> userLogin){
		String branchId = null;
		
		String partyId = userLogin.get("partyId");
		log.info("PPPPPPPPPPPPPPPPPP Party ID is "+partyId);
		branchId = getEmployeeOrganizationPartyId(partyId);
		
		log.info("BBBBBBBBBBBBBBBBBBBB Branch ID is "+branchId);
		
		if ((branchId == null) || (branchId.equals("")))
			return false;
		
		String sourceTreasury = treasuryTransfer.getString("sourceTreasury");
		String destinationTreasury = treasuryTransfer.getString("destinationTreasury");
		
		log.info("SSSSSSSSSSSSSSSS Source Treasury is "+sourceTreasury);
		log.info("DDDDDDDDDDDDDDDD Destination Treasury is "+destinationTreasury);

		
		String sourceTreasuryAccountId = getTansferAccount(sourceTreasury);
		String destinationTreasuryId = getTansferAccount(destinationTreasury);
		
		log.info("SSSSSSSSSSSSSSSS Source ACC ID is "+sourceTreasuryAccountId);
		log.info("DDDDDDDDDDDDDDDD Destination ACC ID is "+destinationTreasuryId);

		
		if (!organizationAccountMapped(sourceTreasuryAccountId, branchId))
		{
			log.info("DDDDDDDDDDDDDDDD Destination Not mapped "+destinationTreasuryId);
			return false;
		}

		if (!organizationAccountMapped(destinationTreasuryId, branchId))
		{
			log.info("SSSSSSSSSSSSSSSS Source Not mapped "+destinationTreasuryId);
			return false;
		}
		
		log.info("AAAAAAAAAAAAAAAAAA The two accounts are mapped "+destinationTreasuryId);

		
		return true;
	}


	/**
	 * Check that the account specified is mapped to the branch specified
	 * **/
	public static boolean organizationAccountMapped(
			String treasuryAccountId, String branchId) {
		
		List<GenericValue> glAccountOrganizationELI = null;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> glAccountOrganizationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("glAccountId", EntityOperator.EQUALS,
								treasuryAccountId),
						EntityCondition
								.makeCondition("organizationPartyId", EntityOperator.EQUALS,
										branchId)
								

				), EntityOperator.AND);

		try {
			glAccountOrganizationELI = delegator.findList("GlAccountOrganization", glAccountOrganizationConditions, null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if ((glAccountOrganizationELI == null) || (!(glAccountOrganizationELI.size() > 0)))
			return false;
		
		return true;
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
		
		
		
		//Get glAccountId from Treasury Type
		String accountId = treasury.getString("glAccountId");
		return accountId;
	}
	
	//accountHasBeenUsed
	public static String accountHasBeenUsed(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String glAccountId = (String) request.getParameter("glAccountId");
		
		log.info(" TTTTTTTTTTTTTTTT The Account is IIIIIIIII "+glAccountId);
		Boolean usedState = false;
		usedState = accountIdUsed(glAccountId);

		result.put("usedState", usedState);
		

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}



	
	public static Boolean accountIdUsed(String glAccountId) {
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("glAccountId",
							glAccountId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (treasuryELI.size() > 0)
			return true;
		
		return false;
	}
	
	//employeeHasBeenGivenTreasury
	public static String employeeHasBeenGivenTreasury(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String employeeResponsible = (String) request.getParameter("employeeResponsible");
		
		Boolean usedState = false;
		usedState = employeeAssigned(employeeResponsible);

		result.put("usedState", usedState);
		

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}


	private static Boolean employeeAssigned(String employeeResponsible) {
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("employeeResponsible",
							employeeResponsible), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if (treasuryELI.size() > 0)
			return true;
		
		return false;
	}
	
	//"getAssignedEmployee"
	public static String getAssignedEmployee(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String destinationTreasury = (String) request.getParameter("destinationTreasury");
		
		GenericValue employee;
		employee = getEmployee(destinationTreasury);
		
		String employeeResponsible = employee.getString("partyId");
		String employeeNames = employee.getString("firstName")!=null?employee.getString("firstName"):""+" "+employee.getString("middleName")!=null?employee.getString("middleName"):""+" "+employee.getString("lastName")!=null?employee.getString("lastName"):"";

//		employeeResponsible = data.employeeResponsible;
//		employeeNames = data.employeeNames;
		result.put("employeeResponsible", employeeResponsible);
		result.put("employeeNames", employeeNames);
		
		//Get Treasury type limit
		BigDecimal bdLimitAmount = getTreasuryTypeLimit(destinationTreasury);
		
		BigDecimal bdTreasuryAvailable = null;
		
		//if (bdLimitAmount != null)
			bdTreasuryAvailable = TreasuryReconciliation.getNetAllocation(destinationTreasury);
		
		BigDecimal bdMaxAllowedTransfer = null;
		
		if (bdLimitAmount != null)
			bdMaxAllowedTransfer = bdLimitAmount.subtract(bdTreasuryAvailable);
		
		result.put("bdLimitAmount", bdLimitAmount);
		result.put("bdTreasuryAvailable", bdTreasuryAvailable);
		result.put("bdMaxAllowedTransfer", bdMaxAllowedTransfer);
		
		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}


	private static BigDecimal getTreasuryTypeLimit(String destinationTreasury) {
		//Get the treasury type
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("treasuryId",
							destinationTreasury), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String treasuryTypeId = "";
		for (GenericValue genericValue : treasuryELI) {
			treasuryTypeId = genericValue.getString("treasuryTypeId");
		}
		
		//get the limit for the treasury type
		List<GenericValue> treasuryTypeELI = null; // =
		try {
			treasuryTypeELI = delegator.findList("TreasuryType",
					EntityCondition.makeCondition("treasuryTypeId",
							treasuryTypeId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		BigDecimal bdLimitAmount = BigDecimal.ZERO;
		for (GenericValue genericValue : treasuryTypeELI) {
			bdLimitAmount = genericValue.getBigDecimal("limitAmount");
		}
		
		return bdLimitAmount;
	}


	private static GenericValue getEmployee(String destinationTreasury) {
		
		//Get the employee assigned to this treasury
		GenericValue employee = null;
		
		
		//Get the employee responsible given a treasury
		//
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("treasuryId",
							destinationTreasury), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String employeeResponsible = "";
		//Get the ID of the employee responsible (employeeResponsible)
		for (GenericValue genericValue : treasuryELI) {
			employeeResponsible = genericValue.getString("employeeResponsible");
		}
		
		//Given the employeeResponsible find person
		List<GenericValue> personELI = null; // =
		//Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			personELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId",
							employeeResponsible), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		//Get the person and return
		for (GenericValue genericValue : personELI) {
			employee = genericValue;
		}
		
		
		return employee;
	}
	
	
	public static BigDecimal getAccountBalance(String glAccountId) {
		// TODO Check if acctgTransId has correct entries (Debits and Credits
		// are equal)
		BigDecimal bdTotalDebits = getTotalEntries(glAccountId, "D");
		BigDecimal bdTotalCredits = getTotalEntries(glAccountId, "C");
		
		BigDecimal bdTotalBalance = bdTotalDebits.subtract(bdTotalCredits);
		
		
		return bdTotalBalance;
	}
	
	
	public static BigDecimal getAccountBalance(String glAccountId, Timestamp transactionDate) {
		// TODO Check if acctgTransId has correct entries (Debits and Credits
		// are equal)
		BigDecimal bdTotalDebits = getTotalEntries(glAccountId, "D", transactionDate);
		BigDecimal bdTotalCredits = getTotalEntries(glAccountId, "C", transactionDate);
		
		BigDecimal bdTotalBalance = bdTotalDebits.subtract(bdTotalCredits);
		
		
		return bdTotalBalance;
	}
	
	public static BigDecimal getAccountBalance(String glAccountId, Timestamp transactionDate, boolean strict) {
		// TODO Check if acctgTransId has correct entries (Debits and Credits
		// are equal)
		BigDecimal bdTotalDebits = getTotalEntries(glAccountId, "D", transactionDate, strict);
		BigDecimal bdTotalCredits = getTotalEntries(glAccountId, "C", transactionDate, strict);
		
		BigDecimal bdTotalBalance = bdTotalDebits.subtract(bdTotalCredits);
		
		
		return bdTotalBalance;
	}

	private static BigDecimal getTotalEntries(String glAccountId, String debitCreditFlag) {
		BigDecimal bdTotalEntryAmt = BigDecimal.ZERO;
		List<GenericValue> listEntries = null;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> entriesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("glAccountId", EntityOperator.EQUALS,
								glAccountId),
						EntityCondition
								.makeCondition("debitCreditFlag", EntityOperator.EQUALS,
										debitCreditFlag)
								

				), EntityOperator.AND);

		try {
			listEntries = delegator.findList("AcctgTransEntry", entriesConditions, null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : listEntries) {
			bdTotalEntryAmt = bdTotalEntryAmt.add(genericValue.getBigDecimal("amount"));
		}
		
		//bdTotalEntryAmt = bdTotalEntryAmt.
		return bdTotalEntryAmt.setScale(4, RoundingMode.HALF_UP);
	}
	
	public static BigDecimal getTotalEntries(String glAccountId, String debitCreditFlag, Timestamp date) {
		BigDecimal bdTotalEntryAmt = BigDecimal.ZERO;
		List<GenericValue> listEntries = null;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> entriesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("glAccountId", EntityOperator.EQUALS,
								glAccountId),
						EntityCondition
								.makeCondition("debitCreditFlag", EntityOperator.EQUALS,
										debitCreditFlag),
										
										EntityCondition
										.makeCondition("createdStamp", EntityOperator.LESS_THAN,
												date)
								

				), EntityOperator.AND);

		try {
			listEntries = delegator.findList("AcctgTransEntry", entriesConditions, null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : listEntries) {
			bdTotalEntryAmt = bdTotalEntryAmt.add(genericValue.getBigDecimal("amount"));
		}
		
		//bdTotalEntryAmt = bdTotalEntryAmt.
		return bdTotalEntryAmt.setScale(4, RoundingMode.HALF_UP);
	}
	
	
	public static BigDecimal getTotalEntries(String glAccountId, String debitCreditFlag, Timestamp date, boolean strict) {
		BigDecimal bdTotalEntryAmt = BigDecimal.ZERO;
		List<GenericValue> listEntries = null;
		Delegator delegator =  DelegatorFactoryImpl.getDelegator(null);
		
		//End of the day
		Calendar calEndDay = Calendar.getInstance();
		calEndDay.setTimeInMillis(date.getTime());
		calEndDay.add(Calendar.DATE, 1);
		calEndDay.set(Calendar.MILLISECOND, 0);
		calEndDay.set(Calendar.SECOND, 0);
		calEndDay.set(Calendar.MINUTE, 0);
		calEndDay.set(Calendar.HOUR_OF_DAY, 0);


		Timestamp tstEndDay = new Timestamp(calEndDay.getTimeInMillis());

		
		EntityConditionList<EntityExpr> entriesConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("glAccountId", EntityOperator.EQUALS,
								glAccountId),
						EntityCondition
								.makeCondition("debitCreditFlag", EntityOperator.EQUALS,
										debitCreditFlag),
										
										EntityCondition
										.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO,
												date),
												
												EntityCondition
												.makeCondition("createdStamp", EntityOperator.LESS_THAN,
														tstEndDay)
								

				), EntityOperator.AND);

		try {
			listEntries = delegator.findList("AcctgTransEntry", entriesConditions, null,
					null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : listEntries) {
			bdTotalEntryAmt = bdTotalEntryAmt.add(genericValue.getBigDecimal("amount"));
		}
		
		//bdTotalEntryAmt = bdTotalEntryAmt.
		return bdTotalEntryAmt.setScale(4, RoundingMode.HALF_UP);
	}
	
	//destinationIsBank
	public static String destinationIsBank(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String destinationTreasury = (String) request.getParameter("destinationTreasury");
		
		Boolean isBank = false;
		isBank = destinationIsBank(destinationTreasury);

		result.put("isBank", isBank);
		

		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}


	private static Boolean destinationIsBank(String destinationTreasury) {
		//Get the treasury type
		List<GenericValue> treasuryELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			treasuryELI = delegator.findList("Treasury",
					EntityCondition.makeCondition("treasuryId",
							destinationTreasury), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String treasuryTypeId = "";
		for (GenericValue genericValue : treasuryELI) {
			treasuryTypeId = genericValue.getString("treasuryTypeId");
		}
		
		//get the limit for the treasury type
		List<GenericValue> treasuryTypeELI = null; // =
		try {
			treasuryTypeELI = delegator.findList("TreasuryType",
					EntityCondition.makeCondition("treasuryTypeId",
							treasuryTypeId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		String name = "";
		for (GenericValue genericValue : treasuryTypeELI) {
			name = genericValue.getString("name");
		}
		
		name = name.toUpperCase();
		
		if (name.equals("BANK"))
			return true;
		
		return false;
	}
	
}
