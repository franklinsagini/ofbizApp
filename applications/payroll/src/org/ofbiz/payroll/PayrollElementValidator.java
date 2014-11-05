package org.ofbiz.payroll;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

/****
 * @author Charles Wambugu @when Nov 1, 2014 
 * 
 *         Staff Payroll Elements Validations
 * 		org.ofbiz.payroll uniqueFieldsValidation
 *         staffPayrollId payrollElementId
 * */


public class PayrollElementValidator {

	public static String uniqueFieldsValidation(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String staffPayrollId = (String) request.getParameter("staffPayrollId");
		String payrollElementId = (String) request.getParameter("payrollElementId");

		String staffpayrollState = "FREE";

		staffpayrollState = checkIfUnique(staffPayrollId, payrollElementId);
		
		
		
/*		
		if (nationalIDNumber.length() < 6){
			idNumberSize = "LESS";
		}
		
		if (nationalIDNumber.length() > 8){
			idNumberSize = "MORE";
		}*/

		result.put("staffpayrollState", staffpayrollState);

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

	private static String checkIfUnique( String staffPayrollId, String payrollElementId) {
		List<GenericValue> speELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> staffPayrollElementConditions = EntityCondition
		.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
				"staffPayrollId", EntityOperator.EQUALS, staffPayrollId),
				EntityCondition.makeCondition("payrollElementId",
						EntityOperator.EQUALS, payrollElementId)),
				EntityOperator.AND);
		
		try {
			speELI = delegator.findList("StaffPayrollElements", staffPayrollElementConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "NE";
		for (GenericValue genericValue : speELI) {
			state = "E";
		}

		return state;
	}
}
