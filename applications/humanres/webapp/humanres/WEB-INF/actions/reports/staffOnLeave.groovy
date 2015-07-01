import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 





now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN(fromDate: today)
			GREATER_THAN(thruDate: today)
			EQUALS(applicationStatus: "Approved")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);

employees = delegator.findList("EmployeeLeavesView", expr, null, ["fromDate ASC"], findOptions, false);
context.employees = employees
