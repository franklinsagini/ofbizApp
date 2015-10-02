import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;

import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;

action = request.getParameter("action");


asDateQuery = parameters.asDate
transactionType = parameters.transaction

transactionals = [];

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
 
          transactionList.eachWithIndex {transactionItem, index ->
 
			 accountNo = org.ofbiz.humanres.Leave.getAccountNo(transactionItem.memberAccountId);
			 accountName = org.ofbiz.humanres.Leave.getAccountName(transactionItem.memberAccountId);
			 mobileNumber =  org.ofbiz.humanres.Leave.getMobileNo(transactionItem.partyId);
			 transactionAmount = transactionItem.getString("transactionAmount");
			 memberNumber =org.ofbiz.humanres.Leave.getMemberNumber(transactionItem.partyId);
			 createdStamp = transactionItem.getString("createdStamp"); 
 
            println("########INSIDE GR AccountNo"+accountNo)
            println("########INSIDE GR accountName"+accountName)
            println("########INSIDE GR MobileNumber"+mobileNumber)
            println("########INSIDE GR MemberNumber"+memberNumber)
            
		 transactionals.add([accountNo :accountNo, accountName :accountName, mobileNumber : mobileNumber, transactionAmount : transactionAmount, memberNumber : memberNumber,
		 createdStamp : createdStamp ]);
		 }
 
 
    context.transactionals = transactionals;
    context.transactionType = transact.transactionType
