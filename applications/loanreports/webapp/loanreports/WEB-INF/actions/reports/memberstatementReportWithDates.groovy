import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

partyId = parameters.partyId


//Account Product
accountProductId = parameters.accountProductId
loanApplicationId = parameters.loanApplicationId

startDate = parameters.startDate
endDate = parameters.endDate

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);

	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}


lpartyId = partyId.toLong();

lmemberAccountId = null;

if((accountProductId != null) && (!accountProductId.equals(""))){
	laccountProductId = accountProductId.toLong();
	lmemberAccountId = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberAccountIdFromMemberAccount(lpartyId, laccountProductId);
}

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;


class MemberStatement{
	def name
	def code
	def loanNo
	def accountCode
	def accountName
	def itemTotal
	def availableBalace
	def listOfTransactions = []
}

class MemberTransaction{
	def transactionDate
	def transactionDescription
	def increaseDecrease
	def transactionAmount
	def isLoan
	def isLoanTransaction
	def repaymentMode
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
accountTransactionList = null;
if ((accountProductId == null) || (accountProductId.equals(""))){
	accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId], null, false);
} else{
	//Get memberAccountId given accountProductId and partyId
	//Get the transactions for this memberAccountId

	println " Member Account ---  "+lmemberAccountId;
	accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId, memberAccountId : lmemberAccountId], null, false);
}

accountTransactionList.eachWithIndex { accountItem, index ->

	if (accountItem.lmemberAccountId == lmemberAccountId){


		println " Member Account in item ---  "+accountItem.memberAccoundId;
		println " Member Account sent ---  "+lmemberAccountId;

		statementItem = delegator.makeValue("ExpectedPaymentSent",
				null);
		statementItem.loanNo = "";
		statementItem.createdStamp = accountItem.createdStamp;

		if (accountItem.transactionType == 'CASHDEPOSIT')
		{
			statementItem.remitanceDescription = 'Cash Deposit';
		} else if (accountItem.transactionType == 'CASHWITHDRAWAL'){
			statementItem.remitanceDescription = 'Cash Withdrawal';
		} 
		
		else if (accountItem.transactionType.equals("TOOTHERACCOUNTS")){
			statementItem.remitanceDescription = 'Transfer to other account (SALARY)';
		}
		
		else if (accountItem.transactionType.equals("LOANREPAYMENT") ){
			statementItem.remitanceDescription = 'Repayment of a loan';
		}
		
		//LOANDISBURSEMENT
		else if (accountItem.transactionType.equals("LOANDISBURSEMENT") ){
			if (accountItem.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : accountItem.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			statementItem.remitanceDescription = loanProduct.name+' loan Disbursement ('+loanApplication.loanNo+')';
			} else{
				statementItem.remitanceDescription = ' Loan Disbursement ';
			}
		}
		
		
		else if (accountItem.transactionType.equals("LOANCLEARANCE") ){
			if (accountItem.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : accountItem.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			statementItem.remitanceDescription = loanProduct.name+' loan Clearance ('+loanApplication.loanNo+')';
			} else{
				statementItem.remitanceDescription = ' Loan Clearance ';
			}
		}
		
		else if (accountItem.transactionType.equals("LOANCLEARANCECHARGES") ){
			if (accountItem.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : accountItem.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			statementItem.remitanceDescription = loanProduct.name+' loan Clearance Charges ('+loanApplication.loanNo+')';
			} else{
				statementItem.remitanceDescription = ' Loan Clearance Charges ';
			}
		}
		
		
		else{
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
}

loansList.eachWithIndex { loanItem, index ->

	statementItem = delegator.makeValue("ExpectedPaymentSent",
			null);
	statementItem.loanNo = loanItem.loanNo;
	statementItem.createdStamp = loanItem.disbursementDate;
	statementItem.remitanceDescription = 'Loan Disbursement';
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
defaultedLoanStatusId = 10030.toLong();

allDisbursedLoansList = null;
allClearedLoansList = null;
allDefaultedLoansList = null;
allLoansList = null;
//loanApplicationId
if (((accountProductId == null) || (accountProductId.equals(""))) && ((loanApplicationId == null) || (loanApplicationId.equals("")))){
	allDisbursedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: theDisburseLoanStatusId], null, false);
	allClearedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: theClearedLoanStatusId], null, false);
	allDefaultedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: defaultedLoanStatusId], null, false);
	
	allLoansList = allDisbursedLoansList;

	allLoansList = allLoansList + allClearedLoansList + allDefaultedLoansList;
}

if ((loanApplicationId != null) && (!loanApplicationId.equals(""))){
	loanApplicationIdLong = loanApplicationId.toLong();

	allDisbursedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId,  loanApplicationId: loanApplicationIdLong], null, false);
	//allClearedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanApplicationId: loanApplicationIdLong], null, false);
	allDefaultedLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: defaultedLoanStatusId], null, false);
	
	allLoansList = allDisbursedLoansList + allDefaultedLoansList;

	//	allLoansList = allLoansList + allClearedLoansList;

}


//delegator.findByAnd("LoanApplication",  [partyId : lpartyId], null, false);
allLoansList.eachWithIndex { loanItem, index ->

	Long loanProductId = loanItem.loanProductId;
	//Get Loan Product Name
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanProductId], false);

	memberStatement =  new MemberStatement()
	
	memberStatement.itemTotal = BigDecimal.ZERO;

	loanTransaction = new MemberTransaction();
	loanTransaction.transactionDate = loanItem.disbursementDate;
	//loanTransaction.transactionDescription = 'Loan Disbursed'
	if (loanItem.parentLoanApplicationId == null){
		loanTransaction.transactionDescription = 'Loan Disbursed'
	} else{
		parentLoanApplicationId = loanItem.parentLoanApplicationId;
		parentLoanApplication = delegator.findOne("LoanApplication", [loanApplicationId : parentLoanApplicationId], false);
		loanTransaction.transactionDescription = 'Loan Attached from ( Loan No : '+parentLoanApplication.loanNo+") - "+loanProduct.name;
	}
	
	loanTransaction.increaseDecrease = 'D'
	loanTransaction.transactionAmount = loanItem.loanAmt
	loanTransaction.isLoan = true
	loanTransaction.isLoanTransaction = true
	
	
	memberStatement.name = "LOAN TYPE : "+loanProduct.name;
	memberStatement.code = "LOAN CODE : "+loanProduct.code;
	memberStatement.loanNo = "LOAN NO :  "+loanItem.loanNo;

	memberStatement.listOfTransactions.add(loanTransaction);

	if (loanItem.outstandingBalance != null){
		//Add Opening Balance
		loanTransaction = new MemberTransaction()
		loanTransaction.transactionDate = loanItem.createdStamp
		loanTransaction.transactionDescription = 'Total Repaid at Opening'
		loanTransaction.increaseDecrease = 'I'
		loanTransaction.transactionAmount = (loanItem.loanAmt - loanItem.outstandingBalance)
		//loanTransaction.isLoan = true
		loanTransaction.isLoanTransaction = true
		memberStatement.listOfTransactions.add(loanTransaction);
		
	}
	
	
	if ((loanItem.interestDue != null) && (loanItem.interestDue != BigDecimal.ZERO)){
		//Add Opening Balance
		loanTransaction = new MemberTransaction()
		loanTransaction.transactionDate = loanItem.createdStamp
		loanTransaction.transactionDescription = 'Interest Due at Opening'
		loanTransaction.increaseDecrease = 'D'
		loanTransaction.transactionAmount = loanItem.interestDue
		//loanTransaction.isLoan = true
		loanTransaction.isLoanTransaction = true
		memberStatement.listOfTransactions.add(loanTransaction);
		
	}
	
	if ((loanItem.insuranceDue != null) && (loanItem.insuranceDue != BigDecimal.ZERO)){
		//Add Opening Balance
		loanTransaction = new MemberTransaction()
		loanTransaction.transactionDate = loanItem.createdStamp
		loanTransaction.transactionDescription = 'Insurance Due at Opening'
		loanTransaction.increaseDecrease = 'D'
		loanTransaction.transactionAmount = loanItem.insuranceDue
		//loanTransaction.isLoan = true
		loanTransaction.isLoanTransaction = true
		memberStatement.listOfTransactions.add(loanTransaction);
		
	}
	

	//Get all interest and insurance charges
	allInterestInsuranceCharges = delegator.findByAnd("LoanExpectation",  [loanApplicationId : loanItem.loanApplicationId], null, false);
	allInterestInsuranceCharges.eachWithIndex { interestInsurance, anindex ->

		if ((interestInsurance.repaymentName.equals("INTEREST")) || (interestInsurance.repaymentName.equals("INSURANCE"))){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = interestInsurance.dateAccrued

			if (interestInsurance.repaymentName.equals("INTEREST")){
				
				if (interestInsurance.acctgTransId != null){
					
					if (interestInsurance.stationMonthInterestManagementId != null){
						stationMonthInterestManagement = delegator.findOne("StationMonthInterestManagement", [stationMonthInterestManagementId : interestInsurance.stationMonthInterestManagementId], false);
						loanTransaction.transactionDescription = "Interest Charged ("+interestInsurance.acctgTransId+") for "+stationMonthInterestManagement.month+"/"+stationMonthInterestManagement.year;
					} else{
						loanTransaction.transactionDescription = "Interest Charged ("+interestInsurance.acctgTransId+")";
					}
					
					
					
				}else{
					loanTransaction.transactionDescription = "Interest Charged";
				}
				
				
			} else if (interestInsurance.repaymentName.equals("INSURANCE")){
				if (interestInsurance.acctgTransId != null){
					
					if (interestInsurance.stationMonthInterestManagementId != null){
						stationMonthInterestManagement = delegator.findOne("StationMonthInterestManagement", [stationMonthInterestManagementId : interestInsurance.stationMonthInterestManagementId], false);
						loanTransaction.transactionDescription = "Insurance Charged ("+interestInsurance.acctgTransId+") for "+stationMonthInterestManagement.month+"/"+stationMonthInterestManagement.year;
					} else{
						loanTransaction.transactionDescription = "Insurance Charged ("+interestInsurance.acctgTransId+")";
					}
					
					
					
				}else{
					loanTransaction.transactionDescription = "Insurance Charged";
				}
				
			}

			loanTransaction.increaseDecrease = 'D'
			
			
			
			loanTransaction.transactionAmount = interestInsurance.amountAccrued
			
			if (interestInsurance.amountAccrued < 0)
			{
				loanTransaction.increaseDecrease = 'I'
				loanTransaction.transactionAmount = loanTransaction.transactionAmount * -1;
				loanTransaction.transactionDescription = loanTransaction.transactionDescription+' (JV) '
			}
			//loanTransaction.isLoan = true
			loanTransaction.isLoanTransaction = true

			memberStatement.listOfTransactions.add(loanTransaction);
		}

	}

	//Add Loan Repayments
	allRepayments = delegator.findByAnd("LoanRepayment",  [loanApplicationId : loanItem.loanApplicationId], null, false);
	allRepayments.eachWithIndex { loanRepaymentItem, repaymentIndex ->

		//Add Insurance is insurance amount greater than ZERO
		if ((loanRepaymentItem.insuranceAmount != null) && (loanRepaymentItem.insuranceAmount.compareTo(BigDecimal.ZERO) != 0)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp

			if (loanRepaymentItem.acctgTransId != null){
			loanTransaction.transactionDescription = "Insurance Paid ("+loanRepaymentItem.acctgTransId+")";
			} else{
				loanTransaction.transactionDescription = "Insurance Paid ";
			}

			
			if (loanRepaymentItem.reverseStatus.equals("Y")){
				loanTransaction.transactionDescription = "Insurance Payment Reversed ("+loanRepaymentItem.acctgTransId+")";
			}

			loanTransaction.increaseDecrease = 'I'
			loanTransaction.transactionAmount = loanRepaymentItem.insuranceAmount
			
			if (loanRepaymentItem.insuranceAmount < 0)
			{
				loanTransaction.increaseDecrease = 'D'
				loanTransaction.transactionAmount = loanTransaction.transactionAmount * -1;
				loanTransaction.transactionDescription = loanTransaction.transactionDescription+' (JV) '
			}
			//loanTransaction.isLoan = true
			loanTransaction.isLoanTransaction = true
			loanTransaction.repaymentMode  = loanRepaymentItem.repaymentMode
			memberStatement.listOfTransactions.add(loanTransaction);
		}


		//Add Interest if interest amount is greater than ZERO
		if ((loanRepaymentItem.interestAmount != null) && (loanRepaymentItem.interestAmount.compareTo(BigDecimal.ZERO) != 0)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp


			
			
			if (loanRepaymentItem.acctgTransId != null){
				loanTransaction.transactionDescription = "Interest Paid ("+loanRepaymentItem.acctgTransId+")";
				} else{
					loanTransaction.transactionDescription = "Interest Paid"
				}
				
				if (loanRepaymentItem.reverseStatus.equals("Y")){
					loanTransaction.transactionDescription = "Interest Payment Reversed ("+loanRepaymentItem.acctgTransId+")";
				}


			loanTransaction.increaseDecrease = 'I'
			loanTransaction.transactionAmount = loanRepaymentItem.interestAmount
			
			if (loanRepaymentItem.interestAmount < 0)
			{
				loanTransaction.increaseDecrease = 'D'
				loanTransaction.transactionAmount = loanTransaction.transactionAmount * -1;
				loanTransaction.transactionDescription = loanTransaction.transactionDescription+' (JV) '
			}
			//loanTransaction.isLoan = true
			loanTransaction.isLoanTransaction = true
			loanTransaction.repaymentMode  = loanRepaymentItem.repaymentMode
			memberStatement.listOfTransactions.add(loanTransaction);
		}


		//Add Principal if principal amount is greater than ZERO
		if ((loanRepaymentItem.principalAmount != null) && (loanRepaymentItem.principalAmount.compareTo(BigDecimal.ZERO) != 0)){
			loanTransaction = new MemberTransaction()
			loanTransaction.transactionDate = loanRepaymentItem.createdStamp


			

			if (loanRepaymentItem.acctgTransId != null){
				loanTransaction.transactionDescription = "Principal Paid ("+loanRepaymentItem.acctgTransId+")";
			} else{
					loanTransaction.transactionDescription = "Principal Paid"
			}
			
			if (loanRepaymentItem.reverseStatus.equals("Y")){
				loanTransaction.transactionDescription = "Principal Payment Reversed ("+loanRepaymentItem.acctgTransId+")";
			} 

			loanTransaction.increaseDecrease = 'I'
			loanTransaction.transactionAmount = loanRepaymentItem.principalAmount
			
			if (loanRepaymentItem.principalAmount < 0)
			{
				loanTransaction.increaseDecrease = 'D'
				loanTransaction.transactionAmount = loanTransaction.transactionAmount * -1;
				
				loanTransaction.transactionDescription = loanTransaction.transactionDescription+' (JV) '
			}
			//loanTransaction.isLoan = true
			loanTransaction.isLoanTransaction = true
			loanTransaction.repaymentMode  = loanRepaymentItem.repaymentMode
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
allAccountProducts = null;
if ((loanApplicationId == null) || (loanApplicationId.equals(""))){
	if ((accountProductId == null) || (accountProductId.equals(""))){
		//accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId], null, false);
		allAccountProducts = delegator.findByAnd("MemberAccount",  [partyId : lpartyId], null, false);

	} else{
		//Get memberAccountId given accountProductId and partyId
		//Get the transactions for this memberAccountId

		println " Member Account last ---  "+lmemberAccountId;
		//accountTransactionList = delegator.findByAnd("AccountTransaction",  [partyId : lpartyId, memberAccountId : memberAccountId], null, false);
			allAccountProducts = delegator.findByAnd("MemberAccount",  [memberAccountId : lmemberAccountId], null, false);
	}
}

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
	String memberAccountIdStr = memberAccount.memberAccountId;
	memberStatement.availableBalace = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getAvailableBalanceVer3(memberAccountIdStr);

	//Add Opening Balance
	memberAccountTransaction = new MemberTransaction()
	
	if ((sqlStartDate == null) && (sqlEndDate == null)){
		openingBalanceAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.calculateOpeningBalance(memberAccountId);
		memberAccountTransaction.transactionDate = memberAccount.createdStamp;
	} else{
		startDateTimestamp = new Timestamp(sqlStartDate.getTime());
		openingBalanceAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getBookBalanceVer3(memberAccountId.toString(), startDateTimestamp);
		memberAccountTransaction.transactionDate = startDateTimestamp
	}

	
	memberAccountTransaction.transactionDescription = 'Opening Balance'
	memberAccountTransaction.increaseDecrease = 'I'
	memberAccountTransaction.transactionAmount = openingBalanceAmount
	memberStatement.listOfTransactions.add(memberAccountTransaction);

	//Add Account Transactions to each product

	allTransactions = null;
	if ((sqlStartDate == null) && (sqlEndDate == null)){
		allTransactions = delegator.findByAnd("AccountTransaction",  [memberAccountId : memberAccountId], null, false);
	} else {
		//Filter by start and end date
		exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
		
		startDateTimestamp = new Timestamp(sqlStartDate.getTime());
		endDateTimestamp = new Timestamp(sqlEndDate.getTime());
		
		expr = exprBldr.AND() { //Timestamp
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			EQUALS(memberAccountId: memberAccountId)
		}
		
		//allTransactions = delegator.findByAnd("AccountTransaction",  expr, null, false);
		
		//membersList = delegator.findList("Member", expr, null, ["joinDate ASC"], findOptions, false)
		EntityFindOptions findOptions = new EntityFindOptions();
		allTransactions = delegator.findList("AccountTransaction", expr, null, null, findOptions, false)
	}
	
	allTransactions.eachWithIndex { theTransaction, anindex ->
		memberAccountTransaction = new MemberTransaction()
		memberAccountTransaction.transactionDate = theTransaction.createdStamp
		//println 'transaction is ####'
		//println(theTransaction.transactionType) ;
		if (theTransaction.transactionType.equals("CASHDEPOSIT"))
		{
			memberAccountTransaction.transactionDescription = 'Cash Deposit ('+theTransaction.acctgTransId+')';
		} else if (theTransaction.transactionType.equals("CASHWITHDRAWAL")){
			memberAccountTransaction.transactionDescription = 'Cash Withdrawal ('+theTransaction.acctgTransId+')';
		}
		
		else if (theTransaction.transactionType.equals("TOOTHERACCOUNTS")){
			memberAccountTransaction.transactionDescription = 'Transfer to other account (SALARY) ('+theTransaction.acctgTransId+')';
		}
		
		else if (theTransaction.transactionType.equals("LOANREPAYMENT") ){
			if (theTransaction.loanApplicationId != null){
				loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : theTransaction.loanApplicationId], false);
				loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
				memberAccountTransaction.transactionDescription = loanProduct.name+' Repayment ('+loanApplication.loanNo+') ('+theTransaction.acctgTransId+')';
				} else{
					memberAccountTransaction.transactionDescription = 'Repayment of a loan ('+theTransaction.acctgTransId+')';
				}
	
			
			
			
			
		}
		
		else if (theTransaction.transactionType.equals("WITHDRAWALCOMMISSION") ){
			memberAccountTransaction.transactionDescription = 'Withdrawal Commission';
		}
		
		else if (theTransaction.transactionType.equals("ATMWITHDRAWAL") ){
			memberAccountTransaction.transactionDescription = 'ATM Withdrawal ('+theTransaction.acctgTransId+')';
		}

		else if (theTransaction.transactionType.equals("MSACCOWITHDRAWAL") ){
			memberAccountTransaction.transactionDescription = 'MSACCO Withdrawal ('+theTransaction.acctgTransId+')';
		}

		else if (theTransaction.transactionType.equals("MSACCOWITHDRAWALREV") ){
			memberAccountTransaction.transactionDescription = 'MSACCO Withdrawal Reversal ('+theTransaction.acctgTransId+')';
		}

		else if (theTransaction.transactionType.equals("ATMWITHDRAWALREVERSAL") ){
			memberAccountTransaction.transactionDescription = 'ATM Withdrawal Reversal ('+theTransaction.acctgTransId+')';
		}

		else if (theTransaction.transactionType.equals("CHEQUEDEPOSIT") ){
			memberAccountTransaction.transactionDescription = 'Cheque Deposit ('+theTransaction.acctgTransId+')';
		}
		
		else if (theTransaction.transactionType.equals("EXCISEDUTY") ){
			memberAccountTransaction.transactionDescription = 'Excise Duty ('+theTransaction.acctgTransId+')';
		}
		
		else if (theTransaction.transactionType.equals("LOANDISBURSEMENT") ){
			if (theTransaction.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : theTransaction.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			memberAccountTransaction.transactionDescription = loanProduct.name+' loan Disbursement ('+loanApplication.loanNo+') ('+theTransaction.acctgTransId+')';
			} else{
				memberAccountTransaction.transactionDescription = ' Loan Disbursement ('+theTransaction.acctgTransId+')';
			}
		}
		
		
		else if (theTransaction.transactionType.equals("LOANCLEARANCE") ){
			if (theTransaction.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : theTransaction.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			memberAccountTransaction.transactionDescription = loanProduct.name+' loan Clearance ('+loanApplication.loanNo+') ('+theTransaction.acctgTransId+')';
			} else{
				memberAccountTransaction.transactionDescription = ' Loan Clearance ('+theTransaction.acctgTransId+')';
			}
		}
		
		else if (theTransaction.transactionType.equals("LOANCLEARANCECHARGES") ){
			if (theTransaction.loanApplicationId != null){
			loanApplication = delegator.findOne("LoanApplication", [loanApplicationId : theTransaction.loanApplicationId], false);
			loanProduct = delegator.findOne("LoanProduct", [loanProductId : loanApplication.loanProductId], false);
			memberAccountTransaction.transactionDescription = loanProduct.name+' loan Clearance Charges ('+loanApplication.loanNo+') ('+theTransaction.acctgTransId+')';
			} else{
				memberAccountTransaction.transactionDescription = ' Loan Clearance Charges ('+theTransaction.acctgTransId+')';
			}
		}
		
		
		else{
			memberAccountTransaction.transactionDescription = theTransaction.transactionType+'('+theTransaction.acctgTransId+')';
		}
		
		//memberAccountTransaction.transactionDescription = theTransaction.transactionType
		memberAccountTransaction.increaseDecrease = theTransaction.increaseDecrease
		memberAccountTransaction.transactionAmount = theTransaction.transactionAmount

		memberStatement.listOfTransactions.add(memberAccountTransaction);

	}

	memberStatementList.add(memberStatement)
}

context.memberStatementList = memberStatementList;