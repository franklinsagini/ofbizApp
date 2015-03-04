import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;

import javolution.util.FastList;
glAccountId = parameters.glAccountId

//balanceTotal = 0
entriesList = [];
List mainAndExprs = FastList.newInstance();
if (parameters.glAccountId) {
  mainAndExprs.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, parameters.glAccountId));
}
//mainAndExprs.add(EntityCondition.makeCondition("isCapitalComponent", EntityOperator.EQUALS, "Y"));
entriesList = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("transactionDate", "acctgTransId", "accountName","acctgTransTypeId", "debitCreditFlag", "amount"), UtilMisc.toList("acctgTransEntrySeqId"), null, false);
runningBalance = BigDecimal.ZERO
totalDebits = BigDecimal.ZERO
totalCredits = BigDecimal.ZERO
finalentriesList = [];
count = 0;
balanceType = "";
entriesList.each { entry ->
  if (count == 0) {
    balanceType = entry.debitCreditFlag;
  }


  if (balanceType == "D") {
        if(entry.debitCreditFlag == "D") {
      if (entry.amount) {
      runningBalance = runningBalance + entry.amount
      totalDebits = totalDebits + entry.amount
      }
    } else {
      if (entry.amount) {
        runningBalance = runningBalance - entry.amount
       totalCredits = totalCredits + entry.amount
      }
    }
  }else{
        if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
      runningBalance = runningBalance + entry.amount
      totalDebits = totalDebits + entry.amount
      }
    } else {
      if (entry.amount) {
        runningBalance = runningBalance - entry.amount
       totalCredits = totalCredits + entry.amount
      }
    }
  }




    finalentriesList.add("transactionDate" : entry.transactionDate, "acctgTransId" : entry.acctgTransId, "accountName" : entry.accountName,"acctgTransTypeId" : entry.acctgTransTypeId, "debitCreditFlag" : entry.debitCreditFlag, "amount" : entry.amount, "runningBalance" : runningBalance)
    count = count + 1;
}




System.out.println("########################################################################################################GL Account: " )
System.out.println("GL Account: " + entriesList)


context.entriesList = finalentriesList
context.totalCredits = totalCredits
context.totalDebits = totalDebits
