package org.ofbiz.loans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.webapp.control.ConfigXMLReader.Event;
import org.ofbiz.webapp.control.ConfigXMLReader.RequestMap;
import org.ofbiz.webapp.event.EventHandler;
import org.ofbiz.webapp.event.EventHandlerException;
import org.ofbiz.webapp.event.JavaEventHandler;

public class LoanEvent implements EventHandler {
	
	public static final String module =LoanEvent.class.getName(); 
    protected EventHandler service; 

	@Override
	public void init(ServletContext context) throws EventHandlerException {
		this.service = new JavaEventHandler(); 
        this.service.init(context);
	}

	@Override
	public String invoke(Event event, RequestMap requestMap,
			HttpServletRequest request, HttpServletResponse response)
			throws EventHandlerException {
		// TODO Auto-generated method stub
		
		String respCode = service.invoke(event, requestMap, request, response); 
		 // pull out the service response from the request attribute 
        Map<String, Object> attrMap =UtilHttp.getParameterMap(request); 
        //Map<String, Object> attrMap = UtilHttp.getJSONAttributeMap(request); 

        // create a JSON Object for return 
        //JSONObject json = JSONObject.fromObject(attrMap); 
        JSONObject json = JSONObject.fromObject(attrMap); 
        String jsonStr = json.toString(); 
        if (jsonStr == null) { 
            throw new EventHandlerException("JSON Object was empty; fatal error!"); 
        } 

        // set the X-JSON content type 
        response.setContentType("application/x-json"); 
        // jsonStr.length is not reliable for unicode characters 
        try { 
            response.setContentLength(jsonStr.getBytes("UTF8").length); 
        } catch (UnsupportedEncodingException e) { 
            throw new EventHandlerException("Problems with Json encoding", e); 
        } 

        // return the JSON String 
        Writer out; 
        try { 
            out = response.getWriter(); 
            out.write(jsonStr); 
            out.flush(); 
        } catch (IOException e) { 
            throw new EventHandlerException("Unable to get response writer", e); 
        } 

        return respCode; 
	}

}
