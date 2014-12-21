
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 



startDate = parameters.startDate
endDate = parameters.endDate

dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

java.sql.Date sqlStartDate = new java.sql.Date(dateStartDate.getTime());
java.sql.Date sqlEndDate = new java.sql.Date(dateEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()



if (startDate) {

	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(actionDate: sqlStartDate)
			LESS_THAN_EQUAL_TO(actionDate: sqlEndDate)
			EQUALS(fileActionTypeId : "Release File")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
//context.activities = delegator.findList("RegistryFileLogs", expr, null, ["actionDate ASC"], findOptions, false)
	
  

context.activities = delegator.findByAnd("RegistryFileLogs", expr, null, false);

}
