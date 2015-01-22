import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 


action = request.getParameter("action");

scheduledLeaveslist = [];
now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			GREATER_THAN(fromDate: today)
			EQUALS(applicationStatus: "Approved")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);

scheduledLeaves = delegator.findList("EmployeeLeavesView", expr, null, ["fromDate ASC"], findOptions, false);

scheduledLeaves.eachWithIndex { scheduledLeavesItem, index ->
 staff = delegator.findOne("Person", [partyId : scheduledLeavesItem.partyId], false);
 leaveType = delegator.findOne("EmplLeaveType", [leaveTypeId : scheduledLeavesItem.leaveTypeId], false);
 
 payrollNo = staff.employeeNumber;
 name = "${staff.firstName} ${staff.lastName}";
 leaveType = leaveType.description;
 
 start = scheduledLeavesItem.fromDate;
 duration = scheduledLeavesItem.leaveDuration;
 end = scheduledLeavesItem.thruDate;
 
 
 scheduledLeaveslist.add([payrollNo :payrollNo, name :name, leaveType : leaveType, start : start,
 duration : duration, end : end]);
 }
 
 
context.scheduledLeaveslist = scheduledLeaveslist;