package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.webapp.event.EventHandlerException;

// org.ofbiz.humanres.AddAllStaffToTraining
public class AddAllStaffToTraining {
	private static Logger log = Logger.getLogger(AddAllStaffToTraining.class);
	
	static ArrayList<String> partyList = new ArrayList<String>();
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    
	
	public static String addAllStaff(HttpServletRequest request,
			HttpServletResponse response) {
		partyList = new ArrayList<String>();
		ArrayList<String> personList = new ArrayList<String>();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		Timestamp fromDate=null;
		Timestamp thruDate=null;
		String approverId = (String) request.getParameter("loginPartyId");
		String workEffortId = (String) request.getParameter("workEffortId");
		String trainingClassTypeId = (String) request.getParameter("trainingClassTypeId");
		String frmDate = (String) request.getParameter("fromDate");
		String thrDate = (String) request.getParameter("thruDate");
		
		try
		{
			if(frmDate!=null)
			{
				Date parsedDate = dateFormat.parse(frmDate);
				fromDate =  new java.sql.Timestamp(parsedDate.getTime());
			}
			if(thrDate!=null)
			{
				Date parsedDate = dateFormat.parse(thrDate);
				thruDate =  new java.sql.Timestamp(parsedDate.getTime());
				Calendar cal = Calendar.getInstance();
				if(thruDate.before(cal.getTime()))
					return "";
			}
			
			
			log.info("######### approverId is :::: " + approverId);
			log.info("######### workEffortId is :::: " + workEffortId);
			log.info("######### trainingClassTypeId is :::: " + trainingClassTypeId);
			log.info("######### fromDate is :::: " + fromDate);
			log.info("######### thruDate is :::: " + thruDate);
			
			
			getPartyList(delegator, trainingClassTypeId, fromDate);
			log.info("######### Party List = :::: " + partyList.size());
			
			List<GenericValue> persELI = null;
			EntityConditionList<EntityExpr> persConditions = EntityCondition
			 .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
			 "employeeNumber", EntityOperator.NOT_EQUAL, ""), 
			 EntityCondition.makeCondition("isSeparated", EntityOperator.NOT_EQUAL,
					 "Y")), EntityOperator.AND);
			try {
				persELI = delegator.findList("Person", persConditions, null,
						null, null, false);
			
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			List<GenericValue> listPartyInfo = new ArrayList<GenericValue>();
			
			for (GenericValue genericValue : persELI) {
				if(!(partyList.contains(genericValue.getString("partyId"))))
				{
					log.info("###########CREATING VALUES############### :::: ");
					
					listPartyInfo.add(createValueToSave(delegator, genericValue.getString("partyId"), approverId,
							workEffortId, trainingClassTypeId, fromDate, thruDate));
				}
						
			}
			try {
				log.info("###########CREATING VALUES.........4############### :::: ");
				delegator.storeAll(listPartyInfo);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}catch(Exception e)
		{
			log.info("");
		}
		
		
		Writer out;
		try {
			out = response.getWriter();
			out.write("");
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}


	private static GenericValue createValueToSave(Delegator delegator,
			String partyId, String approverId, String workEffortId,
			String trainingClassTypeId, Timestamp fromDate, Timestamp thruDate) {
		
		String trainingRequestId = createTrainingReq(delegator);
			
		
		log.info("###########CREATING VALUES.........2############### :::: "+trainingRequestId);
		
		GenericValue personTraining = delegator.makeValidValue(
				"PersonTraining", UtilMisc.toMap(
						"partyId", partyId, 
						"trainingRequestId", trainingRequestId, 
						"trainingClassTypeId", trainingClassTypeId, 
						"workEffortId", workEffortId, 
						"fromDate", fromDate, 
						"thruDate", thruDate,
//						"roleTypeId", "CAL_ATTENDEE",
						"approverId", approverId,
						"approvalStatus", "TRAINING_ASSIGNED"));
		
		try {
			log.info("###########CREATING VALUES.........3############### :::: ");
	//		personTraining = delegator.createSetNextSeqId(personTraining);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return personTraining;
	}


	private static String createTrainingReq(Delegator delegator) {
		String trainingRequestId="";
		try
		{
			trainingRequestId = delegator.getNextSeqId("TrainingRequest");
			
			GenericValue tq = delegator.makeValidValue(
					"TrainingRequest", UtilMisc.toMap(
							"trainingRequestId", trainingRequestId));
			
			try {
				log.info("###########CREATING VALUES.........4############### :::: ");
				delegator.createOrStore(tq);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}catch(Exception e)
		{
			log.info("###########ISSUE ############ :::: ");
		}
		return trainingRequestId;
	}


	private static void getPartyList(Delegator delegator,
			String trainingClassTypeId, Timestamp fromDate) {
		List<GenericValue> ptELI = null;
		
		 EntityConditionList<EntityExpr> ptConditions = EntityCondition
		 .makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
		 "trainingClassTypeId", EntityOperator.EQUALS, trainingClassTypeId), 
		 EntityCondition.makeCondition("fromDate", EntityOperator.EQUALS,
				 fromDate)), EntityOperator.AND);
		
		
		try {
			ptELI = delegator.findList("PersonTraining", ptConditions, null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue genericValue : ptELI) {
			partyList.add(genericValue.getString("partyId"));			
		}
		
	}

}
