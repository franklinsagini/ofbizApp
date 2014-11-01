package org.ofbiz.humanres;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactoryImpl;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.webapp.event.EventHandlerException;

import com.google.gson.Gson;

/****
 * @author Japheth Odonya @when Oct 6, 2014 7:09:13 PM
 * 
 *         Member Validations
 * 		org.ofbiz.party.party.MemberValidation uniqueFieldsValidation
 *         idNumber pinNumber payrollNumber mobileNumber employeeNumber
 * */
public class EmployeeValidation {

	public static String uniqueFieldsValidation(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();

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
		String employeeNumberState = "FREE";
		String nhifNumberState = "FREE";
		String socialSecurityNumberState = "FREE";
		String emailAddressState = "FREE";
		String idNumberSize = "";

		nationalIDNumberState = getIdNumberState(nationalIDNumber);
		pinNumberState = getPinNumberState(pinNumber);
		passportNumberState = getPassportNumberState(passportNumber);
		mobileNumberState = getMobileNumberState(mobNo);
		employeeNumberState = getEmployeeNumberState(employeeNumber);
		nhifNumberState = getNhifNumberState(nhifNumber);
		emailAddressState = getEmailAddressState(emailAddress);
		socialSecurityNumberState = getSocialSecurityNumberState(socialSecurityNumber);
		
		
		if (nationalIDNumber.length() < 6){
			idNumberSize = "LESS";
		}
		
		if (nationalIDNumber.length() > 8){
			idNumberSize = "MORE";
		}

		result.put("nationalIDNumberState", nationalIDNumberState);
		result.put("pinNumberState", pinNumberState);
		result.put("employeeNumberState", employeeNumberState);
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

	private static String getIdNumberState( String nationalIDNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person", EntityCondition
					.makeCondition("nationalIDNumber", nationalIDNumber), null,
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

	private static String getPinNumberState(String pinNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator
					.findList("Person", EntityCondition.makeCondition(
							"pinNumber", pinNumber), null, null, null,
							false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String state = "FREE";
		for (GenericValue genericValue : memberELI) {
			state = "USED";
		}

		return state;
	}

	private static String getPassportNumberState(String passportNumber){
		if ((passportNumber == null) || (passportNumber.equals(""))){
			return "FREE";
		}
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person", EntityCondition
					.makeCondition("passportNumber", passportNumber), null, null,
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

	private static String getMobileNumberState(String mobNo){
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person",
					EntityCondition.makeCondition("mobNo", mobNo), null,
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

	private static String getEmployeeNumberState(String employeeNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person",
					EntityCondition.makeCondition("employeeNumber", employeeNumber), null,
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
	
	private static String getNhifNumberState(String nhifNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person",
					EntityCondition.makeCondition("nhifNumber", nhifNumber), null,
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
	
	private static String getEmailAddressState(String emailAddress) {
		
		if ((emailAddress == null) || (emailAddress.equals(""))){
			return "FREE";
		}
		
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person",
					EntityCondition.makeCondition("emailAddress", emailAddress), null,
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
	
	private static String getSocialSecurityNumberState(String socialSecurityNumber) {
		List<GenericValue> memberELI = null; // =
		Delegator delegator = DelegatorFactoryImpl.getDelegator(null);
		try {
			memberELI = delegator.findList("Person",
					EntityCondition.makeCondition("socialSecurityNumber", socialSecurityNumber), null,
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
