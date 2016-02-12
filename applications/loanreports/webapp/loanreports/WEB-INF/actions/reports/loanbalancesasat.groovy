import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;

import javolution.util.FastList;

partyId = parameters.partyId
stationId = parameters.stationId
loanProductId = parameters.loanProductId
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
//endDateTimestamp = new Timestamp(dateEndDate.getTime());
//dateEndDate.getTime()

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
defaultedLoanStatusId = 10030.toLong();
claimedLoanStatusId = 10013.toLong();
attachmentReversalStatusId = 10016.toLong();
deceasedId = 10018.toLong();

//6,10030,10013,7
exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
EntityFindOptions findOptions = new EntityFindOptions();

if ((partyId != null) && (partyId != "")){
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: disburseLoanStatusId)
	}
	myLoansList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: overpayedLoanStatusId)
	}
	overpayedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: overpayedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: defaultedLoanStatusId)
	}
	defaultedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	//delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: defaultedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: claimedLoanStatusId)
	}
	claimedLoansList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: attachmentReversalStatusId)
	}
	reversalList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(partyId: lpartyId)
		EQUALS(loanStatusId: deceasedId)
	}
	deceasedList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList =  myLoansList + defaultedList;
	myLoansList =  myLoansList + claimedLoansList;
	myLoansList =  myLoansList + reversalList;
	myLoansList =  myLoansList + deceasedList;
	
	
	
	
}

if ((loanProductId != null) && (loanProductId != "")){
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: disburseLoanStatusId)
	}
	
	
	myLoansList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: disburseLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: overpayedLoanStatusId)
	}
	overpayedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: overpayedLoanStatusId], null, false);

	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: defaultedLoanStatusId)
	}
	defaultedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: defaultedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: claimedLoanStatusId)
	}
	claimedLoanList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanProductId : lloanProductId, loanStatusId: claimedLoanStatusId], null, false);
	// = 10013.toLong();
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: attachmentReversalStatusId)
	}
	reversalList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanProductId: lloanProductId)
		EQUALS(loanStatusId: deceasedId)
	}
	deceasedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList =  myLoansList + defaultedList;
	myLoansList =  myLoansList + claimedLoanList;
	
	myLoansList =  myLoansList + reversalList;
	
	myLoansList =  myLoansList + deceasedList;
}

if ((stationId != null) && (stationId != "")){
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: disburseLoanStatusId)
	}
	myLoansList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: disburseLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: overpayedLoanStatusId)
	}
	overpayedList =  delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: overpayedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: defaultedLoanStatusId)
	}
	defaultedList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: defaultedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: claimedLoanStatusId)
	}
	claimedLoanList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [stationId : lstationId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: attachmentReversalStatusId)
	}
	reversalList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(stationId: lstationId)
		EQUALS(loanStatusId: deceasedId)
	}
	deceasedList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList =  myLoansList + defaultedList;
	myLoansList =  myLoansList + claimedLoanList;
	
	myLoansList =  myLoansList + reversalList;
	
	myLoansList =  myLoansList + deceasedList;
	
}



if ((branchId != null) && (branchId != "")){
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: disburseLoanStatusId)
	}
	myLoansList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: disburseLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: overpayedLoanStatusId)
	}
	overpayedList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: overpayedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: defaultedLoanStatusId)
	}
	defaultedList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: defaultedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: claimedLoanStatusId)
	}
	claimedLoanList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoansByStation",  [branchId : branchId, loanStatusId: claimedLoanStatusId], null, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: attachmentReversalStatusId)
	}
	reversalList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(branchId: branchId)
		EQUALS(loanStatusId: deceasedId)
	}
	deceasedList = delegator.findList("LoansByStation", expr, null, null, findOptions, false);
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList =  myLoansList + defaultedList;
	myLoansList =  myLoansList + claimedLoanList;
	
	myLoansList =  myLoansList + reversalList;
	myLoansList =  myLoansList + deceasedList;
	
}


if ((partyId == "") && (loanProductId == "") && (stationId == "") && (branchId == "")){
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: disburseLoanStatusId)
	}
	myLoansList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	//delegator.findByAnd("LoanApplication",  [loanStatusId: disburseLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: overpayedLoanStatusId)
	}
	overpayedList = delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanStatusId: overpayedLoanStatusId], null, false);
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: defaultedLoanStatusId)
	}
	defaultedList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanStatusId: defaultedLoanStatusId], null, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: claimedLoanStatusId)
	}
	claimedLoanList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	//delegator.findByAnd("LoanApplication",  [loanStatusId: claimedLoanStatusId], null, false);
	
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: attachmentReversalStatusId)
	}
	reversalList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	
	expr = exprBldr.AND() { //Timestamp
		LESS_THAN_EQUAL_TO(disbursementDate: endDateTimestamp)
		EQUALS(loanStatusId: deceasedId)
	}
	deceasedList =  delegator.findList("LoanApplication", expr, null, null, findOptions, false);
	
	
	myLoansList =  overpayedList + myLoansList;
	myLoansList =  myLoansList + defaultedList;
	myLoansList =  myLoansList + claimedLoanList;
	
	myLoansList =  myLoansList + reversalList;
	
	myLoansList =  myLoansList + deceasedList;
	
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
	loanBalance = loan.loanAmt - org.ofbiz.loans.LoanServices.getLoansRepaidByLoanApplicationIdByDate(loan.loanApplicationId, endDateTimestamp);
	loanItem.loanBalance = loanBalance;
	
	loanItem.interestAccrued = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInterestByLoanDue(loan.loanApplicationId.toString(), endDateTimestamp);
	loanItem.insuranceAccrued = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInsurancByLoanDue(loan.loanApplicationId.toString(), endDateTimestamp);
	loanStatus = delegator.findOne("LoanStatus", [loanStatusId : loan.loanStatusId], false);
	loanItem.loanStatus = loanStatus.name;
	loanProduct = delegator.findOne("LoanProduct", [loanProductId : loan.loanProductId], false);
	loanItem.productname = loanProduct.name;
	loanItem.loanAmt = loan.loanAmt;
	
	loanItem.interestRatePM = loan.interestRatePM;
	loanItem.maxRepaymentPeriod = loan.maxRepaymentPeriod;
	loanItem.repaymentPeriod = loan.repaymentPeriod;
	
	
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

