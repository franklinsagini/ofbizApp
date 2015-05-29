

action = request.getParameter("action");

staffOnProbationlist = [];
 staffOnProbation = delegator.findByAnd("Person", [employmentStatusEnumId : "15"],null, false);
 
 staffOnProbation.eachWithIndex { staffOnProbationItem, index ->
 
 branchId = staffOnProbationItem.branchId;
 departmentId = staffOnProbationItem.departmentId;
 branch = delegator.findOne("PartyGroup", [partyId : branchId], false);
 department = delegator.findOne("department", [departmentId : departmentId], false)
 
 
 payrollNo = staffOnProbationItem.getString("employeeNumber");
 fname = staffOnProbationItem.getString("firstName");
 lname = staffOnProbationItem.getString("lastName");
 gender = staffOnProbationItem.getString("gender");
 branch = branch.getString("groupName");
 department = department.getString("departmentName");
 appointmentDate = staffOnProbationItem.getString("appointmentdate");
 confirmationDate = staffOnProbationItem.getString("confirmationdate");
 
 
 
 staffOnProbationlist.add([payrollNo :payrollNo, fname :fname, lname : lname, gender : gender,
  branch : branch, department : department, appointmentDate : appointmentDate, confirmationDate : confirmationDate]);
 }
 
 
context.staffOnProbationlist = staffOnProbationlist;
