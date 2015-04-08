package org.ofbiz.treasurymanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
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
	
}
