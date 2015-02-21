import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
loanProductId = parameters.loanProductId

lpartyId = null;
if ((partyId != null) && (partyId != "")){
	lpartyId = partyId.toLong();
}

lstationId = null;

if ((stationId != null) && (stationId != "")){
	lstationId = stationId.toLong();
}

lloanProductId = null;
if ((loanProductId != null) && (loanProductId != "")){
	lloanProductId = loanProductId.toLong();
}



//Loans - by member or by station or by product
//disburseLoanStatusId = 6.toLong();
//overpayedLoanStatusId = 7.toLong();

if ((partyId != null) && (partyId != "")){
	myGuarantorList = delegator.findByAnd("GuarantorListing",  [guarantorId : lpartyId], null, false);
}

if ((loanProductId != null) && (loanProductId != "")){
	myGuarantorList = delegator.findByAnd("GuarantorListing",  [loanProductId : lloanProductId], null, false);
	
}

if ((stationId != null) && (stationId != "")){
	myGuarantorList = delegator.findByAnd("GuarantorListing",  [guarantorStationId : lstationId], null, false);
}


context.myGuarantorList = myGuarantorList
context.partyId = partyId

