package restcomponent;


import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.service.GenericDispatcherFactory;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

@Path("/statement")
public class MemberStatement {
	
	//@Context
	//HttpHeaders headers;
	
	@GET
	@Produces("text/plain")
	@Path("{user}")
    public Response getStatement(@PathParam("user") String user) {
    	
		String username = null;
		String password = null;
		
//		try {
//			username = headers.getRequestHeader("login.username").get(0);
//			password = headers.getRequestHeader("login.password").get(0);
//		} catch (NullPointerException e) {
//			return Response.serverError().entity("Problem reading http header(s): login.username or login.password").build();
//		}
//		
//		if (username == null || password == null) {
//			return Response.serverError().entity("Problem reading http header(s): login.username or login.password").build();
//		}
		
    	GenericDelegator delegator = (GenericDelegator) DelegatorFactory.getDelegator("default");
    	//LocalDispatcher dispatcher = GenericDispatcher.getLocalDispatcher("default",delegator); 
		GenericDispatcherFactory genericDispatcherFactory = new GenericDispatcherFactory();
		LocalDispatcher dispatcher = genericDispatcherFactory
				.createLocalDispatcher("default", delegator);
		//Map<String, String> paramMap = UtilMisc.toMap("message", message);

    	Map<String, String> paramMap = UtilMisc.toMap( 
    			"user", user, 
    			"login.username", username,
    			"login.password", password
    		);
		
		Map<String, Object> result = FastMap.newInstance();
		try {
			result = dispatcher.runSync("statement", paramMap);
		} catch (GenericServiceException e1) {
			Debug.logError(e1, PingResource.class.getName());
			return Response.serverError().entity(e1.toString()).build();
		}
		
		if (ServiceUtil.isSuccess(result)) {   
			//text/json
			//return Response.ok("RESPONSE: *** " + result.get("transactions") + " ***").type("text/plain").build();
			return Response.ok(result.get("transactions")).type("application/json").build();
		}
		
		if (ServiceUtil.isError(result) || ServiceUtil.isFailure(result)) {
			return Response.serverError().entity(ServiceUtil.getErrorMessage(result)).build();
		}
		// shouldn't ever get here ... should we?
		throw new RuntimeException("Invalid ");
    }

}
