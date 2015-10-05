import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;

import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;

asDateQuery = parameters.asDate
transactionType = parameters.transaction

     if ((transactionType != null) && (transactionType != "")){
	         transactionTypeToLong = transactionType.toLong();
        }

    exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
    
     println("####TYPE LONG####"+transactionTypeToLong)
     println("####Date####"+asDateQuery)
    
     transact =  delegator.findOne("AccountProductCharge", [accountProductChargeId : transactionTypeToLong], false);
    
        expr = exprBldr.AND() {
			EQUALS(transactionType : transact.transactionType)
		}
		
     transactionList = delegator.findList("AccountTransaction", expr, null, null, null, false);
    
 
    println("####TYPE####"+transactionType)
     println("####Date####"+asDateQuery)


    context.transactionList = transactionList ;
    context.asDateQuery = asDateQuery ;
    context.transactionType = transact.transactionType ;
    
 





