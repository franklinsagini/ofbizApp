import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

month = parameters.month
stationId = parameters.stationId

//Get Station Number
station = delegator.findOne("Station", [stationId : stationId], false);

//Station Number
stationNumber = station.stationNumber

class MemberExpecation{
	def payrollNumber
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
}

def varianceList = [];
def expectedReceived

def member
//Get Expectation Sent for this stationNumber
expectationList = delegator.findByAnd("ExpectationSentSummary",  [stationNumber : stationNumber, month: month], null, false);


//Buid a variance list

context.stationName = station.name;
context.month = month

expectationList.eachWithIndex { expectItem, index ->
	
	expectedReceived = new ExpectReceive();
	expectedReceived.payrollNo = expectItem.payrollNo
	
	//Get Member
	payrollNo = expectedReceived.payrollNo;
	memberList = delegator.findByAnd("Member",  [payrollNumber : payrollNo], null, false);
	
	memberList.eachWithIndex { memberItem, memberIndex ->
		member = memberItem;
	}
	expectedReceived.memberNo =  member.memberNumber;
	expectedReceived.employeeNo =  member.employeeNumber;
	expectedReceived.mobileNumber = member.mobileNumber;
	expectedReceived.name =  member.firstName+" "+member.middleName+" "+member.lastName;
	
	//Get Member Status
	memberStatusId = member.memberStatusId
	memberStatus = delegator.findOne("MemberStatus", [memberStatusId : memberStatusId], false);
	
	expectedReceived.status = memberStatus.name

	memberTerms = delegator.findOne("EmploymentType", [employmentTypeId : member.employmentTypeId], false);
	
	
	expectedReceived.termsOfService = memberTerms.name
	
	expectedReceived.expected = expectItem.amount
	
	receivedTotal = new BigDecimal(0.0);
	receivedList = delegator.findByAnd("ExpectationReceivedSummary",  [stationNumber : stationNumber, month: month, payrollNo: expectedReceived.payrollNo], null, false);
	receivedList.eachWithIndex { receivedItem, receivedItemIndex ->
		receivedTotal = receivedTotal + receivedItem.amount;
	}
	
	expectedReceived.received = receivedTotal
	
	expectedReceived.variance = expectedReceived.expected - expectedReceived.received;
	
	varianceList << expectedReceived
}

context.varianceList = varianceList;