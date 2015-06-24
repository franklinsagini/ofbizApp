import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilMisc;
import javolution.util.FastList;

exprBldr =  new EntityConditionBuilder();
total = BigDecimal.ZERO;
count = 0
settlementAcc = "943006"
ownedBranchId = parameters.get("partyId");
owingBranchId = parameters.get("organizationPartyId");

context.ownedBranchId = ownedBranchId
context.owingBranchId = owingBranchId

 expr = exprBldr.AND() {
        EQUALS(organizationPartyId: ownedBranchId)
        NOT_EQUAL(owedByPartyId: owingBranchId)
      }
trans = delegator.findList("AcctgTransEntry", expr, null, ["createdTxStamp DESC"], null, false);

trans.each { tran ->
  total = total + tran.amount
  count = count + 1
}
context.total = total
context.count = count
context.transactions = trans

