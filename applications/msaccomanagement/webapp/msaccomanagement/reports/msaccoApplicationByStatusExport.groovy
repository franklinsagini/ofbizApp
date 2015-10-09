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

statusName = org.ofbiz.msaccomanagement.MSaccoManagementServices.getCardStatusName(statusLong);

println("############STATUS NAME########"+statusName); 

msaccoApplications = [];



	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			               EQUALS(cardStatusId : statusLong)
			
		}
EntityFindOptions findOptions = new EntityFindOptions();

msacco = delegator.findList("MSaccoApplication",  expr, null, ["createdStamp ASC"], findOptions, false);
 
 msacco.eachWithIndex { msaccoItem, index ->
	no = msaccoItem.mobilePhoneNumber;

 createdTime = msaccoItem.createdStamp;
 party = msaccoItem.partyId;
 name = delegator.findOne("Member", [partyId : party], false);
 payrollNumber =  name.getString("payrollNumber");
memberNumber =  name.getString("memberNumber");
 fname = name.getString("firstName");
 lname = name.getString("lastName");
 idNo = name.getString("idNumber");
 
 accId = msaccoItem.memberAccountId;
 acc = delegator.findOne("MemberAccount", [memberAccountId : accId], false);
 accNo = acc.getString("accountNo");

 msaccoApplications.add([createdTime :createdTime,payrollNumber :payrollNumber, memberNumber :memberNumber, fname :fname, lname :lname, phone : no, IdNo : idNo, accNo : accNo]);
 }
            
context.msaccoApplications = msaccoApplications;
 context.statusName = statusName
 context.dateStartDate = dateStartDate
context.dateEndDate = dateEndDate


 