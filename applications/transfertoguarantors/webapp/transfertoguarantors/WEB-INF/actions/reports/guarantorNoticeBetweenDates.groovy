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


startDate  = parameters.startDate
endDate = parameters.endDate

java.sql.Date sqlStartDate = null;
java.sql.Date sqlEndDate = null;

dateStartDate = null;
dateEndDate = null;

sqlStartDate = null;
sqlEndDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

//(endDate != null) ||
//if ((endDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(startDate);
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
	
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());

//}

startDateTimestamp = new Timestamp(sqlStartDate.getTime());
endDateTimestamp = new Timestamp(sqlEndDate.getTime());
//endDateTimestamp = new Timestamp(dateEndDate.getTime());
//dateEndDate.getTime()

def combinedList = [];
def noticeItem;

//6,10030,10013,7
exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
EntityFindOptions findOptions = new EntityFindOptions();


	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(noticeDate: startDateTimestamp)
		LESS_THAN_EQUAL_TO(noticeDate: endDateTimestamp)
		
	}
	noticesList = delegator.findList("GuarantorNoticesByDates", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);

	System.out.println "############################ Start Date "+startDateTimestamp;
	System.out.println "############################ End Date "+endDateTimestamp;
System.out.println "############################ The notices";
System.out.println "############################ The notices";
System.out.println "############################ The notices";
//System.out.println noticesList.length()

context.combinedList = noticesList
/** context.partyId = partyId **/

