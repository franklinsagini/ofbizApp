
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
returnedFilesPerDatelist = [];
dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

 sqlStartDate = new java.sql.Timestamp(dateStartDate.getTime());
 sqlEndDate = new java.sql.Timestamp(dateEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()





	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(actionDate: sqlStartDate)
			LESS_THAN_EQUAL_TO(actionDate: sqlEndDate)
			EQUALS(fileActionTypeId : "Release File")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
returnedFilesPerDate = delegator.findList("RegistryFileLogs", expr, null, ["actionDate ASC"], findOptions, false)






returnedFilesPerDate.eachWithIndex { returnedFilesPerDateItem, index ->
 releasedby = delegator.findOne("Person", [partyId : returnedFilesPerDateItem.actionBy], false);
 carriedby = delegator.findOne("Person", [partyId : returnedFilesPerDateItem.carriedBy], false);
 
 
 releasedBy = "${releasedby.firstName}  ${releasedby.lastName}";
 
 carriedBy =  "${carriedby.firstName}  ${carriedby.lastName}";
 timein = returnedFilesPerDateItem.actionDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 timeReleased = dateStringin;
 
 
 
 
 
 returnedFilesPerDatelist.add([releasedBy :releasedBy, carriedBy : carriedBy, timeReleased :timeReleased]);
 }
 
 
context.returnedFilesPerDatelist = returnedFilesPerDatelist;