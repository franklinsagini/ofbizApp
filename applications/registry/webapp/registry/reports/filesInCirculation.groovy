
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

startDate = parameters.dateFrom
endDate = parameters.dateB
activityId = parameters.activityId


dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

 sqlStartDate = new java.sql.Timestamp(dateStartDate.getTime());
 sqlEndDate = new java.sql.Timestamp(dateEndDate.getTime());



exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()





	expr = exprBldr.AND() {
			NOT_EQUAL(currentPossesser: "REGISTRY")
			GREATER_THAN_EQUAL_TO(issueDate: sqlStartDate)
			LESS_THAN_EQUAL_TO(issueDate: sqlEndDate)
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.activities = delegator.findList("RegistryFiles", expr, null, null, findOptions, false)
context.sqlStartDate = sqlStartDate	
context.sqlEndDate = sqlEndDate	
  