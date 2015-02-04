import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;


elementType = parameters.elementType
if (elementType) {
   code  = delegator.findByAnd("PayrollElement", [elementType : parameters.elementType], ['elementCode'], false);
	context.code = code;
}


