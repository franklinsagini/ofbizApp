import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;

partyId = parameters.partyId
leaveTypeId = parameters.leaveTypeId
leaveStatus = parameters.leaveStatus


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()

 if (partyId){
		expr = exprBldr.AND() {
			EQUALS(partyId: partyId)
			NOT_EQUAL(applicationStatus: "NEW")
  }
 } 
	
  if (leaveTypeId){
		expr = exprBldr.AND() {
			EQUALS(leaveTypeId: leaveTypeId)
			NOT_EQUAL(applicationStatus: "NEW")
  }
 } 
	
  if (leaveStatus){
		expr = exprBldr.AND() {
			EQUALS(approvalStatus: leaveStatus)
			NOT_EQUAL(applicationStatus: "NEW")
   }
  } 
  
  if ((partyId) && (leaveStatus)){
		expr = exprBldr.AND() {
		    EQUALS(partyId: partyId)
			EQUALS(approvalStatus: leaveStatus)
   }
  } 	
  
  if ((partyId) && (leaveTypeId)){
		expr = exprBldr.AND() {
			EQUALS(partyId: partyId)
			EQUALS(leaveTypeId: leaveTypeId)
			NOT_EQUAL(applicationStatus: "NEW")
  }
 } 	
 
 if ((leaveTypeId) && (leaveStatus)){
		expr = exprBldr.AND() {
		   EQUALS(leaveTypeId: leaveTypeId)
			EQUALS(approvalStatus: leaveStatus)
			NOT_EQUAL(applicationStatus: "NEW")
   }
  } 	
 
 if ((!partyId) && (!leaveTypeId) && (!leaveStatus)){
			expr = exprBldr.AND() {
			EQUALS(leaveStatus: "DRAFT")
			NOT_EQUAL(applicationStatus: "NEW")
  }
 }
 
 if ((partyId) && (leaveTypeId) && (leaveStatus)){
			expr = exprBldr.AND() {
			 EQUALS(partyId: partyId)
			 EQUALS(leaveTypeId: leaveTypeId)
			EQUALS(approvalStatus: leaveStatus)
  }
 }
 
 EntityFindOptions findOptions = new EntityFindOptions();
//findOptions.setMaxRows(100);
leaveBalancelist = [];
leaveReportList = delegator.findList("EmplLeave", expr, null, ["leaveId ASC"], findOptions, false)

 
context.leaveReportList = leaveReportList
context.leaveBalancelist =leaveBalancelist 
 
 
 
 
 
 
 
