package org.ofbiz.party.party;

import java.util.List;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

//org.ofbiz.party.party.SaccoUtility.getNextSequenc()

//org.ofbiz.party.party.getMemberStatusID.SaccoUtility(name)
public class SaccoUtility {
	
	public static Long getNextSequenc(String sequenceName){
		Long nextId = 0L;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		nextId = delegator.getNextSeqIdLong(sequenceName);
		
		return nextId;
	}
	
	//Get memberStatusId
	public static Long getMemberStatusID(String name) {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name",
							name), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		Long memberStatusId = 0L;
		 for (GenericValue genericValue : memberStatusELI) {
			 memberStatusId = genericValue.getLong("memberStatusId");
		 }

		return memberStatusId;
	}
	
	/**
	 * getMemberStatusActiveId
		getMemberStatusNewId
	 * 
	 * */
	public static Long getMemberStatusActiveId() {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name",
							"ACTIVE"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		Long memberStatusId = 0L;
		 for (GenericValue genericValue : memberStatusELI) {
			 memberStatusId = genericValue.getLong("memberStatusId");
		 }

		return memberStatusId;
	}
	
	public static Long getMemberStatusNewId() {
		List<GenericValue> memberStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberStatusELI = delegator.findList("MemberStatus",
					EntityCondition.makeCondition("name",
							"NEW"), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		Long memberStatusId = 0L;
		 for (GenericValue genericValue : memberStatusELI) {
			 memberStatusId = genericValue.getLong("memberStatusId");
		 }

		return memberStatusId;
	}
	
	/***
	 * Get Loan Status
	 * */
	
	public static Long getLoanStatusId(String name) {
		List<GenericValue> loanStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			loanStatusELI = delegator.findList("LoanStatus",
					EntityCondition.makeCondition("name",
							name), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		Long loanStatusId = 0L;
		 for (GenericValue genericValue : loanStatusELI) {
			 loanStatusId = genericValue.getLong("loanStatusId");
		 }
		 
		 String statusIdString = String.valueOf(loanStatusId);
		 statusIdString = statusIdString.replaceAll(",", "");
		 loanStatusId = Long.valueOf(statusIdString);
		return loanStatusId;
	}
	
	public static String replaceString(String name) {
		name = name.replaceAll(",", "");

		return name;
	}

}
