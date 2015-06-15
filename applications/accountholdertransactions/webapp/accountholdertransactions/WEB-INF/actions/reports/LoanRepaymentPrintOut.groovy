import groovy.time.*
acctgTransId = parameters.acctgTransId

context.title = "Chai Sacco"


if (acctgTransId) {
	loanRepaymentList = delegator.findByAnd("LoanRepayment",  [acctgTransId : acctgTransId], null, false);
	   context.loanRepaymentList = loanRepaymentList;
}

referenceNo = null;
partyId = null;
loanApplicationId = null;
totalAmount = BigDecimal.ZERO;
transactionType = null;
loanRepaymentList.eachWithIndex { repaymentItem, item ->
	partyId = repaymentItem.partyId
	loanApplicationId  = repaymentItem.loanApplicationId
	totalAmount = totalAmount + repaymentItem.transactionAmount
	referenceNo = repaymentItem.acctgTransId
	createdBy = repaymentItem.createdBy
	transactionType = "Loan Repayment"
	loanRepayment = repaymentItem
}

context.totalAmount = totalAmount
member = delegator.findOne("Member", [partyId : partyId], false);
context.member = member
loanApplication =  delegator.findOne("LoanApplication", [loanApplicationId : loanApplicationId], false);
context.loanApplication = loanApplication

branch = delegator.findOne("PartyGroup", [partyId : member.branchId], false);
context.branch =  branch
context.createdBy = createdBy

//Get the Balance after this transaction
//balanceAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getAvailableBalanceVer3(memberAccountId.toString());
//context.balanceAmount = balanceAmount
context.referenceNo = referenceNo
context.loanRepayment = loanRepayment


if (transactionType != null)
	context.transactionType = transactionType