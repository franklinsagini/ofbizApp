

action = request.getParameter("action");

nextOfKinlist = [];
partyId = parameters.partyId
nextofkin = delegator.findByAnd("StaffNextOfKin", [partyId : partyId], null, false);
 
 nextofkin.eachWithIndex { nextofkinItem, index ->
 
 
 name = nextofkinItem.getString("fullname");
 familyRelations = delegator.findOne("FamilyRelations", [familyRelationsId : nextofkinItem.familyRelationsId], false);
 relationship = familyRelations.getString("relationship");
 idNo = nextofkinItem.getString("idno");
 phone = nextofkinItem.getString("phoneNo");
 address = nextofkinItem.getString("postalAddress");
 
 
 
 
 
 nextOfKinlist.add([name :name, relationship :relationship, idNo : idNo, phone :phone, address : address]);
 }
 
 
context.nextOfKinlist = nextOfKinlist;
