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
  transLocal = [:]
  //System.out.println("########MAIN THE acctgTransId: " + tran.acctgTransId)
  if(tran.debitCreditFlag == "C") {
    localTotal = total + tran.amount
    total = total + tran.amount
    totalCredits = total
 //System.out.println("######## ALL CREDITS " + tran.acctgTransId)
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
  if(tran.debitCreditFlag == "D") {
    System.out.println("######## ALL DEBITS " + tran.acctgTransId)
    List mainAndExprs = FastList.newInstance();
    mainAndExprs.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, tran.acctgTransId));
    mainAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, owingBranchId));
    //mainAndExprs.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, settlementAcc));
    //mainAndExprs.add(EntityCondition.makeCondition("glAccountTypeId", EntityOperator.EQUALS, "COMMISSIONS_PAYABLE"));
    debitTrans = delegator.findList("AcctgTransEntry", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND) , null, ["createdTxStamp DESC"], null, false);
    debitTrans.each { obj ->
      if (obj.partyId && obj.partyId==ownedBranchId) {
        System.out.println("########WE ARE STILL ALIVE LETS CONTINUE CODING PARTYID: " + obj.partyId + " AND THE acctgTransId: " + tran.acctgTransId)
        localTotal = total - tran.amount
        total = total - tran.amount
        totalDebits = total + tran.amount

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

  }

}
context.total = total
context.totalCredits = totalCredits
context.totalDebits = totalDebits
context.grandTotal = totalCredits - totalDebits
context.count = count
context.transactions = transFinal

