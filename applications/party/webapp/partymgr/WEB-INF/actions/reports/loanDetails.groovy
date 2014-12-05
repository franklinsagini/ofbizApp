import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;

partyId = parameters.partyId
loanStatusId = parameters.loanStatusId
loanProductId = parameters.loanProductId

LloanStatusId = loanStatusId.toLong();
LloanProductId = loanProductId.toLong();



context.loanDetailsList = delegator.findByAnd("LoanApplication", [loanStatusId : LloanStatusId, loanProductId : LloanProductId], null, false)

context.LloanProductId = LloanStatusId
context.LloanStatusId = LloanStatusId
