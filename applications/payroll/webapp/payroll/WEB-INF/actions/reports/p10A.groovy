import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;

import org.ofbiz.entity.Delegator;

payrollYearId = parameters.payrollYearId

payeMap = [:];
grossMap = [:];
p10AList = [];
staffList=[:];
totalPayeAmount = BigDecimal.ZERO
totalGrossAmount = BigDecimal.ZERO

year = delegator.findOne("PayrollYear", [payrollYearId : payrollYearId], false);
context.year = year;


yearList = delegator.findByAnd("P10AReport",  [payrollYearId : payrollYearId], ['employeeNumber'], false);
context.yearList = yearList;

List mainAndExprs = FastList.newInstance();
mainAndExprs.add(EntityCondition.makeCondition("payrollYearId", EntityOperator.EQUALS, payrollYearId));
componentList = delegator.findList("P10AReport", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND),
	 UtilMisc.toSet("payrollYearId", "yearName", "employeeNumber", "pinNumber", "firstName", "lastName", "payrollElementId", "amount"), UtilMisc.toList("payrollYearId"), null, false);

 staffList=[];
 componentList.each { component ->
	 def payrollElemId = component.payrollElementId
	 
	 if(payrollElemId == 'PAYE'){
		 payeMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, employeeNumber:component.employeeNumber,
			  pinNumber:component.pinNumber, firstName:component.firstName, lastName:component.lastName, payrollElementId:component.payrollElementId,
			  payeamount:component.amount, grossamount:new BigDecimal(0.0)]
		 p10AList.add(payeMap);
		 
		 staffList.add([employeeNumber:component.employeeNumber])
	 }
	 
	 if(payrollElemId == 'GROSSPAY'){
		 grossMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, employeeNumber:component.employeeNumber,
			  pinNumber:component.pinNumber, firstName:component.firstName, lastName:component.lastName, payrollElementId:component.payrollElementId,
			  payeamount:new BigDecimal(0.0), grossamount:component.amount]
		 p10AList.add(grossMap);
		 
		 staffList.add([employeeNumber:component.employeeNumber])
	 }
 }
 
 staffList=staffList.unique();
 System.out.println("<><><>><><><><><>###################<><><><><><><><>< "+staffList)

 
 def yearId
 def yearName
 def employeeNumber 
 def pinNumber
 def firstName
 def lastName
 def payrollElementId
 
 class P10AItem{
	 def yearId
	 def yearName
	 def employeeNumber 
	 def pinNumber
	 def firstName
	 def lastName
	 def payrollElementId
	 def totalPayeAmount;
	 def totalGrossAmount;
}
 
 p10AItemsList = []
 
 totalPayeAmount  = new BigDecimal(0.0);
 totalGrossAmount  = new BigDecimal(0.0);
 
 class StaffDetails{
	 def pinNumber;
	 def firstName;
	 def lastName;
 }
 
 staffList.each { component ->
	 System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii "+component.employeeNumber)
	 
	 person = delegator.findByAnd("Person", [employeeNumber : component.employeeNumber], null, false);
	 

	 
	 staffData = new StaffDetails();
	 person.each{ personItem ->
		 staffData.pinNumber = personItem.pinNumber;
		 staffData.firstName = personItem.firstName;
		 staffData.lastName = personItem.lastName;
	 }
	 
	 
	 totalPayeAmount = new BigDecimal(0.0);
	 totalGrossAmount = new BigDecimal(0.0);
	 
	 p10AList.each{ p10A ->
		 
		 yearId = p10A.payrollYearId;
		 yearName = p10A.yearName;
		 employeeNumber = p10A.employeeNumber;
		 pinNumber = p10A.pinNumber;
		 firstName = p10A.firstName;
		 lastName = p10A.lastName;
		 payrollElementId = p10A.payrollElementId;
		 
		 if(p10A.employeeNumber==component.employeeNumber){
			 
			 println(" PAYEEEEEEEEEEEEEE  "+p10A.payeamount);
			 println(" GROSSSS            "+p10A.grossamount);

			 totalPayeAmount = totalPayeAmount.add(p10A.payeamount.toBigDecimal());
			 totalGrossAmount = totalGrossAmount.add(p10A.grossamount.toBigDecimal());		 
		}
	 }
	 p10AItem = new P10AItem();
	 
	 p10AItem.yearId = yearId;
	 p10AItem.yearName = yearName;
	 p10AItem.employeeNumber = component.employeeNumber;
	 p10AItem.pinNumber = staffData.pinNumber;
	 p10AItem.firstName = staffData.firstName;
	 p10AItem.lastName = staffData.lastName;
	 p10AItem.payrollElementId = payrollElementId;	 
	 p10AItem.totalPayeAmount = totalPayeAmount;
	 p10AItem.totalGrossAmount = totalGrossAmount;
	 
	 p10AItemsList << p10AItem
 }
 println("________________All ITEMS")
 p10AItemsList.each{ item ->
	 
	 println(" PAYEAMOUNT IS  "+item.employeeNumber+" "+item.totalPayeAmount)
	 println(" GROSSAMOUNT IS "+item.employeeNumber+" "+item.totalGrossAmount)
 }
 
 
 p10AList.sort {it.employeeNumber}
 context.p10AItemsList = p10AItemsList

 context.grossMap = grossMap;


	

	
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

