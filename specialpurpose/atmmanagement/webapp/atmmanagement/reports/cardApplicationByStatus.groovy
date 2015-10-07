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

cardStatusId = parameters.cardStatusId

cardStatusIdLong = cardStatusId.toLong();

cardStatusId = parameters.cardStatusId

cardStatusIdLong = cardStatusId.toLong();

action = request.getParameter("action");



startDate = parameters.startDate
endDate = parameters.endDate

 status = parameters.cardStatusId
 statusLong = status.toLong();

action = request.getParameter("action");

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
}
//(endDate != null) ||
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

print "formatted Date"
//println dateStartDate
//println dateEndDate

    startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()


cardApplications = [];

	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			               EQUALS(cardStatusId : cardStatusIdLong) 
			
		}
		
EntityFindOptions findOptions = new EntityFindOptions();


 statusName = org.ofbiz.msaccomanagement.MSaccoManagementServices.getCardStatusId(cardStatusId);

card = delegator.findList("CardApplication", expr, null, ["createdStamp ASC"], findOptions, false);

context.card = card;
context.startDate = startDate;
context.endDate = endDate;



