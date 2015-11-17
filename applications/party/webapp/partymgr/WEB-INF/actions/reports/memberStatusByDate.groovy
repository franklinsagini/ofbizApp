import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;


startDate = parameters.startDate
endDate = parameters.endDate
memberStatusId = parameters.memberStatusId
branchId = parameters.branchId
stationId = parameters.stationId
introducingMember = parameters.introducingMember
memberTypeId = parameters.memberTypeId

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;



if ((endDate?.trim())){
    dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
    sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

print "formatted Date"


println "RRRRRRRRRRRRRR EAL DATES !!!!!!!!!!!!!"

println endDate




 


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()

if ((sqlEndDate)){
    expr = null;
    if ((memberStatusId)){
        expr = exprBldr.AND() {
            EQUALS(memberStatusId: memberStatusId.toLong())
            LESS_THAN_EQUAL_TO(joinDate: sqlEndDate)
        }
    }
}       



EntityFindOptions findOptions = new EntityFindOptions();
membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)

context.membersList = membersList