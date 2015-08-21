import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 




fromToDate=parameters.fromDate



monthLeave=delegator.findList("EmplLeave", [fromDate : fromToDate],null,null,null,false );

context.monthLeave=monthLeave
context.fromDate=fromDate
return


