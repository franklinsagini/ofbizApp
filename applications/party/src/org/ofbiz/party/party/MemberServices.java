package org.ofbiz.party.party;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

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
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class MemberServices {
	
	public static Logger log = Logger.getLogger(MemberServices.class);
	
	public static String generateAccountNumber(HttpServletRequest request,
			HttpServletResponse response){
		
		String partyId = (String) request.getParameter("partyId");
		String accountProductId = (String) request.getParameter("accountProductId");
		
		String accountNumber = "";
		
		//Get Account 
		/**
		 *  Branch Code - 3
		 *	Account Code - 2
		 *	Member No - 7
		 *	Sequence - 2
		 * */
		String branchCode = getBranchCode(partyId);
		 String accountProductCode = getProductCode(accountProductId);
		 
		 String memberNumber = getMemberNumber(partyId);
		 
		 String sequenceNo = getSequence(partyId, accountProductId);
		 
		 accountNumber = branchCode+accountProductCode+memberNumber+sequenceNo;
		 
		 Map<String, Object> result = FastMap.newInstance();

		 result.put("accountNumber", accountNumber);
		 
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

	/***
	 * @author Japheth Odonya  @when Oct 5, 2014 1:59:42 PM
	 * 
	 * Returns the sequence number prepended with zeros to meet the 2 digits requirement
	 * */
	private static String getSequence(String partyId, String accountProductId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> memberAccountELI = null;
		partyId = partyId.replaceAll(",", "");
		accountProductId = accountProductId.replaceAll(",", "");
		EntityConditionList<EntityExpr> accountsConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId)),
						EntityCondition.makeCondition("accountProductId",
								EntityOperator.EQUALS, Long.valueOf(accountProductId))),
						EntityOperator.AND);

		try {
			memberAccountELI = delegator.findList("MemberAccount",
					accountsConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		//Count the accounts for this product
		int count = 0;
		for (GenericValue genericValue : memberAccountELI) {
			count = count+1;
		}
		
		int sequence = count + 1;
		int padDigits = 2;
		String sequenceCode = paddString(padDigits, String.valueOf(sequence));
		return sequenceCode;
	}

	/**
	 * @author Japheth Odonya  @when Oct 5, 2014 1:58:57 PM
	 * 
	 * Returns the Member Number prepended with zeros to meet 7 digits requirement
	 * **/
	private static String getMemberNumber(String partyId) {
		GenericValue member = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		partyId = partyId.replaceAll(",", "");
		try {
			member = delegator.findOne("Member",
					UtilMisc.toMap("partyId", Long.valueOf(partyId)),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Find Member");
		}
		
		String memberNumber = member.getString("memberNumber");
		
		int padDigits = 7;
		String paddedMemberNumber = paddString(padDigits, memberNumber);
		return paddedMemberNumber;
	}

	/***
	 *@author Japheth Odonya  @when Oct 5, 2014 1:58:03 PM
	 *
	 * Returns the Product Code prepended with zeros to meet the 2 digits requirement
	 * */
	private static String getProductCode(String accountProductId) {
		
		GenericValue accountProduct = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		accountProductId = accountProductId.replaceAll(",", "");
		try {
			accountProduct = delegator.findOne("AccountProduct",
					UtilMisc.toMap("accountProductId", Long.valueOf(accountProductId)),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Find Account Product");
		}
		
		String code = accountProduct.getString("code");
		int padDigits = 2;
		String accountProductCode = paddString(padDigits, code);
		return accountProductCode;
	}

	/***
	 * @author Japheth Odonya  @when Oct 5, 2014 1:57:01 PM
	 * 
	 * Returns the branch code prepended with zeros to meet the 3 digits requirements
	 * */
	private static String getBranchCode(String partyId) {
		// TODO Auto-generated method stub
		List<GenericValue> memberELI = null;
		partyId = partyId.replaceAll(",", "");
		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId))),
						EntityOperator.AND);
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);

		try {
			memberELI = delegator.findList("Member",
					memberConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		GenericValue member = null;
		for (GenericValue genericValue : memberELI) {
			member = genericValue;
		}
		
		String branchId = member.getString("branchId");
		
		GenericValue branch = getBranch(branchId);
		
		String branchCode = branch.getString("code");
		
		//Prepend to meet 3 digits
		int padDigits = 3;
		String paddedBranchCode = paddString(padDigits, branchCode);
		
		return paddedBranchCode;
	}

	public static String paddString(int padDigits, String branchCode) {
		String padded = String.format("%"+padDigits+"s", branchCode).replace(' ', '0');
		return padded;
	}

	/***
	 * Returns Branch Entity
	 * */
	private static GenericValue getBranch(String branchId) {
		GenericValue branch = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		try {
			branch = delegator.findOne("PartyGroup",
					UtilMisc.toMap("partyId", branchId),
					false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			log.info("Cannot Find Branch");
		}

		 
		return branch;
	}
	
	public static Long getMemberStatusId(String name) {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long memberStatusId = 0L;
		for (GenericValue genericValue : memberStatusELI) {
			memberStatusId = genericValue.getLong("memberStatusId");
		}

		String statusIdString = String.valueOf(memberStatusId);
		statusIdString = statusIdString.replaceAll(",", "");
		memberStatusId = Long.valueOf(statusIdString);
		return memberStatusId;
	}

}
