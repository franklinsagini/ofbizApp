partyId = parameters.partyId

lpartyId = partyId.toLong();

member = delegator.findOne("Member", [partyId : lpartyId], false);
payrollNo = member.payrollNumber;
context.member = member;

expectedPaymentReceivedList = delegator.findByAnd("ExpectedPaymentReceived",  [payrollNo : payrollNo], null, false);
expectedPaymentSentList = delegator.findByAnd("ExpectedPaymentSent",  [payrollNo : payrollNo], null, false);

expectedPaymentSentList +=expectedPaymentSentList;

context.expectedPaymentSentList = expectedPaymentSentList;

