

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
 pin = staffItem.getString("pinNumber"); 
 nhif = staffItem.getString("nhifNumber");
 nssf = staffItem.getString("socialSecurityNumber");
 isManagement = staffItem.getString("isManagement");
 
 stafflist.add([payrollNo :payrollNo, fname :fname, lname : lname, IdNo : IdNo, gender : gender,
 pin : pin, nhif : nhif, nssf : nssf, appointmentDate : appointmentDate, isManagement : isManagement]);
 }
 
 
context.stafflist = stafflist;
