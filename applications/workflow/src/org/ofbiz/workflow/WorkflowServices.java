package org.ofbiz.workflow;

import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

/**
 * @author Japheth Odonya  @when Aug 15, 2014 9:01:19 PM
 * 
 * **/
public class WorkflowServices {
	
	public static Logger log = Logger.getLogger(WorkflowServices.class);
	
	/***
	 * @author Japheth Odonya  @when Aug 15, 2014 9:01:33 PM
	 * Get the Organization Unit given the Party/User (partyId)
	 *  The OrganizationUnit is gotten by querying UnitEmployeeMap
	 * */
	public static String getUserOrganizationUnit(String partyId) {

		Map<String, Object> result = FastMap.newInstance();
		log.info("What we got the Party ############ " + partyId);

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> unitEmployeeMapELI = null; // =

		try {
			unitEmployeeMapELI = delegator.findList("UnitEmployeeMap",
					EntityCondition.makeCondition("partyId",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String organizationUnitId = "";
		for (GenericValue genericValue : unitEmployeeMapELI) {
			organizationUnitId = genericValue.getString("organizationUnitId");
		}
		
		result.put("organizationUnitId", organizationUnitId);
		return organizationUnitId;
	}
	
	/***
	 * Get the document workflow ID given the document name (this can either be LEAVE, LOAN, INVOICE etc)
	 * **/
	public static String getWorkflowDocumentType(String documentName){
		return null;
	}
}
