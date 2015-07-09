import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.accounting.budget.BudgetWorker;



budget = delegator.findOne('Budget', [budgetId:parameters.budgetId], true)
context.budget = budget
budgetItems = []
singleBudgetItems = []
allBudgetItems = []
currentBudgetItems = []
currentBudgetItems = BudgetWorker.getBudgetItems(budget)
allBudgetItems = delegator.findList('GlAccountAndBudgetItemSums', null, null, ['accountCode'], null, false)
currentAC = ''
allBudgetItems.each { allItem ->
  currentBudgetItems.each { currentItem ->
    if (currentItem.glAccountId == allItem.glAccountId && currentAC != allItem.glAccountId) {
      currentAC = currentItem.glAccountId
      singleBudgetItems = [accountCode:allItem.accountCode, accountName:allItem.accountName, amount:allItem.amount]
      budgetItems.add(singleBudgetItems);
    }
  }
}
context.budgetItems = budgetItems
