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
    System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")

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
    System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")

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

  if(count<1) {
  finalTransListBuilder = [
    glAccountTypeId:"BALANCE BROUGHT FORWARD",
    runningBalance:openingBalance
  ]
  finalTransList.add(finalTransListBuilder)
  }
  if (isDebit) {
    System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")
    if(entry.debitCreditFlag == "D") {
      if (entry.amount) {
        if(count<1){
          runningBalance = entry.amount + openingBalance
        }else{
          runningBalance = runningBalance + entry.amount
        }
      }
    } else {
      if (entry.amount) {
        if(count<1){
          runningBalance = openingBalance - entry.amount
        }else{
           runningBalance = runningBalance - entry.amount
        }

      }
    }
  }else{
    System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")

    if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
        if(count<1){
          runningBalance = openingBalance + entry.amount
        }else{
          runningBalance = runningBalance + entry.amount
        }
      }
    } else {
      if (entry.amount) {
      if(count<1){
          runningBalance = openingBalance - entry.amount
        }else{
          runningBalance = runningBalance - entry.amount
        }
      }
    }
  }

  finalTransListBuilder = [
    createdTxStamp:entry.createdTxStamp,
    acctgTransId:entry.acctgTransId,
    debitCreditFlag:entry.debitCreditFlag,
    glAccountTypeId:entry.glAccountTypeId,
    if(entry.debitCreditFlag == "C"){
        creditAmount:amount:entry.amount,
      }else{
        debitAmount:amount:entry.amount,
      }

    runningBalance:runningBalance
  ]
  finalTransList.add(finalTransListBuilder)


  count = count + 1
}



context.transactionsList = finalTransList
