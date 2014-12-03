partyId=parameters.partyId

context.employee = delegator.findOne("Person",[partyId:partyId], false);

context.p9List=delegator.findByAnd("P9Report", [partyId:partyId], null, false);