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
changedBy = parameters.changedBy

action = request.getParameter("action");

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
	 startDateTimestamp = new Timestamp(sqlStartDate.getTime());
}

if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
}

print "formatted Date"

   
	

ChangeLogs = [];


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()


  if (sqlEndDate){
      expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(changedDate: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(changedDate: endDateTimestamp)
			      NOT_EQUALS(changedByInfo : '')
		}
     }
       
   if (changedBy){
      expr = exprBldr.AND() {
			   EQUALS(changedByInfo : changedBy)
			      NOT_EQUALS(changedByInfo : '')
		}
     }
     
   if ((sqlEndDate) && (changedBy)){
      expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(changedDate: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(changedDate: endDateTimestamp)
			               EQUALS(changedByInfo : changedBy)
			                NOT_EQUALS(changedByInfo : '')
		}
     }  
		
EntityFindOptions findOptions = new EntityFindOptions();

changeLogs = delegator.findList("EntityAuditLog",  expr, null, ["changedDate ASC"], findOptions, false);
	 
	 changeLogs.eachWithIndex { changeLogsItem, index ->
          
         changeLogId = changeLogsItem.changedSessionInfo;
               visit = delegator.findOne("Visit", [visitId : changeLogId], false);
                     clientIpAddress = visit.getString("clientIpAddress");
                     clientHostName = visit.getString("clientHostName");
		 changeTime    = changeLogsItem.changedDate;
		 changedEntity = changeLogsItem.changedEntityName;
		 from = changeLogsItem.oldValueText;
		 to = changeLogsItem.newValueText;
		 ChangedBy = changeLogsItem.changedByInfo;
		 
		 println("##ChangedBy####"+ChangedBy)
		 println("##clientIpAddress####"+clientIpAddress)
		
      ChangeLogs.add([changeLogId :changeLogId,clientIpAddress :clientIpAddress, clientHostName : clientHostName, changeTime :changeTime, changedEntity :changedEntity, from : from, to : to, ChangedBy : ChangedBy]);
   
    }
            
context.ChangeLogs = ChangeLogs;
context.dateStartDate = dateStartDate
context.dateEndDate = dateEndDate


 