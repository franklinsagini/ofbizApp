package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

public class CloseReviews {


private static Logger log = Logger.getLogger(CloseReviews.class);
static ArrayList<String> perfStatus = new ArrayList<String>();
	
	public static String closeReviews(HttpServletRequest request,
			HttpServletResponse response) {
		
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		
		
		String perfManagerId = (String) request.getParameter("perfManagerId");
		log.info("######### perf manager ID :::: "+perfManagerId);
		
		checkExists(delegator, perfManagerId);
		
		
		if(perfStatus.isEmpty())
		{
			log.info("######### NO PERF REVIEWS :::: ");
			return "";
		}
		else if(perfStatus.contains("Unsubmitted"))
		{
			log.info("######### UNSUBMITTED PERF REVIEWS :::: ");
			return "";
		}
		else
		{
			// Get Performance Review
			List<GenericValue> perfReviewELI = null;
			
			try {
				perfReviewELI = delegator.findList("PerfManager", EntityCondition
						.makeCondition("perfManagerId", perfManagerId), null,
						null, null, false);
			
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for (GenericValue perfMgt : perfReviewELI) 
			{
				perfMgt.setString("status", "Closed");
			
				try {
					delegator.createOrStore(perfMgt);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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

	private static ArrayList<String> checkExists(Delegator delegator, String perfManagerId) {
		perfStatus = new ArrayList<String>();
		
		List<GenericValue> perfReviewELI = null;
		
		try {
			perfReviewELI = delegator.findList("PerfReview", EntityCondition
					.makeCondition("perfManagerId", perfManagerId), null,
					null, null, false);
		
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		if(!perfReviewELI.equals(null))
		{
			for (GenericValue perfReview : perfReviewELI) 
			{
				perfStatus.add(perfReview.getString("status"));
				log.info("######### perf STATUS :::: "+perfReview.getString("status"));
				log.info("######### perf STATUS SIZE:::: "+perfStatus.size());
			}
		}
		
		
		return perfStatus;
	}

}
