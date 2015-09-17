import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 


now =parameters.fromDate
noww = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(now);
fromToDate = new java.sql.Date(noww.getTime());


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN_EQUAL_TO(fromDate: fromToDate)
			EQUALS(approvalStatus: "Approved")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.activities = delegator.findList("EmplLeave", expr, null, ["fromDate ASC"], findOptions, false);
context.fromToDate=fromToDate


