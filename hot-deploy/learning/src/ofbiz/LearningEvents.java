/**
 * 
 */
package ofbiz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.UtilHttp;

/**
 * @author samoei
 * 
 */
public class LearningEvents {
	public static String processFirstForm(HttpServletRequest request, HttpServletResponse resposnse) {
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		request.setAttribute("Combined", firstName + " "+lastName);
		request.setAttribute("allParams", UtilHttp.getParameterMap(request));
		request.setAttribute("submit", "Submitted");
		return "success";

	}
	
	public static String processMultiForm(HttpServletRequest request, HttpServletResponse response){
		Collection parsed = UtilHttp.parseMultiFormData(UtilHttp.getParameterMap(request));
		List combined = new ArrayList();
		return null;
	}
}
