import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;

action = request.getParameter("action");


transactionType = parameters.transaction
startDate = parameters.startDate
endDate = parameters.endDate
branchId = parameters.branchId

action = request.getParameter("action");

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

startDateTimestamp = new Timestamp(sqlStartDate.getTime());
endDateTimestamp = new Timestamp(sqlEndDate.getTime());


print "formatted Date"


transactionals = [];

     if ((transactionType != null) && (transactionType != "")){
	         transactionTypeToLong = transactionType.toLong();
        }

    exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
    
     println("####TYPE LONG####"+transactionTypeToLong)
   
    
     transact =  delegator.findOne("AccountProductCharge", [accountProductChargeId : transactionTypeToLong], false);
    
    if (!(sqlEndDate)){
     
        expr = exprBldr.AND() {
			EQUALS(transactionType : transact.transactionType)
		 }
     }
    
    	if ((sqlEndDate) && (transactionType)){
		expr = exprBldr.AND() {
			  GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			  LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			  EQUALS(transactionType : transact.transactionType)
		   }
	   }
     
     	
	if ((branchId) && (transactionType) && (sqlEndDate) ){
		expr = exprBldr.AND() {
		    GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			EQUALS(transactionType : transact.transactionType)
			EQUALS(branchId: branchId)
		}
	}
	
	
	  
 	
     transactionList = delegator.findList("AccountTransaction", expr, null, null, null, false);
 
          transactionList.eachWithIndex {transactionItem, index ->
 
			 accountNo = org.ofbiz.humanres.Leave.getAccountNo(transactionItem.memberAccountId);
			 accountName = org.ofbiz.humanres.Leave.getAccountName(transactionItem.memberAccountId);
			 mobileNumber =  org.ofbiz.humanres.Leave.getMobileNo(transactionItem.partyId);
			 transactionAmount = transactionItem.getString("transactionAmount");
			 memberNumber =org.ofbiz.humanres.Leave.getMemberNumber(transactionItem.partyId);
			 createdStamp = transactionItem.getString("createdStamp"); 
             chequeNo = transactionItem.getString("chequeNo");
             slipNumber = transactionItem.getString("slipNumber");
             
             reference = transactionItem.getString("reference");
             accountTransactionId = transactionItem.getString("accountTransactionId");
             receiptNo = transactionItem.getString("receiptNo");
             branchId = transactionItem.branchId
             
            println("########INSIDE GR AccountNo"+accountNo)
            println("########INSIDE GR accountName"+accountName)
            println("########INSIDE GR MobileNumber"+mobileNumber)
            println("########INSIDE GR MemberNumber"+memberNumber)
            println("########INSIDE GR Date "+endDateTimestamp)
            
            accountTransactionId
            
		 transactionals.add([branchId:branchId, accountTransactionId : accountTransactionId, receiptNo : receiptNo,  reference : reference,slipNumber:slipNumber, chequeNo :chequeNo ,accountNo :accountNo, accountName :accountName, mobileNumber : mobileNumber, transactionAmount : transactionAmount, memberNumber : memberNumber,
		 createdStamp : createdStamp ]);
		 }
 
 
    context.transactionals = transactionals;
    context.transactionType = transact.transactionType
