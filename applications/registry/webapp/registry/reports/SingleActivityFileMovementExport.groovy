action = request.getParameter("action");

singleActivityfileMovementlist = [];
activityCode = parameters.activityId
partyId = parameters.partyId
LpartyId = partyId.toLong();
singleActivityfileMovement = delegator.findByAnd("RegistryFileMovement", [partyId : partyId, activityCode : activityCode], null, false);
  member = delegator.findOne("Member", [partyId : LpartyId], false);
 singleActivityfileMovement.eachWithIndex { singleActivityfileMovementItem, index ->

 releasedby = delegator.findOne("Person", [partyId : singleActivityfileMovementItem.releasedBy], false);
 releasedto = delegator.findOne("Person", [partyId : singleActivityfileMovementItem.releasedTo], false);
 carriedby = delegator.findOne("Person", [partyId : singleActivityfileMovementItem.carriedBy], false);
 receivedby = delegator.findOne("Person", [partyId : singleActivityfileMovementItem.receivedBy], false);
 
 
 releasedBy = "${releasedby.firstName}  ${releasedby.lastName}";
 to = singleActivityfileMovementItem.releasedTo;
 if(to == 'REGISTRY'){
 releasedTo = "REGISTRY";
 }
 else {
 releasedTo = "${releasedto.firstName}  ${releasedto.lastName}";
 }
 
 carriedBy =  "${carriedby.firstName}  ${carriedby.lastName}";
 timeout = singleActivityfileMovementItem.timeOut;
 dateString = timeout.format("yyyy-MMM-dd HH:mm:ss a")
 timeReleased = dateString;
 receivedBy = "${receivedby.firstName}  ${receivedby.lastName}";
 timein = singleActivityfileMovementItem.timeIn;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 timeReceived = dateStringin;
 
 
 
 
 
 singleActivityfileMovementlist.add([releasedBy :releasedBy, releasedTo :releasedTo, carriedBy : carriedBy,
 timeReleased :timeReleased,  receivedBy : receivedBy, timeReceived : timeReceived]);
 }
 context.member = member;
 context.singleActivityfileMovement = singleActivityfileMovement;
context.singleActivityfileMovementlist = singleActivityfileMovementlist;
