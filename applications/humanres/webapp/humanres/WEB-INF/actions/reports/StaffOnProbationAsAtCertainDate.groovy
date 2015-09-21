import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 



now =parameters.asFromDate
noww = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(now);
appointmentdateP = new java.sql.Date(noww.getTime());



exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			
			LESS_THAN_EQUAL_TO(appointmentdate: appointmentdateP)
			EQUALS(employmentStatusEnumId : "15")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.employee = delegator.findList("Person", expr, null, ["appointmentdate ASC"], findOptions, false);
context.appointmentdateP=appointmentdateP
