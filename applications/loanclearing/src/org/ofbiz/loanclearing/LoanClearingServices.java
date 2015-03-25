package org.ofbiz.loanclearing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;


public class LoanClearingServices {
	
		public static BigDecimal getTotalAmountToClear(Long loanClearId){
			BigDecimal totalToClear = BigDecimal.ZERO;
			
			List<GenericValue> loanClearItemELI = null; // =
			EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"loanClearId", EntityOperator.EQUALS, loanClearId)

					), EntityOperator.AND);

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			try {
				loanClearItemELI = delegator.findList("LoanClearItem",
						loanClearItemConditions, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for (GenericValue genericValue : loanClearItemELI) {
				totalToClear = totalToClear.add(genericValue.getBigDecimal("loanAmt"));
			}
			
			return totalToClear;
		}
		
		
		public static List<Long> getLoanApplicationIDsCleared(Long loanClearId){
			List<Long> listLoanApplicationIds = new ArrayList<Long>();
			
			List<GenericValue> loanClearItemELI = null; // =
			EntityConditionList<EntityExpr> loanClearItemConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"loanClearId", EntityOperator.EQUALS, loanClearId)

					), EntityOperator.AND);

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			try {
				loanClearItemELI = delegator.findList("LoanClearItem",
						loanClearItemConditions, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for (GenericValue genericValue : loanClearItemELI) {
				listLoanApplicationIds.add(genericValue.getLong("loanApplicationId"));
			}
			
			return listLoanApplicationIds;
		}
		
		
		public static String hasNewLoan(HttpServletRequest request,
				HttpServletResponse response) {
			Map<String, Object> result = FastMap.newInstance();
			Long loanClearId = Long.valueOf((String) request.getParameter("loanClearId"));

			result.put("hasNewLoan", hasNewLoan(loanClearId));

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


		private static Boolean hasNewLoan(Long loanClearId) {
			List<GenericValue> loanClearELI = null; // =
			EntityConditionList<EntityExpr> loanClearConditions = EntityCondition
					.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
							"loanClearId", EntityOperator.EQUALS, loanClearId),
							
							EntityCondition.makeCondition(
									"loanApplicationId", EntityOperator.NOT_EQUAL, null)

					), EntityOperator.AND);

			Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
			try {
				loanClearELI = delegator.findList("LoanClear",
						loanClearConditions, null, null, null, false);
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			if ((loanClearELI != null) && (loanClearELI.size() > 0))
				return true;
			
			return false;
		}

}
