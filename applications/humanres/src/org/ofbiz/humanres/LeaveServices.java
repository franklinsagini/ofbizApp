package org.ofbiz.humanres;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionListBase;
public class LeaveServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);
	public static String getworkflowDocumentTypeIdtry(GenericValue firstLevel){
		String unitId = "";
		String partyId = firstLevel.getString("partyId");
		
		Delegator delegator = firstLevel.getDelegator();
				
		List<GenericValue> getEmplUnitELI = null;
		try {
			getEmplUnitELI = delegator.findList("UnitEmployeeMap",
					EntityConditionListBase("partyId",
							partyId,"unitId", unitId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueUnit : getEmplUnitELI) {
			unitId = genericValueUnit.getString("organizationUnitId");
		}
		/////////////////////////////////////////////////////

				
		
		return unitId;

	}
	public static String getworkflowDocumentTypeId(String documentType){
		
		Map<String, Object> result = FastMap.newInstance();
		log.info("================" + documentType + "================");

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> documentTypeELI = null; // =

		try {
			documentTypeELI = delegator.findList("WorkflowDocumentType",
					EntityCondition.makeCondition("name",
							documentType), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String workflowDocumentTypeId = "";
		for (GenericValue genericValue : documentTypeELI) {
			workflowDocumentTypeId = genericValue.getString("workflowDocumentTypeId");
		}
		
		result.put("workflowDocumentTypeId", workflowDocumentTypeId);
		return workflowDocumentTypeId;
	}
	
	private static EntityCondition EntityConditionListBase(String string,
			String partyId, String string2, String unitId) {
		// TODO Auto-generated method stub
		return null;
	}


	public static String getEmplUnit(GenericValue person){
		String unitId = "";
		String partyId = person.getString("partyId");
		
		Delegator delegator = person.getDelegator();
				
		List<GenericValue> getEmplUnitELI = null;
		try {
			getEmplUnitELI = delegator.findList("UnitEmployeeMap",
					EntityCondition.makeCondition("partyId",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueUnit : getEmplUnitELI) {
			unitId = genericValueUnit.getString("organizationUnitId");
		}
		/////////////////////////////////////////////////////

				
		
		return unitId;

	}
	public static String getLeaveAppointmentDate(GenericValue person){
		String appointmentdate = "";
		
		String partyId = person.getString("partyId");
		
		Delegator delegator = person.getDelegator();
				
		List<GenericValue> getLeaveAppointmentDateELI = null; 
		
	try {
			getLeaveAppointmentDateELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue genericValue : getLeaveAppointmentDateELI) {
			appointmentdate = genericValue.getString("appointmentdate");
		}
		
				
		return appointmentdate;

	}
	
	public static String getpartyIdFrom(GenericValue party){
		String partyIdFromV = "";

		String partyId = party.getString("partyId");
		
		Delegator delegator = party.getDelegator();
				
		List<GenericValue> employmentsELI = null; 

		try {
			employmentsELI = delegator.findList("Employment",
					EntityCondition.makeCondition("partyIdTo",
							partyId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue genericValue : employmentsELI) {
			partyIdFromV = genericValue.getString("partyIdFrom");
		}

		return partyIdFromV;
	}
	
	
	
	
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
