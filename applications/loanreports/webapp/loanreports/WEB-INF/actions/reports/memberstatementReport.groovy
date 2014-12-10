import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId

lpartyId = partyId.toLong();

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;

expectedPaymentReceivedList = delegator.findByAnd("ExpectedPaymentReceived",  [payrollNo : payrollNo], null, false);
expectedPaymentSentList = delegator.findByAnd("ExpectedPaymentSent",  [payrollNo : payrollNo], null, false);

def combinedList = [];
def totalAmount = BigDecimal.ZERO;

//loanApplication = delegator.makeValue();
disburseLoanStatusId = 6.toLong();
loansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
def statementItem;

//add account transactions
accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId], null, false);

accountTransactionList.eachWithIndex { accountItem, index ->
	
	statementItem = delegator.makeValue("ExpectedPaymentSent",
		null);
	statementItem.loanNo = "";
	statementItem.createdStamp = accountItem.createdStamp;
	
	if (accountItem.transactionType == 'CASHDEPOSIT')
	{
		statementItem.remitanceDescription = 'Deposit';
	} else if (accountItem.transactionType == 'CASHWITHDRAWAL'){
		statementItem.remitanceDescription = 'Withdrawal';
	} else{
		statementItem.remitanceDescription = accountItem.transactionType;
	}
	
	statementItem.amount = accountItem.transactionAmount;
	
	if (accountItem.increaseDecrease == 'I'){
		statementItem.isReceived = 'Y';
	}
	if (accountItem.increaseDecrease == 'D'){
		statementItem.isReceived = 'N';
	}	
	
	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
	statementItem.totalAmount = totalAmount;
	
	combinedList << statementItem
}

loansList.eachWithIndex { loanItem, index ->
	
	statementItem = delegator.makeValue("ExpectedPaymentSent",
		null);
	statementItem.loanNo = loanItem.loanNo;
	statementItem.createdStamp = loanItem.createdStamp;
	statementItem.remitanceDescription = "Loan Disbursement";
	statementItem.amount = loanItem.loanAmt;
	statementItem.isReceived = 'N';
	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
	statementItem.totalAmount = totalAmount;
	
	combinedList << statementItem
}

expectedPaymentSentList.eachWithIndex { sentListValue, index ->
	sentListValue.isReceived = 'N'
	
	if (sentListValue.amount != null){
		totalAmount = totalAmount + sentListValue.amount.toBigDecimal();
	}
	
	sentListValue.totalAmount = totalAmount;
	combinedList << sentListValue
	
	}

expectedPaymentReceivedList.eachWithIndex { receivedListValue, index ->
	receivedListValue.isReceived = 'Y'
	
	if (receivedListValue.amount != null){
		totalAmount = totalAmount.subtract(receivedListValue.amount.toBigDecimal());
	}
	receivedListValue.totalAmount = totalAmount;
	combinedList << receivedListValue
	
	}


//expectedPaymentSentList +=expectedPaymentSentList;
combinedList.sort{it.createdStamp};
newTotal = BigDecimal.ZERO;
combinedList.eachWithIndex { listItem, index ->
	if (listItem.amount != null){
		newTotal = newTotal + listItem.amount;
	}
	listItem.totalAmount = newTotal;
}
context.combinedList = combinedList;

