import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

month = parameters.month
stationId = parameters.stationId

//Get Station Number
station = delegator.findOne("Station", [stationId : stationId], false);

//Station Number
stationNumber = station.stationNumber
employerCode = station.employerCode

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
	def remitanceCode
	def loanNo
	def balance
	
	def expected
	def received
	def variance
}

def memberExpectationList = [];

def varianceList = [];
def expectedReceived

def member
//Get Expectation Sent for this stationNumber
expectationList = delegator.findByAnd("ExpectationSentSummary",  [employerCode : employerCode, month: month], null, false);


//Buid a variance list

context.stationName = station.name;
context.stationNumber = station.stationNumber;
context.month = month

expectationList.eachWithIndex { expectItem, index ->
	
	memberExpecationItem = new MemberExpecation();
	
	memberExpecationItem.payrollNumber = expectItem.payrollNo;
	
	//expectedReceived = new ExpectReceive();
	//expectedReceived.payrollNo = expectItem.payrollNo
	
	//Get Member
	payrollNo = expectItem.payrollNo;
	memberList = delegator.findByAnd("Member",  [payrollNumber : payrollNo], null, false);
	member = null;
	memberList.eachWithIndex { memberItem, memberIndex ->
		member = memberItem;
	}
	
	
	memberExpecationItem.payrollNumber = member.payrollNumber;
	memberExpecationItem.memberNumber =  member.memberNumber;
	memberExpecationItem.mobileNumber =  member.mobileNumber;
	//memberExpecationItem.mobileNumber = member.mobileNumber;
	memberExpecationItem.memberNames =  member.firstName+" "+member.middleName+" "+member.lastName;
	
	//Get Member Status
	memberStatusId = member.memberStatusId
	memberStatus = delegator.findOne("MemberStatus", [memberStatusId : memberStatusId], false);
	
	memberExpecationItem.status = memberStatus.name

	memberTerms = delegator.findOne("EmploymentType", [employmentTypeId : member.employmentTypeId], false);
	
	
	memberExpecationItem.termsOfService = memberTerms.name
	
	//Add Items to Member List
	sentDetailedList = delegator.findByAnd("ExpectedPaymentSent",  [employerCode : employerCode, month: month, payrollNo: expectItem.payrollNo], null, false);
	sentDetailedList.eachWithIndex { sentItem, sentItemIndex ->
		
		expectedReceived = new ExpectReceive();
		
		expectedReceived.expected = sentItem.amount
		expectedReceived.description = sentItem.remitanceDescription;
		expectedReceived.remitanceCode = sentItem.remitanceCode;
		expectedReceived.loanNo = sentItem.loanNo;
		
		//receivedTotal = receivedTotal + receivedItem.amount;
		
		
		receivedTotal = new BigDecimal(0.0);
		//receivedDetailedList = delegator.findByAnd("ExpectedPaymentReceived",  [employerCode : employerCode, month: month, payrollNo: sentItem.payrollNo, remitanceCode: sentItem.remitanceCode], null, false);
		//receivedDetailedList.eachWithIndex { receivedDetailedItem, receivedDetailedItemIndex ->
		//	receivedTotal = receivedTotal + receivedDetailedItem.amount;
		//}
		
		if (sentItem.loanNo.equalsIgnoreCase("0")){
			//code is productcode
			receivedTotal = org.ofbiz.accountholdertransactions.LoanRepayments.getRepaidAmount(sentItem.remitanceCode, sentItem.month, "ACCOUNT", sentItem.payrollNo,  sentItem.loanNo);
		} else{
			//code is loanNo
			receivedTotal = org.ofbiz.accountholdertransactions.LoanRepayments.getRepaidAmount(sentItem.remitanceCode, sentItem.month, "LOAN", sentItem.payrollNo, sentItem.loanNo);;
		}
		
		
		expectedReceived.received = receivedTotal
		
		expectedReceived.variance = expectedReceived.expected - expectedReceived.received;
		
		memberExpecationItem.listOfExpectReceive.add(expectedReceived);
		//varianceList << expectedReceived
	}
	
	//Adding items in received but missing in expected sent
	//delegator
	//delegator.findB
	memberExpectationList << memberExpecationItem;
	
	
}

context.memberExpectationList = memberExpectationList;