import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

payrollYearId = parameters.payrollYearId


year = delegator.findOne("PayrollYear", [payrollYearId : payrollYearId], false);
context.year = year;


yearList = delegator.findByAnd("P10Report",  [payrollYearId : payrollYearId], ['sequence_no'], false);
context.yearList = yearList;
	
	

	
//Employer Details
employerDetailsList = delegator.findByAnd("EmployerDetails",  null, null, false);
class EmpDet{
	def pinNumber;
	def employer;
}
def empDet = new EmpDet();

employerDetailsList.eachWithIndex { empDetItem, index ->
	empDet.pinNumber = empDetItem.pinNumber
	empDet.employer = empDetItem.employer
}
context.empDet = empDet;

