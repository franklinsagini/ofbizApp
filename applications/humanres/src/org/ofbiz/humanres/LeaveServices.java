package org.ofbiz.humanres;

import java.util.List;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

public class LeaveServices {
	
	public static String getSupervisorLevel(GenericValue party){
		String superVisorLevelValue = "";
		
		//GenericValue superVisorLevel = null;
		String partyId = party.getString("partyId");
		
		Delegator delegator = party.getDelegator();
		
//		try {
//			superVisorLevel = delegator.findOne("SupervisorLevel",
//					UtilMisc.toMap("partyId", partyId), false);
//		} catch (GenericEntityException e2) {
//			e2.printStackTrace();
//		}
		
		List<GenericValue> levelsELI = null; // =

		try {
			levelsELI = delegator.findList("SupervisorLevel",
					EntityCondition.makeCondition("partyId",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue genericValue : levelsELI) {
			superVisorLevelValue = genericValue.getString("supervisorLevel");
		}
		
		//superVisorLevelValue = superVisorLevel.getString("supervisorLevel");
		
		return superVisorLevelValue;
	}

}
