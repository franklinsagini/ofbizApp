import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;



payrollPeriodId = parameters.payrollPeriodId

lineTotalAmount = BigDecimal.ZERO
TotalStatutoryAmount = BigDecimal.ZERO
TotalVoluntaryAmount = BigDecimal.ZERO
TotalEmployerAmount = BigDecimal.ZERO
TotalLineAmount = BigDecimal.ZERO

nssfList = [];
totalsList = [];
dataMap=[:];

class TOTDet{
	def TotalStatutoryAmount;
	def TotalVoluntaryAmount;
	def TotalEmployerAmount;
	def TotalLineAmount;
}

if (payrollPeriodId) {
   period = delegator.findOne("PayrollPeriod", [payrollPeriodId : parameters.payrollPeriodId], false);
   context.period = period;

	nssf = delegator.findByAnd("NSSF_NHIFReport", [payrollPeriodId : parameters.payrollPeriodId, payrollElementId: "NSSF"], 
		null, false);
	
	
	nssf.each{ nssf ->
		lineTotalAmount = nssf.statutoryAmount.add(nssf.statutoryAmount).add(nssf.nssfVolAmount);
		TotalStatutoryAmount = TotalStatutoryAmount.add(nssf.statutoryAmount);
		TotalVoluntaryAmount = TotalVoluntaryAmount.add(nssf.nssfVolAmount)
		TotalEmployerAmount = TotalEmployerAmount.add(nssf.statutoryAmount)
		TotalLineAmount = TotalLineAmount.add(lineTotalAmount)
		
		dataMap=[employeeNumber:nssf.employeeNumber, firstName:nssf.firstName, lastName:nssf.lastName, 
			 nationalIDNumber:nssf.nationalIDNumber, socialSecurityNumber:nssf.socialSecurityNumber,
			  nssfVolAmount:nssf.nssfVolAmount, statutoryAmount:nssf.statutoryAmount, lineTotalAmount:lineTotalAmount]
		
		
		nssfList.add(dataMap)
		
		
		System.out.println("##################" +TotalLineAmount);
		
	}
	context.nssfList = nssfList;
	
	
	totalsMap=[TotalStatutoryAmount:TotalStatutoryAmount, TotalVoluntaryAmount:TotalVoluntaryAmount, 
			  TotalEmployerAmount:TotalEmployerAmount, TotalLineAmount:TotalLineAmount]
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

