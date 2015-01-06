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
if ((partyId != null) && (partyId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
}
if ((loanProductId != null) && (loanProductId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: disburseLoanStatusId], null, false);
}

if ((stationId != null) && (stationId != "")){
	myLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: disburseLoanStatusId], null, false);
}
context.myLoansList = myLoansList
context.partyId = partyId

