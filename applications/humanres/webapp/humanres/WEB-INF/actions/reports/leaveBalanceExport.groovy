import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
leaveType = parameters.leaveType
expr = exprBldr.AND() {
			NOT_EQUAL(employmentStatusEnumId: "15")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
leaveBalancelist = [];
leaveBalance = delegator.findList("LeavesBalanceView", expr, null, null, findOptions, false);

leaveBalance.eachWithIndex { leaveBalanceItem, index ->
name = delegator.findOne("Person", [partyId : leaveBalanceItem.partyId], false);


Payroll = name.employeeNumber;
fname = name.firstName;
lname = name.lastName;
broughtForwardA = leaveBalanceItem.annualCarriedOverDays;
broughtForwardC = leaveBalanceItem.compassionateCarryOverLeaveDays;
acruedA = leaveBalanceItem.annualaccruedDays;
acruedC = leaveBalanceItem.compassionateAllocatedLeaveDays;
takenA = leaveBalanceItem.annualUsedLeaveDays;
takenC = leaveBalanceItem.compassionateUsedLeaveDays;
lostA = leaveBalanceItem.annualLostLeaveDays;
lostC = "0";
balanceA = leaveBalanceItem.annualAvailableLeaveDays;
balanceC = leaveBalanceItem.compassionateAvailableLeaveDays;

if(leaveType == 'ANNUAL'){
leaveBalancelist.add([Payroll :Payroll, fname :fname, lname : lname, broughtForward : broughtForwardA, acrued : acruedA, taken : takenA, lost : lostA, balance : balanceA]);
}
else if(leaveType == 'COMPASSIONATE'){
leaveBalancelist.add([Payroll :Payroll, fname :fname, lname : lname, broughtForward : broughtForwardC, acrued : acruedC, taken : takenC, lost : lostC, balance : balanceC]);
}


  
  
 }
 
 

context.leaveBalancelist = leaveBalancelist;

