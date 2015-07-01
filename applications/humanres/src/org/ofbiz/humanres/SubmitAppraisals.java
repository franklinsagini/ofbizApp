package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	static ArrayList<String> perfGoalsList = new ArrayList<String>();
	static ArrayList<BigDecimal> assessmentRatingsPoints = new ArrayList<BigDecimal>();
	static ArrayList<BigDecimal> assessmentRatingsPointsQTT = new ArrayList<BigDecimal>();
	static TreeMap<String, BigDecimal> perfGoalsMap = new TreeMap<String, BigDecimal>();
	static TreeMap<String, BigDecimal> perfRatingTypeMap = new TreeMap<String, BigDecimal>();
	static TreeMap<String, BigDecimal> perfRatingTypeQTTMap = new TreeMap<String, BigDecimal>();
	static BigDecimal totPercentage = BigDecimal.ZERO;
	
private static Logger log = Logger.getLogger(GeneratePerformanceTargets.class);
	
	public static String submitScores(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");		
		
		String perfReviewId = (String) request.getParameter("perfReviewId");
		
		getPerfGoalPercentages(delegator);
		getAssessmentRatings(delegator);
		getQTTAssessmentRatings(delegator);
		
		// Get Performance Review
		List<GenericValue> perfReviewItemsELI = null;
		log.info("######### Perf Review Id is :::: " + perfReviewId);

		Collections.sort(assessmentRatingsPoints);		
		Collections.sort(assessmentRatingsPointsQTT);
		
		calculateRatedScores(delegator, perfReviewItemsELI, perfReviewId);
		
		/*	for (GenericValue perfReviewItems : perfReviewItemsELI) 
		{
			log.info("######### Review target ID " + perfReviewItems.getString("kpiTargetId"));
			perfReviewItems.setString("isSubmitted", "Y");	
			try {
				delegator.createOrStore(perfReviewItems);					
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		
	//	updatePerfReview(delegator, perfReviewId);
		
		
		
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

	private static void calculateRatedScores(Delegator delegator, List<GenericValue> perfReviewItemsELI, String perfReviewId) {
		
		
		
		
		BigDecimal percentageAmnt=BigDecimal.ZERO;
		
		for(int i=0; i<perfGoalsList.size(); i++)
		{
			BigDecimal maxRating = BigDecimal.ZERO;
			BigDecimal maxRatingQTT = BigDecimal.ZERO;
			BigDecimal div = BigDecimal.ZERO;
			BigDecimal goalPercentage = perfGoalsMap.get(perfGoalsList.get(i));
			BigDecimal perfReviewScore = BigDecimal.ZERO;
			
//			System.out.println("Goal %>>>>>>>>>> " +goalPercentage);
			
			EntityConditionList<EntityExpr> perfRevItem = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("perfReviewId", EntityOperator.EQUALS, perfReviewId), 
				EntityCondition.makeCondition("perfGoalsId", EntityOperator.EQUALS, perfGoalsList.get(i))), 
				EntityOperator.AND);
			

			try {
				perfReviewItemsELI = delegator.findList("PerfReviewItem", perfRevItem, null,
						null, null, false);
			
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			
			
			for (GenericValue perfReviewItems : perfReviewItemsELI) 
			{	

				if(!(perfReviewItems.get("perfRatingTypeId")==null))
				{
					perfReviewScore = perfReviewScore.add(perfRatingTypeMap.get(perfReviewItems.get("perfRatingTypeId")));					
					maxRating = maxRating.add(Collections.max(assessmentRatingsPoints));
					
					System.out.println("maxRating %>>>>>>>>>> " +maxRating);
					
					div =  perfReviewScore.divide(maxRating, 1, RoundingMode.HALF_UP).setScale(1,
							RoundingMode.HALF_UP);
				}
				if(!(perfReviewItems.get("perfRatingTypeQTTId")==null))
				{
					perfReviewScore = perfReviewScore.add(perfRatingTypeQTTMap.get(perfReviewItems.get("perfRatingTypeQTTId")));
					maxRatingQTT = maxRatingQTT.add(Collections.max(assessmentRatingsPointsQTT));
					
					System.out.println("maxRatingQTT %>>>>>>>>>> " +maxRatingQTT);
					
					div =  perfReviewScore.divide(maxRatingQTT, 1, RoundingMode.HALF_UP).setScale(1,
							RoundingMode.HALF_UP);
				}
				
				
				

				
			/*	log.info("######### Review target ID " + perfReviewItems.getString("kpiTargetId"));
				perfReviewItems.setString("isSubmitted", "Y");	
				try {
					delegator.createOrStore(perfReviewItems);					
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			
			percentageAmnt = percentageAmnt.add(div.multiply(goalPercentage));
			
		}
		System.out.println("Goal Percentage >>>>>>>>>>>>>>>>" +percentageAmnt);
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
			assessmentRatingsPoints.add(assmntRatings);
		}
		
		log.info("######### Perf Rating Type Tree Map Count =  :::: " + perfRatingTypeMap.size());
		
	}
	private static void getQTTAssessmentRatings(Delegator delegator) {
		List<GenericValue> perfRatingTypeQTTELI = null;
		
		try {
			
			perfRatingTypeQTTELI = delegator.findList("PerfRatingType_QTT", null, null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		for (GenericValue perfRatingTypeQTT : perfRatingTypeQTTELI) 
		{
			BigDecimal assmntRatings = new BigDecimal(perfRatingTypeQTT.getLong("points"));
			log.info("######### Ratings =  :::: " + assmntRatings);
			
			perfRatingTypeQTTMap.put(perfRatingTypeQTT.getString("perfRatingTypeQTTId"), assmntRatings);
			assessmentRatingsPointsQTT.add(assmntRatings);
		}
		
		log.info("######### Perf Rating Type Tree Map Count =  :::: " + perfRatingTypeQTTMap.size());
		
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
			perfGoalsList.add(perfGoals.getString("perfGoalsId"));
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
