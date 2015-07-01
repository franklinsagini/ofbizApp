package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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

public class GeneratePerformanceTargets {
	
	static ArrayList<String> perfTargets = new ArrayList<String>();
	static ArrayList<String> perfQualGoals = new ArrayList<String>();

private static Logger log = Logger.getLogger(GeneratePerformanceTargets.class);
	
	public static String generatePerfTargets(HttpServletRequest request,
			HttpServletResponse response) {
		

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		
		String perfManagerId = (String) request.getParameter("perfManagerId");
		
	//	String perfReviewId = (String) request.getParameter("perfReviewId");
		
		// Get Performance Review
		List<GenericValue> perfReviewELI = null;
		// String partyId = party.getString("partyId");
		log.info("######### Perf Manager Id is :::: " + perfManagerId);
		
		try {
			perfReviewELI = delegator.findList("PerfReview", EntityCondition
					.makeCondition("perfManagerId", perfManagerId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfReview : perfReviewELI) 
		{

			log.info("######### Review ID " + perfReview.getString("perfReviewId"));
			getExistingTargets(delegator, perfReview.getString("perfReviewId"));
			addTargets(delegator, perfReview);
			/*try
			{
				
			}catch(Exception e)
			{
				log.info("#@#@##@##@##@#@##   " +e.getMessage());
			}*/
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
		return "";
	}

	private static void getExistingTargets(Delegator delegator, String perfReviewId) {
		perfTargets = new ArrayList<String>();
		perfQualGoals = new ArrayList<String>();
		
		List<GenericValue> existingReviewsELI = null;
		try {
			perfTargets = new ArrayList<String>();
			
			existingReviewsELI = delegator.findList("PerfReviewItem", EntityCondition
					.makeCondition("perfReviewId", perfReviewId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue existingReviews : existingReviewsELI) 
		{
			log.info("######### target " + existingReviews.getString("kpiTargetId"));
			perfTargets.add(existingReviews.getString("kpiTargetId"));
			perfQualGoals.add(existingReviews.getString("perfGoalsId"));
		}
		
	}

	private static void addTargets(Delegator delegator, GenericValue perfReview) {
		String perfReviewId= perfReview.getString("perfReviewId");
		
		List<GenericValue> targetELI = null;
		
		/*EntityConditionList<EntityExpr> targetConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
				"branchId", EntityOperator.EQUALS, null),
				EntityCondition.makeCondition("branchId",
						EntityOperator.EQUALS, branchId)),
				EntityOperator.OR);*/
		
		try {
			if(perfReview.getString("reviewType").equals("Branch"))
			{
				log.info("###################### ReviewType =  BRANCH ######################");
				String branchId=getPersonDetails(delegator, perfReview.getString("employeePartyId"), "branchId");				
				targetELI = delegator.findList("KPITargets", EntityCondition
						.makeCondition("branchId", branchId), null,
						null, null, false);
			}
			else if(perfReview.getString("reviewType").equals("Department"))
			{
				log.info("###################### ReviewType = DEPARTMENT ######################");
				String deptId =getPersonDetails(delegator, perfReview.getString("employeePartyId"), "departmentId");
				targetELI = delegator.findList("KPITargets", EntityCondition
						.makeCondition("deptId", deptId), null,
						null, null, false);
			}
			else if(perfReview.getString("reviewType").equals("Position"))
			{
				log.info("###################### ReviewType = POSITION ######################");
				String empPosId = getPersonDetails(delegator, perfReview.getString("employeePartyId"), "emplPositionTypeId");
				targetELI = delegator.findList("KPITargets", EntityCondition
						.makeCondition("emplPositionTypeId", empPosId), null,
						null, null, false);
			}
			else
			{
				log.info("###################### ReviewType = STAFF ######################");
				String partyId = perfReview.getString("employeePartyId");
				
				targetELI = delegator.findList("KPITargets", EntityCondition
						.makeCondition("partyId", partyId), null,
						null, null, false);
			}
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		List<GenericValue> listTargets = new ArrayList<GenericValue>();

		for (GenericValue target : targetELI) 
		{
			if(!(perfTargets.contains(target.getString("kpiTargetId"))))
			{
				listTargets.add(createTargetsToSave(delegator, perfReview.getString("employeePartyId"), perfReviewId, target));
			}
		}
		

		List<GenericValue> qualTargetELI = null;
		try{
			qualTargetELI = delegator.findList("PerfGoals", EntityCondition
					.makeCondition("perfGoalsDefId", "QTT_GOALS"), null,
					null, null, false);
			
		}catch (Exception e) {
			e.printStackTrace();			
		}
		
		for (GenericValue qualTarget : qualTargetELI) 
		{
			if(!(perfQualGoals.contains(qualTarget.getString("perfGoalsId"))))
			{
				listTargets.add(createGoalToSave(delegator, perfReview.getString("employeePartyId"), perfReviewId, qualTarget));
			}
		}
		
		try {
			delegator.storeAll(listTargets);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static GenericValue createGoalToSave(Delegator delegator,
			String partyId, String perfReviewId, GenericValue qualTarget) {
		
		String perfReviewItemSequenceId = delegator.getNextSeqId("PerfReviewItem");		

		GenericValue perfReviewItem = delegator.makeValidValue(
				"PerfReviewItem", UtilMisc.toMap(
						"perfReviewItemSeqId", perfReviewItemSequenceId, 
						"employeePartyId", partyId, 
						"employeeRoleTypeId", "EMPLOYEE", 
						"perfReviewId", perfReviewId, 
						"perfGoalsId", qualTarget.getString("perfGoalsId"),
						"isSubmitted","N"));
/*		try {
			perfReviewItem = delegator.createSetNextSeqId(perfReviewItem);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}*/
		return perfReviewItem;
	}

	private static GenericValue createTargetsToSave(Delegator delegator,
			String partyId, String perfReviewId, GenericValue target) {
		
		String perfReviewItemSequenceId = delegator.getNextSeqId("PerfReviewItem");
		

		GenericValue perfReviewItem = delegator.makeValidValue(
				"PerfReviewItem", UtilMisc.toMap(
						"perfReviewItemSeqId", perfReviewItemSequenceId, 
						"employeePartyId", partyId, 
						"employeeRoleTypeId", "EMPLOYEE", 
						"perfReviewId", perfReviewId, 
						"kpiTargetId", target.getString("kpiTargetId"),
						"actionPlans", target.getString("actionPlans"),
						"kpi", target.getString("kpi"),
						"perfGoalsId", target.getString("perfGoalsId"),
						"isSubmitted","N"));
/*		try {
			perfReviewItem = delegator.createSetNextSeqId(perfReviewItem);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}*/
		return perfReviewItem;
	}

	private static String getPersonDetails(Delegator delegator, String empPartyId, String columnName) {
		String neededId=null;
		List<GenericValue> personELI = null;
		try {
			personELI = delegator.findList("Person", EntityCondition
					.makeCondition("partyId", empPartyId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue person : personELI) 
		{
			neededId= person.getString(columnName);
		}
		
		return neededId;
	}

}
