
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
partyId = parameters.partyId
filesInCirculationlist = [];
dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

 sqlStartDate = new java.sql.Timestamp(dateStartDate.getTime());
 sqlEndDate = new java.sql.Timestamp(dateEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()





	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(issueDate: sqlStartDate)
			LESS_THAN_EQUAL_TO(issueDate: sqlEndDate)
			EQUALS(currentPossesser : partyId)
			EQUALS(status : "ISSUED")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.partyId = partyId
filesInCirculation = delegator.findList("RegistryFiles", expr, null, ["issueDate ASC"], findOptions, false)
	
  filesInCirculation.eachWithIndex { filesInCirculationItem, index ->
 party = filesInCirculationItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 
 memberNumber = filesInCirculationItem.memberNumber;
 fileOwner = "${member.firstName}${member.lastName}";
 payroll = filesInCirculationItem.payrollNumber;
 
  timein  = filesInCirculationItem.issueDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 time = dateStringin;
 
 
 
 
 filesInCirculationlist.add([fileOwner :fileOwner, memberNumber :memberNumber, payroll : payroll, time : time]);
 }
context.filesInCirculationlist = filesInCirculationlist;