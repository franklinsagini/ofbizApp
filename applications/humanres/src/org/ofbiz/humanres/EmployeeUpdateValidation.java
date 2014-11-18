
package org.ofbiz.humanres;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
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
public class EmployeeUpdateValidation {
	public static Logger log = Logger.getLogger(EmployeeUpdateValidation.class);

	public static String uniqueFieldsValidation(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

		String nationalIDNumber = (String) request.getParameter("nationalIDNumber");
		String pinNumber = (String) request.getParameter("pinNumber");
		String mobNo = (String) request.getParameter("mobNo");
		String emailAddress = (String) request.getParameter("emailAddress");
		/*String employeeNumber = (String) request.getParameter("employeeNumber");*/
		String socialSecurityNumber = (String) request.getParameter("socialSecurityNumber");
		String nhifNumber = (String) request.getParameter("nhifNumber");
		String passportNumber = (String) request.getParameter("passportNumber");
		String partyId = (String) request.getParameter("partyId");

		String nationalIDNumberState = "FREE";
		String pinNumberState = "FREE";
		String passportNumberState = "FREE";
		String mobileNumberState = "FREE";
		String nhifNumberState = "FREE";
		String socialSecurityNumberState = "FREE";
		String emailAddressState = "FREE";
		String idNumberSize = "";

		nationalIDNumberState = getIdNumberState(nationalIDNumber,partyId);
		pinNumberState = getPinNumberState(pinNumber,partyId);
		passportNumberState = getPassportNumberState(passportNumber,partyId);
		mobileNumberState = getMobileNumberState(mobNo,partyId);
		nhifNumberState = getNhifNumberState(nhifNumber,partyId);
		emailAddressState = getEmailAddressState(emailAddress,partyId);
		socialSecurityNumberState = getSocialSecurityNumberState(socialSecurityNumber,partyId);
		
		
		if (nationalIDNumber.length() < 6){
			idNumberSize = "LESS";
		}
		
		if (nationalIDNumber.length() > 8){
			idNumberSize = "MORE";
		}

		result.put("nationalIDNumberState", nationalIDNumberState);
		result.put("pinNumberState", pinNumberState);
		result.put("mobileNumberState", mobileNumberState);
		result.put("nhifNumberState", nhifNumberState);
		result.put("passportNumberState", passportNumberState);
		result.put("emailAddressState", emailAddressState);
		result.put("socialSecurityNumberState", socialSecurityNumberState);
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

	private static String getIdNumberState( String nationalIDNumber, String partyId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, nationalIDNumber),
				    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			List<String> orderByList = new ArrayList<String>();
			orderByList.add("-partyId");
			
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}
		
		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		
		
		return state;
	}

	private static String getPinNumberState(String pinNumber, String partyId) {
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("pinNumber", EntityOperator.EQUALS, pinNumber),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		

		return state;
	}

	private static String getPassportNumberState(String passportNumber, String partyId){
		if ((passportNumber == null) || (passportNumber.equals(""))){
			return "FREE";
		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("passportNumber", EntityOperator.EQUALS, passportNumber),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		

		return state;
	}

	private static String getMobileNumberState(String mobNo, String partyId){
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("mobNo", EntityOperator.EQUALS, mobNo),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		
		return state;
	}
	
	private static String getNhifNumberState(String nhifNumber, String partyId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("nhifNumber", EntityOperator.EQUALS, nhifNumber),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		

		return state;
	}
	
	private static String getEmailAddressState(String emailAddress, String partyId) {
		
		if ((emailAddress == null) || (emailAddress.equals(""))){
			return "FREE";
		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("emailAddress", EntityOperator.EQUALS, emailAddress),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		

		return state;
	}
	
	private static String getSocialSecurityNumberState(String socialSecurityNumber, String partyId) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("socialSecurityNumber", EntityOperator.EQUALS, socialSecurityNumber),
					    EntityCondition.makeCondition("partyId",EntityOperator.NOT_EQUAL, partyId)),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		if (memberELI.size() > 0) {
			state = "USED";
		} else {
			state = "FREE";

		}
		

		return state;
	}

}

