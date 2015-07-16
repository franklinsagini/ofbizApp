import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
loanProductId = parameters.loanProductId

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

if ((partyId != null) && (partyId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: defaultedLoanStatusId], null, false);
	
	overpayedList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: overpayedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
}
if ((loanProductId != null) && (loanProductId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: disburseLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: defaultedLoanStatusId], null, false);
	
	overpayedList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: overpayedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
	
}

if ((stationId != null) && (stationId != "")){
	myLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: overpayedLoanStatusId], null, false);
	
	defaultedLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: defaultedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
}

//if ((partyId == "") && (loanProductId == "") && (stationId == "")){
//	myLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
//}

if ((partyId == "") && (loanProductId == "") && (stationId == "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoanApplication",  [loanStatusId: overpayedLoanStatusId], null, false);
	defaultedLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: defaultedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList = myLoansList + defaultedLoansList;
}

context.myLoansList = myLoansList
context.partyId = partyId

