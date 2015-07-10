import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.accounting.budget.BudgetWorker;



budget = delegator.findOne('Budget', [budgetId:parameters.budgetId], true)
context.budget = budget
budgetItems = []
totalsList = []
totals = []
singleBudgetItems = []
allBudgetItems = []
currentBudgetItems = []
currentBudgetItems = BudgetWorker.getBudgetItems(budget)
allBudgetItems = delegator.findList('GlAccountAndBudgetItemSums', null, null, ['accountCode'], null, false)
currentAC = ''
commitedAmount = BigDecimal.ZERO //AMOUNT THAT HAS ALREADY BEEN COMMITED BUT NOT SPENT
actualAmount = BigDecimal.ZERO //AMOUNT THAT HAS ALREADY BEEN SPENT
varianceAmount = BigDecimal.ZERO //DIFF BTWN
totalCommitedAmount = BigDecimal.ZERO
totalActualAmount = BigDecimal.ZERO
totalVarianceAmount = BigDecimal.ZERO
totalBudgeted = BigDecimal.ZERO
budgetedAmount = BigDecimal.ZERO
totalCommitedAndSpent = BigDecimal.ZERO

allBudgetItems.each { allItem ->
  currentBudgetItems.each { currentItem ->
    if (currentItem.glAccountId == allItem.glAccountId && currentAC != allItem.glAccountId) {
      currentAC = currentItem.glAccountId
      budgetedAmount = allItem.amount
      totalCommitedAndSpent = commitedAmount.add(actualAmount)
      varianceAmount = budgetedAmount.subtract(totalCommitedAndSpent)
      singleBudgetItems = [
        accountCode:allItem.accountCode,
        accountName:allItem.accountName,
        amount:allItem.amount,
        commitedAmount:commitedAmount,
        actualAmount:actualAmount,
        varianceAmount:varianceAmount,
      ]
      budgetItems.add(singleBudgetItems);

      //Do the totals of the whole rows
      totalBudgeted = totalBudgeted.add(allItem.amount);
      totalVarianceAmount = totalVarianceAmount.add(varianceAmount)
      totalCommitedAmount = totalCommitedAmount.add(commitedAmount)
      totalActualAmount = totalActualAmount.add(actualAmount)
    }
  }
}
totalsList = [
  totalBudgeted:totalBudgeted,
  totalCommitedAmount:totalCommitedAmount,
  totalActualAmount:totalActualAmount,
  totalVarianceAmount:totalVarianceAmount
]

context.totalsList = totalsList
context.budgetItems = budgetItems
