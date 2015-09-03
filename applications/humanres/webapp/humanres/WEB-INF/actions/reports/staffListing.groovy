
staffType = parameters.isManagement
countrr=0
countrrr2=0

staffTypeUpper = staffType.toUpperCase();


if (staffType) {
    staff = delegator.findByAnd("Person", [isManagement : staffType, isSeparated : "N" ], null, false);
     
    staff.eachWithIndex { staffItem, index ->
    countrr=countrr + 1
     }

   context.staffTypeUpper = staffTypeUpper;
   context.staff = staff;
   context.countrr = countrr;
   return
}

employeeList = delegator.findByAnd("Person", [isSeparated : "N" ],  null, false);


employeeList.eachWithIndex { staffItem, index ->
   
     }

 context.staffTypeUpper = staffTypeUpper;
context.employeeList = employeeList;
context.countrrr2 = countrrr2;








