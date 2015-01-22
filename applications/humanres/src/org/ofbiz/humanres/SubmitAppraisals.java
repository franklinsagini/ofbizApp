package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
	static TreeMap<String, BigDecimal> perfGoalsMap = new TreeMap<String, BigDecimal>();
	static TreeMap<String, BigDecimal> perfRatingTypeMap = new TreeMap<String, BigDecimal>();
	static BigDecimal totPercentage = BigDecimal.ZERO;
	
private static Logger log = Logger.getLogger(GeneratePerformanceTargets.class);
	
	public static String submitScores(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");		
		
		String perfReviewId = (String) request.getParameter("perfReviewId");
		
		getPerfGoalPercentages(delegator);
		getAssessmentRatings(delegator);
		
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
			String perfGoalsId = perfReviewItems.getString("perfGoalsId");
			String perfRatingTypeId = perfReviewItems.getString("perfRatingTypeId");
			
			calculateRatedScores(delegator, perfGoalsId, perfRatingTypeId);
			
			

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

	private static void calculateRatedScores(Delegator delegator,
			String perfGoalsId, String perfRatingTypeId) {
		
		
		BigDecimal score = perfRatingTypeMap.get(perfRatingTypeId);
//		totPercentage
		
		
		
	}

	private static void getAssessmentRatings(Delegator delegator) {
		List<GenericValue> perfRatingTypeELI = null;
		
		try {
			
			perfRatingTypeELI = delegator.findList("PerfRatingType", null, null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfRatingType : perfRatingTypeELI) 
		{
			BigDecimal assmntRatings = new BigDecimal(perfRatingType.getLong("points"));
			log.info("######### Ratings =  :::: " + assmntRatings);
			
			perfRatingTypeMap.put(perfRatingType.getString("perfRatingTypeId"), assmntRatings);
		}
		
		log.info("######### Perf Rating Type Tree Map Count =  :::: " + perfRatingTypeMap.size());
		
	}

	private static void getPerfGoalPercentages(Delegator delegator) {
		
		List<GenericValue> perfGoalsELI = null;
		
		try {
			
			perfGoalsELI = delegator.findList("PerfGoals", null, null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfGoals : perfGoalsELI) 
		{
			BigDecimal pgoals =  new BigDecimal(perfGoals.getLong("percentage"));
			log.info("######### Goals =  :::: " + pgoals);
			
			perfGoalsMap.put(perfGoals.getString("perfGoalsId"), pgoals);
		}
		
		log.info("######### Perf Goals Tree Map Count =  :::: " + perfGoalsMap.size());
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
