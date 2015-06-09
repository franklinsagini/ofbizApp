package org.ofbiz.party.party;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.commons.lang.StringUtils;
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
 * @author Japheth Odonya @when Oct 6, 2014 7:09:13 PM
 * 
 *         Member Validations
 * 		org.ofbiz.party.party.MemberValidation uniqueFieldsValidation
 *         idNumber pinNumber payrollNumber mobileNumber employeeNumber
 * */
public class MemberValidation {
	public static String BUSINESSPREFIX = "HAZ9";
	public static Integer COUNTDIGITS = 6;

	public static String uniqueFieldsValidation(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String idNumber = (String) request.getParameter("idNumber");
		String pinNumber = (String) request.getParameter("pinNumber");
		String payrollNumber = (String) request.getParameter("payrollNumber");
		String mobileNumber = (String) request.getParameter("mobileNumber");
		String employeeNumber = (String) request.getParameter("employeeNumber");
		String memberType = (String) request.getParameter("memberType");
		String stationId = (String) request.getParameter("stationId");
		
		if (memberType.equals("BUSINESS")){
			//Get the count of business numbers and + 1
			Integer businessMemberCount = SaccoUtility.getBusinessMemberCount();
			businessMemberCount = businessMemberCount + 1;
			
			payrollNumber = BUSINESSPREFIX+StringUtils.leftPad(String.valueOf(businessMemberCount), COUNTDIGITS, "0");
			result.put("payrollNumber", payrollNumber);
			System.out.println(" TTTTTTTTTTTTTPPPPPPPPPPPPP "+payrollNumber);
		}else{
			result.put("payrollNumber", "");
		}

		String idNumberState = "FREE";
		String pinNumberState = "FREE";
		String payrollNumberState = "FREE";
		String mobileNumberState = "FREE";
		String employeeNumberState = "FREE";
		String idNumberSize = "";

		idNumberState = getIdNumberState(idNumber);
		pinNumberState = getPinNumberState(pinNumber);
		payrollNumberState = getPayrollNumberState(payrollNumber);
		mobileNumberState = getMobileNumberState(mobileNumber);
		employeeNumberState = getEmployeeNumberState(employeeNumber, stationId);
		
		
		if (idNumber.length() < 6){
			idNumberSize = "LESS";
		}
		
		if (idNumber.length() > 8){
			idNumberSize = "MORE";
		}

		result.put("idNumberState", idNumberState);
		result.put("pinNumberState", pinNumberState);
		result.put("payrollNumberState", payrollNumberState);
		result.put("mobileNumberState", mobileNumberState);
		result.put("employeeNumberState", employeeNumberState);
		result.put("idNumberSize", idNumberSize);

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

	private static String getEmployeeNumberState(String employeeNumber, String stationId) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		
		stationId = stationId.replaceAll(",", "");
		
		EntityConditionList<EntityExpr> memberConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"employeeNumber", EntityOperator.EQUALS,
						employeeNumber), EntityCondition
						.makeCondition("stationId",
								EntityOperator.EQUALS, Long.valueOf(stationId))),
						EntityOperator.AND);

		
		try {
			memberELI = delegator.findList("Member",memberConditions, null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		String state = "FREE";
		
		if (memberELI.size() > 0)
		{
			state = "USED";
		}

		return state;
	}

	private static String getMobileNumberState(String mobileNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member", EntityCondition.makeCondition(
							"mobileNumber", mobileNumber), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "FREE";
		if (memberELI.size() > 0)
		{
			state = "USED";
		}
		return state;
	}

	private static String getPayrollNumberState(String payrollNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member", EntityCondition
					.makeCondition("payrollNumber", payrollNumber), null, null,
					null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

	private static String getPinNumberState(String pinNumber) {
		
		if ((pinNumber == null) || (pinNumber.equals(""))){
			return "FREE";
		}
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("pinNumber", pinNumber),
					null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

	private static String getIdNumberState(String idNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Member",
					EntityCondition.makeCondition("idNumber", idNumber), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

}
