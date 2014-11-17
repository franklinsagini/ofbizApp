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

public class SubmitAppraisals {

	static ArrayList<String> subs = new ArrayList<String>();
private static Logger log = Logger.getLogger(GeneratePerformanceTargets.class);
	
	public static String submitScores(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");		
		
		String perfReviewId = (String) request.getParameter("perfReviewId");
		
		// Get Performance Review
		List<GenericValue> perfReviewItemsELI = null;
		log.info("######### Perf Review Id is :::: " + perfReviewId);
		
		try {
			perfReviewItemsELI = delegator.findList("PerfReviewItem", EntityCondition
					.makeCondition("perfReviewId", perfReviewId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfReviewItems : perfReviewItemsELI) 
		{
			log.info("######### Review target ID " + perfReviewItems.getString("kpiTargetId"));
			
			
				perfReviewItems.setString("isSubmitted", "Y");	
				try {
					delegator.createOrStore(perfReviewItems);					
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		updatePerfReview(delegator, perfReviewId);
		
		
		
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

	private static void updatePerfReview(Delegator delegator, String perfReviewId) {
		List<GenericValue> perfReviewELI = null;
		log.info("######### Perf Review Id is :::: " + perfReviewId);
		
		getSubmitList(delegator, perfReviewId);
		
		try {
			perfReviewELI = delegator.findList("PerfReview", EntityCondition
					.makeCondition("perfReviewId", perfReviewId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfReview : perfReviewELI) 
		{
			if(!(subs.contains("N")))
			{
				perfReview.setString("status", "Submitted");	
				try {
					delegator.createOrStore(perfReview);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
					
		}
		
	}

	private static void getSubmitList(Delegator delegator, String perfReviewId) {
		subs = new ArrayList<String>();
		
		List<GenericValue> perfReviewItemsELI = null;
		log.info("######### Perf Review Id is :::: " + perfReviewId);
		
		try {
			
			perfReviewItemsELI = delegator.findList("PerfReviewItem", EntityCondition
					.makeCondition("perfReviewId", perfReviewId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfReviewItems : perfReviewItemsELI) 
		{
			subs.add(perfReviewItems.getString("isSubmitted"));
		}
		
	}
}
