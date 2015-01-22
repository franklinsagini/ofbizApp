action = request.getParameter("action");

durationBeforeIssuancelist = [];
 fileActionTypeId = parameters.fileActionTypeId;
durationBeforeIssuance = delegator.findByAnd("RegistryFileLogs", [fileActionTypeId : fileActionTypeId], null, false);
 
 durationBeforeIssuance.eachWithIndex { durationBeforeIssuanceItem, index ->
 party = durationBeforeIssuanceItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 issuer = delegator.findOne("Person", [partyId : durationBeforeIssuanceItem.actionBy], false);
 issuedto = delegator.findOne("Person", [partyId : durationBeforeIssuanceItem.currentPossesser], false);
 act = delegator.findOne("RegistryFileActivity", [activityId : durationBeforeIssuanceItem.Reason], false);
 
 fileOwner = "${member.firstName}  ${member.lastName}";
 issuedBy =  "${issuer.firstName}  ${issuer.lastName}";
 issuedTo =  "${issuedto.firstName}  ${issuedto.lastName}";
 requestReason = act.activity;
 duration = durationBeforeIssuanceItem.interActivityDuration;
 timein = durationBeforeIssuanceItem.actionDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 timeIssued = dateStringin;
 
 
 
 
 
 durationBeforeIssuancelist.add([fileOwner :fileOwner, issuedBy : issuedBy, issuedTo :issuedTo, 
 requestReason :requestReason, timeIssued : timeIssued, duration :duration]);
 }
 
 
context.durationBeforeIssuancelist = durationBeforeIssuancelist;
