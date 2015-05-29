import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

List emplCond = [];

   employee = delegator.findByAnd("Person", [employmentStatusEnumId : "15"],null, false);
   context.employee = employee;



