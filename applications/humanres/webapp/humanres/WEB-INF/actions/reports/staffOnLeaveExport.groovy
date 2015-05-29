import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 


action = request.getParameter("action");

staffOnLeavelist = [];
now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Date(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN(fromDate: today)
			GREATER_THAN(thruDate: today)
			EQUALS(applicationStatus: "Approved")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(1000);

staffOnLeave = delegator.findList("EmployeeLeavesView", expr, null, ["fromDate ASC"], findOptions, false);

staffOnLeave.eachWithIndex { staffOnLeaveItem, index ->
 staff = delegator.findOne("Person", [partyId : staffOnLeaveItem.partyId], false);
 leaveType = delegator.findOne("EmplLeaveType", [leaveTypeId : staffOnLeaveItem.leaveTypeId], false);
 
 Payroll = staff.employeeNumber;
 name = "${staff.firstName} ${staff.lastName}";
 leaveType = leaveType.description;
 
 timein = staffOnLeaveItem.createdDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 
 
 applicationDate = dateStringin;
 duration = staffOnLeaveItem.leaveDuration;
 resumeDate = staffOnLeaveItem.resumptionDate;
 
 
 staffOnLeavelist.add([Payroll :Payroll, name :name, leaveType : leaveType, applicationDate : applicationDate,
 duration : duration, resumeDate : resumeDate]);
 }
 
 
context.staffOnLeavelist = staffOnLeavelist;