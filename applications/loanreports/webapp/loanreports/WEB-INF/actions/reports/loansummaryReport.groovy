import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
loanProductId = parameters.loanProductId
branchId = parameters.branchId

if ((partyId != null) && (partyId != "")){
	lpartyId = partyId.toLong();
}

if ((stationId != null) && (stationId != "")){
	lstationId = stationId.toLong();
}

	if ((loanProductId != null) && (loanProductId != "")){
	lloanProductId = loanProductId.toLong();
}



//Loans - by member or by station or by product
disburseLoanStatusId = 6.toLong();
overpayedLoanStatusId = 7.toLong();
defaultedLoanStatusId = 10030.toLong();
claimedLoanStatusId = 10013.toLong();

if ((partyId != null) && (partyId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: defaultedLoanStatusId], null, false);
	
	overpayedList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: overpayedLoanStatusId], null, false);
	claimedLoanList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	myLoansList = myLoansList + claimedLoanList;
	
}
if ((loanProductId != null) && (loanProductId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: disburseLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: defaultedLoanStatusId], null, false);
	
	overpayedList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: overpayedLoanStatusId], null, false);
	
	claimedLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: claimedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	myLoansList = myLoansList + claimedLoansList;
	
	
}

if ((stationId != null) && (stationId != "")){
	myLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: overpayedLoanStatusId], null, false);
	
	defaultedLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: defaultedLoanStatusId], null, false);
	
	claimedLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	myLoansList = myLoansList + claimedLoansList;
	
}



if ((branchId != null) && (branchId != "")){
	myLoansList = delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: overpayedLoanStatusId], null, false);
	
	defaultedLoansList = delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: defaultedLoanStatusId], null, false);
	
	claimedLoansList = delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	myLoansList = myLoansList + claimedLoansList;
	
}

//if ((partyId == "") && (loanProductId == "") && (stationId == "")){
//	myLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
//}

if ((partyId == "") && (loanProductId == "") && (stationId == "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoanApplication",  [loanStatusId: overpayedLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: defaultedLoanStatusId], null, false);
	claimedLoanList = delegator.findByAnd("LoanApplication",  [loanStatusId: claimedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	myLoansList = myLoansList + claimedLoanList;
}

context.myLoansList = myLoansList
context.partyId = partyId

