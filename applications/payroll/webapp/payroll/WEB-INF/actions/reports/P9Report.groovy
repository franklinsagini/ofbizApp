import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;

import org.ofbiz.entity.Delegator;
import java.math.BigDecimal;

payrollYearId = parameters.payrollYearId
partyId = parameters.partyId

basicPayMap = [:];
grossMap = [:];
pensionMap = [:];
nssfMap = [:];
taxablePayMap = [:];
mprMap = [:];
insuranceReliefMap = [:];
payeMap = [:];

P9List = [];

totalBasicPayAmount = BigDecimal.ZERO
totalGrossAmount = BigDecimal.ZERO
totalPensionAmount = BigDecimal.ZERO
totalNSSFAmount = BigDecimal.ZERO
totalTaxablePayAmount = BigDecimal.ZERO
totalMPRAmount = BigDecimal.ZERO
totalInsuranceReliefAmount = BigDecimal.ZERO
totalPAYEeAmount = BigDecimal.ZERO

totalE1Amount  = BigDecimal.ZERO
totalE2Amount  = BigDecimal.ZERO
totalRetConOwnAmount  = BigDecimal.ZERO
totalTaxChargedAmount = BigDecimal.ZERO


employeeSet = new HashSet();
employeePeriodList = delegator.findByAnd("P9PayrollGuys",  [payrollYearId : payrollYearId], null, false);

//Add Party ID to aset
employeePeriodList.each { employeePeriod ->
	employeeSet.add(employeePeriod.partyId);
}

employeeSet.each { employee ->
	println " PPPPPPPPPPPPPP ID is "+employee.partyId;
}



year = delegator.findOne("PayrollYear", [payrollYearId : payrollYearId], false);
context.year = year;

maxPension = delegator.findOne("PayrollConstants", [payrollConstantsId : "10001"], false);
context.maxPension = maxPension;

yearList = delegator.findByAnd("P9Report",  [payrollYearId : payrollYearId], ['sequence_no'], false);
context.yearList = yearList;


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

//Start

class P9Staff{
	def partyId;
	def firstName;
	def lastName;
	def employeeNumber;
	def pinNumber

	def listOfP9Items = []
}

listP9Staff = []


employeeSet.each { employee ->

	currentStaff = new P9Staff();
	currentStaff.partyId = employee.partyId;

	
	List mainAndExprs = FastList.newInstance();
	mainAndExprs.add(EntityCondition.makeCondition("payrollYearId", EntityOperator.EQUALS, payrollYearId));
	mainAndExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, currentStaff.partyId));

	componentList = delegator.findList("P9Report", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND),null, UtilMisc.toList("payrollYearId"), null, false);

	monthList=[];
	staffList=[];
	componentList.each { component ->
		def payrollElemId = component.payrollElementId
		def (value1, value2) = component.periodName.tokenize( '-' )

		System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV " + value1)
		if(value1 == 'Jan'){
			component.periodName = "JANUARY"
		}
		if(value1 == 'Feb'){
			component.periodName = "FEBRUARY"
		}
		if(value1 == 'Mar'){
			component.periodName = "MARCH"
		}
		if(value1 == 'Apr'){
			component.periodName = "APRIL"
		}
		if(value1 == 'May'){
			component.periodName = "MAY"
		}
		if(value1 == 'Jun'){
			component.periodName = "JUNE"
		}
		if(value1 == 'Jul'){
			component.periodName = "JULY"
		}
		if(value1 == 'Aug'){
			component.periodName = "AUGUST"
		}
		if(value1 == 'Sep'){
			component.periodName = "SEPTEMBER"
		}
		if(value1 == 'Oct'){
			component.periodName = "OCTOBER"
		}
		if(value1 == 'Nov'){
			component.periodName = "NOVEMBER"
		}
		if(value1 == 'Dec'){
			component.periodName = "DECEMBER"
		}
		
		//Get the Names
		currentStaff.firstName = component.firstName;
		currentStaff.lastName = component.lastName;
		currentStaff.employeeNumber = component.employeeNumber;
		currentStaff.pinNumber = component.pinNumber;

		if(payrollElemId == 'BASICPAY'){
			basicPayMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:component.amount,
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(basicPayMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'GROSSPAY'){
			grossMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:component.amount,
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(grossMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'PENSION'){
			pensionMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:component.amount,
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(pensionMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'NSSF'){
			nssfMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:component.amount,
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(nssfMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'TAXABLEINCOME'){
			taxablePayMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:component.amount,
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(taxablePayMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'MPR'){
			mprMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:component.amount,
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:new BigDecimal(0.0)]
			P9List.add(mprMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'INSURANCERELIEF'){
			insuranceReliefMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:component.amount,
				payeAmount:new BigDecimal(0.0)]
			P9List.add(insuranceReliefMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
		if(payrollElemId == 'PAYE'){
			payeMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, payrollPeriodId:component.payrollPeriodId,
				periodName:component.periodName, sequence_no:component.sequence_no, employeeNumber:component.employeeNumber, firstName:component.firstName,
				lastName:component.lastName, pinNumber:component.pinNumber, partyId:component.partyId, payrollElementId:component.payrollElementId,
				basicAmount:new BigDecimal(0.0),
				grossAmount:new BigDecimal(0.0),
				pensionAmount:new BigDecimal(0.0),
				nssfAmount:new BigDecimal(0.0),
				taxablePayAmount:new BigDecimal(0.0),
				mprAmount:new BigDecimal(0.0),
				insuranceReliefAmount:new BigDecimal(0.0),
				payeAmount:component.amount]
			P9List.add(payeMap);
			monthList.add([periodName:component.periodName])
			staffList.add([partyId:component.partyId])
		}
	}

	monthList=monthList.unique();
	staffList=staffList.unique();
	System.out.println("<><><>><><><><><>###################<><><><><><><><>< "+monthList)
	System.out.println("<><><>><><><><><>###################<><><><><><><><>< "+staffList)

	def yearId
	def yearName
	def payrollPeriodId
	def periodName
	def sequenceNo
	def employeeNumber
	def firstName
	def lastName
	def pinNumber
	def partyId
	def payrollElementId

	def totalE1Amount
	def totalE2Amount
	def totalRetConOwnAmount
	def totalTaxChargedAmount

	class P9Item{

		def yearId
		def yearName
		def payrollPeriodId
		def periodName
		def sequenceNo
		def employeeNumber
		def firstName
		def lastName
		def pinNumber
		def partyId
		def payrollElementId

		def totalBasicAmount;
		def totalGrossAmount;
		def totalPensionAmount;
		def totalNssfAmount;
		def totalTaxablePayAmount;
		def totalMprAmount;
		def totalInsuranceReliefAmount;
		def totalPayeAmount;
		def totalE1Amount;
		def totalE2Amount;
		def totalRetConOwnAmount;
		def totalTaxChargedAmount;
	}

	p9ItemsList = []


	totalBasicAmount  = new BigDecimal(0.0);
	totalGrossAmount  = new BigDecimal(0.0);
	totalPensionAmount  = new BigDecimal(0.0);
	totalNssfAmount  = new BigDecimal(0.0);
	totalTaxablePayAmount  = new BigDecimal(0.0);
	totalMprAmount  = new BigDecimal(0.0);
	totalInsuranceReliefAmount  = new BigDecimal(0.0);
	totalPayeAmount  = new BigDecimal(0.0);

	totalE1Amount  = new BigDecimal(0.0);
	totalE2Amount  = new BigDecimal(0.0);
	totalRetConOwnAmount  = new BigDecimal(0.0);
	totalTaxChargedAmount  = new BigDecimal(0.0);


	class StaffDetails{
		def employeeNumber;
		def pinNumber;
		def firstName;
		def lastName;
	}

	monthList.each { component ->
		//	System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii "+component.partyId)

		//

		totalBasicAmount  = new BigDecimal(0.0);
		totalGrossAmount  = new BigDecimal(0.0);
		totalPensionAmount  = new BigDecimal(0.0);
		totalNssfAmount  = new BigDecimal(0.0);
		totalTaxablePayAmount  = new BigDecimal(0.0);
		totalMprAmount  = new BigDecimal(0.0);
		totalInsuranceReliefAmount  = new BigDecimal(0.0);
		totalPayeAmount  = new BigDecimal(0.0);

		totalE1Amount  = new BigDecimal(0.0);
		totalE2Amount  = new BigDecimal(0.0);
		totalRetConOwnAmount  = new BigDecimal(0.0);
		totalTaxChargedAmount  = new BigDecimal(0.0);

		P9List.each{ p9 ->

			yearId = p9.payrollYearId;
			yearName = p9.yearName;
			payrollPeriodId = p9.payrollPeriodId;
			periodName = p9.periodName;
			sequenceNo = p9.sequence_no;
			employeeNumber = p9.employeeNumber;
			firstName = p9.firstName;
			lastName = p9.lastName;
			pinNumber = p9.pinNumber;
			partyId = p9.partyId;
			payrollElementId = p9.payrollElementId;

			if(p9.periodName==component.periodName)
			{
				/*println(" PAYEEEEEEEEEEEEEE  "+p9.payeamount);
				 println(" GROSSSS            "+p9.grossamount);*/


				totalBasicAmount  = totalBasicAmount.add(p9.basicAmount.toBigDecimal());
				totalGrossAmount  = totalGrossAmount.add(p9.grossAmount.toBigDecimal());
				totalPensionAmount  = totalPensionAmount.add(p9.pensionAmount.toBigDecimal());
				totalNssfAmount  = totalNssfAmount.add(p9.nssfAmount.toBigDecimal());
				totalTaxablePayAmount  = totalTaxablePayAmount.add(p9.taxablePayAmount.toBigDecimal());
				totalMprAmount  = totalMprAmount.add(p9.mprAmount.toBigDecimal());
				totalInsuranceReliefAmount  = totalInsuranceReliefAmount.add(p9.insuranceReliefAmount.toBigDecimal());
				totalPayeAmount  = totalPayeAmount.add(p9.payeAmount.toBigDecimal());

				totalE1Amount = totalBasicAmount.multiply(new BigDecimal(0.3));
				totalE2Amount = totalPensionAmount.add(totalNssfAmount);
				totalRetConOwnAmount = totalPensionAmount.add(totalNssfAmount);
				totalTaxChargedAmount = totalPayeAmount.add(totalMprAmount).add(totalInsuranceReliefAmount);

			}
		}


		//	println("TOTATAAAAAAAAAAAAAAAAAAAAAAAAL totalPayeAmount 2 "+totalPayeAmount)
		p9Item = new P9Item();

		p9Item.yearId = yearId;
		p9Item.yearName = yearName;
		p9Item.payrollPeriodId = payrollPeriodId;
		p9Item.periodName = component.periodName;
		p9Item.sequenceNo = sequenceNo;
		/*	p9Item.employeeNumber = person.employeeNumber;
		 p9Item.firstName = person.firstName;
		 p9Item.lastName = person.lastName;
		 p9Item.pinNumber = person.pinNumber;*/
		p9Item.partyId = partyId;
		p9Item.payrollElementId = payrollElementId;

		p9Item.totalBasicAmount = totalBasicAmount;
		p9Item.totalGrossAmount = totalGrossAmount;
		p9Item.totalPensionAmount = totalPensionAmount;
		p9Item.totalNssfAmount = totalNssfAmount;
		p9Item.totalTaxablePayAmount = totalTaxablePayAmount;
		p9Item.totalMprAmount = totalMprAmount;
		p9Item.totalInsuranceReliefAmount = totalInsuranceReliefAmount;
		p9Item.totalPayeAmount = totalPayeAmount;

		p9Item.totalE1Amount = totalE1Amount;
		p9Item.totalE2Amount = totalE2Amount;
		p9Item.totalRetConOwnAmount = totalRetConOwnAmount;
		p9Item.totalTaxChargedAmount = totalTaxChargedAmount;

		p9ItemsList << p9Item
	}

	println("________________All ITEMS")
	p9ItemsList.each{ item ->

		println(" totalBasicAmount IS "+item.totalBasicAmount)
		println(" totalGrossAmount IS "+item.totalGrossAmount)
		println(" totalPensionAmount IS "+item.totalPensionAmount)
		println(" totalNssfAmount IS "+item.totalNssfAmount)
		println(" totalTaxablePayAmount IS "+item.totalTaxablePayAmount)
		println(" totalMprAmount IS "+item.totalMprAmount)
		println(" totalInsuranceReliefAmount IS "+item.totalInsuranceReliefAmount)
		println(" totalPayeAmount IS "+item.totalPayeAmount)

		println(" totalE1Amount IS "+item.totalE1Amount)
		println(" totalE2Amount IS "+item.totalE2Amount)
		println(" totalRetConOwnAmount IS "+item.totalRetConOwnAmount)
		println(" totalTaxChargedAmount IS "+item.totalTaxChargedAmount)
	}

	p9ItemsList.sort {it.sequenceNo}
	
	

	currentStaff.listOfP9Items = p9ItemsList;
	listP9Staff << currentStaff;

}
//context.p9ItemsList = p9ItemsList

context.listP9Staff = listP9Staff

//context.grossMap = grossMap;





//Employer Details


/*maxPension = delegator.findByAnd("PayrollConstants", null, null, false);
 class PayrollConts{
 def pension_maxContibution;
 }
 def payrollConstsStatic = new PayrollConts();
 maxPension.eachWithIndex { pcItem, index ->
 payrollConstsStatic.pension_maxContibution = pcItem.pension_maxContibution
 }
 context.payrollConstsStatic = payrollConstsStatic;*/

