package org.ofbiz.msaccomanagement;

import java.util.List;

import org.ofbiz.atmmanagement.ATMManagementServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

public class MSaccoManagementServices {

	public static Long getMemberAccountId(String phoneNumber) {
		Long memberAccountId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long cardStatusId = ATMManagementServices.getCardStatus("ACTIVE");
		List<GenericValue> msaccoApplicationELI = null;
		EntityConditionList<EntityExpr> msaccoApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("mobilePhoneNumber",
								EntityOperator.EQUALS, phoneNumber),
						EntityCondition.makeCondition("cardStatusId",
								EntityOperator.EQUALS, cardStatusId)),
						EntityOperator.AND);

		try {
			msaccoApplicationELI = delegator.findList("MSaccoApplication",
					msaccoApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		GenericValue msaccoApplication = null;

		for (GenericValue genericValue : msaccoApplicationELI) {
			msaccoApplication = genericValue;
		}

		if (msaccoApplication != null) {
			memberAccountId = msaccoApplication.getLong("memberAccountId");
		}
		return memberAccountId;
	}

	public static MSaccoStatus getMSaccoAccount(String phoneNumber) {
		String status = "";
		
		GenericValue msaccoApplication = getMemberMsaccoApplication(phoneNumber);
		
		Long cardStatusId = null;
		cardStatusId = ATMManagementServices.getCardStatus("ACTIVE");
		if (msaccoApplication == null){
			status = "NOTREGISTERED";
		} else if (msaccoApplication.getLong("cardStatusId").equals(cardStatusId)){
			status = "SUCCESS";
		} else{
			status = "NOTACTIVE";
		}
		
		MSaccoStatus msaccoStatus = new MSaccoStatus();
		msaccoStatus.setStatus(status);
		msaccoStatus.setMsaccoApplication(msaccoApplication);
		
		return msaccoStatus;
	}

	/***
	 * Get MSacco Application given a phone number
	 * 
	 * Either Returns an MSacco Application or Null if none exists
	 * */
	private static GenericValue getMemberMsaccoApplication(String phoneNumber) {
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> msaccoApplicationELI = null;
		EntityConditionList<EntityExpr> msaccoApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("mobilePhoneNumber",
								EntityOperator.EQUALS, phoneNumber)),
						EntityOperator.AND);

		try {
			msaccoApplicationELI = delegator.findList("MSaccoApplication",
					msaccoApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		GenericValue msaccoApplication = null;

		for (GenericValue genericValue : msaccoApplicationELI) {
			msaccoApplication = genericValue;
		}
		return msaccoApplication;
	}
	
	public static Long getCardStatusId(String name) {
		List<GenericValue> cardStatusELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			cardStatusELI = delegator.findList("CardStatus",
					EntityCondition.makeCondition("name", name), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		Long cardStatusId = 0L;
		for (GenericValue genericValue : cardStatusELI) {
			cardStatusId = genericValue.getLong("cardStatusId");
		}

		
		return cardStatusId;
	}
	
	public static Long getMemberId(String phoneNumber) {
		Long memberId = null;
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		Long cardStatusId = ATMManagementServices.getCardStatus("ACTIVE");
		List<GenericValue> msaccoApplicationELI = null;
		EntityConditionList<EntityExpr> msaccoApplicationConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition
						.makeCondition("mobilePhoneNumber",
								EntityOperator.EQUALS, phoneNumber),
						EntityCondition.makeCondition("cardStatusId",
								EntityOperator.EQUALS, cardStatusId)),
						EntityOperator.AND);

		try {
			msaccoApplicationELI = delegator.findList("MSaccoApplication",
					msaccoApplicationConditions, null, null, null, false);

		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		GenericValue msaccoApplication = null;

		for (GenericValue genericValue : msaccoApplicationELI) {
			msaccoApplication = genericValue;
		}

		if (msaccoApplication != null) {
			memberId = msaccoApplication.getLong("partyId");
		}
		return memberId;
	}
	

}
