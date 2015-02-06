import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

payrollcodes = delegator.findList("PayrollElement", null, null, ['elementCode'], null, false);
context.payrollcodes = payrollcodes;


println "############### "+payrollcodes


