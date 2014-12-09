partyId = parameters.partyId

lpartyId = partyId.toLong();

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;

expectedPaymentReceivedList = delegator.findByAnd("ExpectedPaymentReceived",  [payrollNo : payrollNo], null, false);
expectedPaymentSentList = delegator.findByAnd("ExpectedPaymentSent",  [payrollNo : payrollNo], null, false);

def combinedList = [];

expectedPaymentSentList.eachWithIndex { sentListValue, index ->
	sentListValue.isReceived = 'N'
	combinedList << sentListValue
	
	}

expectedPaymentReceivedList.eachWithIndex { receivedListValue, index ->
	receivedListValue.isReceived = 'Y'
	combinedList << receivedListValue
	
	}


//expectedPaymentSentList +=expectedPaymentSentList;

context.combinedList = combinedList;

