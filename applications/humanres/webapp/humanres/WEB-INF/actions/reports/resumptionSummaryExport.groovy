import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

action = request.getParameter("action");

resumptionSummarylist = [];

now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN_EQUAL_TO(fromDate: today)
			GREATER_THAN_EQUAL_TO(thruDate: today)
			EQUALS(approvalStatus: "Approved")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);
resumptionSummary = delegator.findList("EmplLeave", expr, null, ["thruDate ASC"], findOptions, false);
 
 resumptionSummary.eachWithIndex { resumptionSummaryItem, index ->
 staff = delegator.findOne("Person", [partyId : resumptionSummaryItem.partyId], false);
 leaveType = delegator.findOne("EmplLeaveType", [leaveTypeId : resumptionSummaryItem.leaveTypeId], false);
 
 payrollNo = staff.employeeNumber;
 name = "${staff.firstName} ${staff.lastName}";
 leaveType = leaveType.description;
 leaveDuration = resumptionSummaryItem.leaveDuration;
 leaveEnd = resumptionSummaryItem.thruDate;
 resumptionDate = resumptionSummaryItem.resumptionDate;
 
 
 resumptionSummarylist.add([payrollNo :payrollNo, name :name, leaveType : leaveType, leaveDuration : leaveDuration,
 leaveEnd : leaveEnd, resumptionDate : resumptionDate]);
 }
 
 
context.resumptionSummarylist = resumptionSummarylist;
