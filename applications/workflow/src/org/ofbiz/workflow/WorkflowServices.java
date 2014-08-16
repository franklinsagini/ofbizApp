package org.ofbiz.workflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
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

/**
 * @author Japheth Odonya @when Aug 15, 2014 9:01:19 PM
 * 
 * **/
public class WorkflowServices {

	public static Logger log = Logger.getLogger(WorkflowServices.class);

	/***
	 * @author Japheth Odonya @when Aug 15, 2014 9:01:33 PM Get the Organization
	 *         Unit given the Party/User (partyId) The OrganizationUnit is
	 *         gotten by querying UnitEmployeeMap
	 * */
	public static String getUserOrganizationUnit(String partyId) {

		Map<String, Object> result = FastMap.newInstance();
		log.info("What we got the Party ############ " + partyId);

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);

		List<GenericValue> unitEmployeeMapELI = null; // =

		try {
			unitEmployeeMapELI = delegator.findList("UnitEmployeeMap",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String organizationUnitId = "";
		for (GenericValue genericValue : unitEmployeeMapELI) {
			organizationUnitId = genericValue.getString("organizationUnitId");
		}

		result.put("organizationUnitId", organizationUnitId);
		return organizationUnitId;
	}

	/***
	 * Get the document workflow ID given the document name (this can either be
	 * LEAVE, LOAN, INVOICE etc)
	 * **/
	public static String getWorkflowDocumentType(String documentName) {
		Map<String, Object> result = FastMap.newInstance();
		log.info("What we got the Document Name ############ " + documentName);

		Delegator delegator;
		delegator = DelegatorFactoryImpl.getDelegator(null);
		List<GenericValue> workflowDocumentTypeELI = null; // =

		try {
			workflowDocumentTypeELI = delegator.findList(
					"WorkflowDocumentType",
					EntityCondition.makeCondition("name", documentName), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		String workflowDocumentTypeId = "";
		for (GenericValue genericValue : workflowDocumentTypeELI) {
			workflowDocumentTypeId = genericValue
					.getString("workflowDocumentTypeId");
		}

		result.put("workflowDocumentTypeId", workflowDocumentTypeId);
		return workflowDocumentTypeId;
	}

	/***
	 * @author Japheth Odonya @when Aug 16, 2014 12:27:33 AM Forward Loan
	 *         Application
	 * */
	public static String forwardApplication(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		// Delegator delegator = dctx.getDelegator();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// request.getParameter(arg0)
		String loanApplicationId = (String) request
				.getParameter("loanApplicationId");

		GenericValue loanApplication = null;
		try {
			loanApplication = delegator.findOne("LoanApplication",
					UtilMisc.toMap("loanApplicationId", loanApplicationId),
					false);
		} catch (GenericEntityException e) {
			// UtilMisc.toMap("errMessage", e.getMessage()), locale));
			return "Cannot Get Loan Application";
		}

		// Get Unit and Document
		String organizationUnitId = loanApplication
				.getString("organizationUnitId");
		String workflowDocumentTypeId = loanApplication
				.getString("workflowDocumentTypeId");
		String documentApprovalId = null;
		documentApprovalId = loanApplication.getString("documentApprovalId");
		
		GenericValue documentApproval = doFoward(delegator, organizationUnitId,
				workflowDocumentTypeId, documentApprovalId);
		
		if (documentApproval == null){
			//Loan Approved
			result.put("fowardMessage", "");
		} else{
			//Foward Loan Application by setting the documentApprovalId
			loanApplication.set("documentApprovalId", documentApproval.getString("documentApprovalId"));
			
			if ((documentApproval.getString("nextLevel") == null) || (documentApproval.getString("nextLevel").equals(""))){
				loanApplication.set("approvalStatus", documentApproval.getString("stageAction"));
			} else{
				loanApplication.set("approvalStatus", documentApproval.getString("stageAction")+" (APPROVED)");
			}
			
			//Set Responsible
			//responsibleEmployee
			loanApplication.set("responsibleEmployee", documentApproval.getString("responsibleEmployee"));
			
			
			
			try {
				delegator.createOrStore(loanApplication);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			result.put("fowardMessage", documentApproval.getString("stageAction"));
			
		}
		
		// return JSONBuilder.class.
		// JSONObject root = new JSONObject();
		
		//		Gson gson = new Gson();
		//		String json = gson.toJson(result);

		//System.out.println("json = " + json);

		// set the X-JSON content type
		//response.setContentType("application/x-json");
		// jsonStr.length is not reliable for unicode characters
		//		try {
		//			response.setContentLength(json.getBytes("UTF8").length);
		//		} catch (UnsupportedEncodingException e) {
		//			try {
		//				throw new EventHandlerException("Problems with Json encoding",
		//						e);
		//			} catch (EventHandlerException e1) {
		//				e1.printStackTrace();
		//			}
		//		}
		// return the JSON String
		Writer out;
		try {
			out = response.getWriter();
			out.write(result.get("fowardMessage").toString());
			out.flush();
		} catch (IOException e) {
			try {
				throw new EventHandlerException(
						"Unable to get response writer", e);
			} catch (EventHandlerException e1) {
				e1.printStackTrace();
			}
		}
		return result.get("fowardMessage").toString();

	}

	public static GenericValue doFoward(Delegator delegator,
			String organizationUnitId, String workflowDocumentTypeId,
			String documentApprovalId) {
		GenericValue currentApproval = null;
		if ((documentApprovalId == null) || (documentApprovalId.equals(""))) {
			// This is the first forwad, get the first approver from Document
			// Level Config
			currentApproval = getCurrentApprovalFromLevelConfig(delegator,
					organizationUnitId, workflowDocumentTypeId);
			return currentApproval;
		} else {
			// Get the DocumentApproval from the DocumentApproval using the
			// documentApprovalId
			currentApproval = getCurrentApprovalFromDocumentApproval(delegator,
					documentApprovalId);
			
			String nextLevelId = null;
			nextLevelId = currentApproval.getString("nextLevel");
			
			if ((nextLevelId == null) || (nextLevelId.equals(""))){
				return null;
			} else{
				return getCurrentApprovalFromDocumentApproval(delegator,
						nextLevelId);
			}
		}
	}

	/**
	 * Get Current Approval from DocumentLevelConfig
	 * **/
	private static GenericValue getCurrentApprovalFromLevelConfig(
			Delegator delegator, String organizationUnitId,
			String workflowDocumentTypeId) {

		List<GenericValue> documentLevelConfigELI = new LinkedList<GenericValue>();

		EntityConditionList<EntityExpr> documentLevelConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"organizationUnitId", EntityOperator.EQUALS,
						organizationUnitId), EntityCondition.makeCondition(
						"workflowDocumentTypeId", EntityOperator.EQUALS,
						workflowDocumentTypeId)), EntityOperator.AND);
		try {
			documentLevelConfigELI = delegator.findList("DocumentLevelConfig",
					documentLevelConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			e2.printStackTrace();
		}

		GenericValue documentLevelConfig = null;

		for (GenericValue genericValue : documentLevelConfigELI) {
			documentLevelConfig = genericValue;
		}

		String currentApprovalId = documentLevelConfig
				.getString("documentApprovalId");

		// Get the DocumentApproval
		GenericValue currentApproval = null;
		currentApproval = getCurrentApprovalFromDocumentApproval(delegator,
				currentApprovalId);
		return currentApproval;
	}

	/**
	 * @author Japheth Odonya  @when Aug 16, 2014 1:45:27 AM
	 * Get the ApprovalDocument given documentApprovalId
	 * **/
	public static GenericValue getCurrentApprovalFromDocumentApproval(
			Delegator delegator, String documentApprovalId) {

		GenericValue documentApproval = null;
		try {
			documentApproval = delegator.findOne("DocumentApproval",
					UtilMisc.toMap("documentApprovalId", documentApprovalId),
					false);
		} catch (GenericEntityException e) {
			// UtilMisc.toMap("errMessage", e.getMessage()), locale));
			// return "Document Approved";
			e.printStackTrace();
		}
		return documentApproval;
	}
}
