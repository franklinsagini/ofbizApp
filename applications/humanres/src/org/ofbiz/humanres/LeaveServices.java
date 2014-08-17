package org.ofbiz.humanres;

import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.ParseException;
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

public class LeaveServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);

	public static String forwardApplication(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		// Delegator delegator = dctx.getDelegator();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// request.getParameter(arg0)
		String partyId = (String) request.getParameter("partyId");
		String leaveTypeId = (String) request.getParameter("leaveTypeId");
		Timestamp fromDate = null;
		// LocalDateTime fromDate = null;
		try {

			// fromDates = (Date)(new
			// SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")));
			fromDate = new Timestamp(
					((Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request
							.getParameter("fromDate")))).getTime());
			// fromDate = new LocalDateTime(fromDates);
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		GenericValue leaveApplication = null;
		List<GenericValue> leaveApplicationELI = null;

		log.info(" From Date : " + fromDate);

		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("leaveTypeId",
								EntityOperator.EQUALS, leaveTypeId),
						EntityCondition.makeCondition("fromDate",
								EntityOperator.EQUALS, new java.sql.Date(
										fromDate.getTime()))),
						EntityOperator.AND);

		try {
			leaveApplicationELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		GenericValue documentApproval = null;
		String documentApprovalId = null, workflowDocumentTypeId = null, organizationUnitId = null;
		for (GenericValue genericValue : leaveApplicationELI) {
			// Get Unit and Document
			organizationUnitId = genericValue.getString("organizationUnitId");
			workflowDocumentTypeId = genericValue
					.getString("workflowDocumentTypeId");
			documentApprovalId = genericValue.getString("documentApprovalId");
			}
			documentApproval =  doFoward(delegator, organizationUnitId,
					workflowDocumentTypeId, documentApprovalId);
		
		if (documentApproval == null) {
			// Loan Approved
			result.put("fowardMessage", "");
		} else {
			// Foward Loan Application by setting the documentApprovalId
			leaveApplication.set("documentApprovalId",
					documentApproval.getString("documentApprovalId"));

			if ((documentApproval.getString("nextLevel") == null)
					|| (documentApproval.getString("nextLevel").equals(""))) {
				leaveApplication.set("approvalStatus",
						documentApproval.getString("stageAction"));
			} else {
				leaveApplication.set("approvalStatus",
						documentApproval.getString("stageAction")
								+ " (APPROVED)");
			}

			// Set Responsible
			// responsibleEmployee
			leaveApplication.set("responsibleEmployee",
					documentApproval.getString("responsibleEmployee"));

			try {
				delegator.createOrStore(leaveApplication);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			result.put("fowardMessage",
					documentApproval.getString("stageAction"));

		}

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
		return "";// result.get("fowardMessage").toString();

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

			if ((nextLevelId == null) || (nextLevelId.equals(""))) {
				return null;
			} else {
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

	// =====================================//

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

	public static String getworkflowDocumentTypeIdtry(GenericValue firstLevel) {
		String unitId = "";
		String partyId = firstLevel.getString("partyId");

		Delegator delegator = firstLevel.getDelegator();

		List<GenericValue> getEmplUnitELI = null;
		try {
			getEmplUnitELI = delegator.findList(
					"UnitEmployeeMap",
					EntityConditionListBase("partyId", partyId, "unitId",
							unitId), null, null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueUnit : getEmplUnitELI) {
			unitId = genericValueUnit.getString("organizationUnitId");
		}
		// ///////////////////////////////////////////////////

		return unitId;

	}

	private static EntityCondition EntityConditionListBase(String string,
			String partyId, String string2, String unitId) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getEmplUnit(GenericValue person) {
		String unitId = "";
		String partyId = person.getString("partyId");

		Delegator delegator = person.getDelegator();

		List<GenericValue> getEmplUnitELI = null;
		try {
			getEmplUnitELI = delegator.findList("UnitEmployeeMap",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		for (GenericValue genericValueUnit : getEmplUnitELI) {
			unitId = genericValueUnit.getString("organizationUnitId");
		}
		// ///////////////////////////////////////////////////

		return unitId;

	}

	public static String getLeaveAppointmentDate(GenericValue person) {
		String appointmentdate = "";

		String partyId = person.getString("partyId");

		Delegator delegator = person.getDelegator();

		List<GenericValue> getLeaveAppointmentDateELI = null;

		try {
			getLeaveAppointmentDateELI = delegator.findList("Person",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : getLeaveAppointmentDateELI) {
			appointmentdate = genericValue.getString("appointmentdate");
		}

		return appointmentdate;

	}

	public static String getpartyIdFrom(GenericValue party) {
		String partyIdFromV = "";

		String partyId = party.getString("partyId");

		Delegator delegator = party.getDelegator();

		List<GenericValue> employmentsELI = null;

		try {
			employmentsELI = delegator.findList("Employment",
					EntityCondition.makeCondition("partyIdTo", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : employmentsELI) {
			partyIdFromV = genericValue.getString("partyIdFrom");
		}

		return partyIdFromV;
	}

	public static String getSupervisorLevel(GenericValue party) {
		String superVisorLevelValue = "";

		// GenericValue superVisorLevel = null;
		String partyId = party.getString("partyId");

		Delegator delegator = party.getDelegator();

		// try {
		// superVisorLevel = delegator.findOne("SupervisorLevel",
		// UtilMisc.toMap("partyId", partyId), false);
		// } catch (GenericEntityException e2) {
		// e2.printStackTrace();
		// }

		List<GenericValue> levelsELI = null; // =

		try {
			levelsELI = delegator.findList("SupervisorLevel",
					EntityCondition.makeCondition("partyId", partyId), null,
					null, null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		for (GenericValue genericValue : levelsELI) {
			superVisorLevelValue = genericValue.getString("supervisorLevel");
		}

		// superVisorLevelValue = superVisorLevel.getString("supervisorLevel");

		return superVisorLevelValue;
	}

}
