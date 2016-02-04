import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

month = parameters.month
//stationId = parameters.stationId

//Get Station Number
//station = delegator.findOne("Station", [stationId : stationId], false);

//Station Number
//stationNumber = station.stationNumber


class ExpectReceive{
	def stationNumber
	def stationName
	def month
	def expected
	def received
	def variance
}

def varianceList = [];
def expectedReceived

def member
//Get Expectation Sent for this stationNumber
expectationList = delegator.findByAnd("ExpectationStationTotalSentSummary",  [month: month], null, false);


//Buid a variance list

//context.stationName = station.name;
context.month = month

expectationList.eachWithIndex { expectItem, index ->
	
//	def stationNumber
//	def stationName
//	def month
//	def expected
//	def received
//	def variance
	
	expectedReceived = new ExpectReceive();
	expectedReceived.stationNumber = expectItem.employerCode
	
	
	expectedReceived.month = month
	expectedReceived.stationNumber = expectItem.employerCode
	
	//Get Member
	employerCode = expectedReceived.stationNumber;
	stationList = delegator.findByAnd("Station",  [employerCode : employerCode], null, false);
	stationName = "";
	stationList.eachWithIndex { stationItem, stationIndex ->
		stationName = stationItem.name;
	}
	
	expectedReceived.stationName = stationName;
	
	
	expectedReceived.expected = expectItem.amount;
	
	receivedTotal = new BigDecimal(0.0);
	receivedList = delegator.findByAnd("ExpectationStationTotalReceivedSummary",  [employerCode : expectedReceived.stationNumber, month: month], null, false);
	receivedList.eachWithIndex { receivedItem, receivedItemIndex ->
		receivedTotal = receivedTotal + receivedItem.amount;
	}
	
	expectedReceived.received = receivedTotal
	
	expectedReceived.variance = expectedReceived.expected - expectedReceived.received;
	
	
	varianceList.add([stationNo:expectedReceived.stationNumber,stationName:expectedReceived.stationName,month:expectedReceived.month,
	    expected: expectedReceived.expected,received:expectedReceived.received,variance:expectedReceived.variance ]);
	
	
	//varianceList << expectedReceived
}

context.varianceList = varianceList;