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
libMap = [:];
p10List = [];
year = delegator.findOne("PayrollYear", [payrollYearId : payrollYearId], false);
context.year = year;
totalPayeAmount = BigDecimal.ZERO
totalFringeAmount = BigDecimal.ZERO

yearList = delegator.findByAnd("P10Report",  [payrollYearId : payrollYearId], ['sequence_no'], false);
context.yearList = yearList;
	
List mainAndExprs = FastList.newInstance();
mainAndExprs.add(EntityCondition.makeCondition("payrollYearId", EntityOperator.EQUALS, payrollYearId));
componentList = delegator.findList("P10Report", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND),
	 UtilMisc.toSet("payrollYearId", "yearName", "periodName", "sequence_no", "payrollElementId", "amount"), UtilMisc.toList("payrollYearId"), null, false);

 System.out.println("##################YYYYYYYYYYYYYYYYYYY" +yearList);
 System.out.println("##################" +componentList);
 System.out.println("##################" +payrollYearId);
 
 

 
 
 monthList = ["JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"]
 
 
	
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
	
	     
	 
	 if(payrollElemId == 'PAYE'){
		 payeMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, periodName:component.periodName, 
			 sequence_no:component.sequence_no, payrollElementId:component.payrollElementId,
			  payeamount:component.amount, fringeamount:new BigDecimal(0.0)]
		 p10List.add(payeMap);
		
	 }
	 
	 if(payrollElemId == 'LOWINTERESTBENEFIT'){
		 libMap = [payrollYearId:component.payrollYearId, yearName:component.yearName, periodName:component.periodName, 
			 sequence_no:component.sequence_no, payrollElementId:component.payrollElementId,
			  payeamount:new BigDecimal(0.0), fringeamount:component.amount]
		 p10List.add(libMap);
	 }
	 
	 

   }
 
 p10_Last = []
 p10ListFinal = []
 
 def yearId
 def  yearName
 def periodName
 def sequenceNo
 def payrollElementId
 
 class P10Item{
	 
	 def yearId;
	 def yearName;
	 def periodName;
	 def sequenceNo;
	 def payrollElementId;
	 def totalPayeAmount;
	 def totalFringeAmount;
}
 
 p10ItemsList = []
 
 totalPayeAmount  = new BigDecimal(0.0);
 totalFringeAmount  = new BigDecimal(0.0);

 monthList.each { component ->
	 System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii "+component)
	 periodName = component;
	 
	 totalPayeAmount = new BigDecimal(0.0);
	 totalFringeAmount = new BigDecimal(0.0);
	 
	 p10List.each{ p10 ->
		 
		 yearId = p10.payrollYearId;
		 yearName = p10.yearName;
		 
		 
		 
		 println(" HHHHHHHHHHHHHHHHHHHHHHHHHHHHHH "+periodName);
		 
		 sequenceNo = p10.sequence_no;
		 payrollElementId = p10.payrollElementId;
		 
		 
		 if(p10.periodName==component){
			 
			 println(" PAYEEEEEEEEEEEEEE  "+p10.payeamount);
			 println(" FRIIIIIIIIIIIIIII "+p10.fringeamount);
			 
			 //if (p10.payeamount != 0){
				 totalPayeAmount = totalPayeAmount.add(p10.payeamount.toBigDecimal());
			 //}
			 
			 //if ( p10.fringeamount != 0){
			totalFringeAmount = totalFringeAmount.add(p10.fringeamount);
			 //}
			

		
		 }
		 
		 println(" OCCCCCCCCCCCCC  payamount "+p10.payeamount);
		 println(" OCCCCCCCCCCCCCCCC fringeamount "+p10.fringeamount)
		 
		
		 
		 println("TOTATAAAAAAAAAAAAAAAAAAAAAAAAL totalPayeAmount 1 "+totalPayeAmount)
	
 	}
	 println("TOTATAAAAAAAAAAAAAAAAAAAAAAAAL totalPayeAmount 2 "+totalPayeAmount)
	 p10Item = new P10Item();
	 
	 
	p10Item.yearId = yearId;
	 p10Item.yearName = yearName;
	 p10Item.periodName = periodName;
	 p10Item.sequenceNo = sequenceNo;
	 p10Item.payrollElementId = payrollElementId;
	 p10Item.totalPayeAmount = totalPayeAmount;
	 p10Item.totalFringeAmount = totalFringeAmount;

	 p10ItemsList << p10Item
//	 p10ListFinal=[payrollYearId:yearId, yearName:yearName, periodName:periodName,
//		 sequence_no:sequenceNo, payrollElementId:payrollElementId,
//		  payeamount:theTotalPayeAmount as BigDecimal, fringeamount:totalFringeAmount]
//	 p10_Last.add(p10ListFinal);
	 
//	 p10ListFinal=[payrollYearId:yearId, yearName:yearName, periodName:periodName,
//		 sequence_no:sequenceNo, payrollElementId:payrollElementId,
//		  payeamount:totalPayeAmount, fringeamount:totalFringeAmount]
//	 p10_Last.add(p10ListFinal);
	 
  }
 
 println("________________All ITEMS")
 p10ItemsList.each{ item ->
	 
	 println(" PAYEAMOUNT IS "+item.totalPayeAmount)
 }
 
 
 System.out.println("##################P10 List" +p10List);
 System.out.println("##################PPPPPPPPPPPPPPPPPPPPPPPPPPPP" +payeMap);
 p10List.sort {it.sequence_no}
 context.p10ItemsList = p10ItemsList
 //p10_Last;
 context.libMap = libMap;
 
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

