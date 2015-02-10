
staffType = parameters.isManagement

if (staffType) {
    staff = delegator.findByAnd("EmployeeRoleView", [isManagement : staffType], null, false);
	
   context.staff = staff;
   return
}


employeeList = delegator.findList("EmployeeRoleView", null, null, null, null, false);
context.employeeList = employeeList;












