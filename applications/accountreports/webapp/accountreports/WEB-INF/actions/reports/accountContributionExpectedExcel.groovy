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


startDate = parameters.startDate
endDate = parameters.endDate

stationId = parameters.stationId
employmentTypeId = parameters.employmentTypeId
branchId = parameters.branchId
accountProductId = parameters.accountProductId

if ((accountProductId) && (accountProductId != null)){
	accountProductIdLong = accountProductId.toLong()
}



if ((stationId) && (stationId != null)){
	stationIdLong = stationId.toLong()
}



if ((employmentTypeId) && (employmentTypeId != null)){
	employmentTypeIdLong = employmentTypeId.toLong()
}

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

print "formatted Date"
//println dateStartDate
//println dateEndDate


println "RRRRRRRRRRRRRR EAL DATES !!!!!!!!!!!!!"
println startDate
println endDate



exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()
//(startDate == null) || (endDate == null) ||
if (!(sqlEndDate)){
	expr = null;
}
// if (memberStatusId)
else if ((!accountProductId)){
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
	
	expr = null;
//	 = exprBldr.AND() {
//		GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//		LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
//	}
}
 else{
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
	if (accountProductId){
		
		if (branchId){
		expr = exprBldr.AND() {
//			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			EQUALS(accountProductId: accountProductIdLong)
			EQUALS(branchId: branchId)
			
		}
		} else{
		
		expr = exprBldr.AND() {
//			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			EQUALS(accountProductId: accountProductIdLong)
			
		}
		
		}
		
		//for station
		if (stationId){
			
			station = delegator.findOne("Station", [stationId : stationId.toString()], false);
			
			expr = exprBldr.AND() {
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				EQUALS(accountProductId: accountProductIdLong)
				EQUALS(employerCode: station.employerCode)
				
			}
			}
		
		if ((employmentTypeId) && (branchId)){
			expr = exprBldr.AND() {
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				EQUALS(accountProductId: accountProductIdLong)
				EQUALS(branchId: branchId)
				EQUALS(employmentTypeId: employmentTypeIdLong)
				
			}
		}
		
		
		if ((employmentTypeId) && (!branchId)){
			expr = exprBldr.AND() {
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				EQUALS(accountProductId: accountProductIdLong)
				EQUALS(employmentTypeId: employmentTypeIdLong)
				
			}
		}
		
		
		if ((employmentTypeId) && (stationId)){
			station = delegator.findOne("Station", [stationId : stationId.toString()], false);
			expr = exprBldr.AND() {
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)

				EQUALS(accountProductId: accountProductIdLong)
				EQUALS(employmentTypeId: employmentTypeIdLong)
				EQUALS(employerCode: station.employerCode)
			}
		}
		
		if ((employmentTypeId) && (!stationId) && (!branchId)){
			expr = exprBldr.AND() {
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)

								EQUALS(accountProductId: accountProductIdLong)
				EQUALS(employmentTypeId: employmentTypeIdLong)
			}
		}
		
		
		if ((!employmentTypeId) && (stationId) && (!branchId)){
			station = delegator.findOne("Station", [stationId : stationId.toString()], false);
			expr = exprBldr.AND() {
				
//				GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				
				EQUALS(accountProductId: accountProductIdLong)
				EQUALS(employerCode: station.employerCode)
			}
		}
		
		

	} else{
	startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());
		expr = null;
		
		// = exprBldr.AND() {
//			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
//			LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
//		}
	}


}

EntityFindOptions findOptions = new EntityFindOptions();
//findOptions.setMaxRows(100);
myAccountsList = delegator.findList("AccountContributionAmountsExpected", expr, null, null, findOptions, false)

//

//myLoansList = delegator.findByAnd("LoanApplication",  [partyId : lpartyId, loanStatusId: disburseLoanStatusId], null, false);


def combinedList = [];
def loanItem;


//def loanBalance = BigDecimal.ZERO;
//myLoansList.eachWithIndex { loan, index ->
//
//	loanItem = delegator.makeValue("LoanReportItem",
//		null);
//	loanItem.loanNo = loan.loanNo;
//
//	member = delegator.findOne("Member", [partyId : loan.partyId], false);
//	loanItem.names = member.firstName+" "+member.middleName+" "+member.lastName;
//
//	loanItem.payrollNumber = member.payrollNumber.trim();
//	loanItem.memberNumber = member.memberNumber.trim();
//	loanItem.idNumber = member.idNumber.trim();
//	loanItem.disbursementDate = loan.disbursementDate;
//	loanBalance = loan.loanAmt - org.ofbiz.loans.LoanServices.getLoansRepaidByLoanApplicationId(loan.loanApplicationId);
//	loanItem.loanBalance = loanBalance;
//
//	loanItem.interestAccrued = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInterestByLoanDue(loan.loanApplicationId.toString());
//	loanItem.insuranceAccrued = org.ofbiz.accountholdertransactions.LoanRepayments.getTotalInsurancByLoanDue(loan.loanApplicationId.toString());
//	loanStatus = delegator.findOne("LoanStatus", [loanStatusId : loan.loanStatusId], false);
//	loanItem.loanStatus = loanStatus.name;
//	loanProduct = delegator.findOne("LoanProduct", [accountProductId : loan.loanProductId], false);
//	loanItem.productname = loanProduct.name;
//	loanItem.loanAmt = loan.loanAmt;
//
//	loanItem.interestRatePM = loan.interestRatePM;
//	loanItem.maxRepaymentPeriod = loan.maxRepaymentPeriod;
//	loanItem.repaymentPeriod = loan.repaymentPeriod;
//
//
//	/*** statementItem.remitanceDescription = "Loan Disbursement";
//	statementItem.amount = loanItem.loanAmt;
//	statementItem.isReceived = 'N';
//	totalAmount = totalAmount + statementItem.amount.toBigDecimal();
//	statementItem.totalAmount = totalAmount; ***/
//
//	stationId = member.stationId;
//	station = delegator.findOne("Station", [stationId : stationId.toString()], false);
//
//	loanItem.stationName = station.name;
//
//	combinedList << loanItem
//}

//Build a list and add 
//expectedAmount, Variance, phone number
def mylistExpectedContributions = [];

//Build the my expected contribution list

startDateTimestamp = new Timestamp(sqlStartDate.getTime());
endDateTimestamp = new Timestamp(sqlEndDate.getTime());

myAccountsList.eachWithIndex { account, index ->
	partyId = account.partyId;
	partyIdLong = partyId.toLong();
	minimumExpectedAmount = org.ofbiz.accountholdertransactions.RemittanceServices.getMinimumExpectedContributingAmount(partyIdLong);
	//transactionAmount
	transactionAmount = org.ofbiz.accountholdertransactions.AccHolderTransactionServices.getTotalDepositsExcludeReversed("901", partyIdLong, startDateTimestamp, endDateTimestamp);
	variance = transactionAmount - minimumExpectedAmount;
	
	anAccount = [firstName:account.firstName, middleName:account.middleName, lastName:account.lastName, payrollNumber:account.payrollNumber, mobileNumber:account.mobileNumber,  memberNumber:account.memberNumber, idNumber:account.idNumber, stationId:account.stationId, branchId:account.branchId, memberStatusId:account.memberStatusId, employmentTypeId:account.employmentTypeId, accountNo:account.accountNo, accountName:account.accountName, transactionAmount:transactionAmount, minimumExpectedAmount:minimumExpectedAmount, variance:variance ]
	mylistExpectedContributions << anAccount
}


context.combinedList = mylistExpectedContributions


/** context.partyId = partyId **/

