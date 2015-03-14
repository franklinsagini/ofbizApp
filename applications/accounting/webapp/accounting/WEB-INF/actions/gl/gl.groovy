import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.accounting.util.UtilAccounting

import javolution.util.FastList;
glAccountId = parameters.glAccountId
fromDate = thruDate - 366
//balanceTotal = 0
entriesList = [];
List mainAndExprs = FastList.newInstance();
finalentriesList = [];
if (parameters.glAccountId) {
  mainAndExprs.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, parameters.glAccountId));
  accountBalance = dispatcher.runSync('computeGlAccountBalanceForTrialBalance', [organizationPartyId: "Company", thruDate : thruDate, fromDate: fromDate,  glAccountId: parameters.glAccountId, userLogin: userLogin]);
System.out.println("ACCOUNT BALANCE MAPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP " + accountBalance)
entriesList = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("transactionDate", "acctgTransId", "accountName","acctgTransTypeId", "debitCreditFlag", "amount"), UtilMisc.toList("acctgTransEntrySeqId"), null, false);
entriesList.sort {it.transactionDate}
runningBalance = accountBalance. openingBalance
totalDebits = BigDecimal.ZERO
totalCredits = BigDecimal.ZERO
openingBalance = BigDecimal.ZERO

count = 0;
balanceType = "";
GenericValue account = delegator.findOne("GlAccount", UtilMisc.toMap("glAccountId", glAccountId), true);
isDebit = org.ofbiz.accounting.util.UtilAccounting.isDebitAccount(account);
entriesList.each { entry ->
  if (count==0) {
    openingBalance = entry.amount
  }
  if (isDebit) {
    System.out.println("THIS IS A DEBIT BALANCE ACCOUNT")

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

  }

  if (!isDebit) {
    System.out.println("THIS IS A CREDIT BALANCE ACCOUNT")

    if(entry.debitCreditFlag == "C") {
      if (entry.amount) {
      runningBalance = runningBalance + entry.amount
       totalCredits = totalCredits + entry.amount
      }
    } else {
      if (entry.amount) {
        runningBalance = runningBalance - entry.amount
         totalDebits = totalDebits + entry.amount
      }
    }
  }



    finalentriesList.add("transactionDate" : entry.transactionDate, "acctgTransId" : entry.acctgTransId, "accountName" : entry.accountName,"acctgTransTypeId" : entry.acctgTransTypeId, "debitCreditFlag" : entry.debitCreditFlag, "amount" : entry.amount, "runningBalance" : runningBalance)
    count = count + 1;
}
System.out.println("####################################################################### Account Balance " + accountBalance)
context.entriesList = finalentriesList
context.openingBalance = openingBalance
context.totalCredits = totalCredits
context.totalDebits = totalDebits
context.runningBalance = runningBalance
}else{
entriesList = delegator.findList("AcctgTransAndEntries", null, UtilMisc.toSet("transactionDate", "acctgTransId", "accountName","acctgTransTypeId", "debitCreditFlag", "amount"), UtilMisc.toList("acctgTransEntrySeqId"), null, false);
context.entriesList = entriesList
}
//mainAndExprs.add(EntityCondition.makeCondition("isCapitalComponent", EntityOperator.EQUALS, "Y"));



