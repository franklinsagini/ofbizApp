

action = request.getParameter("action");

employeeSpouselist = [];
partyId = parameters.partyId
spouse = delegator.findByAnd("StaffSpouse", [partyId : partyId], null, false);
 
 spouse.eachWithIndex { spouseItem, index ->
 
 
 name = spouseItem.getString("fullname");

 familyRelations = delegator.findOne("FamilyRelations", [familyRelationsId : spouseItem.familyRelationsId], false);
  relationship = familyRelations.getString("relationship");
 idNo = spouseItem.getString("idno");
 gender = spouseItem.getString("gender");
 dob = spouseItem.getString("dob");
 
 
 
 
 
 employeeSpouselist.add([name :name, relationship :relationship, idNo : idNo, gender :gender, dob : dob]);
 }
 
 
context.employeeSpouselist = employeeSpouselist;
