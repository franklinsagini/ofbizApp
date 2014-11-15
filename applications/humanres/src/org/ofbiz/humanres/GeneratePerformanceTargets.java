package org.ofbiz.humanres;

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
import org.ofbiz.payroll.ClosePayroll;

public class GeneratePerformanceTargets {
private static Logger log = Logger.getLogger(ClosePayroll.class);
	
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
			addTargets(delegator, perfReview);
		}
		return null;
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
			listTargets.add(createTargetsToSave(delegator, perfReview.getString("employeePartyId"), perfReviewId, target.getString("kpiTargetId")));
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

		GenericValue perfReviewItem = delegator.makeValidValue(
				"PerfReviewItem", UtilMisc.toMap(
						"perfReviewItemSeqId", perfReviewItemSequenceId, 
						"employeePartyId", partyId, 
						"employeeRoleTypeId", "EMPLOYEE", 
						"perfReviewId", perfReviewId, 
						"kpiTargetId", kpitargetId));
		try {
			perfReviewItem = delegator.createSetNextSeqId(perfReviewItem);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
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
