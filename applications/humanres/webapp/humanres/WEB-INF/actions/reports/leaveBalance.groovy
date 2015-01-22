import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

partyId = parameters.partyId
leaveType = parameters.leaveType
context.leaveType = leaveType
context.title = "Chai Sacco"

if (partyId) {
    employee = delegator.findOne("LeavesBalanceView", [partyId : partyId], false);
   context.employee = employee;
   return
}
expr = exprBldr.AND() {
			NOT_EQUAL(employmentStatusEnumId: "15")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
leaveBalancelist = [];
leaveBalances = delegator.findList("LeavesBalanceView", expr, null, null, findOptions, false);
context.leaveBalances = leaveBalances;
