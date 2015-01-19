

action = request.getParameter("action");

dependentslist = [];
partyId = parameters.partyId
dependent = delegator.findByAnd("StaffDependant", [partyId : partyId], null, false);
 
 dependent.eachWithIndex { dependentItem, index ->
 
 
 name = dependentItem.getString("fullname");
 familyRelations = delegator.findOne("FamilyRelations", [familyRelationsId : dependentItem.familyRelationsId], false);
 relationship = familyRelations.getString("relationship");
 idNo = dependentItem.getString("idno");
 gender = dependentItem.getString("gender");
 dob = dependentItem.getString("dob");
 
 
 
 
 
 dependentslist.add([name :name, relationship :relationship, idNo : idNo, gender :gender, dob : dob]);
 }
 
 
context.dependentslist = dependentslist;
