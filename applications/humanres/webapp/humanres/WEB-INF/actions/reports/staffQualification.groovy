import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];

partyId = parameters.partyId
if (partyId) {
   employee = delegator.findOne("Person", [partyId : partyId], false);
   context.employee = employee;

    context.depend = delegator.findByAnd("PartyQual", [partyId : parameters.partyId], null, false);
}

