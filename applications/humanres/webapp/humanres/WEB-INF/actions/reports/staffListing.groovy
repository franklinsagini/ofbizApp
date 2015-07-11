
staffType = parameters.isManagement
countrr=0
countrrr2=0

if (staffType) {
    staff = delegator.findByAnd("EmployeeRoleView", [isManagement : staffType, isSeparated : "N" ], null, false);
     
    staff.eachWithIndex { staffItem, index ->
    countrr=countrr + 1
     }
	
   context.staff = staff;
   context.countrr = countrr;
   return
}

employeeList = delegator.findByAnd("EmployeeRoleView", [isSeparated : "N" ],  null, false);


employeeList.eachWithIndex { staffItem, index ->
    countrrr2=countrrr2 + 1
     }

context.employeeList = employeeList;
context.countrrr2 = countrrr2;








