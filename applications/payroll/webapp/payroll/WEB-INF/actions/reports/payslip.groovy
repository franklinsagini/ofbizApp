import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
payrollPeriodId = parameters.payrollPeriodId;


person = delegator.findOne("Person", [partyId : partyId], false);
//payrollNo = member.payrollNumber;
context.person = person;

period = delegator.findOne("PayrollPeriod", [payrollPeriodId : payrollPeriodId], false);

context.period = period;

paypointsList = delegator.findByAnd("PayPoints",  [partyId : partyId], null, false);

class Paypoint{
	def bankName;
	def brannchName;
	def accountNumber;	
}
def paypoint = new Paypoint();

paypointsList.eachWithIndex { payPointItem, index ->
	paypoint.bankName = payPointItem.bankName
	paypoint.brannchName = payPointItem.brannchName
	paypoint.accountNumber = payPointItem.accountNumber
}
context.paypoint = paypoint;

staffPayrollList = delegator.findByAnd("StaffPayroll",  [partyId : partyId, payrollPeriodId : payrollPeriodId], null, false);

def staffPayrollId

staffPayrollList.eachWithIndex { staffPayrollItem, index ->
	staffPayrollId = staffPayrollItem.staffPayrollId
}

println "############### Staff Payroll ID "+staffPayrollId 
println "############### Party  ID "+partyId 
println "############### Payroll Period  ID "+payrollPeriodId 
println "############### Payroll Period  ID "+payrollPeriodId
//Earnings
earningsList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, elementType : 'Payment'], ['elementCode'], false);

	context.earningsList = earningsList;
	
	
//Calculated
	
	calculatedList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, elementType : 'System Element'], ['elementCode'], false);
	
		context.calculatedList = calculatedList;

//PAYE
		payeList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'PAYE'], ['elementCode'], false);
	
		context.payeList = payeList;

//NHIF
		nhifList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'NHIF'], ['elementCode'], false);
	
		context.nhifList = nhifList;
		
//NSSF
		nssfList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'NSSF'], ['elementCode'], false);
	
		context.nssfList = nssfList;
		
//NSSF VOL
		nssfVolList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'NSSFVOL'], ['elementCode'], false);
	
		context.nssfVolList = nssfVolList;
		
//PENSION
		pensionList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'PENSION'], ['elementCode'], false);
	
		context.pensionList = pensionList;

//Deductions
		deductionsList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, elementType : 'Deduction'], ['elementCode'], false);
	
		context.deductionsList = deductionsList;
		
//Total Deductions
		totDeductionsList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'TOTDEDUCTIONS'], ['elementCode'], false);
	
		context.totDeductionsList = totDeductionsList;

//Net Pay
		netPayList = delegator.findByAnd("PayrollElementAndStaffPayrollElement",  [staffPayrollId : staffPayrollId, payrollElementId : 'NETPAY'], ['elementCode'], false);
	
		context.netPayList = netPayList;
	
//Pay Slip Message
		payslipMsgList = delegator.findByAnd("PayslipMessage",  [display : 'Y'], null, false);
		
			context.payslipMsgList = payslipMsgList;
	

