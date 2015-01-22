import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId
stationId = parameters.stationId
loanProductId = parameters.loanProductId

def combinedList = [];
def loanItem;

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
if ((partyId != null) && (partyId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: overpayedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
}
if ((loanProductId != null) && (loanProductId != "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: overpayedLoanStatusId], null, false);
	myLoansList =  overpayedList + myLoansList;
}

if ((stationId != null) && (stationId != "")){
	myLoansList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: overpayedLoanStatusId], null, false);
	
	
	myLoansList =  overpayedList + myLoansList;
}


if ((partyId == "") && (loanProductId == "") && (stationId == "")){
	myLoansList = delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
	overpayedList = delegator.findByAnd("LoanApplication",  [loanStatusId: overpayedLoanStatusId], null, false);
	
	myLoansList =  overpayedList + myLoansList;
}

def loanBalance = BigDecimal.ZERO;
myLoansList.eachWithIndex { loan, index ->
	
	loanItem = delegator.makeValue("LoanReportItem",
		null);
	loanItem.loanNo = loan.loanNo;
	
	member = delegator.findOne("Member", [partyId : loan.partyId], false);
	loanItem.names = member.firstName+" "+member.middleName+" "+member.lastName;
	
	loanItem.payrollNumber = member.payrollNumber.trim();
	loanItem.memberNumber = member.memberNumber.trim();
	loanItem.idNumber = member.idNumber.trim();
	loanItem.disbursementDate = loan.disbursementDate;
	loanBalance = loan.loanAmt - org.ofbiz.loans.LoanServices.getLoansRepaidByLoanApplicationId(loan.loanApplicationId);
	loanItem.loanBalance = loanBalance;
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loan.loanProductId], false);
	loanItem.productname = loanProduct.name;
	loanItem.loanAmt = loan.loanAmt;
	
	
	/*** statementItem.remitanceDescription = "Loan Disbursement";
	statementItem.amount = loanItem.loanAmt;
	statementItem.isReceived = 'N';
	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
	statementItem.totalAmount = totalAmount; ***/
	
	stationId = member.stationId;
	station = delegator.findOne("Station", [stationId : stationId.toString()], false);
	
	loanItem.stationName = station.name;
	
	combinedList << loanItem
}

context.combinedList = combinedList
/** context.partyId = partyId **/

