import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;


exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
partyId = parameters.partyId
LpartyId = partyId.toLong();
expr = exprBldr.AND() {
			EQUALS(partyId: LpartyId)
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
context.memAccount = delegator.findList("MemberAccountDetails", expr, null, null, findOptions, false);

context.memmm = delegator.findOne("Member", [partyId : LpartyId], false);

