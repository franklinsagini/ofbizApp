import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;



payrollPeriodId = parameters.payrollPeriodId

lineTotalAmount = BigDecimal.ZERO
TotalStatutoryAmount = BigDecimal.ZERO
TotalEmployerAmount = BigDecimal.ZERO
TotalLineAmount = BigDecimal.ZERO

nhifList = [];
totalsList = [];
dataMap=[:];

class TOTDet{
	def TotalStatutoryAmount;
	def TotalEmployerAmount;
	def TotalLineAmount;
}

if (payrollPeriodId) {
   period = delegator.findOne("PayrollPeriod", [payrollPeriodId : payrollPeriodId], false);
   context.period = period;

	nhif = delegator.findByAnd("NSSF_NHIFReport", [payrollPeriodId : parameters.payrollPeriodId, payrollElementId: "NHIF"], 
		null, false);
	
	
	nhif.each{ nhif ->
		lineTotalAmount = nhif.statutoryAmount.add(nhif.statutoryAmount);
		TotalStatutoryAmount = TotalStatutoryAmount.add(nhif.statutoryAmount);
		TotalEmployerAmount = TotalEmployerAmount.add(nhif.statutoryAmount)
		TotalLineAmount = TotalLineAmount.add(lineTotalAmount)
		
		dataMap=[employeeNumber:nhif.employeeNumber, firstName:nhif.firstName, lastName:nhif.lastName, 
			 nationalIDNumber:nhif.nationalIDNumber, nhifNumber:nhif.nhifNumber,
			  statutoryAmount:nhif.statutoryAmount, lineTotalAmount:lineTotalAmount]
		
		
		nhifList.add(dataMap)
		
		
		System.out.println("##################" +TotalLineAmount);
		
	}
	context.nhifList = nhifList;
	
	
	totalsMap=[TotalStatutoryAmount:TotalStatutoryAmount, TotalEmployerAmount:TotalEmployerAmount, TotalLineAmount:TotalLineAmount]
	totalsList.add(totalsMap)
	

/*	def totDet = new TOTDet();
	
	totalsList.eachWithIndex { totDetItem, index ->
		totDet.TotalStatutoryAmount = totDetItem.TotalStatutoryAmount
		totDet.TotalVoluntaryAmount = totDetItem.TotalVoluntaryAmount
		totDet.TotalEmployerAmount = totDetItem.TotalEmployerAmount
		totDet.TotalLineAmount = totDetItem.TotalLineAmount
	}	
	context.totDet = totDet;
	System.out.println("##########<><><><>########" +totDet.TotalStatutoryAmount);*/
	
	context.totalsList=totalsList

	
	


}

