import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
accountProductId = parameters.accountProductId

branchId = parameters.branchId

if ((partyId != null) && (partyId != "")){
	lpartyId = partyId.toLong();
}

if ((stationId != null) && (stationId != "")){
	lstationId = stationId.toLong();
}

	if ((accountProductId != null) && (accountProductId != "")){
	laccountProductId = accountProductId.toLong();
}



//Loans - by member or by station or by product
//disburseLoanStatusId = 6.toLong();

if ((partyId != null) && (partyId != "")){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  [partyId : lpartyId], null, false);
}
if ((accountProductId != null) && (accountProductId != "")){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  [accountProductId : laccountProductId], null, false);
}

if ((stationId != null) && (stationId != "")){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  [stationId : lstationId], null, false);
}


if ((partyId == "") && (accountProductId == "") && (stationId == "") && (branchId == "")){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  null, null, false);
}

if ((stationId != "") && (accountProductId != "") ){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  [stationId : lstationId, accountProductId : laccountProductId], null, false);
}

if ((branchId != null) && (branchId != "")){
	myAccountsList = delegator.findByAnd("MemberAccountBalance",  [branchId : branchId], null, false);
}


myAccountsList.eachWithIndex { myAccount, index ->
	memberAccountIdStr = myAccount.memberAccountId.toString();
	//Book Balance
	myAccount.minSavingsAmt = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getBookBalanceNow(memberAccountIdStr);
	//Available Balance
	myAccount.interestPerAnum = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getTotalBalanceNow(memberAccountIdStr);
	
	
	//combinedList << loanItem
}

context.myAccountsList = myAccountsList
context.partyId = partyId