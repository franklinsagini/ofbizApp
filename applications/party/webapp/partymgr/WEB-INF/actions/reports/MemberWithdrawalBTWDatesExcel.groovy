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
withDrawalStatus = parameters.withDrawalStatus

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
	
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	
}
//(endDate != null) || 
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
	
}



print "formatted Date"

println "RRRRRRRRRRRRRR EAL DATES !!!!!!!!!!!!!"
println startDate
println endDate

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()
	
if (withDrawalStatus){
			expr = exprBldr.AND() {
				EQUALS(withdrawalstatus: withDrawalStatus)
			}
}
	     
if ((sqlStartDate) && (sqlEndDate)){
		    expr = exprBldr.AND() {
			    GREATER_THAN_EQUAL_TO(dateApplied: startDateTimestamp)
				LESS_THAN_EQUAL_TO(dateApplied: endDateTimestamp)
		}
}   
	     
 if((sqlStartDate) && (sqlEndDate) && (withDrawalStatus)){
	         expr = exprBldr.AND() {
	            GREATER_THAN_EQUAL_TO(dateApplied: startDateTimestamp)
				LESS_THAN_EQUAL_TO(dateApplied: endDateTimestamp)
				EQUALS(withdrawalstatus: withDrawalStatus)
		}
}
	
 
EntityFindOptions findOptions = new EntityFindOptions();
//findOptions.setMaxRows(100);
membersList = delegator.findList("MemberWithdrawal", expr, null, ["dateApplied ASC"], findOptions, false)


context.membersList = membersList


