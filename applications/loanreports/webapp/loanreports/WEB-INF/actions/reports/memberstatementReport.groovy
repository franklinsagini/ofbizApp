import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId

lpartyId = partyId.toLong();

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;


class MemberStatement{
	def name
	def code
	def itemTotal
	def listOfTransactions = []
}

class MemberTransaction{
	def transactionDate
	def transactionDescription
	def increaseDecrease
	def transactionAmount
}

expectedPaymentReceivedList = delegator.findByAnd("ExpectedPaymentReceived",  [payrollNo : payrollNo], null, false);
expectedPaymentSentList = delegator.findByAnd("ExpectedPaymentSent",  [payrollNo : payrollNo], null, false);

def combinedList = [];
def totalAmount = BigDecimal.ZERO;

//loanApplication = delegator.makeValue();
disburseLoanStatusId = 6.toLong();
loansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
def statementItem;

//add account transactions
accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId], null, false);

accountTransactionList.eachWithIndex { accountItem, index ->
	
	statementItem = delegator.makeValue("ExpectedPaymentSent",
		null);
	statementItem.loanNo = "";
	statementItem.createdStamp = accountItem.createdStamp;
	
	if (accountItem.transactionType == 'CASHDEPOSIT')
	{
		statementItem.remitanceDescription = 'Deposit';
	} else if (accountItem.transactionType == 'CASHWITHDRAWAL'){
		statementItem.remitanceDescription = 'Withdrawal';
	} else{
		statementItem.remitanceDescription = accountItem.transactionType;
	}
	
	statementItem.amount = accountItem.transactionAmount;
	
	if (accountItem.increaseDecrease == 'I'){
		statementItem.isReceived = 'Y';
	}
	if (accountItem.increaseDecrease == 'D'){
		statementItem.isReceived = 'N';
	}	
	
	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
	statementItem.totalAmount = totalAmount;
	
	combinedList << statementItem
}

loansList.eachWithIndex { loanItem, index ->
	
	statementItem = delegator.makeValue("ExpectedPaymentSent",
		null);
	statementItem.loanNo = loanItem.loanNo;
	statementItem.createdStamp = loanItem.disbursementDate;
	statementItem.remitanceDescription = "Loan Disbursement";
	statementItem.amount = loanItem.loanAmt;
	statementItem.isReceived = 'N';
	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
	statementItem.totalAmount = totalAmount;
	
	combinedList << statementItem
}

expectedPaymentSentList.eachWithIndex { sentListValue, index ->
	sentListValue.isReceived = 'N'
	
	if (sentListValue.amount != null){
		totalAmount = totalAmount + sentListValue.amount.toBigDecimal();
	}
	
	sentListValue.totalAmount = totalAmount;
	combinedList << sentListValue
	
	}

expectedPaymentReceivedList.eachWithIndex { receivedListValue, index ->
	receivedListValue.isReceived = 'Y'
	
	if (receivedListValue.amount != null){
		totalAmount = totalAmount.subtract(receivedListValue.amount.toBigDecimal());
	}
	receivedListValue.totalAmount = totalAmount;
	combinedList << receivedListValue
	
	}


//expectedPaymentSentList +=expectedPaymentSentList;
combinedList.sort{it.createdStamp};
newTotal = BigDecimal.ZERO;
combinedList.eachWithIndex { listItem, index ->
	if (listItem.amount != null){
		newTotal = newTotal + listItem.amount;
	}
	listItem.totalAmount = newTotal;
}
context.combinedList = combinedList;

def memberStatement =  new MemberStatement()
def memberStatementList = []

theDisburseLoanStatusId = 6.toLong();
theClearedLoanStatusId = 7.toLong();

allDisbursedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: theDisburseLoanStatusId], null, false);
allClearedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: theClearedLoanStatusId], null, false);


allLoansList = allDisbursedLoansList;

allLoansList = allLoansList + allClearedLoansList;
//delegator.findByAnd("LoanApplication",  [partyId : lpartyId], null, false);
allLoansList.eachWithIndex { loanItem, index ->
	
	Long loanProductId = loanItem.loanProductId;
	//Get Loan Product Name
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanProductId], false);
	
	memberStatement =  new MemberStatement()
	memberStatement.name = loanProduct.name;
	memberStatement.code = loanProduct.code;
	memberStatement.itemTotal = BigDecimal.ZERO;
	
	loanTransaction = new MemberTransaction();
	loanTransaction.transactionDate = loanItem.disbursementDate;
	loanTransaction.transactionDescription = 'Loan Disbursed'
	loanTransaction.increaseDecrease = 'I'
	loanTransaction.transactionAmount = loanItem.loanAmt
	
	memberStatement.listOfTransactions.add(loanTransaction);
	
	if (loanItem.outstandingBalance != null){
		//Add Opening Balance
		loanTransaction = new MemberTransaction()
		loanTransaction.transactionDate = loanItem.createdStamp
		loanTransaction.transactionDescription = 'Total Repaid at Opening'
		loanTransaction.increaseDecrease = 'D'
		loanTransaction.transactionAmount = (loanItem.loanAmt - loanItem.outstandingBalance)
		memberStatement.listOfTransactions.add(loanTransaction);
	}
	
	//Get all interest and insurance charges
	allInterestInsuranceCharges = delegator.findByAnd("LoanExpectation",  [loanApplicationId : loanItem.loanApplicationId], null, false);
	allInterestInsuranceCharges.eachWithIndex { interestInsurance, anindex ->
		
		if ((interestInsurance.repaymentName.equals("INTEREST")) || (interestInsurance.repaymentName.equals("INSURANCE"))){
		loanTransaction = new MemberTransaction()
		loanTransaction.transactionDate = interestInsurance.dateAccrued
		
		if (interestInsurance.repaymentName.equals("INTEREST")){
			loanTransaction.transactionDescription = "Interest Charged"
		} else if (interestInsurance.repaymentName.equals("INSURANCE")){
			loanTransaction.transactionDescription = "Insurance Charged"
		}
		
		loanTransaction.increaseDecrease = 'I'
		loanTransaction.transactionAmount = interestInsurance.amountAccrued
		
		memberStatement.listOfTransactions.add(loanTransaction);
		}
		
	}
	
	//Add Loan Repayments
	allRepayments = delegator.findByAnd("LoanRepayment",  [loanApplicationId : loanItem.loanApplicationId], null, false);
	allRepayments.eachWithIndex { loanRepaymentItem, repaymentIndex ->
		
		//Add Insurance is insurance amount greater than ZERO
		if ((loanRepaymentItem.insuranceAmount != null) && (loanRepaymentItem.insuranceAmount.compareTo(BigDecimal.ZERO) == 1)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp
			
			
			loanTransaction.transactionDescription = "Insurance Paid"
			
			
			loanTransaction.increaseDecrease = 'D'
			loanTransaction.transactionAmount = loanRepaymentItem.insuranceAmount
			
			memberStatement.listOfTransactions.add(loanTransaction);
		}
		
		
		//Add Interest if interest amount is greater than ZERO
		if ((loanRepaymentItem.interestAmount != null) && (loanRepaymentItem.interestAmount.compareTo(BigDecimal.ZERO) == 1)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp
			
			
			loanTransaction.transactionDescription = "Interest Paid"
			
			
			loanTransaction.increaseDecrease = 'D'
			loanTransaction.transactionAmount = loanRepaymentItem.interestAmount
			
			memberStatement.listOfTransactions.add(loanTransaction);
		}

		
		//Add Principal if principal amount is greater than ZERO
		if ((loanRepaymentItem.principalAmount != null) && (loanRepaymentItem.principalAmount.compareTo(BigDecimal.ZERO) == 1)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp
			
			
			loanTransaction.transactionDescription = "Principal Paid"
			
			
			loanTransaction.increaseDecrease = 'D'
			loanTransaction.transactionAmount = loanRepaymentItem.principalAmount
			
			memberStatement.listOfTransactions.add(loanTransaction);
		}
		
		
	}
	
	memberStatement.listOfTransactions.sort{it.transactionDate};
	memberStatementList.add(memberStatement)
//	statementItem = delegator.makeValue("ExpectedPaymentSent",
//		null);
//	statementItem.loanNo = loanItem.loanNo;
//	statementItem.createdStamp = loanItem.disbursementDate;
//	statementItem.remitanceDescription = "Loan Disbursement";
//	statementItem.amount = loanItem.loanAmt;
//	statementItem.isReceived = 'N';
//	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
//	statementItem.totalAmount = totalAmount;
//	
//	combinedList << statementItem
}
//Add Account Products
allAccountProducts = delegator.findByAnd("MemberAccount",  [partyId : lpartyId], null, false);
allAccountProducts.eachWithIndex { memberAccount, index ->
	
	
	//Get Account Product
	Long accountProductId = memberAccount.accountProductId;
	Long memberAccountId = memberAccount.memberAccountId.toLong();
	//Get Loan Product Name
	accountProduct = delegator.findOne("AccountProduct", [accountProductId : accountProductId], false);
	
	memberStatement =  new MemberStatement()
	memberStatement.name = accountProduct.name;
	memberStatement.code = accountProduct.code;
	memberStatement.itemTotal = BigDecimal.ZERO;
	
	//Add Opening Balance
	openingBalanceAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.calculateOpeningBalance(memberAccountId);
	
	
	
	memberAccountTransaction = new MemberTransaction()
	memberAccountTransaction.transactionDate = memberAccount.createdStamp
	memberAccountTransaction.transactionDescription = 'Opening Balance'
	memberAccountTransaction.increaseDecrease = 'I'
	memberAccountTransaction.transactionAmount = openingBalanceAmount
	memberStatement.listOfTransactions.add(memberAccountTransaction);
	
	//Add Account Transactions to each product
	
	allTransactions = delegator.findByAnd("AccountTransaction",  [memberAccountId : memberAccountId], null, false);
	allTransactions.eachWithIndex { theTransaction, anindex ->
		memberAccountTransaction = new MemberTransaction()
		memberAccountTransaction.transactionDate = theTransaction.createdStamp
		memberAccountTransaction.transactionDescription = theTransaction.transactionType
		memberAccountTransaction.increaseDecrease = theTransaction.increaseDecrease
		memberAccountTransaction.transactionAmount = theTransaction.transactionAmount
		
		memberStatement.listOfTransactions.add(memberAccountTransaction);
		
	}
	
	memberStatementList.add(memberStatement)
	
	
}

context.memberStatementList = memberStatementList;

