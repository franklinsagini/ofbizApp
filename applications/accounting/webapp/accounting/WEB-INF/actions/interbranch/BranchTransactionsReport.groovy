import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilMisc;
import javolution.util.FastList;

exprBldr =  new EntityConditionBuilder();
total = BigDecimal.ZERO;
totalCredits = BigDecimal.ZERO;
totalDebits = BigDecimal.ZERO;
count = 0
settlementAcc = "943006"
context.settlementAcc = settlementAcc
ownedBranchId = parameters.get("partyId");
owingBranchId = parameters.get("organizationPartyId");

context.ownedBranchId = ownedBranchId
context.owingBranchId = owingBranchId

 expr = exprBldr.AND() {
        EQUALS(organizationPartyId: owingBranchId)
        EQUALS(glAccountId: settlementAcc)
        //EQUALS(owesPartyId: ownedBranchId)
      }
trans = delegator.findList("AcctgTransEntry", expr, null, ["createdTxStamp DESC"], null, false);

transFinal = []
trans.each { tran ->
  if (tran.owesPartyId == ownedBranchId) {
  transLocal = [:]
  if(tran.debitCreditFlag == "C") {
    localTotal = total + tran.amount
    total = total + tran.amount
    totalCredits = total
  } else {
    localTotal = total - tran.amount
    total = total - tran.amount
    totalDebits = total + tran.amount
  }

  count = count + 1
  transLocal.acctgTransId = tran.acctgTransId
  transLocal.createdTxStamp = tran.createdTxStamp
  transLocal.debitCreditFlag = tran.debitCreditFlag
  transLocal.amount = tran.amount
  transLocal.localTotal = localTotal
  transLocal.totalCredits = totalCredits
  transLocal.totalDebits = totalDebits
  transFinal.add(transLocal)
  }
  if(tran.glAccountTypeId=="COMMISSIONS_PAYABLE" && tran.partyId == ownedBranchId){
  transLocal = [:]
  if(tran.debitCreditFlag == "C") {
    localTotal = total + tran.amount
    total = total + tran.amount
    totalCredits = total
  } else {
    localTotal = total - tran.amount
    total = total - tran.amount
    totalDebits = total + tran.amount
  }

  count = count + 1
  transLocal.acctgTransId = tran.acctgTransId
  transLocal.createdTxStamp = tran.createdTxStamp
  transLocal.debitCreditFlag = tran.debitCreditFlag
  transLocal.amount = tran.amount
  transLocal.localTotal = localTotal
  transLocal.totalCredits = totalCredits
  transLocal.totalDebits = totalDebits
  transFinal.add(transLocal)
  }
}
context.total = total
context.totalCredits = totalCredits
context.totalDebits = totalDebits
context.grandTotal = totalCredits - totalDebits
context.count = count
context.transactions = transFinal

