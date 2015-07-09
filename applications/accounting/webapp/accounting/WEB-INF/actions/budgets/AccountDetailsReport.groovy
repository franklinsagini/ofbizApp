import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.accounting.budget.BudgetWorker;



budget = delegator.findOne('Budget', [budgetId:parameters.budgetId], true)
context.budget = budget
List allBudgetItems = BudgetWorker.getBudgetItems(budget)
budgetItems = []
singleBudgetItems = []
currentAC = ''
allBudgetItems.each { currentItem ->
  if (currentAC != currentItem.glAccountId) {
    currentAC = currentItem.glAccountId
    singleBudgetItems = [glAccountId:currentItem.glAccountId]
    budgetItems.add(singleBudgetItems);
  }
}
context.budgetItems = budgetItems


