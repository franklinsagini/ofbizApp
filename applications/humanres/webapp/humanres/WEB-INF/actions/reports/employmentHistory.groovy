import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];

partyId = parameters.partyId
if (partyId) {
   employee = delegator.findOne("Person", [partyId : partyId], false);
   context.employee = employee;

    context.history = delegator.findByAnd("EmploymentHistory", [partyId : parameters.partyId], null, false);
}

