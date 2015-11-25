import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 


now =parameters.fromDate
noww = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(now);
fromToDate = new java.sql.Date(noww.getTime());


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN_EQUAL_TO(fromDate: fromToDate)
			EQUALS(approvalStatus: "Approved")
		}
		
		
		
 EntityFindOptions findOptions = new EntityFindOptions();
//findOptions.setMaxRows(100);
leaveBalancelist = [];
leaveReportList = delegator.findList("EmplLeave", expr, null, ["leaveId ASC"], findOptions, false)
  leaveReportList.eachWithIndex{reportItem, index ->
       
        personDetail =  delegator.findOne("Person",[partyId : reportItem.partyId ],false);
            firstname =  personDetail.firstName
            payRollno =  personDetail.employeeNumber
            lastname  =  personDetail.lastName
            name = firstname+"  "+lastname
            
         approverDetail =  delegator.findOne("Person",[partyId : reportItem.responsibleEmployee ],false);
            Appfirstname =  approverDetail.firstName
            Applastname  =  approverDetail.lastName
            Appname = Appfirstname+"  "+Applastname    
      
         handedOverDetail =  delegator.findOne("Person",[partyId : reportItem.handedOverTo],false);
             overNameF =  handedOverDetail.firstName
             overNameL =  handedOverDetail.lastName
             nameOver = overNameF+"  "+overNameL
             
         leaveDetail =  delegator.findOne("EmplLeaveType",[leaveTypeId : reportItem.leaveTypeId],false);              
              leaveName = leaveDetail.description
                  
          leaveReason =  delegator.findOne("EmplLeaveReasonType",[emplLeaveReasonTypeId : reportItem.emplLeaveReasonTypeId],false);              
              leaveReasonName = leaveReason.reason
            
            leaveListBuilder = [
              payrollNo :payRollno,
              name : name,
              leaveTypeId :leaveName,
              emplLeaveReasonTypeId : leaveReasonName,
              fromDate : reportItem.fromDate,
              thruDate : reportItem.thruDate,
              leaveDuration : reportItem.leaveDuration,
              approvalStatus : reportItem.approvalStatus,
              rejectReason : reportItem.rejectReason,
              responsibleEmployee: Appname,
              handedOverTo : nameOver,
              createdDate : reportItem.createdDate
              
              ]   
           
           leaveBalancelist.add(leaveListBuilder);             
  }
 
context.leaveReportList = leaveReportList
context.leaveBalancelist =leaveBalancelist 
context.fromToDate=fromToDate





