import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
accountProductId = parameters.accountProductId

branchId = parameters.branchId


endDate = parameters.endDate

java.sql.Date sqlEndDate = null;

dateEndDate = null;
sqlEndDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

//(endDate != null) ||
//if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
//}

endDateTimestamp = new Timestamp(sqlEndDate.getTime());


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
	//getBookBalanceVer3
	//myAccount.minSavingsAmt = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getBookBalanceNow(memberAccountIdStr);
	myAccount.minSavingsAmt = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getBookBalanceVer3(memberAccountIdStr, endDateTimestamp);
	
	
	//Available Balance
	//getTotalBalance
	//myAccount.interestPerAnum = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getTotalBalanceNow(memberAccountIdStr);
	myAccount.interestPerAnum = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getTotalBalance(memberAccountIdStr, endDateTimestamp);
	
	
	//combinedList << loanItem
}

context.myAccountsList = myAccountsList
context.partyId = partyId