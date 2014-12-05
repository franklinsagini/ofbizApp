import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

partyId = parameters.partyId
loanStatusId = parameters.loanStatusId
loanProductId = parameters.loanProductId
LloanProductId = loanProductId.toLong();

context.product_id = LloanProductId
product = delegator.findOne("LoanProduct", [loanProductId : LloanProductId], false);
context.product = product;

//if i have loanStatusId && partyId
if (loanStatusId && partyId) {
  LloanStatusId = loanStatusId.toLong();
  LpartyId = partyId.toLong();
  context.loanDetailsList = delegator.findByAnd("LoanApplication", [loanStatusId : LloanStatusId, loanProductId : LloanProductId, partyId : LpartyId], null, false)
  return
}

//if i have loanStatusId
if (loanStatusId) {
  LloanStatusId = loanStatusId.toLong();
  context.loanDetailsList = delegator.findByAnd("LoanApplication", [loanStatusId : LloanStatusId, loanProductId : LloanProductId], null, false)
  return
}


//or else just query with the default passed loanProductId
context.loanDetailsList = delegator.findByAnd("LoanApplication", [loanProductId : LloanProductId], null, false)





