

action = request.getParameter("action");

stafflist = [];
 staff = delegator.findList("EmployeeRoleView", null, null, null,null, false);
 
 staff.eachWithIndex { staffItem, index ->
 
 payrollNo = staffItem.getString("employeeNumber");
 fname = staffItem.getString("firstName");
 lname = staffItem.getString("lastName");
 IdNo = staffItem.getString("nationalIDNumber");
 gender = staffItem.getString("gender");
 appointmentDate = staffItem.getString("appointmentdate");
 
 
 
 stafflist.add([payrollNo :payrollNo, fname :fname, lname : lname, IdNo : IdNo, gender : gender, appointmentDate : appointmentDate]);
 }
 
 
context.stafflist = stafflist;
