import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator

finAccountId = parameters.finAccountId
glReconciliationId = parameters.glReconciliationId
if (finAccountId) {
  bankAccount = delegator.findOne("FinAccount", [finAccountId : finAccountId], false)
}
if (glReconciliationId) {
   reconciliation = delegator.findOne("GlReconciliation", [glReconciliationId : glReconciliationId], false)
}
context.bankAccount = bankAccount
context.reconciliation = reconciliation

summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("glReconciliationId", EntityOperator.EQUALS, glReconciliationId));
List summaryList = delegator.findList('BankReconSummary', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, null, null, false)
count = 1
cashBookBalance = BigDecimal.ZERO;
unreceiptedBankings = BigDecimal.ZERO;
unpresentedCheques = BigDecimal.ZERO;
uncreditedBankings = BigDecimal.ZERO;
unidentifiedDebits = BigDecimal.ZERO;
bankBalance = BigDecimal.ZERO;
summaryList.each { summary ->
  cashBookBalance = summary.cashBookBalance
  unreceiptedBankings = summary.unreceiptedBankings
  unpresentedCheques = summary.unpresentedCheques
  uncreditedBankings = summary.uncreditedBankings
  unidentifiedDebits = summary.unidentifiedDebits
  bankBalance = summary.bankBalance
  System.out.println("COUNTTTTTTTTTTTTTTTTTT " + count)
  count++
}

context.cashBook = cashBookBalance
context.unreceiptedDirectDeposits = unreceiptedBankings
context.unpresentedCheques = unpresentedCheques
context.uncreditedCheques = uncreditedBankings
context.withdrawalNotInCashBook = unidentifiedDebits
context.bankStatement = bankBalance







//UB CONDITION
ubCondition = [];
ubCondition.add(EntityCondition.makeCondition("glReconciliationId", EntityOperator.EQUALS, glReconciliationId));
ubCondition.add(EntityCondition.makeCondition("finAccountTransTypeId", EntityOperator.EQUALS, 'UB'));
List ubTransList = delegator.findList('FinAccountTrans', EntityCondition.makeCondition(ubCondition, EntityOperator.AND), null, ['finAccountTransId'], null, false)

    ubTotal = 0
    ubTransList.each { ubTransaction ->
      ubTotal = ubTotal + ubTransaction.amount
    }
    context.ubTotal = ubTotal

//UD CONDITION
udCondition = [];
udCondition.add(EntityCondition.makeCondition("glReconciliationId", EntityOperator.EQUALS, glReconciliationId));
udCondition.add(EntityCondition.makeCondition("finAccountTransTypeId", EntityOperator.EQUALS, 'UD'));
List udTransList = delegator.findList('FinAccountTrans', EntityCondition.makeCondition(udCondition, EntityOperator.AND), null, ['finAccountTransId'], null, false)

    udTotal = 0
    udTransList.each { udTransaction ->
      udTotal = udTotal + udTransaction.amount
    }
    context.udTotal = udTotal

//DEPOSIT CONDITION
depositCondition = [];
depositCondition.add(EntityCondition.makeCondition("glReconciliationId", EntityOperator.EQUALS, glReconciliationId));
depositCondition.add(EntityCondition.makeCondition("finAccountTransTypeId", EntityOperator.EQUALS, 'DEPOSIT'));
List depositTransList = delegator.findList('FinAccountTrans', EntityCondition.makeCondition(depositCondition, EntityOperator.AND), null, ['finAccountTransId'], null, false)

    depositTotal = 0
    depositTransList.each { depositTransaction ->
      depositTotal = depositTotal + depositTransaction.amount
    }
    context.depositTotal = depositTotal

//WITHDRAWAL CONDITION
withdrawalCondition = [];
withdrawalCondition.add(EntityCondition.makeCondition("glReconciliationId", EntityOperator.EQUALS, glReconciliationId));
withdrawalCondition.add(EntityCondition.makeCondition("finAccountTransTypeId", EntityOperator.EQUALS, 'WITHDRAWAL'));
List withdrawalTransList = delegator.findList('FinAccountTrans', EntityCondition.makeCondition(withdrawalCondition, EntityOperator.AND), null, ['finAccountTransId'], null, false)

    withdrawalTotal = 0
    withdrawalTransList.each { withdrawalTransaction ->
      withdrawalTotal = withdrawalTotal + withdrawalTransaction.amount
    }
    context.withdrawalTotal = withdrawalTotal
