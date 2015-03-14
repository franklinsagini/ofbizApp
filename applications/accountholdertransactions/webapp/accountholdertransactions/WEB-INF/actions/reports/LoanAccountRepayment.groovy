
import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityOperator

partyId = org.ofbiz.accountholdertransactions.LoanUtilities.stripStringName(partyId);
Long memberId = partyId.toLong();

//Get the running loans given member
class LoanOrAccount{
	def loanApplicationId
	def loanNo
	def loanProductId
	def loanAmt
	def loanBalance
	def totalExpected
	def interestDue
	def insuranceDue
	def principalDue
	def amountPaid
	def isLoan
}


def combinedList = [];
def totalAmount = BigDecimal.ZERO;

//loanApplication = delegator.makeValue();
disburseLoanStatusId = 6.toLong();
loansList = delegator.findByAnd("LoanApplication",  [partyId : memberId, loanStatusId: disburseLoanStatusId], null, false);
def statementItem;

// account transactions
memberAccountList = delegator.findByAnd("MemberAccount",  [partyId : memberId, contributing: 'YES'], null, false);

//accountTransactionList.eachWithIndex { accountItem, index ->
//	
//	statementItem = delegator.makeValue("ExpectedPaymentSent",
//		null);
//	statementItem.loanNo = "";
//	statementItem.createdStamp = accountItem.createdStamp;
//	
//	if (accountItem.transactionType == 'CASHDEPOSIT')
//	{
//		statementItem.remitanceDescription = 'Deposit';
//	} else if (accountItem.transactionType == 'CASHWITHDRAWAL'){
//		statementItem.remitanceDescription = 'Withdrawal';
//	} else{
//		statementItem.remitanceDescription = accountItem.transactionType;
//	}
//	
//	statementItem.amount = accountItem.transactionAmount;
//	
//	if (accountItem.increaseDecrease == 'I'){
//		statementItem.isReceived = 'Y';
//	}
//	if (accountItem.increaseDecrease == 'D'){
//		statementItem.isReceived = 'N';
//	}
//	
//	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
//	statementItem.totalAmount = totalAmount;
//	
//	//combinedList << statementItem
//}

loansList.eachWithIndex { loanItem, index ->
	
	
	/**
	 * 
	 * 
	def loanApplicationId
	def loanNo
	def loanProductId
	def loanAmt
	def loanBalance
	def totalExpected
	def interestDue
	def insuranceDue
	def principalDue
	def amountPaid
	def isLoan
	 * **/
	
	def loanMap = [:]
	loan = new LoanOrAccount()
	//loan.loanApplicationId = loanItem.loanApplicationId
	loanMap["loanApplicationId"] = loanItem.loanApplicationId
	//loan.loanNo = loanItem.loanNo
	loanMap["loanNo"] = loanItem.loanNo
	//loan.loanProductId = loanItem.loanProductId
	loanMap["loanProductId"] = loanItem.loanProductId
	
	/****
	 * 
	 * 		<set field="totalLoanDue" value="${bsh:org.ofbiz.accountholdertransactions.LoanRepayments.getTotalLoanDue(partyId);}" type="BigDecimal"/>
	 *  	<set field="totalInterestDue" value="${bsh:org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInterestDue(partyId);}" type="BigDecimal"/>
        	<set field="totalInsuranceDue" value="${bsh:org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInsuranceDue(partyId);}" type="BigDecimal"/>
        	<set field="totalPrincipalDue" value="${bsh:org.ofbiz.accountholdertransactions.LoanRepayments.getTotalPrincipalDue(partyId);}" type="BigDecimal"/>
	 * 
	 * 
	 * */
	def loanAmt
	def loanBalance
	def totalExpected
	def interestDue
	def insuranceDue
	def principalDue
	def amountPaid
	def isLoan

	loanApplicationIdStr = loanItem.loanApplicationId.toString();
	
	loan.loanAmt = loanItem.loanAmt
	loanMap["loanAmt"] = loanItem.loanAmt
	
	System.out.println (" The Money is  "+loanItem.loanAmt)
	loanBalance = org.ofbiz.loans.LoanServices.getLoanBalanceAmount(loanApplicationIdStr)
	loanMap["loanBalance"] = loanBalance
	totalExpected = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalLoanByLoanDue(loanApplicationIdStr)
	loanMap["totalLoanDue"] = totalExpected
	interestDue = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInterestByLoanDue(loanApplicationIdStr)
	loanMap["totalInterestDue"] = interestDue
	insuranceDue = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInsurancByLoanDue(loanApplicationIdStr)
	loanMap["totalInsuranceDue"] = insuranceDue
	
	principalDue = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalPrincipaByLoanDue(loanApplicationIdStr)
	loanMap["totalPrincipalDue"] = principalDue
	
	amountPaid = BigDecimal.ZERO
	loanMap["amountPaid"] = amountPaid
	isLoan = "Y"
	loanMap["isLoan"] = isLoan
	
	System.out.println (" Balance #########  "+loanBalance)
	
	combinedList.add(loanMap)
}

context.combinedList = combinedList





