

action = request.getParameter("action");

employmentHistorylist = [];
partyId = parameters.partyId
history = delegator.findByAnd("EmploymentHistory", [partyId : partyId], null, false);
 
 history.eachWithIndex { historyItem, index ->
 
 
 employer = historyItem.getString("employer");
 position = historyItem.getString("position");
 startDate = historyItem.getString("startDate");
 toDate = historyItem.getString("endDate");
 separateReason = historyItem.getString("reason");
 
 
 
 
 
 employmentHistorylist.add([employer :employer, position :position, startDate : startDate, toDate :toDate, separateReason : separateReason]);
 }
 
 
context.employmentHistorylist = employmentHistorylist;
