action = request.getParameter("action");

fileMovementlist = [];
partyId = parameters.partyId
LpartyId = partyId.toLong();
filemovement = delegator.findByAnd("RegistryFileMovement", [partyId : partyId], null, false);
 
 filemovement.eachWithIndex { filemovementItem, index ->
 member = delegator.findOne("Member", [partyId : LpartyId], false);
 releasedby = delegator.findOne("Person", [partyId : filemovementItem.releasedBy], false);
 releasedto = delegator.findOne("Person", [partyId : filemovementItem.releasedTo], false);
 carriedby = delegator.findOne("Person", [partyId : filemovementItem.carriedBy], false);
 receivedby = delegator.findOne("Person", [partyId : filemovementItem.receivedBy], false);
 act = delegator.findOne("RegistryFileActivity", [activityId : filemovementItem.activityCode], false);
 
 
 releasedBy = "${releasedby.firstName}  ${releasedby.lastName}";
 to = filemovementItem.releasedTo;
 if(to == 'REGISTRY'){
 releasedTo = "REGISTRY";
 }
 else {
 releasedTo = "${releasedto.firstName}  ${releasedto.lastName}";
 }
 
 carriedBy =  "${carriedby.firstName}  ${carriedby.lastName}";
 timeout = filemovementItem.timeOut;
 dateString = timeout.format("yyyy-MMM-dd HH:mm:ss a")
 timeReleased = dateString;
 activity = act.activity;
 receivedBy = "${receivedby.firstName}  ${receivedby.lastName}";
 timein = filemovementItem.timeIn;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 timeReceived = dateStringin;
 
 
 
 
 
 fileMovementlist.add([releasedBy :releasedBy, releasedTo :releasedTo, carriedBy : carriedBy,
 timeReleased :timeReleased, activity : activity, receivedBy : receivedBy, timeReceived : timeReceived]);
 }
 
 
context.fileMovementlist = fileMovementlist;
