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
    context.separationDetail  = separationDetail
    
    party = separationDetail.partyId
    context.party = party
    
    personDetail = delegator.findByAnd("Person", [partyId : party], null,false);
    context.personDetail = personDetail
    
    branch = personDetail.branchId
    branchDetail = delegator.findByAnd("PartyGroup", [partyId : branch], null, false);
    context.branchDetail = branchDetail
    