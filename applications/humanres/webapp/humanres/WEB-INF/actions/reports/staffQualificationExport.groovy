

action = request.getParameter("action");

staffQualificationslist = [];
partyId = parameters.partyId
 staffqual = delegator.findByAnd("PartyQual", [partyId : partyId], null, false);
 
 staffqual.eachWithIndex { staffqualItem, index ->
 
 
 qualName = staffqualItem.getString("partyQualTypeId");
 title = staffqualItem.getString("title");
 specialization = staffqualItem.getString("specialization");
 grade = staffqualItem.getString("grade");
 institution = staffqualItem.getString("institute");
 startDate = staffqualItem.getString("fromDate");
 toDate = staffqualItem.getString("thruDate");
 
 
 
 staffQualificationslist.add([qualName :qualName, title :title, specialization : specialization, grade : grade,
  institution : institution, startDate : startDate, toDate : toDate]);
 }
 
 
context.staffQualificationslist = staffQualificationslist;
