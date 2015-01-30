import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;

balanceTotal = BigDecimal.ZERO;
accountBalanceList = [];
accountBalanceList = delegator.findList("CapitalAdequacyReport", null, UtilMisc.toSet("code", "name", "amount"), UtilMisc.toList("code"), null, false);
//accountBalanceList.each { component ->
//    balanceTotal = balanceTotal + component.amount;
//}

//context.accountBalanceList.add(UtilMisc.toMap("code", "1.1.8", "name", "Sub Total", "amount", balanceTotal));
context.capitalAdequacyList = accountBalanceList;

