	import org.ofbiz.entity.util.EntityUtil;
	import org.ofbiz.entity.condition.EntityCondition;
	import org.ofbiz.entity.condition.EntityConditionBuilder;
	import org.ofbiz.entity.condition.EntityConditionList;
	import org.ofbiz.entity.condition.EntityExpr;
	import org.ofbiz.entity.condition.EntityOperator;
	import org.ofbiz.base.util.UtilDateTime;
	import org.ofbiz.entity.util.EntityFindOptions;
	import java.text.SimpleDateFormat; 


    leaveId = parameters.leaveId

    leaveDetail = delegator.findOne("EmplLeave", [leaveId : leaveId ],false);
	            context.createdDate = leaveDetail.createdDate
	            context.handedOverTo = leaveDetail.handedOverTo
	            context.leaveTypeId = leaveDetail.leaveTypeId
	            context.emplLeaveReasonTypeId = leaveDetail.emplLeaveReasonTypeId
	            context.leaveDuration = leaveDetail.leaveDuration
	            context.leaveStatus = leaveDetail.approvalStatus
	            context.fromDate = leaveDetail.fromDate
	            context.thruDate = leaveDetail.thruDate
	            context.resumptionDate = leaveDetail.resumptionDate
	            context.rejectReason = leaveDetail.rejectReason
	            context.responsibleEmployee = leaveDetail.responsibleEmployee
	            context.responsibleEmployee = leaveDetail.responsibleEmployee
                         
     context.leaveDetail  = leaveDetail
     party = leaveDetail.partyId
     context.party = party
     handOverPerson = leaveDetail.handedOverTo
     context.handOverPerson = handOverPerson
     leaveTypeId = leaveDetail.leaveTypeId
     emplLeaveReasonTypeId =leaveDetail.emplLeaveReasonTypeId
     responsibleEmployeePer =  leaveDetail.responsibleEmployee
     
    
    personDetail = delegator.findByAnd("Person", [partyId : party], null,false);
             personDetail.eachWithIndex{personDetailtem, index->
                context.payroll = personDetailtem.employeeNumber
                context.firstName = personDetailtem.firstName
                context.lastName = personDetailtem.lastName
                context.idNumber = personDetailtem.nationalIDNumber
                context.dateOfAppointment = personDetailtem.appointmentdate
               }
    context.personDetail = personDetail
   
   
   handOverPersonDetail = delegator.findByAnd("Person", [partyId : handOverPerson], null,false);
             handOverPersonDetail.eachWithIndex{handOverPersonDetailtem, index->
                context.handOverPersonpayroll           = handOverPersonDetailtem.employeeNumber
                context.handOverPersonfirstName         = handOverPersonDetailtem.firstName
                context.handOverPersonlastName          = handOverPersonDetailtem.lastName
                context.handOverPersonidNumber          = handOverPersonDetailtem.nationalIDNumber
                context.handOverPersondateOfAppointment = handOverPersonDetailtem.appointmentdate
             }
             
   approverPersonDetail = delegator.findByAnd("Person", [partyId : responsibleEmployeePer], null,false);
             approverPersonDetail.eachWithIndex{approverPersonDetailtem, index->
                context.approverPersonDetailpayroll           = approverPersonDetailtem.employeeNumber
                context.approverPersonDetaifirstName         = approverPersonDetailtem.firstName
                context.approverPersonDetailastName          = approverPersonDetailtem.lastName
               
             }           
             
    leaveTypeDetail = delegator.findOne("EmplLeaveType",[leaveTypeId : leaveTypeId ],false);
    context.leaveTypeDetail = leaveTypeDetail
    
     
     leaveReason = delegator.findOne("EmplLeaveReasonType",[emplLeaveReasonTypeId : emplLeaveReasonTypeId ],false);
    context.leaveReason = leaveReason
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   // branch = personDetail.branchId
   // branchDetail = delegator.findByAnd("PartyGroup", [partyId : branch], null, false);
    //context.branchDetail = branchDetail
   // context.branchName = branchDetail.groupName
    