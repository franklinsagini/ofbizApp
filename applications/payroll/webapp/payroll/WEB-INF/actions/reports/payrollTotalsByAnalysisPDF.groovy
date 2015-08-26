import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;



payrollPeriodId = parameters.payrollPeriodId
println " PPPPPPPPPP Payroll Period "+payrollPeriodId

TotalAmount = BigDecimal.ZERO


if (payrollPeriodId) {
	earningsTotalsReport = delegator.findByAnd("EarningsTotalsReport", [payrollPeriodId : payrollPeriodId],
		null, false);

   //earningsTotalsReport = delegator.findOne("EarningsTotalsReport", [payrollPeriodId : payrollPeriodId], false);
   context.payrollPeriodId = payrollPeriodId;
   context.earningsTotalsReport = earningsTotalsReport;
   
   period = delegator.findOne("PayrollPeriod", [payrollPeriodId : payrollPeriodId], false);
   context.period = period
  
}

if (payrollPeriodId) {
	deductionsTotalsReport = delegator.findByAnd("DeductionsTotalsReport", [payrollPeriodId : payrollPeriodId],
		null, false);


	context.deductionsTotalsReport = deductionsTotalsReport;
   
 }

if (payrollPeriodId) {
	
	//payeTotalsReport = delegator.findOne("PayeTotalsReport", [payrollPeriodId : payrollPeriodId], false);
	payeTotalsReport = delegator.findByAnd("PayeTotalsReport", [payrollPeriodId : payrollPeriodId],
		null, false);

	context.payeTotalsReport = payeTotalsReport;
   
 }

//Gross Total
grossTotal = BigDecimal.ZERO;
if (payrollPeriodId) {
	
	//payeTotalsReport = delegator.findOne("PayeTotalsReport", [payrollPeriodId : payrollPeriodId], false);
	grossTotalsReport = delegator.findByAnd("GrossTotalsReport", [payrollPeriodId : payrollPeriodId],
		null, false);
	
	grossTotalsReport.each{ gross ->
		
		
				grossTotal = grossTotal.add(gross.amount);
		
			}

	context.grossTotal = grossTotal;
   
 }
//Paye only
payeTotal = BigDecimal.ZERO;
if (payrollPeriodId) {
	
	//payeTotalsReport = delegator.findOne("PayeTotalsReport", [payrollPeriodId : payrollPeriodId], false);
	payeOnlyTotalsReport = delegator.findByAnd("PayeOnlyTotalsReport", [payrollPeriodId : payrollPeriodId],
		null, false);
	
	payeOnlyTotalsReport.each{ paye ->
		
		
				payeTotal = payeTotal.add(paye.amount);
		
			}

	context.payeTotal = payeTotal;

 }

