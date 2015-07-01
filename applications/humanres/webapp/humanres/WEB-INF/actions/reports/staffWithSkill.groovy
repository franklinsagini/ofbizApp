import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];
skillTypeId = parameters.skillTypeId

   employee = delegator.findByAnd("PartySkill", [skillTypeId : skillTypeId],null, false);
   context.employee = employee;
   context.skillTypeId = skillTypeId



