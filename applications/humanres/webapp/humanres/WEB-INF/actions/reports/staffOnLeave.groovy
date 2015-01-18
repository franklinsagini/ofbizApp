import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();


now = Calendar.getInstance().getTime().toString();
//noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Timestamp(now.getTime());

xpr = exprBldr.AND() {
			LESS_THAN(fromDate: today)
			GREATER_THAN(thruDate: today)
			EQUALS(applicationStatus: "Approved")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);


partyId = parameters.partyId

if (partyId) {

  context.employees = delegator.findByAnd("EmployeeLeavesView", [partyId : parameters.partyId], null, false);
  return
}
//employees = delegator.findList("EmployeeLeavesView", null, null, null, null, false);
employees = delegator.findList("EmployeeLeavesView", expr, null, ["fromDate ASC"], findOptions, false);
context.employees = employees
