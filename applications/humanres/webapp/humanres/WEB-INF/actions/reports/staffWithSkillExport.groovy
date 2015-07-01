

action = request.getParameter("action");

staffWithThisSkillslist = [];
skillTypeId = parameters.skillTypeId
 staffWithskill = delegator.findByAnd("PartySkill", [skillTypeId : skillTypeId],null, false);
 
 staffWithskill.eachWithIndex { staffWithskillItem, index ->
 
 employee = delegator.findOne("Person", [partyId : staffWithskillItem.partyId], false);
 payrollNo = employee.getString("employeeNumber");
 fname = employee.getString("firstName");
 lname = employee.getString("lastName");
 experience = staffWithskillItem.getString("yearsExperience");
 level = staffWithskillItem.getString("skillLevel");
 
 
 
 staffWithThisSkillslist.add([payrollNo :payrollNo, fname :fname, lname : lname, experience : experience, level : level]);
 }
 
 
context.staffWithThisSkillslist = staffWithThisSkillslist;
