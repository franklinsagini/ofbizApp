import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
payrollPeriodId = parameters.payrollPeriodId;


person = delegator.findOne("Person", [partyId : partyId], false);
//payrollNo = member.payrollNumber;
context.person = person;

period = delegator.findOne("PayrollPeriod", [payrollPeriodId : payrollPeriodId], false);

context.period = period;

staffPayrollList = delegator.findByAnd("StaffPayroll",  [partyId : partyId, payrollPeriodId : payrollPeriodId], null, false);

def staffPayrollId

staffPayrollList.eachWithIndex { staffPayrollItem, index ->
	staffPayrollId = staffPayrollItem.staffPayrollId
}

println "############### Staff Payroll ID "+staffPayrollId 
println "############### Party  ID "+partyId 
println "############### Payroll Period  ID "+payrollPeriodId 

staffPayrollELementList = delegator.findByAnd("StaffPayrollElements",  [staffPayrollId : staffPayrollId], null, false);

	context.staffPayrollELementList = staffPayrollELementList;


