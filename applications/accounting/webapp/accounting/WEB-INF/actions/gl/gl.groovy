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
entriesList.each { entry ->
    if(entry.debitCreditFlag == "D") {
      runningBalance = runningBalance + entry.amount
      totalDebits = totalDebits + entry.amount
    } else {
       runningBalance = runningBalance - entry.amount
       totalCredits = totalCredits + entry.amount
    }
    finalentriesList.add("transactionDate" : entry.transactionDate, "acctgTransId" : entry.acctgTransId, "accountName" : entry.accountName,"acctgTransTypeId" : entry.acctgTransTypeId, "debitCreditFlag" : entry.debitCreditFlag, "amount" : entry.amount, "runningBalance" : runningBalance)
}




System.out.println("########################################################################################################GL Account: " )
System.out.println("GL Account: " + entriesList)


context.entriesList = finalentriesList
context.totalCredits = totalCredits
context.totalDebits = totalDebits
