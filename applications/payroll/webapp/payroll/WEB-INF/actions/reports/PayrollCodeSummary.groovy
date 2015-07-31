import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;



payrollPeriodId = parameters.payrollPeriodId
payrollElementId = parameters.payrollElementId


TotalAmount = BigDecimal.ZERO


if (payrollPeriodId) {
   period = delegator.findOne("PayrollPeriod", [payrollPeriodId : payrollPeriodId], false);
   context.period = period;

   element = delegator.findOne("PayrollElement", [payrollElementId : payrollElementId], false);
   context.element = element;

	codes = delegator.findByAnd("PayrollCodeSummaryReport", [payrollPeriodId : parameters.payrollPeriodId, payrollElementId: payrollElementId],
		null, false);


	codes.each{ codes ->


		TotalAmount = TotalAmount.add(codes.amount);

	}


	context.codes = codes;
	context.TotalAmount = TotalAmount;


}


