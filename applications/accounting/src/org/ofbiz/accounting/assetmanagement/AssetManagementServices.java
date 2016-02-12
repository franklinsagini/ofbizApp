/**
 * 
 */
package org.ofbiz.accounting.assetmanagement;

import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * @author samoei
 *
 */
public class AssetManagementServices {
	
	public static final String module = AssetManagementServices.class.getName();
	
	
	public static Map<String, Object>newAssetAssignment(DispatchContext dctx, Map<String, Object>context){
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String assetId = (String) context.get("assetId");
		String assignedTo = (String) context.get("assignedTo");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		String assetAssignmentStatusId = getAssignedStatusId(delegator, "ASSIGNED");
		//Get The Asset Object
		
		GenericValue asset = getAssest(delegator, assetId);
		
		//Check if this assest has already been asigned
		if (asset.getString("assetAssignmentStatusId").equalsIgnoreCase(assetAssignmentStatusId)) {
			Map<String, Object> errorResult = ServiceUtil.returnError("THIS ASSET ["+asset.getString("assetName") + "] HAS ALREADY BEEN ASSIGNED. IT MUST BE SURRENDERED FIRST BEFORE ANOTHER ASSIGNMENT");
			errorResult.put("assetId", assetId);
			return errorResult;
		}
		
		
		
		GenericValue assetAssignment = null;
		String assetAssignmentId = null;
		
		assetAssignment = delegator.makeValue("AssetAssignment");
		assetAssignmentId = delegator.getNextSeqId("AssetAssignment");
		assetAssignment.put("assetAssignmentId", assetAssignmentId);
		assetAssignment.put("assetId", assetId);
		assetAssignment.put("dateAssigned", UtilDateTime.nowTimestamp());
		assetAssignment.put("assignedTo", assignedTo);
		assetAssignment.put("assignedBy", userLogin.getString("userLoginId"));
		assetAssignment.put("assignmentStatus", "ASSIGNED");
		
		try {
			assetAssignment.create();
			updateAssestAssignmentStatus(delegator, asset, assetAssignmentStatusId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Map<String, Object> successResult = ServiceUtil.returnSuccess();
		successResult.put("assetId", assetId);
		return successResult;
	}


	private static String getAssignedStatusId(Delegator delegator, String statusName) {
		GenericValue assetAssignmentStatus = null;
		List<GenericValue>listAssetAssignmentStatus = null;

		EntityConditionList<EntityExpr> cond = EntityCondition.makeCondition(UtilMisc.toList(
				EntityCondition.makeCondition("statusName", EntityOperator.EQUALS, statusName)
				));
		try {
			listAssetAssignmentStatus = delegator.findList("AssetAssignmentStatus", cond, null, null, null, false);
			assetAssignmentStatus = listAssetAssignmentStatus.get(0);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return assetAssignmentStatus.getString("assetAssignmentStatusId");
	}


	private static void updateAssestAssignmentStatus(Delegator delegator, GenericValue asset, String assetAssignmentStatusId) {
		asset.put("assetAssignmentStatusId", assetAssignmentStatusId);
		
		try {
			asset.store();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	private static GenericValue getAssest(Delegator delegator, String assetId) {
		GenericValue asset = null;
		try {
			asset = delegator.findOne("Assets", UtilMisc.toMap("assetId", assetId), false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return asset;
	}

}
