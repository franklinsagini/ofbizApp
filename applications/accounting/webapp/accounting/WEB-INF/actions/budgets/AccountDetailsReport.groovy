import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator
import org.ofbiz.accounting.budget.BudgetWorker;



budget = delegator.findOne('Budget', [budgetId:parameters.budgetId], true)
context.budget = budget
List allBudgetItems = allBudgetItems = BudgetWorker.getBudgetItems(budget)
List budgetItems = delegator.findList('GlAccountAndBudgetItemSums', null, null, ['accountCode'], null, false)

context.budgetItems = budgetItems
context.allBudgetItems = allBudgetItems

