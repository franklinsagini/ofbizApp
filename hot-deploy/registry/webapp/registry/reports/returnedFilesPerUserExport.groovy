action = request.getParameter("action");

returnedFilesPerUserlist = [];
partyId = parameters.partyId
returnedFilesPerUser = delegator.findByAnd("RegistryFileLogs", [actionBy : partyId, fileActionTypeId : "Release File"], null, false);
 
 returnedFilesPerUser.eachWithIndex { returnedFilesPerUserItem, index ->
 releasedby = delegator.findOne("Person", [partyId : returnedFilesPerUserItem.actionBy], false);
 carriedby = delegator.findOne("Person", [partyId : returnedFilesPerUserItem.carriedBy], false);
 
 
 releasedBy = "${releasedby.firstName}  ${releasedby.lastName}";
 
 carriedBy =  "${carriedby.firstName}  ${carriedby.lastName}";
 timein = returnedFilesPerUserItem.actionDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 timeReleased = dateStringin;
 
 
 
 
 
 returnedFilesPerUserlist.add([releasedBy :releasedBy, carriedBy : carriedBy, timeReleased :timeReleased]);
 }
 
 
context.returnedFilesPerUserlist = returnedFilesPerUserlist;
