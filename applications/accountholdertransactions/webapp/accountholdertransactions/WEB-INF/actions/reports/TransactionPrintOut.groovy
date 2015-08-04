import groovy.time.*
accountTransactionParentId = parameters.accountTransactionParentId

context.title = "Chai Sacco"

accountTransactionParent = delegator.findOne("AccountTransactionParent", [accountTransactionParentId : accountTransactionParentId], false);
context.accountTransactionParent = accountTransactionParent

if (accountTransactionParentId) {
	accountTransactionList = delegator.findByAnd("AccountTransaction",  [accountTransactionParentId : accountTransactionParentId], null, false);
	   context.accountTransactionList = accountTransactionList;
}

referenceNo = null;
partyId = null;
memberAccountId = null;
totalAmount = BigDecimal.ZERO;
depositTotalAmount = BigDecimal.ZERO;
transactionType = null;
transactionTypeWithdrawal = null;
chequeNo = null;
accountTransactionList.eachWithIndex { transactionItem, item ->
	partyId = transactionItem.partyId
	memberAccountId  = transactionItem.memberAccountId
	totalAmount = totalAmount + transactionItem.transactionAmount
	referenceNo = transactionItem.acctgTransId
	
	if (transactionItem.transactionType.equals("CASHWITHDRAWAL")){
		transactionType = "CASHWITHDRAWAL"
		transactionTypeWithdrawal = "CASHWITHDRAWAL"
	}
	
	if (transactionItem.transactionType.equals("CHEQUEDEPOSIT")){
		transactionType = "CHEQUEDEPOSIT";
		depositTotalAmount = transactionItem.transactionAmount;
		chequeNo = transactionItem.chequeNo;
	}
	
	if (transactionItem.transactionType.equals("CHEQUEWITHDRAWAL")){
		chequeNo = transactionItem.chequeNo;
	}
	
	if (transactionItem.transactionType.equals("BANKERSWITHDRAWAL")){
		chequeNo = transactionItem.chequeNo;
		transactionTypeWithdrawal = "Bankers Cheque"
		transactionType = "Bankers Cheque";
	}
	
}

if ((transactionType != null) && (transactionType.equals("CHEQUEDEPOSIT"))){
	totalAmount = depositTotalAmount;
}

context.totalAmount = totalAmount
member = delegator.findOne("Member", [partyId : partyId], false);
context.member = member
memberAccount =  delegator.findOne("MemberAccount", [memberAccountId : memberAccountId], false);
context.memberAccount = memberAccount

branch = delegator.findOne("PartyGroup", [partyId : member.branchId], false);
context.branch =  branch
context.createdBy = accountTransactionParent.createdBy

//Get the Balance after this transaction
balanceAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getAvailableBalanceVer3(memberAccountId.toString());
context.balanceAmount = balanceAmount
context.referenceNo = referenceNo
context.chequeNo = chequeNo
context.transactionTypeWithdrawal = transactionTypeWithdrawal



if (transactionType != null)
	context.transactionType = transactionType
