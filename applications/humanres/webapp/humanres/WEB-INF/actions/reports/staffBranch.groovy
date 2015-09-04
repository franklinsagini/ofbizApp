
staffType = parameters.branchId
dept = parameters.departmentId

staffTypeUpper = staffType.toUpperCase();

  branchName = delegator.findOne("PartyGroup",[partyId : parameters.branchId], false);  
  context.branchName = branchName;
  
 
if (staffType) {
    staff = delegator.findByAnd("Person", [branchId : staffType, isSeparated : "N" ], null, false);
     
    staff.eachWithIndex { staffItem, index ->
      
     }

   context.staffTypeUpper = staffTypeUpper;
   context.staffTypeUpper = staffTypeUpper;
   context.staff = staff;
   return
    
    }
 
 if (staffType && dept) {
  employeeList = delegator.findByAnd("Person", [branchId : staffType, departmentId : dept , isSeparated : "N" ],  null, false);
  employeeList.eachWithIndex { staffItem, index ->
  deptN= delegator.findOne("department",[departmentId : parameters.departmentId], false);
  deptName = deptN.departmentName
  deptNameUpper = deptName.toUpperCase();
  context.deptNameUpper = deptNameUpper;
   
     }
     
 context.staffTypeUpper = staffTypeUpper;
 context.staffTypeUpper = staffTypeUpper;
 context.employeeList = employeeList;

}








