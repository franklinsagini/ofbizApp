

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
activityId = parameters.activityId
fileRequestByActivitylist = [];

dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

 sqlStartDate = new java.sql.Timestamp(dateStartDate.getTime());
 sqlEndDate = new java.sql.Timestamp(dateEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()





	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(actionDate: sqlStartDate)
			LESS_THAN_EQUAL_TO(actionDate: sqlEndDate)
			EQUALS(Reason : activityId)
			EQUALS(fileActionTypeId : "Request")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.activityId = activityId
fileRequestByActivity = delegator.findList("RegistryFileLogs", expr, null, ["actionDate ASC"], findOptions, false)

 fileRequestByActivity.eachWithIndex { fileRequestByActivityItem, index ->
 party = fileRequestByActivityItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 actionBy = delegator.findOne("Person", [partyId : fileRequestByActivityItem.actionBy], false);
 
 fileOwner = "${member.firstName}${member.lastName}";
 requestedBy = "${actionBy.firstName}${actionBy.lastName}";
 
  timein  = fileRequestByActivityItem.actionDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 time = dateStringin;
 
 
 
 
 fileRequestByActivitylist.add([fileOwner :fileOwner, requestedBy :requestedBy, time : time]);
 }
 
 
context.fileRequestByActivitylist = fileRequestByActivitylist;