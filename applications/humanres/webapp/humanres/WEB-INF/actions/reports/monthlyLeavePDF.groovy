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

thru = parameters.thruDate
nowwThru=new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(thru);
thruToDate=new java.sql.Date(nowwThru.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(fromDate: fromToDate)
			LESS_THAN_EQUAL_TO(fromDate: thruToDate)
			EQUALS(approvalStatus: "Approved")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.activities = delegator.findList("EmplLeave", expr, null, ["fromDate ASC"], findOptions, false);
context.fromToDate=fromToDate
context.thruToDate=thruToDate

