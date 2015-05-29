

action = request.getParameter("action");

staffWithThisQualificationslist = [];
title = parameters.title
 staffWithqual = delegator.findByAnd("PartyQual", [title : title],null, false);
 
 staffWithqual.eachWithIndex { staffWithqualItem, index ->
 
  employee = delegator.findOne("Person", [partyId : staffWithqualItem.partyId], false);
 payrollNo = employee.getString("employeeNumber");
 fname = employee.getString("firstName");
 lname = employee.getString("lastName");
 specialization = staffWithqualItem.getString("specialization");
 institution = staffWithqualItem.getString("institute");
 institutionLocation = staffWithqualItem.getString("institutelocation");
 grade = staffWithqualItem.getString("grade");
 
 
 
 staffWithThisQualificationslist.add([payrollNo :payrollNo, fname :fname, lname : lname, specialization : specialization,
  institution : institution, institutionLocation : institutionLocation, grade : grade]);
 }
 
 
context.staffWithThisQualificationslist = staffWithThisQualificationslist;
