import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

stationId = parameters.stationId

//Get Station Number
station = null;
globalEmployerCode = null;

if ((stationId != null) && (!stationId.equals(""))){
	station = delegator.findOne("Station", [stationId : stationId], false);
	globalEmployerCode = station.employerCode
	
	//Get Employers with defaulters
	employerList = delegator.findByAnd("DisbursedLoansStationEmployer",  [employerCode : globalEmployerCode], null, false);
	
} else{
	employerList = delegator.findByAnd("DisbursedLoansStationEmployer",  null, null, false);

}
//Station Number
//stationNumber = station.stationNumber

//employerCode = station.employerCode
//def defaultLoanStatusId = org.ofbiz.accountholdertransactions.LoanUtilities.getLoanStatusId('DEFAULTED');
//defaultLoanStatusId = defaultLoanStatusId.toLong();
class StationDefaultedLoan{
	def employerCode
	def employerName
	def loanCount
	def originalAmountTotal
	def loanBalanceTotal
	def memberDepositTotal
	
	def listOfDefaultedLoans = []
}

class DefaultedLoan {
	def loanNo
	def loanType
	def loanAmt
	def loanBalance
	def disbursementDate
	def lastPaid
	def payrollNo
	def name
	def memberStatus
	def timeDifference
	def shareAmount
	def termsOfService
}

/***
class MemberExpecation{
	def payrollNumber
	def memberNumber
	def mobileNumber
	def memberNames;
	def status
	def termsOfService
	
	def listOfExpectReceive = []
}


class ExpectReceive{
	def payrollNumber
	def name
	def status
	def termsOfService
	def description
	def balance
	
	def expected
	def received
	def variance
} **/

def listStationDefaulted = [];

//def member



//Buid a variance list

//context.stationName = station.name;
//context.stationNumber = station.stationNumber;
//context.month = month

employerList.eachWithIndex { stationEmployerItem, index ->
	
	stationDefaultedLoan = new StationDefaultedLoan();
	
	stationDefaultedLoan.employerCode = stationEmployerItem.employerCode;
	employerCode = stationEmployerItem.employerCode;
	stationDefaultedLoan.employerName = org.ofbiz.accountholdertransactions.LoanUtilities.getStationName(employerCode);
	stationDefaultedLoan.loanCount = stationEmployerItem.loanCount;
	stationDefaultedLoan.originalAmountTotal = stationEmployerItem.loanAmt;
	stationDefaultedLoan.loanBalanceTotal = 0;
	stationDefaultedLoan.memberDepositTotal = 0;
	
	//Get Defaulted Loans for the Employer and add them to the list
	employerCode = stationEmployerItem.employerCode;
	defaultedList = delegator.findByAnd("DefaultedUnattchedLoans",  [employerCode : employerCode], null, false);
		
	defaultedList.eachWithIndex { defaultedItem, defaultedItemIndex ->
		
		defaultedLoan = new DefaultedLoan();
		
		defaultedLoan.loanNo = defaultedItem.loanNo;
		defaultedLoan.loanType = org.ofbiz.accountholdertransactions.LoanUtilities.getLoanProductNameGivenLoanNo(defaultedItem.loanNo);
		defaultedLoan.loanAmt = defaultedItem.loanAmt
		
		loanBalance = org.ofbiz.loansprocessing.LoansProcessingServices.getTotalLoanBalancesByLoanApplicationId(defaultedItem.loanApplicationId);
		defaultedLoan.loanBalance = loanBalance
		
		defaultedLoan.disbursementDate = defaultedItem.disbursementDate
		defaultedLoan.lastPaid = org.ofbiz.accountholdertransactions.LoanUtilities.getLoanLastPaymentDate(defaultedItem.loanApplicationId, defaultedItem.lastRepaymentDate);
		
		//defaultedItem.lastRepaymentDate
		defaultedLoan.payrollNo = defaultedItem.payrollNumber
		
		memberName = defaultedItem.firstName + ' '+defaultedItem.middleName+' '+defaultedItem.lastName;
		defaultedLoan.name = memberName
		
		
		defaultedLoan.memberStatus = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberStatusGivenPayrollNo(defaultedItem.payrollNumber);
		
		timeDefaulted = org.ofbiz.accountholdertransactions.LoanUtilities.loanDefaultTimeDiff(defaultedItem.loanNo);
		defaultedLoan.timeDifference = timeDefaulted;
		
		defaultedLoan.shareAmount = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberDepositsBalance(defaultedItem.payrollNumber);
		defaultedLoan.termsOfService = org.ofbiz.accountholdertransactions.LoanUtilities.getMemberEmploymentTypeGivenPayrollNo(defaultedItem.payrollNumber);
		
		
		//defaultedItem.employmentTypeName

		
		//receivedTotal = receivedTotal + receivedItem.amount;
		
		//receivedTotal = new BigDecimal(0.0);
		//receivedDetailedList = delegator.findByAnd("ExpectedPaymentReceived",  [employerCode : employerCode, month: month, payrollNo: sentItem.payrollNo, remitanceCode: sentItem.remitanceCode], null, false);
		//receivedDetailedList.eachWithIndex { receivedDetailedItem, receivedDetailedItemIndex ->
		//	receivedTotal = receivedTotal + receivedDetailedItem.amount;
		//}
		
		//expectedReceived.received = receivedTotal
		
		//expectedReceived.variance = expectedReceived.expected - expectedReceived.received;
		
		if ((timeDefaulted == null) || (timeDefaulted >= 3)){
			
			timeDefaulted = timeDefaulted - 3;
			defaultedLoan.timeDifference = timeDefaulted;
			stationDefaultedLoan.listOfDefaultedLoans.add(defaultedLoan);
		}
		//varianceList << expectedReceived
	}
	 
	listStationDefaulted << stationDefaultedLoan;
	
	
}

context.listStationDefaulted = listStationDefaulted;