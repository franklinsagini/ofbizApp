partyId = parameters.partyId

lpartyId = partyId.toLong();

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;

expectedPaymentReceivedList = delegator.findByAnd("ExpectedPaymentReceived",  [payrollNo : payrollNo], null, false);
expectedPaymentSentList = delegator.findByAnd("ExpectedPaymentSent",  [payrollNo : payrollNo], null, false);

def combinedList = [];
def totalAmount = BigDecimal.ZERO;

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

context.combinedList = combinedList;

