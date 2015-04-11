package org.ofbiz.party.party;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

//org.ofbiz.party.party.SaccoUtility.getNextSequenc()

//org.ofbiz.party.party.getMemberStatusID.SaccoUtility(name)
public class SaccoUtility {
	
	public static Log log = LogFactory.getLog(SaccoUtility.class);
	
	public static Long getNextSequenc(String sequenceName){
		Long nextId = 0L;
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		nextId = delegator.getNextSeqIdLong(sequenceName);
		
		return nextId;
	}
	
	public static Long getNextPartySequence(String sequenceName){
		String nextId = "";
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		nextId = delegator.getNextSeqId(sequenceName);
		
		return Long.valueOf(nextId);
	}
	
	public static String getNextStringSequence(String sequenceName){
		String nextId = "";
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		nextId = delegator.getNextSeqId(sequenceName);
		
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

	public static Integer getBusinessMemberCount() {
		Long memberTypeId = getMemberTypeId("BUSINESS");
		
		//Get the count of member of type Business
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("memberTypeId",
							memberTypeId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		
		
		return memberELI.size();
	}
	
	
	public static Long getMemberTypeId(String name) {
		List<GenericValue> memberTypeELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberTypeELI = delegator.findList("MemberType",
					EntityCondition.makeCondition("name",
							name), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		Long memberTypeId = 0L;
		 for (GenericValue genericValue : memberTypeELI) {
			 memberTypeId = genericValue.getLong("memberTypeId");
		 }
		return memberTypeId;
	}
	
	
	//value - Mother columnName name, entityName Relationship
	public static Boolean itemExists(String value, String columnName, String entityName) {
		List<GenericValue> entityListELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		log.info("##########VVVVVVVVVV Value :: "+value+" Column Name "+columnName+" Entity Name "+entityName);
		
		try {
			entityListELI = delegator.findList(entityName,
					EntityCondition.makeCondition(columnName,
							value), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		log.info(" SSSSSSSSSSSS The Size is "+entityListELI.size());
		
		if (entityListELI.size() > 0)
			return true;

		return false;
	}

}
