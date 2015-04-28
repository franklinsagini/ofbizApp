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
transactionType = null;
accountTransactionList.eachWithIndex { transactionItem, item ->
	partyId = transactionItem.partyId
	memberAccountId  = transactionItem.memberAccountId
	totalAmount = totalAmount + transactionItem.transactionAmount
	referenceNo = transactionItem.acctgTransId
	
	if (transactionItem.transactionType.equals("CASHWITHDRAWAL")){
		transactionType = "CASHWITHDRAWAL"
	}
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


if (transactionType != null)
	context.transactionType = transactionType
