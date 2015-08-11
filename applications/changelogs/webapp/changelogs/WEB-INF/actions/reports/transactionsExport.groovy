import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;


startDate = parameters.startDate
endDate = parameters.endDate

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


	allTransactions = null;
	if ((sqlStartDate == null) && (sqlEndDate == null)){
		allTransactions = delegator.findByAnd("AccountTransaction",  null, null, false);
	} else {
		//Filter by start and end date
		exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
		
		startDateTimestamp = new Timestamp(sqlStartDate.getTime());
		endDateTimestamp = new Timestamp(sqlEndDate.getTime());
		
		expr = exprBldr.AND() { //Timestamp
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
		}
		
		//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
		
		//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
		EntityFindOptions findOptions = new EntityFindOptions();
		allTransactions = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	}

//findOptions.setMaxRows(100);
transactionDetalis = [];
//memAccount = delegator.findList("AccountTransaction", expr, null, null, findOptions, false);

allTransactions.eachWithIndex { transactionItem, index ->

	if (transactionItem.memberAccountId != null){
		acc = delegator.findOne("MemberAccount", [memberAccountId : transactionItem.memberAccountId], false);
		accproduct = delegator.findOne("AccountProduct", [accountProductId : acc.accountProductId], false);


		AccountCode = accproduct.code;
		AccountType = accproduct.name;
		AccountNo = acc.accountNo;
	}
	else{
		AccountCode = "";
		AccountType = "";
		AccountNo = "";

	}
//AccountBalance = memAccountItem.savingsOpeningBalance;
TransactionAmount = transactionItem.transactionAmount;
TransactionType = transactionItem.transactionType;

TransactionDate = transactionItem.createdStamp;
CreatedBy = transactionItem.createdBy;

  
  transactionDetalis.add([AccountCode :AccountCode, AccountType :AccountType, AccountNo : AccountNo, TransactionAmount : TransactionAmount, TransactionType: TransactionType, CreatedBy: CreatedBy, TransactionDate: TransactionDate]);
 }
 
 

context.transactionDetalis = transactionDetalis;

