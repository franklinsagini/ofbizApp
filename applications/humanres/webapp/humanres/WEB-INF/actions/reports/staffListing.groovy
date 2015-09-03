
staffType = parameters.isManagement

staffTypeUpper = staffType.toUpperCase();


if (staffType) {
    staff = delegator.findByAnd("EmployeeRoleView", [isManagement : staffType, isSeparated : "N" ], null, false);
     
    staff.eachWithIndex { staffItem, index ->
    
     }

   context.staffTypeUpper = staffTypeUpper;
   context.staff = staff;
   return
}

employeeList = delegator.findByAnd("EmployeeRoleView", [isSeparated : "N" ],  null, false);


employeeList.eachWithIndex { staffItem, index ->
   
     }

 context.staffTypeUpper = staffTypeUpper;
context.employeeList = employeeList;









