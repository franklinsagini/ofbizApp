import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;

if (!glAccountId) {
  return;
}

//thruDate =  parameters.thruDate
if (!thruDate) {
  thruDate = UtilDateTime.nowTimestamp();
}


if (!fromDate) {
  transFromDate = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId", "1"), false)
  fromDate = transFromDate.transactionDate
}


println "############################################## thruDate: "+thruDate
println "############################################## fromDate: "+fromDate
println "############################################## glAccountId: "+glAccountId
println "############################################## organizationPartyId: "+organizationPartyId
finalTransList = []
finalTransListBuilder = []
runningBalance = 0

summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
summaryCondition.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));

acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, ["createdTxStamp"], null, false)

GenericValue account = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", glAccountId), true);
isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(account);


acctgTransEntry.each { entry ->

    if (isDebit) {
    System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")

    if(entry.debitCreditFlag == "D") {
      if (entry.amount) {
      runningBalance = runningBalance + entry.amount
      }
    } else {
      if (entry.amount) {
        runningBalance = runningBalance - entry.amount
      }
    }
  }else{
    System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")

    if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
      runningBalance = runningBalance + entry.amount
      }
    } else {
      if (entry.amount) {
        runningBalance = runningBalance - entry.amount
      }
    }
  }

  finalTransListBuilder = [
    createdTxStamp:entry.createdTxStamp,
    acctgTransId:entry.acctgTransId,
    debitCreditFlag:entry.debitCreditFlag,
    glAccountTypeId:entry.glAccountTypeId,
    amount:entry.amount,
    runningBalance:runningBalance
  ]
  finalTransList.add(finalTransListBuilder)
}



context.transactionsList = finalTransList
