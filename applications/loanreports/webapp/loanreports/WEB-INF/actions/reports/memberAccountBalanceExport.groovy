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
memAccountDetalis = [];
memAccount = delegator.findList("MemberAccountDetails", expr, null, null, findOptions, false);

memAccount.eachWithIndex { memAccountItem, index ->
acc = delegator.findOne("MemberAccount", [memberAccountId : memAccountItem.memberAccountId], false);
accproduct = delegator.findOne("AccountProduct", [accountProductId : acc.accountProductId], false);


AccountCode = accproduct.code;
AccountType = accproduct.name;
AccountNo = acc.accountNo;
AccountBalance = memAccountItem.savingsOpeningBalance;


  
  memAccountDetalis.add([AccountCode :AccountCode, AccountType :AccountType, AccountNo : AccountNo, AccountBalance : AccountBalance]);
 }
 
 

context.memAccountDetalis = memAccountDetalis;

