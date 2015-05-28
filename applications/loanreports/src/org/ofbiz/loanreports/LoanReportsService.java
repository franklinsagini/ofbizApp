package org.ofbiz.loanreports;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ofbiz.accountholdertransactions.LoanUtilities;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.loans.LoanServices;
import org.ofbiz.loansprocessing.LoansProcessingServices;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

public class LoanReportsService {
	
	public static Logger log = Logger.getLogger(LoanReportsService.class);

	public static Timestamp getYearStartDate(){
		
		DateTime date = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
		//DateTime date = new DateTime().dayOf
		
		Timestamp startDate = new Timestamp(date.toDate().getTime());
		return startDate;
	}
	
	
	public static Timestamp getYearEndDate(){
		
		DateTime date = new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay();
		date = date.plusYears(1).minusMillis(1);
		
		Timestamp startDate = new Timestamp(date.toDate().getTime());
		
		return startDate;
	}
	
	public static String getLoanApplications(HttpServletRequest request, HttpServletResponse response){
		Long loanStatusId = LoanServices.getLoanStatusId("DISBURSED");
		Long loanStatusIdCleared = LoanServices.getLoanStatusId("CLEARED");
		
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String partyId = (String) request.getParameter("partyId");
		partyId = partyId.replaceAll(",", "");
		List<GenericValue> loanApplicationELI = null;
		EntityConditionList<EntityExpr> loanApplicationELIConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId)),
						
						EntityCondition.makeCondition(
								"loanStatusId", EntityOperator.EQUALS, loanStatusId)
						),
						EntityOperator.AND);
		
		
		//loanStatusId
		try {
			loanApplicationELI = delegator.findList("LoanApplication", loanApplicationELIConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		if (loanApplicationELIConditions == null){
			result.put("", "No Loans");
		}
		
		GenericValue loanProduct = null;
		
		//log.info(" LLLLLLLLLL We have "+loanApplicationELI.size()+" Loans !!!");
		
		Long loanApplicationId = null;
		
		for (GenericValue genericValue : loanApplicationELI) {
			loanProduct = LoanUtilities.getLoanProduct(genericValue.getLong("loanProductId"));
			loanApplicationId = genericValue.getLong("loanApplicationId");
			result.put(loanApplicationId.toString(), loanProduct.get("name")+"("+loanProduct.get("code")+")"+" - "+" Loan No : "+genericValue.getString("loanNo")+" - "+genericValue.getBigDecimal("loanAmt").setScale(2, RoundingMode.HALF_UP)+" Balance : "+LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId).setScale(2, RoundingMode.HALF_UP));
			
			//log.info(" PPPPP Product name :::  "+loanProduct.get("name")+" Product !!!");
		}
		
		//Add CLeared Loans
		List<GenericValue> loanApplicationClearedELI = null;
		EntityConditionList<EntityExpr> loanApplicationClearedELIConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, Long.valueOf(partyId)),
						
						EntityCondition.makeCondition(
								"loanStatusId", EntityOperator.EQUALS, loanStatusIdCleared)
						),
						EntityOperator.AND);
		
		
		//loanStatusId
		try {
			loanApplicationClearedELI = delegator.findList("LoanApplication", loanApplicationClearedELIConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
		
		for (GenericValue genericValue : loanApplicationClearedELI) {
			loanProduct = LoanUtilities.getLoanProduct(genericValue.getLong("loanProductId"));
			loanApplicationId = genericValue.getLong("loanApplicationId");
			result.put(loanApplicationId.toString(), loanProduct.get("name")+"("+loanProduct.get("code")+")"+" - "+genericValue.getBigDecimal("loanAmt").setScale(2, RoundingMode.HALF_UP)+" Balance : "+LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(loanApplicationId).setScale(2, RoundingMode.HALF_UP));
			
			//log.info(" PPPPP Product name :::  "+loanProduct.get("name")+" Product !!!");
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(result);

		// set the X-JSON content type
		response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		try {
			response.setContentLength(json.getBytes("UTF8").length);
		} catch (UnsupportedEncodingException e) {
			try {
				throw new EventHandlerException("Problems with Json encoding",
						e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(json);
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return json;
	}
	
	
}
