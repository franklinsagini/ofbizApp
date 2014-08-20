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
import org.ofbiz.workflow.WorkflowServices;


public class LeaveServices {
	public static Logger log = Logger.getLogger(LeaveServices.class);

	public static String forwardApplication(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		// =============== primary Keys     ============//
		
		String partyId = (String) request.getParameter("partyId");
		String leaveTypeId = (String) request.getParameter("leaveTypeId");
		String leaveId = (String) request.getParameter("leaveId");
		Timestamp fromDate = null;
		try {
			fromDate = new Timestamp(
					((Date) (new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("fromDate")))).getTime());
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// =============== primary Keys     ============//
		
		List<GenericValue> leaveApplicationELI = null;
		GenericValue leave = null;
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>From Date : " + fromDate);

		EntityConditionList<EntityExpr> leaveConditions = EntityCondition
				.makeCondition(UtilMisc.toList(EntityCondition.makeCondition(
						"partyId", EntityOperator.EQUALS, partyId),
						EntityCondition.makeCondition("leaveTypeId",EntityOperator.EQUALS, leaveTypeId),
						EntityCondition.makeCondition("fromDate",
								EntityOperator.EQUALS, new java.sql.Date(fromDate.getTime()))),
						EntityOperator.AND);
		
		log.info(" Date : "+fromDate);
		log.info(" Leave Type : "+leaveTypeId);
		log.info(" Party : "+partyId);

		try {
			leaveApplicationELI = delegator.findList("EmplLeave",
					leaveConditions, null, null, null, false);
		} catch (GenericEntityException e2) {
			//e2.printStackTrace();
			return "Cannot Get Leave Application";
		}
		
		//String documentApprovalId = null, workflowDocumentTypeId = null, organizationUnitId = null;
		for (GenericValue genericValue : leaveApplicationELI) {
			// Get Unit and Document
				leave = genericValue;
			}
			String organizationUnitId = leave.getString("organizationUnitId");
			String workflowDocumentTypeId = leave.getString("workflowDocumentTypeId");
			String documentApprovalId = leave.getString("documentApprovalId");
			
			GenericValue documentApproval = null;
			documentApproval =  WorkflowServices.doFoward(delegator, organizationUnitId,	workflowDocumentTypeId, documentApprovalId);
		log.info("=====================" +documentApproval);

		if (documentApproval == null) {
			// Leave Approved
			result.put("fowardMessage", "");
		} else {
			leave.set("documentApprovalId", documentApproval.getString("documentApprovalId"));

			if ((documentApproval.getString("nextLevel") == null)|| (documentApproval.getString("nextLevel").equals(""))) {
				leave.set("approvalStatus", documentApproval.getString("stageAction"));
				leave.set("applicationStatus","LEAVE_APPROVED"); // Employee to go for leave.
			} else {
				leave.set("approvalStatus", documentApproval.getString("stageAction")	+ " (APPROVED)");
			}

			// Set Responsible
			// responsibleEmployee
			leave.set("responsibleEmployee",	documentApproval.getString("responsibleEmployee"));
			//}
			try {
				delegator.createOrStore(leave);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			result.put("fowardMessage",	documentApproval.getString("stageAction"));

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


		public static String getEmplUnit(GenericValue person) {
		String organizationUnitId = "";
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
			organizationUnitId = genericValueUnit.getString("organizationUnitId");
		}
		// ///////////////////////////////////////////////////

		return organizationUnitId;

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
