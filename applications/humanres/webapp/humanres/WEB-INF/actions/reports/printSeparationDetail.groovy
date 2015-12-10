	import org.ofbiz.entity.util.EntityUtil;
	import org.ofbiz.entity.condition.EntityCondition;
	import org.ofbiz.entity.condition.EntityConditionBuilder;
	import org.ofbiz.entity.condition.EntityConditionList;
	import org.ofbiz.entity.condition.EntityExpr;
	import org.ofbiz.entity.condition.EntityOperator;
	import org.ofbiz.base.util.UtilDateTime;
	import org.ofbiz.entity.util.EntityFindOptions;
	import java.text.SimpleDateFormat; 


    separationDetailId = parameters.separationDetailId

    separationDetail = delegator.findByAnd("SeparationDetail", [separationDetailId : separationDetailId ],null,false);
                   separationDetail.eachWithIndex{separationItem , index->
                        context.salary = separationItem.salary
                        context.leaveAllowance = separationItem.leaveAllowance
                        context.goldenHandShake = separationItem.goldenHandShake
                        context.transportAllowance = separationItem.transportAllowance
                        context.servicePay = separationItem.servicePay
                        context.total = separationItem.total
                        
                         context.PAYE = separationItem.PAYE
                         context.lostItemAmount = separationItem.lostItemAmount
                         context.noticePeriod = separationItem.noticePeriod
                         context.lienOfNotice = separationItem.lienOfNotice
                         context.staffLoans = separationItem.staffLoans
                         context.amountPayableToChai = separationItem.amountPayableToChai
                         
                   }
    
    context.separationDetail  = separationDetail
    
    party = separationDetail.partyId
    context.party = party
    
    personDetail = delegator.findByAnd("Person", [partyId : party], null,false);
             personDetail.eachWithIndex{personDetailtem, index->
                context.payroll = personDetailtem.employeeNumber
                context.firstName = personDetailtem.firstName
                context.lastName = personDetailtem.lastName
                context.idNumber = personDetailtem.nationalIDNumber
                context.dateOfAppointment = personDetailtem.appointmentdate
             }
    context.personDetail = personDetail
   
    
    branch = personDetail.branchId
    branchDetail = delegator.findByAnd("PartyGroup", [partyId : branch], null, false);
    context.branchDetail = branchDetail
     context.branchName = branchDetail.groupName
    