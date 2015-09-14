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

partyId = parameters.partyId

if ((partyId) && (partyId != null)){
	partyIdLong = partyId.toLong()
}

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


println "RRRRRRRRRRRRRR EAL DATES !!!!!!!!!!!!!"
println startDate
println endDate



exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()
//(startDate == null) || (endDate == null) ||
if (!(sqlEndDate)){
	expr = null;
	
	if (partyId){
		
		expr = exprBldr.AND() {
			EQUALS(partyId: partyIdLong)
			
		}
		
	}
}
// if (memberStatusId)
else if ((!partyId)){
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
	
	expr = exprBldr.AND() {
		GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
	}
}
 else{
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
	if (partyId){
		
		
		
		expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			EQUALS(partyId: partyIdLong)
			
		}
		
	}

}

EntityFindOptions findOptions = new EntityFindOptions();
//findOptions.setMaxRows(100);
memberTransactionsList = delegator.findList("AccountContributionCreditAmounts", expr, null, ["createdStamp ASC"], findOptions, false)


//myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);

context.creditsList = memberTransactionsList
if (startDate){
context.dateStartDate = dateStartDate
context.dateEndDate = dateEndDate
}
/** context.partyId = partyId **/

