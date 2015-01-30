import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;

balanceTotal = 0
componentList = [];
componentList = delegator.findList("CapitalAdequacyReport", null, UtilMisc.toSet("code", "name", "amount"), UtilMisc.toList("code"), null, false);

if (componentList) {
    subtotal = BigDecimal.ZERO
    componentMap = [];
    componentList.each { component ->

      if (component.amount) {
        subtotal = subtotal.add(component.amount)
      }

      def codeString = component.code
      def orderString = codeString.replaceAll('.', '')
      //component.order =  orderString

    componentMap = [order:"118", code:"1.1.8", name:"Sub-Total(1.1.1 to 1.1.7)", amount:subtotal]
    componentList.add(componentMap);
//    componentList.sort {
//      it.order
//    }
}

context.capitalAdequacyList = componentList;




