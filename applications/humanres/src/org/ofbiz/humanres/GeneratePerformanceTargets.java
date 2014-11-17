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
			setExistingTargets(delegator, perfReview.getString("perfReviewId"));
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

	private static void setExistingTargets(Delegator delegator, String perfReviewId) {
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
		}
		
	}

	private static void addTargets(Delegator delegator, GenericValue perfReview) {
		String perfReviewId= perfReview.getString("perfReviewId");
		String branchId=getBranch(delegator, perfReview.getString("employeePartyId"));
		List<GenericValue> targetELI = null;
		
		EntityConditionList<EntityExpr> targetConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
				"branchId", EntityOperator.EQUALS, null),
				EntityCondition.makeCondition("branchId",
						EntityOperator.EQUALS, branchId)),
				EntityOperator.OR);
		
		try {
			targetELI = delegator.findList("KPITargets",targetConditions, null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		List<GenericValue> listTargets = new ArrayList<GenericValue>();

		for (GenericValue target : targetELI) 
		{
			if(!(perfTargets.contains(target.getString("kpiTargetId"))))
			{
				listTargets.add(createTargetsToSave(delegator, perfReview.getString("employeePartyId"), perfReviewId, target.getString("kpiTargetId")));
			}
		}
		try {
			delegator.storeAll(listTargets);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static GenericValue createTargetsToSave(Delegator delegator,
			String partyId, String perfReviewId, String kpitargetId) {
		
		String perfReviewItemSequenceId = delegator.getNextSeqId("PerfReviewItem");
//		String perfReviewItemSequenceId = delegator.getNextSeqId("");
		

		GenericValue perfReviewItem = delegator.makeValidValue(
				"PerfReviewItem", UtilMisc.toMap(
						"perfReviewItemSeqId", perfReviewItemSequenceId, 
						"employeePartyId", partyId, 
						"employeeRoleTypeId", "EMPLOYEE", 
						"perfReviewId", perfReviewId, 
						"kpiTargetId", kpitargetId,
						"isSubmitted","N"));
/*		try {
			perfReviewItem = delegator.createSetNextSeqId(perfReviewItem);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}*/
		return perfReviewItem;
	}

	private static String getBranch(Delegator delegator, String partyId) {
		String branchId=null;
		List<GenericValue> personELI = null;
		try {
			personELI = delegator.findList("Person", EntityCondition
					.makeCondition("partyId", partyId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		

		for (GenericValue person : personELI) 
		{
			branchId= person.getString("branchId");
		}
		
		return branchId;
	}

}
