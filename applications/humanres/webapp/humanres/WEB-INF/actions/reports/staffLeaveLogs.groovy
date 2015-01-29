import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];

partyId = parameters.partyId
if (partyId) {
   staff = delegator.findOne("Person", [partyId : partyId], false);
   context.staff = staff;

    context.logs = delegator.findByAnd("LeaveStatusLog", [partyId : parameters.partyId], null, false);
}
