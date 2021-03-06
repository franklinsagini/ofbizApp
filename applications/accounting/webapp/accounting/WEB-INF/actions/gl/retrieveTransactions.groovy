import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.accounting.ledger.GeneralLedgerServices;

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

description = null;
println "############################################## thruDate: "+thruDate
println "############################################## fromDate: "+fromDate
println "############################################## glAccountId: "+glAccountId
println "############################################## organizationPartyId: "+organizationPartyId
println "############################################## DESCRIPTION: "+description
finalTransList = []
finalTransListBuilder = []
runningBalance = 0
openingBalance = 0
count = 0

summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
summaryCondition.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));

openinBalanceCond = [];
openinBalanceCond.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN, fromDate));
openinBalanceCond.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
openinBalanceCond.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId));

openingBalacctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(openinBalanceCond, EntityOperator.AND), null, null, null, false)
acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, ["createdTxStamp"], null, false)

GenericValue account = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", glAccountId), true);
isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(account);

openingBalacctgTransEntry.each { entry ->
  if (isDebit) {
    if(entry.debitCreditFlag == "D") {
      if (entry.amount) {
      openingBalance = openingBalance + entry.amount
      }
    } else {
      if (entry.amount) {
        openingBalance = openingBalance - entry.amount
      }
    }
  }else{
    if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
      openingBalance = openingBalance + entry.amount
      }
    } else {
      if (entry.amount) {
        openingBalance = openingBalance - entry.amount
      }
    }
  }
}

acctgTransEntry.each { entry ->
org.ofbiz.entity.GenericValue entryGV = entry
if (entryGV != null) {
  description = GeneralLedgerServices.getGlNarration(delegator, entryGV);
}else{
  description = "TRANSACTION NOT IN AcctgTransEntry"
}

creditAmount = null
debitAmount = null
  if(count<1) {
  finalTransListBuilder = [
    description:"BALANCE BROUGHT FORWARD",
    runningBalance:openingBalance
  ]
  finalTransList.add(finalTransListBuilder)
  }
  if (isDebit) {
    if(entry.debitCreditFlag == "D") {
      if (entry.amount) {
        if(count<1){
          runningBalance = entry.amount + openingBalance
        }else{
          runningBalance = runningBalance + entry.amount
        }
      }
       debitAmount = entry.amount
    } else {
      if (entry.amount) {
        if(count<1){
          runningBalance = openingBalance - entry.amount
        }else{
           runningBalance = runningBalance - entry.amount
        }
        creditAmount = entry.amount
      }
    }
  }else{

    if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
        if(count<1){
          runningBalance = openingBalance + entry.amount
        }else{
          runningBalance = runningBalance + entry.amount
        }
      }
      creditAmount = entry.amount
    } else {
      if (entry.amount) {
      if(count<1){
          runningBalance = openingBalance - entry.amount
        }else{
          runningBalance = runningBalance - entry.amount
        }
        debitAmount = entry.amount
      }
    }
  }

  finalTransListBuilder = [
    createdTxStamp:entry.createdTxStamp,
    acctgTransId:entry.acctgTransId,
    debitCreditFlag:entry.debitCreditFlag,
    description:description,
    creditAmount:creditAmount,
    debitAmount:debitAmount,
    runningBalance:runningBalance
  ]
  finalTransList.add(finalTransListBuilder)


  count = count + 1
}



context.transactionsList = finalTransList
