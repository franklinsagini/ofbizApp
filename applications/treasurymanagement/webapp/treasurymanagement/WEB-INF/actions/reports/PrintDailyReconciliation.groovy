import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;
destinationTreasury = parameters.destinationTreasury
transferDate = parameters.transferDate


java.sql.Date sqlTransferDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((transferDate?.trim())){
	dateTransferDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(transferDate);

	sqlTransferDate = new java.sql.Date(dateTransferDate.getTime());
}

endOfDay = org.ofbiz.treasurymanagement.TreasuryUtility.getEndOfDay(sqlTransferDate)
context.title = "Chai Sacco"

class TreasuryTransaction{
	def memberAccountNo
	def description
	def transactionAmount
	def increaseDecrease
	def transactionDateTime
}
def mergedTransactionsList = [];

def tellerName = null

//Add opening Balance
openingBalance = BigDecimal.ZERO;
openingBalance = org.ofbiz.treasurymanagement.TreasuryUtility.getOpeningBalance(destinationTreasury, sqlTransferDate);

treasuryTransaction =  new TreasuryTransaction();

treasuryTransaction.memberAccountNo = "Opening Balance"
treasuryTransaction.description = "Opening Balance"
treasuryTransaction.transactionAmount = openingBalance
treasuryTransaction.increaseDecrease = "I"
treasuryTransaction.transactionDateTime = sqlTransferDate

mergedTransactionsList.add(treasuryTransaction)


//Get the transfers and add
transfersInTotalList = delegator.findByAnd("TreasuryTransfersIncomingTotalDetailed",  [transferDate : sqlTransferDate, destinationTreasury: destinationTreasury], null, false);
transfersInTotalList.eachWithIndex { transferInItem, inIndex ->
	//totalOut = totalOut.add(transferOutItem.transactionAmount);
	
	treasuryTransaction =  new TreasuryTransaction();
	
	treasuryTransaction.memberAccountNo = "Transfer In"
	treasuryTransaction.description = "Transfer In"
	treasuryTransaction.transactionAmount = transferInItem.transactionAmount
	treasuryTransaction.increaseDecrease = "I"
	treasuryTransaction.transactionDateTime = transferInItem.createdStamp
	
	if (tellerName == null){
		tellerName = transferInItem.name
	}
	
	mergedTransactionsList.add(treasuryTransaction)
	
}


transfersOutTotalList = delegator.findByAnd("TreasuryTransfersOutgoingTotalDetailed",  [transferDate : sqlTransferDate, sourceTreasury: destinationTreasury], null, false);
transfersOutTotalList.eachWithIndex { transferOutItem, outIndex ->
	//totalOut = totalOut.add(transferOutItem.transactionAmount);
	
	treasuryTransaction =  new TreasuryTransaction();
	
	treasuryTransaction.memberAccountNo = "Transfer Out"
	treasuryTransaction.description = "Transfer Out "
	treasuryTransaction.transactionAmount = transferOutItem.transactionAmount
	treasuryTransaction.increaseDecrease = "D"
	treasuryTransaction.transactionDateTime = transferOutItem.createdStamp
	
	if (tellerName == null){
		tellerName = transferInItem.name
	}
	mergedTransactionsList.add(treasuryTransaction)
	
}

//Get the Cash Deposit transactions and add
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	transferDateTimestamp = new Timestamp(sqlTransferDate.getTime());
	//endDateTimestamp = new Timestamp(endOfDay.getTime());
	
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'CASHDEPOSIT')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	EntityFindOptions findOptions = new EntityFindOptions();
	accountTransactionCashDepositList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionCashDepositList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountNumber(depositsItem.memberAccountId)
		treasuryTransaction.description = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountName(depositsItem.memberAccountId)+" (Cash Deposit)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "I"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}

	
//Adding Loan Repay
	
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'LOANCASHPAY')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	findOptions = new EntityFindOptions();
	accountTransactionLoanCashRepayList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionLoanCashRepayList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		loanApplicationId = depositsItem.loanApplicationId
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberNumberLoanNumber(loanApplicationId.toLong());
		treasuryTransaction.description = " (Loan Cash Pay)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "I"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}
	
//accountTransactionCashDepositList = delegator.findByAnd("AccountTransaction",  [treasuryId : destinationTreasury, ], null, false);

//Get the Cash Withdrawals transactions and add
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	transferDateTimestamp = new Timestamp(sqlTransferDate.getTime());
	//endDateTimestamp = new Timestamp(endOfDay.getTime());
	
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'CASHWITHDRAWAL')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	findOptions = new EntityFindOptions();
	accountTransactionCashDepositList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionCashDepositList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountNumber(depositsItem.memberAccountId)
		treasuryTransaction.description = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountName(depositsItem.memberAccountId)+" (Cash Withdrawal)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "D"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}
	
	//add CASHWITHDRAWALREVERSED
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	transferDateTimestamp = new Timestamp(sqlTransferDate.getTime());
	//endDateTimestamp = new Timestamp(endOfDay.getTime());
	
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'CASHWITHDRAWALREVERSED')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	findOptions = new EntityFindOptions();
	accountTransactionCashDepositList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionCashDepositList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountNumber(depositsItem.memberAccountId)
		treasuryTransaction.description = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountName(depositsItem.memberAccountId)+" (Cash Withdrawal)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "I"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}
	
	
	//minus CASHDEPOSITREVERSED
	exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
	
	transferDateTimestamp = new Timestamp(sqlTransferDate.getTime());
	//endDateTimestamp = new Timestamp(endOfDay.getTime());
	
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'CASHDEPOSITREVERSED')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	findOptions = new EntityFindOptions();
	accountTransactionCashDepositList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionCashDepositList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountNumber(depositsItem.memberAccountId)
		treasuryTransaction.description = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountName(depositsItem.memberAccountId)+" (Cash Withdrawal)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "D"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}

	
	//Minus LOANCASHPAYREVERSED
	expr = exprBldr.AND() { //Timestamp
		GREATER_THAN_EQUAL_TO(createdStamp: transferDateTimestamp)
		LESS_THAN_EQUAL_TO(createdStamp: endOfDay)
		EQUALS(treasuryId: destinationTreasury)
		EQUALS(transactionType: 'LOANCASHPAYREVERSED')
	}
	
	//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
	
	//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
	findOptions = new EntityFindOptions();
	accountTransactionLoanCashReversalList = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	
	
	accountTransactionLoanCashReversalList.eachWithIndex { depositsItem, depIndex ->
		//totalOut = totalOut.add(transferOutItem.transactionAmount);
		
		treasuryTransaction =  new TreasuryTransaction();
		loanApplicationId = depositsItem.loanApplicationId
		treasuryTransaction.memberAccountNo = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberNumberLoanNumber(loanApplicationId.toLong());
		treasuryTransaction.description = " (Loan Cash Reversal)"
		treasuryTransaction.transactionAmount = depositsItem.transactionAmount
		treasuryTransaction.increaseDecrease = "D"
		treasuryTransaction.transactionDateTime = depositsItem.createdStamp
		
		mergedTransactionsList.add(treasuryTransaction)
		
	}
	
	
	mergedTransactionsList.sort{it.transactionDateTime};

	context.mergedTransactionsList = mergedTransactionsList
	context.transferDate = sqlTransferDate
	context.tellerName = tellerName
