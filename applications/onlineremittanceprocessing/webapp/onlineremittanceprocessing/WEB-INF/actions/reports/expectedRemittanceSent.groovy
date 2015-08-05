import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

pushMonthYearStationId = parameters.pushMonthYearStationId
pushMonthYearStationIdLong = pushMonthYearStationId.toLong()





//Loans - by member or by station or by product
//disburseLoanStatusId = 6.toLong();

expectedRemittanceList = delegator.findByAnd("ExpectedPaymentSent",  [pushMonthYearStationId : pushMonthYearStationIdLong], null, false);



//myAccountsList.eachWithIndex { myAccount, index ->
//	memberAccountIdStr = myAccount.memberAccountId.toString();
//	//Book Balance
//	myAccount.minSavingsAmt = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getBookBalanceNow(memberAccountIdStr);
//	//Available Balance
//	myAccount.interestPerAnum = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getTotalBalanceNow(memberAccountIdStr);
//	
//	
//	//combinedList << loanItem
//}

context.expectedRemittanceList = expectedRemittanceList
context.pushMonthYearStationId = pushMonthYearStationIdLong