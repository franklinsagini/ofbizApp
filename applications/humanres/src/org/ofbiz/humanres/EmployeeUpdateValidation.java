
package org.ofbiz.humanres;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
		Delegator delegator = (Delegator) request.getAttribute("delegator");

		String nationalIDNumber = (String) request.getParameter("nationalIDNumber");
		String pinNumber = (String) request.getParameter("pinNumber");
		String mobNo = (String) request.getParameter("mobNo");
		String emailAddress = (String) request.getParameter("emailAddress");
		String employeeNumber = (String) request.getParameter("employeeNumber");
		String socialSecurityNumber = (String) request.getParameter("socialSecurityNumber");
		String nhifNumber = (String) request.getParameter("nhifNumber");
		String passportNumber = (String) request.getParameter("passportNumber");

		String nationalIDNumberState = "FREE";
		String pinNumberState = "FREE";
		String passportNumberState = "FREE";
		String mobileNumberState = "FREE";
		String nhifNumberState = "FREE";
		String socialSecurityNumberState = "FREE";
		String emailAddressState = "FREE";
		String idNumberSize = "";

		nationalIDNumberState = getIdNumberState(nationalIDNumber,employeeNumber);
		pinNumberState = getPinNumberState(pinNumber,employeeNumber);
		passportNumberState = getPassportNumberState(passportNumber,employeeNumber);
		mobileNumberState = getMobileNumberState(mobNo,employeeNumber);
		nhifNumberState = getNhifNumberState(nhifNumber,employeeNumber);
		emailAddressState = getEmailAddressState(emailAddress,employeeNumber);
		socialSecurityNumberState = getSocialSecurityNumberState(socialSecurityNumber,employeeNumber);
		
		
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

	private static String getIdNumberState( String nationalIDNumber, String employeeNumber) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, nationalIDNumber)
						),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}
		
		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			log.info("################## "+genericValue.getString("nationalIDNumber")+ " Employee Number "+genericValue.getString("employeeNumber"));
			state = "USED";
		}

		return state;
	}

	private static String getPinNumberState(String pinNumber, String employeeNumber) {
	
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, pinNumber),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

	private static String getPassportNumberState(String passportNumber, String employeeNumber){
		if ((passportNumber == null) || (passportNumber.equals(""))){
			return "FREE";
		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, passportNumber),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

	private static String getMobileNumberState(String mobNo, String employeeNumber){
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, mobNo),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}
	
	private static String getNhifNumberState(String nhifNumber, String employeeNumber) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, nhifNumber),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}
	
	private static String getEmailAddressState(String emailAddress, String employeeNumber) {
		
		if ((emailAddress == null) || (emailAddress.equals(""))){
			return "FREE";
		}
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, emailAddress),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}
	
	private static String getSocialSecurityNumberState(String socialSecurityNumber, String employeeNumber) {
		
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		EntityConditionList<EntityExpr> idConditions = EntityCondition
				.makeCondition(UtilMisc.toList(
					EntityCondition.makeCondition("nationalIDNumber", EntityOperator.EQUALS, socialSecurityNumber),
						EntityCondition.makeCondition("employeeNumber",EntityOperator.NOT_EQUAL, employeeNumber),
					null,null),EntityOperator.AND);
		
		List<GenericValue> memberELI = null;		
		
		try {
			memberELI = delegator.findList("Person",idConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get approved leaves";
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

}

